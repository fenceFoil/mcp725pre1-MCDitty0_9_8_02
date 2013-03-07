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

import net.minecraft.src.TileEntitySign;
import net.minecraft.src.World;

import com.minetunes.Point3D;
import com.minetunes.ditty.Ditty;
import com.minetunes.signs.ParsedSign;
import com.minetunes.signs.SignParser;
import com.minetunes.signs.keywords.argparser.ArgParseError;
import com.minetunes.signs.keywords.argparser.ArgParser;

/**
 * Represents a single parsed keyword read from a sign.
 * 
 * TODO: Move stuff to SignParser that doesn't belong here
 * 
 */
public abstract class SignTuneKeyword {
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

	protected ArgParser argParser;

	public SignTuneKeyword(String wholeKeyword) {
		setWholeKeyword(wholeKeyword);
	}

	public void parse() {
		if (argParser != null) {
			argParser.parse(wholeKeyword);
			if (argParser.getParseErrors().size() > 0) {
				setGoodKeyword(false);
			} else {
				setGoodKeyword(true);
			}
		}
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
		if (argParser != null) {
			ArgParseError e = argParser.getParseErrors().peekFirst();
			if (e != null) {
				return e.getMessage();
			} else {
				return null;
			}
		} else {
			return errorMessage;
		}
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
		if (argParser != null) {
			ArgParseError e = argParser.getParseErrors().peekFirst();
			if (e != null) {
				return (e.isFatal())? ERROR : WARNING;
			} else {
				return NO_ERRORS;
			}
		} else {
			return errorMessageType;
		}
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
		setDeprecated(SignTuneKeyword.isKeywordDeprecated(keyword));
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

	public ArgParser getArgParser() {
		return argParser;
	}

	public void setArgParser(ArgParser argParser) {
		this.argParser = argParser;
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
	public <T extends SignTuneKeyword> void parseWithMultiline(
			ParsedSign parsedSign, int keywordLine, T k) {
		// Do the first line check
		if (k.isFirstLineOnly() && keywordLine != 0) {
			k.setGoodKeyword(false);
			k.setErrorMessageType(ERROR);
			k.setErrorMessage("The keyword " + k.getKeyword()
					+ " must be on the first line of a sign.");
			return;
		}

		// Tag lines as indicated by flags
		if (isAllBelow()) {
			// Tag all lines below. Duh.
			for (int i = keywordLine; i < parsedSign.getLines().length; i++) {
				parsedSign.getLines()[i] = this;
			}
		} else if (getLineCount() > 1) {
			// Tag lines indicated below
			int linesRemaining = getLineCount();
			int currLine = keywordLine;
			while (linesRemaining > 0) {
				if (currLine < 4) {
					parsedSign.getLines()[currLine] = this;
				} else {
					break;
				}
				currLine++;
				linesRemaining--;
			}
		}

		// Use argparser
		if (argParser != null) {
			argParser.parse(parsedSign.getSignText(), keywordLine);
		}

		// TODO: Check that signs that require a certain number of lines have
		// space beneath

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
	// TODO: Make less strict, redefine as "occupies multiple lines", and
	// confirm in every keyword class
	public boolean isMultiline() {
		return false;
	}

	/**
	 * The number of lines that this keyword encompassases.
	 */
	// TODO: Implement in all keyword classes
	public int getLineCount() {
		return 1;
	}

	/**
	 * Whether a keyword controls its line and all below its line as well to the
	 * bottom of the sign
	 * 
	 * @return
	 */
	public boolean isAllBelow() {
		// TODO: IMPLEMENT FOR ALL NECESSARY KEYWORDS
		return false;
	}

	/**
	 * Executes the keyword on a given ditty and in a given location.
	 * 
	 * @param ditty
	 * @param location
	 * @return the next sign to read (as in a goto's target), or null if no
	 *         change is made
	 */
	public Point3D execute(Ditty ditty, Point3D location,
			TileEntitySign signTileEntity, Point3D nextSign, World world,
			StringBuilder readMusicString) {
		return null;
	}

	/**
	 * Indicates that a keyword can handle its entire effect on a ditty with its
	 * execute method. If false..afjekjaefkl
	 * 
	 * @return
	 */
	public boolean hasSpecialExecution() {
		return false;
	}

}
