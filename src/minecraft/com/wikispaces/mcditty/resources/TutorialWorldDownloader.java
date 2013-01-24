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

package com.wikispaces.mcditty.resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.client.Minecraft;
import net.minecraft.src.BlockSign;
import net.minecraft.src.GuiMainMenu;
import net.minecraft.src.StatList;
import net.minecraft.src.WorldClient;

import com.wikispaces.mcditty.CompareVersion;
import com.wikispaces.mcditty.Finder;
import com.wikispaces.mcditty.MCDitty;
import com.wikispaces.mcditty.Point3D;
import com.wikispaces.mcditty.config.MCDittyConfig;
import com.wikispaces.mcditty.signs.Comment;

/**
 * Manages checking the version of and downloading MCDittyLand from the
 * internet.
 * 
 */
public class TutorialWorldDownloader {

	public static boolean downloadingExampleWorld = false;
	private static boolean notYetChecked = true;

	private static String currentVersionBuffer;

	/**
	 * Attempts to retrieve the current version of the MCDittyLand from a file
	 * on the internet.
	 * 
	 * Note: Version number is cached.
	 * 
	 * @return If successful, the version from the file. If not, it will return
	 *         a string that starts with "§c"
	 */
	public static String downloadExampleWorldVersion(String mcVersion) {
		if (currentVersionBuffer != null) {
			return currentVersionBuffer;
		} else if (notYetChecked) {
			BlockSign.simpleLog("downloadCurrentVersion called");
			notYetChecked = false;

			// Create a url pointing at the current version file on dropbox
			URL currentVersionURL = null;
			try {
				// currentVersionURL = new
				// URL("http://mcditty.wikispaces.com/file/view/MCDitty_Current_Version.txt");
				String url;
				url = "http://dl.dropbox.com/s/sf8ayhlqv5vdol4/MCDitty_Example_World_Version.txt";
				currentVersionURL = new URL(url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				// If the person (fenceFoil, hopefully) editing this has half a
				// wit
				// about his person, this will never happen.
				return "§cMCDittyLand Version File URL Malformed";
			}

			// Open connection to file, and download it.
			String currVersionLegacy = null;
			ArrayList<String> currentVersionLines = new ArrayList<String>();
			try {
				// Open connection
				BufferedReader currVersionIn = new BufferedReader(
						new InputStreamReader(currentVersionURL.openStream()));
				// Read the first line of the file: this is a straight version
				// number.
				// This version number is outdated, as it does not take the
				// current
				// MC version into account.
				currVersionLegacy = currVersionIn.readLine();
				// Read subsequent lines until the words "END Versions"
				while (true) {
					String lineIn = currVersionIn.readLine();
					if (lineIn == null) {
						break;
					} else if (lineIn.equalsIgnoreCase("end versions")) {
						// End of versions keys
						// Stop reading
						break;
					} else {
						currentVersionLines.add(lineIn);
					}
				}
				currVersionIn.close();
			} catch (IOException e) {
				e.printStackTrace();
				return "§cCouldn't Download MCDittyLand Version File";
			}

			// Find the current version of the MCDittyLand for the current
			// version of
			// Minecraft

			// Process the downloaded strings into a map
			HashMap<String, String> versionKeys = new HashMap<String, String>();
			for (String s : currentVersionLines) {
				String[] parts = s.split(":");
				if (parts.length != 2) {
					// Inavlid key: too many parts
					return "§cMCDittyLand Version File Has Illegible Key: " + s;
				}

				// Check each version number for proper syntax
				for (String part : parts) {
					// Proxper syntax: One to four parts, separated by dots:
					// each
					// part can
					// be numbers or a *
					if (!CompareVersion.isVersionNumber(part)) {
						return "§cInvalid Number in Version File: " + s;
					}
				}

				// Finally, add key
				versionKeys.put(parts[0], parts[1]);
			}

			// Get the version for this version of Minecraft
			String foundVersion = versionKeys.get(mcVersion);

			if (foundVersion == null) {
				// No version of MCDitty given in file for this version of MC
				return "§cNo Version for MC " + mcVersion;
			}

			BlockSign.simpleLog("downloadCurrentVersion successfully returned "
					+ foundVersion);
			currentVersionBuffer = foundVersion;
			return foundVersion;
		} else {
			return currentVersionBuffer;
		}
	}

	public static String downloadExampleWorld(boolean quiet) {
		if (downloadingExampleWorld == true) {
			return "§bWait for the last download to finish before starting another.";
		}
		downloadingExampleWorld = true;
		BlockSign.simpleLog("downloadExampleWorld called");

		if (!quiet) {
			BlockSign.showTextAsLyricNow("§aDownloading MCDittyLand...");
			BlockSign.showTextAsLyricNow("§aChecking version...");
		}

		String newVersion = downloadExampleWorldVersion(MCDittyConfig.MC_CURRENT_VERSION);

		if (!quiet) {
			if (CompareVersion.isVersionNumber(newVersion)) {
				BlockSign.showTextAsLyricNow("§aDownloading version "
						+ newVersion + " for Minecraft "
						+ MCDittyConfig.MC_CURRENT_VERSION);
			} else {
				BlockSign
						.showTextAsLyricNow("§cError getting new version info: "
								+ newVersion);
			}

			BlockSign.showTextAsLyricNow("§aDownloading...");
		}

		// Create a url pointing at the current version download file on dropbox
		URL currentDownloadVersionsURL = null;
		try {
			// currentVersionURL = new
			// URL("http://mcditty.wikispaces.com/file/view/MCDitty_Current_Version.txt");
			String url;
			url = "http://dl.dropbox.com/s/ap6d98o7d51k1pi/MCDitty_Example_World_Download_Latest.txt";
			currentDownloadVersionsURL = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			// If the person (fenceFoil, hopefully) editing this has half a wit
			// about his person, this will never happen.
			return "§cMCDittyLand Version Download File URL Malformed";
		}

		// Open connection to download versions file, and download it.
		ArrayList<String> currentVersionLines = new ArrayList<String>();
		try {
			// Open connection
			BufferedReader currVersionIn = new BufferedReader(
					new InputStreamReader(
							currentDownloadVersionsURL.openStream()));
			// Read lines until the words "END Versions"
			while (true) {
				String lineIn = currVersionIn.readLine();
				if (lineIn == null) {
					break;
				} else if (lineIn.equalsIgnoreCase("end versions")) {
					// End of versions keys
					// Stop reading
					break;
				} else {
					currentVersionLines.add(lineIn);
				}
			}
			currVersionIn.close();
		} catch (IOException e) {
			e.printStackTrace();
			return "§cCouldn't Download 'New Version Download URLs' File";
		}

		// Find the latest version download URL for the current version of
		// Minecraft

		// Process the downloaded urls into a map
		HashMap<String, String> versionDownloadURLKeys = new HashMap<String, String>();
		for (String s : currentVersionLines) {
			String[] parts = s.split(":");
			if (parts.length <= 1) {
				// Inavlid key: too few parts
				return "§cVersion Download URL Table Has Illegible Key: " + s;
			}

			// Check each MC version number for proper syntax
			if (!CompareVersion.isVersionNumber(parts[0])) {
				return "§cInvalid Version Number in Version Download URL Table: "
						+ s;
			}

			// Check url validity (from the end of the version number and first
			// colon)
			String url = s.substring((parts[0].length() - 1) + 2);
			// Check that url is valid
			if (!url.matches("(http|https)://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?")) {
				return "§cInvalid Download URL: " + url;
			}

			// // URL Must be on dropbox
			// if (!url.toLowerCase().startsWith("http://dl.dropbox.com/")) {
			// return "§cDownload URL is not on DropBox";
			// }

			// Finally, add key
			versionDownloadURLKeys.put(parts[0], url);

			BlockSign.simpleLog("Version Download URL Key Read: " + parts[0]
					+ " : " + url);
		}

		// Get the download url for this version of Minecraft
		String foundVersionURL = versionDownloadURLKeys
				.get(MCDittyConfig.MC_CURRENT_VERSION);

		if (foundVersionURL == null) {
			// No version of MCDitty given in file for this version of MC
			return "§cNo Download Available for Minecraft Version "
					+ MCDittyConfig.MC_CURRENT_VERSION;
		}

		// Download new version of MCDitty!
		URL versionDownloadURL;
		try {
			versionDownloadURL = new URL(foundVersionURL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return "§cDownload URL for new version cannot be read: "
					+ foundVersionURL;
		}

		// Create new file to download to
		File downloadDir = new File(Minecraft.getMinecraft().getMinecraftDir()
				+ File.separator + "MCDitty/ExampleWorld/");
		if (!downloadDir.exists()) {
			downloadDir.mkdirs();
		}
		File newVersionFile = new File(downloadDir,
				foundVersionURL.substring(foundVersionURL.lastIndexOf("/") + 1));
		BlockSign
				.simpleLog("Saving new version as " + newVersionFile.getPath());
		try {
			newVersionFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			return "§cCould not create file: " + newVersionFile.getPath();
		}

		// Download file
		try {
			ReadableByteChannel downloadByteChannel = Channels
					.newChannel(versionDownloadURL.openStream());
			FileOutputStream newVersionZipFileOutputStream = new FileOutputStream(
					newVersionFile);
			// TODO: Show download progress
			// TODO: Allow more than 16 MB downloads
			newVersionZipFileOutputStream.getChannel().transferFrom(
					downloadByteChannel, 0, 1 << 24);
		} catch (IOException e) {
			e.printStackTrace();
			return "§cCould not download new version.";
		}

		if (!quiet) {
			BlockSign.showTextAsLyricNow("§aDownload successful!");

			BlockSign.showTextAsLyricNow("§aExtracting...");
		}
		// TOOD: Extract, etc.
		File saveWorldDir = new File(Minecraft.getMinecraft().getMinecraftDir()
				+ File.separator + "saves");

		MCDittyResourceManager.extractZipFiles(newVersionFile.getPath(),
				saveWorldDir.getPath());

		if (!quiet) {
			BlockSign.showTextAsLyricNow("§aMCDittyLand saved.");
		}

		// Finish up
		downloadingExampleWorld = false;

		// Note the version downloaded
		MCDittyConfig.lastTutorialVersionDownloaded = downloadExampleWorldVersion(MCDittyConfig.MC_CURRENT_VERSION);

		try {
			MCDittyConfig.flushAll();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "§bMCDittyLand ready! It is now a Singleplayer world in your /saves/ folder.";
	}

	/**
	 * 
	 * @return true if an updated version has been found since the last time the
	 *         tutorial world was downloaded
	 */
	public static boolean checkForUpdates() {
		String newVersion = downloadExampleWorldVersion(MCDittyConfig.MC_CURRENT_VERSION);

		if (newVersion == null) {
			return false;
		}

		// If there was an error, return early
		if (newVersion.startsWith("§c")) {
			return false;
		}

		BlockSign
				.simpleLog("TutorialWorldDownloader.checkForUpdates: downloaded version number "
						+ newVersion);
		if (CompareVersion.isVersionNumber(newVersion)) {
			if (CompareVersion.compareVersions(
					MCDittyConfig.lastTutorialVersionDownloaded, newVersion) == CompareVersion.LESSER) {
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

	public static void downloadExampleWorldButton() {
		final Minecraft mc = Minecraft.getMinecraft();
		mc.displayGuiScreen(null);

		// Decide whether to exit to main menu
		// Look for a sign that only exists in MCDittyLand
		boolean exitToMainMenu = false;
		MCDitty.optimizeCommentList(Minecraft.getMinecraft().theWorld);
		LinkedList<Comment> allComments = MCDitty
				.getCommentsSortedByDistFrom(new Point3D(0, 0, 0));
		for (Comment c : allComments) {
			if (c.getCommentText().equalsIgnoreCase("#I Am So Proud#")) {
				exitToMainMenu = true;
				break;
			}
		}

		if (exitToMainMenu) {
			// Prevents null pointer exceptions
			BlockSign.mutePlayingDitties();

			mc.statFileWriter.readStat(StatList.leaveGameStat, 1);
			mc.theWorld.sendQuittingDisconnectingPacket();
			mc.loadWorld((WorldClient) null);
		}

		final boolean beQuiet = exitToMainMenu;
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				// Be quiet if exiting to main menu
				BlockSign.writeChatMessage(mc.theWorld,
						TutorialWorldDownloader.downloadExampleWorld(beQuiet));
			}

		});
		t.setName("MCDittyLand Downloader");
		t.start();

		if (exitToMainMenu) {
			mc.displayGuiScreen(new GuiMainMenu());
		}
	}
}
