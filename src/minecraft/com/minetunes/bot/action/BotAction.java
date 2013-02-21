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
 * Represents an instruction addressed to some running bots. Also keeps static
 * info about the type of action it represents.
 */
public abstract class BotAction {

	/**
	 * Does this type of action have a cooresponding action ending in "off"?
	 */
	protected static boolean hasOffAction = false;

	/**
	 * Is this option the "off" variant of the action.
	 */
	protected boolean isOffAction = false;
	
	/**
	 * The ditty that this action was called by
	 */
	private int dittyID = -1;

	/**
	 * The address of the bots this action is aimed at. Can contain wildcards
	 * '*' and '?' (used as defined in MS-DOS).
	 */
	private String address = null;
	
	protected BotAction (String address, int dittyID) {
		setAddress(address);
		setDittyID (dittyID);
	}

	/**
	 * @return the hasOffAction
	 */
	public static boolean isHasOffAction() {
		return hasOffAction;
	}

	/**
	 * @param hasOffAction the hasOffAction to set
	 */
	public static void setHasOffAction(boolean hasOffAction) {
		BotAction.hasOffAction = hasOffAction;
	}

	/**
	 * @return the isOffAction
	 */
	public boolean isOffAction() {
		return isOffAction;
	}

	/**
	 * @param isOffAction the isOffAction to set
	 */
	public void setOffAction(boolean isOffAction) {
		this.isOffAction = isOffAction;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the dittyID
	 */
	public int getDittyID() {
		return dittyID;
	}

	/**
	 * @param dittyID the dittyID to set
	 */
	public void setDittyID(int dittyID) {
		this.dittyID = dittyID;
	}

	/**
	 * Performs the following transformations on a string:<br>
	 * <br>
	 * Changes all "?"s to ".?"s<br>
	 * Changes all "*"s to ".*"s<br>
	 * @param address2
	 * @return
	 */
	public static String convertWildcardsToRegex(String input) {
		return input.replaceAll("\\?", ".?").replaceAll("\\*", ".*");
	}
}
