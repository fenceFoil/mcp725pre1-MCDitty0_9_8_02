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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

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
	public static String loadCachedResource(String resourceName) {
		// First try to load cached copy
		if (txtFileCache.get(resourceName) != null) {
			return txtFileCache.get(resourceName);
		}

		// Otherwise, cache and return
		// Read resource to a buffer
		InputStream helpTextStream = MCDittyResourceManager.class.getResourceAsStream(
				resourceName);
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

}
