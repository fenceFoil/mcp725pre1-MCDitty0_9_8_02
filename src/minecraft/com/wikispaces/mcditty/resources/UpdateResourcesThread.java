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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.wikispaces.mcditty.CompareVersion;
import com.wikispaces.mcditty.MCDitty;
import com.wikispaces.mcditty.config.MCDittyConfig;

/**
 * Checks the versions of and downloads the latest ZIP of resources for this
 * version of MCDitty. Call whenever MCDitty is updated. Also, call while
 * MCDitty loads if the property resources.missing is true: this property
 * indicates that resources failed to download last time.
 */
public class UpdateResourcesThread extends Thread {

	public UpdateResourcesThread() {

	}

	@Override
	public synchronized void run() {
		System.out.println("Updating MCDitty Resources");

		// Download the properties for resources
		Properties prop = MCDittyResourceManager
				.downloadProperties("http://dl.dropbox.com/s/vvlvz8pg8c5s5bz/MCDitty_Resources.txt");
		if (prop == null) {
			System.err
					.println("MCDitty: Unable to download MCDitty_Resources.txt");
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
						MCDittyConfig.CURRENT_VERSION);
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
			System.err.println("MCDitty cannot download resources zip version "
					+ resourcesVersion);
			fail();
			return;
		}

		if (!resourceVersionFound) {
			// Failed to find a latest version
			System.err
					.println("MCDitty's resources index has no version entries.");
			fail();
			return;
		}

		// Check to make sure this is not a duplicate download
		if (MCDittyConfig.getInt("resources.lastZipDownloaded") == resourcesVersion) {
			//System.out.println ("MCDitty: Resources already up-to-date.");
			return;
		}

		// Download resources
		String downloadURL = prop.getProperty(resourcesVersion + ".url");
		if (downloadURL == null) {
			System.err.println("MCDitty could not find key " + resourcesVersion
					+ ".url in the resource index.");
			fail();
			return;
		}
		

		File resourcesDir = MCDittyConfig.resourcesDir;
		
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
		File zipFile = MCDittyResourceManager.downloadFile(downloadURL,
				resourcesDir.getPath() + File.separator + "resourcesVer"
						+ resourcesVersion + ".zip");
		if (zipFile == null) {
			// Did not download
			System.err.println("MCDitty: Unable to download resources version "
					+ resourcesVersion);
			fail();
			return;
		}

		// Extract resources
		MCDittyResourceManager.extractZipFiles(zipFile.getPath(),
				resourcesDir.getPath());

		// Delete zip file
		zipFile.delete();

		// Re-index any sound resources
		MCDitty.registerSoundResources();

		// Note success
		MCDittyConfig.setBoolean("resources.missing", false);
		MCDittyConfig.setInt("resources.lastZipDownloaded", resourcesVersion);
		MCDittyConfig.flushPropertiesXML();
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
	 * Notes and handles a failure to download MCDitty's resources
	 */
	private void fail() {
		System.err
				.println("MCDitty could not update its resources from the Internet. Will try again later.");

		// Note the failure
		MCDittyConfig.setBoolean("resources.missing", true);
		MCDittyConfig.flushPropertiesXML();
	}

}
