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

import java.util.LinkedList;

import javax.sound.midi.MidiUnavailableException;

import org.lwjgl.input.Keyboard;

import com.sun.media.sound.SoftSynthesizer;
import com.wikispaces.mcditty.GetMinecraft;

/**
 * Responsible for caching a number of synthesizers ready to play ditties at a
 * moment's notice.
 * 
 */
public class SoftSynthPool extends Thread {
	private static long SYNTH_CACHE_CHECK_TIME = 3000;
	private static int POOL_SIZE = 3;
	private Object cachedSynthMutex = new Object();
	// private SoftSynthesizer cachedSynth = null;
	private LinkedList<SoftSynthesizer> pool = new LinkedList<SoftSynthesizer>();

	@Override
	public void run() {
		setName("MCDitty Synth Cache");
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
		if (GetMinecraft.instance() != null
				&& GetMinecraft.instance().gameSettings != null
				&& GetMinecraft.instance().gameSettings.keyBindForward != null) {
			isWalkingForward = Keyboard
					.isKeyDown(GetMinecraft.instance().gameSettings.keyBindForward.keyCode);
		}
		if (pool.size() < POOL_SIZE
				&& DittyPlayerThread.jFuguePlayerThreads.size() <= 0
				&& !isWalkingForward) {
			// System.out.println("Adding a new synth to the cache.");
			synchronized (cachedSynthMutex) {
				pool.add(createOpenedSynth());
			}
			// System.out.println("Done.");
		} else if (pool.size() > POOL_SIZE) {
			synchronized (cachedSynthMutex) {
				// System.out.println("Removing a synth from the cache");
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

}
