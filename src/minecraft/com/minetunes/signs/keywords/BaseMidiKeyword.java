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

import java.io.File;

import com.minetunes.config.MinetunesConfig;
import com.minetunes.signs.ParsedSign;
import com.minetunes.signs.SignParser;

/**
 * @author William
 * 
 */
public class BaseMidiKeyword extends SignTuneKeyword {

	protected File midiFile;

	public BaseMidiKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	@Override
	public <T extends SignTuneKeyword> void parseWithMultiline(
			ParsedSign parsedSign, int keywordLine, T k) {
		super.parseWithMultiline(parsedSign, keywordLine, k);

		String[] signText = parsedSign.getSignText();

		// Read the filename from line 2
		// Line 2 contains the filename
		String givenFilename = signText[1].trim();
		if (SignParser.recognizeKeyword(signText[1]) != null) {
			// If a keyword is found beneath the midi sign,
			// notify user
			// ditty.addErrorHighlight(currSignPoint, 1);
			// ditty.addErrorMessage("§cA keyword (§b"
			// + SignParser.recognizeKeyword(signText[1])
			// +
			// "§c) was found on the second line of a midi sign instead of a filename.");
			setErrorMessage("Instead of a filename, a keyword was found beneath a Midi keyword.");
			setErrorMessageType(ERROR);
			// Bad filename: non-alphanumeric characters
			// simpleLog("Bad filename is keyword: " + givenFilename);
		} else if (!givenFilename.matches("[\\d\\w]*")
				&& (!givenFilename.equals(""))) {
			// ditty.addErrorHighlight(currSignPoint, 1);
			// ditty.addErrorMessage("§cA midi file name should only contain letters and numbers (no spaces)");
			setErrorMessage("A MIDI file name should only contains letters and numbers (no spaces)");
			setErrorMessageType(ERROR);
			// Bad filename: non-alphanumeric characters
			// simpleLog("Bad filename: " + givenFilename);
		} else if (givenFilename.equals("")) {
			// Empty filenames are frowned upon
			// ditty.addErrorHighlight(currSignPoint, 0);
			// ditty.addErrorMessage("§cNo file name was written on the second line of the midi sign");
			setErrorMessage("No MIDI file is given after a MIDI keyword.");
			setErrorMessageType(ERROR);
		}

		// Otherwise, good filename. Note it, and move on.
		// Will save later, once signs are read.
		setMidiFile(new File(MinetunesConfig.getMinetunesDir().getPath()
				+ File.separator + "midi", givenFilename + ".mid"));
		// simpleLog("Good filename: midi is " + midiSaveFile.getPath());
	}

	@Override
	public boolean isMultiline() {
		return true;
	}

	@Override
	public boolean isFirstLineOnly() {
		return true;
	}

	public File getMidiFile() {
		return midiFile;
	}

	public void setMidiFile(File midiFile) {
		this.midiFile = midiFile;
	}

	@Override
	public int getLineCount() {
		return 2;
	}

}
