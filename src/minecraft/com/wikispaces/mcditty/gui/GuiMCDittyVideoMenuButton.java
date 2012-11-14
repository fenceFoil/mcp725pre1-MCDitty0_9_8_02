/**
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
package com.wikispaces.mcditty.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiVideoSettings;

import com.wikispaces.mcditty.GetMinecraft;

public class GuiMCDittyVideoMenuButton extends GuiButton {

	private GuiScreen backscreen;

	public GuiMCDittyVideoMenuButton(GuiScreen backscreen) {
		super(19087892, 5, 5, 50, 20, "Signs");
		this.backscreen = backscreen;
	}

	@Override
	public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3) {
		boolean result = super.mousePressed(par1Minecraft, par2, par3);
		if (result) {
			// Open MCDitty video menu
			GetMinecraft.instance().displayGuiScreen(
					new GuiMCDittyGraphics(backscreen));
		}
		return result;
	}

	public GuiScreen getBackscreen() {
		return backscreen;
	}

	public void setBackscreen(GuiScreen backscreen) {
		this.backscreen = backscreen;
	}
}
