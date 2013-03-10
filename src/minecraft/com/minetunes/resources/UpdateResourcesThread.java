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
package com.minetunes.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.minetunes.Minetunes;
import com.minetunes.autoUpdate.CompareVersion;
import com.minetunes.autoUpdate.FileUpdater;
import com.minetunes.config.MinetunesConfig;

/**
 * Checks the versions of and downloads the latest ZIP of resources for this
 * version of MineTunes. Call whenever MineTunes is updated. Also, call while
 * MineTunes loads if the property resources.missing is true: this property
 * indicates that resources failed to download last time.
 */
public class UpdateResourcesThread extends Thread {

	public UpdateResourcesThread() {

	}

	@Override
	public synchronized void run() {
		System.out.println("Updating MineTunes Resources");

		// Download the properties for resources
		Properties prop = FileUpdater
				.downloadProperties("http://dl.dropbox.com/s/ag91p4f653q4asl/ResourcesVersions.txt");
		if (prop == null) {
			System.err
					.println("MineTunes: Unable to download ResourcesVersions.txt");
			fail();
			return;
		}

		// Look for the latest resources URL that fits this version
		int resourcesVersion = 0;
		boolean resourceVersionFound = false;
		while (true) {
			if (prop.containsKey(resourcesVersion + ".minVer")) {
				// A version has been found; note this
				resourceVersionFound = true;

				String minVer = prop.getProperty(resourcesVersion + ".minVer");
				int verCompare = CompareVersion.compareVersions(minVer,
						MinetunesConfig.CURRENT_VERSION);
				if (verCompare == CompareVersion.GREATER) {
					// Last compatible version of resources found
					// Go back one, and stop looking.
					resourcesVersion--;
					break;
				} else {
					// Not last compatible version yet. Keep looking
					resourcesVersion++;
				}
			} else {
				// No more versions; latest found for sure
				// Since this one did not exist, backtrack 1
				resourcesVersion--;
				break;
			}
		}

		if (resourcesVersion < 0) {
			// Illegal version num
			System.err.println("MineTunes cannot download resources zip version "
					+ resourcesVersion);
			fail();
			return;
		}

		if (!resourceVersionFound) {
			// Failed to find a latest version
			System.err
					.println("MineTunes's resources index has no version entries.");
			fail();
			return;
		}

		// Check to make sure this is not a duplicate download
		if (MinetunesConfig.getInt("resources.lastZipDownloaded") == resourcesVersion) {
			//System.out.println ("MineTunes: Resources already up-to-date.");
			return;
		}

		// Download resources
		String downloadURL = prop.getProperty(resourcesVersion + ".url");
		if (downloadURL == null) {
			System.err.println("MineTunes could not find key " + resourcesVersion
					+ ".url in the resource index.");
			fail();
			return;
		}
		
		File resourcesDir = MinetunesConfig.getResourcesDir();
		
		// Clear existing resources
		try {
			deleteRecursively(resourcesDir);
		} catch (IOException e) {
			e.printStackTrace();
			// not a big enough problem to fail() about.
		}

		if (!resourcesDir.exists()) {
			resourcesDir.mkdirs();
		}
		File zipFile = FileUpdater.downloadFile(downloadURL,
				resourcesDir.getPath() + File.separator + "resourcesVer"
						+ resourcesVersion + ".zip");
		if (zipFile == null) {
			// Did not download
			System.err.println("MineTunes: Unable to download resources version "
					+ resourcesVersion);
			fail();
			return;
		}

		// Extract resources
		ResourceManager.extractZipFiles(zipFile.getPath(),
				resourcesDir.getPath());

		// Delete zip file
		zipFile.delete();

		// Re-index any sound resources
		Minetunes.registerSoundResources();

		// Note success
		MinetunesConfig.setBoolean("resources.missing", false);
		MinetunesConfig.setInt("resources.lastZipDownloaded", resourcesVersion);
		try {
			MinetunesConfig.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void deleteRecursively(File f) throws IOException {
		if (f.isDirectory()) {
			for (File c : f.listFiles()) {
				deleteRecursively(c);
			}
		}
		// if (!f.delete()) {
		// throw new FileNotFoundException("Failed to delete file: " + f);
		// }
	}

	/**
	 * Notes and handles a failure to download MineTunes's resources
	 */
	private void fail() {
		System.err
				.println("MineTunes could not update its resources from the Internet. Will try again later.");

		// Note the failure
		MinetunesConfig.setBoolean("resources.missing", true);
		try {
			MinetunesConfig.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
