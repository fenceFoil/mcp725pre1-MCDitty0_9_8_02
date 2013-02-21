/**
 * Copyright (c) 2012-2013 William Karnavas 
 * All Rights Reserved
 */

/**
 * 
 * This file is part of MineTunes.
 * 
 *  MineTunes is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 *  MineTunes is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MineTunes. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.minetunes.blockTune;

import java.util.HashMap;
import java.util.LinkedList;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.Patch;

import com.minetunes.config.MinetunesConfig;
import com.minetunes.ditty.MIDISynthPool;
import com.sun.media.sound.SoftSynthesizer;

/**
 * Handles the synthesizer, playback, and timing for a BlockTune via an abstract
 * interface. Should be easily separable from the rest of MineTunes, give or take
 * MIDISynthPool and Gervill.
 * 
 */
public class BlockTunePlayer extends Thread {

	/**
	 * Used when tuneAccess derps and gives us a null result for a frame
	 */
	private static final Frame NULL_FRAME = new Frame(1);

	/**
	 * MS to wait while paused before releasing the synth
	 */
	private static final long RELEASE_SYNTH_TIME = 30 * 1000;

	/**
	 * 0-127: Midi attack velocity for a note
	 */
	private static final int NOTE_VELOCITY = 63;

	/**
	 * Source of music
	 */
	private BlockTuneAccess tuneAccess;

	/**
	 * Current frame in the tuneAccess
	 */
	private int currFrame = 0;

	/**
	 * Tempo: beat = frame here
	 */
	private double beatsPerSecond = 8;

	/**
	 * Synthesizer used by this player
	 */
	private SoftSynthesizer synth = null;

	/**
	 * If true, player will release resources and stop music as soon as possible
	 */
	private boolean exiting = false;

	/**
	 * Source of synths
	 */
	private MIDISynthPool synthPool = null;

	/**
	 * Channels with notes playing; used for turning off notes before playing
	 * new ones
	 */
	private boolean[] channelsUsedLastFrame = new boolean[16];

	private Patch[] instruments = new Patch[16];

	private double lastMasterVolume = 1;

	/**
	 * Set up a new blocktune player
	 * 
	 * @param tuneAccess
	 * @param synthPool
	 */
	public BlockTunePlayer(BlockTuneAccess tuneAccess, MIDISynthPool synthPool) {
		this.tuneAccess = tuneAccess;
		this.synthPool = synthPool;

		setName("BlockTune Player");
		setPriority(MAX_PRIORITY);
	}

	@Override
	public void run() {
		while (!exiting) {
			boolean updateResult = update();
			if (updateResult == false) {
				exiting = true;
			}
		}

		// Tune over; close the synth
		closeSynth();
	}

	/**
	 * 
	 * @return false if the block tune should stop updating
	 */
	private boolean update() {
		// If paused, wait until unpaused again or interrupted
		if (tuneAccess.isPaused()) {
			long pauseStart = System.currentTimeMillis();

			// Turn off instruments
			turnOffPlayingNotes();

			// Wait
			while (tuneAccess.isPaused() && !exiting) {
				if (!isSynthClosed()
						&& System.currentTimeMillis() - pauseStart > RELEASE_SYNTH_TIME) {
					closeSynth();
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		}

		// Update tempo
		beatsPerSecond = tuneAccess.getBeatsPerSecond();

		// Update volumes if they have changed and there is a synth
		// available
		if (synth != null && synth.getChannels() != null) {
			double currMasterVolume = tuneAccess.getMasterVolume();
			if (currMasterVolume != lastMasterVolume) {
				// Master volume has changed (right now there is no channel
				// volume control, so just change all chanells to new master
				// volume)

				// Change coarse volume to a value between 0 and 1
				double boundedVolume = Math.min(1,
						Math.max(0, currMasterVolume));
				// Get value from 0 to 127 from that
				byte coarseVolume = (byte) (boundedVolume * 127);
				// Apply this volume to all channels
				for (MidiChannel c : synth.getChannels()) {
					c.controlChange(0x07, coarseVolume);
				}
			}
			lastMasterVolume = currMasterVolume;
		}

		// Decide what frame to play next
		if (currFrame >= tuneAccess.getFrameCount()) {
			if (tuneAccess.isLooping()) {
				currFrame = 0;
			} else {
				return false;
			}
		}

		// Read frame
		Frame frame = tuneAccess.getFrame(currFrame);

		// Handle null frame
		if (frame == null) {
			frame = NULL_FRAME;
		}

		// Play any notes
		playFrame(frame);

		// Tell blockTune
		tuneAccess.onFramePlayed(frame, currFrame);

		// Delay until next frame
		long endTime = (long) (System.nanoTime() + (1000000000d / beatsPerSecond));
		while (System.nanoTime() < endTime && !exiting) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
			}
		}

		// Increment current frame
		currFrame++;

		// Say that we still need to repeat
		return true;
	}

	/**
	 * Immediately pauses or unpauses a player based on "paused" and the result
	 * of a BlockTuneAccess's isPaused() method. If you don't call this, you
	 * might wait anywhere from 100ms to over a second for playback to resume
	 * after the result of isPaused() changes.
	 * 
	 * @param paused
	 */
	public void setPaused(boolean paused) {
		interrupt();
	}

	/**
	 * Stops playback and releases resources as soon as possible
	 */
	public void close() {
		exiting = true;
		interrupt();
	}

	/**
	 * Plays the notes of given frame, turning off any notes from previous
	 * frames and retrieving a synth first if necessary.
	 * 
	 * @param frame
	 */
	private void playFrame(Frame frame) {
		if (synth == null) {
			setUpSynth();
		}

		// Turn off any notes left on
		turnOffPlayingNotes();

		// Start any new notes this frame
		HashMap<Integer, LinkedList<Byte>> notesStarted = frame
				.getChannelNotesStarted();
		for (int channel : notesStarted.keySet()) {
			LinkedList<Byte> notes = notesStarted.get(channel);
			if (notes != null) {
				for (byte note : notes) {
					synth.getChannels()[channel].noteOn(note, NOTE_VELOCITY);
					channelsUsedLastFrame[channel] = true;
				}
			}
		}
	}

	/**
	 * Stops all notes on channels marked as being used
	 */
	private void turnOffPlayingNotes() {
		for (int i = 0; i < channelsUsedLastFrame.length; i++) {
			if (channelsUsedLastFrame[i]) {
				synth.getChannels()[i].allNotesOff();
				channelsUsedLastFrame[i] = false;
			}
		}
	}

	/**
	 * Retreives a synth from the pool and sets up any instruments or other
	 * settings as required
	 * 
	 * @param synth2
	 */
	private void setUpSynth() {
		synth = synthPool.getOpenedSynth();

		// Set up instruments
		setUpChannelInstruments();
	}

	private void setUpChannelInstruments() {
		for (int i = 0; i < instruments.length; i++) {
			Patch p = instruments[i];
			if (p != null && synth != null && synth.getChannels() != null
					&& synth.getChannels()[i] != null) {
				if (synth.getChannels()[i].getProgram() != p.getProgram()) {
					if (MinetunesConfig.customSF2 != null
							&& MinetunesConfig.customSF2.isSF2Loaded()) {
						synth.loadInstrument(MinetunesConfig.customSF2
								.getCachedSoundbank().getInstrument(p));
					}
					synth.getChannels()[i].programChange(p.getProgram());
				}
			}
		}
	}

	/**
	 * @return
	 */
	private boolean isSynthClosed() {
		return synth == null;
	}

	/**
	 * Returns a synth back to the pool
	 */
	private void closeSynth() {
		synthPool.returnUsedSynth(synth, null, null);
		synth = null;
	}

	public void setInstrument(int channel, int program) {
		instruments[channel] = new Patch(0, program);
		setUpChannelInstruments();
	}
}
