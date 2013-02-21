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
package com.minetunes;

import com.minetunes.ditty.DittyPlayerThread;

/**
 * CHECK BEFORE USING: this code is probably obsolete and doesn't account for
 * things like volume. Plays a musicString.
 * 
 */
public class SimpleTunePlayerThread extends Thread {

	private String tuneToPlay = null;
	
	public SimpleTunePlayerThread () {
		setName("MineTunes Simple MusicString Player");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		while (true) {
			if (tuneToPlay != null) {
				DittyPlayerThread.playMusicString(tuneToPlay + " Rwww");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					break;
				}
				tuneToPlay = null;
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	public void play(String endOfLineTune) {
		tuneToPlay = endOfLineTune;
	}

}
