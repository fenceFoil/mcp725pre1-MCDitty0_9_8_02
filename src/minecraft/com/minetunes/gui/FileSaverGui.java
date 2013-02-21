/**
 * Copyright (c) 2012 William Karnavas 
 * All Rights Reserved
 */

/**
 * 
 * This file is part of MineTunes.
 * 
 * MineTunes is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * MineTunes is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MineTunes. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.minetunes.gui;

import java.io.File;

import net.minecraft.src.ChatAllowedCharacters;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiTextField;

import org.lwjgl.input.Keyboard;

import com.minetunes.config.MinetunesConfig;

public class FileSaverGui extends GuiScreen {

	private GuiScreen previousScreen;
	private File saveDir;
	private String extension;
	private String title;
	private String saveFileName = null;
	private GuiTextField saveNameTextField;
	private GuiButton saveButton;
	private boolean overwriting = false;

	public FileSaverGui(GuiScreen previousScreen, File saveDir, String extension, String title) {
		this.previousScreen = previousScreen;
		this.saveDir = saveDir;
		this.extension = extension;
		this.title = title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#drawScreen(int, int, float)
	 */
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		drawDefaultBackground();

		// Print version
		drawString(fontRenderer, "MineTunes Version " + MinetunesConfig.CURRENT_VERSION, 0, 0, 0x444444);

		// Draw label at top of screen
		drawCenteredString(fontRenderer, title, width / 2, 25, 0xffffff);
		drawCenteredString(fontRenderer, "(Press Escape To Cancel)", width / 2, 35, 0x444444);

		// Draw helpful labels
		drawCenteredString(fontRenderer, "Type a Name", width / 2, 85, 0xdddddd);

		// Draw text area
		saveNameTextField.drawTextBox();
		
		// Show overwriting warning
		if (overwriting) {
			drawCenteredString(fontRenderer, "Will Overwrite "+saveFileName, width/2, 125, 0xff0000);
		}

		super.drawScreen(par1, par2, par3);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#keyTyped(char, int)
	 */
	@Override
	protected void keyTyped(char par1, int par2) {
		if (par2 == Keyboard.KEY_ESCAPE) {
			// Same as clicking cancel
			saveFileName = null;
			if (previousScreen instanceof FileSaverGuiListener) {
				FileSaverGuiListener l = (FileSaverGuiListener) previousScreen;
				l.setSavedFile(null);
			}
			mc.displayGuiScreen(previousScreen);
		} else if (par2 == Keyboard.KEY_RETURN) {
			// Same as clicking save
			if (previousScreen instanceof FileSaverGuiListener) {
				FileSaverGuiListener l = (FileSaverGuiListener) previousScreen;
				if (!saveFileName.endsWith(".txt")) {
					saveFileName += ".txt";
				}
				l.setSavedFile(saveDir.getPath() + File.separator + saveFileName);
			}
			mc.displayGuiScreen(previousScreen);
		} else {
			saveNameTextField.textboxKeyTyped(par1, par2);
		}
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
			// save
			// Leave the current save file name
			if (previousScreen instanceof FileSaverGuiListener) {
				FileSaverGuiListener l = (FileSaverGuiListener) previousScreen;
				if (!saveFileName.endsWith(".txt")) {
					saveFileName += ".txt";
				}
				l.setSavedFile(saveDir.getPath() + File.separator + saveFileName);
			}
			mc.displayGuiScreen(previousScreen);
		} else if (guibutton.id == 200) {
			// cancel
			// Clear the current save file name
			saveFileName = null;
			if (previousScreen instanceof FileSaverGuiListener) {
				FileSaverGuiListener l = (FileSaverGuiListener) previousScreen;
				l.setSavedFile(null);
			}
			mc.displayGuiScreen(previousScreen);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#initGui()
	 */
	@Override
	public void initGui() {
		// Add buttons
		saveButton = new GuiButton(100, width / 2 - 55, height - 60, 110, 20, "Save");
		controlList.add(saveButton);
		controlList.add(new GuiButton(200, width / 2 - 55, height - 20, 110, 20, "Cancel"));

		saveNameTextField = new GuiTextField(fontRenderer, width / 4, 100, width / 2, 20);
		saveNameTextField.setFocused(true);
		saveNameTextField.setCanLoseFocus(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#updateScreen()
	 */
	@Override
	public void updateScreen() {
		saveNameTextField.updateCursorCounter();

		saveFileName = saveNameTextField.getText();
		// Filter out invalid characters
		for (int loc = 0; loc < saveFileName.length(); loc++) {
			if (!ChatAllowedCharacters.isAllowedCharacter((saveFileName.toCharArray()[loc]))) {
				saveFileName.replace(saveFileName.toCharArray()[loc], '-');
			}
		}

		if (saveFileName.trim().length() <= 0) {
			// Cannot save
			saveButton.enabled = false;
		} else {
			saveButton.enabled = true;
		}
		
		// Check for overwrite
		String saveFileNameWithExtension = saveFileName;
		if (!saveFileName.endsWith(".txt")) {
			saveFileNameWithExtension += ".txt";
		}
		File saveFile = new File (saveDir, saveFileNameWithExtension);
		if (saveFile.exists()) {
			overwriting  = true;
		} else {
			overwriting = false;
		}
		
		// TODO: Show message if name exists
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#onGuiClosed()
	 */
	@Override
	public void onGuiClosed() {
		// TODO Auto-generated method stub
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
	protected void mouseClicked(int par1, int par2, int par3) {
		saveNameTextField.mouseClicked(par1, par2, par3);
		super.mouseClicked(par1, par2, par3);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#mouseMovedOrUp(int, int, int)
	 */
	@Override
	protected void mouseMovedOrUp(int par1, int par2, int par3) {
		super.mouseMovedOrUp(par1, par2, par3);
	}
}
