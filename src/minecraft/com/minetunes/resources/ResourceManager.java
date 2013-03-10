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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Loads and caches resources from minecraft.jar
 * 
 */
public class ResourceManager {

	private static HashMap<String, String> txtFileCache = new HashMap<String, String>();

	/**
	 * Attempts to load and return the indicated resource file as text. Caches
	 * the file after first load.
	 * 
	 * @param resourceName
	 * @return
	 */
	public static String loadCached(String resourceName) {
		// First try to load cached copy
		if (txtFileCache.get(resourceName) != null) {
			return txtFileCache.get(resourceName);
		}

		// Otherwise, cache and return
		// Read resource to a buffer
		InputStream helpTextStream = ResourceManager.class
				.getResourceAsStream(resourceName);
		if (helpTextStream == null) {
			System.out.println("MineTunesResourceManager: " + resourceName
					+ " not found.");
			return null;
		}

		StringBuilder txtBuffer = new StringBuilder();
		try {
			BufferedReader txtIn = new BufferedReader(new InputStreamReader(
					helpTextStream));
			while (true) {
				String lineIn = txtIn.readLine();
				if (lineIn == null) {
					break;
				} else {
					txtBuffer.append(lineIn);
					txtBuffer.append("\n");
				}
			}
			txtIn.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Cache and return
		String readTxt = txtBuffer.toString();
		txtFileCache.put(resourceName, readTxt);
		return readTxt;
	}

	public static InputStream getResource(String resourceName) {
		return ResourceManager.class.getResourceAsStream(resourceName);
	}

	/**
	 * Copies the files in a zip file to the given directory in the filesystem.
	 * 
	 * @param zipFileName
	 * @param destName
	 */
	public static void extractZipFiles(String zipFileName, String destName) {
		try {
			byte[] buf = new byte[1024];
			ZipInputStream zipInputStream = null;
			ZipEntry currZipEntry;
			zipInputStream = new ZipInputStream(
					new FileInputStream(zipFileName));
			currZipEntry = zipInputStream.getNextEntry();
	
			while (currZipEntry != null) {
				// for each entry to be extracted
				String entryName = currZipEntry.getName();
				System.out.println("Extracting MineTunes ZIP Entry: "
						+ entryName);
	
				File newFile = new File(destName + File.separator + entryName);
				if (currZipEntry.isDirectory()) {
					newFile.mkdirs();
				} else {
					newFile.getParentFile().mkdirs();
					FileOutputStream fileOutputStream = new FileOutputStream(
							newFile);
					int n;
					while ((n = zipInputStream.read(buf, 0, 1024)) > -1)
						fileOutputStream.write(buf, 0, n);
	
					fileOutputStream.close();
				}
	
				zipInputStream.closeEntry();
				currZipEntry = zipInputStream.getNextEntry();
			}
	
			zipInputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates parent dirs as necessary if possible
	 * 
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException
	 */
	public static void copyFile(File sourceFile, File destFile)
			throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		// Make any parent directories as necessary
		destFile.getParentFile().mkdirs();

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

}
