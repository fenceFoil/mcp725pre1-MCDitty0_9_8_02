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

import com.minetunes.signs.ParsedSign;

/**
 *
 */
public class CueKeyword extends SignTuneKeyword {

	private String label = "";
	private int repetition = 1;

	public CueKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	@Override
	public void parse() {
		// Read arguments
		int numArgs = getWholeKeyword().split(" ").length;

		// Get arguments
		// Get label (required)
		if (numArgs >= 2) {
			label = getWholeKeyword().split(" ")[1];
			if (!LyricKeyword.isValidCueLabel(label)) {
				// Illegal label
				setGoodKeyword(false);
				setErrorMessageType(WARNING);
				setErrorMessage("Cue names should only contain letters, numbers, and underscores.");
			}
		} else {
			// No label; this results in an error
			setGoodKeyword(false);
			setErrorMessageType(ERROR);
			setErrorMessage("Follow the Cue keyword with a cue name: e.g., 'Cue chorus'.");
		}

		// Check for third argument (color code or repetition)

		// Default values for the ensuing (optional) arguments
		int repetition = getRepetition();

		if (numArgs >= 3) {
			String argument = getWholeKeyword().split(" ")[2];
			if (argument.matches("\\d+")) {
				// Repetition!
				repetition = Integer.parseInt(argument);
			} else {
				// This means that the third word on the line is
				// not a number -- probably someone putting a
				// space in a label.
				// Throw error
				setGoodKeyword(false);
				setErrorMessageType(ERROR);
				setErrorMessage("A cue's name can't contain spaces.");
			}
		}

		// Too many arguments
		if (numArgs > 3) {
			setErrorMessageType(INFO);
			setErrorMessage("At most, Cue only needs to be followed with a name and repetition.");
		}

		return;
	}

	@Override
	public <T extends SignTuneKeyword> void parseWithMultiline(
			ParsedSign parsedSign, int keywordLine, T k) {
		super.parseWithMultiline(parsedSign, keywordLine, k);

		// Figure out how many lines are beneath the first line of the cue
		int linesAfterFirst = 4 - keywordLine;

		// Tag all lines on and below the keyword
		if (linesAfterFirst <= 0) {
			return;
		} else {
			for (int i = keywordLine; i < 4; i++) {
				parsedSign.getLines()[i] = this;
			}
		}

		// Line 1 beneath the sign is the target
		// Form: [target type]
		// if [target] == bot, the next argument is the bots affected
		String targetLine = parsedSign.getSignText()[keywordLine + 1];
		String[] targetLineTokens = targetLine.split("\\s+");

	}

	@Override
	public boolean isFirstLineOnly() {
		return false;
	}

	@Override
	public boolean isMultiline() {
		return true;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getRepetition() {
		return repetition;
	}

	public void setRepetition(int repetition) {
		this.repetition = repetition;
	}

}
