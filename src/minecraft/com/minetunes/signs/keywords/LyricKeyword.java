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

import com.minetunes.CueScheduler;
import com.minetunes.Point3D;
import com.minetunes.config.MinetunesConfig;
import com.minetunes.ditty.Ditty;
import com.minetunes.signs.SignTuneParser;
import com.minetunes.signs.ParsedSign;

/**
 * @author William
 * 
 */
public class LyricKeyword extends SignTuneKeyword {

	private String colorCode = "";
	private int repetition = 1;
	private String label = "";
	private String lyricText = "";

	/**
	 * @param wholeKeyword
	 */
	public LyricKeyword(String wholeKeyword) {
		super(wholeKeyword);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void parse() {
		// Read arguments
		int numArgs = getWholeKeyword().split(" ").length;

		// Get arguments
		// Get label (required)
		if (numArgs >= 2) {
			label = getWholeKeyword().split(" ")[1];
			if (!isValidCueLabel(label)) {
				// Illegal label
				setGoodKeyword(false);
				setErrorMessageType(WARNING);
				setErrorMessage("Lyric names should only contain letters, numbers, and underscores.");
			}
		} else {
			// No label; this results in an error
			setGoodKeyword(false);
			setErrorMessageType(ERROR);
			setErrorMessage("Follow the Lyric keyword with a lyric name: e.g., 'Lyric chorus'.");
		}

		// Check for third argument (color code or repetition)

		// Default values for the ensuing (optional) arguments
		int repetition = getRepetition();
		String colorCode = getColorCode();

		if (numArgs >= 3) {
			String argument = getWholeKeyword().split(" ")[2];
			if (argument.matches("\\d+")) {
				// Repetition!
				repetition = Integer.parseInt(argument);
			} else if (argument.trim()
					.matches("&[\\dabcdefABCDEFlmnokrLMNOKR]")) {
				// Color code!
				colorCode = argument.trim();
			} else {
				// This means that the third word on the line is
				// not a number -- probably someone putting a
				// space in a label.
				// Throw error
				setGoodKeyword(false);
				setErrorMessageType(ERROR);
				setErrorMessage("A lyric's name can't contain spaces.");
			}
		}

		// Check for fourth argument (color code or repetition)
		if (numArgs >= 4) {
			String argument = getWholeKeyword().split(" ")[3];
			if (argument.matches("\\d+")) {
				// Repetition!
				repetition = Integer.parseInt(argument);
			} else if (argument.trim()
					.matches("&[\\dabcdefABCDEFlmnokrLMNOKR]")) {
				// Color code!
				colorCode = argument.trim();
			} else {
				// This means that the third word on the line is
				// not a number -- probably someone putting a
				// space in a label.
				// Throw error
				setGoodKeyword(false);
				setErrorMessageType(ERROR);
				setErrorMessage("A lyric's name can't contain spaces.");
			}
		}
		
		setRepetition(repetition);
		setColorCode(colorCode);

		// Too many arguments
		if (numArgs > 4) {
			setErrorMessageType(INFO);
			setErrorMessage("At most, lyric only needs a lyric name, a repetition, and a color code.");
		}

		return;
	}

	public static boolean isValidCueLabel(String label) {
		return label.matches("[a-zA-Z0-9_]*");
	}

	/**
	 * @return the colorCode
	 */
	public String getColorCode() {
		return colorCode;
	}

	/**
	 * @param colorCode
	 *            the colorCode to set
	 */
	public void setColorCode(String colorCode) {
		this.colorCode = colorCode;
	}

	/**
	 * @return the repetition
	 */
	public int getRepetition() {
		return repetition;
	}

	/**
	 * @param repetition
	 *            the repetition to set
	 */
	public void setRepetition(int repetition) {
		this.repetition = repetition;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public <T extends SignTuneKeyword> void parseWithMultiline(
			ParsedSign parsedSign, int keywordLine, T k) {
		super.parseWithMultiline(parsedSign, keywordLine, k);

		// Read lyric text (but not if there's no line after the keyword)
		if (keywordLine < 3) {
			setLyricText(SignTuneParser.readLyricFromSign(keywordLine + 1,
					parsedSign.getSignText(), getColorCode()));
		}
	}

	@Override
	public boolean isMultiline() {
		return true;
	}

	@Override
	public Point3D execute(Ditty ditty, Point3D location,
			TileEntitySign signTilEntity, Point3D nextSign, World world,
			StringBuilder b) {
		// Check that this isn't on the last line (no lyric text
		// possible), after parsing arguments.

		// Adding to existing lyric?
		if (MinetunesConfig.getBoolean("lyrics.enabled")) {
			CueScheduler lyrics = ditty.getLyricsStorage();
			lyrics.addLyricText(getLabel(), lyricText, getRepetition());
		}

		return null;
	}

	public String getLyricText() {
		return lyricText;
	}

	public void setLyricText(String lyricText) {
		this.lyricText = lyricText;
	}

	@Override
	public boolean isAllBelow() {
		return true;
	}

}
