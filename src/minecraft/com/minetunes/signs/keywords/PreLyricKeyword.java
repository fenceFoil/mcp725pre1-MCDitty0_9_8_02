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
public class PreLyricKeyword extends SignTuneKeyword {

	private String colorCode = "";
	// private int repetition = 1;
	private String label = "";
	private String lyricText = "";

	/**
	 * @param wholeKeyword
	 */
	public PreLyricKeyword(String wholeKeyword) {
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
				setErrorMessageType(SignTuneKeyword.WARNING);
				setErrorMessage("Lyric names should only contain letters, numbers, and underscores.");
			}
		} else {
			// No label; this results in an error
			setGoodKeyword(false);
			setErrorMessageType(SignTuneKeyword.ERROR);
			setErrorMessage("Follow the PreLyric keyword with a lyric name: e.g., 'PreLyric chorus'.");
		}

		// Check for third argument (color code or repetition)

		// Default values for the ensuing (optional) arguments
		String colorCode = getColorCode();

		if (numArgs >= 3) {
			String argument = getWholeKeyword().split(" ")[2];
			if (argument.trim().matches("&[\\dabcdefABCDEFlmnokrLMNOKR]")) {
				// Color code!
				colorCode = argument.trim();
			} else {
				// This means that the third word on the line is
				// not a number -- probably someone putting a
				// space in a label.
				// Throw error
				setGoodKeyword(false);
				setErrorMessageType(SignTuneKeyword.ERROR);
				setErrorMessage("A lyric's name can't contain spaces.");
			}
		}
		
		setColorCode(colorCode);

		// Too many arguments
		if (numArgs > 3) {
			setErrorMessageType(INFO);
			setErrorMessage("At most, PreLyric only needs a lyric name and a color code.");
		}
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

		// If there's a line below (or more), read as the lyric prefix
		if (keywordLine < 3) {
			setLyricText(SignTuneParser.readLyricFromSign(keywordLine + 1,
					parsedSign.getSignText(), getColorCode()));
		}
	}

	@Override
	public boolean isMultiline() {
		return true;
	}

	public String getLyricText() {
		return lyricText;
	}

	public void setLyricText(String lyricText) {
		this.lyricText = lyricText;
	}

	@Override
	public Point3D execute(Ditty ditty, Point3D location,
			TileEntitySign signTileEntity, Point3D nextSign, World world,
			StringBuilder b) {
		// Adding to existing lyric?
		if (MinetunesConfig.getBoolean("lyrics.enabled")) {
			CueScheduler lyrics = ditty.getLyricsStorage();
			lyrics.addLyricPreText(getLabel(), getLyricText());
		}

		return null;
	}

	@Override
	public boolean isAllBelow() {
		return true;
	}

}
