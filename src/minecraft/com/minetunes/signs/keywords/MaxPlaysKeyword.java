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
import com.minetunes.signs.SignDitty;

/**
 * MaxPlays [Times]
 */
public class MaxPlaysKeyword extends SignTuneKeyword {

	private int maxPlays = 1;

	public MaxPlaysKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	@Override
	public void parse() {
		// Get number of blocks to move; default is 1 if not specified
		int numArgs = getWholeKeyword().split(" ").length;
		if (numArgs == 2) {
			String argument = getWholeKeyword().split(" ")[1];
			if (argument.trim().matches("\\d+")) {
				setMaxPlays(Integer.parseInt(argument.trim()));
			} else {
				// Error: invalid agument
				setGoodKeyword(true);
				setErrorMessageType(ERROR);
				setErrorMessage("Follow MaxPlays with a number.");
			}
		} else if (numArgs > 2) {
			// Warning: Too Many Arguments
			setGoodKeyword(true);
			setErrorMessageType(INFO);
			setErrorMessage("Only one number is needed.");
		}

		return;
	}

	public int getMaxPlays() {
		return maxPlays;
	}

	public void setMaxPlays(int maxPlays) {
		this.maxPlays = maxPlays;
	}

	@Override
	public Point3D execute(Ditty ditty, Point3D location,
			TileEntitySign signTileEntity, Point3D nextSign, World world,
			StringBuilder readMusicString) {
		// Add maxplay lock point
		if (ditty instanceof SignDitty) {
			((SignDitty) ditty).addMaxPlayLockPoint(location, getMaxPlays());
		}

		return null;
	}

}
