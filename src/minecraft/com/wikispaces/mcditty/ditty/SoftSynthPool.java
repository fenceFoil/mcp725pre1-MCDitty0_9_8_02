/**
 * Copyright (c) 2012 William Karnavas 
 * All Rights Reserved
 */

/**
 * 
 * This file is part of MCDitty.
 * 
 * MCDitty is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MCDitty is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MCDitty. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.wikispaces.mcditty.ditty;

import javax.sound.midi.MidiUnavailableException;

import com.sun.media.sound.SoftSynthesizer;

/**
 * Responsible for caching a number of synthesizers ready to play ditties at a
 * moment's notice.
 * 
 */
public class SoftSynthPool extends Thread {
	private static final long SYNTH_CACHE_CHECK_TIME = 5000;
	private Object cachedSynthMutex = new Object();
	private SoftSynthesizer cachedSynth = null;

	@Override
	public void run() {
		setName("MCDitty Synth Cache");
		setPriority(MIN_PRIORITY);

		// Disabled -- observed large memory usage increase after synths cached
		// and left open for several minutes

		System.out.println("Synth Cache started");
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
		if (cachedSynth == null
				&& DittyPlayerThread.jFuguePlayerThreads.size() <= 0) {
			System.out.println("Filling synth cache");
			synchronized (cachedSynthMutex) {
				cachedSynth = createOpenedSynth();
			}
			cachedSynth = createOpenedSynth();
			System.out.println("Done.");
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
		if (cachedSynth != null) {
			// Try to use the cached synth
			synchronized (cachedSynthMutex) {
				SoftSynthesizer returnSynth = cachedSynth;
				// Mark cached synth as used
				cachedSynth = null;
				// Return the cached synth
				return returnSynth;
			}
		}

		// Still here? A new synth must then be created
		System.out.println("Creating new synth to return...");
		return createOpenedSynth();
	}

}
