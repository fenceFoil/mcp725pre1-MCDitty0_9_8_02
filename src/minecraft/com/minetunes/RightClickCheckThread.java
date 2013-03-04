/**
 * Copyright (c) 2012 William Karnavas 
 * All Rights Reserved
 */

/**
 * 
 * This file is part of MineTunes.
 * 
 * MineTunes is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * MineTunes is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MineTunes. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.minetunes;

import org.lwjgl.input.Mouse;

import com.minetunes.signs.SignTuneParser;

/**
 * Waits for all mouse buttons to be lifted, then sets a flag in BlockSign.
 *
 */
public class RightClickCheckThread extends Thread {
	
	public RightClickCheckThread () {
		setName ("MineTunes Sign Click Release Monitor");
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		while (true) {
			if (!Mouse.isButtonDown(0) && !Mouse.isButtonDown(1)) {
				SignTuneParser.clickHeld = false;
				break;
			}
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
				SignTuneParser.clickHeld = false;
				break;
			}
		}
	}
	
}
