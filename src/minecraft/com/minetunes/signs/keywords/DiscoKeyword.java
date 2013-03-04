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
package com.minetunes.signs.keywords;

import java.util.ArrayList;

import net.minecraft.src.TileEntitySign;
import net.minecraft.src.World;

import com.minetunes.Point3D;
import com.minetunes.disco.DiscoFloor;
import com.minetunes.ditty.Ditty;

/**
 * 
 *
 */
public class DiscoKeyword extends SignTuneKeyword {

	private ArrayList<Integer> voices = new ArrayList<Integer>();

	public DiscoKeyword(String wholeKeyword) {
		super(wholeKeyword);

		// By default, follow all voices
		for (int i = 0; i < 16; i++) {
			voices.add(i);
		}
	}

	@Override
	public void parse() {

		String[] argsSplit = getWholeKeyword().split(" ");
		int numArgs = argsSplit.length;

		if (numArgs <= 1) {
			// No args; return a default disco keywords
			return;
		} else {
			// There are arguments!
			// All must be numbers
			// Reset voices
			getVoices().clear();
			for (int i = 1; i < numArgs; i++) {
				if (!argsSplit[i].matches("\\d+")) {
					setGoodKeyword(false);
					setErrorMessageType(ERROR);
					setErrorMessage("Follow disco with the numbers of the voices you want to beat to.");
					return;
				} else {
					// Valid number
					Integer voice = Integer.parseInt(argsSplit[i]);
					if (voice < 0 || voice > 15) {
						// Voice out of range
						setGoodKeyword(false);
						setErrorMessageType(ERROR);
						setErrorMessage("Voices range from 0 to 15.");
						return;
					} else {
						// Valid voice!
						getVoices().add(voice);
					}
				}
			}
		}
	}

	public ArrayList<Integer> getVoices() {
		return voices;
	}

	public void setVoices(ArrayList<Integer> voices) {
		this.voices = voices;
	}

	@Override
	public Point3D execute(Ditty ditty, Point3D location,
			TileEntitySign signTileEntity, Point3D nextSign, World world,
			StringBuilder readMusicString) {
		// Register a disco floor into the ditty properties
		DiscoFloor newFloor = new DiscoFloor(signTileEntity, getVoices());
		ditty.addDiscoFloor(newFloor);
		return null;
	}

}
