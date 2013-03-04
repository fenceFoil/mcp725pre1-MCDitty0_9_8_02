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
package com.minetunes.gui;

import org.lwjgl.input.Keyboard;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;

/**
 * @author William
 *
 */
public class GuiBlockarinaTest extends GuiScreen {

	private GuiScreen backScreen;

	/**
	 * @param minetunesGui
	 */
	public GuiBlockarinaTest(GuiScreen back) {
		backScreen = back;
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		// TODO Auto-generated method stub
		super.drawScreen(par1, par2, par3);
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		if (par2 == 1) {
			mc.displayGuiScreen(backScreen);
		}
		
		if (par1 == ' ') {
			// Spacebar -- play note
			int midiNote = 64;
			if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
				midiNote = 66;
				mc.sndManager.playSoundFX("note.harp", 3.0f, 2.0f);
			} else {
				mc.sndManager.playSoundFX("note.harp", 3.0f, 1.0f);
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		// TODO Auto-generated method stub
		super.actionPerformed(par1GuiButton);
	}

	@Override
	public void initGui() {
		// TODO Auto-generated method stub
		super.initGui();
	}

	@Override
	public void updateScreen() {
		// TODO Auto-generated method stub
		super.updateScreen();
	}

}
