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
import java.io.FilenameFilter;
import java.util.ArrayList;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiTextField;

import org.lwjgl.input.Keyboard;

import com.minetunes.config.MinetunesConfig;

public class FileSelectorGui extends GuiScreen {

	private GuiScreen previousScreen;
	private File fileDir;
	private String title;
	private GuiTextField fileNameTextField;
	private GuiButton openButton;
	private FilenameFilter fileFilter;
	private ArrayList<File> files = null;
	private ArrayList<File> matchingFiles = new ArrayList<File>();

	public FileSelectorGui(GuiScreen previousScreen, File fileDir, FilenameFilter filter, String title) {
		this.previousScreen = previousScreen;
		this.fileDir = fileDir;
		this.title = title;
		this.fileFilter = filter;

		// Load files in dir
		files = new ArrayList<File>();
		File[] filesInDir = fileDir.listFiles(fileFilter);
		for (File f : filesInDir) {
			files.add(f);
		}
		
		// Init list of matching files
		matchingFiles.addAll(files);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#initGui()
	 */
	@Override
	public void initGui() {
		// Add buttons
		openButton = new GuiButton(100, 10, height - 30, 70, 20, "Open");
		controlList.add(openButton);
		controlList.add(new GuiButton(200, width - 80, height - 30, 70, 20, "Cancel"));

		fileNameTextField = new GuiTextField(fontRenderer, width / 4, 45, width / 2, 20);
		fileNameTextField.setFocused(true);
		fileNameTextField.setCanLoseFocus(false);
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
		drawCenteredString(fontRenderer, title, width / 2, 15, 0xffffff);

		// Draw helpful labels
		drawCenteredString(fontRenderer, "Type a Name, or Part of a Name", width / 2, 30, 0xdddddd);
		drawCenteredString(fontRenderer, "Matches:", width / 2, 90, 0xffffffff);

		// Draw text area
		fileNameTextField.drawTextBox();

		// Show matching files
		if (files.size() <= 0) {
			drawCenteredString(fontRenderer, "Folder is Empty", width / 2, height / 2, 0xff0000);
			openButton.enabled = false;
		} else {
			if (files.size() > 0 && matchingFiles.size() <= 0) {
				drawCenteredString(fontRenderer, "No Matching Files", width / 2, height / 2, 0xff0000);
				openButton.enabled = false;
			} else {
				openButton.enabled = true;
				drawCenteredString(fontRenderer, "Will Open: " + matchingFiles.get(0).getName(), width / 2, 100, 0x00ff00);
				for (int i = 1; i < matchingFiles.size(); i++) {
					int yOnGui = 100 + i * 12;
					if (yOnGui > height - 30) {
						break;
					} else {
						drawCenteredString(fontRenderer, matchingFiles.get(i).getName(), width / 2, yOnGui, 0x004400);
					}
				}
			}
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
			actionPerformed(new GuiButton(200, 0, 0, ""));
		} else if (par2 == Keyboard.KEY_RETURN) {
			// Same as clicking open
			actionPerformed(new GuiButton(100, 0, 0, ""));
		} else {
			fileNameTextField.textboxKeyTyped(par1, par2);
			// Update matching files
			String matchString = fileNameTextField.getText();
			matchingFiles.clear();
			if (matchString.length() > 0) {
				for (File f : files) {
					if (f.getName().contains(matchString)) {
						// Found a match
						matchingFiles.add(f);
					}
				}
			} else {
				matchingFiles.addAll(files);
			}
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
			// open
			if (previousScreen instanceof FileSelectorGuiListener) {
				((FileSelectorGuiListener) previousScreen).fileSelected(matchingFiles.get(0));
			}
			mc.displayGuiScreen(previousScreen);
		} else if (guibutton.id == 200) {
			// cancel
			if (previousScreen instanceof FileSelectorGuiListener) {
				((FileSelectorGuiListener) previousScreen).fileSelected(null);
			}
			mc.displayGuiScreen(previousScreen);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#updateScreen()
	 */
	@Override
	public void updateScreen() {
		fileNameTextField.updateCursorCounter();

		super.updateScreen();
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
		fileNameTextField.mouseClicked(par1, par2, par3);
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
