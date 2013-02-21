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
package com.minetunes.ditty.event;

/**
 * A request that some action take place or some info be noted at a certain time
 * as a ditty plays.
 * 
 */
public abstract class TimedDittyEvent implements Comparable<TimedDittyEvent> {

	// Time in the song to fire the event in PPQ
	// -1 indicates that no time has been given, i.e. it will not be fire
	protected long timeToPlay = -1;

	// Times this event has been fired
	protected int timesPlayed = 0;

	protected int dittyID = -1;

	protected int eventID = 0;
	protected static int nextEventID = 0;

	protected int voice = 0;

	public TimedDittyEvent() {
		eventID = nextEventID++;
	}

	public TimedDittyEvent(int dittyID) {
		setDittyID(dittyID);
		eventID = nextEventID++;
	}

	/**
	 * @return the timeToPlay
	 */
	public long getTime() {
		return timeToPlay;
	}

	/**
	 * @param timeToPlay
	 *            the timeToPlay to set
	 */
	public void setTime(long timeToPlay) {
		this.timeToPlay = timeToPlay;
	}

	/**
	 * @return the timesPlayed
	 */
	public int getTimesPlayed() {
		return timesPlayed;
	}

	/**
	 * @param timesPlayed
	 *            the timesPlayed to set
	 */
	public void setTimesPlayed(int timesPlayed) {
		this.timesPlayed = timesPlayed;
	}

	/**
	 * @return the dittyID
	 */
	public int getDittyID() {
		return dittyID;
	}

	/**
	 * @param dittyID
	 *            the dittyID to set
	 */
	public void setDittyID(int dittyID) {
		this.dittyID = dittyID;
	}

	/**
	 * Compares events by time.
	 */
	@Override
	public int compareTo(TimedDittyEvent o) {
		if (getTime() > o.getTime()) {
			return 1;
		} else if (getTime() < o.getTime()) {
			return -1;
		} else {
			return 0;
		}
	}

	public int getEventID() {
		return eventID;
	}

	public void setEventID(int eventID) {
		this.eventID = eventID;
	}

	@Override
	public String toString() {
		return "TimedDittyEvent:timeToPlay=" + timeToPlay + ";timesPlayed="
				+ timesPlayed + ";dittyID="
				+ dittyID + ";eventID=" + eventID + ";nextEventID="
				+ nextEventID + ";voice=" + voice + ";";
	}

	public int getVoice() {
		return voice;
	}

	public void setVoice(int voice) {
		this.voice = voice;
	}

	/**
	 * 
	 * @return true if this TimedDittyEvent must be handled in the
	 *         DittyPlayerThread. Most events are executed in MineTunes.java,
	 *         which keeps a queue and interacts with the world, but some need
	 *         access to the synthesizer to change instruments, etc. These set
	 *         this method to return true.
	 */
	public boolean isExecutedAtPlayerLevel() {
		return false;
	}
}
