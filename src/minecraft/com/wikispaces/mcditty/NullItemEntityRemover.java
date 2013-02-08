/**
 * Copyright (c) 2012-2013 William Karnavas 
 * All Rights Reserved
 */

/**
 * 
 * This file is part of MCDitty.
 * 
 * MCDitty is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MCDitty is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MCDitty. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.wikispaces.mcditty;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityItem;
import net.minecraft.src.WorldServer;

/**
 * @author William
 * 
 */
public class NullItemEntityRemover implements TickListener {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wikispaces.mcditty.TickListener#onTick(float,
	 * net.minecraft.client.Minecraft)
	 */
	@Override
	public boolean onTick(float partialTick, Minecraft minecraft) {
		for (Object o : minecraft.theWorld.loadedEntityList) {
			if (o instanceof EntityItem) {
				EntityItem e = (EntityItem) o;
				if (e.func_92059_d().itemID == 0) {
					// Buggy item stack found
					// Kill it with fire!
					System.err
							.println("MCDitty: Destroyed a buggy EntityItem in the Client. This is a nasty jukebox bug in Vanilla Minecraft (See https://mojang.atlassian.net/browse/MC-2711)");
					e.setDead();
				}
			}
		}
		int serverNum = 0;
		for (WorldServer server : minecraft.getIntegratedServer().worldServers) {
			for (Object o : server.loadedEntityList) {
				if (o instanceof EntityItem) {
					EntityItem e = (EntityItem) o;
					if (e.func_92059_d().itemID == 0) {
						// Buggy item stack found
						// Kill it with fire!
						System.err
								.println("MCDitty: Destroyed a buggy EntityItem in WorldServer #"
										+ serverNum
										+ ". This is a nasty jukebox bug in Vanilla Minecraft (See https://mojang.atlassian.net/browse/MC-2711)");
						e.setDead();
					}
				}
			}
			serverNum++;
		}
		return true;
	}

}
