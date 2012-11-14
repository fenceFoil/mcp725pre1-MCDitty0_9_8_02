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
package com.wikispaces.mcditty;

import net.minecraft.src.BlockSign;

/**
 * CHECK BEFORE USING: this code is probably obsolete and doesn't account for
 * things like volume. Plays a musicString.
 * 
 */
public class SimpleTunePlayerThread extends Thread {

	private String tuneToPlay = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		while (true) {
			if (tuneToPlay != null) {
				BlockSign.playMusicString(tuneToPlay + " Rwww");
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
