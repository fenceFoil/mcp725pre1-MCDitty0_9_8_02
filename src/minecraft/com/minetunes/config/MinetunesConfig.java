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
package com.minetunes.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import net.minecraft.client.Minecraft;

import com.minetunes.CachedCustomSoundfont;
import com.minetunes.Color4f;
import com.minetunes.Minetunes;
import com.minetunes.autoUpdate.CompareVersion;
import com.minetunes.config.legacy.MCDittyConfigConverter;
import com.minetunes.signs.TileEntitySignRendererMinetunes;

/**
 * Represents the values stored in the MineTunes config file and other
 * information.
 * 
 */
public class MinetunesConfig {

	/**
	 * Current MineTunes version.
	 */
	public static final String CURRENT_VERSION = "0.9.12.00";
	/**
	 * Minecraft version that the mod is designed for.
	 */
	public static final String MC_CURRENT_VERSION = "1.4.6";
	private static final String[] UPDATE_MESSAGE = { "§aNew:§r Book keyword." };
	/**
	 * Disable to stop costly printlns from being called
	 */
	public static boolean DEBUG = false;

	/**
	 * Used when Auto-Updating. A rough list of packages to remove when
	 * stripping out this version of Minetunes.
	 */
	public static final String[] mineTunesModPackages = { "org/jfugue",
			"com/minetunes", "aurelienribon/tweenengine", "de/jarnbjo" };

	/**
	 * Whether the mod attempts to emit any particles (cached for speed from
	 * Properties)
	 */
	public static boolean particlesEnabled = true;

	/**
	 * From config file: if true, only the first sign emits a particle (cached
	 * from Properties)
	 */
	public static boolean noteParticlesDisabled = false;

	/**
	 * Rendering: Full Minecraft-style rendering enabled
	 */
	private static boolean fullSignRenderingEnabled = false;

	/**
	 * The custom soundfont selected by the player
	 */
	public static CachedCustomSoundfont customSF2 = new CachedCustomSoundfont();

	/**
	 * Highlight keywords, comments, ect?
	 */
	public static boolean highlightEnabled = false;

	/**
	 * Whether config file is loaded yet
	 */
	private static boolean configLoaded = false;

	private static Properties defaultProperties = new Properties();
	static {
		// SET UP DEFAULT PROPERTIES, if normal defaults for primitive type are
		// unsuitable
		defaultProperties.setProperty("enableNoteblockTooltips", "true");
		defaultProperties.setProperty("particles.enabled", "true");
		defaultProperties.setProperty("signs.errorBlinkMS", "6000");
		defaultProperties.setProperty("signs.errorBlinkRed", "true");
		defaultProperties.setProperty("signs.saveMidiEnabled", "true");
		defaultProperties.setProperty("signs.firstErrorOnly", "true");
		defaultProperties.setProperty("signs.showErrors", "true");
		defaultProperties.setProperty("lyrics.enabled", "true");
		defaultProperties.setProperty("signs.playingColor", new Color4f(0,
				0xff, 0xff, 0xff).toString());
		defaultProperties.setProperty("signs.proximityEnabled", "true");
		defaultProperties.setProperty("signs.disabled", "false");
		defaultProperties.setProperty("blockTunes.disabled", "false");
		defaultProperties.setProperty("updates.lastVersionFound",
				CURRENT_VERSION);
		defaultProperties.setProperty("signeditor.keywordAreaVisible", "true");
		defaultProperties.setProperty("signs.playingColor.sliderPos", "0.45");
		// defaultProperties.setProperty("signs.highlightErrorLines", "true");
		defaultProperties.setProperty("mod.lastVersionRun", "0");
		defaultProperties.setProperty("tutorial.lastDownload", "0");
		defaultProperties.setProperty("slides.highQuality", "true");
	}

	/**
	 * Holds most config settings. accessed by various
	 * "getInt, getString, getBoolean, ..." methods in this class.
	 */
	private static Properties properties = new Properties(defaultProperties);

	/**
	 * The properties file used by MineTunes after 0.9.9.01
	 */
	private static File propertiesFile = new File(getMinetunesDir().getPath()
			+ File.separator + "MCDittySettings.xml");
	private static File resourcesDir = new File(getMinetunesDir().getPath()
			+ File.separator + "resources");

	/**
	 * @return
	 */
	public static File getMinetunesDir() {
		return new File(Minecraft.getMinecraftDir().getPath() + File.separator
				+ "mineTunes" + File.separator);
	}

	/**
	 * @return
	 */
	public static File getResourcesDir() {
		return resourcesDir;
	}

	/**
	 * Checks whether the config file has been loaded. If not, this loads it. If
	 * the config file is outdated, this updates it and loads it.
	 * 
	 * Also loads noPlayTokens if the config isn't loaded yet.
	 * 
	 * @param world
	 *            needed to display chat message to player
	 */
	public static void loadAndUpdateSettings() {
		// Check that config file is loaded, and load it if it isn't
		if (!configLoaded) {
			// Check for old-style config folder
			if (MCDittyConfigConverter.conversionNeeded()) {
				MCDittyConfigConverter.convert();
			}

			// Load config settings
			load();

			int versionCompare = CompareVersion.compareVersions(
					CURRENT_VERSION, getString("mod.lastVersionRun"));

			if (versionCompare == CompareVersion.GREATER) {
				// Config is old
				// Write a new one that is up to date
				try {
					flush();
					Minetunes
							.showTextAsLyricNow("§aMineTunes updated to version "
									+ CURRENT_VERSION + "!");
					if (UPDATE_MESSAGE != null) {
						for (String s : UPDATE_MESSAGE) {
							Minetunes.showTextAsLyricNow(s);
						}
					}
					// Update resources
					Minetunes.updateResources();
				} catch (IOException e) {
					// TODO Tell user
					e.printStackTrace();
				}
			} else if (versionCompare == CompareVersion.LESSER) {
				// Gulp! Config is too new!
				Minetunes.showTextAsLyricNow("§eMineTunes reverted to version "
						+ CURRENT_VERSION + " from "
						+ getString("mod.lastVersionRun")
						+ ". These are the good old days.");
				if (UPDATE_MESSAGE != null) {
					for (String s : UPDATE_MESSAGE) {
						Minetunes.showTextAsLyricNow(s);
					}
				}

				try {
					flush();
				} catch (IOException e) {
					e.printStackTrace();
				}

				// Just in case MC version changed too
				Minetunes.updateResources();
			} else {
				// If up to date, and resources have failed to load during the
				// last update, download resources now
				if (getBoolean("resources.missing")) {
					Minetunes.updateResources();
				}
			}
		}
	}

	/**
	 * Thread-safe.
	 * 
	 * @throws IOException
	 */
	public static void flush() throws IOException {
		// NOT Properties (used as lock in flushPropertiesXML)
		synchronized (propertiesFile) {
			// Create MineTunes dir if it does not exist already
			propertiesFile.getParentFile().mkdirs();

			Minetunes.keypressHandler.writeConfig();

			// Save certain fields to properties before flushing properties
			// These are either complex objects or were cached to static fields
			// for better accessing speed
			setString("soundfont.config", customSF2.toConfigString());
			setBoolean("particles.enabled", particlesEnabled);
			setBoolean("particles.noteParticlesDisabled", noteParticlesDisabled);
			setBoolean("signs.fullRender", fullSignRenderingEnabled);

			// Finally, note the current version of the mod before saving
			setString("mod.lastVersionRun", CURRENT_VERSION);

			// Flush properties
			flushProperties();
		}

		// Flush some settings to their respective fields (placed in other
		// classes for accessing speed over using a properties key)

		// Push this value to the sign renderer (extreme speed needed there,
		// can't use getInt every sign render!)
		TileEntitySignRendererMinetunes.blinkTimeMS = getInt("signs.errorBlinkMS");
		TileEntitySignRendererMinetunes.blinkSignsRed = getBoolean("signs.errorBlinkRed");

		// Another one that meddles with the sign renderer
		setFullRenderingEnabled(getBoolean("signs.fullRender"));
	}

	/**
	 * Loads values in config file, if it exists, to various static variables in
	 * BlockSign. If file does not exist, creates a new one. If config was
	 * outdated (but probably loaded anyway), return false.
	 * 
	 * @return true if config file is up to date, false if obsolete
	 */
	private static void load() {
		// Load keyboard settings
		Minetunes.keypressHandler.loadConfig();

		// Load No Play Tokens
		NoPlayTokens.reloadNoPlayTokens();

		// Load Properties
		try {
			FileInputStream xmlIn = new FileInputStream(propertiesFile);
			properties.loadFromXML(xmlIn);
			xmlIn.close();
		} catch (FileNotFoundException e1) {
			// e1.printStackTrace();
		} catch (InvalidPropertiesFormatException e1) {
			// e1.printStackTrace();
		} catch (IOException e1) {
			// e1.printStackTrace();
		}

		// Copy certain properties settings to fields and do other loading tasks
		customSF2 = new CachedCustomSoundfont();
		try {
			customSF2.loadFromConfigString(getString("soundfont.config"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Particles
		particlesEnabled = getBoolean("particles.enabled");
		noteParticlesDisabled = getBoolean("particles.noteParticlesDisabled");

		// Push this value to the sign renderer (extreme speed needed there,
		// can't use getInt every sign render!)
		TileEntitySignRendererMinetunes.blinkTimeMS = getInt("signs.errorBlinkMS");
		TileEntitySignRendererMinetunes.blinkSignsRed = getBoolean("signs.errorBlinkRed");

		// Another one that meddles with the sign renderer
		setFullRenderingEnabled(getBoolean("signs.fullRender"));

		// Note that the config was loaded
		configLoaded = true;

		return;
	}

	private static void flushProperties() {
		// NOT PropertiesFile (used as lock in flushAll)
		synchronized (properties) {
			// Save XML settings
			try {
				propertiesFile.getParentFile().mkdirs();
				propertiesFile.delete();
				propertiesFile.createNewFile();
				FileOutputStream xmlOut = new FileOutputStream(propertiesFile);
				properties.storeToXML(xmlOut,
						"MineTunes Mod Settings: Version " + CURRENT_VERSION);
				xmlOut.flush();
				xmlOut.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean isFullRenderingEnabled() {
		return fullSignRenderingEnabled;
	}

	public static void setFullRenderingEnabled(boolean fullRenderingEnabled) {
		MinetunesConfig.fullSignRenderingEnabled = fullRenderingEnabled;

		// Also set the renderer's copy
		TileEntitySignRendererMinetunes.fullRenderingEnabled = fullRenderingEnabled;
	}

	/**
	 */
	public static MidiVolumeMode getVolumeMode() {
		Integer i = getInt("volume.mode");
		if (i == null) {
			i = 0;
		}
		int I = i;
		return MidiVolumeMode.fromInt(I);
	}

	/**
	 */
	public static void setVolumeMode(MidiVolumeMode mode) {
		setInt("volume.mode", MidiVolumeMode.toInt(mode));
	}

	public static String getMCDittyTurnedOffText() {
		if (getBoolean("noteblock.mute")) {
			return "MineTunes & Noteblocks §cOff";
		} else if (getMinetunesOff()) {
			return "MineTunes §cOff";
		} else if (getBoolean("noteblock.signsDisabled")) {
			return "Noteblock Signs §cOff";
		} else {
			return "MineTunes §aOn";
		}
	}

	public static void incrementMCDittyOffState() {
		if (getMinetunesOff() && getBoolean("noteblock.mute")
				&& getBoolean("noteblock.signsDisabled")) {
			// to MineTunes On
			setBoolean("noteblock.signsDisabled", false);
			setBoolean("noteblock.mute", false);
			setMinetunesOff(false);
		} else if (getMinetunesOff() && getBoolean("noteblock.signsDisabled")) {
			// to MineTunes off, mute noteblocks
			setBoolean("noteblock.mute", true);
		} else if (getBoolean("noteblock.signsDisabled")) {
			// to MineTunes off
			setMinetunesOff(true);
		} else {
			// To noteblock signs off
			setBoolean("noteblock.signsDisabled", true);
		}

		try {
			flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean getMinetunesOff() {
		return getBoolean("mod.off");
	}

	public static void setMinetunesOff(boolean off) {
		setBoolean("mod.off", off);
	}

	public static boolean getBoolean(String key) {
		String value = properties.getProperty(key, "false");
		boolean boolValue = false;
		try {
			boolValue = Boolean.parseBoolean(value);
		} catch (Exception e) {
		}
		return boolValue;
	}

	/**
	 * Handles setting particlesEnabled and noteParticlesDisabled as fields in
	 * this class as well
	 * 
	 * @param key
	 * @param value
	 */
	public static void setBoolean(String key, boolean value) {
		if (key.equalsIgnoreCase("particles.noteParticlesDisabled")) {
			noteParticlesDisabled = value;
		} else if (key.equalsIgnoreCase("particles.enabled")) {
			particlesEnabled = value;
		}

		properties.setProperty(key, Boolean.toString(value));
	}

	/**
	 * 
	 * @param key
	 *            value or null if none is defined
	 * @return
	 */
	public static String getString(String key) {
		return properties.getProperty(key);
	}

	/**
	 * 
	 * @param key
	 * @param defaultValue
	 * @return value or default if none is defined
	 */
	public static String getStringWithDefault(String key, String defaultValue) {
		String value = getString(key);
		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}

	public static void setString(String key, String value) {
		properties.setProperty(key, value);
	}

	public static void setInt(String key, int value) {
		properties.setProperty(key, Integer.toString(value));
	}

	/**
	 * 
	 * @param key
	 * @return 0 if no key was defined or there is a formatting error
	 */
	public static int getInt(String key) {
		String value = properties.getProperty(key, "0");
		int keyValue = 0;
		try {
			keyValue = Integer.parseInt(value);
		} catch (Exception e) {
			return 0;
		}
		return keyValue;
	}

	public static void setFloat(String key, float value) {
		properties.setProperty(key, Float.toString(value));
	}

	/**
	 * 
	 * @param key
	 * @return 0 is default for malformed float or missing key
	 */
	public static float getFloat(String key) {
		String value = properties.getProperty(key, "0");
		float keyValue = 0;
		try {
			keyValue = Float.parseFloat(value);
		} catch (Exception e) {
			return 0;
		}
		return keyValue;
	}

	/**
	 * Toggles the boolean value of the given key.
	 * 
	 * @param key
	 * @return the new value
	 */
	public static boolean toggleBoolean(String key) {
		boolean currValue = getBoolean(key);
		setBoolean(key, !currValue);
		return !currValue;
	}

	/**
	 * 
	 */
	public static void incrementVolumeMode() {
		if (MinetunesConfig.getVolumeMode() == MidiVolumeMode.MAX) {
			MinetunesConfig.setVolumeMode(MidiVolumeMode.MC_SOUND);
		} else if (MinetunesConfig.getVolumeMode() == MidiVolumeMode.MC_SOUND) {
			MinetunesConfig.setVolumeMode(MidiVolumeMode.MC_MUSIC);
		} else if (MinetunesConfig.getVolumeMode() == MidiVolumeMode.MC_MUSIC) {
			MinetunesConfig.setVolumeMode(MidiVolumeMode.MAX);
		} else {
			// Unknown volume mode
			MinetunesConfig.setVolumeMode(MidiVolumeMode.MC_SOUND);
		}
	}

}
