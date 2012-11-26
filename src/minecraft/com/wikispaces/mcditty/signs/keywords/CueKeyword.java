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

/**
 *
 */
public class CueKeyword extends ParsedKeyword {

	private String label = "";
	private int repetition = 1;

	public CueKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	public static CueKeyword parse(String currLine) {
		CueKeyword keyword = new CueKeyword(currLine);

		// Read arguments
		int numArgs = currLine.split(" ").length;

		// Get arguments
		// Get label (required)
		if (numArgs >= 2) {
			keyword.label = currLine.split(" ")[1];
			if (!LyricKeyword.isValidCueLabel(keyword.label)) {
				// Illegal label
				keyword.setGoodKeyword(false);
				keyword.setErrorMessageType(ParsedKeyword.WARNING);
				keyword.setErrorMessage("Cue names should only contain letters, numbers, and underscores.");
			}
		} else {
			// No label; this results in an error
			keyword.setGoodKeyword(false);
			keyword.setErrorMessageType(ParsedKeyword.ERROR);
			keyword.setErrorMessage("Follow the Cue keyword with a cue name: e.g., 'Cue chorus'.");
		}

		// Check for third argument (color code or repetition)

		// Default values for the ensuing (optional) arguments
		int repetition = keyword.getRepetition();

		if (numArgs >= 3) {
			String argument = currLine.split(" ")[2];
			if (argument.matches("\\d+")) {
				// Repetition!
				repetition = Integer.parseInt(argument);
			} else {
				// This means that the third word on the line is
				// not a number -- probably someone putting a
				// space in a label.
				// Throw error
				keyword.setGoodKeyword(false);
				keyword.setErrorMessageType(ParsedKeyword.ERROR);
				keyword.setErrorMessage("A cue's name can't contain spaces.");
			}
		} 
		
		// Too many arguments
		if (numArgs > 3) {
			keyword.setErrorMessageType(INFO);
			keyword.setErrorMessage("At most, Cue only needs to be followed with a name and repetition.");
		}

		// Set the read (or default) color codes and repetitions
		keyword.repetition = repetition;

		return keyword;
	}

	@Override
	public <T extends ParsedKeyword> void parseWithMultiline(
			ParsedSign parsedSign, int keywordLine, T k) {
		super.parseWithMultiline(parsedSign, keywordLine, k);
		
		// Figure out how many lines are beneath the first line of the cue
		int linesAfterFirst = 4 - keywordLine;
		
		// Tag all lines on and below the keyword
		if (linesAfterFirst <= 0) {
			return;
		} else {
			for (int i=keywordLine;i<4;i++) {
				parsedSign.getLines()[i] = this;
			}
		}
		
		// Line 1 beneath the sign is the target
		// Form: [target type]
		// if [target] == bot, the next argument is the bots affected
		String targetLine = parsedSign.getSignText()[keywordLine+1];
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
