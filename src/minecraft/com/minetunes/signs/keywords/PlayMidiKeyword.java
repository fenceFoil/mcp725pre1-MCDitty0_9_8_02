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

import com.minetunes.signs.BlockSignMinetunes;
import com.minetunes.signs.ParsedSign;

/**
 *
 */
public class PlayMidiKeyword extends ParsedKeyword {

	private String midiFilename;

	public PlayMidiKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	public static PlayMidiKeyword parse(String rawLine) {
		PlayMidiKeyword keyword = new PlayMidiKeyword(rawLine);
		return keyword;
	}

	@Override
	public boolean isFirstLineOnly() {
		return true;
	}

	@Override
	public boolean isMultiline() {
		return true;
	}

	@Override
	public <T extends ParsedKeyword> void parseWithMultiline(
			ParsedSign parsedSign, int keywordLine, T k) {
		super.parseWithMultiline(parsedSign, keywordLine, k);
		
		// Mark filename line
		parsedSign.getLines()[1] = this;

		String givenFilename = (String) parsedSign.getSignText()[1];
		if (!givenFilename.matches("[\\d\\w]*") && (!givenFilename.equals(""))) {
			// Bad filename: non-alphanumeric characters
			setGoodKeyword(false);
			setErrorMessageType(ERROR);
			setErrorMessage("A MIDI file name should only contain letters and numbers (no spaces)");
			BlockSignMinetunes.simpleLog("Bad filename: " + givenFilename);
			return;
		} else if (givenFilename.equals("")) {
			// Empty filenames are frowned upon
			setGoodKeyword(false);
			setErrorMessageType(ERROR);
			setErrorMessage("Put a file name on the line after a "
					+ getKeyword() + " keyword.");
			return;
		}

		// Otherwise, good filename
		setMidiFilename(givenFilename + ".mid");
	}

	public String getMidiFilename() {
		return midiFilename;
	}

	public void setMidiFilename(String midiFilename) {
		this.midiFilename = midiFilename;
	}

}
