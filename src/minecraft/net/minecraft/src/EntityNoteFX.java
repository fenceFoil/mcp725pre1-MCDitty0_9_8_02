/**
 * ALL CHANGES FROM MOJANG CODE:
 * 
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
package net.minecraft.src;

import com.wikispaces.mcditty.MCDitty;

public class EntityNoteFX extends EntityFX {
	float noteParticleScale;

	public EntityNoteFX(World par1World, double par2, double par4, double par6,
			double par8, double par10, double par12) {
		this(par1World, par2, par4, par6, par8, par10, par12, 2.0F);
	}

	/**
	 * Called when MCDitty makes a note particle. Faster than the constructor
	 * Minecraft calls, since if dontCheckForNoteblock is true there's no hacky
	 * check for note block value.
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param xVel
	 *            color of note
	 * @param yVel
	 *            ignored
	 * @param zVel
	 *            ignored
	 * @param scaleFactor
	 *            2.0 is the "normal" value
	 * @param dontCheckForNoteblock
	 */
	public EntityNoteFX(World world, double x, double y, double z, double xVel,
			double yVel, double zVel, float scaleFactor,
			boolean dontCheckForNoteblock) {
		super(world, x, y, z, 0.0D, 0.0D, 0.0D);
		motionX *= 0.01D;
		motionY *= 0.01D;
		motionZ *= 0.01D;
		motionY += 0.2D;
		particleRed = getRedForNote(xVel);
		particleGreen = getGreenForNote(xVel);
		particleBlue = getBlueForNote(xVel);
		particleScale *= 0.75F;
		particleScale *= scaleFactor;
		noteParticleScale = particleScale;
		particleMaxAge = 6;
		noClip = false;
		setParticleTextureIndex(64);

		if (!dontCheckForNoteblock) {
			// Try to set the note value on a tileEntityNoteBlock
			TileEntity tile = world.getBlockTileEntity((int) (x - 0.5),
					(int) (y - 1.2), (int) (z - 0.5));
			if (tile != null && tile instanceof TileEntityNote) {
				TileEntityNote noteTile = (TileEntityNote) tile;
				noteTile.note = (byte) (xVel * 24);
				MCDitty.onNoteBlockValueUpdated(noteTile);
			}
		}
	}

	public static float getBlueForNote(double xVel) {
		return MathHelper.sin(((float) xVel + 0.6666667F)
				* (float) Math.PI * 2.0F) * 0.65F + 0.35F;
	}

	public static float getGreenForNote(double xVel) {
		return MathHelper.sin(((float) xVel + 0.33333334F)
				* (float) Math.PI * 2.0F) * 0.65F + 0.35F;
	}

	public static float getRedForNote(double xVel) {
		return MathHelper.sin(((float) xVel + 0.0F) * (float) Math.PI
				* 2.0F) * 0.65F + 0.35F;
	}

	/**
	 * The constructor Minecraft calls on a note particle. Slower because it
	 * does double duty putting a note value into a noteblock tile entity. (The
	 * only way to get a noteblock's note value is when the server sends a
	 * particle and sound event to play a note).
	 * 
	 * @param par1World
	 * @param par2
	 * @param par4
	 * @param par6
	 * @param par8
	 * @param par10
	 * @param par12
	 * @param par14
	 */
	public EntityNoteFX(World par1World, double par2, double par4, double par6,
			double par8, double par10, double par12, float par14) {
		this(par1World, par2, par4, par6, par8, par10, par12, par14, false);
	}

	public void renderParticle(Tessellator par1Tessellator, float par2,
			float par3, float par4, float par5, float par6, float par7) {
		float var8 = ((float) this.particleAge + par2)
				/ (float) this.particleMaxAge * 32.0F;

		if (var8 < 0.0F) {
			var8 = 0.0F;
		}

		if (var8 > 1.0F) {
			var8 = 1.0F;
		}

		particleScale = noteParticleScale * var8;
		super.renderParticle(par1Tessellator, par2, par3, par4, par5, par6,
				par7);
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	public void onUpdate() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		if (particleAge++ >= particleMaxAge) {
			setDead();
		}

		moveEntity(motionX, motionY, motionZ);

		if (posY == prevPosY) {
			motionX *= 1.1D;
			motionZ *= 1.1D;
		}

		motionX *= 0.66D;
		motionY *= 0.66D;
		motionZ *= 0.66D;

		if (onGround) {
			motionX *= 0.7D;
			motionZ *= 0.7D;
		}
	}
}
