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
import net.minecraft.src.EntityVillager;
import net.minecraft.src.World;

import com.minetunes.Finder;

public class EntityVillagerBot extends EntityVillager {

//	private AnimatedValue headPitch = new AnimatedValue(0,
//			Minecraft.getMinecraft().theWorld.getTotalWorldTime());

	public EntityVillagerBot(World par1World) {
		super(par1World);
		// TODO Auto-generated constructor stub
		jumpMovementFactor = 0;
	}

	public EntityVillagerBot(World par1World, int par2) {
		super(par1World, par2);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.EntityVillager#isAIEnabled()
	 */
	@Override
	public boolean isAIEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.EntityLiving#onEntityUpdate()
	 */
	@Override
	public void onEntityUpdate() {
		super.onEntityUpdate();
		posY = lastTickPosY;
		motionY = 0;
		//rotationPitch = (float) headPitch.getValue();
		// rotationYawHead += 15;
		// newPosRotationIncrements = 1;
		// newPosX = posX;
		// newPosY = posY;
		// newPosZ = posZ;
		// rotationYaw -= 5;
		// legSwing = 0.5f;
		// legYaw += 0.5;
		// legYaw = 0.5f;
		// renderYawOffset -= 5;
	}

}
