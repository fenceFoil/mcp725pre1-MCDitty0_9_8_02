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
package com.wikispaces.mcditty.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import com.wikispaces.mcditty.CachedCustomSoundfont;
import com.wikispaces.mcditty.CompareVersion;
import com.wikispaces.mcditty.MCDitty;

import net.minecraft.client.Minecraft;
import net.minecraft.src.BlockSign;
import net.minecraft.src.GuiEditSign;
import net.minecraft.src.TileEntitySignRenderer;
import net.minecraft.src.World;

/**
 * Represents the values stored in the MCDitty config file and other
 * information.
 * 
 */
public class MCDittyConfig {

	public static boolean particlesEnabled = true;
	/**
	 * From config file: if true, only the first sign emits a particle
	 */
	public static boolean emitOnlyOneParticle = false;
	/**
	 * Current MCDitty version.
	 */
	public static final String CURRENT_VERSION = "0.9.9.04";
	/**
	 * Minecraft version that the mod is designed for.
	 */
	public static final String MC_CURRENT_VERSION = "1.4.6";

	/**
	 * Constants representing volume modes
	 */
	public static final int IGNORE_MC_VOLUME = 100;
	public static final int USE_MUSIC_VOLUME = 200;
	public static final int USE_SOUND_VOLUME = 0;
	private static final String UPDATE_MESSAGE = "<MCDitty> New! Putting netherrack near a noteblock = Lower notes!";

	/**
	 * The custom soundfont selected by the player
	 */
	public static CachedCustomSoundfont customSF2 = new CachedCustomSoundfont();

	/**
	 * For config file: whether config file is loaded yet
	 */
	public static boolean configLoaded = false;
	/**
	 * The config file used by MCDitty prior to 0.9.7
	 */
	public static File oldConfigFile = new File(Minecraft.getMinecraftDir()
			.getPath() + File.separator + "MCDittyConfig.txt");

	/**
	 * The properties file used by MCDitty after 0.9.9.01
	 */
	public static File xmlSettingsFile = new File(Minecraft.getMinecraftDir()
			.getPath()
			+ File.separator
			+ "MCDitty"
			+ File.separator
			+ "MCDittySettings.xml");

	private static Properties defaultProperties = new Properties();
	static {
		// SET UP DEFAULT PROPERTIES
		defaultProperties.setProperty("enableNoteblockTooltips", "true");
	}

	private static Properties properties = new Properties(defaultProperties);

	/**
	 * The config file used by MCDitty in and after 0.9.7
	 */
	public static File configFile = new File(Minecraft.getMinecraftDir()
			.getPath()
			+ File.separator
			+ "MCDitty"
			+ File.separator
			+ "MCDittySettings.txt");

	public static File noPlayTokensFile = new File(Minecraft.getMinecraftDir()
			.getPath()
			+ File.separator
			+ "MCDitty"
			+ File.separator
			+ "MCDittyNoPlayTokens.txt");
	/**
	 * Time to blink signs
	 */
	public static long blinkTimeMS = 6000;
	/**
	 * From config file: whether midi files from midi signs are saved or not
	 */
	public static boolean midiSavingEnabled = true;
	/**
	 * From config file: whether signs blink with the end sky texture on a error
	 * for a bit
	 */
	public static boolean blinkSignsTexturesEnabled = true;
	/**
	 * From config file: whether all errors found are printed to chat at once,
	 * or just first
	 */
	public static boolean onlyFirstErrorShown = true;
	/**
	 * From config file: whether errors are shown at all on the chat.
	 */
	public static boolean showErrors = true;
	/**
	 * From config file: highlight keywords, comments, ect?
	 */
	public static boolean highlightEnabled = false;
	/**
	 * From config file: show welcome message?
	 */
	public static boolean showWelcomeMessage = true;
	/**
	 * From config file: are lyrics turned on?
	 */
	public static boolean lyricsEnabled = true;
	/**
	 * From config file: the tune played when letters are typed on a sign when
	 * the line is full
	 */
	public static String endOfLineTune = "F5min";
	/**
	 * Whether the sound or the music volume is used to control MCDitty's volume
	 */
	public static boolean useMCMusicVolume = false;
	/**
	 * Always plays MIDI at full volume, ignoring MC volume settings
	 */
	public static boolean ignoreMCVolume = false;
	/**
	 * Whether a chat message is shown upon saving a midi
	 */
	public static boolean showMidiMessageEnabled = false;
	/**
	 * Disable to stop costly printlns from being called
	 */
	public static boolean debug = false;
	public static float[] signPlayingHighlightColor = { 0, 0xff, 0xff, 0xff };
	public static String lastVersionFound = CURRENT_VERSION;
	/**
	 * Current sign editor mode.
	 */
	public static int signEditorMode = GuiEditSign.SIGN_EDITOR_MODE_NORMAL;
	public static boolean keywordTextAreaVisible = true;
	public static boolean proxPadsEnabled = true;

	/**
	 * Rendering: Full Minecraft-style rendering enabled
	 */
	private static boolean fullRenderingEnabled = false;

	/**
	 * Last version of the MCDitty tutorial world downloaded
	 */
	public static String lastTutorialVersionDownloaded = "0";

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
	public static float signPlayingHighlightSlider = 0.45f;
	public static boolean showErrorsOnSigns = true;

	/**
	 * Whether to act as though MCDitty isn't there
	 */
	public static boolean mcdittyOff = false;
	public static File resourcesDir = new File(Minecraft.getMinecraftDir()
			.getPath()
			+ File.separator
			+ "MCDitty"
			+ File.separator
			+ "resources");

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
	 * Checks whether the config file has been loaded. If not, this loads it. If
	 * the config file is outdated, this updates it and loads it.
	 * 
	 * Also loads noPlayTokens if the config isn't loaded yet.
	 * 
	 * @param world
	 *            needed to display chat message to player
	 */
	public static void checkConfig(World world) {
		// Check that config file is loaded, and load it if it isn't
		if (!configLoaded) {
			// Check that there isn't an old (pre 0.9.7) config file lying
			// around.
			// If there is, move it to the new location before anyone gets hurt
			if (oldConfigFile.exists()) {
				// Move it
				try {
					copyFile(oldConfigFile, configFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				oldConfigFile.delete();
			}

			// Load config file. Will create one if necessary.
			// Returns false if config file is for an old version
			// Loads values even from an old config
			boolean upToDate = loadSettings();
			if (!upToDate) {
				// If it is old, clear the config file...
				deleteConfigFile();
				// Write a new one that is up to date
				try {
					flushAll();
					BlockSign.writeChatMessage(world,
							"§aMCDitty updated to version " + CURRENT_VERSION
									+ "!");
					if (UPDATE_MESSAGE != null) {
						BlockSign.writeChatMessage(world, UPDATE_MESSAGE);
					}
					// Update resources
					MCDitty.updateResources();
				} catch (IOException e) {
					// TODO Tell user
					e.printStackTrace();
				}

			} else {
				// If up to date, and resources have failed to load during the
				// last update, download resources now
				if (getBoolean("resources.missing")) {
					MCDitty.updateResources();
				}
			}

			// Load no play tokens
			reloadNoPlayTokens();

			configLoaded = true;
		}
	}

	/**
	 * Deletes MCDitty config file.
	 */
	private static void deleteConfigFile() {
		if (oldConfigFile.exists()) {
			oldConfigFile.delete();
		}
		if (configFile.exists()) {
			configFile.delete();
		}
	}

	/**
	 * Loads values in config file, if it exists, to various static variables in
	 * BlockSign. If file does not exist, creates a new one. If config was
	 * outdated (but probably loaded anyway), return false.
	 * 
	 * @return true if config file is up to date, false if obsolete
	 */
	private static boolean loadSettings() {
		// Load keyboard settings
		MCDitty.keypressHandler.loadConfig();

		// Load XML Config
		try {
			FileInputStream xmlIn = new FileInputStream(xmlSettingsFile);
			properties.loadFromXML(xmlIn);
			xmlIn.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			// e1.printStackTrace();
		} catch (InvalidPropertiesFormatException e1) {
			// TODO Auto-generated catch block
			// e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			// e1.printStackTrace();
		}

		File fileToLoad = configFile;

		// If it does not exist, create new config file with the default
		// settings in it.
		if (configFile.exists() == false) {
			// Write a new default version
			try {
				flushAll();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		} else {
			String fileVersion = null;
			try {
				// Otherwise, read the config file
				BufferedReader configIn = new BufferedReader(new FileReader(
						fileToLoad));
				while (true) {
					// Loop for each line. If line is empty or there are no more
					// lines, end the loop.
					String lineIn = configIn.readLine();
					if (lineIn == null) {
						break;
					}
					lineIn = lineIn.trim();

					// Skip comments
					if (lineIn.startsWith("#")) {
						continue;
					}

					// Check for individual settings
					if (lineIn.startsWith("Version=")) {
						lineIn = lineIn.replace("Version=", "");
						fileVersion = lineIn;
					} else if (lineIn.startsWith("ShowWelcomeMessage=")) {
						lineIn = lineIn.replace("ShowWelcomeMessage=", "");
						try {
							showWelcomeMessage = Boolean.parseBoolean(lineIn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("ParticlesOn=")) {
						lineIn = lineIn.replace("ParticlesOn=", "");
						try {
							particlesEnabled = Boolean.parseBoolean(lineIn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("OnlyOneParticleEmitted=")) {
						lineIn = lineIn.replace("OnlyOneParticleEmitted=", "");
						try {
							emitOnlyOneParticle = Boolean.parseBoolean(lineIn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("MidiSavingEnabled=")) {
						lineIn = lineIn.replace("MidiSavingEnabled=", "");
						try {
							midiSavingEnabled = Boolean.parseBoolean(lineIn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("NoteblockTooltips=")) {
						lineIn = lineIn.replace("NoteblockTooltips=", "");
						try {
							setBoolean("enableNoteblockTooltips",
									Boolean.parseBoolean(lineIn));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("ShowErrorsOnSigns=")) {
						lineIn = lineIn.replace("ShowErrorsOnSigns=", "");
						try {
							showErrorsOnSigns = Boolean.parseBoolean(lineIn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("BlinkSignsEnabled=")) {
						lineIn = lineIn.replace("BlinkSignsEnabled=", "");
						try {
							blinkSignsTexturesEnabled = Boolean
									.parseBoolean(lineIn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("OnlyFirstErrorShown=")) {
						lineIn = lineIn.replace("OnlyFirstErrorShown=", "");
						try {
							onlyFirstErrorShown = Boolean.parseBoolean(lineIn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("showErrors=")) {
						lineIn = lineIn.replace("showErrors=", "");
						try {
							showErrors = Boolean.parseBoolean(lineIn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("HighlightingEnabled=")) {
						lineIn = lineIn.replace("HighlightingEnabled=", "");
						try {
							highlightEnabled = Boolean.parseBoolean(lineIn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("CustomSF2=")) {
						lineIn = lineIn.replace("CustomSF2=", "");
						try {
							customSF2.loadFromConfigString(lineIn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("LyricsEnabled=")) {
						lineIn = lineIn.replace("LyricsEnabled=", "");
						try {
							lyricsEnabled = Boolean.parseBoolean(lineIn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("showMidiMessageEnabled=")) {
						lineIn = lineIn.replace("showMidiMessageEnabled=", "");
						try {
							showMidiMessageEnabled = Boolean
									.parseBoolean(lineIn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("SignEditorInfoBox=")) {
						lineIn = lineIn.replace("SignEditorInfoBox=", "");
						try {
							keywordTextAreaVisible = Boolean
									.parseBoolean(lineIn);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("UseMCMusicVolume=")) {
						lineIn = lineIn.replace("UseMCMusicVolume=", "");
						try {
							useMCMusicVolume = Boolean.parseBoolean(lineIn);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("SignBlinkMS=")) {
						lineIn = lineIn.replace("SignBlinkMS=", "");
						try {
							blinkTimeMS = Long.parseLong(lineIn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("SignEditorMode=")) {
						lineIn = lineIn.replace("SignEditorMode=", "");
						try {
							signEditorMode = Integer.parseInt(lineIn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("MuteKeyCode=")) {
						// ONLY KEPT TO UPDATE OLD CONFIGS TO NEW KEY CONFIG
						// STORAGE
						lineIn = lineIn.replace("MuteKeyCode=", "");
						try {
							MCDitty.keypressHandler.setMuteKeyID(Integer
									.parseInt(lineIn));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("ProxPads=")) {
						lineIn = lineIn.replace("ProxPads=", "");
						try {
							proxPadsEnabled = Boolean.parseBoolean(lineIn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("MCDittyOff=")) {
						lineIn = lineIn.replace("MCDittyOff=", "");
						try {
							mcdittyOff = Boolean.parseBoolean(lineIn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("SignColorSlider=")) {
						lineIn = lineIn.replace("SignColorSlider=", "");
						try {
							signPlayingHighlightSlider = Float
									.parseFloat(lineIn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("IgnoreMCVolume=")) {
						lineIn = lineIn.replace("IgnoreMCVolume=", "");
						try {
							ignoreMCVolume = Boolean.parseBoolean(lineIn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("FullRender=")) {
						lineIn = lineIn.replace("FullRender=", "");
						try {
							setFullRenderingEnabled(Boolean
									.parseBoolean(lineIn));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("MenuKeyCode=")) {
						// ONLY KEPT TO UPDATE OLD CONFIGS TO NEW KEY CONFIG
						// STORAGE
						lineIn = lineIn.replace("MenuKeyCode=", "");
						try {
							MCDitty.keypressHandler.setGuiKeyID(Integer
									.parseInt(lineIn));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("LastVersionFound=")) {
						lineIn = lineIn.replace("LastVersionFound=", "");
						lastVersionFound = lineIn;
					} else if (lineIn
							.startsWith("LastTutorialVersionDownloaded=")) {
						lineIn = lineIn.replace(
								"LastTutorialVersionDownloaded=", "");
						lastTutorialVersionDownloaded = lineIn;
					} else if (lineIn.startsWith("noteblockTuner.mute=")) {
						lineIn = lineIn.replace("noteblockTuner.mute=", "");
						try {
							setBoolean("noteblockTuner.mute",
									Boolean.parseBoolean(lineIn));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("SignPlayingHighlight=")) {
						lineIn = lineIn.replace("SignPlayingHighlight=", "");
						try {
							String[] values = lineIn.split(":");
							if (values.length != 4) {
								continue;
							}
							for (int i = 0; i < 4; i++) {
								signPlayingHighlightColor[i] = Float
										.parseFloat(values[i]);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				configIn.close();

				if (fileVersion != null) {
					if (CompareVersion.compareVersions(fileVersion,
							CURRENT_VERSION) == CompareVersion.LESSER) {
						return false;
					} else {
						return true;
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (fileVersion != null) {
					if (CompareVersion.compareVersions(fileVersion,
							CURRENT_VERSION) == CompareVersion.LESSER) {
						return false;
					} else {
						return true;
					}
				}
			}
		}
		// If we for some reason fall this far, return true.
		return true;
	}

	/**
	 * Thread-safe.
	 * 
	 * @throws IOException
	 */
	public static void flushAll() throws IOException {
		synchronized (configFile) {
			// Create MCDitty dir if it does not exist already
			configFile.getParentFile().mkdirs();

			MCDitty.keypressHandler.writeConfig();

			flushPropertiesXML();

			configFile.createNewFile();
			BufferedWriter configOut = new BufferedWriter(new FileWriter(
					configFile));
			configOut
					.write("MCDitty Settings File (settings are case sensitive)");
			configOut.newLine();
			configOut
					.write("To reset this file to defaults, simply delete it.");
			configOut.newLine();
			configOut.newLine();
			configOut
					.write("Don't change the version. That's just for the mod's reference.");
			configOut.newLine();
			configOut.write("Version=" + CURRENT_VERSION);
			configOut.newLine();
			configOut.newLine();
			configOut
					.write("Show the welcome message next time Minecraft starts? Possible values: true false");
			configOut.newLine();
			configOut.write("ShowWelcomeMessage="
					+ Boolean.toString(showWelcomeMessage));
			configOut.newLine();
			configOut.newLine();
			configOut
					.write("Whether MCDitty plays ditties. Possible values: true false");
			configOut.newLine();
			configOut.write("MCDittyOff=" + Boolean.toString(mcdittyOff));
			configOut.newLine();
			configOut.newLine();
			configOut
					.write("Whether colorful notes are emitted when a sign is activated. Possible values: true false");
			configOut.newLine();
			configOut
					.write("ParticlesOn=" + Boolean.toString(particlesEnabled));
			configOut.newLine();
			configOut.newLine();
			configOut
					.write("Whether only the first sign click emits a particle. Possible values: true false");
			configOut.newLine();
			configOut.write("OnlyOneParticleEmitted="
					+ Boolean.toString(emitOnlyOneParticle));
			configOut.newLine();
			configOut.newLine();
			configOut
					.write("Whether only the first error in a flawed song is shown on the chat. Possible values: true false");
			configOut.newLine();
			configOut.write("OnlyFirstErrorShown="
					+ Boolean.toString(onlyFirstErrorShown));
			configOut.newLine();
			configOut.newLine();
			configOut.write("Possible values: true false");
			configOut.newLine();
			configOut.write("ShowErrorsOnSigns="
					+ Boolean.toString(showErrorsOnSigns));
			configOut.newLine();
			configOut.newLine();
			configOut
					.write("Whether errors are shown on the chat. Possible values: true false");
			configOut.newLine();
			configOut.write("showErrors=" + Boolean.toString(showErrors));
			configOut.newLine();
			configOut.newLine();
			configOut
					.write("MS that signs blink to show where a problem in a song is (in MS: 1000 = 1 second)");
			configOut.newLine();
			configOut.write("SignBlinkMS=" + Long.toString(blinkTimeMS));
			configOut.newLine();
			configOut.newLine();
			configOut.write("Whether midi signs are obeyed (true or false)");
			configOut.newLine();
			configOut.write("MidiSavingEnabled="
					+ Boolean.toString(midiSavingEnabled));
			configOut.newLine();
			configOut.newLine();
			configOut.write("Whether lyrics enabled (true or false)");
			configOut.newLine();
			configOut.write("LyricsEnabled=" + Boolean.toString(lyricsEnabled));
			configOut.newLine();
			configOut.newLine();
			configOut
					.write("If true, every single sign within eyeshot is rendered, with text. Normal Minecraft behaviour, but very expensive for lots of signs.");
			configOut.newLine();
			configOut.write("FullRender="
					+ Boolean.toString(isFullRenderingEnabled()));
			configOut.newLine();
			configOut.newLine();
			configOut
					.write("Should signs blink red if they have a mistake, as opposed to just blinking their text? (true or false)");
			configOut.newLine();
			configOut.write("BlinkSignsEnabled="
					+ Boolean.toString(blinkSignsTexturesEnabled));
			configOut.newLine();
			configOut.newLine();
			configOut
					.write("Should parts of a ditty, such as keywords and comments, be highlighted (in progress)? (true or false)");
			configOut.newLine();
			configOut.write("HighlightingEnabled="
					+ Boolean.toString(highlightEnabled));
			configOut.newLine();
			configOut.newLine();
			// configOut.write("Tokens that, in a sign, immediately prevent a song from playing. A list of tokens, separated by spaces. Not case sensitive.");
			// configOut.newLine();
			// configOut.write("NoPlayTokens=" + getNoPlayTokensString());
			// configOut.newLine();
			// configOut.newLine();
			configOut
					.write("The color that signs will be tinted as they are played. (R:G:B:A, each value 0-255)");
			configOut.newLine();
			configOut.write("SignPlayingHighlight="
					+ signPlayingHighlightColor[0] + ":"
					+ signPlayingHighlightColor[1] + ":"
					+ signPlayingHighlightColor[2] + ":"
					+ signPlayingHighlightColor[3]);
			configOut.newLine();
			configOut.newLine();
			configOut
					.write("The position of the sign hightlight color slider in the settings menu (0-1 float).");
			configOut.newLine();
			configOut.write("SignColorSlider=" + signPlayingHighlightSlider);
			configOut.newLine();
			configOut.newLine();
			configOut
					.write("The sign editor's current mode. (Normal Mode is 0)");
			configOut.newLine();
			configOut.write("SignEditorMode=" + signEditorMode);
			configOut.newLine();
			configOut.newLine();
			configOut.write("noteblockTuner.mute="
					+ getBoolean("noteblockTuner.mute"));
			configOut.newLine();
			configOut.newLine();
			configOut
					.write("Whether the keyword info box is shown in the sign editor.");
			configOut.newLine();
			configOut.write("SignEditorInfoBox=" + keywordTextAreaVisible);
			configOut.newLine();
			configOut.newLine();
			configOut
					.write("Whether a message is printed every time a midi is saved.");
			configOut.newLine();
			configOut.write("showMidiMessageEnabled=" + showMidiMessageEnabled);
			configOut.newLine();
			configOut.newLine();
			configOut.write("Whether ProxPads are obeyed.");
			configOut.newLine();
			configOut.write("ProxPads=" + proxPadsEnabled);
			configOut.newLine();
			configOut.newLine();
			configOut.write("The last version of the mod found available.");
			configOut.newLine();
			configOut.write("LastVersionFound=" + lastVersionFound);
			configOut.newLine();
			configOut.newLine();
			configOut
					.write("The last version of the tutorial world downloaded.");
			configOut.newLine();
			configOut.write("LastTutorialVersionDownloaded="
					+ lastTutorialVersionDownloaded);
			configOut.newLine();
			configOut.newLine();
			configOut
					.write("Whether the Minecraft music or sound volume controls MCDitty's volume.");
			configOut.newLine();
			configOut.write("UseMCMusicVolume=" + useMCMusicVolume);
			configOut.newLine();
			configOut.newLine();
			configOut
					.write("Whether MCDitty's volume is entirely independent of minecraft's volume.");
			configOut.newLine();
			configOut.write("IgnoreMCVolume=" + ignoreMCVolume);
			configOut.newLine();
			configOut.newLine();
			configOut
					.write("The current custom SoundFont loaded (instrument patches).");
			configOut.newLine();
			configOut.write("CustomSF2=" + customSF2.toConfigString());
			configOut.newLine();
			configOut.newLine();
			configOut.close();
		}
	}

	public static void flushPropertiesXML() {
		// Save XML settings
		try {
			xmlSettingsFile.getParentFile().mkdirs();
			xmlSettingsFile.delete();
			xmlSettingsFile.createNewFile();
			FileOutputStream xmlOut = new FileOutputStream(xmlSettingsFile);
			properties.storeToXML(xmlOut, "MCDitty XML Settings");
			xmlOut.flush();
			xmlOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isFullRenderingEnabled() {
		return fullRenderingEnabled;
	}

	public static void setFullRenderingEnabled(boolean fullRenderingEnabled) {
		MCDittyConfig.fullRenderingEnabled = fullRenderingEnabled;

		// Also set the renderer's copy
		TileEntitySignRenderer.fullRenderingEnabled = fullRenderingEnabled;
	}

	/**
	 * Combines the states of useMCMusicVolume and ignoreMCVolume into the
	 * following single states:
	 * 
	 * @return USE_MUSIC_VOLUME, USE_SOUND_VOLUME, or IGNORE_MC_VOLUME
	 */
	public static int getVolumeMode() {
		if (ignoreMCVolume) {
			return IGNORE_MC_VOLUME;
		} else {
			if (useMCMusicVolume) {
				return USE_MUSIC_VOLUME;
			} else {
				return USE_SOUND_VOLUME;
			}
		}
	}

	/**
	 * Sets useMCMusicVolume and ignoreMCVolume together to create the given
	 * single volume state.
	 * 
	 * @param volumeState
	 *            USE_MUSIC_VOLUME, USE_SOUND_VOLUME, or IGNORE_MC_VOLUME
	 */
	public static void setVolumeMode(int volumeState) {
		if (volumeState == IGNORE_MC_VOLUME) {
			ignoreMCVolume = true;
		} else if (volumeState == USE_MUSIC_VOLUME) {
			ignoreMCVolume = false;
			useMCMusicVolume = true;
		} else if (volumeState == USE_SOUND_VOLUME) {
			ignoreMCVolume = false;
			useMCMusicVolume = false;
		}
	}

	/**
	 * Creates parent dirs as necessary if possible
	 * 
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException
	 */
	private static void copyFile(File sourceFile, File destFile)
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

	public static String getMCDittyTurnedOffText() {
		if (getBoolean("noteblock.mute")) {
			return "MCDitty & Noteblocks §cOff";
		} else if (mcdittyOff) {
			return "MCDitty §cOff";
		} else if (getBoolean("noteblock.signsDisabled")) {
			return "Noteblock Signs §cOff";
		} else {
			return "MCDitty §aOn";
		}
	}

	public static void incrementMCDittyOffState() {
		if (mcdittyOff && getBoolean("noteblock.mute")
				&& getBoolean("noteblock.signsDisabled")) {
			// to MCDitty On
			setBoolean("noteblock.signsDisabled", false);
			setBoolean("noteblock.mute", false);
			mcdittyOff = false;
		} else if (mcdittyOff && getBoolean("noteblock.signsDisabled")) {
			// to mcditty off, mute noteblocks
			setBoolean("noteblock.mute", true);
		} else if (getBoolean("noteblock.signsDisabled")) {
			// to mcditty off
			mcdittyOff = true;
		} else {
			// To noteblock signs off
			setBoolean("noteblock.signsDisabled", true);
		}

		try {
			flushAll();
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	public static void setBoolean(String key, boolean value) {
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
	 * @return null if there was a formatting error, 0 if no key was defined
	 */
	public static Integer getInt(String key) {
		String value = properties.getProperty(key, "0");
		int keyValue = 0;
		try {
			keyValue = Integer.parseInt(value);
		} catch (Exception e) {
			return null;
		}
		return keyValue;
	}

}
