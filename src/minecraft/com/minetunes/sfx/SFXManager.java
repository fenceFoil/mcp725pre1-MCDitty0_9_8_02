/**
 * 
 * Copyright (c) 2012 William Karnavas All Rights Reserved
 * 
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
package com.minetunes.sfx;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.minecraft.client.Minecraft;

import org.jfugue.elements.Note;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.minetunes.DOMUtil;
import com.minetunes.Finder;
import com.minetunes.resources.ResourceManager;

/**
 * Handles all things SFX and effects.<br>
 * <br>
 * Converts between MineTunes shorthand names for SFX keyword and full effect
 * names for Minecraft; plays SFX and effects; loads and stores properties of
 * effects such as default center pitch; and manages conversion between
 * minecraft effect names and filenames.<br>
 * <br>
 * Glossary:<br>
 * <br>
 * Handle: a short name, such as "dogbark", which represents...<br>
 * Effect: a full sound effect name, such as newsound.mob.dog.bark, such as
 * Minecraft uses as a sound effect name<br>
 * Source: a number representing a set of sound effects; 0 is for the Alpha to
 * 1.3 sound folder, while 1 represents sound3.<br>
 * SFX: Sound Effect<br>
 * <br>
 * To Do: Fix the potential null pointers involving blind "sources.get(source)"
 * calls everywhere.
 * 
 */
public class SFXManager {

	private static Random rand = new Random();

	private static int latestSourceNum = 0;

	private static HashMap<Integer, SFXSource> sources = new HashMap<Integer, SFXSource>();

	public static void load() throws IOException, SAXException {
		// Load general config info
		NodeList configNodes = parseXMLStream(ResourceManager
				.getResource("sfx/sfxManagerConfig.xml"));
		configNodes = DOMUtil.findFirstElement("sfxConfig", configNodes).getChildNodes();

		// Read the number of the latest source for sound effects in this
		// version of Minecraft
		Element latestSourceNode = DOMUtil.findFirstElement("latestSource",
				configNodes);
		if (latestSourceNode != null) {
			latestSourceNum = DOMUtil.parseIntStringWithDefault(
					DOMUtil.getAttributeValue(latestSourceNode, "num"), 0);
		}

		// Load sfx sources
		LinkedList<Element> sourceElements = DOMUtil.findElements("source",
				DOMUtil.findFirstElement("sources", configNodes)
						.getChildNodes());
		for (Element e : sourceElements) {
			SFXSource newSource = SFXSource.loadFromElement(e);
			sources.put(newSource.num, newSource);
		}
	}

	/**
	 * Attempts to return the full MC name of a given SFX shorthand.
	 * 
	 * @param name
	 * @return the name, or null if not found
	 */
	public static String getEffectForShorthandName(String name, int source) {
		return sources.get(source).effectNames.get(name.toLowerCase());
	}

	public static void playEffectByShorthand(String name, int source) {
		Minecraft.getMinecraft().theWorld.playSoundAtEntity(
				Minecraft.getMinecraft().thePlayer,
				getEffectForShorthandName(name, source), 1.0f, 1.0f);
	}

	public static void playEffect(String mcName) {
		playEffect(mcName, 1.0f, 1.0f);
	}

	public static void playEffect(String mcName, float volume, float pitch) {
		Minecraft.getMinecraft().theWorld.playSoundAtEntity(
				Minecraft.getMinecraft().thePlayer, mcName, volume, pitch);
	}

	public static HashMap<String, String> getAllEffects(int source) {
		return (HashMap<String, String>) sources.get(source).effectNames
				.clone();
	}

	/**
	 * Determines if the given effect exists in /.minecraft/resources/newsound/
	 * 
	 * @param effectName
	 * @return
	 */
	public static boolean doesEffectExist(String effectName, int effectNumber,
			int source) {
		File effectFile = getEffectFile(effectName, effectNumber, source);
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
	 * @param source
	 *            The SFX source of the effect; needed to determine whether the
	 *            file is in newSound or sound3 etc.
	 * @return the effect's filename or null if the effect name is bad
	 */
	public static String getEffectFilename(String effectName, int effectNumber,
			int source) {
		// 0 implies the first sound effect available, but this function
		// requires an input of -1 or 1 and up. '0's are converted to 1.
		if (effectNumber == 0) {
			effectNumber = 1;
		}

		// Base filename, complete up to the newsound dir.
		StringBuilder filename = new StringBuilder()
				.append(Minecraft.getMinecraft().getMinecraftDir())
				.append(File.separator).append("resources")
				.append(File.separator)
				.append(sources.get(source).resourceFolder)
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
	public static File getEffectFile(String effectName, int effectNumber,
			int source) {
		return new File(getEffectFilename(effectName, effectNumber, source));
	}

	/**
	 * Get the semitone-number format of the center pitch of the effect
	 * 
	 * @param effectName
	 * @param sfxNumber
	 *            >= 1; if not, it is rounded up
	 * @return null if effect has no default center pitch
	 */
	public static Integer getDefaultTuningInt(String effectName, int sfxNumber,
			int source) {
		if (sfxNumber < 1) {
			sfxNumber = 1;
		}
		return sources.get(source).effectTuningsInt.get(effectName + sfxNumber);
	}

	/**
	 * Get the string version of the effect's center pitch; e.g. "F#3"
	 * 
	 * @param effectName
	 * @param sfxNumber
	 *            >= 1; if not, it is rounded up
	 * @return null if effect has no default center pitch
	 */
	public static String getDefaultTuningString(String effectName,
			int sfxNumber, int source) {
		if (sfxNumber < 1) {
			sfxNumber = 1;
		}
		return sources.get(source).effectTuningsString.get(effectName
				+ sfxNumber);
	}

	/**
	 * Returns whether a given SFX is flagged not to be used by SFXInst (usually
	 * due to decoder bugginess... grr!)
	 * 
	 * @param shorthand
	 *            not case sensitive
	 * @return
	 */
	public static boolean isShorthandOnSFXInstBlacklist(String shorthand,
			int source) {
		for (String s : sources.get(source).sfxBlackListShorthands) {
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
	public static boolean isEffectOnSFXInstBlacklist(String effect, int source) {
		for (String s : sources.get(source).sfxBlackListEffects) {
			if (s.equals(effect)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @return the number to give methods with "source" parameters that
	 *         represents the latest source of SFX files; sound3 for Minecraft
	 *         1.4.x
	 */
	public static int getLatestSource() {
		return latestSourceNum;
	}

	/**
	 * Returns the number of meow1.ogg, meow2.ogg, meow3.ogg, etc. alternatives
	 * a given effect has. Checks the filesystem, so this may be slightly
	 * expensive.
	 * 
	 * @param effect
	 * @return between 0 and 9. (0 signifies none found, 9 is the maximum this
	 *         will check for).
	 */
	public static int getNumberOfAlternativesForEffect(String effect, int source) {
		int found = 0;
		for (int i = 1; i <= 9; i++) {
			if (doesEffectExist(effect, i, source)) {
				// Try another.
				found++;
			} else {
				// Stop trying.
				break;
			}
		}
		return found;
	}

	/**
	 * Reads the input stream as a CSV list, but with a customizable regex
	 * between the values. When the line containing endLineText
	 * (non-case-sensitive) is reached, the stream is closed.
	 * 
	 * @param separatorRegex
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static LinkedList<String[]> readSeparatedList(String separatorRegex,
			InputStream in, String endLineText) throws IOException {
		LinkedList<String[]> values = new LinkedList<String[]>();

		// Set up a text file reader
		BufferedReader listIn = new BufferedReader(new InputStreamReader(in));

		// Read values
		String readLine;
		while (true) {
			readLine = listIn.readLine();

			if (readLine == null) {
				break;
			} else if (readLine.equalsIgnoreCase(endLineText)) {
				break;
			} else {
				// split line by the regex
				String[] lineValues = readLine.split(separatorRegex);
				values.add(lineValues);
			}
		}

		// Close stream
		listIn.close();

		// Return read values
		return values;
	}

	public static NodeList parseXMLStream(InputStream stream)
			throws IOException, SAXException {
		// Parse the XML ditty data
		// Create the document builder
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}

		// Parse
		Document bookDocument = documentBuilder.parse(stream);

		// Normalize and return the read xml document
		if (bookDocument != null) {
			bookDocument.getDocumentElement().normalize();
			return bookDocument.getChildNodes();
		} else {
			return null;
		}
	}

	/**
	 * Returns an array of all sfx source numbers loaded
	 * @return
	 */
	public static int[] getSourceNums() {
		LinkedList<Integer> l = new LinkedList<Integer>();
		for (Integer s : sources.keySet()) {
			l.add(s);
		}

		int[] nums = new int[l.size()];
		int count = 0;
		for (int i : l) {
			nums[count] = i;
			count++;
		}
		return nums;
	}
	
	public static String getSourceName (int source) {
		return sources.get(source).displayName;
	}
	
	public static boolean isValidSourceNum (int sourceNum) {
		SFXSource ifThisIsNullThenInvalidNum = sources.get(sourceNum);
		return (ifThisIsNullThenInvalidNum != null);
	}

	private static class SFXSource {

		public int num;
		public String displayName = "";
		public String resourceFolder = "newsound";
		public String dataFilePrefix = "";

		public HashMap<String, String> effectNames = new HashMap<String, String>();
		public HashMap<String, String> effectTuningsString = new HashMap<String, String>();
		public HashMap<String, Integer> effectTuningsInt = new HashMap<String, Integer>();

		public HashSet<String> sfxBlackListEffects = new HashSet<String>();
		public HashSet<String> sfxBlackListShorthands = new HashSet<String>();

		private SFXSource() {

		}

		public static SFXSource loadFromElement(Element element) {
			SFXSource source = new SFXSource();

			// Load settings for this source from the config
			source.num = DOMUtil.parseIntStringWithDefault(
					DOMUtil.getAttributeValue(element, "num"), 0);
			source.displayName = DOMUtil.getAttributeValue(element, "name");
			source.resourceFolder = DOMUtil
					.getAttributeValue(element, "folder");
			source.dataFilePrefix = DOMUtil.getAttributeValue(element,
					"dataFilePrefix");

			// Load maps' and sets' data from file

			// Load effect names
			try {
				// System.out.println("MineTunes: Loading sfx names");
				LinkedList<String[]> values = readSeparatedList(
						":",
						ResourceManager.getResource("sfx/"
								+ source.dataFilePrefix + "SfxNames.txt"),
						"end of effects");

				for (String[] v : values) {
					// Store the effect name for the given shorthand
					//System.out.println (v[0]);
					source.effectNames.put(v[0], v[1]);

					// Check for third part
					if (v.length >= 3) {
						// Entry has a third part
						if (v[2].equals("NoSFXInst")) {
							source.sfxBlackListEffects.add(v[1]);
							source.sfxBlackListShorthands.add(v[0]);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Load default center pitches
			try {
				LinkedList<String[]> values = readSeparatedList(
						":",
						ResourceManager.getResource("sfx/"
								+ source.dataFilePrefix + "EffectPitches.txt"),
						"end of effects");
				for (String[] v : values) {
					// Store the effect name for the given shorthand
					source.effectTuningsString.put(v[0], v[1]);
					int pitchValue = Note.createNote(v[1]).getValue();
					source.effectTuningsInt.put(v[0], pitchValue);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return source;
		}

	}

}
