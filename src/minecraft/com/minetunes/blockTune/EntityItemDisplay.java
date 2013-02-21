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
package com.minetunes.blockTune;

import net.minecraft.src.Block;
import net.minecraft.src.EntityItem;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.MathHelper;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Vec3;
import net.minecraft.src.World;

/**
 * A floating item, identical to a item or block dropped with the "q" key, that
 * can't be picked up, is client-only, and does not move or disappear after 5
 * mins.
 * 
 */
public class EntityItemDisplay extends EntityItem {

	public EntityItemDisplay(World par1World, double par2, double par4,
			double par6, ItemStack par8ItemStack) {
		super(par1World, par2, par4, par6, par8ItemStack);
	}

	public EntityItemDisplay(World par1World, double par2, double par4,
			double par6) {
		super(par1World, par2, par4, par6);
	}

	public EntityItemDisplay(World par1World) {
		super(par1World);
	}

	private int updateCount;
	private ItemStack itemStack;
	private int ageMultiplier = 1;

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate() {
		// Entity.onUpdate just calls this next line; we don't want the
		// EntityItem onUpdate to be called, so we skip the ususal
		// super.onUpdate() call
		onEntityUpdate();

		// This is the only part of EntityItem.onUpdate() that we need to
		// preserve
		age+=ageMultiplier;
	}

	@Override
	public void func_92058_a(ItemStack par1) {
		itemStack = par1;
	}

	@Override
	public ItemStack func_92059_d() {
		return itemStack;
	}

	public int getAgeMultiplier() {
		return ageMultiplier;
	}

	public void setAgeMultiplier(int ageMultiplier) {
		this.ageMultiplier = ageMultiplier;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound par1nbtTagCompound) {
		// TODO Auto-generated method stub
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound par1nbtTagCompound) {
		// TODO Auto-generated method stub
	}

	/**
	 * Render distance: 64 (up from 16)
	 */
	@Override
	public boolean isInRangeToRenderVec3D(Vec3 par1Vec3) {
		return par1Vec3.squareDistanceTo(posX, posY, posZ) < 64 * 64;
	}

	/**
	 * Render distance: 64 (up from 16)
	 */
	@Override
	public boolean isInRangeToRenderDist(double par1) {
		if (par1 < 64) {
			return true;
		} else {
			return false;
		}
	}

}
