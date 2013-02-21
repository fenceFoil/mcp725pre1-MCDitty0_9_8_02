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
package com.minetunes.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author William
 * 
 */
public class NoPlayTokens {

	/**
	 * Note: Strips newlines, leaving spaces
	 * 
	 * @return the no play tokens from file or an empty string
	 */
	public static void reloadNoPlayTokens() {
		// TODO
		try {
			StringBuilder b = new StringBuilder();
			BufferedReader in = new BufferedReader(new FileReader(
					noPlayTokensFile));
			while (true) {
				String inLine = in.readLine();
				if (inLine == null) {
					break;
				} else {
					b.append(inLine);
					b.append(" ");
				}
			}
			in.close();
			setNoPlayTokensString(b.toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// No file; attempt to create it
			// e.printStackTrace();
			saveNoPlayTokens();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}

	public static boolean saveNoPlayTokens() {
		try {
			noPlayTokensFile.getParentFile().mkdir();
			noPlayTokensFile.createNewFile();
			BufferedWriter out = new BufferedWriter(new FileWriter(
					noPlayTokensFile));
			for (int i = 0; i < getNoPlayTokens().length; i++) {
				String token = getNoPlayTokens()[i];
				out.write(token);
				if (i < getNoPlayTokens().length - 1) {
					out.newLine();
				}
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean editNoPlayTokens() {
		if (!noPlayTokensFile.exists()) {
			try {
				noPlayTokensFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		NoPlayDittyTokensEditor f = new NoPlayDittyTokensEditor();
		f.setSize(500, 400);
		f.setVisible(true);
		f.show();
		f.openFile(noPlayTokensFile);

		return true;
	}

	public static String getNoPlayTokensString() {
		return noPlayTokens;
	}

	public static void setNoPlayTokensString(String s) {
		noPlayTokens = s;
	}

	public static String[] getNoPlayTokens() {
		return noPlayTokens.split(" ");
	}

	/**
	 * Tokens, separated by spaces, that immediately signal a song not to be
	 * played. Example: "[Buy] [Sell] [Donate]" Saves one from activating shop
	 * signs.
	 */
	// Defaults are from the SignShop bukkit plugin
	public static String noPlayTokens = "[Buy] [Sell] [Share] [Donate] [Donatehand] "
			+ "[Dispose] [Slot] [DeviceOn] [DeviceOff] [Toggle] [Device] [DeviceItem] "
			+ "[gBuy] [gSell] [iBuy] [iSell] [iTrade] [Class] [iBuyXP] [iSellXP] [iSlot] "
			+ "[Day] [Night] [Rain] [ClearSkies] [Repair] [Heal] [Enchant] [Disenchant] "
			+ "[TpToOwner] [Command]";

	public static File noPlayTokensFile = new File(MinetunesConfig
			.getMinetunesDir().getPath() + File.separator + "noPlayTokens.txt");

}
