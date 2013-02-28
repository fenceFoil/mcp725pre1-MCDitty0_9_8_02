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

/**
 * @author William
 * 
 */
public enum SettingType {
	BOOLEAN, BOOLEAN_YES_NO, BOOLEAN_ON_OFF, BOOLEAN_ENABLED_DISABLED, INTEGER_SHORT_TIME, COLOR;

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
					return "True";
				} else {
					return "False";
				}
			}
			break;
		case BOOLEAN_ENABLED_DISABLED:
			if (value instanceof Boolean) {
				if (b) {
					return "Enabled";
				} else {
					return "Disabled";
				}
			}
			break;
		case BOOLEAN_ON_OFF:
			if (value instanceof Boolean) {
				if (b) {
					return "On";
				} else {
					return "Off";
				}
			}
			break;
		case BOOLEAN_YES_NO:
			if (value instanceof Boolean) {
				if (b) {
					return "Yes";
				} else {
					return "No";
				}
			}
			break;
		case COLOR:
			break;
		case INTEGER_SHORT_TIME:
			if (value instanceof Integer) {
				return Integer.toString((Integer) value);
			}
			break;
		default:
			break;

		}
		return "";
	}
	
	public static int nextShortTimeIntValue (int value) {
		if (value < 10) {
			value += 2;
		} else if (value < 15) {
			value += 5;
		} else if (value < 30) {
			value += 15;
		} else {
			value = 2;
		}
		return value;
	}
}
