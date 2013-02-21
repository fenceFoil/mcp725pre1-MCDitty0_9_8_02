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
 * Alert that the ditty has ended, by running out of music or simply muted.
 *
 */
public class DittyEndedEvent extends TimedDittyEvent {

	private boolean wasMuted = false;

	public DittyEndedEvent() {
		super();
	}

	/**
	 * 
	 * @param muted
	 *            if true, ditty was ended abnormally or by user. Otherwise it
	 *            ended naturally.
	 * @param id
	 */
	public DittyEndedEvent(boolean muted, int id) {
		super(id);
		this.wasMuted = muted;
	}

	/**
	 * @return the wasMuted
	 */
	public boolean isWasMuted() {
		return wasMuted;
	}

	/**
	 * @param wasMuted
	 *            the wasMuted to set
	 */
	public void setWasMuted(boolean wasMuted) {
		this.wasMuted = wasMuted;
	}

}
