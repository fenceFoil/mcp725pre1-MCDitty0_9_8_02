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
package com.minetunes.signs;

import java.util.LinkedList;

/**
 * Represents a parsed sign, noting the text on each line and any keywords or
 * comments etc that each line represents or is a part of.
 * 
 */
public class ParsedSign {

	/**
	 * A catalog of all sign items by line number.
	 */
	private Object[] lines = new Object[4];

	/**
	 * The original sign text
	 */
	private String[] signText = new String[4];

	/**
	 * A list of all items on sign, in top-to-bottom order.
	 */
	private LinkedList<Object> signItems = new LinkedList<Object>();

	public ParsedSign(String[] originalSignText) {
		signText = originalSignText;
	}

	// public Object getLine(int editLine) {
	// return lines [editLine];
	// }

	public LinkedList<Object> getSignItems() {
		return signItems;
	}

	public Object[] getLines() {
		return lines;
	}

	public Object getLine(int line) {
		return lines[line];
	}

	public void setLines(Object[] lines) {
		this.lines = lines;
	}

	public String[] getSignText() {
		return signText;
	}

	public void setSignText(String[] signText) {
		this.signText = signText;
	}

	public void setSignItems(LinkedList<Object> signItems) {
		this.signItems = signItems;
	}

}
