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
package com.minetunes.signs.keywords;

import java.util.LinkedList;
import java.util.Random;

import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntitySign;
import net.minecraft.src.World;

import com.minetunes.Minetunes;
import com.minetunes.Point3D;
import com.minetunes.ditty.Ditty;
import com.minetunes.ditty.event.FireworkEvent;
import com.minetunes.signs.SignTuneParser;
import com.minetunes.signs.SignDitty;

/**
 * 
 */
public class FireworkKeyword extends SignTuneKeyword {

	private static Random random = new Random();

	private int up = 1;

	public FireworkKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	@Override
	public void parse() {
		// Get number of blocks to move; default is 1 if not specified
		int numArgs = getWholeKeyword().split(" ").length;
		if (numArgs == 2) {
			String argument = getWholeKeyword().split(" ")[1];
			if (argument.trim().matches("[+-]?\\d+")) {
				setUp(Integer.parseInt(argument.trim()));
			} else {
				// Error: invalid agument
				setGoodKeyword(false);
				setErrorMessageType(ERROR);
				setErrorMessage("Follow Firework with a number.");
			}
		} else if (numArgs > 2) {
			// Warning: Too Many Arguments
			setGoodKeyword(true);
			setErrorMessageType(INFO);
			setErrorMessage("Only one number is needed.");
		}
	}

	/**
	 * @return the amountMove
	 */
	public int getUp() {
		return up;
	}

	/**
	 * @param amountMove
	 *            the amountMove to set
	 */
	public void setUp(int amountMove) {
		this.up = amountMove;
	}

	@Override
	public Point3D execute(Ditty ditty, Point3D currSignPoint,
			TileEntitySign signTileEntity, Point3D nextSign, World world,
			StringBuilder readMusicString) {

		// Find nearby fireworks in frames
		LinkedList<ItemStack> fireworks = Minetunes.getFramedItemsNearby(world,
				currSignPoint, 2, 401);

		if (fireworks.size() > 0) {
			// Choose a firework
			ItemStack fireworkItem = fireworks.get(random.nextInt(fireworks
					.size()));

			// Create the event
			int yOffset = getUp();
			FireworkEvent event = new FireworkEvent(currSignPoint.x + 0.5f,
					currSignPoint.y + yOffset, currSignPoint.z + 0.5f,
					fireworkItem, ditty.getDittyID());

			// Add the event to the ditty
			int eventID = ditty.addDittyEvent(event);
			ditty.addMusicStringTokens(readMusicString,
					SignTuneParser.TIMED_EVENT_TOKEN + eventID, false);
		} else {
			// No fireworks :(
			ditty.addErrorMessage("A firework sign has no fireworks in Item Frames nearby.");
			for (int i = 0; i < 4; i++) {
				if (ditty instanceof SignDitty) {
					((SignDitty) ditty).addErrorHighlight(currSignPoint, i);
				}
			}
		}

		return null;
	}

}
