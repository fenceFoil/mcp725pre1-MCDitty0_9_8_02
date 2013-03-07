/**
 * Copyright (c) 2012-2013 William Karnavas 
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
package com.minetunes.signs.keywords.argparser;

import java.util.LinkedList;

/**
 * @author William
 * 
 */
public class IntArg extends Arg {

	private Integer parsedInt = null;
	private int lowBound = 0;
	private int highBound = 0;
	private Integer defaultValue = null;

	public IntArg(String name, boolean optional, int lowBound, int highBound, Integer defaultValue) {
		super (name, optional);
		setLowBound(lowBound);
		setHighBound(highBound);
		setDefaultValue(defaultValue);
	}

	public IntArg(String name, boolean optional, int lowBound, int highBound) {
		this(name, optional, lowBound, highBound, null);
	}

	public IntArg(String name, boolean optional, Integer defaultValue) {
		this(name, optional, Integer.MIN_VALUE, Integer.MAX_VALUE, defaultValue);
	}
	
	public IntArg(String name, boolean optional) {
		this(name, optional, null);
	}

	/**
	 * Rests the parsed value to null.
	 */
	@Override
	public void reset() {
		super.reset();
		parsedInt = null;
	}

	@Override
	public int parse(LinkedList<String> tokens) {
		super.parse(tokens);

		// Max of one token used
		String numToken = tokens.peekFirst();

		// Check for null token
		if (numToken == null) {
			if (!isOptional()) {
				addMissingError();
			}
			return 0;
		}
		
		setParsed(true);

		// Validate token as integer and in range
		String regex = "-?\\d+";
		if (!numToken.matches(regex)) {
			addParseError(new ArgParseError(this, "not a number.", true));
			return 0;
		}
		int num = Integer.parseInt(numToken);
		if (num < lowBound || num > highBound) {
			// Out of bounds
			addParseError(new ArgParseError(this, "must be between " + lowBound
					+ " and " + highBound, true));
			return 0;
		}

		// Valid number -- mission complete
		parsedInt = num;
		return 1;
	}

	public int getLowBound() {
		return lowBound;
	}

	public void setLowBound(int lowBound) {
		this.lowBound = lowBound;
	}

	public int getHighBound() {
		return highBound;
	}

	public void setHighBound(int highBound) {
		this.highBound = highBound;
	}

	/**
	 * handles default
	 * 
	 * @return
	 */
	public Integer getParsedInt() {
		if (parsedInt == null && defaultValue != null) {
			return defaultValue;
		} else {
			return parsedInt;
		}
	}

	public Integer getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Integer defaultValue) {
		this.defaultValue = defaultValue;
	}

}
