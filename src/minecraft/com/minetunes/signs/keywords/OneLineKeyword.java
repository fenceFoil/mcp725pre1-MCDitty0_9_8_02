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
package com.minetunes.signs.keywords;

import net.minecraft.src.TileEntitySign;
import net.minecraft.src.World;

import com.minetunes.Point3D;
import com.minetunes.ditty.Ditty;
import com.minetunes.signs.Comment;
import com.minetunes.signs.ParsedSign;
import com.minetunes.signs.SignDitty;
import com.minetunes.signs.SignParser;
import com.minetunes.signs.TileEntitySignMinetunes;

/**
 * @author William
 * 
 */
public class OneLineKeyword extends SignTuneKeyword {

	/**
	 * @param wholeKeyword
	 */
	public OneLineKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	@Override
	public Point3D execute(Ditty ditty, Point3D currSignPoint,
			TileEntitySign signTileEntity, Point3D nextSign, World world,
			StringBuilder readMusicString) {
		// Try to get a no-codes version of the sign's text
		String[] signText = signTileEntity.signText;
		if (signTileEntity instanceof TileEntitySignMinetunes) {
			signText = ((TileEntitySignMinetunes) signTileEntity)
					.getSignTextNoCodes();
		}

		// TODO: Cheat to get the line number. Most of this stuff should be in
		// "parse()"
		ParsedSign s = SignParser.parseSign(signText);
		int line = 0;
		for (int i = 0; i < 3; i++) {
			if (s.getLine(i) instanceof OneLineKeyword) {
				// Bingo
				line = i;
				break;
			}
		}

		// Smash all lines below this keyword onto the one
		// line
		// (the one after this keyword)
		// Throw an error if there are keywords below
		String comboLine = "";
		for (int i = line + 1; i < signText.length; i++) {
			String onelineLine = signText[i];

			// Check for keywords
			String keywordFromOnelineLine = SignParser
					.recognizeKeyword(onelineLine);
			if (keywordFromOnelineLine != null) {
				// Highlight both this keyword and the offending
				// keyword being processed
				if (ditty instanceof SignDitty) {
					((SignDitty) ditty).addErrorHighlight(currSignPoint, i);
				}
				if (ditty instanceof SignDitty) {
					((SignDitty) ditty).addErrorHighlight(currSignPoint, line);
				}
				ditty.addErrorMessage("§cOneline cannot deal with the keyword (§b"
						+ keywordFromOnelineLine
						+ "§c): oneline only works with MusicString tokens. Remove any keywords from beneath Oneline keywords.");
				break;
			}

			// Check for comments; if line is not a comment, add
			// it.
			if (!Comment.isLineComment(onelineLine)) {
				comboLine += onelineLine;
			}
		}

		// Add comboline to the music buffer
		if (!ditty
				.addMusicStringTokens(readMusicString, comboLine.trim(), true)) {
			// If the comboline contained errors, highlight all
			// lines in comboLine
			for (int i = line + 1; i < signText.length; i++) {
				if (ditty instanceof SignDitty) {
					((SignDitty) ditty).addErrorHighlight(currSignPoint, i);
				}
			}
		}

		// // Do not read any more lines from this sign
		return null;
	}

	@Override
	public boolean isMultiline() {
		return true;
	}

	@Override
	public boolean isAllBelow() {
		return true;
	}

}
