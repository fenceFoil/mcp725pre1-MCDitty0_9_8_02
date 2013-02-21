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

import com.minetunes.Point3D;

public class HighlightSignPlayingEvent extends TimedDittyEvent {

	private boolean turnOn;
	private Point3D pos;
	private int highlightID;

	/**
	 * @return the highlightID
	 */
	public int getHighlightID() {
		return highlightID;
	}

	public int getNextHighlightID() {
		return nextHighlightID++;
	}

	/**
	 * @param highlightID
	 *            the highlightID to set
	 */
	public void setHighlightID(int highlightID) {
		this.highlightID = highlightID;
	}

	private static int nextHighlightID = 0;

	public HighlightSignPlayingEvent(Point3D signPoint,
			boolean turnOnHighlight, long time, int dittyID) {
		super(dittyID);
		timeToPlay = time;
		turnOn = turnOnHighlight;
		pos = signPoint;
	}

	/**
	 * @return the turnOn
	 */
	public boolean isTurnOn() {
		return turnOn;
	}

	/**
	 * @param turnOn
	 *            the turnOn to set
	 */
	public void setTurnOn(boolean turnOn) {
		this.turnOn = turnOn;
	}

	/**
	 * @return the pos
	 */
	public Point3D getPos() {
		return pos;
	}

	/**
	 * @param pos
	 *            the pos to set
	 */
	public void setPos(Point3D pos) {
		this.pos = pos;
	}

}
