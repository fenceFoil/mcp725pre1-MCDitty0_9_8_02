/**
 * Copyright (c) 2012 William Karnavas 
 * All Rights Reserved
 */

/**
 * 
 * This file is part of MineTunes.
 * 
 * MineTunes is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * MineTunes is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MineTunes. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.minetunes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import com.minetunes.config.MinetunesConfig;
import com.minetunes.ditty.event.CueEvent;
import com.minetunes.ditty.event.TimedDittyEvent;
import com.minetunes.signs.SignTuneParser;

/**
 * CueScheduler accepts lyric texts and BotActions, indexes and combines them
 * based on their name and repetition number, manages setting the times that
 * each repetition occurs at, and produces a series of TimedEvents with lyrics
 * and bot actions.<br>
 * <br>
 * To use, first use AddLyricText to set the texts for each label and repetition
 * as necessary.<br>
 * <br>
 * Then, use finalizeLyrics to get the data ready for retrieval and time setting<br>
 * <br>
 * Finally, use setLyricTime to set the times that each label and repetition
 * will appear.<br>
 * <br>
 * It is important to do these things in this exact order, on pain of unexpected
 * behavior. THERE IS NO ACTIVE CHECKING TO ENFORCE THIS.<br>
 * <br>
 * Glossary: REPETITION: A number starting at 1. REPEITIONINDEX: A number
 * starting at 0, formed by subtracting one from a repetition.<br>
 * <br>
 * TODO: Decide to remove repetition from MineTunesLyric?
 * 
 * TODO: Should case sensitivity be dealt with in here? TODO: Case insensitive
 */
public class CueScheduler {

	// Lyrics stored by this object, sorted by label.
	// Each array SOULD be sorted so that entry 0 contains repetition 1, etc.
	private HashMap<String, ArrayList<CueEvent>> labelMap = new HashMap<String, ArrayList<CueEvent>>();

	// Lyric prefix texts, sorted by label (label, prefixText)
	private HashMap<String, String> lyricPrefixes = new HashMap<String, String>();

	/**
	 * Adds a new lyric text to the lists, under the given label. If this is
	 * called on a lyric that is already stored, the given text is appended to
	 * the previous lyric
	 * 
	 * @param lyricLabel
	 * @param lyricText
	 */
	public void addLyricText(String lyricLabel, String lyricText, int repetition) {
		if (MinetunesConfig.DEBUG) {
			SignTuneParser.simpleLog("addLyricText called: " + lyricLabel + " with "
					+ lyricText + " rep=" + repetition);
		}

		// Is lyric already in lists?
		boolean alreadyInLists = false;
		ArrayList<CueEvent> lyricsForLabel = labelMap.get(lyricLabel
				.toLowerCase());
		// If label exists, and the repetition is defined already, it is in the
		// lists
		if (lyricsForLabel != null && lyricsForLabel.size() >= repetition
				&& lyricsForLabel.get(repetition - 1) != null) {
			alreadyInLists = true;
		}

		if (MinetunesConfig.DEBUG) {
			SignTuneParser.simpleLog("addLyricText: in list already? "
					+ alreadyInLists);
		}

		if (alreadyInLists) {
			// Add to existing lyric
			CueEvent existingLyric = getLyric(lyricLabel.toLowerCase(),
					repetition);
			String existingLyricText = existingLyric.getLyricText();
			// TODO: Should there be a space?
			existingLyric.setLyricText(existingLyricText + lyricText);
		} else {
			// Create a new lyric

			// Handle the case where a label must be created
			if (labelMap.get(lyricLabel.toLowerCase()) == null) {
				labelMap.put(lyricLabel.toLowerCase(),
						new ArrayList<CueEvent>());
			}

			// Then add lyric
			if (repetition > labelMap.get(lyricLabel.toLowerCase()).size()) {
				// Lengthen array as necessary
				while (labelMap.get(lyricLabel.toLowerCase()).size() < repetition) {
					labelMap.get(lyricLabel.toLowerCase()).add(null);
				}
			}

			labelMap.get(lyricLabel.toLowerCase()).set(repetition - 1,
					new CueEvent(lyricText));

			printAllLyrics("After lyric added");
		}
	}

	public void addLyricPreText(String label, String prefixText) {
		label = label.toLowerCase();

		String existingPrefixText = lyricPrefixes.get(label);
		if (existingPrefixText == null) {
			existingPrefixText = "";
		}

		existingPrefixText += prefixText;
		lyricPrefixes.put(label, existingPrefixText);
	}

	/**
	 * Performs range checking.
	 * 
	 * @param lyricLabel
	 * @param repetition
	 * @return the requested lyric if it exists, otherwise null. Null also if
	 *         repetition is out of range.
	 */
	public CueEvent getLyric(String lyricLabel, int repetition) {
		if (MinetunesConfig.DEBUG) {
			SignTuneParser.simpleLog("getLyric called: " + lyricLabel + " rep="
					+ repetition);
		}

		if (repetition < 0) {
			return null;
		}

		ArrayList<CueEvent> labelLyrics = labelMap
				.get(lyricLabel.toLowerCase());
		if (labelLyrics.size() < repetition) {
			return null;
		} else {
			return labelLyrics.get(repetition - 1);
		}
	}

	/**
	 * Processes all stored lyrics of each label by the following rules:
	 * 
	 * 1. Any "holes" (e.g. where a 1, (no 2), (no 3), and a 4 are listed) are
	 * filled by duplicating the preceding lyric or (if there are no previous
	 * lyrics) by an empty lyric.
	 * 
	 * 2. All prefixes are added to lyric texts
	 */
	public void finalizeLyrics() {
		// Apply rule 1: Fill holes
		Set<String> allDefinedLabels = labelMap.keySet();
		for (String label : allDefinedLabels) {
			ArrayList<CueEvent> labelLyrics = labelMap.get(label);
			for (int i = 0; i < labelLyrics.size(); i++) {
				if (labelLyrics.get(i) == null) {
					// There is a hole at (i). Fill it by copying the preceding
					// lyric or a new empty lyric if no preceding lyric exists

					// Get the preceeding lyric
					CueEvent precedingLyric;
					if (i - 1 >= 0) {
						precedingLyric = labelLyrics.get(i - 1);
					} else {
						// The first lyric is guaranteed to have no preceeding
						// lyric
						precedingLyric = null;
					}

					// Fill the hole
					if (precedingLyric == null) {
						// There is no preceeding lyric. Fill hole with empty
						// lyric
						CueEvent emptyLyric = new CueEvent("");
						labelLyrics.set(i, emptyLyric);
					} else {
						// There is a preceeding lyric! Fill hole with a copy.
						CueEvent copyOfPreceedingLyric = new CueEvent(
								precedingLyric.getLyricText());
						labelLyrics.set(i, copyOfPreceedingLyric);
					}
				}
			}
		}

		// 2: Apply lyric prefixes
		for (String label : allDefinedLabels) {
			// Get prefix text for this label
			String prefixText = lyricPrefixes.get(label.toLowerCase());
			if (prefixText == null || prefixText.length() <= 0) {
				// no prefix. move on.
				continue;
			}

			ArrayList<CueEvent> labelLyrics = labelMap.get(label);
			for (int i = 0; i < labelLyrics.size(); i++) {
				// Add prefix to each lyric's text
				String lyricText = labelLyrics.get(i).getLyricText();
				lyricText = prefixText + lyricText;
				labelLyrics.get(i).setLyricText(lyricText);
			}
		}

		printAllLyrics("After finalization");
	}

	/**
	 * Sets the time for a certain lyric to appear in a song, identified by its
	 * marker.
	 * 
	 * The first time it is called, it sets the time for the "1" repetition, the
	 * second time, the "2" rep, and so on.
	 * 
	 * Note that failing to call finalizeLyrics() before this method may result
	 * in unexpected behavior.
	 * 
	 * Bug: we duplicate all repetitions when we really want to duplicate the
	 * pattern of unique lyrics once. This weird algorithm can result in
	 * excessive memory being used by very very possibly creating many excess
	 * lyrics that will never be needed, after the first duplication.
	 * 
	 * Silently fails if no lyrics are found with the given label.
	 */
	public void setLyricTime(String lyricLabel, long time) {
		if (MinetunesConfig.DEBUG) {
			SignTuneParser.simpleLog("setLyricTime called: " + lyricLabel + ":"
					+ time);
		}

		// Get all repetitions for the given label
		ArrayList<CueEvent> repsArray = labelMap.get(lyricLabel.toLowerCase());

		if (repsArray == null) {
			// If no lyrics are found matching the label, well, don't do
			// anything due to lack of target
			return;
		}

		// Otherwise look for and define the first undefined lyric in the list
		// If the list is too short, duplicate everything in the list and define
		// the first repetition in the duplicates
		for (int i = 0; i < repsArray.size(); i++) {
			CueEvent lyric = repsArray.get(i);
			if (lyric.getTime() < 0) {
				// Found first unassigned repetition
				lyric.setTime(time);
				printAllLyrics("After setting a lyric time");
				return;
			} else {
				// Did not yet find; move on.
			}
		}

		// If this point is hit in the code, we need to copy the pattern of
		// unique lyrics for this label and append it to the list of lyrics
		int duplicateRange = repsArray.size();
		for (int i = 0; i < duplicateRange; i++) {
			repsArray.add(new CueEvent(repsArray.get(i).getLyricText()));
		}

		// Then set the time for the first of the new duplicates
		repsArray.get(duplicateRange).setTime(time);
		printAllLyrics("After setting a lyric time (w/ duplication)");
	}

	/**
	 * This works at any time at all; before and after finalization. It returns
	 * a collection of all the lyrics currently stored.
	 * 
	 * @return
	 */
	public Collection<? extends TimedDittyEvent> getAllLyrics() {
		LinkedList<CueEvent> allLyrics = new LinkedList<CueEvent>();
		Set<String> labelKeys = labelMap.keySet();
		for (String label : labelKeys) {
			for (CueEvent l : labelMap.get(label)) {
				allLyrics.add(l);
			}
		}
		return allLyrics;
	}

	private void printAllLyrics(String title) {
		if (MinetunesConfig.DEBUG) {
			System.out.println("LYRICSTORAGE: " + title);
			Set<String> labelKeys = labelMap.keySet();
			for (String label : labelKeys) {
				System.out.println(label + ":");
				for (CueEvent l : labelMap.get(label)) {
					System.out.println(l);
				}
			}
		}
	}
}
