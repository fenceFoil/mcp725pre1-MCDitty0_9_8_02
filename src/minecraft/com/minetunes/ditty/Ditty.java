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
package com.minetunes.ditty;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.jfugue.JFugueException;

import com.minetunes.CueScheduler;
import com.minetunes.config.MinetunesConfig;
import com.minetunes.config.NoPlayTokens;
import com.minetunes.disco.DiscoFloor;
import com.minetunes.ditty.event.TimedDittyEvent;
import com.minetunes.signs.SignTuneParser;
import com.minetunes.signs.SignDitty;

/**
 * Holds all the data necessary to play a ditty. A ditty is composed of a number
 * of properties and a musicstring.
 */
public class Ditty {
	private boolean muting = false;
	private boolean loud = false;
	private boolean midiAlreadySaved = false;
	private File midiSaveFile = null;
	
	private boolean playLast = false;

	private CueScheduler lyricsStorage = new CueScheduler();

	private int dittyID = -1;
	private static int nextDittyID = 0;

	/**
	 * All dittyevents to execute during the ditty.
	 */
	private LinkedList<TimedDittyEvent> dittyEvents = new LinkedList<TimedDittyEvent>();
	private boolean dittyEventsSorted = false;
	private LinkedList<DiscoFloor> discoFloors = new LinkedList<DiscoFloor>();

	private LinkedList<String> errorMessages = new LinkedList<String>();

	private HashSet<Byte> instrumentUsed = new HashSet<Byte>();

	/**
	 * Tallies of valid and invalid tokens used to determine if signs contain a
	 * ditty or random text.
	 */
	private int badTokens = 0;
	private int totalTokens = 0;

	private String musicString = "";

	public Ditty() {
	}

	/**
	 * Adds a new ditty event which can be recalled later when playing the ditty
	 * with getNextDittyEvent
	 * 
	 * @param e
	 * @return
	 */
	public int addDittyEvent(TimedDittyEvent e) {
		e.setDittyID(getDittyID());
		dittyEvents.add(e);
		dittyEventsSorted = false;
		return e.getEventID();
	}

	public void addAllDittyEvents(List<TimedDittyEvent> l) {
		for (TimedDittyEvent e : l) {
			e.setDittyID(getDittyID());
		}
		dittyEvents.addAll(l);
		dittyEventsSorted = false;
	}

	/**
	 * Called in DittyPlayerThread (or equivalent) right before a ditty is to be
	 * played. At this point, all lyrics have been defined with text and their
	 * cues to display have all been added.
	 */
	public void dumpLyricsStorageToDittyEvents() {
		for (TimedDittyEvent e : lyricsStorage.getAllLyrics()) {
			e.setDittyID(getDittyID());
		}
		dittyEvents.addAll(lyricsStorage.getAllLyrics());
	}

	/**
	 * Checks for the next event to be played in a ditty up to the given time.
	 * If one is found it is removed from the list.
	 * 
	 * @param time
	 * @return An event if one still needs to be played, or null if none.
	 */
	public TimedDittyEvent getNextDittyEventAtTime(long time) {
		if (!dittyEventsSorted) {
			sortDittyEvents();
			dittyEventsSorted = true;
		}
		if (dittyEvents.size() <= 0) {
			return null;
		}

		// Keep trying until we get an event that will play at some time in the
		// ditty
		TimedDittyEvent candidateEvent = dittyEvents.getFirst();
		while (candidateEvent.getTime() < 0) {
			dittyEvents.removeFirst();
			if (dittyEvents.size() <= 0) {
				return null;
			} else {
				// Try the next event
				candidateEvent = dittyEvents.getFirst();
			}
		}

		if (candidateEvent.getTime() <= time) {
			// Return this candidate, and remove it from list for future calls.
			dittyEvents.removeFirst();
			return candidateEvent;
		} else {
			// Too late. Do not return an event.
			return null;
		}
	}

	private void sortDittyEvents() {
		Collections.sort(dittyEvents);
	}

	public void setMuting(boolean b) {
		muting = b;
	}

	public boolean getMuting() {
		return muting;
	}

	public void setLoud(boolean b) {
		loud = b;
	}

	public boolean getMidiAlreadySaved() {
		return midiAlreadySaved;
	}

	public void setMidiAlreadySaved(boolean b) {
		midiAlreadySaved = b;
	}

	public void setMidiSaveFile(File midiSaveFile) {
		this.midiSaveFile = midiSaveFile;
	}

	public File getMidiSaveFile() {
		return midiSaveFile;
	}

	public CueScheduler getLyricsStorage() {
		return lyricsStorage;
	}

	/**
	 * @return the dittyID (generated on first call to getDittyID)
	 */
	public int getDittyID() {
		if (dittyID < 0) {
			dittyID = nextDittyID++;
		}
		return dittyID;
	}

	/**
	 * Adds disco floor to list for this ditty, and sets the disco floor's ditty
	 * id to this ditty.
	 * 
	 * @param newFloor
	 */
	public void addDiscoFloor(DiscoFloor newFloor) {
		discoFloors.add(newFloor);

		// Just in case, set the disco floor's ditty id to this ditty
		newFloor.setDittyID(getDittyID());
	}

	public LinkedList<DiscoFloor> getDiscoFloors() {
		return discoFloors;
	}

	public String getMusicString() {
		return musicString;
	}

	public void setMusicString(String musicString) {
		this.musicString = musicString;
	}

	public TimedDittyEvent getDittyEvent(int eventID) {
		TimedDittyEvent matchingEvent = null;
		for (TimedDittyEvent e : dittyEvents) {
			if (e.getEventID() == eventID) {
				matchingEvent = e;
				break;
			}
		}
		return matchingEvent;
	}

	public void addErrorMessage(String string) {
		errorMessages.add("* " + string);
	}

	public LinkedList<String> getErrorMessages() {
		return errorMessages;
	}

	public void incrementBadTokens() {
		badTokens++;
	}

	public void incrementTotalTokens() {
		totalTokens++;
	}

	/**
	 * @return the totalTokens
	 */
	public int getTotalTokens() {
		return totalTokens;
	}

	/**
	 * @param totalTokens
	 *            the totalTokens to set
	 */
	public void setTotalTokens(int totalTokens) {
		this.totalTokens = totalTokens;
	}

	/**
	 * @return the badTokens
	 */
	public int getBadTokens() {
		return badTokens;
	}

	/**
	 * @param badTokens
	 *            the badTokens to set
	 */
	public void setBadTokens(int badTokens) {
		this.badTokens = badTokens;
	}

	public HashSet<Byte> getInstrumentUsed() {
		return instrumentUsed;
	}

	/**
	 * Adds musicString tokens to a musicString token buffer, checking them for
	 * errors as they are added. Any error messages found are registered with
	 * dittyProperties; error highlights are left to the calling method to add.
	 * 
	 * Also checks for noPlayTokens
	 * 
	 * @param buffer
	 * @param tokens
	 * @param checkForErrors
	 * @return true if added without errors; false if added wtih errors.
	 */
	public boolean addMusicStringTokens(StringBuilder buffer,
			String musicString, boolean checkForErrors) {
		boolean errorFree = true;
		String[] tokens = musicString.split(" ");
		for (String token : tokens) {
			// Only bother adding non-blank tokens
			if (token.trim().length() > 0) {
				// Check against NoPlayTokens
				if (this instanceof SignDitty) {
					for (String noPlayToken : NoPlayTokens.getNoPlayTokens()) {
						// Check for equality, stripping color codes
						if (noPlayToken.equalsIgnoreCase(token.replaceAll("§.",
								""))) {
							// Is a no play token!
							((SignDitty) this).setContainsNoPlayTokens(true);
							break;
						}
					}
				}

				// Check tokens for errors
				if (checkForErrors) {
					try {
						SignTuneParser.musicStringParser.parseTokenStrict(token);
					} catch (JFugueException e) {
						// Token is not a valid token
						addErrorMessage("§b" + token + "§c: " + e.getMessage());
						incrementBadTokens();
						errorFree = false;
						if (MinetunesConfig.DEBUG) {
							SignTuneParser
									.simpleLog("addMusicStringTokens: Bad token found ("
											+ token + "):");
							e.printStackTrace();
						}
					} catch (Exception e) {
						// Token is a really screwed up token!
						addErrorMessage("§cMineTunes cannot figure out this token: §b"
								+ token);
						incrementBadTokens();
						errorFree = false;
						if (MinetunesConfig.DEBUG) {
							SignTuneParser
									.simpleLog("addMusicStringTokens: Really bad token found ("
											+ token + "):");
							e.printStackTrace();
						}
					}
				}

				incrementTotalTokens();
				buffer.append(" ").append(token);
			}
		}
		return errorFree;
	}

	public boolean isPlayLast() {
		return playLast;
	}

	public void setPlayLast(boolean playLast) {
		this.playLast = playLast;
	}

	public LinkedList<TimedDittyEvent> getDittyEvents() {
		return dittyEvents;
	}

}
