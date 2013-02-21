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
 * Represents a volume change at a point in time on a certain track in a ditty.
 * One of these events should be added to a ditty whenever the volume is
 * changed, so that non-MIDI parts of the ditty such as SFX keywords play at the
 * same volume as the ditty.
 * 
 */
public class VolumeEvent extends TimedDittyEvent {
	private float volume = 1.0f;

	public VolumeEvent(float volume, int dittyID) {
		super(dittyID);
		setVolume(volume);
	}

	private void setVolume(float volume) {
		this.volume = volume;
	}

	public float getVolume() {
		return volume;
	}
}
