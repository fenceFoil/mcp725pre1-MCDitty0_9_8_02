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
 * MaxPlays [Times]
 */
public class MaxPlaysKeyword extends ParsedKeyword {

	private int maxPlays= 1;

	public MaxPlaysKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	public static MaxPlaysKeyword parse(String rawLine) {
		MaxPlaysKeyword keyword = new MaxPlaysKeyword(rawLine);

		// Get number of blocks to move; default is 1 if not specified
		int numArgs = rawLine.split(" ").length;
		if (numArgs == 2) {
			String argument = rawLine.split(" ")[1];
			if (argument.trim().matches("\\d+")) {
				keyword.setMaxPlays(Integer.parseInt(argument.trim()));
			} else {
				// Error: invalid agument
				keyword.setGoodKeyword(true);
				keyword.setErrorMessageType(ERROR);
				keyword.setErrorMessage("Follow MaxPlays with a number.");
			}
		} else if (numArgs > 2) {
			// Warning: Too Many Arguments
			keyword.setGoodKeyword(true);
			keyword.setErrorMessageType(INFO);
			keyword.setErrorMessage("Only one number is needed.");
		}

		return keyword;
	}

	public int getMaxPlays() {
		return maxPlays;
	}

	public void setMaxPlays(int maxPlays) {
		this.maxPlays = maxPlays;
	}

}
