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

import java.util.HashSet;

import net.minecraft.src.TileEntitySign;
import net.minecraft.src.World;

import org.jfugue.JFugueDefinitions;

import com.minetunes.Point3D;
import com.minetunes.ditty.Ditty;
import com.minetunes.signs.SignTuneParser;
import com.minetunes.signs.SignParser;

/**
 * Keyword format:
 * 
 * OctavesOff [octaves] ...
 * 
 * [octaves]: -10 to 10 Ints; multiple; optional (default is just a single 1)
 */
public class OctavesOffKeyword extends SignTuneKeyword {

	private HashSet<Integer> octaves = new HashSet<Integer>();

	public OctavesOffKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	@Override
	public void parse() {
		String[] args = getWholeKeyword().split(" ");
		int numArgs = getWholeKeyword().split(" ").length;

		int argsLeft = 0;
		if (numArgs <= 1) {
			return;
		} else {
			argsLeft = numArgs - 1;
		}

		int currArg = 1;
		boolean octavesSpecified = false;
		while (argsLeft > 0) {
			String argument = args[currArg];

			if (argument.trim().matches("[-\\+]?\\d+")) {
				Integer tonesArg = Integer.parseInt(argument);
				if (tonesArg > 10 || tonesArg < -10) {
					// Out of range
					setGoodKeyword(false);
					setErrorMessageType(WARNING);
					setErrorMessage("Follow OctavesOff with numbers (-10 to +10).");
					return;
				}
				getOctaves().add(tonesArg);

				// Note that some octaves were specified
				octavesSpecified = true;
			} else {
				// Bad argument
				setGoodKeyword(false);
				setErrorMessageType(ERROR);
				setErrorMessage("Follow OctavesOff with numbers (-10 to +10).");
				return;
			}

			argsLeft--;
		}

		return;
	}

	public HashSet<Integer> getOctaves() {
		return octaves;
	}

	public void setOctaves(HashSet<Integer> octaves) {
		this.octaves = octaves;
	}

	@Override
	public Point3D execute(Ditty ditty, Point3D location,
			TileEntitySign signTileEntity, Point3D nextSign, World world,
			StringBuilder readMusicString) {
		// Add an octaves off note effect token
		Object[] octaves = getOctaves().toArray(new Integer[0]);
		String token = SignTuneParser.createNoteEffectToken(true,
				SignTuneParser.NOTE_EFFECT_OCTAVES, octaves);
		ditty.addMusicStringTokens(readMusicString, token, false);
		return null;
	}

}
