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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Loads and caches resources from minecraft.jar
 * 
 */
public class MCDittyResourceManager {

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
		InputStream helpTextStream = MCDittyResourceManager.class
				.getResourceAsStream(resourceName);
		if (helpTextStream == null) {
			System.out.println("MCDittyResourceManager: " + resourceName
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
		return MCDittyResourceManager.class.getResourceAsStream(resourceName);
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
				System.out.println("Extracting MCDitty ZIP Entry: "
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

	/**
	 * @param url
	 * @return null if failed
	 */
	public static Properties downloadProperties(String url) {
		InputStream propsIn = null;
		try {
			URL propsURL = new URL(url);
			propsIn = propsURL.openStream();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// Could not open props file
			e.printStackTrace();
			return null;
		}
	
		// Read a properties file from the internet stream
		Properties prop = new Properties();
		try {
			prop.load(propsIn);
			propsIn.close();
		} catch (InvalidPropertiesFormatException e) {
			e.printStackTrace();
			try {
				propsIn.close();
			} catch (IOException e1) {
			}
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			try {
				propsIn.close();
			} catch (IOException e1) {
			}
			return null;
		}
		return prop;
	}

}
