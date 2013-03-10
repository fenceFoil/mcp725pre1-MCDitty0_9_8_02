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
package com.minetunes.autoUpdate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.client.Minecraft;

/**
 * Downloads new versions of your mod, and patches them into minecraft.jar<br>
 * <br>
 * This class should not be used for updating Forge mods installed in a \mods\ folder.
 * 
 */
public class ModUpdater extends FileUpdater {
	private String[] modPackages = {};

	private String specialFileTitle = "";

	/**
	 * AutoUpdate has been run once or more already.
	 */
	public static boolean alreadyRun = false;

	/**
	 * 
	 * @param versionInfo
	 * @param modPackages
	 *            an array of packages to remove from minecraft.jar when
	 *            updating. For example, {"com/minetunes/", "de/jarnjbo/",
	 *            "org/jfugue/"}
	 */
	public ModUpdater(String versionInfo, String[] modPackages) {
		super(versionInfo, "mod");
		this.modPackages = modPackages;
	}

	/**
	 * Restart the current Java application. Adapted From
	 * http://java.dzone.com/articles/programmatically-restart-java
	 * 
	 * @param runBeforeRestart
	 *            some custom code to be run before restarting
	 * @throws IOException
	 */
	private static void initJarSwapperToRun(File jarToRun) throws IOException {
		try {
			// java binary
			String java = System.getProperty("java.home") + "/bin/java";
			// init the command to execute, add the vm args
			final StringBuffer cmd = new StringBuffer("\"" + java + "\" ");

			// program main and program arguments
			String mainCommand = jarToRun.getPath();
			// program main is a jar
			if (mainCommand.endsWith(".jar")) {
				// if it's a jar, add -jar mainJar
				cmd.append("-jar " + "\"" + new File(mainCommand).getPath()
						+ "\"");
			} else {
				// else it's a .class, add the classpath and mainClass
				cmd.append("-cp \"" + System.getProperty("java.class.path")
						+ "\" " + mainCommand);
			}

			// Add argument containing the location of this minecraft.jar
			cmd.append(" " + Minecraft.getMinecraftDir() + File.separator
					+ "bin" + File.separator);

			// execute the command in a shutdown hook, to be sure that all the
			// resources have been disposed before running the new file
			Runtime.getRuntime().addShutdownHook(
					new RunCommandThread(cmd.toString()));

			System.out.println("Going to run on shutdown: " + cmd.toString());
		} catch (Exception e) {
			// something went wrong
			throw new IOException(
					"Error while trying to restart the application", e);
		}
	}

	private LinkedList<JarEntry> loadJarEntries(File jarFile) {
		LinkedList<JarEntry> entries = new LinkedList<JarEntry>();
		try {
			// Set up to read minecraft.jar
			JarFile minecraftJarFile = new JarFile(jarFile);
			JarInputStream minecraftJarInputStream = new JarInputStream(
					new FileInputStream(jarFile));
			// Read in a list of the entries in minecraft.jar
			while (true) {
				JarEntry entry = minecraftJarInputStream.getNextJarEntry();
				if (entry == null) {
					break;
				}
				entries.add(entry);
			}
			minecraftJarInputStream.close();
			minecraftJarFile.close();
		} catch (IOException e1) {
			e1.printStackTrace();

			fireFileUpdaterEvent(UpdateEventLevel.ERROR, "Mixing",
					"Could not read jar");
			return null;
		}

		return entries;
	}

	/**
	 * AutoUpdates your mod. Note that this method outputs messages to the chat,
	 * and should be run in its own thread to avoid blocking Minecraft for a
	 * very long time. (Upwards of 30 seconds assuming broadband internet and
	 * small, ~1MB mod download + a moderately sized (in terms of file count)
	 * minecraft.jar)
	 * 
	 * @param messagePrefix
	 * @return
	 */
	public boolean autoUpdate(String mcVer, File zipDir, File jarSwapperDir,
			InputStream jarSwapperInputStream) {
		if (alreadyRun) {
			fireFileUpdaterEvent(UpdateEventLevel.WARN, "",
					"Can't auto-update twice! Restart Minecraft before trying again.");
			return false;
		}

		alreadyRun = true;

		fireFileUpdaterEvent(UpdateEventLevel.INFO, "",
				"Auto-Updating to latest version.");
		fireFileUpdaterEvent(UpdateEventLevel.INFO, "Checking",
				"Checking for new version");

		String newVersion = getLatestVersion(mcVer);
		if (newVersion == null) {
			newVersion = "";
		}
		if (CompareVersion.isVersionNumber(newVersion)) {
			fireFileUpdaterEvent(UpdateEventLevel.INFO, "Checking",
					"Updating to version " + newVersion + " for Minecraft "
							+ mcVer);
		} else {
			fireFileUpdaterEvent(UpdateEventLevel.WARN, "Checking",
					"Cannot get the newest version number. Found: "
							+ newVersion);
		}

		fireFileUpdaterEvent(UpdateEventLevel.INFO, "Downloading",
				"Downloading...");

		// Get the download url for this version of Minecraft
		String foundVersionURL = getLatestURL(mcVer);
		if (foundVersionURL == null) {
			// No version of MineTunes given in file for this version of MC
			fireFileUpdaterEvent(UpdateEventLevel.ERROR, "Downloading",
					"No versions available for Minecraft " + mcVer);
			return false;
		}

		// Download new version of MineTunes!

		// Create folder to download into
		File downloadDir = zipDir;
		if (!downloadDir.exists()) {
			downloadDir.mkdirs();
		}

		// Create new file to download into. Name after the file in the last
		// part of the URL.
		File modZipFile = new File(downloadDir,
				foundVersionURL.substring(foundVersionURL.lastIndexOf("/") + 1));
		fireFileUpdaterEvent(UpdateEventLevel.DEBUG, "Downloading",
				"Saving as " + modZipFile.getPath());

		// Do the download
		FileUpdater.downloadFile(foundVersionURL, modZipFile.getPath());
		fireFileUpdaterEvent(UpdateEventLevel.INFO, "Downloading",
				"Downloaded to " + modZipFile.getPath());

		// Load the contents of the minecraft jar
		fireFileUpdaterEvent(UpdateEventLevel.INFO, "Mixing",
				"Loading minecraft.jar...");
		File minecraftFile = getMinecraftJarFile();
		LinkedList<JarEntry> minecraftJarEntries = loadJarEntries(minecraftFile);

		// Load the contents of the mod zip
		fireFileUpdaterEvent(UpdateEventLevel.INFO, "Mixing",
				"Loading new mod files...");
		LinkedList<ZipEntry> modZipEntries = loadZipEntries(modZipFile);

		// Decide what files to put in updated minecraft.jar
		LinkedList<ZipEntry> updatedMinecraftEntries = new LinkedList<ZipEntry>();

		// Start with every file in the old minecraft jar
		updatedMinecraftEntries.addAll(minecraftJarEntries);

		// Attempt to clean MCDitty files from the minecraft jar
		// Remove most mod files; basically anything in a package like
		// com.minetunes or org.jfugue (or de.jarnbjo).
		// (Leave all other files; if some are MineTunes they will be
		// overwritten hopefully)
		// Vanilla Minecraft classes are left, even if they are modded.
		for (int i = 0; i < updatedMinecraftEntries.size(); i++) {
			JarEntry e = (JarEntry) updatedMinecraftEntries.get(i);
			for (String s : modPackages) {
				if (e.getName().startsWith(s)) {
					fireFileUpdaterEvent(UpdateEventLevel.DEBUG, "Mixing",
							"Stripping minecraft.jar entry: " + e.getName());
					updatedMinecraftEntries.remove(i);
					i--;
				}
			}
		}

		// Add new files from zip, replacing any entries from minecraft.jar
		for (int i = 0; i < modZipEntries.size(); i++) {
			ZipEntry z = modZipEntries.get(i);

			// if (z.getName().toLowerCase().contains("mcdittysrc.zip")
			// || z.getName().toLowerCase().contains("readme.txt")
			// || z.getName().toLowerCase().contains("lgpl.txt")
			// || z.getName().toLowerCase().contains("license.txt")) {
			// BlockSign.simpleLog("NOT ADDING FILE FROM NEW ZIP: "
			// + z.getName());
			// continue;
			// }

			// Remove any files with the same name as this from the updated
			// minecraft files
			for (int f = 0; f < updatedMinecraftEntries.size(); f++) {
				if (updatedMinecraftEntries.get(f).getName()
						.equals(z.getName())) {
					fireFileUpdaterEvent(UpdateEventLevel.DEBUG, "Mixing",
							"Overwriting jar entry: " + z.getName());
					updatedMinecraftEntries.remove(f);
					break;
				}
			}

			// Add zip file to updated minecraft's files
			updatedMinecraftEntries.add(z);
		}

		// Note: this code relies on the files from the old minecraft.jar being
		// "jarEntry"s, and files form the zip being "ZipEntry"s to
		// differentiate the two sources of files.

		// Write the files into a new minecraft jar file
		fireFileUpdaterEvent(UpdateEventLevel.INFO, "Mixing",
				"Repacking new minecraft.jar...");

		try {
			File newMinecraftFile = new File(Minecraft.getMinecraft()
					.getMinecraftDir()
					+ File.separator
					+ "bin"
					+ File.separator + "minecraft.updatedMCDitty.jar");
			if (newMinecraftFile.exists()) {
				newMinecraftFile.delete();
			}
			newMinecraftFile.createNewFile();
			// JarFile newMinecraftJar = new JarFile(newMinecraftFile);
			JarFile minecraftJarFile = new JarFile(minecraftFile);
			ZipFile newVersionZipFile = new ZipFile(modZipFile);
			JarOutputStream newMinecraftJarOut = new JarOutputStream(
					new FileOutputStream(newMinecraftFile));
			for (int i = 0; i < updatedMinecraftEntries.size(); i++) {
				ZipEntry e = updatedMinecraftEntries.get(i);

				newMinecraftJarOut.putNextEntry(new JarEntry(e.getName()));
				InputStream entryDataIn;
				if (e instanceof JarEntry) {
					// Read from minecraft.jar
					entryDataIn = minecraftJarFile.getInputStream(e);
				} else {
					// Instance of zipentry
					// Read from new version's zip
					entryDataIn = newVersionZipFile.getInputStream(e);
				}
				byte[] buffer = new byte[4096];
				int bytesRead = 0;
				while ((bytesRead = entryDataIn.read(buffer)) != -1) {
					newMinecraftJarOut.write(buffer, 0, bytesRead);
				}
				entryDataIn.close();
				newMinecraftJarOut.flush();
				newMinecraftJarOut.closeEntry();

				// BlockSign.simpleLog("Wrote to new jar from "
				// + ((e instanceof JarEntry) ? "JAR" : "ZIP") + ": "
				// + e.getName());

				if (i % (updatedMinecraftEntries.size() / 10) == 1) {
					int percent = (int) (((double) i / (double) updatedMinecraftEntries
							.size()) * 100d);
					fireFileUpdaterEvent(UpdateEventLevel.INFO, "Mixing",
							"Writing: " + percent + "%%");
				}
			}
			newMinecraftJarOut.flush();
			newMinecraftJarOut.finish();
			newMinecraftJarOut.close();
			minecraftJarFile.close();
			newVersionZipFile.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			fireFileUpdaterEvent(UpdateEventLevel.ERROR, "Mixing",
					"Could not find the updated minecraft.jar file.");
			return false;
		} catch (IOException e1) {
			e1.printStackTrace();
			fireFileUpdaterEvent(UpdateEventLevel.ERROR, "Mixing",
					"Could not write the updated minecraft.jar");
			return false;
		}

		fireFileUpdaterEvent(UpdateEventLevel.INFO, "Mixing",
				"Finished new minecraft.jar");

		// Extract renaming script to file
		fireFileUpdaterEvent(UpdateEventLevel.INFO, "Swapper",
				"Extracting jar swapper...");
		File jarSwapperFile = new File(jarSwapperDir + File.separator
				+ "jarSwapper.jar");
		try {
			ReadableByteChannel jarSwapperChannel = Channels
					.newChannel(jarSwapperInputStream);
			FileOutputStream jarSwapperFileOutputStream = new FileOutputStream(
					jarSwapperFile);
			// Do the transfer
			jarSwapperFileOutputStream.getChannel().transferFrom(
					jarSwapperChannel, 0, Long.MAX_VALUE);
			jarSwapperFileOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			fireFileUpdaterEvent(
					UpdateEventLevel.ERROR,
					"Swapper",
					"Could not extract jar swapper. You can still swap the jars yourself in .minecraft/bin/");
			return false;
		} catch (NullPointerException e) {
			e.printStackTrace();
			fireFileUpdaterEvent(
					UpdateEventLevel.ERROR,
					"Swapper",
					"Could not extract jar swapper. You can still swap the jars yourself in .minecraft/bin/");
			return false;
		}

		fireFileUpdaterEvent(UpdateEventLevel.INFO, "Swapper",
				"Jar swapper extracted.");
		try {
			initJarSwapperToRun(jarSwapperFile);
		} catch (IOException e) {
			e.printStackTrace();
			fireFileUpdaterEvent(
					UpdateEventLevel.ERROR,
					"Swapper",
					"Could not start jar swapper. You can still swap the jars yourself in .minecraft/bin/");
			return false;
		}

		fireFileUpdaterEvent(UpdateEventLevel.INFO, "", "Update successful.");
		return true;
	}

	private File getMinecraftJarFile() {
		return new File(Minecraft.getMinecraft().getMinecraftDir()
				+ File.separator + "bin" + File.separator + "minecraft.jar");
	}

	/**
	 * Uses a non-normal source for update checking. Instead of looking under
	 * "mod.latest.mc.1.4.6", you can look under "mod.dev.latest.mc.1.4.6" to
	 * get snapshots, etc.
	 * 
	 * @param mode
	 */
	public void setSpecialMode(String mode) {
		clearCache();
		clearStaticCache();
		fileTitle = "mod." + mode;
		specialFileTitle = mode;
	}

	/**
	 * Lets auto-update run again.
	 */
	public void clearAlreadyTriedFlag() {
		alreadyRun = false;
	}
}
