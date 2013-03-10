package com.minetunes.gui;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;

import org.lwjgl.input.Keyboard;

import com.minetunes.Minetunes;
import com.minetunes.autoUpdate.CompareVersion;
import com.minetunes.autoUpdate.FileUpdaterListener;
import com.minetunes.autoUpdate.ModUpdater;
import com.minetunes.autoUpdate.UpdateEventLevel;
import com.minetunes.config.MinetunesConfig;
import com.minetunes.resources.ResourceManager;
import com.minetunes.signs.SignTuneParser;

/**
 * Copyright (c) 2012 William Karnavas 
 * All Rights Reserved
 */

/**
 * 
 * This file is part of MineTunes.
 * 
 * MineTunes is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
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
public class MinetunesUpdateGui extends GuiScreen implements
		FileUpdaterListener {
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
		drawString(fontRenderer, "MineTunes Version "
				+ MinetunesConfig.CURRENT_VERSION, 0, 0, 0x444444);

		// Is up to date?
		drawRect(width / 4, 70, width / 4 * 3, 88, 0x228888ff);
		drawCenteredString(fontRenderer, isUpToDateString, width / 2, 75,
				isUpToDateColor);

		// Draw label at top of screen
		drawCenteredString(fontRenderer, "Check for Updates", width / 2, 25,
				0x0000ff);

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
			mc.displayGuiScreen(new MinetunesGui(null));
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
			String errorMessage = Minetunes.openMCForumsThread();
			if (errorMessage != null) {
				isUpToDateString = errorMessage;
				isUpToDateColor = OUTDATED_COLOR;
			}
		} else if (guibutton.id == 400) {
			// Download and view the changelog
			String errorMessage = Minetunes.downloadAndShowChangelog(this);
			if (errorMessage != null) {
				isUpToDateString = errorMessage;
				isUpToDateColor = OUTDATED_COLOR;
			}
		} else if (guibutton.id == 500) {
			// Go back to menu
			mc.displayGuiScreen(new MinetunesGui(null));
		} else if (guibutton.id == 600) {
			// Auto-update
			mc.displayGuiScreen(null);

			boolean isInstalledFromModsFolder = false;
			File modsFolder2 = new File(Minecraft.getMinecraftDir(), "mods");
			File[] modsInFolder = modsFolder2.listFiles();
			if (modsFolder2 != null && modsInFolder != null) {
				for (File f : modsInFolder) {
					if (f.getName().toLowerCase().contains("minetunes")) {
						isInstalledFromModsFolder = true;
						break;
					}
				}
			}

			if (isInstalledFromModsFolder) {
				onUpdaterEvent(UpdateEventLevel.INFO, "Setup",
						"Downloading new version into mods folder.");
			}

			if (Minetunes.forgeMode && isInstalledFromModsFolder) {
				ModUpdater autoUpdater = Minetunes.autoUpdater;
				onUpdaterEvent(
						UpdateEventLevel.INFO,
						"Init",
						"Downloading version "
								+ autoUpdater
										.getLatestVersion(MinetunesConfig.MC_CURRENT_VERSION));

				File modsFolder = new File(Minecraft.getMinecraftDir(), "mods");
				File destFile = new File(modsFolder, "MineTunes-"
						+ autoUpdater.getLatestVersion(
								MinetunesConfig.MC_CURRENT_VERSION).replace(
								'.', '_') + "-MC"
						+ MinetunesConfig.MC_CURRENT_VERSION.replace('.', '_')
						+ ".zip");
				autoUpdater.addFileUpdaterListener(this);
				autoUpdater.downloadToFile(destFile,
						MinetunesConfig.MC_CURRENT_VERSION);

				// Check for successful download, and remove spare files if it
				// was
				File[] modsFiles = modsFolder.listFiles();
				boolean successfulUpdate = false;
				for (File f : modsFiles) {
					if (f.equals(destFile)) {
						successfulUpdate = true;
						break;
					}
				}

				// Respond to whether update was successful
				if (successfulUpdate) {
					// Remove spare files
					for (File f : modsFiles) {
						if (!f.equals(destFile)
								&& f.getName().toLowerCase()
										.contains("minetunes")) {
							f.deleteOnExit();
							onUpdaterEvent(UpdateEventLevel.INFO, "Cleanup",
									"Removed old MineTunes (" + f.getName()
											+ ")");
						}
					}
					Minetunes
							.showTextAsLyricNow("§aUpdate successful! Next time you start Minecraft, MineTunes will be updated to version "
									+ Minetunes.autoUpdater
											.getLatestVersion(MinetunesConfig.MC_CURRENT_VERSION));
				} else {
					// Oh well.
					Minetunes
							.showTextAsLyricNow("§cCould not update MineTunes. Sorry!");
				}
			} else {
				final MinetunesUpdateGui thisGui = this;
				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						Minetunes.autoUpdater.addFileUpdaterListener(thisGui);

						boolean result = Minetunes.autoUpdater.autoUpdate(
								MinetunesConfig.MC_CURRENT_VERSION,
								new File(MinetunesConfig.getMinetunesDir(),
										"versions"),
								MinetunesConfig.getMinetunesDir(),
								ResourceManager
										.getResource("autoUpdate/swapperJar/AutoUpdateJarSwapper.jar"));

						if (result) {
							Minetunes
									.showTextAsLyricNow("* * * * * * * * * * * * * * * * * * * * * *");
							Minetunes
									.showTextAsLyricNow("§bA backup of the old minecraft.jar was saved.");
							Minetunes
									.showTextAsLyricNow("§dNew version of MineTunes was downloaded to /.minecraft/MineTunes/Versions/");
							Minetunes.showTextAsLyricNow("§aUpdate successful! Next time you start Minecraft, MineTunes will be updated to version "
									+ Minetunes.autoUpdater
											.getLatestVersion(MinetunesConfig.MC_CURRENT_VERSION));
						} else {
							Minetunes
									.showTextAsLyricNow("<MineTunes> §cDid not auto-update.");
						}
					}

				});
				t.setName("MineTunes Update Gui AutoUpdater");
				t.start();
			}
		}
	}

	public static boolean checkForUpdates() {
		String newVersion = Minetunes.autoUpdater
				.getLatestVersion(MinetunesConfig.MC_CURRENT_VERSION);
		if (newVersion == null) {
			newVersion = "Could Not Download Version";
		}
		SignTuneParser
				.simpleLog("GuiMCDittyUpdates.checkForUpdates: downloaded version number "
						+ newVersion);
		if (CompareVersion.isVersionNumber(newVersion)) {
			if (CompareVersion.isVersionNewerThanCurrent(newVersion)) {
				// MineTunes is outdated
				isUpToDateString = OUTDATED_DETAIL + newVersion;
				isUpToDateColor = OUTDATED_COLOR;
				// Enable auto-update button
				if (autoUpdateButton != null) {
					autoUpdateButton.enabled = true;
				}
				return true;
			} else {
				// MineTunes is up to date
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
		controlList.add(new GuiButton(300, width / 4, height - 120, width / 2,
				20, "MinecraftForums Thread"));
		controlList.add(new GuiButton(400, width / 4, height - 90, width / 2,
				20, "Changelog"));
		autoUpdateButton = new GuiButton(600, width / 4, height - 60,
				width / 2, 20, "Auto-Update");
		autoUpdateButton.enabled = false;
		controlList.add(autoUpdateButton);
		controlList.add(new GuiButton(500, width / 8 * 3, height - 30,
				width / 4, 20, "Exit"));

		isUpToDateColor = DOWNLOADING_VERSION_COLOR;
		isUpToDateString = "Checking Version...";

		// Check for updates
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				Minetunes.slowDownMC(10L);
				checkForUpdates();
				Minetunes.stopMCSlowdown();
			}
		});
		t.setPriority(Thread.MAX_PRIORITY);
		t.setName("MineTunes Update Checker (Update Gui)");
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.minetunes.autoUpdate.FileUpdaterListener#onUpdaterEvent(com.minetunes
	 * .autoUpdate.UpdateEventLevel, java.lang.String, java.lang.String)
	 */
	@Override
	public void onUpdaterEvent(UpdateEventLevel level, String stage,
			String event) {
		if (level == UpdateEventLevel.ERROR || level == UpdateEventLevel.WARN
				|| level == UpdateEventLevel.INFO) {
			Minetunes.showTextAsLyricNow("<MineTunes> §7" + event);
		}
	}
}
