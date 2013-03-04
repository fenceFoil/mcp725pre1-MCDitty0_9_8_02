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

import org.jfugue.JFugueDefinitions;
import org.jfugue.factories.NoteFactory;
import org.jfugue.factories.NoteFactory.NoteContext;

import com.minetunes.Point3D;
import com.minetunes.ditty.Ditty;
import com.minetunes.signs.SignTuneParser;
import com.minetunes.signs.SignParser;

/**
 * Keyword format:
 * 
 * Staccato [eighths] [duration]
 * 
 * [eighths]: 0-8 Int [duration]: JFugue duration
 */
public class StaccatoKeyword extends SignTuneKeyword {

	private int eighths = 2;

	private int duration = -1;

	private String durationString = "";

	public StaccatoKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	@Override
	public void parse() {
		String[] args = getWholeKeyword().split(" ");
		int numArgs = getWholeKeyword().split(" ").length;

		int argsLeft = 0;
		if (numArgs <= 1) {
			return;
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
					setGoodKeyword(false);
					setErrorMessageType(ERROR);
					setErrorMessage("Follow Staccato with a number from 0 to 8.");
					return;
				}
				setEighths(eighthsArg);

				eighthsRead = true;
			} else {
				double decimalDuration = parseLetterDuration(
						argument.toUpperCase(), argument.length(), 0);
				setDuration((int) (decimalDuration * JFugueDefinitions.SEQUENCE_RESOLUTION));

				setDurationString(argument);

				// System.out.println (decimalDuration + ":"+argument);

				durationRead = true;
			}

			argsLeft--;
			currArg++;
		}

		return;
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
	public static double parseLetterDuration(String s, int slen, int index) {
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

	@Override
	public Point3D execute(Ditty ditty, Point3D location,
			TileEntitySign signTileEntity, Point3D nextSign, World world,
			StringBuilder readMusicString) {
		// Create and add a staccato note effect token
		String staccatoToken = SignTuneParser.createNoteEffectToken(false,
				SignTuneParser.NOTE_EFFECT_STACCATO, getEighths(),
				getDuration());

		ditty.addMusicStringTokens(readMusicString, staccatoToken, false);

		return null;
	}

}
