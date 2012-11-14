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
package com.wikispaces.mcditty.signs.keywords;

/**
 * 
 */
public class PatternKeyword extends ParsedKeyword {

	private int repeatCount = 1;

	public PatternKeyword(String rawLine) {
		super(rawLine);
	}

	public static PatternKeyword parse(String rawLine) {
		PatternKeyword keyword = new PatternKeyword(rawLine);

		// Get number of repetitions of pattern; default is 1 if not specified
		int numArgs = rawLine.split(" ").length;
		if (numArgs == 2) {
			String argument = rawLine.split(" ")[1];
			if (argument.trim().matches("\\d+")) {
				keyword.setRepeatCount(Integer.parseInt(argument.trim()));
			} else {
				// Error: invalid agument
				keyword.setGoodKeyword(false);
				keyword.setErrorMessageType(ERROR);
				keyword.setErrorMessage("Follow Pattern with a number: how many times should it repeat?");
			}
		} else if (numArgs > 2) {
			// Warning: Too Many Arguments
			keyword.setGoodKeyword(true);
			keyword.setErrorMessageType(INFO);
			keyword.setErrorMessage("Only one number is needed.");
		}

		return keyword;
	}

	public void setRepeatCount(int parseInt) {
		repeatCount = parseInt;
	}

	public int getRepeatCount() {
		return repeatCount;
	}

	@Override
	public boolean isFirstLineOnly() {
		return true;
	}
}
