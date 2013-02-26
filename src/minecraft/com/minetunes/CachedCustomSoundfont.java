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
package com.minetunes;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.Instrument;

import com.sun.media.sound.SF2Soundbank;

/**
 * Info about the custom soundfont, if any, loaded by MineTunes at present.
 */
public class CachedCustomSoundfont {
	private String filename = "";
	private SF2Soundbank cachedSoundbank = null;
	private boolean isGoodBank = false;
	private boolean isSF2Loaded = false;

	public CachedCustomSoundfont() {

	}

	public boolean loadAndCacheSF2(String file) {
		if (!file.equals(filename)) {
			SF2Soundbank soundbank;
			try {
				soundbank = new SF2Soundbank(new File(file));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				// no soundbank. Unload and return false;
				unload();
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				unload();
				return false;
			}
			// System.out.println(soundbank.getName() + ":"
			// + soundbank.getInstruments()[14].getName());
			// System.out.println(synth.isSoundbankSupported(soundbank));
			// Instrument[] instruments = soundbank.getInstruments();

			// for (Instrument inst : instruments) {
			// System.out.println("Bank=" + inst.getPatch().getBank()
			// + " Patch=" + inst.getPatch().getProgram() + " Inst="
			// + inst);
			// }

			filename = file;
			cachedSoundbank = soundbank;
			isGoodBank = true;
			isSF2Loaded = true;
		}
		return true;
	}

	public String toConfigString() {
		if (isSF2Loaded) {
			return filename;
		} else {
			return "null";
		}
	}

	public void loadFromConfigString(String configString) throws IOException {
		// Handle null string
		if (configString == null) {
			unload();
			return;
		}
		
		// Check if there is really a change
		if (configString.equals(filename)) {
			// No change
		} else if (configString.equals("null")) {
			// Unload
			unload();
		} else {
			// Change loaded sf2
			// System.out.println ("loading soundbanks");
			loadAndCacheSF2(configString);
		}
	}

	public void unload() {
		isGoodBank = false;
		isSF2Loaded = false;
		filename = "";
		cachedSoundbank = null;
	}

	public String getFilename() {
		return filename;
	}

	public SF2Soundbank getCachedSoundbank() {
		return cachedSoundbank;
	}

	public boolean isGoodBank() {
		return isGoodBank;
	}

	public boolean isSF2Loaded() {
		return isSF2Loaded;
	}
}
