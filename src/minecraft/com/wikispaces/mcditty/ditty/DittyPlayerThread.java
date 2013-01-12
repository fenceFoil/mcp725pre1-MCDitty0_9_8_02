/**
 * 
 * Copyright (c) 2012 William Karnavas All Rights Reserved
 * 
 */

/**
 * 
 * This file is part of MCDitty.
 * 
 * MCDitty is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * MCDitty is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MCDitty. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.wikispaces.mcditty.ditty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Patch;
import javax.sound.midi.Synthesizer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import net.minecraft.src.BlockSign;

import org.jfugue.MidiRenderer;
import org.jfugue.Player;
import org.jfugue.elements.Lyric;
import org.jfugue.elements.MCDittyEvent;
import org.jfugue.elements.Note;
import org.jfugue.elements.Tempo;

import paulscode.sound.SoundBuffer;
import paulscode.sound.codecs.CodecJOrbis;

import com.sun.media.sound.SF2Instrument;
import com.sun.media.sound.SF2InstrumentRegion;
import com.sun.media.sound.SF2Layer;
import com.sun.media.sound.SF2LayerRegion;
import com.sun.media.sound.SF2Region;
import com.sun.media.sound.SF2Sample;
import com.sun.media.sound.SF2Soundbank;
import com.sun.media.sound.SoftSynthesizer;
import com.wikispaces.mcditty.MCDitty;
import com.wikispaces.mcditty.config.MCDittyConfig;
import com.wikispaces.mcditty.ditty.event.CreateEmitterEvent;
import com.wikispaces.mcditty.ditty.event.DittyEndedEvent;
import com.wikispaces.mcditty.ditty.event.HighlightSignPlayingEvent;
import com.wikispaces.mcditty.ditty.event.NoteStartEvent;
import com.wikispaces.mcditty.ditty.event.PlayMidiDittyEvent;
import com.wikispaces.mcditty.ditty.event.SFXInstrumentEvent;
import com.wikispaces.mcditty.ditty.event.SFXInstrumentOffEvent;
import com.wikispaces.mcditty.ditty.event.SFXMCDittyEvent;
import com.wikispaces.mcditty.ditty.event.SignPlayingTimedEvent;
import com.wikispaces.mcditty.ditty.event.TempoDittyEvent;
import com.wikispaces.mcditty.ditty.event.TimedDittyEvent;
import com.wikispaces.mcditty.ditty.event.VolumeEvent;
import com.wikispaces.mcditty.sfx.SFXManager;
import com.wikispaces.mcditty.signs.SignDitty;

/**
 * Instantiate once per Ditty played; these are one use only. Plays a Ditty.
 */

public class DittyPlayerThread extends Thread implements
		DittyPlayerPlayingHookListener, LyricEventReadListener,
		TempoEventReadListener, MCDittyEventReadListener,
		ParticleWorthyNoteReadListener, InstrumentEventReadListener {

	private boolean muting = false;

	private static SoftSynthPool synthPool;

	public static ConcurrentLinkedQueue<DittyPlayerThread> jFuguePlayerThreads = new ConcurrentLinkedQueue<DittyPlayerThread>();

	private Player player;

	private Ditty ditty;

	/**
	 * The Instruments loaded into the synthesizer after it is set up with
	 * soundbanks for the ditty. Used by SFXInstruments to revert when they are
	 * turned off.
	 */
	private Instrument[] originalSynthInstruments;

	public static Object staticPlayerMutex = new Object();
	private static Object staticPlayerMutex2 = new Object();

	public DittyPlayerThread(Ditty ditty) {
		this.ditty = ditty;
		setName("Ditty Player");
		
		// Set up synthpool if not done already
		setUpSynthPool();
	}

	public static void setUpSynthPool() {
		if (synthPool == null) {
			synthPool = new SoftSynthPool();
			synthPool.start();
		}
	}

	// public void setDitty(Ditty prop) {
	// dittyProperties = prop;
	// }

	@Override
	public void run() {

		jFuguePlayerThreads.add(this);

		if (ditty instanceof SignDitty) {
			// Add this tune to the list of oneatatime ditties
			if (((SignDitty) ditty).isOneAtATime()) {
				synchronized (BlockSign.oneAtATimeSignsBlocked) {
					BlockSign.oneAtATimeSignsBlocked.add(((SignDitty) ditty)
							.getStartPoint());
				}
			}
		}

		// Finalize the ditty's lyrics
		ditty.getLyricsStorage().finalizeLyrics();

		synchronized (staticPlayerMutex) {
			player = setUpPlayer();
			// Register this thread as receiving hook events once the music
			// starts playing
			player.addHookListener(this);

			// Register this thread to get lyrics info as it is read
			player.getRenderer().setLyricEventReadListener(this);
			// And tempo info
			player.getRenderer().setTempoEventReadListener(this);
			// And MCDitty events!
			player.getRenderer().setMcdittyEventReadListener(this);
			// And Particle Worthy Note Events!!! :) @_@
			player.getRenderer().setParticleWorthyNoteReadListener(this);
			// AND EVEN INSTRUMENT EVENTS!!11!!11!1 UPVOTES!
			player.getRenderer().setInstrumentEventReadListener(this);
		}

		BlockSign.simpleLog("Starting ditty!");
		if (!muting) {
			try {
				synchronized (staticPlayerMutex) {
					this.sleep(20);
				}
				player.play(ditty.getMusicString());
				if (!muting) {
					// Flush events at end of song
					for (int z = 0; z < 2; z++) {
						playerHook(lastTimeChecked + 10000000, lastTempo);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		BlockSign.simpleLog("Done playing a MusicString");
		try {
			synchronized (staticPlayerMutex) {
				player.close();
				if (synth != null && synth.isOpen()) {
					synth.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Fire dittyEnded event
		MCDitty.executeTimedDittyEvent(new DittyEndedEvent(muting, ditty
				.getDittyID()));

		// Remove this thread from the list of player threads currently running
		jFuguePlayerThreads.remove(this);

		if (ditty instanceof SignDitty) {
			// Remove this tune from the list of blocked tunes
			// (Loop until all copies are removed)
			synchronized (BlockSign.oneAtATimeSignsBlocked) {
				while (BlockSign.oneAtATimeSignsBlocked
						.remove(((SignDitty) ditty).getStartPoint()))
					;
			}
		}

		// Turn off muting flag
		muting = false;
	}

	private Player setUpPlayer() {
		synth = synthPool.getOpenedSynth();
		Player p = null;
		try {
			p = new Player(synth);
		} catch (MidiUnavailableException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// Note the instruments that are now loaded into the synthesizer at the
		// start of the song (for things that replace them, like SFXInstruments)
		originalSynthInstruments = synth.getLoadedInstruments();
		return p;
	}

	public boolean isMuting() {
		return muting;
	}

	private void setMuting(boolean muting) {
		this.muting = muting;
	}

	/**
	 * Stops currently running JFuguePlayerThread.
	 */
	public void mute() {
		if (!muting) {
			setMuting(true);
			try {
				synchronized (staticPlayerMutex2) {
					if (player != null) {
						player.stop();
						this.sleep(20);
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// private static long startMS = 0;

	@Override
	public void initPlayerHook() {
		// All lyrics have been read in. Add LyricStorage's events to the
		// DittyProperties
		ditty.dumpLyricsStorageToDittyEvents();
	}

	// For playerHook
	private long lastTimeChecked = 0;
	private float lastTempo = 0.0f;

	private Synthesizer synth;

	@Override
	public void playerHook(long time, float tempo) {
		if (MCDittyConfig.debug) {
			BlockSign.simpleLog("playerHook() called: time=" + time);
		}

		MCDitty.onDittyTick(ditty.getDittyID(), time);

		// Check for / fire ditty events
		while (true) {
			TimedDittyEvent nextEventToFire = ditty
					.getNextDittyEventAtTime(time);
			if (nextEventToFire == null) {
				// No more events to fire right now
				break;
			} else {
				// Time to fire this event!
				// Add it to the queue
				if (nextEventToFire.isExecutedAtPlayerLevel()) {
					// Requires something at a low level, like an instrument
					// change with player.getSynthesizer()
					if (nextEventToFire instanceof SFXInstrumentEvent) {
						try {
							loadSFXInstrument(
									(SFXInstrumentEvent) nextEventToFire, synth);
						} catch (UnsupportedAudioFileException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (nextEventToFire instanceof SFXInstrumentOffEvent) {
						unloadSFXInstrument(
								(SFXInstrumentOffEvent) nextEventToFire, synth);
					}
				} else {
					// Pass it on to the queue with access to the world
					MCDitty.executeTimedDittyEvent(nextEventToFire);
				}
			}
		}
		lastTimeChecked = time;

		if (lastTempo != tempo) {
			MCDitty.updateDittyTempo(ditty.getDittyID(), tempo);
			lastTempo = tempo;
		}
	}

	/**
	 * A list of SFX instruments prepared before the ditty began, ready to be
	 * loaded by the synthesizer
	 */
	private HashMap<Integer, SF2Soundbank> cachedSFXInstruments = new HashMap<Integer, SF2Soundbank>();

	/**
	 * Create a SF2Soundbank for the given SFXInst event before the ditty
	 * starts, to cut delay during the ditty.
	 * 
	 * TODO: Even more efficiency could come from intelligent re-use of
	 * Soundbanks for duplicate SFXInst calls.
	 * 
	 * TODO: Clean up code.
	 * 
	 * @param timedEvent
	 */
	private void cacheSFXInstrument(SFXInstrumentEvent timedEvent) {
		/*
		 * Create new SoundFont2 soundbank
		 */
		SF2Soundbank sf2 = new SF2Soundbank();

		/*
		 * Select audio file.
		 */
		File audioFile = SFXManager.getEffectFile(
				SFXManager.getAllEffects(timedEvent.getSfxSource()).get(
						timedEvent.getSfxName()), timedEvent.getSfxNumber(),
				timedEvent.getSfxSource());

		CodecJOrbis codec = new CodecJOrbis();
		try {
			codec.initialize(audioFile.toURI().toURL());
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		SoundBuffer readAudio = codec.readAll();
		codec.cleanup();

		/*
		 * Convert the decoded audio into the correct format for soundfonts
		 * e.g.16 bit signed, little endian
		 * 
		 * (In practice, this usually seems to involve simply toggling the
		 * "endian-ness")
		 */
		AudioFormat sf2Format = new AudioFormat(
				readAudio.audioFormat.getSampleRate(), 16,
				readAudio.audioFormat.getChannels(), true, false);
		AudioInputStream audioStream = new AudioInputStream(
				new ByteArrayInputStream(readAudio.audioData),
				readAudio.audioFormat, readAudio.audioData.length);
		AudioInputStream converterAudioStream = AudioSystem
				.getAudioInputStream(sf2Format, audioStream);

		// Read converted sample into a byte buffer
		ByteArrayOutputStream sounddataOutputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[256 * 16];
		int bytesRead = 0;

		while (true) {
			// Try to read a bufferfull
			try {
				bytesRead = converterAudioStream.read(buffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// If bytes were read, add them to the audio data
			if (bytesRead > 0) {
				sounddataOutputStream.write(buffer, 0, bytesRead);
			}

			// If an incomplete buffer was read or the end of the stream was
			// reached
			if (bytesRead < buffer.length) {
				break;
			}
		}

		// Release resources
		try {
			converterAudioStream.close();
			sounddataOutputStream.close();
			audioStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[] convertedSoundData = sounddataOutputStream.toByteArray();

		/*
		 * Create SoundFont2 sample.
		 */
		SF2Sample sample = new SF2Sample(sf2);
		sample.setName(timedEvent.getSfxName() + timedEvent.getSfxNumber()
				+ " Sample");
		sample.setData(convertedSoundData);
		sample.setSampleRate((long) readAudio.audioFormat.getSampleRate());
		sample.setOriginalPitch(timedEvent.getCenterPitch());
		sf2.addResource(sample);

		/*
		 * Create SoundFont2 layer.
		 */
		SF2Layer layer = new SF2Layer(sf2);
		layer.setName(timedEvent.getSfxName() + timedEvent.getSfxNumber()
				+ " Layer");
		sf2.addResource(layer);

		/*
		 * Create region for layer.
		 */
		SF2LayerRegion region = new SF2LayerRegion();
		region.putInteger(SF2Region.GENERATOR_RELEASEVOLENV, 12000);
		region.setSample(sample);
		layer.getRegions().add(region);

		/*
		 * Create SoundFont2 instrument.
		 */
		SF2Instrument ins = new SF2Instrument(sf2);
		ins.setName(timedEvent.getSfxName() + timedEvent.getSfxNumber()
				+ " Instrument");
		ins.setPatch(new Patch(0, timedEvent.getInstrument()));
		sf2.addInstrument(ins);

		/*
		 * Create region for instrument.
		 */
		SF2InstrumentRegion insregion = new SF2InstrumentRegion();
		insregion.setLayer(layer);
		ins.getRegions().add(insregion);

		// Save SF2 instrument to cache
		cachedSFXInstruments.put(timedEvent.getEventID(), sf2);

		// Cleanup read audio for sample
		readAudio.cleanup();
	}

	/**
	 * 
	 * @param nextEventToFire
	 * @throws IOException
	 * @throws UnsupportedAudioFileException
	 */
	private void loadSFXInstrument(SFXInstrumentEvent event, Synthesizer synth)
			throws UnsupportedAudioFileException, IOException {
		// Select sound bank from cache
		SF2Soundbank sf2 = cachedSFXInstruments.get(event.getEventID());

		// Load instrument
		synth.loadInstrument(sf2.getInstruments()[0]);

		// Debug
		// System.out.println(sf2.getInstruments()[0].getName());
		for (SF2Instrument i : sf2.getInstruments()) {
			if (MCDittyConfig.debug) {
				BlockSign.simpleLog(i.getPatch() + ": " + i.getName() + ",");
			}
		}
	}

	private void unloadSFXInstrument(SFXInstrumentOffEvent event,
			Synthesizer synth2) {
		Instrument restoreInstrument = null;

		System.out.println("8");

		// Find the instrument to restore
		for (Instrument i : originalSynthInstruments) {
			System.out.println(i.getName());
			if (i.getPatch().getBank() == 0
					&& i.getPatch().getProgram() == event.getInstrument()) {
				// Match found
				restoreInstrument = i;
				break;
			}
		}

		if (restoreInstrument != null) {
			// Load it into the synthesizer again
			System.out.println("9");
			synth2.loadInstrument(restoreInstrument);
		}
	}

	@Override
	public void lyricEventRead(Lyric lyric, long time) {
		// Add lyric event to lyric index
		if (MCDittyConfig.debug) {
			BlockSign.simpleLog("JFuguePlayerThread.lyricReadEvent: "
					+ lyric.getVerifyString() + " t=" + time);
		}

		// Set lyric time (in PPQ)
		ditty.getLyricsStorage().setLyricTime(lyric.getLyricLabel(), time);
	}

	/**
	 * TODO: Decide whether to remove this tempo-listening capability to save
	 * time as it is now useless.
	 */
	@Override
	public void tempoEventRead(Tempo tempo, long time) {
		if (MCDittyConfig.debug) {
			BlockSign.simpleLog("JFuguePlayerThread.tempoEventRead: "
					+ tempo.getVerifyString() + " t=" + time);
		}
		ditty.addDittyEvent(new TempoDittyEvent(ditty.getDittyID(), time, tempo
				.getTempo()));
	}

	@Override
	public void fireParticleWorthyNoteEvent(long time, byte voice, byte layer,
			Note note, int currentSignID) {
		if (ditty instanceof SignDitty) {
			ditty.addDittyEvent(new NoteStartEvent(((SignDitty) ditty)
					.getPointForID(currentSignID), time, voice, layer, note,
					ditty.getDittyID()));
		}
	}

	@Override
	public void mcDittyEventRead(MCDittyEvent event, long time, byte voice,
			byte layer) {
		if (MCDittyConfig.debug) {
			System.out.println("MCDitty Event Read! " + event.toString()
					+ " @ " + time);
		}

		if (event.getToken().startsWith(BlockSign.TIMED_EVENT_TOKEN)) {
			// System.out.println("MCDitty Event Read! " + event.toString()
			// + " @ " + time);
			// Get the event denoted by this token
			String eventIDString = event.getToken().replaceAll(
					BlockSign.TIMED_EVENT_TOKEN, "");
			int eventID = Integer.parseInt(eventIDString);
			TimedDittyEvent timedEvent = ditty.getDittyEvent(eventID);
			// System.out.println("Found event: " + timedEvent);
			if (timedEvent == null) {
				// Just to be safe.
				return;
			}

			// Do things depending on type of event
			if (timedEvent instanceof SignPlayingTimedEvent
					&& ditty instanceof SignDitty) {
				// NOTE: CURRENTLY REVERTED TO USING ~a and ~b tokens instead of
				// using this code
				SignPlayingTimedEvent spte = (SignPlayingTimedEvent) timedEvent;
				ditty.addDittyEvent(new HighlightSignPlayingEvent(
						((SignDitty) ditty).getPointForID(spte.getSignID()),
						spte.getStartPlaying(), time, ditty.getDittyID()));
			} else if (timedEvent instanceof PlayMidiDittyEvent) {
				timedEvent.setTime(time);
				timedEvent.setVoice(voice);
			} else if (timedEvent instanceof SFXMCDittyEvent) {
				timedEvent.setTime(time);
				timedEvent.setVoice(voice);
			} else if (timedEvent instanceof VolumeEvent) {
				timedEvent.setTime(time);
				timedEvent.setVoice(voice);
			} else if (timedEvent instanceof CreateEmitterEvent) {
				timedEvent.setTime(time);
				timedEvent.setVoice(voice);
			} else if (timedEvent instanceof SFXInstrumentEvent) {
				// Load the SFX instrument before the song begins so that it can
				// be applied as quickly as possible at runtime
				cacheSFXInstrument((SFXInstrumentEvent) timedEvent);
				timedEvent.setTime(Math.max(time - 4, 0));
				timedEvent.setVoice(voice);
				// Force full attack and decay
				MidiRenderer r = player.getRenderer();
				r.setForceFullAttackDecay(
						((SFXInstrumentEvent) timedEvent).getInstrument(), true);
			} else if (timedEvent instanceof SFXInstrumentOffEvent) {
				// Execute a short time ahead of normal to give synth time to
				// restore old instrument
				timedEvent.setTime(Math.max(time - 4, 0));
				timedEvent.setVoice(voice);
				// Turn off forcing full attack and decay
				MidiRenderer r = player.getRenderer();
				r.setForceFullAttackDecay(
						((SFXInstrumentOffEvent) timedEvent).getInstrument(),
						false);
			} else {
				timedEvent.setTime(time);
				timedEvent.setVoice(voice);
			}
		}

		/*
		 * Handle different types of MCDitty tokens, besides standard event
		 * tokens.
		 * 
		 * This area should be getting smaller as the transition to MCDitty
		 * TimedEvents, which only require a ~E token with an index number (as
		 * opposed to a different token for different types of events)
		 * continues.
		 */
		if (event.getToken().startsWith(BlockSign.SIGN_START_TOKEN)
				&& ditty instanceof SignDitty) {
			ditty.addDittyEvent(new HighlightSignPlayingEvent(
					((SignDitty) ditty).getPointForID(Integer.parseInt(event
							.getToken().substring(
									BlockSign.SIGN_START_TOKEN.length()))),
					true, time, ditty.getDittyID()));
		} else if (event.getToken().startsWith(BlockSign.SIGN_END_TOKEN)
				&& ditty instanceof SignDitty) {
			ditty.addDittyEvent(new HighlightSignPlayingEvent(
					((SignDitty) ditty).getPointForID(Integer.parseInt(event
							.getToken().substring(
									BlockSign.SIGN_START_TOKEN.length()))),
					false, time, ditty.getDittyID()));
		}

	}

	public class ByteBufferBackedInputStream extends InputStream {

		ByteBuffer buf;

		public ByteBufferBackedInputStream(ByteBuffer buf) {
			this.buf = buf;
		}

		public synchronized int read() throws IOException {
			if (!buf.hasRemaining()) {
				return -1;
			}
			return buf.get() & 0xFF;
		}

		public synchronized int read(byte[] bytes, int off, int len)
				throws IOException {
			if (!buf.hasRemaining()) {
				return -1;
			}

			len = Math.min(len, buf.remaining());
			buf.get(bytes, off, len);
			return len;
		}
	}

	@Override
	public void instrumentUsed(byte instrument) {
		HashSet<Byte> used = ditty.getInstrumentUsed();
		if (!used.contains(new Byte(instrument))) {
			used.add(new Byte(instrument));
			// Try to load the instrument into the synthesizer
			SF2Soundbank soundbank = MCDittyConfig.customSF2
					.getCachedSoundbank();
			if (soundbank != null) {
				Instrument[] instruments = soundbank.getInstruments();
				for (Instrument i : instruments) {
					if (i.getPatch().getBank() == 0
							&& i.getPatch().getProgram() == instrument) {
						// Instrument found.
						// Load it
						synth.loadInstrument(i);
						// System.out.println(i.getName());
						break;
					}
				}
			}
		}
	}

	public Ditty getDitty() {
		return ditty;
	}
}
