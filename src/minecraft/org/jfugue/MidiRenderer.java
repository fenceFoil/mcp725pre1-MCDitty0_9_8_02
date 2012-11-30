/*
 * JFugue - API for Music Programming
 * Copyright (C) 2003-2008  David Koelle
 *
 * http://www.jfugue.org 
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *  
 */

package org.jfugue;

import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;

import net.minecraft.src.BlockSign;

import org.jfugue.elements.ChannelPressure;
import org.jfugue.elements.Controller;
import org.jfugue.elements.Instrument;
import org.jfugue.elements.KeySignature;
import org.jfugue.elements.Layer;
import org.jfugue.elements.Lyric;
import org.jfugue.elements.MCDittyEvent;
import org.jfugue.elements.Measure;
import org.jfugue.elements.Note;
import org.jfugue.elements.PitchBend;
import org.jfugue.elements.PolyphonicPressure;
import org.jfugue.elements.SystemExclusive;
import org.jfugue.elements.Tempo;
import org.jfugue.elements.Time;
import org.jfugue.elements.Voice;

import com.wikispaces.mcditty.config.MCDittyConfig;
import com.wikispaces.mcditty.ditty.InstrumentEventReadListener;
import com.wikispaces.mcditty.ditty.LyricEventReadListener;
import com.wikispaces.mcditty.ditty.MCDittyEventReadListener;
import com.wikispaces.mcditty.ditty.ParticleWorthyNoteReadListener;
import com.wikispaces.mcditty.ditty.TempoEventReadListener;

/**
 * This class takes a Pattern, and turns it into wonderful music.
 * 
 * <p>
 * Playing music is only one thing that can be done by rendering a pattern. You
 * could also create your own renderer that draws sheet music based on a
 * pattern. Or, you could create a graphical light show based on the musical
 * notes in the pattern.
 * </p>
 * 
 * <p>
 * This was named Renderer in previous versions of JFugue. The name has been
 * changed to differentiate it from other types of renderers.
 * </p>
 * 
 * @author David Koelle
 * @version 2.0
 * @version 3.0 - Renderer renamed to MidiRenderer
 */
public final class MidiRenderer extends ParserListenerAdapter {
	private MidiEventManager eventManager;
	double initialNoteTime = 0;
	private float sequenceTiming;
	private int resolution;

	// MCDITTY
	private LyricEventReadListener lyricEventReadListener = null;
	private TempoEventReadListener tempoEventReadListener = null;
	private MCDittyEventReadListener mcdittyEventReadListener = null;
	private ParticleWorthyNoteReadListener particleWorthyNoteReadListener = null;
	private InstrumentEventReadListener instrumentEventReadListener;

	/**
	 * Whether to force the notes in [index] instrument to be played at full
	 * volume.
	 */
	private boolean[] forceInstrumentFullAttackDecay = new boolean[128];

	private byte currentTrack = 0;

	/**
	 * Current instrument per track
	 */
	private byte[] currentInstrument = new byte[16];

	// MCDitty
	private int currentSignID = 0;
	private byte currentLayer = 0;

	/**
	 * Staccato settings for each track; 0 = off, 1-8 = x/8 note length, 9 = 1
	 * tick.
	 */
	private int[] staccato = new int[16];

	/**
	 * How many ticks of staccato are left for each track; -1 means on
	 * indefinitely
	 */
	private Double[] staccatoEnd = new Double[16];

	/**
	 * Instantiates a Renderer
	 */
	public MidiRenderer(float sequenceTiming, int resolution) {
		reset(sequenceTiming, resolution);
		for (int i = 0; i < forceInstrumentFullAttackDecay.length; i++) {
			forceInstrumentFullAttackDecay[i] = false;
		}
	}

	/**
	 * Creates a new MidiEventManager. If this isn't called, events from
	 * multiple calls to render() will be added to the same eventManager, which
	 * means that the second time render() is called, it will contain music left
	 * over from the first time it was called. (This wasn't a problem with Java
	 * 1.4)
	 * 
	 * @since 3.0
	 */
	public void reset(float sequenceTiming, int resolution) {
		this.sequenceTiming = sequenceTiming;
		this.resolution = resolution;
		this.eventManager = new MidiEventManager(sequenceTiming, resolution);
	}

	/**
	 * Creates a new MidiEventManager using the sequenceTiming and resolution
	 * already used to create this MidiRenderer. If this isn't called, events
	 * from multiple calls to render() will be added to the same eventManager,
	 * which means that the second time render() is called, it will contain
	 * music left over from the first time it was called. (This wasn't a problem
	 * with Java 1.4)
	 * 
	 * @since 3.2
	 */
	public void reset() {
		this.eventManager = new MidiEventManager(this.sequenceTiming,
				this.resolution);
	}

	/**
	 * Returns the last sequence generated by this renderer
	 */
	public Sequence getSequence() {
		return this.eventManager.getSequence();
	}

	// ParserListener methods
	// //////////////////////////

	public void voiceEvent(Voice voice) {
		// TODO: Add again
		// MCDitty: End current sign
		mcDittyEvent(new MCDittyEvent(BlockSign.SIGN_END_TOKEN + currentSignID));

		// Original code (the only line left!)
		this.eventManager.setCurrentTrack(voice.getVoice());

		// MCDITTY
		currentTrack = voice.getVoice();

		// TODO: Add again
		// Reset current sign location
		mcDittyEvent(new MCDittyEvent(BlockSign.SIGN_START_TOKEN
				+ currentSignID));
	}

	public void tempoEvent(Tempo tempo) {
		byte[] threeTempoBytes = TimeFactor.convertBPMToBytes(tempo.getTempo());
		
		// Add event to track 0 at the current track's time
		// (Non-track 0 tempo changes upset the synth: it ignores them)
		// Note that there is no harm in doing this even when we're at track 0 already.
		
		// note the time we want the event at
		double currTrackTime = eventManager.getTrackTimerDec();
		// switch to track 0
		eventManager.setCurrentTrack((byte) 0);
		// note the track 0 time
		double oldTrack0Time = eventManager.getTrackTimerDec();
		// move track 0 to the desired time, and add the event
		eventManager.setTrackTimerDec(currTrackTime);
		this.eventManager.addMetaMessage(0x51, threeTempoBytes);
		// move track 0 back to where it was
		eventManager.setTrackTimerDec(oldTrack0Time);
		// switch back to the current track
		eventManager.setCurrentTrack(currentTrack);

//		// MCDitty
//		// Note: For some reason, only "Txxx" tokens in the first track (any
//		// layer) are counted when played back.
//		// Therefore, MCDitty must explicitly do the same thing that JFugue does
//		// implicitly.
//		if (currentTrack == 0) {
//			if (tempoEventReadListener != null) {
//				long now = (long) eventManager.getTrackTimerDec();
//				tempoEventReadListener.tempoEventRead(tempo, now);
//			}
//		}
	}

	public void instrumentEvent(Instrument instrument) {
		this.eventManager.addEvent(ShortMessage.PROGRAM_CHANGE,
				instrument.getInstrument(), 0);
		
		// MCDitty: note instrument change
		currentInstrument[currentTrack] = instrument.getInstrument();
		// Relay to listeners
		if (instrumentEventReadListener != null) {
			instrumentEventReadListener.instrumentUsed(instrument
					.getInstrument());
		}
	}

	public void layerEvent(Layer layer) {
		// TODO: Add again
		// MCDitty: End current sign
		mcDittyEvent(new MCDittyEvent(BlockSign.SIGN_END_TOKEN + currentSignID));

		this.eventManager.setCurrentLayer(layer.getLayer());

		// MCDITTY
		currentLayer = layer.getLayer();

		// TODO: Add again
		// Reset current sign location
		mcDittyEvent(new MCDittyEvent(BlockSign.SIGN_START_TOKEN
				+ currentSignID));
	}

	public void timeEvent(Time time) {
		this.eventManager.setTrackTimerDec(time.getTime());
	}

	public void measureEvent(Measure measure) {
		// No MIDI is generated when a measure indicator is identified.
	}

	public void keySignatureEvent(KeySignature keySig) {
		this.eventManager.addMetaMessage(0x59, new byte[] { keySig.getKeySig(),
				keySig.getScale() });
	}

	public void systemExclusiveEvent(SystemExclusive systemExclusiveEvent) {
		this.eventManager.addSystemExclusiveEvent(systemExclusiveEvent
				.getBytes());
	}

	public void controllerEvent(Controller controller) {
		this.eventManager.addEvent(ShortMessage.CONTROL_CHANGE,
				controller.getIndex(), controller.getValue());
	}

	public void channelPressureEvent(ChannelPressure channelPressure) {
		this.eventManager.addEvent(ShortMessage.CHANNEL_PRESSURE,
				channelPressure.getPressure());
	}

	public void polyphonicPressureEvent(PolyphonicPressure polyphonicPressure) {
		this.eventManager.addEvent(ShortMessage.POLY_PRESSURE,
				polyphonicPressure.getKey(), polyphonicPressure.getPressure());
	}

	public void pitchBendEvent(PitchBend pitchBend) {
		this.eventManager.addEvent(ShortMessage.PITCH_BEND,
				pitchBend.getBend()[0], pitchBend.getBend()[1]);
	}

	public void noteEvent(Note note) {
		// Remember the current track time, so we can flip back to it
		// if there are other notes to play in parallel
		this.initialNoteTime = this.eventManager.getTrackTimerDec();
		// long duration = note.getMillisDuration();
		double duration = note.getDecimalDuration() * (double) resolution;

		// If there is no duration, don't add this note to the event manager
		// TODO: This is a special case as of v4.0.3 that should be re-thought
		// if a new noteEvent callback is created in v5.0
		if (duration == 0) {
			return;
		}

		emitParticleForNote(note);

		// Add messages to the track
		if (note.isRest()) {
			this.eventManager.addRest(duration);
		} else {
			initialNoteTime = eventManager.getTrackTimerDec();
			byte attackVelocity = note.getAttackVelocity();
			byte decayVelocity = note.getDecayVelocity();
			// MCDitty: Apply forced full attack and decay
			// System.out.println ("Forced full attack & decay");
			if (forceInstrumentFullAttackDecay[currentInstrument[currentTrack]]) {
				// System.out.println
				// (currentTrack+","+note.getMusicString()+": Forcing full attack/decay");
				attackVelocity = 127;
				decayVelocity = 127;
			}

			// Apply staccato
			if (staccato[currentTrack] > 0
					&& eventManager.getTrackTimerDec() < staccatoEnd[currentTrack]) {
				double staccadoDuration = duration;
				if (staccato[currentTrack] == 9) {
					// one tick
					staccadoDuration = 1;
				} else if (staccato[currentTrack] >= 1
						&& staccato[currentTrack] <= 8) {
					staccadoDuration = (1d / 8d) * duration
							* (double) staccato[currentTrack];
				}

				// Add a note, then a rest
				this.eventManager.addNoteEvent(note.getValue(), attackVelocity,
						decayVelocity, staccadoDuration, !note.isEndOfTie(),
						!note.isStartOfTie());
				this.eventManager.addRest(duration - staccadoDuration);
			} else {
				// No staccato
				this.eventManager.addNoteEvent(note.getValue(), attackVelocity,
						decayVelocity, duration, !note.isEndOfTie(),
						!note.isStartOfTie());
			}
		}
	}

	public void sequentialNoteEvent(Note note) {
		emitParticleForNote(note);

		// long duration = note.getMillisDuration();
		double duration = note.getDecimalDuration() * (double) resolution;
		if (note.isRest()) {
			this.eventManager.addRest(duration);
		} else {
			byte attackVelocity = note.getAttackVelocity();
			byte decayVelocity = note.getDecayVelocity();
			// MCDitty: Apply forced full attack and decay
			if (forceInstrumentFullAttackDecay[currentInstrument[currentTrack]]) {
				attackVelocity = 127;
				decayVelocity = 127;
			}
			// Apply staccato
			if (staccato[currentTrack] > 0
					&& eventManager.getTrackTimerDec() < staccatoEnd[currentTrack]) {
				double staccadoDuration = duration;
				if (staccato[currentTrack] == 9) {
					// one tick
					staccadoDuration = 1;
				} else if (staccato[currentTrack] >= 1
						&& staccato[currentTrack] <= 8) {
					staccadoDuration = (1d / 8d) * duration
							* (double) staccato[currentTrack];
				}

				// Add a note, then a rest
				this.eventManager.addNoteEvent(note.getValue(), attackVelocity,
						decayVelocity, staccadoDuration, !note.isEndOfTie(),
						!note.isStartOfTie());
				this.eventManager.addRest(duration - staccadoDuration);
			} else {
				// No staccato
				this.eventManager.addNoteEvent(note.getValue(), attackVelocity,
						decayVelocity, duration, !note.isEndOfTie(),
						!note.isStartOfTie());
			}
		}
	}

	public void parallelNoteEvent(Note note) {
		// long duration = note.getMillisDuration();
		double duration = note.getDecimalDuration() * (double) resolution;
		this.eventManager.setTrackTimerDec(this.initialNoteTime);

		emitParticleForNote(note);

		if (note.isRest()) {
			this.eventManager.addRest(duration);
		} else {
			byte attackVelocity = note.getAttackVelocity();
			byte decayVelocity = note.getDecayVelocity();
			// MCDitty: Apply forced full attack and decay
			if (forceInstrumentFullAttackDecay[currentInstrument[currentTrack]]) {
				attackVelocity = 127;
				decayVelocity = 127;
			}

			// Apply staccato
			if (staccato[currentTrack] > 0
					&& eventManager.getTrackTimerDec() < staccatoEnd[currentTrack]) {
				double staccadoDuration = duration;
				if (staccato[currentTrack] == 9) {
					// one tick
					staccadoDuration = 1;
				} else if (staccato[currentTrack] >= 1
						&& staccato[currentTrack] <= 8) {
					staccadoDuration = (1d / 8d) * duration
							* (double) staccato[currentTrack];
				}

				// Add a note, then a rest
				this.eventManager.addNoteEvent(note.getValue(), attackVelocity,
						decayVelocity, staccadoDuration, !note.isEndOfTie(),
						!note.isStartOfTie());
				this.eventManager.addRest(duration - staccadoDuration);
			} else {
				// No staccato
				this.eventManager.addNoteEvent(note.getValue(), attackVelocity,
						decayVelocity, duration, !note.isEndOfTie(),
						!note.isStartOfTie());
			}
		}
	}

	private void emitParticleForNote(Note note) {
		long now = (long) eventManager.getTrackTimerDec();
		if (!note.isRest() && !note.isEndOfTie()
				&& particleWorthyNoteReadListener != null) {
			particleWorthyNoteReadListener.fireParticleWorthyNoteEvent(now,
					currentTrack, currentLayer, note, currentSignID);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jfugue.ParserListenerAdapter#mcDittyEvent(org.jfugue.parsers.MCDittyEvent
	 * )
	 */
	@Override
	public void mcDittyEvent(MCDittyEvent event) {
		if (MCDittyConfig.debug) {
			System.out.println("MidiRenderer: MCDittyEvent hit: "
					+ event.getVerifyString());
		}
		
		// Actually do something
		if (event.getToken().equalsIgnoreCase(BlockSign.SYNC_VOICES_TOKEN)) {
			eventManager.alignChannelTimes();
		} else if (event.getToken().toLowerCase()
				.startsWith(BlockSign.SYNC_WITH_TOKEN.toLowerCase())) {
			// Parse token
			String arguments = event.getToken().substring(
					BlockSign.SYNC_WITH_TOKEN.length());

			// Get voice
			String voiceNum = arguments.substring(1, 3);
			int voice = 0;
			if (voiceNum.matches("\\d\\d")) {
				voice = Integer.parseInt(voiceNum);
			} else if (voiceNum.substring(0, 1).matches("\\d")) {
				voice = Integer.parseInt(voiceNum.substring(0, 1));
			} else {
				// !?!?!
				System.err.println("Bad SyncWith token (invalid voice): "
						+ event.getToken());
			}

			// Get Layer
			arguments = arguments.substring(arguments.lastIndexOf("L") + 1);
			int layer = 0;
			if (arguments.matches("\\d\\d")) {
				layer = Integer.parseInt(arguments);
			} else if (arguments.matches("\\d")) {
				layer = Integer.parseInt(arguments);
			} else {
				// !?!?!
				if (arguments.equalsIgnoreCase("u")) {
					// THIS IS A SPECIAL CASE.
					// Sync with longest layer in that voice
					layer = -1000;
				} else {
					System.err.println("Bad SyncWith token (invalid layer): "
							+ event.getToken());
				}
			}
			eventManager.syncCurrVoiceAndLayerWith(voice, layer);
		} else if (event.getToken().toLowerCase()
				.startsWith(BlockSign.SIGN_START_TOKEN.toLowerCase())) {
			// TODO: Add again
			// Note current sign id
			currentSignID = Integer.parseInt(event.getToken().substring(2));
		} else if (event.getToken().toLowerCase()
				.equals(BlockSign.STACCATO_OFF_TOKEN.toLowerCase())) {
			// Turn off staccato for curr track
			staccato[currentTrack] = 0;
			staccatoEnd[currentTrack] = 0d;
		} else if (event.getToken().toLowerCase()
				.startsWith(BlockSign.STACCATO_TOKEN.toLowerCase())) {
			// Turn on staccato for curr track
			String tokenArgs = event.getToken().toLowerCase()
					.replace(BlockSign.STACCATO_TOKEN.toLowerCase(), "");

			// Eighths
			String eighthsString = tokenArgs.substring(0, 1);
			tokenArgs = tokenArgs.substring(1);

			Integer eighths = Integer.parseInt(eighthsString);
			if (eighths == 0) {
				// zero is reserved for "staccato off" here; use 9 to signal
				// one-tick note lengths instead
				eighths = 9;
			}

			// Duration
			Double duration = Double.parseDouble(tokenArgs);

			// Set up staccato

			// System.out.println(duration + ":" + eighths);
			if (duration >= 0) {
				staccatoEnd[currentTrack] = eventManager.getTrackTimerDec()
						+ duration;
			} else {
				staccatoEnd[currentTrack] = Double.MAX_VALUE;
			}
			staccato[currentTrack] = eighths;
		}

		// Report to listsners
		if (mcdittyEventReadListener != null) {
			long now = (long) eventManager.getTrackTimerDec();
			if (MCDittyConfig.debug) {
				System.out.println("mcDittyEvent: time=" + now);
			}
			mcdittyEventReadListener.mcDittyEventRead(event, now, currentTrack,
					currentLayer);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jfugue.ParserListenerAdapter#lyricEvent(org.jfugue.elements.Lyric)
	 */
	/**
	 * MCDitty: obviously, since lyric events are added by MCDitty. This
	 * forwards the event onwards to something else.
	 */
	@Override
	public void lyricEvent(Lyric event) {
		if (MCDittyConfig.debug) {
			System.out.println("MidiRenderer: lyricEvent hit: "
					+ event.getVerifyString());
			System.out.println(resolution + ":" + sequenceTiming + ":"
					+ initialNoteTime);
		}
		if (lyricEventReadListener != null) {
			// long now = channelTimesMS[currentTrack];
			long now = (long) eventManager.getTrackTimerDec();
			if (MCDittyConfig.debug) {
				System.out.println("lyricEvent: time=" + now);
			}
			lyricEventReadListener.lyricEventRead(event, now);
		}
	}

	/**
	 * MCDitty
	 * 
	 * @return the lyricEventReadListener
	 */
	public LyricEventReadListener getLyricEventReadListener() {
		return lyricEventReadListener;
	}

	/**
	 * MCDitty
	 * 
	 * @param lyricEventReadListener
	 *            the lyricEventReadListener to set
	 */
	public void setLyricEventReadListener(
			LyricEventReadListener lyricEventReadListener) {
		this.lyricEventReadListener = lyricEventReadListener;
	}

	/**
	 * MCDitty
	 * 
	 * @return the tempoEventReadListener
	 */
	public TempoEventReadListener getTempoEventReadListener() {
		return tempoEventReadListener;
	}

	/**
	 * MCDitty
	 * 
	 * @param tempoEventReadListener
	 *            the tempoEventReadListener to set
	 */
	public void setTempoEventReadListener(
			TempoEventReadListener tempoEventReadListener) {
		this.tempoEventReadListener = tempoEventReadListener;
	}

	/**
	 * @return the mcdittyEventReadListener
	 */
	public MCDittyEventReadListener getMcdittyEventReadListener() {
		return mcdittyEventReadListener;
	}

	/**
	 * @param mcdittyEventReadListener
	 *            the mcdittyEventReadListener to set
	 */
	public void setMcdittyEventReadListener(
			MCDittyEventReadListener mcdittyEventReadListener) {
		this.mcdittyEventReadListener = mcdittyEventReadListener;
	}

	public ParticleWorthyNoteReadListener getParticleWorthyNoteReadListener() {
		return particleWorthyNoteReadListener;
	}

	public void setParticleWorthyNoteReadListener(
			ParticleWorthyNoteReadListener particleWorthyNoteReadListener) {
		this.particleWorthyNoteReadListener = particleWorthyNoteReadListener;
	}

	public void setForceFullAttackDecay(int instrument, boolean force) {
		forceInstrumentFullAttackDecay[instrument] = force;
	}

	public InstrumentEventReadListener getInstrumentEventReadListener() {
		return instrumentEventReadListener;
	}

	public void setInstrumentEventReadListener(
			InstrumentEventReadListener instrumentEventReadListener) {
		this.instrumentEventReadListener = instrumentEventReadListener;
	}

}
