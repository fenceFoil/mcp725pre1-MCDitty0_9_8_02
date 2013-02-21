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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;

import org.lwjgl.input.Keyboard;

/**
 * 
 */
public class ChangelogGui extends GuiScreen {

	private GuiScreen backGui;
	private GuiScrollingTextPanel textPanel;
	private File sourceFile;
	private String title;

	public ChangelogGui(GuiScreen backScreen, File displayTextFile, String guiTitle) {
		backGui = backScreen;
		title = guiTitle;
		sourceFile = displayTextFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#initGui()
	 */
	@Override
	public void initGui() {
		controlList.add(new GuiButton(100, width / 2 - 100, height - 30, "Done"));
		controlList.add(new MinetunesVersionGuiElement(100));

		textPanel = new GuiScrollingTextPanel(10, 40, width - 20, height - 80, false, fontRenderer, true);

		// Load guide text
		try {
			BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
			String guideText = "";
			while (true) {
				String lineIn = reader.readLine();
				if (lineIn == null) {
					break;
				} else {
					guideText += lineIn + "\n";
				}
			}
			reader.close();
			textPanel.setText(guideText);
		} catch (IOException e) {
			e.printStackTrace();
			textPanel.setText("Couldn't read changelog. Sorry!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#drawScreen(int, int, float)
	 */
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		drawDefaultBackground();

		// Draw label at top of screen
		drawCenteredString(fontRenderer, title, width / 2, 15, 0xffff00);

		// Draw sliding panel o' text
		textPanel.draw(par1, par2);

		super.drawScreen(par1, par2, par3);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.minecraft.src.GuiScreen#actionPerformed(net.minecraft.src.GuiButton)
	 */
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id == 100) {
			// Go back
			mc.displayGuiScreen(backGui);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#updateScreen()
	 */
	@Override
	public void updateScreen() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#onGuiClosed()
	 */
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#doesGuiPauseGame()
	 */
	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#mouseClicked(int, int, int)
	 */
	@Override
	protected void mouseClicked(int x, int y, int button) {
		textPanel.mouseClicked(x, y, button);

		// TODO Auto-generated method stub
		super.mouseClicked(x, y, button);
	}

	/* (non-Javadoc)
	 * @see net.minecraft.src.GuiScreen#keyTyped(char, int)
	 */
	@Override
	protected void keyTyped(char par1, int par2) {
		if (par2 == Keyboard.KEY_ESCAPE) {
			mc.displayGuiScreen(backGui);
		}
		//super.keyTyped(par1, par2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#mouseMovedOrUp(int, int, int)
	 */
	@Override
	protected void mouseMovedOrUp(int par1, int par2, int par3) {
		textPanel.mouseMovedOrUp(par1, par2, par3);

		// TODO Auto-generated method stub
		super.mouseMovedOrUp(par1, par2, par3);
	}

}
