/**
 * 
 * Changes from Mojang AB Code:
 * 
 * Copyright (c) 2012 William Karnavas All Rights Reserved
 * 
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
package com.wikispaces.mcditty.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiButton;

import org.lwjgl.opengl.GL11;

/**
 * Version of GuiSlider that does away with the unbreakable link with Minecraft
 * settings. Obviously this mod's guis don't alter Minecraft settings, so this
 * link was removed here.
 * 
 */
public class GuiMCDittySlider extends GuiButton {

	/** The value of this slider control. */
	public float sliderValue;

	/** Is this slider control being dragged. */
	public boolean dragging;

	public GuiMCDittySlider(int par1, int par2, int par3, String par5Str, float par6) {
		super(par1, par2, par3, 120, 20, par5Str);
		sliderValue = 1.0F;
		dragging = false;
		sliderValue = par6;
	}

	/**
	 * Fired when the mouse button is dragged. Equivalent of
	 * MouseListener.mouseDragged(MouseEvent e).
	 */
	protected void mouseDragged(Minecraft par1Minecraft, int par2, int par3) {
		if (!drawButton) {
			return;
		}

		if (dragging) {
			sliderValue = (float) (par2 - (xPosition + 4)) / (float) (width - 8);

			if (sliderValue < 0.0F) {
				sliderValue = 0.0F;
			}

			if (sliderValue > 1.0F) {
				sliderValue = 1.0F;
			}

			// par1Minecraft.gameSettings.setOptionFloatValue(idFloat,
			// sliderValue);
			// displayString =
			// par1Minecraft.gameSettings.getKeyBinding(idFloat);
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawTexturedModalRect(xPosition + (int) (sliderValue * (float) (width - 8)), yPosition, 0, 66, 4, 20);
		drawTexturedModalRect(xPosition + (int) (sliderValue * (float) (width - 8)) + 4, yPosition, 196, 66, 4, 20);
	}

	/**
	 * Returns true if the mouse has been pressed on this control. Equivalent of
	 * MouseListener.mousePressed(MouseEvent e).
	 */
	public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3) {
		if (super.mousePressed(par1Minecraft, par2, par3)) {
			sliderValue = (float) (par2 - (xPosition + 4)) / (float) (width - 8);

			if (sliderValue < 0.0F) {
				sliderValue = 0.0F;
			}

			if (sliderValue > 1.0F) {
				sliderValue = 1.0F;
			}

			// par1Minecraft.gameSettings.setOptionFloatValue(idFloat,
			// sliderValue);
			// displayString =
			// par1Minecraft.gameSettings.getKeyBinding(idFloat);
			dragging = true;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Fired when the mouse button is released. Equivalent of
	 * MouseListener.mouseReleased(MouseEvent e).
	 */
	public void mouseReleased(int par1, int par2) {
		dragging = false;
	}

	/**
	 * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over
	 * this button and 2 if it IS hovering over this button.
	 */
	protected int getHoverState(boolean par1) {
		return 0;
	}
}
