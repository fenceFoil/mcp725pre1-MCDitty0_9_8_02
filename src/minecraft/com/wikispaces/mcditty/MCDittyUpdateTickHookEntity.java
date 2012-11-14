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

import net.minecraft.src.BlockSign;
import net.minecraft.src.Entity;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

/**
 * The onUpdate method calls the onTick method in MCDitty.class
 * 
 */
public class MCDittyUpdateTickHookEntity extends Entity {

	/**
	 * @param par1World
	 */
	public MCDittyUpdateTickHookEntity(World par1World) {
		super(par1World);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		
		// Call the hook method in MCDitty
		BlockSign.mcDittyMod.onTick(GetMinecraft.timer().elapsedPartialTicks,
				GetMinecraft.instance());
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

}
