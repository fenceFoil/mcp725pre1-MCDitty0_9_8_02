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
 * @author William
 * 
 */
public class ProxPadKeyword extends ParsedKeyword {

	private int width = 0;
	private int length = 0;
	private int height = 0;

	/**
	 * @param wholeKeyword
	 */
	public ProxPadKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	public static ProxPadKeyword parse(String currLine) {
		ProxPadKeyword k = new ProxPadKeyword(currLine);

		// Break up input string
		String[] args = currLine.split(" ");
		int numArgs = args.length;

		if (numArgs != 3 && numArgs != 4) {
			k.setErrorMessageType(ERROR);
			k.setGoodKeyword(false);
			if (numArgs == 1) {
				k.setErrorMessage("Follow Area with two or three numbers (width, depth, and (optional) height of area).");
			} else if (numArgs == 2) {
				k.setErrorMessage("Follow Area with two or three numbers (width, depth, and (optional) height of area).");
			} else {
				k.setErrorMessage("Follow Area with two or three numbers (width, depth, and (optional) height of area).");
			}
			return k;
		}

		// Read width
		String argument = args[1];
		if (argument.matches("\\d+")) {
			k.width = Integer.parseInt(argument);
		} else {
			k.setGoodKeyword(false);
			k.setErrorMessageType(ERROR);
			k.setErrorMessage("First number (width) isn't a number.");
			return k;
		}

		// range check
		if (k.width <= 0 || k.width >= 16) {
			k.setGoodKeyword(false);
			k.setErrorMessageType(ERROR);
			k.setErrorMessage("Area widths range from 1 to 15.");
			return k;
		}

		// Read length
		argument = args[2];
		if (argument.matches("\\d+")) {
			k.length = Integer.parseInt(argument);
		} else {
			k.setGoodKeyword(false);
			k.setErrorMessageType(ERROR);
			k.setErrorMessage("Second number (depth) isn't a number.");
			return k;
		}

		// range check
		if (k.length <= 0 || k.length >= 16) {
			k.setGoodKeyword(false);
			k.setErrorMessageType(ERROR);
			k.setErrorMessage("Area depths range from 1 to 15.");
			return k;
		}

		// Read height
		k.height = 1;
		if (numArgs >= 4) {
			argument = args[3];
			if (argument.matches("\\d+")) {
				k.height = Integer.parseInt(argument);
			} else {
				k.setGoodKeyword(false);
				k.setErrorMessageType(ERROR);
				k.setErrorMessage("Third number (height) isn't a number.");
				return k;
			}
		}

		// range check
		if (k.height <= 0 || k.height >= 16) {
			k.setGoodKeyword(false);
			k.setErrorMessageType(ERROR);
			k.setErrorMessage("Area heights range from 1 to 15.");
			return k;
		}

		return k;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width
	 *            the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

	/**
	 * @param length
	 *            the length to set
	 */
	public void setLength(int length) {
		this.length = length;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height
	 *            the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

}
