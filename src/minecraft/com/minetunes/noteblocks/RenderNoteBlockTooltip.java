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
package com.minetunes.noteblocks;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityNoteFX;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.MathHelper;
import net.minecraft.src.Render;
import net.minecraft.src.Tessellator;

import org.lwjgl.opengl.GL11;

public class RenderNoteBlockTooltip extends Render {
	@Override
	public void doRender(Entity var1, double x, double y, double z, float var8,
			float var9) {
		renderNoteBlockTooltip((EntityNoteBlockTooltip) var1, x + 0.5, y + 0.5,
				z + 0.5, var8, var9);
	}

	private void renderNoteBlockTooltip(EntityNoteBlockTooltip entity,
			double x, double y, double z, float var8, float maybeTime) {
		renderLivingLabel(entity, entity.getText(), x, y, z, 15);
	}

	private void renderLivingLabel(EntityNoteBlockTooltip entity,
			String labelText, double renderX, double renderY, double renderZ,
			int maxDistanceFromPlayer) {
		double sqDistToPlayer = entity
				.getDistanceSqToEntity(this.renderManager.livingPlayer);

		if (sqDistToPlayer <= (double) (maxDistanceFromPlayer * maxDistanceFromPlayer)) {
			FontRenderer fontRenderer = this.getFontRendererFromRenderManager();
			float var13 = 1.6F;
			float var14 = 0.016666668F * var13;
			GL11.glPushMatrix();
			GL11.glTranslatef((float) renderX, (float) renderY, (float) renderZ);
			GL11.glNormal3f(0.0F, 1.0F, 0.0F);
			GL11.glRotatef(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
			GL11.glScalef(-var14, -var14, var14);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDepthMask(false);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			Tessellator var15 = Tessellator.instance;

			GL11.glDisable(GL11.GL_TEXTURE_2D);
			var15.startDrawingQuads();
			int var17 = fontRenderer.getStringWidth(labelText) / 2;
			double xVel = (double) (entity.getNoteTile().note) / 24d;
			float r = getRedForNote(xVel);
			float g = getGreenForNote(xVel);
			float b = getBlueForNote(xVel);
			var15.setColorRGBA_F(r, g, b, 0.75F * entity.getOpacity());
			var15.addVertex((double) (-var17 - 1), (double) (-1), 0.0D);
			var15.addVertex((double) (-var17 - 1), (double) (8), 0.0D);
			var15.addVertex((double) (var17 + 1), (double) (8), 0.0D);
			var15.addVertex((double) (var17 + 1), (double) (-1), 0.0D);
			var15.draw();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			// Color is white + transparency, which grows paler as getOpacity
			// gets smaller
			fontRenderer
					.drawString(
							labelText,
							-fontRenderer.getStringWidth(labelText) / 2,
							0,
							(int) (0x000000 + ((int) ((1 - entity.getOpacity()) * 230) * 0xFF000000)));
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glDepthMask(true);
			fontRenderer.drawString(labelText,
					-fontRenderer.getStringWidth(labelText) / 2, 0, -1);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glPopMatrix();
		}
	}
	
	public static float getBlueForNote(double xVel) {
		return MathHelper.sin(((float) xVel + 0.6666667F) * (float) Math.PI
				* 2.0F) * 0.65F + 0.35F;
	}

	public static float getGreenForNote(double xVel) {
		return MathHelper.sin(((float) xVel + 0.33333334F) * (float) Math.PI
				* 2.0F) * 0.65F + 0.35F;
	}

	public static float getRedForNote(double xVel) {
		return MathHelper.sin(((float) xVel + 0.0F) * (float) Math.PI * 2.0F) * 0.65F + 0.35F;
	}

}
