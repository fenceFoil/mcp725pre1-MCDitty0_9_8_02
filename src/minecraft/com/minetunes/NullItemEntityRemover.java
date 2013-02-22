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
package com.minetunes;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityItem;
import net.minecraft.src.WorldServer;

import com.minetunes.blockTune.EntityItemDisplay;

/**
 * @author William
 * 
 */
public class NullItemEntityRemover implements TickListener {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wikispaces.MineTunes.TickListener#onTick(float,
	 * net.minecraft.client.Minecraft)
	 */
	@Override
	public boolean onTick(float partialTick, Minecraft minecraft) {
		for (int i = 0; i < minecraft.theWorld.loadedEntityList.size(); i++) {
			Object o = minecraft.theWorld.loadedEntityList.get(i);
			if (o instanceof EntityItem && !(o instanceof EntityItemDisplay)) {
				EntityItem e = (EntityItem) o;
				if (e.func_92059_d().itemID == 0) {
					// Buggy item stack found
					// Kill it with fire!
					System.err
							.println("MineTunes: Destroyed a buggy EntityItem in the Client. This is a nasty jukebox bug in Vanilla Minecraft (See https://mojang.atlassian.net/browse/MC-2711)");
					e.setDead();
				}
			}
		}
		if (minecraft.getIntegratedServer() != null
				&& minecraft.getIntegratedServer().worldServers != null) {
			int serverNum = 0;
			for (WorldServer server : minecraft.getIntegratedServer().worldServers) {
				for (int i = 0; i < server.loadedEntityList.size(); i++) {
					Object o = server.loadedEntityList.get(i);
					if (o instanceof EntityItem) {
						EntityItem e = (EntityItem) o;
						if (e.func_92059_d().itemID == 0) {
							// Buggy item stack found
							// Kill it with fire!
							System.err
									.println("MineTunes: Destroyed a buggy EntityItem in WorldServer #"
											+ serverNum
											+ ". This is a nasty jukebox bug in Vanilla Minecraft (See https://mojang.atlassian.net/browse/MC-2711)");
							e.setDead();
						}
					}
				}
				serverNum++;
			}
		}
		return true;
	}

}
