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

import com.minetunes.signs.keywords.AccelerateKeyword;
import com.minetunes.signs.keywords.DiscoKeyword;
import com.minetunes.signs.keywords.EmitterKeyword;
import com.minetunes.signs.keywords.ExplicitGotoKeyword;
import com.minetunes.signs.keywords.FireworkKeyword;
import com.minetunes.signs.keywords.GotoKeyword;
import com.minetunes.signs.keywords.LyricKeyword;
import com.minetunes.signs.keywords.MaxPlaysKeyword;
import com.minetunes.signs.keywords.NewBotKeyword;
import com.minetunes.signs.keywords.NoteblockTriggerKeyword;
import com.minetunes.signs.keywords.OctavesKeyword;
import com.minetunes.signs.keywords.OctavesOffKeyword;
import com.minetunes.signs.keywords.ParsedKeyword;
import com.minetunes.signs.keywords.PattKeyword;
import com.minetunes.signs.keywords.PatternKeyword;
import com.minetunes.signs.keywords.PlayMidiKeyword;
import com.minetunes.signs.keywords.PreLyricKeyword;
import com.minetunes.signs.keywords.ProxPadKeyword;
import com.minetunes.signs.keywords.RepeatKeyword;
import com.minetunes.signs.keywords.SFXInstKeyword;
import com.minetunes.signs.keywords.SFXInstOffKeyword;
import com.minetunes.signs.keywords.SFXKeyword;
import com.minetunes.signs.keywords.SaveMidiKeyword;
import com.minetunes.signs.keywords.StaccatoKeyword;
import com.minetunes.signs.keywords.SyncWithKeyword;
import com.minetunes.signs.keywords.TransposeKeyword;

/**
 * Given the lines of text upon a sign, SignParser will resolve the sign into
 * keywords, series of MusicString tokens, and comments, by line.
 * 
 * Unlike code that parses the lines of a sign individually, SignParser accounts
 * for multi-line keywords easily.
 * 
 */
public class SignParser {

	/**
	 * Keywords that should not be shown in help and the like, but still
	 * technically work or are read without errors.
	 */
	public static final String[] deprecatedKeywords = { "loud", "tutorial",
			"proxpad", "disco", "newbot" };

	/**
	 * Array of all keywords recognized by MineTunes
	 */
	public static final String[] keywords = { "pattern", "left", "right", "up",
			"down", "disco", "in", "out", "end", "endline", "mute", "reset",
			"proximity", "midi", "loud", "repeat", "oneline", "lyric",
			"oneatatime", "isditty", "syncvoices", "syncwith", "sfx",
			"proxpad", "volume", "area", "goto", "savemidi", "playmidi",
			"emitter", "sfxinst2", "sfxinst", "sfxinstoff", "newbot",
			"staccato", "staccatooff", "tran", "tranoff", "octaves",
			"octavesoff", "prelyric", "accel", "patt", "[ditty]", "ditty",
			"maxplays", "playlast", "flare", "firework", "[signtune]" };

	/**
	 * Prevent people from instantiating this class of static methods
	 */
	private SignParser() {
	}

	/**
	 * Fills a ParsedSign according to the results of parsing the given sign's
	 * text.
	 * 
	 * @param signText
	 * @return
	 */
	public static ParsedSign parseSign(String[] signText) {
		ParsedSign parsedSign = new ParsedSign(signText);

		// Check each line of a sign
		for (int currLine = 0; currLine < signText.length; currLine++) {
			Object currLineContents = parsedSign.getLines()[currLine];

			if (currLineContents == null) {
				// Line contents undefined. Must parse.

				// First, check for a comment
				String currLineText = signText[currLine];
				if (Comment.isLineComment(currLineText)) {
					parsedSign.getLines()[currLine] = new Comment(currLineText);
					continue;
				}

				// It might be a keyword at this point
				if (recognizeKeyword(currLineText) != null) {
					// Parse kewyord
					parseKeywordInContext(parsedSign, currLine);
					continue;
				}

				// If there is nothing on the line, set it to null.
				// TOOD: Trim()?
				if (currLineText.length() <= 0) {
					parsedSign.getLines()[currLine] = null;
					continue;
				}

				// If all those fail, save line as music tokens
				parsedSign.getLines()[currLine] = currLineText;
			} else {
				// Line already parsed and filled before. Skip.
			}
		}

		return parsedSign;
	}

	// Classes that were in ParsedKeyword before 0.9.6

	/**
	 * 
	 * Parses keywords that return true on "isFullSign()".
	 * 
	 * Tries to detect a keyword on the raw line of a sign given. If a keyword
	 * (deprecated or not) is found, an instance or subclass of ParsedKeyword is
	 * returned corresponding to the found keyword. If no keyword is recognized,
	 * parse() returns null.
	 * 
	 * TODO: Features the hack of replacing "Proximity" with "Area 1 1"
	 * 
	 * @param rawLine
	 * @return
	 */
	public static void parseKeywordInContext(ParsedSign parsedSign,
			int keywordLine) {
		ParsedKeyword keyword = SignParser.parseKeyword(parsedSign
				.getSignText()[keywordLine]);

		parsedSign.getLines()[keywordLine] = keyword;

		// Check for first line only stuff
		if (keywordLine != 0 && keyword.isFirstLineOnly()) {
			// Not on first line when it should be
			keyword.setGoodKeyword(false);
			keyword.setErrorMessageType(ParsedKeyword.ERROR);
			keyword.setErrorMessage("The keyword " + keyword.getKeyword()
					+ " must be on the first line of a sign.");
			return;
		}

		// If a full sign keyword, parse further
		if (keyword.isMultiline()) {
			keyword.parseWithMultiline(parsedSign, keywordLine, keyword);
			return;
		} else {
			// keyword is one line; done parsing
			return;
		}
	}

	/**
	 * Tries to detect a keyword on the raw line of a sign given. If a keyword
	 * (deprecated or not) is found, an instance or subclass of ParsedKeyword is
	 * returned corresponding to the found keyword. If no keyword is recognized,
	 * parse() returns null.
	 * 
	 * TODO: Features the hack of replacing "Proximity" with "Area 1 1"
	 * 
	 * @param rawLine
	 * @return
	 */
	public static ParsedKeyword parseKeyword(String rawLine) {
		String keyword = SignParser.recognizeKeyword(rawLine);

		if (keyword == null) {
			return null;
		} else if (keyword.equals("sfx")) {
			return SFXKeyword.parse(rawLine);
		} else if (keyword.equals("left") || keyword.equals("right")
				|| keyword.equals("up") || keyword.equals("down")
				|| keyword.equals("in") || keyword.equals("out")) {
			// Goto keyword
			return ExplicitGotoKeyword.parse(rawLine);
		} else if (keyword.equals("lyric")) {
			return LyricKeyword.parse(rawLine);
		} else if (keyword.equals("pattern")) {
			return PatternKeyword.parse(rawLine);
		} else if (keyword.equals("repeat")) {
			return RepeatKeyword.parse(rawLine);
		} else if (keyword.equals("syncwith")) {
			return SyncWithKeyword.parse(rawLine);
		} else if (keyword.equals("proxpad") || keyword.equals("area")) {
			return ProxPadKeyword.parse(rawLine);
		} else if (keyword.equals("disco")) {
			return DiscoKeyword.parse(rawLine);
		} else if (keyword.equals("proximity")) {
			// Substitute a 1x1 proxpad for the proximity keyword
			ProxPadKeyword k = ProxPadKeyword.parse("area 1 1");
			return k;
		} else if (keyword.equals("goto")) {
			return GotoKeyword.parse(rawLine);
		} else if (keyword.equals("midi") || keyword.equals("savemidi")) {
			return SaveMidiKeyword.parse(rawLine);
		} else if (keyword.equals("playmidi")) {
			return PlayMidiKeyword.parse(rawLine);
		} else if (keyword.equals("emitter")) {
			return EmitterKeyword.parse(rawLine);
		} else if (keyword.equals("sfxinst") || keyword.equals("sfxinst2")) {
			return SFXInstKeyword.parse(rawLine);
		} else if (keyword.equals("sfxinstoff")) {
			return SFXInstOffKeyword.parse(rawLine);
		} else if (keyword.equals("newbot")) {
			return NewBotKeyword.parse(rawLine);
		} else if (keyword.equals("staccato")) {
			return StaccatoKeyword.parse(rawLine);
		} else if (keyword.equals("tran")) {
			return TransposeKeyword.parse(rawLine);
		} else if (keyword.equals("octaves")) {
			return OctavesKeyword.parse(rawLine);
		} else if (keyword.equals("octavesoff")) {
			return OctavesOffKeyword.parse(rawLine);
		} else if (keyword.equals("prelyric")) {
			return PreLyricKeyword.parse(rawLine);
		} else if (keyword.equals("accel")) {
			return AccelerateKeyword.parse(rawLine);
		} else if (keyword.equals("patt")) {
			return PattKeyword.parse(rawLine);
		} else if (keyword.equals("ditty") || keyword.equals("[ditty]")
				|| keyword.equals("[signtune]")) {
			return new NoteblockTriggerKeyword(rawLine);
		} else if (keyword.equals("maxplays")) {
			return MaxPlaysKeyword.parse(rawLine);
			// } else if (keyword.equals("flare")) {
			// return FlareKeyword.parse(rawLine);
		} else if (keyword.equals("firework")) {
			return FireworkKeyword.parse(rawLine);
		} else {
			// Unknown or simple (no arguments) keyword
			ParsedKeyword k = new ParsedKeyword(rawLine);
			k.setKeyword(keyword);
			return k;
		}
	}

	/**
	 * Tries to recognize a keyword on a line of a sign.
	 * 
	 * Returns for input:
	 * 
	 * endline -> endline NOT end; end line -> end; pattern 6 -> pattern
	 * 
	 * Pattern -> pattern; DOwN -> down
	 * 
	 * " up" -> "up"
	 * 
	 * null -> null
	 * 
	 * @param line
	 *            the line; any case is fine
	 * @return the keyword recognized, or null if none was
	 */
	public static String recognizeKeyword(String line) {
		if (line == null) {
			return null;
		}

		for (String keyword : SignParser.keywords) {
			if ((line.trim() + " ").toLowerCase().startsWith(keyword + " ")) {
				return keyword;
			}
		}
		return null;
	}

}
