/**
 * 
 * Copyright (c) 2012 William Karnavas All Rights Reserved
 * 
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
package com.wikispaces.mcditty.sfx;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import org.jfugue.elements.Note;

import com.wikispaces.mcditty.GetMinecraft;

/**
 * Handles all things SFX and effects.<br>
 * <br>
 * Converts between MCDitty shorthand names for SFX keyword and full effect
 * names for Minecraft; plays SFX and effects; loads and stores properties of
 * effects such as default center pitch; and manages conversion between
 * minecraft effect names and filenames.<br>
 * <br>
 * Glossary:<br>
 * <br>
 * Handle: a short name, such as "dogbark", which represents...<br>
 * Effect: a full sound effect name, such as newsound.mob.dog.bark, such as Minecraft uses as a sound effect name<br>
 * SFX: Sound Effect<br>
 * 
 */
public class SFXManager {

	private static Random rand = new Random();

	private static HashMap<String, String> effectNames = new HashMap<String, String>();
	private static HashMap<String, String> effectTuningsString = new HashMap<String, String>();
	private static HashMap<String, Integer> effectTuningsInt = new HashMap<String, Integer>();

	private static Set<String> sfxBlackListEffects = new HashSet<String>();

	private static Set<String> sfxBlackListShorthands = new HashSet<String>();

	public static void load() {
		// Load effect names
		try {
			// System.out.println("MCDitty: Loading sfx names");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					SFXManager.class.getResourceAsStream("sfxNames.txt")));
			while (true) {
				String inLine = reader.readLine();
				if (inLine == null
						|| inLine.toLowerCase().startsWith("end of effects")) {
					break;
				}

				// Break apart each line of sfxNames
				String[] mapping = inLine.split(":");

				// Store the effect name for the given shorthand
				effectNames.put(mapping[0], mapping[1]);

				// Check for third pard
				if (mapping.length >= 3) {
					// Entry has a third part
					if (mapping[2].equals("NoSFXInst")) {
						sfxBlackListEffects.add(mapping[1]);
						sfxBlackListShorthands.add(mapping[0]);
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Load default center pitches
		try {
			// System.out.println("MCDitty: Loading sfx names");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					SFXManager.class.getResourceAsStream("effectPitches.txt")));
			while (true) {
				String inLine = reader.readLine();
				if (inLine == null
						|| inLine.toLowerCase().startsWith("end of effects")) {
					break;
				}

				// Break apart each line of sfxNames
				String[] mapping = inLine.split(":");

				// Store the effect name for the given shorthand
				effectTuningsString.put(mapping[0], mapping[1]);
				int pitchValue = Note.createNote(mapping[1]).getValue();
				effectTuningsInt.put(mapping[0], pitchValue);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Attempts to return the full MC name of a given SFX shorthand.
	 * 
	 * @param name
	 * @return the name, or null if not found
	 */
	public static String getEffectForShorthandName(String name) {
		return effectNames.get(name.toLowerCase());
	}

	public static void playEffectByShorthand(String name) {
		GetMinecraft.instance().theWorld.playSoundAtEntity(
				GetMinecraft.instance().thePlayer,
				getEffectForShorthandName(name), 1.0f, 1.0f);
	}

	public static void playEffect(String mcName) {
		playEffect(mcName, 1.0f, 1.0f);
	}

	public static void playEffect(String mcName, float volume, float pitch) {
		GetMinecraft.instance().theWorld.playSoundAtEntity(
				GetMinecraft.instance().thePlayer, mcName, volume, pitch);
	}

	public static HashMap<String, String> getAllEffects() {
		return (HashMap<String, String>) effectNames.clone();
	}

	/**
	 * Determines if the given effect exists in /.minecraft/resources/newsound/
	 * 
	 * @param effectName
	 * @return
	 */
	public static boolean doesEffectExist(String effectName, int effectNumber) {
		File effectFile = getEffectFile(effectName, effectNumber);
		return effectFile.exists();
	}

	/**
	 * Gets the effect's filename. If the effectNumber is 1, tries to choose
	 * either "effectname.ogg" or "effectname1.ogg" (returning the one that
	 * exists, or the bare version if neither. Otherwise, makes no check to
	 * ensure that a given effectNumber exists. If given -1 to choose a random
	 * file, and none of the effects actually exist (of any number), the same
	 * result is returned as if "1" had been the effectNumber.
	 * 
	 * @param effectName
	 *            The name of the effect (mob.cookiemonster.nomnomnom)
	 * @param effectNumber
	 *            The number (as in /newsound/mob/cookiemonster/nomnomnom3.ogg)
	 *            OR -1 for a random effect (as minecraft does; returns
	 *            nomnomnom1 2 or 3). 0 is a valid input; is replaced with 1.
	 * @return the effect's filename or null if the effect name is bad
	 */
	public static String getEffectFilename(String effectName, int effectNumber) {
		// 0 implies the first sound effect available, but this function
		// requires an input of -1 or 1 and up. '0's are converted to 1.
		if (effectNumber == 0) {
			effectNumber = 1;
		}

		// Base filename, complete up to the newsound dir.
		StringBuilder filename = new StringBuilder()
				.append(GetMinecraft.instance().getMinecraftDir())
				.append(File.separator).append("resources")
				.append(File.separator).append("newsound")
				.append(File.separator);
		// Complete filename
		String[] effectNameParts = effectName.split("\\.");

		// Check for invalid effect name
		if (effectNameParts.length <= 0) {
			// Bad effect name
			return null;
		}

		// Add everything but last part as a directory
		for (int i = 0; i < effectNameParts.length - 1; i++) {
			String s = effectNameParts[i];
			filename.append(s);
			filename.append(File.separator);
		}

		// Add last part as the file name
		filename.append(effectNameParts[effectNameParts.length - 1]);

		// Do the randomization of effect number
		if (effectNumber == -1) {
			// Get the list of choices
			LinkedList<Integer> effectsThatExist = new LinkedList<Integer>();

			// Try bare (0) and 1 through 9.
			for (int i = 0; i < 10; i++) {
				String existCheckFilename = filename.toString();

				// Add the 1-9 as necessary
				if (i > 0) {
					existCheckFilename += Integer.toString(i);
				}

				// Add extension
				existCheckFilename += ".ogg";

				// Try filename
				if (new File(existCheckFilename).exists()) {
					effectsThatExist.add(i);
				}
			}

			// If none exist, use the bare effect (in this case, use 1 to
			// activate choice code below which will eventaully result in bare)
			if (effectsThatExist.size() <= 0) {
				effectNumber = 1;
			} else if (effectsThatExist.size() == 1) {
				// Gee, what a tough random choice this is.
				effectNumber = effectsThatExist.getFirst();
			} else {
				// Okay, 2 or more effects existing means we have to get jiggy.
				// Choose from the list of effects that are known to exist
				effectNumber = effectsThatExist.get(rand
						.nextInt(effectsThatExist.size()));

				// 0 doesn't work well here, so change it to 1; the end result
				// should still be a bare filename as deemed suitable by the
				// code below
				if (effectNumber == 0) {
					effectNumber = 1;
				}
			}
		}

		// Here we try both the effect.ogg and effect1.ogg versions to decide
		// which to return, if needed
		if (effectNumber == 1) {
			// File bareName = new File (filename.toString()+".ogg");
			File nameAnd1 = new File(filename.toString() + "1.ogg");

			// Try the name and 1
			if (nameAnd1.exists()) {
				// If it exists, use it
				// Add 1 to the returned name
				filename.append("1");
			} else {
				// If it does not, use the bare name
				// Add nothing.
			}
		} else if (effectNumber > 1) {
			// Add the effect number to the filename
			filename.append(Integer.toString(effectNumber));
		}

		// Add the .ogg extension
		filename.append(".ogg");

		return filename.toString();
	}

	/**
	 * See SFXManager.getEffectFileName(String, int).
	 * 
	 * Converts its output to a File.
	 * 
	 * @param effectName
	 * @return
	 */
	public static File getEffectFile(String effectName, int effectNumber) {
		return new File(getEffectFilename(effectName, effectNumber));
	}

	/**
	 * Get the semitone-number format of the center pitch of the effect
	 * 
	 * @param effectName
	 * @param sfxNumber
	 *            >= 1; if not, it is rounded up
	 * @return null if effect has no default center pitch
	 */
	public static Integer getDefaultTuningInt(String effectName, int sfxNumber) {
		if (sfxNumber < 1) {
			sfxNumber = 1;
		}
		return effectTuningsInt.get(effectName + sfxNumber);
	}

	/**
	 * Get the string version of the effect's center pitch; e.g. "F#3"
	 * 
	 * @param effectName
	 * @param sfxNumber
	 *            >= 1; if not, it is rounded up
	 * @return null if effect has no default center pitch
	 */
	public static String getDefaultTuningString(String effectName, int sfxNumber) {
		if (sfxNumber < 1) {
			sfxNumber = 1;
		}
		return effectTuningsString.get(effectName + sfxNumber);
	}

	/**
	 * Returns whether a given SFX is flagged not to be used by SFXInst (usually
	 * due to decoder bugginess... grr!)
	 * 
	 * @param shorthand
	 *            not case sensitive
	 * @return
	 */
	public static boolean isShorthandOnSFXInstBlacklist(String shorthand) {
		for (String s : sfxBlackListShorthands) {
			if (s.equals(shorthand)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether a given SFX is flagged not to be used by SFXInst (usually
	 * due to decoder bugginess... grr!)
	 * 
	 * @param effect
	 *            not case sensitive
	 * @return
	 */
	public static boolean isEffectOnSFXInstBlacklist(String effect) {
		for (String s : sfxBlackListEffects) {
			if (s.equals(effect)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the number of meow1.ogg, meow2.ogg, meow3.ogg, etc. alternatives a given effect has.
	 * Checks the filesystem, so this may be slightly expensive.
	 * @param effect
	 * @return between 0 and 9. (0 signifies none found, 9 is the maximum this will check for).
	 */
	public static int getNumberOfAlternativesForEffect (String effect) {
		int found = 0;
		for (int i=1;i<=9;i++) {
			if (doesEffectExist(effect, i)) {
				// Try another.
				found++;
			} else {
				// Stop trying.
				break;
			}
		}
		return found;
	}
}
