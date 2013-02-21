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
package com.minetunes.ditty.event;

import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;

public class FireworkEvent extends TimedDittyEvent {
	private ItemStack fireworkItem;

	private double x;
	private double y;
	private double z;

	public FireworkEvent(double x, double y, double z, ItemStack fireworkItem,
			int dittyID) {
		super(dittyID);
		setX(x);
		setY(y);
		setZ(z);
		setFireworkItem(fireworkItem);
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public ItemStack getFireworkItem() {
		return fireworkItem;
	}

	public void setFireworkItem(ItemStack fireworkItem) {
		this.fireworkItem = fireworkItem;
	}
}
