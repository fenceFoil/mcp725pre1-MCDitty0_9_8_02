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
package com.minetunes.ditty.event;

import java.util.LinkedList;

import com.minetunes.bot.action.BotAction;


/**
 * Represents lyric text and bot instructions to be shown/executed during a ditty.
 */
public class CueEvent extends TimedDittyEvent {

	// Lyric text, if any
	private String lyricText = "";

	// BotActions to be executed
	private LinkedList<BotAction> botActions = new LinkedList<BotAction>();

	public CueEvent() {
		super();
	}

	public CueEvent(String text) {
		super();
		this.lyricText = text;
	}

	public void addBotAction(BotAction action) {
		botActions.add(action);
	}

	/**
	 * @return the text
	 */
	public String getLyricText() {
		return lyricText;
	}

	/**
	 * @param text
	 *            the text to set
	 */
	public void setLyricText(String text) {
		this.lyricText = text;
	}

	public LinkedList<BotAction> getBotActions() {
		return botActions;
	}

	public void setBotActions(LinkedList<BotAction> botActions) {
		this.botActions = botActions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected CueEvent clone() {
		CueEvent l = new CueEvent();
		l.lyricText = lyricText;
		l.timeToPlay = timeToPlay;
		l.timesPlayed = timesPlayed;
		l.botActions.addAll(botActions);
		return l;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getName() + ":" + lyricText + ":time:" + timeToPlay
				+ ":timesPlayed:" + timesPlayed;
	}
}
