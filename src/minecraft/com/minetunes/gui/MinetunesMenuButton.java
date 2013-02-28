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

import com.minetunes.Finder;

/**
 * A button that appears in the upper left corner of a gui. Opens the MineTunes
 * menu when pressed. To add to a vanilla gui screen, put a new instance of this
 * into its controlList. No need to modifiy vanilla gui to do this!
 * 
 */
public class MinetunesMenuButton extends GuiButton {

	public MinetunesMenuButton() {
		// Arbitrary id number, not likely to conflict with vanilla gui
		super(19087890, 5, 5, 70, 20, "MineTunes");
	}

	@Override
	public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3) {
		boolean result = super.mousePressed(par1Minecraft, par2, par3);
		if (result) {
			// Open MineTunes menu
			Minecraft.getMinecraft().displayGuiScreen(new MinetunesGui(Minecraft.getMinecraft().currentScreen));
		}
		return result;
	}
}
