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
 * Represents a single parsed keyword read from a sign.
 * 
 * TODO: Move stuff to SignParser that doesn't belong here
 * 
 */
public class ParsedKeyword {
	public static final int NO_ERRORS = 100;
	public static final int ERROR = 42;
	public static final int WARNING = 24601;
	public static final int INFO = 9430;

	// If false, this error prevents a ditty from playing
	// If true, even if there are errors, a ditty will still play
	private boolean goodKeyword = true;

	private String errorMessage = "";
	private int errorMessageType = NO_ERRORS;
	private String keyword = null;
	private String wholeKeyword = null;
	private boolean deprecated = false;
	
	public ParsedKeyword(String wholeKeyword) {
		setWholeKeyword(wholeKeyword);
	}

	/**
	 * @return the goodKeyword
	 */
	public boolean isGoodKeyword() {
		return goodKeyword;
	}

	/**
	 * @param goodKeyword
	 *            the goodKeyword to set
	 */
	public void setGoodKeyword(boolean goodKeyword) {
		this.goodKeyword = goodKeyword;
	}

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage
	 *            the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * @return the errorMessageType
	 */
	public int getErrorMessageType() {
		return errorMessageType;
	}

	/**
	 * @param errorMessageType
	 *            the errorMessageType to set
	 */
	public void setErrorMessageType(int errorMessageType) {
		this.errorMessageType = errorMessageType;
	}

	/**
	 * @return the keyword
	 */
	public String getKeyword() {
		return keyword;
	}

	/**
	 * Also checks and sets deprecation
	 * 
	 * @param keyword
	 *            the keyword to set
	 */
	public void setKeyword(String keyword) {
		this.keyword = keyword;
		setDeprecated(ParsedKeyword.isKeywordDeprecated(keyword));
	}

	/**
	 * Also checks and sets deprecation
	 * 
	 * @param keyword
	 *            the keyword to set
	 */
	public void setWholeKeyword(String line) {
		wholeKeyword = line;
		if (line != null) {
			setKeyword(wholeKeyword.split(" ")[0]);
		}
	}

	public String getWholeKeyword() {
		return wholeKeyword;
	}

	/**
	 * @return the deprecated
	 */
	public boolean isDeprecated() {
		return deprecated;
	}

	/**
	 * @param deprecated
	 *            the deprecated to set
	 */
	public void setDeprecated(boolean deprecated) {
		this.deprecated = deprecated;
	}

	public static boolean isKeywordDeprecated(String s) {
		for (String deprecatedEntry : SignParser.deprecatedKeywords) {
			if (s.equalsIgnoreCase(deprecatedEntry)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Does the work of parsing a keyword in context. Override for your keyword
	 * if necessary. Also checks for whether a keyword is on the first line of a
	 * sign or not.
	 * 
	 * @param rawLines
	 * @param keywordLine
	 * @param k
	 *            the keyword to parse, already parsed by its parse method or
	 *            returned by ParsedKeyword.parse();
	 */
	public <T extends ParsedKeyword> void parseWithMultiline(
			ParsedSign parsedSign, int keywordLine, T k) {
		// Do the first line check
		if (k.isFirstLineOnly() && keywordLine != 0) {
			k.setGoodKeyword(false);
			k.setErrorMessageType(ERROR);
			k.setErrorMessage("The keyword " + k.getKeyword()
					+ " must be on the first line of a sign.");
			return;
		}

		// Insert any other checks here later, and override with
		// Keyword-specific checks
		return;
	}

	/**
	 * Whether the keyword MUST be on the first line of a sign.
	 * 
	 * @return
	 */
	public boolean isFirstLineOnly() {
		return false;
	}

	/**
	 * Whether the keyword occupies the ENTIRE SIGN by itself when read.
	 * 
	 * @return
	 */
	public boolean isMultiline() {
		return false;
	}

}
