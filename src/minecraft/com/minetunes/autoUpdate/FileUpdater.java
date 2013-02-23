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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.minetunes.resources.ResourceManager;

/**
 *
 */
public class FileUpdater {

	private HashSet<FileUpdaterListener> listeners = new HashSet<FileUpdaterListener>();
	private String versionInfoURL;
	private Properties versionInfos = null;
	private String fileTitle = "";

	/**
	 * If another instance of FileUpdater is created with the same URL, it uses
	 * a cached Properties from this map instead of downloading it again
	 */
	private HashMap<String, Properties> cachedProperties = new HashMap<String, Properties>();

	/**
	 * @param versionInfo
	 */
	public FileUpdater(String versionInfoURL, String fileTitle) {
		this.versionInfoURL = versionInfoURL;
		this.fileTitle = fileTitle;
	}

	/**
	 * Returns either versionInfos, or if it is null this downloads versionInfos
	 * from the internet
	 * 
	 * @return
	 */
	protected Properties getVersionInfos() {
		if (versionInfos != null) {
			// If version info already found, return it
			return versionInfos;
		} else {
			// If version info need to be found, find it wherever it may be

			// Try to get a cached version first
			Properties cached = cachedProperties.get(versionInfoURL);
			if (cached == null) {
				// No cached version yet. Download the properties, and add to
				// the cache
				versionInfos = ResourceManager
						.downloadProperties(versionInfoURL);
				cachedProperties.put(versionInfoURL, versionInfos);
			} else {
				// Use the cached version
				versionInfos = cached;
			}

			// Wherever it came from, return the properties
			return versionInfos;
		}
	}

	public LinkedList<ZipEntry> loadZipEntries(File zipFile) {
		LinkedList<ZipEntry> newVersionZipEntries = new LinkedList<ZipEntry>();
		try {
			// Set up to read new version zip
			ZipFile newVersionZipFile = new ZipFile(zipFile);
			ZipInputStream newVersionZipInputStream = new ZipInputStream(
					new FileInputStream(zipFile));
			// Read in a list of entries in the new version's zip file
			while (true) {
				ZipEntry entry = newVersionZipInputStream.getNextEntry();
				if (entry == null) {
					break;
				}
				newVersionZipEntries.add(entry);
			}
			newVersionZipInputStream.close();
			newVersionZipFile.close();
		} catch (ZipException e1) {
			e1.printStackTrace();
			fireFileUpdaterEvent(UpdateEventLevel.ERROR, "Mixing",
					"Could not read zip file. (ZipException)");
			return null;
		} catch (IOException e1) {
			e1.printStackTrace();
			fireFileUpdaterEvent(UpdateEventLevel.ERROR, "Mixing",
					"Could not read zip file. (IOException)");
			return null;
		}
		return newVersionZipEntries;
	}

	public void downloadToFile(File destFile, String mcVersion) {
		fireFileUpdaterEvent(UpdateEventLevel.INFO, "Download", "Downloading "
				+ destFile.getName());
		String latestURL = getLatestURL(mcVersion);
		if (latestURL != null) {
			FileUpdater.downloadFile(latestURL, destFile.getPath());
			fireFileUpdaterEvent(UpdateEventLevel.INFO, "Download",
					"Downloaded successfully.");
		} else {
			fireFileUpdaterEvent(UpdateEventLevel.ERROR, "Download",
					"No URL available for file.");
		}
	}

	/**
	 * 
	 * @return If successful, the version from the file. If not, it will return
	 *         a string that starts with "§c"
	 */
	public String getLatestVersion(String mcVersion) {
		// Get the mod version for this version of Minecraft
		String foundVersion = getVersionInfos().getProperty(
				fileTitle + ".latest.mc." + mcVersion);

		if (foundVersion == null) {
			// No version given for this version of MC
			return null;
		}

		return foundVersion;
	}

	public String getLatestURL(String mcVersion) {
		return getVersionInfos().getProperty(
				fileTitle + ".download.mc." + mcVersion);
	}

	public void addFileUpdaterListener(FileUpdaterListener l) {
		listeners.add(l);
	}

	public void removeFileUpdaterListener(FileUpdaterListener l) {
		listeners.remove(l);
	}

	protected void fireFileUpdaterEvent(UpdateEventLevel level, String stage,
			String event) {
		for (FileUpdaterListener l : listeners) {
			l.onUpdaterEvent(level, stage, event);
		}
	}

	public static File downloadFile(String url, String destFilename) {
		// Download file
		try {
			URL versionDownloadURL = new URL(url);
			File destFile = new File(destFilename);
			ReadableByteChannel downloadByteChannel = Channels
					.newChannel(versionDownloadURL.openStream());
			FileOutputStream newVersionZipFileOutputStream = new FileOutputStream(
					destFile);
			newVersionZipFileOutputStream.getChannel().transferFrom(
					downloadByteChannel, 0, Long.MAX_VALUE);
			downloadByteChannel.close();
			newVersionZipFileOutputStream.close();
			return destFile;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
