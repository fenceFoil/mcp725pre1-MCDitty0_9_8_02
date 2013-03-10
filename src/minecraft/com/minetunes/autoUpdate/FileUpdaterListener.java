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
package com.minetunes.autoUpdate;

/**
 * Most of the time, you'll want the following implementation of this interface
 * (in whatever class is conveneint, or your mod's central class):<br>
 * <br>
 * onUpdaterEvent() {<br>
 * if level is "info" { <br>
 * show on the chat (possibly with [MineTunes] in front.)<br>
 * } else if level is warning or error {<br>
 * same as info, but with a yellow or red color code in front.<br>
 * }}<br>
 * <br>
 * Another option is to write the messages into a gui (preferably the
 * one where the user presses the "update" button)
 * 
 */
public interface FileUpdaterListener {

	/**
	 * Called whenever progress is made or an error is thrown during an update
	 * operation.
	 * 
	 * @param stage
	 * @param event
	 */
	public void onUpdaterEvent(UpdateEventLevel level, String stage,
			String event);
}
