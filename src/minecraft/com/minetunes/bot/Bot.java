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
package com.minetunes.bot;

import java.util.LinkedList;

import com.minetunes.bot.action.BotAction;

public class Bot {

	/**
	 * The bot's name.
	 */
	private String name = "";

	/**
	 * All actions supported by this bot
	 */
	protected static LinkedList<Class<? extends BotAction>> supportedActions = new LinkedList<Class<? extends BotAction>>();

	public Bot(String name) {
		setName(name);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public void doAction(BotAction action) {
		// Currently does nothing
	}

	public LinkedList<Class<? extends BotAction>> getSupportedActions() {
		return supportedActions;
	}

	protected void addSupportedAction(Class<? extends BotAction> action) {
		supportedActions.add(action);
	}

}
