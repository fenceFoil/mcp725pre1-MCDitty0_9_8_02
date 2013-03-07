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

import net.minecraft.src.TileEntitySign;
import net.minecraft.src.World;

import com.minetunes.Point3D;
import com.minetunes.ditty.Ditty;
import com.minetunes.signs.SignTuneParser;
import com.minetunes.signs.keywords.argparser.ArgParser;
import com.minetunes.signs.keywords.argparser.IntArg;

/**
 * @author William
 * 
 */
public class VolumeKeyword extends SignTuneKeyword {

	private int volume = 1;
	private IntArg volumeArg = new IntArg("Percent", false, 0, 100, 100);

	public VolumeKeyword(String wholeKeyword) {
		super(wholeKeyword);
		argParser = new ArgParser().addLine(volumeArg);
	}

	@Override
	public void parse() {
		super.parse();
		setVolume (volumeArg.getParsedInt());
		
//		// Get volume; it must be specified
//		int numArgs = getWholeKeyword().split(" ").length;
//		if (numArgs >= 2) {
//			String argument = getWholeKeyword().split(" ")[1];
//			if (argument.trim().matches("\\d+")) {
//				setVolume(Integer.parseInt(argument.trim()));
//			} else {
//				// Error: invalid agument
//				setGoodKeyword(false);
//				setErrorMessageType(ERROR);
//				setErrorMessage("Follow Volume with a number from 0 to 100.");
//			}
//		}
//
//		if (numArgs > 2) {
//			// Error: Too many arguments
//			setGoodKeyword(true);
//			setErrorMessageType(INFO);
//			setErrorMessage("Only one number is needed.");
//		} else if (numArgs < 2) {
//			// Error: No arguemnts
//			setGoodKeyword(false);
//			setErrorMessageType(ERROR);
//			setErrorMessage("Follow Volume with a number from 0 to 100.");
//		}
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	@Override
	public Point3D execute(Ditty ditty, Point3D location,
			TileEntitySign signTileEntity, Point3D nextSign, World world,
			StringBuilder readMusicString) {
		// Inserts a volume token into the song
		ditty.addMusicStringTokens(readMusicString,
				SignTuneParser.getAdjustedVolumeToken(getVolume(), ditty),
				false);
		return null;
	}

}
