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

import net.minecraft.client.Minecraft;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;

import com.minetunes.Finder;
import com.minetunes.bot.action.BotAction;
import com.minetunes.bot.action.DestroyAction;
import com.minetunes.bot.action.RiseAction;

/**
 *
 */
public class VillagerBot extends Bot {

	protected Entity entity;

	private EntityVillagerBot villagerEntity;

	/**
	 * @param name
	 */
	public VillagerBot(String name, float x, float y, float z, float rotation) {
		super(name);

		// Add all actions this bot supports
		addSupportedAction(RiseAction.class);

		// Create a creepily still-looking testificate dude
		villagerEntity = new EntityVillagerBot(Minecraft.getMinecraft().theWorld);

		double var2 = x;
		double var4 = y;
		double var6 = z;
		float var8 = rotation;
		float var9 = 0;
		EntityLiving var10 = villagerEntity;
		var10.serverPosX = (int) x;
		var10.serverPosY = (int) y;
		var10.serverPosZ = (int) z;
		var10.rotationYawHead = rotation;
		var10.rotationYaw = rotation;

		Entity[] var11 = var10.getParts();

		if (var11 != null) {
			int var12 = 12341234 - var10.entityId;
			Entity[] var13 = var11;
			int var14 = var11.length;

			for (int var15 = 0; var15 < var14; ++var15) {
				Entity var16 = var13[var15];
				var16.entityId += var12;
			}
		}

		var10.entityId = 45645656;
		var10.setPositionAndRotation(var2, var4, var6, var8, var9);
		var10.motionX = (double) ((float) 0 / 8000.0F);
		var10.motionY = (double) ((float) 0 / 8000.0F);
		var10.motionZ = (double) ((float) 0 / 8000.0F);
		Minecraft.getMinecraft().theWorld
				.addEntityToWorld(var10.entityId, var10);
	}
	
	@Override
	public void doAction(BotAction action) {
		super.doAction(action);

		if (action instanceof RiseAction) {

		} else if (action instanceof DestroyAction) {
			villagerEntity.setDead();
		}
	}

}
