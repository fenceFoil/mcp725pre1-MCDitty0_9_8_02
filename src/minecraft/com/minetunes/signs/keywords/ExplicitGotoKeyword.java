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
 * An Explicit Goto keyword is one of 'up, down, in, out, left, or right.' Not
 * to be confused with the keyword "GoTo".
 * 
 */
public class ExplicitGotoKeyword extends ParsedKeyword {

	private int amountMove = 1;

	public ExplicitGotoKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	public static ExplicitGotoKeyword parse(String rawLine) {
		ExplicitGotoKeyword keyword = new ExplicitGotoKeyword(rawLine);

		// Get number of blocks to move; default is 1 if not specified
		int numArgs = rawLine.split(" ").length;
		if (numArgs == 2) {
			String argument = rawLine.split(" ")[1];
			if (argument.trim().matches("\\d+")) {
				keyword.setAmountMove(Integer.parseInt(argument.trim()));
			} else {
				// Error: invalid agument
				keyword.setGoodKeyword(false);
				keyword.setErrorMessageType(ERROR);
				keyword.setErrorMessage("Follow this go-to with a number of blocks to move.");
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
	public int getAmountMove() {
		return amountMove;
	}

	/**
	 * @param amountMove
	 *            the amountMove to set
	 */
	public void setAmountMove(int amountMove) {
		this.amountMove = amountMove;
	}

}
