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

import java.io.File;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.minetunes.Minetunes;
import com.minetunes.autoUpdate.TutorialWorldUpdater;
import com.minetunes.config.MinetunesConfig;
import com.minetunes.resources.ResourceManager;

/**
 * @author William
 * 
 */
public class TutorialGui extends GuiScreen {

	private GuiScreen backGui;
	private GuiScrollingTextPanel textPanel;
	private File sourceFile;
	private String title = "Visit MineTunesLand";
	private GuiButton downloadButton;

	private static final int UP_TO_DATE_COLOR = 0xffffff;
	private static final int OUTDATED_COLOR = 0x00ff00;

	public TutorialGui(GuiScreen backScreen) {
		backGui = backScreen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#initGui()
	 */
	@Override
	public void initGui() {
		controlList
				.add(new GuiButton(100, width / 2 - 100, height - 30, "Done"));
		controlList.add(new MinetunesVersionGuiElement(100));

		textPanel = new GuiScrollingTextPanel(10, 60, width / 2 - 20,
				height - 130, false, fontRenderer, false);

		// Load guide text
		textPanel.setText(ResourceManager
				.loadCached("help/tutorialWorldGuide.txt"));

		downloadButton = new GuiButton(200, width / 2 - 100, height - 60,
				"Download");
		controlList.add(downloadButton);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#drawScreen(int, int, float)
	 */
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		int mcDittyLandPictureNumber = mc.renderEngine
				.getTexture("/com/minetunes/resources/textures/MCDittyLand1.png");

		drawDefaultBackground();
		// GL11.glColor4f(1.0F, 0F, 1.0F, 0.5f);
		mc.renderEngine.bindTexture(mcDittyLandPictureNumber);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5f);
		drawTexturedModalRect(width - 10 - (width / 2 - 20), 60, 0, 0,
				Math.min(width / 2 - 20, 256), Math.min(height - 130, 133));
		// drawTexturedModalRect(width - 10 - (width / 2 - 20), 60, 0, 0,
		// 256, 133);

		// Draw label at top of screen
		drawCenteredString(fontRenderer, title, width / 2, 15, 0xaaaaff);

		// Is up to date?
		String isUpToDateString;
		int isUpToDateColor;
		// TODO: Make more robust
		if (Minetunes.tutorialUpdater
				.checkForUpdates(MinetunesConfig.MC_CURRENT_VERSION) == true) {
			isUpToDateString = "New Version Found: "
					+ Minetunes.tutorialUpdater
							.getLatestVersion(MinetunesConfig.MC_CURRENT_VERSION);
			isUpToDateColor = OUTDATED_COLOR;
			downloadButton.displayString = "Download MineTunesLand";
		} else {
			isUpToDateString = "Up To Date ("
					+ Minetunes.tutorialUpdater
							.getLatestVersion(MinetunesConfig.MC_CURRENT_VERSION)
					+ ")";
			isUpToDateColor = UP_TO_DATE_COLOR;
			downloadButton.displayString = "Reset MineTunesLand";
		}
		drawRect(width / 4, 70 - 40, width / 4 * 3, 88 - 40, 0x228888ff);
		drawCenteredString(fontRenderer, isUpToDateString, width / 2, 75 - 40,
				isUpToDateColor);

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
		} else if (guibutton.id == 200) {
			// Download
			TutorialWorldUpdater.downloadExampleWorldButton(Minetunes.tutorialUpdater);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#keyTyped(char, int)
	 */
	@Override
	protected void keyTyped(char par1, int par2) {
		if (par2 == Keyboard.KEY_ESCAPE) {
			mc.displayGuiScreen(backGui);
		}
		// super.keyTyped(par1, par2);
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
