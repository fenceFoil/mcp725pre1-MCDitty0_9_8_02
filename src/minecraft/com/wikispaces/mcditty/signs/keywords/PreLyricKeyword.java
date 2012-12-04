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
 * @author William
 * 
 */
public class PreLyricKeyword extends ParsedKeyword {

	private String colorCode = "";
	//private int repetition = 1;
	private String label = "";
	private String lyricText = "";

	/**
	 * @param wholeKeyword
	 */
	public PreLyricKeyword(String wholeKeyword) {
		super(wholeKeyword);
		// TODO Auto-generated constructor stub
	}

	public static PreLyricKeyword parse(String currLine) {
		PreLyricKeyword keyword = new PreLyricKeyword(currLine);

		// Read arguments
		int numArgs = currLine.split(" ").length;

		// Get arguments
		// Get label (required)
		if (numArgs >= 2) {
			keyword.label = currLine.split(" ")[1];
			if (!isValidCueLabel(keyword.label)) {
				// Illegal label
				keyword.setGoodKeyword(false);
				keyword.setErrorMessageType(ParsedKeyword.WARNING);
				keyword.setErrorMessage("Lyric names should only contain letters, numbers, and underscores.");
			}
		} else {
			// No label; this results in an error
			keyword.setGoodKeyword(false);
			keyword.setErrorMessageType(ParsedKeyword.ERROR);
			keyword.setErrorMessage("Follow the PreLyric keyword with a lyric name: e.g., 'PreLyric chorus'.");
		}

		// Check for third argument (color code or repetition)

		// Default values for the ensuing (optional) arguments
		String colorCode = keyword.getColorCode();

		if (numArgs >= 3) {
			String argument = currLine.split(" ")[2];
			if (argument.trim().matches("&[\\dabcdefABCDEFlmnokrLMNOKR]")) {
				// Color code!
				colorCode = argument.trim();
			} else {
				// This means that the third word on the line is
				// not a number -- probably someone putting a
				// space in a label.
				// Throw error
				keyword.setGoodKeyword(false);
				keyword.setErrorMessageType(ParsedKeyword.ERROR);
				keyword.setErrorMessage("A lyric's name can't contain spaces.");
			}
		}
		
		// Too many arguments
		if (numArgs > 3) {
			keyword.setErrorMessageType(INFO);
			keyword.setErrorMessage("At most, PreLyric only needs a lyric name and a color code.");
		}

		// Set the read (or default) color codes and repetitions
		keyword.colorCode = colorCode;

		return keyword;
	}

	public static boolean isValidCueLabel(String label) {
		return label.matches("[a-zA-Z0-9_]*");
	}

	/**
	 * @return the colorCode
	 */
	public String getColorCode() {
		return colorCode;
	}

	/**
	 * @param colorCode
	 *            the colorCode to set
	 */
	public void setColorCode(String colorCode) {
		this.colorCode = colorCode;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public <T extends ParsedKeyword> void parseWithMultiline(
			ParsedSign parsedSign, int keywordLine, T k) {
		super.parseWithMultiline(parsedSign, keywordLine, k);
		
		// TODO: Read lyric text
		StringBuilder lyricTextB = new StringBuilder();
		for (int i=keywordLine+1;i<parsedSign.getLines().length;i++) {
			// TODO
			parsedSign.getLines()[i] = this;
		}
		lyricTextB.append("LYRIC TEXT PLACEHOLDER -- SEE parseWithMultiline()");
		lyricText = lyricTextB.toString();
	}

	@Override
	public boolean isMultiline() {
		return true;
	}

}
