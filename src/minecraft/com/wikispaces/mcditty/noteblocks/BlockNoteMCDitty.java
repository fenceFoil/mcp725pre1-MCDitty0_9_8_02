/**
 * CHANGES FROM MOJANG CODE
 * Copyright (c) 2012 William Karnavas 
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
package com.wikispaces.mcditty.noteblocks;

import net.minecraft.src.Block;
import net.minecraft.src.BlockNote;
import net.minecraft.src.World;

import com.wikispaces.mcditty.GetMinecraft;

/**
 * The Herobrine of NoteBlock: hijacked into the list of blocks by id instead of
 * net.minecraft.src.BlockNote. Contains modified methods.
 * 
 * TODO: Move hacky solution for finding pitch of noteblock from EntityFXNote to
 * this class's onBlockEventReceived()
 * 
 */
public class BlockNoteMCDitty extends BlockNote {
	public static void removeNormalNoteBlockFromList()
			throws IllegalArgumentException, IllegalAccessException {
		Object blockListObj = GetMinecraft.getUniqueTypedFieldFromClass(
				Block.class, Block[].class, null);
		if (blockListObj != null && blockListObj instanceof Block[]) {
			Block[] blockList = (Block[]) blockListObj;
			blockList[25] = null;
		}
	}

	public BlockNoteMCDitty(int par1) {
		super(par1);
	}

//	/**
//	 * Returns the block texture based on the side being looked at. Args: side
//	 */
//	public int getBlockTextureFromSide(int par1) {
//		return this.blockIndexInTexture;
//	}
//
//	/**
//	 * Lets the block know when one of its neighbor changes. Doesn't know which
//	 * neighbor changed (coordinates passed are their own) Args: x, y, z,
//	 * neighbor blockID
//	 */
//	public void onNeighborBlockChange(World par1World, int par2, int par3,
//			int par4, int par5) {
//		if (par5 > 0) {
//			boolean var6 = par1World.isBlockIndirectlyGettingPowered(par2,
//					par3, par4);
//			TileEntityNote var7 = (TileEntityNote) par1World
//					.getBlockTileEntity(par2, par3, par4);
//
//			if (var7 != null && var7.previousRedstoneState != var6) {
//				if (var6) {
//					var7.triggerNote(par1World, par2, par3, par4);
//				}
//
//				var7.previousRedstoneState = var6;
//			}
//		}
//	}
//
//	/**
//	 * Called upon block activation (right click on the block.)
//	 */
//	public boolean onBlockActivated(World par1World, int par2, int par3,
//			int par4, EntityPlayer par5EntityPlayer, int par6, float par7,
//			float par8, float par9) {
//		if (par1World.isRemote) {
//			return true;
//		} else {
//			TileEntityNote var10 = (TileEntityNote) par1World
//					.getBlockTileEntity(par2, par3, par4);
//
//			if (var10 != null) {
//				var10.changePitch();
//				var10.triggerNote(par1World, par2, par3, par4);
//			}
//
//			return true;
//		}
//	}
//
//	/**
//	 * Called when the block is clicked by a player. Args: x, y, z, entityPlayer
//	 */
//	public void onBlockClicked(World par1World, int par2, int par3, int par4,
//			EntityPlayer par5EntityPlayer) {
//		if (!par1World.isRemote) {
//			TileEntityNote var6 = (TileEntityNote) par1World
//					.getBlockTileEntity(par2, par3, par4);
//
//			if (var6 != null) {
//				var6.triggerNote(par1World, par2, par3, par4);
//			}
//		}
//	}
//
//	/**
//	 * Returns a new instance of a block's tile entity class. Called on placing
//	 * the block.
//	 */
//	public TileEntity createNewTileEntity(World par1World) {
//		return new TileEntityNote();
//	}

	/**
	 * Called when the block receives a BlockEvent - see World.addBlockEvent. By
	 * default, passes it on to the tile entity at this location. Args: world,
	 * x, y, z, blockID, EventID, event parameter
	 */
	public void onBlockEventReceived(World par1World, int par2, int par3,
			int par4, int par5, int par6) {
		float var7 = (float) Math.pow(2.0D, (double) (par6 - 12) / 12.0D);
		String var8 = "harp";

		if (par5 == 1) {
			var8 = "bd";
		}

		if (par5 == 2) {
			var8 = "snare";
		}

		if (par5 == 3) {
			var8 = "hat";
		}

		if (par5 == 4) {
			var8 = "bassattack";
		}

		par1World.playSoundEffect((double) par2 + 0.5D, (double) par3 + 0.5D,
				(double) par4 + 0.5D, "note." + var8, 3.0F, var7);
		par1World.spawnParticle("note", (double) par2 + 0.5D,
				(double) par3 + 1.2D, (double) par4 + 0.5D,
				(double) par6 / 24.0D, 0.0D, 0.0D);
	}
}
