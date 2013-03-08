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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import com.minetunes.signs.keywords.AccelerateKeyword;
import com.minetunes.signs.keywords.BookKeyword;
import com.minetunes.signs.keywords.DiscoKeyword;
import com.minetunes.signs.keywords.EmitterKeyword;
import com.minetunes.signs.keywords.EndKeyword;
import com.minetunes.signs.keywords.EndLineKeyword;
import com.minetunes.signs.keywords.ExplicitGotoKeyword;
import com.minetunes.signs.keywords.FireworkKeyword;
import com.minetunes.signs.keywords.GotoKeyword;
import com.minetunes.signs.keywords.IsDittyKeyword;
import com.minetunes.signs.keywords.LoudKeyword;
import com.minetunes.signs.keywords.LyricKeyword;
import com.minetunes.signs.keywords.MaxPlaysKeyword;
import com.minetunes.signs.keywords.MuteKeyword;
import com.minetunes.signs.keywords.NewBotKeyword;
import com.minetunes.signs.keywords.NoteblockTriggerKeyword;
import com.minetunes.signs.keywords.OctavesKeyword;
import com.minetunes.signs.keywords.OctavesOffKeyword;
import com.minetunes.signs.keywords.OneAtATimeKeyword;
import com.minetunes.signs.keywords.OneLineKeyword;
import com.minetunes.signs.keywords.PattKeyword;
import com.minetunes.signs.keywords.PatternKeyword;
import com.minetunes.signs.keywords.PlayLastKeyword;
import com.minetunes.signs.keywords.PlayMidiKeyword;
import com.minetunes.signs.keywords.PreLyricKeyword;
import com.minetunes.signs.keywords.ProxPadKeyword;
import com.minetunes.signs.keywords.ResetKeyword;
import com.minetunes.signs.keywords.SFXInstKeyword;
import com.minetunes.signs.keywords.SFXInstOffKeyword;
import com.minetunes.signs.keywords.SFXKeyword;
import com.minetunes.signs.keywords.SaveMidiKeyword;
import com.minetunes.signs.keywords.SignTuneKeyword;
import com.minetunes.signs.keywords.StaccatoKeyword;
import com.minetunes.signs.keywords.StaccatoOffKeyword;
import com.minetunes.signs.keywords.SyncVoicesKeyword;
import com.minetunes.signs.keywords.SyncWithKeyword;
import com.minetunes.signs.keywords.TransposeKeyword;
import com.minetunes.signs.keywords.TransposeOffKeyword;
import com.minetunes.signs.keywords.VolumeKeyword;

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
			"proxpad", "disco", "newbot", "pattern" };

	/**
	 * Array of all keywords recognized by MineTunes
	 */
	public static String[] keywords = null;

	// TODO: Use annotations instead on each keyword type?
	public static final HashMap<String, Class<? extends SignTuneKeyword>> keywordClasses = new HashMap<String, Class<? extends SignTuneKeyword>>();
	static {
		keywordClasses.put("pattern", PatternKeyword.class);
		keywordClasses.put("left", ExplicitGotoKeyword.class);
		keywordClasses.put("right", ExplicitGotoKeyword.class);
		keywordClasses.put("up", ExplicitGotoKeyword.class);
		keywordClasses.put("down", ExplicitGotoKeyword.class);
		keywordClasses.put("in", ExplicitGotoKeyword.class);
		keywordClasses.put("out", ExplicitGotoKeyword.class);
		keywordClasses.put("disco", DiscoKeyword.class);
		keywordClasses.put("end", EndKeyword.class);
		keywordClasses.put("endline", EndLineKeyword.class);
		keywordClasses.put("mute", MuteKeyword.class);
		keywordClasses.put("reset", ResetKeyword.class);
		keywordClasses.put("proximity", ProxPadKeyword.class);
		keywordClasses.put("midi", SaveMidiKeyword.class);
		keywordClasses.put("loud", LoudKeyword.class);
		keywordClasses.put("repeat", PatternKeyword.class);
		keywordClasses.put("oneline", OneLineKeyword.class);
		keywordClasses.put("lyric", LyricKeyword.class);
		keywordClasses.put("oneatatime", OneAtATimeKeyword.class);
		keywordClasses.put("isditty", IsDittyKeyword.class);
		keywordClasses.put("syncvoices", SyncVoicesKeyword.class);
		keywordClasses.put("syncwith", SyncWithKeyword.class);
		keywordClasses.put("sfx", SFXKeyword.class);
		keywordClasses.put("proxpad", ProxPadKeyword.class);
		keywordClasses.put("volume", VolumeKeyword.class);
		keywordClasses.put("area", ProxPadKeyword.class);
		keywordClasses.put("goto", GotoKeyword.class);
		keywordClasses.put("savemidi", SaveMidiKeyword.class);
		keywordClasses.put("playmidi", PlayMidiKeyword.class);
		keywordClasses.put("emitter", EmitterKeyword.class);
		keywordClasses.put("sfxinst", SFXInstKeyword.class);
		keywordClasses.put("sfxinst2", SFXInstKeyword.class);
		keywordClasses.put("sfxinstoff", SFXInstOffKeyword.class);
		keywordClasses.put("newbot", NewBotKeyword.class);
		keywordClasses.put("staccato", StaccatoKeyword.class);
		keywordClasses.put("staccatooff", StaccatoOffKeyword.class);
		keywordClasses.put("tran", TransposeKeyword.class);
		keywordClasses.put("tranoff", TransposeOffKeyword.class);
		keywordClasses.put("octaves", OctavesKeyword.class);
		keywordClasses.put("octavesoff", OctavesOffKeyword.class);
		keywordClasses.put("prelyric", PreLyricKeyword.class);
		keywordClasses.put("accel", AccelerateKeyword.class);
		keywordClasses.put("patt", PattKeyword.class);
		keywordClasses.put("ditty", NoteblockTriggerKeyword.class);
		keywordClasses.put("[ditty]", NoteblockTriggerKeyword.class);
		keywordClasses.put("[signtune]", NoteblockTriggerKeyword.class);
		keywordClasses.put("maxplays", MaxPlaysKeyword.class);
		keywordClasses.put("playlast", PlayLastKeyword.class);
		// keywordClasses.put("flare", FlareKeyword.class);
		keywordClasses.put("firework", FireworkKeyword.class);
		keywordClasses.put("book", BookKeyword.class);

		// Create the array of keywords
		keywords = keywordClasses.keySet().toArray(new String[0]);
	}

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
		SignTuneKeyword keyword = SignParser.parseKeyword(parsedSign
				.getSignText()[keywordLine]);
		if (keyword == null) {
			return;
		}

		parsedSign.getLines()[keywordLine] = keyword;

		// Check for first line only stuff
		if (keywordLine != 0 && keyword.isFirstLineOnly()) {
			// Not on first line when it should be
			keyword.setGoodKeyword(false);
			keyword.setErrorMessageType(SignTuneKeyword.ERROR);
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
	 * @return null if no keyword found
	 */
	public static SignTuneKeyword parseKeyword(String rawLine) {
		String keyword = SignParser.recognizeKeyword(rawLine);

		if (keyword == null) {
			return null;
		}

		Class<? extends SignTuneKeyword> keywordClass = keywordClasses
				.get(keyword);
		if (keywordClass != null) {
			// Parse and return
			SignTuneKeyword keywordInstance = null;
			try {
				keywordInstance = keywordClass.getConstructor(String.class)
						.newInstance(rawLine);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			keywordInstance.parse();
			return keywordInstance;
		}

		// Unknown keyword
		return null;
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
