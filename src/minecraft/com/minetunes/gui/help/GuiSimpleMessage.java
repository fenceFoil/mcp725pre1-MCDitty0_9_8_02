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
package com.minetunes.gui.help;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;

/**
 * @author William
 * 
 */
public class GuiSimpleMessage extends GuiScreen {
	private GuiScreen backScreen;
	private String text;
	private int textColor;

	public GuiSimpleMessage(GuiScreen backScreen, String message, int color) {
		this.backScreen = backScreen;
		text = message;
		textColor = color;
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		drawDefaultBackground();
		drawCenteredString(fontRenderer, text, width / 2,
				height / 5, textColor);
		
		super.drawScreen(par1, par2, par3);
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		if (par1GuiButton.id == 0) {
			mc.displayGuiScreen(backScreen);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		controlList.add(new GuiButton(0, width / 2 - 100, height / 7 * 6, "Back"));
	}

}
