/**
 * 
 * Copyright (c) 2012 William Karnavas All Rights Reserved
 * 
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

/**
 *
 *
 */
public class SignPlayingTimedEvent extends TimedDittyEvent {
	private boolean startPlaying = false;

	private int signID = 0;

	public SignPlayingTimedEvent(boolean startPlaying, int signID, int dittyID) {
		super(dittyID);
		setStartPlaying(startPlaying);
		this.signID = signID;
	}

	/**
	 * If true, sign has begun playing. if false, it has ended.
	 * 
	 * @return
	 */
	public boolean getStartPlaying() {
		return startPlaying;
	}

	public void setStartPlaying(boolean startPlaying) {
		this.startPlaying = startPlaying;
	}

	public int getSignID() {
		return signID;
	}

	public void setSignID(int signID) {
		this.signID = signID;
	}

	@Override
	public String toString() {
		return ("SignPlayingTimedEvent:startPlaying="+startPlaying+";signID="+signID+";"+super.toString());
	}

}
