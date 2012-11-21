package com.wikispaces.mcditty.gui;

import net.minecraft.src.BlockSign;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;

import org.lwjgl.input.Keyboard;

import com.wikispaces.mcditty.CompareVersion;
import com.wikispaces.mcditty.MCDitty;
import com.wikispaces.mcditty.config.MCDittyConfig;

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
public class GuiMCDittyUpdates extends GuiScreen {
	private static final String UP_TO_DATE = "Up To Date!";
	private static final String UP_TO_DATE_DETAIL = "Up To Date: Found Version ";
	private static final String OUTDATED_DETAIL = "New Version Available: ";
	private static final String UNKNOWN_IF_UP_TO_DATE = "???";

	private static final int UP_TO_DATE_COLOR = 0xffffff;
	private static final int OUTDATED_COLOR = 0x00ff00;
	private static final int UNKNOWN_IF_UP_TO_DATE_COLOR = 0x8888ff;
	private static final int DOWNLOADING_VERSION_COLOR = 0x0000ff;
	private static final int ERROR_COLOR = 0xffff00;

	private static String isUpToDateString = UNKNOWN_IF_UP_TO_DATE;
	private static int isUpToDateColor = UNKNOWN_IF_UP_TO_DATE_COLOR;
	private static GuiButton autoUpdateButton;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#drawScreen(int, int, float)
	 */
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		drawDefaultBackground();
	
		// Print version
		drawString(fontRenderer, "MCDitty Version " + MCDittyConfig.CURRENT_VERSION, 0, 0, 0x444444);
	
		// Is up to date?
		drawRect(width / 4, 70, width / 4 * 3, 88, 0x228888ff);
		drawCenteredString(fontRenderer, isUpToDateString, width / 2, 75, isUpToDateColor);
	
		// Draw label at top of screen
		drawCenteredString(fontRenderer, "Check for Updates", width / 2, 25, 0x0000ff);
	
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
			mc.displayGuiScreen(new GuiMCDitty());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#mouseClicked(int, int, int)
	 */
	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		// TODO Auto-generated method stub
		super.mouseClicked(par1, par2, par3);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#mouseMovedOrUp(int, int, int)
	 */
	@Override
	protected void mouseMovedOrUp(int par1, int par2, int par3) {
		// TODO Auto-generated method stub
		super.mouseMovedOrUp(par1, par2, par3);
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
			// Check for updates now
			checkForUpdates();
		} else if (guibutton.id == 300) {
			// Open MCForums thread
			String errorMessage = BlockSign.openMCForumsThread();
			if (errorMessage != null) {
				isUpToDateString = errorMessage;
				isUpToDateColor = OUTDATED_COLOR;
			}
		} else if (guibutton.id == 400) {
			// Download and view the changelog
			String errorMessage = BlockSign.downloadAndShowChangelog(this);
			if (errorMessage != null) {
				isUpToDateString = errorMessage;
				isUpToDateColor = OUTDATED_COLOR;
			}
		} else if (guibutton.id == 500) {
			// Go back to menu
			mc.displayGuiScreen(new GuiMCDitty());
		} else if (guibutton.id == 600) {
			// Auto-update
			mc.displayGuiScreen(null);
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					BlockSign.showTextAsLyricNow(BlockSign.autoUpdate());
				}

			});
			t.setName("MCDitty Update Gui AutoUpdater");
			t.start();
		}
	}

	public static boolean checkForUpdates() {
		String newVersion = BlockSign.downloadCurrentVersion(MCDittyConfig.MC_CURRENT_VERSION);
		BlockSign.simpleLog("GuiMCDittyUpdates.checkForUpdates: downloaded version number " + newVersion);
		if (CompareVersion.isVersionNumber(newVersion)) {
			if (CompareVersion.compareVersions(MCDittyConfig.CURRENT_VERSION, newVersion) == CompareVersion.LESSER) {
				// MCDitty is outdated
				isUpToDateString = OUTDATED_DETAIL + newVersion;
				isUpToDateColor = OUTDATED_COLOR;
				// Enable auto-update button
				if (autoUpdateButton != null) {
					autoUpdateButton.enabled = true;
				}
				return true;
			} else {
				// MCDitty is up to date
				isUpToDateString = UP_TO_DATE_DETAIL + newVersion;
				isUpToDateColor = UP_TO_DATE_COLOR;
				if (autoUpdateButton != null) {
					autoUpdateButton.enabled = false;
				}
				return false;
			}
		} else {
			// Error
			isUpToDateString = newVersion;
			isUpToDateColor = ERROR_COLOR;
			return false;
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
		// controlList.add(new GuiButton(100, width / 4, height - 90, width / 2,
		// 20, "Check Now"));
		controlList.add(new GuiButton(300, width / 4, height - 120, width / 2, 20, "Open MinecraftForums Thread"));
		controlList.add(new GuiButton(400, width / 4, height - 90, width / 2, 20, "Changelog"));
		autoUpdateButton = new GuiButton(600, width / 4, height - 60, width / 2, 20, "Auto-Update");
		autoUpdateButton.enabled = false;
		controlList.add(autoUpdateButton);
		controlList.add(new GuiButton(500, width / 8 * 3, height - 30, width / 4, 20, "Exit"));

		isUpToDateColor = DOWNLOADING_VERSION_COLOR;
		isUpToDateString = "Checking Version...";

		// Check for updates
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				MCDitty.slowDownMC(10L);
				checkForUpdates();
				MCDitty.stopMCSlowdown();
			}
		});
		t.setPriority(Thread.MAX_PRIORITY);
		t.setName("MCDitty Update Checker (Update Gui)");
		t.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#updateScreen()
	 */
	@Override
	public void updateScreen() {
		// TODO Auto-generated method stub
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
}
