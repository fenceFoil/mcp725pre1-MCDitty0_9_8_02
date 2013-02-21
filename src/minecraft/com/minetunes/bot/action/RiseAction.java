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
package com.minetunes.bot.action;

/**
 * Requests a bot to rise into the air and hover, or fall if the amount to rise
 * is negative. Important parameters include how long it takes to rise and how
 * far it will rise.
 * 
 */
public class RiseAction extends BotAction {

	/**
	 * JFugue duration (how much of a whole note) to rise for. Default value is
	 * a quarter note.
	 */
	private double duration = 0.25;

	/**
	 * Amount to rise, in blocks. Default value is a slight hover, a quarter
	 * block.
	 */
	private double distance = 0.25;

	/**
	 * Creates a new RiseAction with given parameters.
	 * @param address
	 * @param distance can be null for the default value
	 * @param duration can be null for the default value
	 */
	public RiseAction(String address, int dittyID, Double distance, Double duration) {
		this(address, dittyID);
		
		if (distance != null) {
			setDistance(distance);
		}

		if (duration != null) {
			setDuration(duration);
		}
	}

	public RiseAction(String address, int dittyID) {
		super(address, dittyID);
	}

	/**
	 * @return the duration
	 */
	public double getDuration() {
		return duration;
	}

	/**
	 * @param duration
	 *            the duration to set
	 */
	public void setDuration(double duration) {
		this.duration = duration;
	}

	/**
	 * @return the distance
	 */
	public double getDistance() {
		return distance;
	}

	/**
	 * @param distance
	 *            the distance to set
	 */
	public void setDistance(double distance) {
		this.distance = distance;
	}

}
