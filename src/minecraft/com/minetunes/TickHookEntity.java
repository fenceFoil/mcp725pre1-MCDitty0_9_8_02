/**
 * Copyright (c) 2012 William Karnavas 
 * All Rights Reserved
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
package com.minetunes;

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
public class TickHookEntity extends Entity {

	private HashSet<TickListener> tickListeners = new HashSet<TickListener>();

	/**
	 * @param par1World
	 */
	public TickHookEntity(World par1World) {
		super(par1World);
		ignoreFrustumCheck = true;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		// Call the hook method in MineTunes
		fireTickEvent();

		// Follow player
		EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
		posX = player.posX;
		posY = player.posY + 25;
		posZ = player.posZ;
	}

	/**
	 * Kinda nothing to init in this thing...
	 */
	@Override
	protected void entityInit() {

	}

	/**
	 * Intentionally blank -- no need to save this fella.
	 */
	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {

	}

	/**
	 * Intentionally blank -- no need to save this fella.
	 */
	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
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
		// Profile each listener's tick as their onTick methods are called
		// Remove them if they signal to by returning "false" from their ontick
		// methods
		// Profiling appears under root.tick.level.entity.regular.*
		// Or something like that.
		for (TickListener l : tickListeners) {
			try {
				Minecraft.getMinecraft().mcProfiler
						.startSection("MineTunes"
								+ l.getClass()
										.getName()
										.substring(
												l.getClass().getName()
														.lastIndexOf(".") + 1));
			} catch (Exception e) {
				e.printStackTrace();
			}
			boolean result = true;
			try {
				result = l.onTick(Finder.getMCTimer().elapsedPartialTicks,
						Minecraft.getMinecraft());
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!result) {
				tickListeners.remove(l);
			}
			Minecraft.getMinecraft().mcProfiler.endSection();
		}
	}
}
