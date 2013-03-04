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
 * 
 */
public class PatternKeyword extends SignTuneKeyword {

	private int repeatCount = 1;

	public PatternKeyword(String rawLine) {
		super(rawLine);
	}

	@Override
	public void parse() {
		// Decide defaults
		if (getKeyword().toLowerCase().equals("repeat")) {
			// Repeats historically implied a default of 2 repetitions
			repeatCount = 2;
		} else if (getKeyword().toLowerCase().equals("pattern")){
			// Pattern historically implied a default of 1 repetition
			repeatCount = 1;
		}
		
		// Get number of repetitions of pattern
		int numArgs = getWholeKeyword().split(" ").length;
		if (numArgs == 2) {
			String argument = getWholeKeyword().split(" ")[1];
			if (argument.trim().matches("\\d+")) {
				setRepeatCount(Integer.parseInt(argument.trim()));
			} else {
				// Error: invalid agument
				setGoodKeyword(false);
				setErrorMessageType(ERROR);
				setErrorMessage("Follow Pattern with a number: how many times should it repeat?");
			}
		} else if (numArgs > 2) {
			// Warning: Too Many Arguments
			setGoodKeyword(true);
			setErrorMessageType(INFO);
			setErrorMessage("Only one number is needed.");
		}
		
	}

	public void setRepeatCount(int parseInt) {
		repeatCount = parseInt;
	}

	public int getRepeatCount() {
		return repeatCount;
	}

	@Override
	public boolean hasSpecialExecution() {
		return true;
	}
}
