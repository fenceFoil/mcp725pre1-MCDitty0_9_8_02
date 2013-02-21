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

/**
 * @author William
 * 
 */
public class VolumeKeyword extends ParsedKeyword {

	private int volume = 1;

	public VolumeKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	public static VolumeKeyword parse(String rawLine) {
		VolumeKeyword keyword = new VolumeKeyword(rawLine);

		// Get volume; it must be specified
		int numArgs = rawLine.split(" ").length;
		if (numArgs >= 2) {
			String argument = rawLine.split(" ")[1];
			if (argument.trim().matches("\\d+")) {
				keyword.setVolume(Integer.parseInt(argument.trim()));
			} else {
				// Error: invalid agument
				keyword.setGoodKeyword(false);
				keyword.setErrorMessageType(ERROR);
				keyword.setErrorMessage("Follow Volume with a number from 0 to 100.");
			}
		}

		if (numArgs > 2) {
			// Error: Too many arguments
			keyword.setGoodKeyword(true);
			keyword.setErrorMessageType(INFO);
			keyword.setErrorMessage("Only one number is needed.");
		} else if (numArgs < 2) {
			// Error: No arguemnts
			keyword.setGoodKeyword(false);
			keyword.setErrorMessageType(ERROR);
			keyword.setErrorMessage("Follow Volume with a number from 0 to 100.");
		}

		return keyword;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

}
