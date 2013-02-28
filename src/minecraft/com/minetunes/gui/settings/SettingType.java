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
package com.minetunes.gui.settings;

import com.minetunes.config.MidiVolumeMode;

/**
 * @author William
 * 
 */
public enum SettingType {
	BOOLEAN, BOOLEAN_YES_NO, BOOLEAN_ON_OFF, BOOLEAN_ENABLED_DISABLED, INTEGER_SHORT_TIME, COLOR, MIDI_VOLUME, NO_PLAY_TOKENS;

	public static String getButtonLabel(SettingType t, Object value,
			boolean inverted) {
		boolean b = false;
		if (value instanceof Boolean) {
			if (inverted) {
				b = !(Boolean) value;
			} else {
				b = (Boolean) value;
			}
		}

		switch (t) {
		case BOOLEAN:
			if (value instanceof Boolean) {
				if (b) {
					return "§aTrue";
				} else {
					return "§cFalse";
				}
			}
			break;
		case BOOLEAN_ENABLED_DISABLED:
			if (value instanceof Boolean) {
				if (b) {
					return "§aEnabled";
				} else {
					return "§cDisabled";
				}
			}
			break;
		case BOOLEAN_ON_OFF:
			if (value instanceof Boolean) {
				if (b) {
					return "§aOn";
				} else {
					return "§cOff";
				}
			}
			break;
		case BOOLEAN_YES_NO:
			if (value instanceof Boolean) {
				if (b) {
					return "§aYes";
				} else {
					return "§cNo";
				}
			}
			break;
		case COLOR:
			break;
		case INTEGER_SHORT_TIME:
			if (value instanceof Integer) {
				return Integer.toString(((Integer) value) / 1000);
			}
			break;
		case MIDI_VOLUME:
			if (value instanceof MidiVolumeMode) {
				switch ((MidiVolumeMode)value) {
				case MAX:
					return "100%";
				case MC_MUSIC:
					return "Minecraft Music";
				case MC_SOUND:
					return "Minecraft Sound";
				default:
					return "??";
				}
			}
			break;
		case NO_PLAY_TOKENS:
			return "§9§nEdit";
		default:
			break;

		}
		return "";
	}
	
	public static int nextShortTimeIntValue (int value) {
		if (value < 10000) {
			value += 2000;
		} else if (value < 15000) {
			value += 5000;
		} else if (value < 30000) {
			value += 15000;
		} else {
			value = 2000;
		}
		return value;
	}
}
