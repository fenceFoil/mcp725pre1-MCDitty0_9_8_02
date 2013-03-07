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
package com.minetunes.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiVideoSettings;

import com.minetunes.Finder;

public class GuiMinetimesGraphicsMenuButton extends GuiButtonL {

	private GuiScreen backscreen;
	
	private int texNum = -1;

	public GuiMinetimesGraphicsMenuButton(GuiScreen backscreen) {
		super("signGraphics", 5, 5, 20, 20, -100000, 12);
		id = 1923129982;
		this.backscreen = backscreen;
	}

	@Override
	public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3) {
		boolean result = super.mousePressed(par1Minecraft, par2, par3);
		if (result) {
			// Open MineTunes video menu
			Minecraft.getMinecraft().displayGuiScreen(
					new GraphicsGui(backscreen));
		}
		return result;
	}

	public GuiScreen getBackscreen() {
		return backscreen;
	}

	public void setBackscreen(GuiScreen backscreen) {
		this.backscreen = backscreen;
	}

	@Override
	public void drawButton(Minecraft mc, int mx, int my) {
		if (texNum == -1) {
			texNum = Minecraft.getMinecraft().renderEngine
					.getTexture("/com/minetunes/resources/textures/signEditor1.png");
			setIconTex(texNum);
		}
		super.drawButton(mc, mx, my);
	}
}
