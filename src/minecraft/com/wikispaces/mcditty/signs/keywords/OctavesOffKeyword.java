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

import java.util.HashSet;

import org.jfugue.JFugueDefinitions;

/**
 * Keyword format:
 * 
 * OctavesOff [octaves] ...
 * 
 * [octaves]: -10 to 10 Ints; multiple; optional (default is just a single 1)
 */
public class OctavesOffKeyword extends ParsedKeyword {

	private HashSet<Integer> octaves = new HashSet<Integer>();

	public OctavesOffKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	public static OctavesOffKeyword parse(String rawLine) {
		
		OctavesOffKeyword keyword = new OctavesOffKeyword(rawLine);

		String[] args = rawLine.split(" ");
		int numArgs = rawLine.split(" ").length;

		int argsLeft = 0;
		if (numArgs <= 1) {
			return keyword;
		} else {
			argsLeft = numArgs - 1;
		}

		int currArg = 1;
		boolean octavesSpecified = false;
		while (argsLeft > 0) {
			String argument = args[currArg];

			if (argument.trim().matches("[-\\+]?\\d+")) {
				Integer tonesArg = Integer.parseInt(argument);
				if (tonesArg > 10 || tonesArg < -10) {
					// Out of range
					keyword.setGoodKeyword(false);
					keyword.setErrorMessageType(WARNING);
					keyword.setErrorMessage("Follow OctavesOff with numbers (-10 to +10).");
					return keyword;
				}
				keyword.getOctaves().add(tonesArg);
				
				// Note that some octaves were specified
				octavesSpecified = true;
			} else {
				// Bad argument
				keyword.setGoodKeyword(false);
				keyword.setErrorMessageType(ERROR);
				keyword.setErrorMessage("Follow OctavesOff with numbers (-10 to +10).");
				return keyword;
			}

			argsLeft--;
		}
		
		return keyword;
	}

	public HashSet<Integer> getOctaves() {
		return octaves;
	}

	public void setOctaves(HashSet<Integer> octaves) {
		this.octaves = octaves;
	}

}
