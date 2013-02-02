/**
 * Copyright (c) 2012 William Karnavas 
 * All Rights Reserved
 */

/**
 * 
 * This file is part of MCDitty.
 * 
 * MCDitty is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * MCDitty is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MCDitty. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.wikispaces.mcditty;

import java.util.HashSet;

import net.minecraft.client.Minecraft;
import net.minecraft.src.BlockSign;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Vec3;
import net.minecraft.src.World;

/**
 * Fires a tick event to all registered TickListeners every tick
 * 
 */
public class MCDittyTickHookEntity extends Entity {

	// public static long lastUpdateTime = 0;
	private HashSet<TickListener> tickListeners = new HashSet<TickListener>();

	/**
	 * @param par1World
	 */
	public MCDittyTickHookEntity(World par1World) {
		super(par1World);
		ignoreFrustumCheck = true;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		// Call the hook method in MCDitty
		fireTickEvent();

		// Follow player
		EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
		posX = player.posX;
		posY = player.posY+25;
		posZ = player.posZ;
	}

	/**
	 * Kinda nothing to init in this thing...
	 */
	@Override
	protected void entityInit() {
		// TODO Auto-generated method stub

	}

	/**
	 * Intentionally blank -- no need to save this fella.
	 */
	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		// TODO Auto-generated method stub

	}

	/**
	 * Intentionally blank -- no need to save this fella.
	 */
	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isInRangeToRenderVec3D(Vec3 par1Vec3) {
		return true;
	}

	@Override
	public boolean isInRangeToRenderDist(double par1) {
		return true;
	}

	/**
	 */
	public void addTickListener(TickListener t) {
		tickListeners.add(t);
	}

	private void fireTickEvent() {
		for (TickListener l : tickListeners) {
			boolean result = l.onTick(Finder.getMCTimer().elapsedPartialTicks,
					Minecraft.getMinecraft());
			if (!result) {
				tickListeners.remove(l);
			}
		}
	}

}
