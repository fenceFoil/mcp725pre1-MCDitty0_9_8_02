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
package com.minetunes.particle;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityFX;
import net.minecraft.src.Tessellator;
import net.minecraft.src.World;

/**
 * @author William
 * 
 */
public class EntityFXMinetunes extends EntityFX {

	/**
	 * @param par1World
	 * @param par2
	 * @param par4
	 * @param par6
	 */
	public EntityFXMinetunes(World par1World, double par2, double par4,
			double par6) {
		super(par1World, par2, par4, par6);
	}

	/**
	 * @param par1World
	 * @param par2
	 * @param par4
	 * @param par6
	 * @param par8
	 * @param par10
	 * @param par12
	 */
	public EntityFXMinetunes(World par1World, double par2, double par4,
			double par6, double par8, double par10, double par12) {
		super(par1World, par2, par4, par6, par8, par10, par12);
		particleGravity = 1f;
		particleMaxAge = 15;
	}

	@Override
	public void renderParticle(Tessellator par1Tessellator, float par2,
			float par3, float par4, float par5, float par6, float par7) {
		int particleTex = Minecraft.getMinecraft().renderEngine
				.getTexture("/com/minetunes/resources/textures/particles.png");
		Minecraft.getMinecraft().renderEngine.bindTexture(particleTex);
		// if (particleAge > particleMaxAge * 0.8) {
		// particleAlpha = (particleMaxAge - particleAge)
		// / (particleMaxAge * 0.2f);
		// }
		particleAlpha = 1f - (particleAge / particleMaxAge);
		super.renderParticle(par1Tessellator, par2, par3, par4, par5, par6,
				par7);
	}

}
