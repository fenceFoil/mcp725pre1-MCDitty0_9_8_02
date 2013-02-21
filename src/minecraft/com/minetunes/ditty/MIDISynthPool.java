/**
 * Copyright (c) 2012 William Karnavas 
 * All Rights Reserved
 */

/**
 * 
 * This file is part of MineTunes.
 * 
 * MineTunes is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MineTunes is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MineTunes. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.minetunes.ditty;

import java.util.HashMap;
import java.util.LinkedList;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiUnavailableException;

import com.sun.media.sound.SF2Soundbank;
import com.sun.media.sound.SoftSynthesizer;

/**
 * Responsible for caching a number of synthesizers ready to play ditties at a
 * moment's notice.
 * 
 */
public class MIDISynthPool extends Thread {
	private static long SYNTH_CACHE_CHECK_TIME = 4000;
	private static int POOL_SIZE = 5;
	private Object cachedSynthMutex = new Object();
	// private SoftSynthesizer cachedSynth = null;
	private LinkedList<SoftSynthesizer> pool = new LinkedList<SoftSynthesizer>();
	private NoNewSynthIndicator noNewSynthIndicator = null;

	private MIDISynthPool() {

	}

	public MIDISynthPool(NoNewSynthIndicator indicator) {
		noNewSynthIndicator = indicator;
	}

	@Override
	public void run() {
		setName("MineTunes Synth Cache");
		setPriority(MIN_PRIORITY);

		// Disabled -- observed large memory usage increase after synths cached
		// and left open for several minutes

		// System.out.println("Synth Cache started");
		while (true) {
			updatePool();
			try {
				Thread.sleep(SYNTH_CACHE_CHECK_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void updatePool() {
		boolean isWalkingForward = false;
		if (noNewSynthIndicator != null) {
			isWalkingForward = noNewSynthIndicator.getNewSynthsAllowed();
		}
		if (pool.size() < POOL_SIZE
		// && DittyPlayerThread.jFuguePlayerThreads.size() <= 0
				&& !isWalkingForward) {
			// System.out.println("Adding a new synth to the cache. n="
			// + pool.size());
			SoftSynthesizer newSynth = createOpenedSynth();
			synchronized (cachedSynthMutex) {
				pool.add(newSynth);
			}
			// System.out.println("Done.");
		} else if (pool.size() > 2 * POOL_SIZE) {
			synchronized (cachedSynthMutex) {
				// System.out.println("Removing a synth from the cache n="
				// + pool.size());
				pool.poll().close();
			}
		}
	}

	private SoftSynthesizer createOpenedSynth() {
		SoftSynthesizer s = new SoftSynthesizer();
		try {
			s.open();
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		}
		return s;
	}

	/**
	 * Tries to return a cached synth; otherwise one is created
	 * 
	 * @return
	 */
	public SoftSynthesizer getOpenedSynth() {
		synchronized (cachedSynthMutex) {
			if (pool.size() > 0) {
				// Try to use the cached synth
				// System.out.println("Using pool synth. Remaining = "
				// + pool.size());
				return pool.pollLast();
			}
		}

		// Still here? A new synth must then be created
		// System.out.println("Synth pool empty: creating new synth.");
		return createOpenedSynth();
	}

	public void returnUsedSynth(SoftSynthesizer synth,
			HashMap<Integer, SF2Soundbank> cachedSFXInstruments,
			Instrument[] originalSynthInstruments) {
		// TODO: First strip out all SFXInstruments

		// // TEMP: Only put back in pool if not SFXInstruments were loaded
		// if (cachedSFXInstruments.size() <= 0) {
		// synchronized (cachedSynthMutex) {
		// pool.add(synth);
		// }
		// } else {
		synth.close();
		// }
	}

}
