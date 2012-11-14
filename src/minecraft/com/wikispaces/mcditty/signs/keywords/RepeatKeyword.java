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

import com.wikispaces.mcditty.signs.ParsedSign;
import com.wikispaces.mcditty.signs.SignParser;

/**
 *
 */
public class RepeatKeyword extends ParsedKeyword {

	int repeatCount = 2;

	/**
	 * @param wholeKeyword
	 */
	public RepeatKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	public static RepeatKeyword parse(String rawLine) {
		RepeatKeyword keyword = new RepeatKeyword(rawLine);

		// Get number of blocks to move; default is 1 if not specified
		int numArgs = rawLine.split(" ").length;
		if (numArgs == 2) {
			String argument = rawLine.split(" ")[1];
			if (argument.trim().matches("\\d+")) {
				keyword.setRepeatCount(Integer.parseInt(argument.trim()));
			} else {
				// Error: invalid agument
				keyword.setGoodKeyword(false);
				keyword.setErrorMessageType(ERROR);
				keyword.setErrorMessage("Follow repeat with a number: how many times should it repeat the music below?");
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
	 * @return the repeatCount
	 */
	public int getRepeatCount() {
		return repeatCount;
	}

	/**
	 * @param repeatCount
	 *            the repeatCount to set
	 */
	public void setRepeatCount(int repeatCount) {
		this.repeatCount = repeatCount;
	}

	@Override
	public <T extends ParsedKeyword> void parseWithMultiline(
			ParsedSign parsedSign, int keywordLine, T k) {
		super.parseWithMultiline(parsedSign, keywordLine, k);
		
		for (int i = keywordLine + 1; i < parsedSign.getSignText().length; i++) {
			parsedSign.getLines()[i] = this;
		}

		for (int i = keywordLine + 1; i < parsedSign.getSignText().length; i++) {
			if (SignParser.recognizeKeyword(parsedSign.getSignText()[i]) != null) {
				// Keyword; can't read.
				setGoodKeyword(false);
				setErrorMessage("'Repeat' cannot repeat keywords: only music. Use 'Pattern' to repeat keywords.");
				setErrorMessageType(ERROR);
				return;
			}
		}
	}

	@Override
	public boolean isMultiline() {
		return true;
	}

}
