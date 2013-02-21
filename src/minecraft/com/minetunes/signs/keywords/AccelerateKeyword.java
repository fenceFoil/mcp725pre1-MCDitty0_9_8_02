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

import org.jfugue.JFugueDefinitions;
import org.jfugue.factories.NoteFactory;
import org.jfugue.factories.NoteFactory.NoteContext;

/**
 * Keyword format:
 * 
 * Accel [bpm] [duration]
 * 
 * [bpm]: -300 to 300 Int [duration]: JFugue duration
 */
public class AccelerateKeyword extends ParsedKeyword {

	private int bpm = 2;

	// NOT infinity in this case
	private int duration = 0;

	private String durationString = "";

	public AccelerateKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	public static AccelerateKeyword parse(String rawLine) {
		AccelerateKeyword keyword = new AccelerateKeyword(rawLine);

		String[] args = rawLine.split(" ");
		int numArgs = rawLine.split(" ").length;

		int argsLeft = 0;
		if (numArgs <= 1) {
			return keyword;
		} else {
			argsLeft = numArgs - 1;
		}

		boolean tonesRead = false;
		boolean durationRead = false;
		int currArg = 1;
		while (argsLeft > 0 && !(tonesRead && durationRead)) {

			String argument = args[currArg];

			if (argument.trim().matches("[-\\+]?\\d+") && !tonesRead) {
				Integer arg = Integer.parseInt(argument);
				if (arg > 300 || arg < -300) {
					// Out of range
					keyword.setGoodKeyword(false);
					keyword.setErrorMessageType(ERROR);
					keyword.setErrorMessage("Follow Accel with bpm to increase (-300 to +300).");
					return keyword;
				}
				keyword.setBPM(arg);

				tonesRead = true;
			} else {
				double decimalDuration = parseLetterDuration(
						argument.toUpperCase(), argument.length(), 0);
				keyword.setDuration((int) (decimalDuration * JFugueDefinitions.SEQUENCE_RESOLUTION));
				keyword.setDurationString(argument);

				// System.out.println (decimalDuration + ":"+argument);

				durationRead = true;
			}

			argsLeft--;
			currArg++;
		}

		// Ensure that tones are specified
		if (!tonesRead) {
			keyword.setGoodKeyword(false);
			keyword.setErrorMessageType(ERROR);
			keyword.setErrorMessage("Follow Accel with bpm to increase (-300 to +300).");
			return keyword;
		}

		return keyword;
	}

	public int getBPM() {
		return bpm;
	}

	public void setBPM(int semitones) {
		this.bpm = semitones;
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
		return StaccatoKeyword.parseLetterDuration(s, slen, index);
	}

}
