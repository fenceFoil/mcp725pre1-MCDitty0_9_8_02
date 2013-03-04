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

import javax.swing.text.AbstractDocument.LeafElement;

/**
 * @author William
 * 
 */
public class ProxPadKeyword extends SignTuneKeyword {

	private int width = 0;
	private int length = 0;
	private int height = 0;

	/**
	 * @param wholeKeyword
	 */
	public ProxPadKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	@Override
	public void parse() {
		if (getKeyword().toLowerCase().equals("proximity")) {
			// Proximity keyword: a 1x1 version of a proxpad
			width = 1;
			length = 1;
			height = 1;
			return;
		}

		// Break up input string
		String[] args = getWholeKeyword().split(" ");
		int numArgs = args.length;

		if (numArgs != 3 && numArgs != 4) {
			setErrorMessageType(ERROR);
			setGoodKeyword(false);
			if (numArgs == 1) {
				setErrorMessage("Follow Area with two or three numbers (width, depth, and (optional) height of area).");
			} else if (numArgs == 2) {
				setErrorMessage("Follow Area with two or three numbers (width, depth, and (optional) height of area).");
			} else {
				setErrorMessage("Follow Area with two or three numbers (width, depth, and (optional) height of area).");
			}
			return;
		}

		// Read width
		String argument = args[1];
		if (argument.matches("\\d+")) {
			width = Integer.parseInt(argument);
		} else {
			setGoodKeyword(false);
			setErrorMessageType(ERROR);
			setErrorMessage("First number (width) isn't a number.");
			return;
		}

		// range check
		if (width <= 0 || width >= 16) {
			setGoodKeyword(false);
			setErrorMessageType(ERROR);
			setErrorMessage("Area widths range from 1 to 15.");
			return;
		}

		// Read length
		argument = args[2];
		if (argument.matches("\\d+")) {
			length = Integer.parseInt(argument);
		} else {
			setGoodKeyword(false);
			setErrorMessageType(ERROR);
			setErrorMessage("Second number (depth) isn't a number.");
			return;
		}

		// range check
		if (length <= 0 || length >= 16) {
			setGoodKeyword(false);
			setErrorMessageType(ERROR);
			setErrorMessage("Area depths range from 1 to 15.");
			return;
		}

		// Read height
		height = 1;
		if (numArgs >= 4) {
			argument = args[3];
			if (argument.matches("\\d+")) {
				height = Integer.parseInt(argument);
			} else {
				setGoodKeyword(false);
				setErrorMessageType(ERROR);
				setErrorMessage("Third number (height) isn't a number.");
				return;
			}
		}

		// range check
		if (height <= 0 || height >= 16) {
			setGoodKeyword(false);
			setErrorMessageType(ERROR);
			setErrorMessage("Area heights range from 1 to 15.");
			return;
		}
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
