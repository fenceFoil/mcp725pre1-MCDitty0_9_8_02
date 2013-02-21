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

import java.io.File;

import org.jfugue.elements.Note;

import com.minetunes.sfx.SFXManager;
import com.minetunes.signs.ParsedSign;

/**
 * 
 *
 */
public class SFXInstOffKeyword extends ParsedKeyword {

	/**
	 * The instrument number to replace with a sound effect
	 */
	private int instrument;

	/**
	 * @param wholeKeyword
	 */
	public SFXInstOffKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	public static SFXInstOffKeyword parse(String rawArgs) {
		SFXInstOffKeyword keyword = new SFXInstOffKeyword(rawArgs);

		// Parse the first (and only) line of a SFXInstOff keyword

		// Get the instrument number
		String[] args = rawArgs.split(" ");
		int numArgs = args.length;

		if (numArgs > 2) {
			// Too many arguments
			keyword.setGoodKeyword(true);
			keyword.setErrorMessageType(INFO);
			keyword.setErrorMessage("Only the instrument number is needed on the first line.");
		} else if (numArgs <= 1) {
			// No instrument number
			keyword.setGoodKeyword(false);
			keyword.setErrorMessageType(ERROR);
			keyword.setErrorMessage("Follow SFXInstOff with an instrument number.");
			return keyword;
		} else {
			// Instrument number given
			String argument = args[1];
			if (argument.trim().matches("\\d+")) {
				keyword.setInstrument(Integer.parseInt(argument));
			} else {
				// Error: invalid agument
				keyword.setGoodKeyword(false);
				keyword.setErrorMessageType(ERROR);
				keyword.setErrorMessage("Follow SFXInstOff with an instrument number.");
				return keyword;
			}

			if (keyword.getInstrument() < 0 || keyword.getInstrument() > 127) {
				// Out of bounds
				keyword.setInstrument(0);
				keyword.setGoodKeyword(false);
				keyword.setErrorMessageType(ERROR);
				keyword.setErrorMessage("Instrument numbers range from 0 to 127.");
				return keyword;
			}
		}

		return keyword;
	}

	public int getInstrument() {
		return instrument;
	}

	public void setInstrument(int instrument) {
		this.instrument = instrument;
	}

}
