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

import org.jfugue.JFugueDefinitions;
import org.jfugue.factories.NoteFactory;
import org.jfugue.factories.NoteFactory.NoteContext;

/**
 * 
 */
public class StaccatoKeyword extends ParsedKeyword {

	private int eighths = 2;

	private int duration = -1;
	
	private String durationString = "";

	public StaccatoKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	public static StaccatoKeyword parse(String rawLine) {
		StaccatoKeyword keyword = new StaccatoKeyword(rawLine);

		String[] args = rawLine.split(" ");
		int numArgs = rawLine.split(" ").length;

		int argsLeft = 0;
		if (numArgs <= 1) {
			return keyword;
		} else {
			argsLeft = numArgs - 1;
		}

		boolean eighthsRead = false;
		boolean durationRead = false;
		int currArg = 1;
		while (argsLeft > 0 && !(eighthsRead && durationRead)) {

			String argument = args[currArg];

			if (argument.trim().matches("\\d+") && !eighthsRead) {
				Integer eighthsArg = Integer.parseInt(argument);
				if (eighthsArg > 8 || eighthsArg < 0) {
					// Out of range
					keyword.setGoodKeyword(false);
					keyword.setErrorMessageType(ERROR);
					keyword.setErrorMessage("Follow Staccato with a number from 0 to 8.");
					return keyword;
				}
				keyword.setEighths(eighthsArg);
				
				eighthsRead = true;
			} else {
				double decimalDuration = parseLetterDuration(argument.toUpperCase(), argument.length(), 0);
				keyword.setDuration((int) (decimalDuration * JFugueDefinitions.SEQUENCE_RESOLUTION));
				
				keyword.setDurationString (argument);
				
				//System.out.println (decimalDuration + ":"+argument);
				
				durationRead = true;
			}

			argsLeft--;
			currArg++;
		}

		return keyword;
	}

	public int getEighths() {
		return eighths;
	}

	public void setEighths(int eighths) {
		this.eighths = eighths;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getDurationString() {
		return durationString;
	}

	public void setDurationString(String durationString) {
		this.durationString = durationString;
	}

	// Copied from JFugue's MusicStringParser (I know, bad form); modified
	private static double parseLetterDuration(String s, int slen, int index) {
		// Check duration
		boolean durationExists = true;
		boolean isDotted = false;
		double duration = 0;

		while (durationExists == true) {
			int durationNumber = 0;
			// See if the note has a duration
			// Duration is optional; default is Q (4)
			if (index < slen) {
				char durationChar = s.charAt(index);
				switch (durationChar) {
				case 'W':
					durationNumber = 1;
					break;
				case 'H':
					durationNumber = 2;
					break;
				case 'Q':
					durationNumber = 4;
					break;
				case 'I':
					durationNumber = 8;
					break;
				case 'S':
					durationNumber = 16;
					break;
				case 'T':
					durationNumber = 32;
					break;
				case 'X':
					durationNumber = 64;
					break;
				case 'O':
					durationNumber = 128;
					break;
				default:
					index--;
					durationExists = false;
					break;
				}
				index++;
				if ((index < slen) && (s.charAt(index) == '.')) {
					isDotted = true;
					index++;
				}

				if (durationNumber > 0) {
					double d = 1.0 / durationNumber;
					if (isDotted) {
						duration += d + (d / 2.0);
					} else {
						duration += d;
					}
				}
			} else {
				durationExists = false;
			}
		}

		return duration;
	}

}
