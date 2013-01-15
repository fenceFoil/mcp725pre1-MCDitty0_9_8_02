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
public class FireworkKeyword extends ParsedKeyword {

	private int up = 1;

	public FireworkKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	public static FireworkKeyword parse(String rawLine) {
		FireworkKeyword keyword = new FireworkKeyword(rawLine);

		// Get number of blocks to move; default is 1 if not specified
		int numArgs = rawLine.split(" ").length;
		if (numArgs == 2) {
			String argument = rawLine.split(" ")[1];
			if (argument.trim().matches("[+-]?\\d+")) {
				keyword.setUp(Integer.parseInt(argument.trim()));
			} else {
				// Error: invalid agument
				keyword.setGoodKeyword(false);
				keyword.setErrorMessageType(ERROR);
				keyword.setErrorMessage("Follow Firework with a number.");
			}
		} else if (numArgs > 2) {
			// Warning: Too Many Arguments
			keyword.setGoodKeyword(true);
			keyword.setErrorMessageType(INFO);
			keyword.setErrorMessage("Only one number is needed.");
		}

		return keyword;
	}

	/**
	 * @return the amountMove
	 */
	public int getUp() {
		return up;
	}

	/**
	 * @param amountMove
	 *            the amountMove to set
	 */
	public void setUp(int amountMove) {
		this.up = amountMove;
	}

}
