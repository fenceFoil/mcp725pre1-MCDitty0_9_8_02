package net.minecraft.src;



import com.minetunes.Finder;
import com.minetunes.Minetunes;

/**
 * Changes to Mojang AB code are copyrighted:
 * 
 * Copyright (c) 2012 William Karnavas All Rights Reserved
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

public class TileEntitySign extends TileEntity {

	public String signText[] = { "", "", "", "" };

	/**
	 * The index of the line currently being edited. Only used on client side,
	 * but defined on both. Note this is only really used when the > < are going
	 * to be visible.
	 */
	public int lineBeingEdited;
	private boolean isEditable;

	public TileEntitySign() {
		lineBeingEdited = -1;
		isEditable = true;
	}

	/**
	 * Notably, this method is called by NetClientHandler when it changes the
	 * text of a sign after reading a packet.
	 * 
	 * Here, it checks to see if the sign's text has been changed by a buggy
	 * packet, and resets it if it has been.
	 */
	@Override
	public void onInventoryChanged() {
		super.onInventoryChanged();
	}

	/**
	 * Writes a tile entity to NBT.
	 * 
	 * You may note that this is completely unaltered from an unmodified
	 * TileEntitySign's code.
	 */
	public void writeToNBT(NBTTagCompound par1NBTTagCompound) {
		super.writeToNBT(par1NBTTagCompound);
		par1NBTTagCompound.setString("Text1", signText[0]);
		par1NBTTagCompound.setString("Text2", signText[1]);
		par1NBTTagCompound.setString("Text3", signText[2]);
		par1NBTTagCompound.setString("Text4", signText[3]);
	}

	/**
	 * Reads a tile entity from NBT. Again, same as a vanilla TileEntitySign
	 */
	public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
		// System.out.println ("Reading sign from NBT...");
		isEditable = false;
		super.readFromNBT(par1NBTTagCompound);

		for (int i = 0; i < 4; i++) {
			signText[i] = par1NBTTagCompound.getString((new StringBuilder())
					.append("Text").append(i + 1).toString());

			if (signText[i].length() > 15) {
				signText[i] = signText[i].substring(0, 15);
			}
		}
	}

	public boolean isEditable() {
		return isEditable;
	}

	/**
	 * Sets the sign's isEditable flag to the specified parameter.
	 */
	public void setEditable(boolean par1) {
		isEditable = par1;
	}

	/**
	 * New in 1.3.1 Release
	 */
	@Override
	public Packet getDescriptionPacket() {
		// System.out.println ("func 70319 called");
		String as[] = new String[4];
		System.arraycopy(signText, 0, as, 0, 4);
		return new Packet130UpdateSign(xCoord, yCoord, zCoord, as);
	}
}
