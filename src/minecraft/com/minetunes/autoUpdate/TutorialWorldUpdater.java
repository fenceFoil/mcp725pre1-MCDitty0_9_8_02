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

package com.minetunes.autoUpdate;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiMainMenu;
import net.minecraft.src.StatList;
import net.minecraft.src.WorldClient;

import com.minetunes.Minetunes;
import com.minetunes.Point3D;
import com.minetunes.config.MinetunesConfig;
import com.minetunes.resources.ResourceManager;
import com.minetunes.signs.SignTuneParser;
import com.minetunes.signs.Comment;

/**
 * Manages checking the version of and downloading MineTunesLand from the
 * internet.
 * 
 */
public class TutorialWorldUpdater extends FileUpdater {

	/**
	 * @param versionInfoURL
	 * @param fileTitle
	 */
	public TutorialWorldUpdater(String versionInfoURL) {
		super(versionInfoURL, "tutorial");
	}

	public boolean downloadingExampleWorld = false;

	public String downloadExampleWorld(boolean quiet, String mcVersion) {
		if (downloadingExampleWorld == true) {
			return "§bWait for the last download to finish before starting another.";
		}
		downloadingExampleWorld = true;
		SignTuneParser.simpleLog("downloadExampleWorld called");

		if (!quiet) {
			Minetunes.showTextAsLyricNow("§aDownloading MineTunesLand...");
			Minetunes.showTextAsLyricNow("§aChecking version...");
		}

		String newVersion = getLatestVersion(MinetunesConfig.MC_CURRENT_VERSION);

		if (!quiet) {
			if (CompareVersion.isVersionNumber(newVersion)) {
				Minetunes.showTextAsLyricNow("§aDownloading version "
						+ newVersion + " for Minecraft "
						+ MinetunesConfig.MC_CURRENT_VERSION);
			} else {
				Minetunes
						.showTextAsLyricNow("§cError getting new version info: "
								+ newVersion);
			}

			Minetunes.showTextAsLyricNow("§aDownloading...");
		}

		// Create new folder to download into
		File downloadDir = new File(MinetunesConfig.getMinetunesDir(), "exampleWorld");
		if (!downloadDir.exists()) {
			downloadDir.mkdirs();
		}
		
		// Create new file to download into
		String foundVersionURL = getLatestURL(mcVersion);
		File newVersionFile = new File(downloadDir,
				foundVersionURL.substring(foundVersionURL.lastIndexOf("/") + 1));
		SignTuneParser
				.simpleLog("Saving new version as " + newVersionFile.getPath());

		// Do the downloaad
		downloadToFile(newVersionFile, mcVersion);

		if (!quiet) {
			Minetunes.showTextAsLyricNow("§aDownload successful!");

			Minetunes.showTextAsLyricNow("§aExtracting...");
		}

		// TODO: Extract, etc.
		File saveWorldDir = new File(Minecraft.getMinecraft().getMinecraftDir()
				+ File.separator + "saves");
		ResourceManager.extractZipFiles(newVersionFile.getPath(),
				saveWorldDir.getPath());

		if (!quiet) {
			Minetunes.showTextAsLyricNow("§aMineTunesLand saved.");
		}

		// Finish up
		downloadingExampleWorld = false;

		// Note the version downloaded
		MinetunesConfig.setString("tutorial.lastDownload", getLatestVersion(mcVersion));

		try {
			MinetunesConfig.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "§bMineTunesLand ready! It is now a Singleplayer world in your /saves/ folder.";
	}

	/**
	 * 
	 * @return true if an updated version has been found since the last time the
	 *         tutorial world was downloaded
	 */
	public boolean checkForUpdates(String mcVersion) {
		String newVersion = getLatestVersion(mcVersion);

		if (newVersion == null) {
			return false;
		}

		if (CompareVersion.isVersionNumber(newVersion)) {
			if (CompareVersion.compareVersions(
					MinetunesConfig.getString("tutorial.lastDownload"), newVersion) == CompareVersion.LESSER) {
				// Tutorial is outdated
				return true;
			} else {
				// Tutorial is up to date
				return false;
			}
		} else {
			// Error
			return false;
		}
	}

	public static void downloadExampleWorldButton(
			final TutorialWorldUpdater twd) {
		final Minecraft mc = Minecraft.getMinecraft();
		mc.displayGuiScreen(null);

		// Decide whether to exit to main menu
		// Look for a sign that only exists in MineTunesLand
		boolean exitToMainMenu = false;
		Minetunes.optimizeCommentList(Minecraft.getMinecraft().theWorld);
		LinkedList<Comment> allComments = Minetunes
				.getCommentsSortedByDistFrom(new Point3D(0, 0, 0));
		for (Comment c : allComments) {
			if (c.getCommentText().equalsIgnoreCase("#I Am So Proud#")) {
				exitToMainMenu = true;
				break;
			}
		}

		if (exitToMainMenu) {
			// Prevents null pointer exceptions
			Minetunes.mutePlayingDitties();

			mc.statFileWriter.readStat(StatList.leaveGameStat, 1);
			mc.theWorld.sendQuittingDisconnectingPacket();
			mc.loadWorld((WorldClient) null);
		}

		final boolean beQuiet = exitToMainMenu;
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				// Be quiet if exiting to main menu
				Minetunes.writeChatMessage(mc.theWorld, twd
						.downloadExampleWorld(beQuiet,
								MinetunesConfig.MC_CURRENT_VERSION));
			}

		});
		t.setName("MineTunesLand Downloader");
		t.start();

		if (exitToMainMenu) {
			mc.displayGuiScreen(new GuiMainMenu());
		}
	}
}
