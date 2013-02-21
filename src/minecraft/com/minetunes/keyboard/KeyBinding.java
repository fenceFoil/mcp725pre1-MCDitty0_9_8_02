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
package com.minetunes.keyboard;

import org.lwjgl.input.Keyboard;

public class KeyBinding {
	public static final int[] CTRL_KEYS = { Keyboard.KEY_LCONTROL,
			Keyboard.KEY_RCONTROL };
	public static final int[] SHIFT_KEYS = { Keyboard.KEY_LSHIFT,
			Keyboard.KEY_RSHIFT };

	private int mainKey;
	private int[] modifierKeys = null;
	private String action;
	private String displayName;

	public KeyBinding(String displayName, String action, int mainKey,
			int[] modifierKeys) {
		setDisplayName(displayName);
		setAction(action);
		setMainKey(mainKey);
		setModifierKeys(modifierKeys);
	}

	private boolean lastState = false;

	public int getMainKey() {
		return mainKey;
	}

	public void setMainKey(int mainKey) {
		this.mainKey = mainKey;
	}

	public int[] getModifierKeys() {
		return modifierKeys;
	}

	public void setModifierKeys(int[] modifierKeys) {
		this.modifierKeys = modifierKeys;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public boolean isLastState() {
		return lastState;
	}

	public void setLastState(boolean lastState) {
		this.lastState = lastState;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Returns a string suitable for restoring this instance of KeyBinding from
	 * a file. It does not store temporary properties such as whether it was
	 * pressed at last check, but does store properties such as the keys it
	 * involves and its name and action.
	 * 
	 * @return
	 */
	public String toConfigString() {
		StringBuilder configStringBuffer = new StringBuilder().append("Key;")
				.append(displayName).append(";").append(action).append(";")
				.append(Integer.toString(mainKey)).append(";");
		for (int i = 0; i < modifierKeys.length; i++) {
			configStringBuffer.append(Integer.toString(modifierKeys[i]));
			if (i < modifierKeys.length - 1) {
				// If not the last modifer key, add a comma
				configStringBuffer.append(",");
			}
		}
		return configStringBuffer.toString();
	}

	/**
	 * 
	 * 
	 * @param config
	 * @return
	 */
	public static KeyBinding fromConfigString(String config) throws Exception {
		String[] tokens = config.split(";");
		if (!tokens[0].equalsIgnoreCase("key")) {
			// Bad config string
			return null;
		}

		String displayName = tokens[1];
		String action = tokens[2];
		int mainKey = Integer.parseInt(tokens[3]);


		int[] modifierKeys = new int[0];
		if (tokens.length >= 5) {
			String[] modifierKeyTokens = tokens[4].split(",");
			modifierKeys = new int[modifierKeyTokens.length];
			modifierKeys[0] = -1234;
			int currmodkey = 0;
			for (String s : modifierKeyTokens) {
				if (s.trim().length() > 0) {
					modifierKeys[currmodkey] = Integer.parseInt(s);
				}
				currmodkey++;
			}
			if (modifierKeys[0] == -1234) {
				// no modifier keys
				modifierKeys = new int[0];
			}
		}

		KeyBinding k = new KeyBinding(displayName, action, mainKey,
				modifierKeys);
		return k;

	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		KeyBinding k = new KeyBinding(displayName, action, mainKey, modifierKeys.clone());
		return k;
	}
	
	
}
