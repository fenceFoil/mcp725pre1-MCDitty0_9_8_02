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
package com.minetunes.config.legacy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;

import net.minecraft.client.Minecraft;

import com.minetunes.Color4f;
import com.minetunes.Minetunes;
import com.minetunes.config.MidiVolumeMode;
import com.minetunes.config.MinetunesConfig;

/**
 * Checks for a MCDitty folder and converts it to a MineTunes folder and config
 * file
 * 
 */
public class MCDittyConfigConverter {

	private static File getMCDittyDir() {
		return new File(Minecraft.getMinecraftDir().getPath() + File.separator
				+ "MCDitty" + File.separator);
	}

	/**
	 * The config file used by MCDitty in and after 0.9.7
	 */
	private static File configFile = new File(getMCDittyDir().getPath()
			+ File.separator + "MCDittySettings.txt");

	/**
	 * Checks for the existence of a /MCDitty/ directory in /.minecraft/, and a
	 * lack of the updated /mineTunes/ dir
	 * 
	 * @return
	 */
	public static boolean conversionNeeded() {
		File mcdittyDir = new File(Minecraft.getMinecraftDir().getPath()
				+ File.separator + "MCDitty");
		if (mcdittyDir.exists() && !MinetunesConfig.getMinetunesDir().exists()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 */
	public static void convert() {
		System.out.println("Converting MCDitty files to MineTunes files.");

		File mcdittyDir = new File(Minecraft.getMinecraftDir().getPath()
				+ File.separator + "MCDitty");
		File minetunesDir = MinetunesConfig.getMinetunesDir();

		// Load old config into a new MinetunesConfig instance
		File fileToLoad = configFile;

		if (configFile.exists() == false) {
			// Not there. Don't worry then, brah.
			return;
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
					} else if (lineIn.startsWith("ParticlesOn=")) {
						lineIn = lineIn.replace("ParticlesOn=", "");
						try {
							MinetunesConfig.particlesEnabled = Boolean
									.parseBoolean(lineIn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("OnlyOneParticleEmitted=")) {
						lineIn = lineIn.replace("OnlyOneParticleEmitted=", "");
						try {
							MinetunesConfig.noteParticlesDisabled = Boolean
									.parseBoolean(lineIn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("MidiSavingEnabled=")) {
						lineIn = lineIn.replace("MidiSavingEnabled=", "");
						try {
							MinetunesConfig.setBoolean("signs.saveMidiEnabled",
									Boolean.parseBoolean(lineIn));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("NoteblockTooltips=")) {
						lineIn = lineIn.replace("NoteblockTooltips=", "");
						try {
							MinetunesConfig.setBoolean(
									"enableNoteblockTooltips",
									Boolean.parseBoolean(lineIn));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
//					} else if (lineIn.startsWith("ShowErrorsOnSigns=")) {
//						lineIn = lineIn.replace("ShowErrorsOnSigns=", "");
//						try {
//							MinetunesConfig.setBoolean(
//									"signs.highlightErrorLines",
//									Boolean.parseBoolean(lineIn));
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
					} else if (lineIn.startsWith("BlinkSignsEnabled=")) {
						lineIn = lineIn.replace("BlinkSignsEnabled=", "");
						try {
							MinetunesConfig.setBoolean("signs.errorBlinkRed",
									Boolean.parseBoolean(lineIn));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
//					} else if (lineIn.startsWith("OnlyFirstErrorShown=")) {
//						lineIn = lineIn.replace("OnlyFirstErrorShown=", "");
//						try {
//							MinetunesConfig.setBoolean("signs.firstErrorOnly",
//									Boolean.parseBoolean(lineIn));
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
					} else if (lineIn.startsWith("showErrors=")) {
						lineIn = lineIn.replace("showErrors=", "");
						try {
							MinetunesConfig.setBoolean("signs.showErrors",
									Boolean.parseBoolean(lineIn));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("CustomSF2=")) {
						lineIn = lineIn.replace("CustomSF2=", "");
						try {
							MinetunesConfig.customSF2
									.loadFromConfigString(lineIn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("LyricsEnabled=")) {
						lineIn = lineIn.replace("LyricsEnabled=", "");
						try {
							MinetunesConfig.setBoolean("lyrics.enabled",
									Boolean.parseBoolean(lineIn));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("showMidiMessageEnabled=")) {
						lineIn = lineIn.replace("showMidiMessageEnabled=", "");
						try {
							MinetunesConfig.setBoolean("midiSavedMessage",
									Boolean.parseBoolean(lineIn));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("SignEditorInfoBox=")) {
						lineIn = lineIn.replace("SignEditorInfoBox=", "");
						try {
							MinetunesConfig.setBoolean(
									"signeditor.keywordAreaVisible",
									Boolean.parseBoolean(lineIn));
						} catch (Exception e) {
							e.printStackTrace();
						}
						// } else if (lineIn.startsWith("UseMCMusicVolume=")) {
						// lineIn = lineIn.replace("UseMCMusicVolume=", "");
						// try {
						// MinetunesConfig.useMCMusicVolume = Boolean
						// .parseBoolean(lineIn);
						// } catch (Exception e) {
						// e.printStackTrace();
						// }
					} else if (lineIn.startsWith("SignBlinkMS=")) {
						lineIn = lineIn.replace("SignBlinkMS=", "");
						try {
							MinetunesConfig.setInt("signs.errorBlinkMS",
									Integer.parseInt(lineIn));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("SignEditorMode=")) {
						lineIn = lineIn.replace("SignEditorMode=", "");
						try {
							MinetunesConfig.setInt("signeditor.mode",
									Integer.parseInt(lineIn));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("MuteKeyCode=")) {
						// ONLY KEPT TO UPDATE OLD CONFIGS TO NEW KEY CONFIG
						// STORAGE
						lineIn = lineIn.replace("MuteKeyCode=", "");
						try {
							Minetunes.keypressHandler.setMuteKeyID(Integer
									.parseInt(lineIn));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("ProxPads=")) {
						lineIn = lineIn.replace("ProxPads=", "");
						try {
							MinetunesConfig.setBoolean(
									"signs.proximityEnabled",
									Boolean.parseBoolean(lineIn));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("MCDittyOff=")) {
						lineIn = lineIn.replace("MCDittyOff=", "");
						try {
							MinetunesConfig.setMinetunesOff(Boolean
									.parseBoolean(lineIn));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("SignColorSlider=")) {
						lineIn = lineIn.replace("SignColorSlider=", "");
						try {
							MinetunesConfig.setFloat(
									"signs.playingColor.sliderPos",
									Float.parseFloat(lineIn));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("IgnoreMCVolume=")) {
						lineIn = lineIn.replace("IgnoreMCVolume=", "");
						try {
							boolean ignoreMCVolume = Boolean
									.parseBoolean(lineIn);
							if (ignoreMCVolume) {
								MinetunesConfig
										.setVolumeMode(MidiVolumeMode.MAX);
							} else {
								// ignore the possibility of following the music
								// volume
								MinetunesConfig
										.setVolumeMode(MidiVolumeMode.MC_SOUND);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn.startsWith("FullRender=")) {
						lineIn = lineIn.replace("FullRender=", "");
						try {
							MinetunesConfig.setFullRenderingEnabled(Boolean
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
							Minetunes.keypressHandler.setGuiKeyID(Integer
									.parseInt(lineIn));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (lineIn
							.startsWith("LastTutorialVersionDownloaded=")) {
						lineIn = lineIn.replace(
								"LastTutorialVersionDownloaded=", "");
						MinetunesConfig.setString("tutorial.lastDownload",
								lineIn);
					} else if (lineIn.startsWith("noteblockTuner.mute=")) {
						lineIn = lineIn.replace("noteblockTuner.mute=", "");
						try {
							MinetunesConfig.setBoolean("noteblockTuner.mute",
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
							Color4f color = new Color4f();
							for (int i = 0; i < 4; i++) {
								color.color[i] = Float.parseFloat(values[i]);
							}
							MinetunesConfig.setString("signs.playingColor",
									color.toString());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				configIn.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Delete old config
		configFile.delete();

		// Flush settings read into new MinetunesConfig
		try {
			MinetunesConfig.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Make sure the destination dir exists
		minetunesDir.mkdirs();

		// Make a copy of the mcditty dir into a new minetunes dir, then delete
		// the mcditty dir
		copyDirsToMinetunes(mcdittyDir);
		try {
			moveFiles(mcdittyDir, minetunesDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		removeOldDirs(mcdittyDir);
	}

	/**
	 * @param mcdittyDir
	 */
	private static void removeOldDirs(File mcdittyDir) {
		File[] dirFiles = mcdittyDir.listFiles();
		for (File f : dirFiles) {
			if (f.isDirectory()) {
				// recurse
				removeOldDirs(f);
			} else {
				// delete
				f.delete();
			}
		}
		mcdittyDir.delete();
	}

	/**
	 * @param mcdittyDir
	 * @param minetunesDir
	 */
	private static void moveFiles(File sourceDir, File destDir)
			throws IOException {
		File[] sourceFiles = sourceDir.listFiles();
		for (File f : sourceFiles) {
			if (!f.isDirectory()) {
				// move file
				File destFile = new File(destDir, f.getName());
				// handle filename changes
				if (f.getName().equalsIgnoreCase("MCDittyNoPlayTokens.txt")) {
					destFile = new File(destDir, "noPlayTokens.txt");
				}
				destFile.createNewFile();

				// move file contents
				FileChannel source = null;
				FileChannel destination = null;
				FileInputStream sourceInputStream = new FileInputStream(f);
				source = sourceInputStream.getChannel();
				FileOutputStream destOutputStream = new FileOutputStream(
						destFile);
				destination = destOutputStream.getChannel();
				if (destination != null && source != null) {
					destination.transferFrom(source, 0, source.size());
				}
				if (source != null) {
					source.close();
				}
				if (destination != null) {
					destination.close();
				}
				sourceInputStream.close();
				destOutputStream.close();

				// delete old
				f.delete();
			} else {
				// recurse
				moveFiles(
						f,
						new File(destDir.getPath() + File.separator
								+ f.getName()));
			}
		}
	}

	/**
	 * @param mcdittyDir
	 * @param minetunesDir
	 */
	private static void copyDirsToMinetunes(File mcdittyDir) {
		File[] mcdittyDirFiles = mcdittyDir.listFiles();
		for (File f : mcdittyDirFiles) {
			if (f.isDirectory()) {
				// Copy over
				File newFile = new File(f.getPath().replace(
						File.separator + "MCDitty" + File.separator,
						File.separator + "mineTunes" + File.separator));
				newFile.mkdirs();
				// Recurse
				copyDirsToMinetunes(f);
			}
		}
	}
}
