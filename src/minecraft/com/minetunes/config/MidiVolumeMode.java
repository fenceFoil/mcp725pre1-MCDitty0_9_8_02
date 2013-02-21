/**
 * Copyright (c) 2012-2013 William Karnavas 
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
package com.minetunes.config;

/**
 * @author William
 * 
 */
public enum MidiVolumeMode {
	MAX, MC_SOUND, MC_MUSIC;

	public static int toInt(MidiVolumeMode m) {
		switch (m) {
		case MAX:
			return 100;
		case MC_SOUND:
			return 0;
		case MC_MUSIC:
			return 200;
		default:
			return 0;
		}
	}

	public static MidiVolumeMode fromInt(int i) {
		switch (i) {
		case 100:
			return MAX;
		case 0:
			return MC_SOUND;
		case 200:
			return MC_MUSIC;
		default:
			return MC_SOUND;
		}
	}
}
