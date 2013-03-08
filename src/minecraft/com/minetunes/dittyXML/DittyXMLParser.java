/**
 * 
 * Copyright (c) 2012 William Karnavas All Rights Reserved
 * 
 */

/**
 * 
 * This file is part of MineTunes.
 * 
 * MineTunes is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * MineTunes is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MineTunes. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.minetunes.dittyXML;

import java.io.File;
import java.util.LinkedList;

import org.jfugue.elements.Note;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.minetunes.CueScheduler;
import com.minetunes.DOMUtil;
import com.minetunes.config.MinetunesConfig;
import com.minetunes.ditty.Ditty;
import com.minetunes.ditty.event.SFXInstrumentEvent;
import com.minetunes.ditty.event.SFXInstrumentOffEvent;
import com.minetunes.sfx.SFXManager;
import com.minetunes.signs.SignTuneParser;

/**
 * Accepts a list of DittyXML container elements, and creates a Ditty from them.
 */
public class DittyXMLParser {

	/**
	 * Attempts to parse the given DittyXML container.
	 * 
	 * @param dittyXMLContainer
	 *            Null elements are allowed.
	 * @param ditty
	 *            a ditty to append read music to, or null to create a new one
	 * @return null if there is no ditty in the first container in the list, or
	 *         if dittyXMLContainers is null or empty.
	 * @throws UnnumberedLyricException
	 * @throws UnnamedLyricsException
	 */
	public static Ditty parseDittyXMLContainer(Element dittyXMLContainer,
			Ditty ditty) throws MissingContainerException,
			UnnamedLyricsException, UnnumberedLyricException {
		LinkedList<Element> l = new LinkedList();
		l.add(dittyXMLContainer);
		return parseDittyXMLContainers(l, ditty);
	}

	/**
	 * Attempts to parse all given DittyXML containers for the ditty that begins
	 * in the first container in the list.
	 * 
	 * @param dittyXMLContainers
	 *            must be already put in order to be read in. Null elements are
	 *            allowed.
	 * @param ditty
	 *            a ditty to append read music to, or null to create a new one
	 * @return null if there is no ditty in the first container in the list, or
	 *         if dittyXMLContainers is null or empty.
	 * @throws UnnumberedLyricException
	 * @throws UnnamedLyricsException
	 */
	public static Ditty parseDittyXMLContainers(
			LinkedList<Element> dittyXMLContainers, Ditty ditty)
			throws MissingContainerException, UnnamedLyricsException,
			UnnumberedLyricException {
		// System.out.println("ParseDittyXMLContainers called");

		if (dittyXMLContainers == null || dittyXMLContainers.size() < 1) {
			return null;
		}

		// Combine all elements from each container into one list
		LinkedList<Node> allNodes = new LinkedList<Node>();
		for (Element e : dittyXMLContainers) {
			if (e == null) {
				// System.out.println("Null container found.");
				continue;
			}
			// System.out.println("Container found: " + e.getNodeName() + " ("
			// + e.getChildNodes().getLength() + " children)");
			NodeList nodes = e.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				// System.out.println("Adding node: "
				// + nodes.item(i).getNodeName());
				allNodes.add(nodes.item(i));
			}
		}

		// Set up the ditty that will be parsed
		if (ditty == null) {
			ditty = new Ditty();
		}

		// TODO: Parse each element, referring each one to the appropriate parse
		// method
		LinkedList<Element> musicElements = new LinkedList<Element>();
		for (Node n : allNodes) {
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				// Parse an element
				String name = n.getNodeName();

				// Direct to correct parse method based on name
				if (name.equals("music")) {
					// System.out.println("Music element found!");
					musicElements.add((Element) n);
				} else if (name.equals("lyrics")) {
					// System.out.println("Lyrics element found!");
					parseLyricsElement((Element) n, ditty);
				}
			}
		}

		// Parse the discovered music nodes
		parseMusicNodes(musicElements, ditty);

		// TODO: Finalize and return the read Ditty
		return ditty;
	}

	private static void parseLyricsElement(Element element, Ditty ditty)
			throws UnnamedLyricsException, UnnumberedLyricException {
		// Fetch the Lyrics processor for the ditty
		CueScheduler lyricsStorage = ditty.getLyricsStorage();

		// Find attributes of element

		// Get lyrics' name
		String lyricsName = DOMUtil.getAttributeValue(element, "name");
		if (lyricsName == null) {
			// If there is no name for this lyrics element, throw an exception
			throw new UnnamedLyricsException();
		}

		// Get a prefix (optional) for the text of each lyric
		String lyricPrefix = DOMUtil.getAttributeValueOrDefault(element,
				"prefix", "");

		// Get a style (optional) for how the lyrics are stored in the element
		String style = DOMUtil.getAttributeValueOrDefault(element, "style",
				"lines");
		if (style.equalsIgnoreCase("tags")) {
			// Lyrics are stored one-per-child element
		} else if (style.equalsIgnoreCase("lines")) {
			// Lyrics are stored one-per-line in a text node
		} else {
			// Invalid type. Ignore lyric element
			// TODO: Throw error
			return;
		}

		// Add lyrics
		NodeList lyricNodes = element.getChildNodes();
		for (int i = 0; i < lyricNodes.getLength(); i++) {
			Node n = lyricNodes.item(i);
			if (style.equalsIgnoreCase("tags")) {
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					// Node is an element
					if (n.getNodeName().equals("l")) {
						// Node is a lyric element

						// Get lyric text
						StringBuilder lyricTextBuffer = new StringBuilder();
						lyricTextBuffer.append(lyricPrefix);

						// Assemble each part of the text
						NodeList lyricChildren = n.getChildNodes();
						for (int j = 0; j < lyricChildren.getLength(); j++) {
							Node child = lyricChildren.item(j);
							if (child.getNodeType() == Node.TEXT_NODE) {
								// Only read text
								lyricTextBuffer.append(child.getNodeValue());
							}
						}

						// Render buffer to string
						String lyricText = lyricTextBuffer.toString();

						// Look for the required "n" attribute
						Integer nAttribute = DOMUtil.parseIntString(DOMUtil
								.getAttributeValue((Element) n, "n"));
						if (nAttribute == null) {
							// N attribute not found
							// TODO: Error
							throw new UnnumberedLyricException(lyricText);
						} else {
							// Process lyric
							// Add lyric to lyricstorage
							lyricsStorage.addLyricText(lyricsName, lyricText,
									nAttribute);
						}
					}
				}
			} else if (style.equalsIgnoreCase("lines")) {
				if (n.getNodeType() == Node.TEXT_NODE) {
					int currLyric = 1;
					String[] lines = n.getNodeValue().split("\\n");
					for (String s : lines) {
						if (s != null && !s.trim().equals("")) {
							lyricsStorage.addLyricText(lyricsName, lyricPrefix
									+ s, currLyric);
						}
						currLyric++;
					}
				}
			}
		}
	}

	/**
	 * Attempts to find a part element as a child in a DittyXML container
	 * element, and if found returns that element's attributes in a
	 * DittyXMLPartElement object. If the part element is not found, returns
	 * null.
	 * 
	 * @param dittyXMLContainer
	 * @return
	 */
	public static DittyXMLPartElement getPartElementFromDittyXMLContainer(
			Element dittyXMLContainer) {
		LinkedList<Element> partElements = DOMUtil.findElements("part",
				dittyXMLContainer.getChildNodes());
		if (partElements.size() <= 0) {
			return null;
		} else {
			// Part element found!
			Element partElement = partElements.get(0);
			// Fill the returned part element object's fields
			DittyXMLPartElement e = new DittyXMLPartElement();
			e.setPart(DOMUtil.parseIntStringWithDefault(
					DOMUtil.getAttributeValueOrDefault(partElement, "p", "1"),
					1));
			e.setOf(DOMUtil.parseIntStringWithDefault(
					DOMUtil.getAttributeValueOrDefault(partElement, "of", "1"),
					1));
			e.setSet(DOMUtil.getAttributeValueOrDefault(partElement, "set", ""));
			e.setVersion(DOMUtil.parseIntStringWithDefault(
					DOMUtil.getAttributeValueOrDefault(partElement, "ver", "0"),
					0));
			return e;
		}
	}

	private static void parseMusicNodes(LinkedList<Element> musicNodes,
			Ditty ditty) {
		// Find the main music elements
		LinkedList<Element> mainNodes = new LinkedList<Element>();
		for (Element e : musicNodes) {
			String label = DOMUtil.getAttributeValue(e, "label");
			if (label == null) {
				// Main node found
				mainNodes.add(e);
			}
		}

		// If there is no start node, all music elements are irrelevant anyway,
		// so
		// don't bother doing anything more
		if (mainNodes.size() <= 0) {
			return;
		}

		// Parse the main nodes
		StringBuilder parsedMusicString = new StringBuilder();
		for (Element e : mainNodes) {
			parsedMusicString.append(parseMusicNode(e, ditty));
		}

		// Save musicstring in the ditty
		ditty.setMusicString(parsedMusicString.toString());
	}

	// TODO: Parse music string tokens similarly to sign parsing
	// TODO: Implement infinite loop prevention
	public static String parseMusicNode(Element parseNode, Ditty ditty) {
		if (parseNode == null) {
			return null;
		}

		NodeList subNodes = parseNode.getChildNodes();
		StringBuilder musicStringBuffer = new StringBuilder();

		// Iterate over every child node of a music node
		for (int nodeIndex = 0; nodeIndex < subNodes.getLength(); nodeIndex++) {
			Node currNode = subNodes.item(nodeIndex);
			int currNodeType = currNode.getNodeType();

			// Sort nodes by type
			if (currNodeType == Node.TEXT_NODE) {
				// Filter newlines from MusicString
				String s = currNode.getNodeValue();
				if (s.contains("\n")) {
					// Remove
					s = s.replaceAll("\n", " ");
				}
				// Text is read as music.
				ditty.addMusicStringTokens(musicStringBuffer, s, true);
			} else if (currNodeType == Node.ELEMENT_NODE) {
				// If an element, go by name
				String currNodeName = currNode.getNodeName();
				Element currElement = (Element) currNode;
				if (currNodeName.equals("reset")) {
					// Reset element
					// Add reset token to musicstring
					ditty.addMusicStringTokens(musicStringBuffer,
							SignTuneParser.getResetToken(ditty), false);
				} else if (currNodeName.equals("syncVoices")) {
					// SyncVoices element
					// Add token to musicstring
					ditty.addMusicStringTokens(musicStringBuffer,
							SignTuneParser.SYNC_VOICES_TOKEN, false);
				} else if (currNodeName.equals("syncWith")) {
					// SyncWith element
					// Add syncwith token to musicstring
					// TODO: Range checks
					int layer = DOMUtil.parseIntStringWithDefault(DOMUtil
							.getAttributeValueOrDefault(currElement, "l",
									"-1000"), -1000);
					int voice = DOMUtil.parseIntStringWithDefault(DOMUtil
							.getAttributeValueOrDefault(currElement, "v",
									"-1000"), -1000);

					// If voice is undefined, throw error
					if (voice == -1000) {
						ditty.addErrorMessage("A <syncWith> element lacks a voice. The \"v\" attribute is required.");
					}

					// Finally, add token
					if (layer != -1000) {
						ditty.addMusicStringTokens(musicStringBuffer,
								SignTuneParser.SYNC_WITH_TOKEN + "V" + voice
										+ "L" + layer, false);
					} else {
						ditty.addMusicStringTokens(musicStringBuffer,
								SignTuneParser.SYNC_WITH_TOKEN + "V" + voice
										+ "Lu", false);
					}
				} else if (currNodeName.equals("volume")) {
					// Volume element
					int percent = DOMUtil.parseIntStringWithDefault(DOMUtil
							.getAttributeValueOrDefault(currElement, "percent",
									"-1000"), -1000);

					// If volume is undefined, throw error
					if (percent == -1000) {
						ditty.addErrorMessage("A <volume> element lacks a percent volume. The \"percent\" attribute is required.");
					} else if (percent < 0 || percent > 100) {
						ditty.addErrorMessage("A <volume> element request a volume of "
								+ percent + "%, which is impossible.");
					}

					// Finally, add token
					ditty.addMusicStringTokens(musicStringBuffer,
							SignTuneParser.getAdjustedVolumeToken(percent,
									ditty), false);
				} else if (currNodeName.equals("repeat")) {
					// Repeat element
					// Look for times to repeat
					String timesString = DOMUtil.getAttributeValue(currElement,
							"x");
					int times = 1;
					if (timesString == null) {
						timesString = DOMUtil.getAttributeValue(currElement,
								"times");
					}
					times = DOMUtil.parseIntStringWithDefault(timesString, 1);

					// Look for label to repeat
					// TODO: Unimplemented

					// Repeat contents x times
					for (int i = 0; i < times; i++) {
						// can't simply copy musicstring x times, since
						// cooresponding events and messages etc. would not be
						// duplicated in the ditty
						String musicString = parseMusicNode(currElement, ditty);
						if (musicString != null) {
							ditty.addMusicStringTokens(musicStringBuffer,
									musicString, false);
						}
					}
				} else if (currNodeName.equals("sfxInst")) {

					// Get tuning attiribute (either t or tuning)
					String tuningString = DOMUtil.getAttributeValue(
							currElement, "t");
					int tuning = 0;
					if (tuningString == null) {
						tuningString = DOMUtil.getAttributeValue(currElement,
								"tuning");
					}
					// Check for validity
					if (tuningString == null) {
						// Assign default tuning
						tuningString = "default";
					}
					// Parse, depending on whether the tuning was a name or
					// number
					if (tuningString.matches("\\d+")) {
						tuning = DOMUtil.parseIntString(tuningString);
					} else if (tuningString.toLowerCase().matches(
							"[abcdefg][#b]*[\\d]?")) {
						// Check for a note (C#3, D, G5)
						// If found, parse it
						Note note = Note.createNote(tuningString);
						// Save the pitch value
						tuning = note.getValue();
					}

					// range check the tuning
					if (tuning < 0 || tuning > 127) {
						ditty.addErrorMessage("A <sfxInst> element tried to tune to "
								+ tuning
								+ ", which is out of range (0-127 are valid).");
						// Arbitrary
						tuning = 64;
					}

					// Get the SFX source folder (optional; default is alpha
					// thru 1.3)
					int sfxSource = DOMUtil
							.parseIntStringWithDefault(DOMUtil
									.getAttributeValue(currElement, "source"),
									0);

					// Get the instrument number to swap in (required)
					String instString = DOMUtil.getAttributeValue(currElement,
							"i");
					Integer instrument = 0;
					if (instString == null) {
						// Instrument not given
						ditty.addErrorMessage("A <sfxInst> element lacks the 'i' attribute, and doesn't specify what instrument to replace.");
					} else {
						instrument = DOMUtil.parseIntString(instString);
						if (instrument == null || instrument < 0
								|| instrument > 127) {
							ditty.addErrorMessage("A <sfxInst> element does not specify a valid instrument number (from 0 to 127)");
							instrument = 0;
						}
					}

					// Get the sfx name (required)
					String sfxNameAttr = DOMUtil.getAttributeValue(currElement,
							"sfx");
					int sfxNumber = 1;
					String sfxFilename = "";
					String sfxName = "";
					String sfxNameIncomplete = "";
					if (sfxNameAttr == null) {
						ditty.addErrorMessage("A <sfxInst> element is missing the 'sfx=' attribute.");
					} else {
						// Always set the incomplete name, whatever else betide
						sfxNameIncomplete = sfxNameAttr.replaceAll("\\d", "");

						// Check for valid SFX name, stripping any digits first.
						// XXX: Could cause bugs by removing digits in middle of
						// shorthand
						String fullSFXName = SFXManager
								.getEffectForShorthandName(
										sfxNameAttr.replaceAll("\\d", ""),
										sfxSource);

						if (fullSFXName == null) {
							// No SFX by that name.
							if (sfxNameAttr.replaceAll("\\d", "").length() <= 0) {
								// No SFX ever given
								ditty.addErrorMessage("A <sfxInst> element has an empty 'sfx=' attribute.");

							} else {
								// SFX does not exist
								// TODO: Warning (see SFXinst keyword)
							}
						} else {
							// SFX is valid; set up

							// Decide what sound effect number to ask for, based
							// on the
							// digit or absence of a digit at the end of the sfx
							// name
							char digitChar = sfxNameAttr.charAt(sfxNameAttr
									.length() - 1);
							String digitString = new StringBuilder().append(
									digitChar).toString();

							int digit = 1;
							if (digitString.matches("[1234567890]")) {
								// Last character is a digit!
								digit = Integer.parseInt(digitString);
							}

							// Save the SFX number
							sfxNumber = digit;

							// Get the filename of the sfx
							File sfxFile = SFXManager.getEffectFile(
									fullSFXName, digit, sfxSource);
							if (!sfxFile.exists()) {
								// If the sfx filename does not exist
								// Try resetting the digit to 0
								sfxFile = SFXManager.getEffectFile(fullSFXName,
										1, sfxSource);
								// Update the saved SFX number to reflect this
								sfxNumber = 1;
							}
							sfxFilename = sfxFile.getPath();
						}

						// Save original SFX name
						sfxName = sfxNameAttr.replaceAll("\\d", "");

						// Check that the SFX is not an out-of-order SFX
						if (SFXManager.isShorthandOnSFXInstBlacklist(sfxName,
								sfxSource)) {
							// blacklisted as out of order
							ditty.addErrorMessage("A <sfxInst> element uses the sfx "
									+ sfxName
									+ ", which is out of order in "
									+ MinetunesConfig.CURRENT_VERSION
									+ ". Try another SFX instead.");
						}
					}

					if (tuningString.equals("default")) {
						// Try to get the default center pitch if it is not
						// given
						Integer defaultCenterPitch = SFXManager
								.getDefaultTuningInt(SFXManager
										.getEffectForShorthandName(sfxName,
												sfxSource), sfxNumber,
										sfxSource);
						if (defaultCenterPitch != null) {
							tuning = defaultCenterPitch;
						} else {
							// No center pitch default given
							ditty.addErrorMessage("<sfxInst> ("
									+ sfxName
									+ "): No default tuning is given for this SFX; please specify oen with the 'tuning=' attribute.");
							tuning = 60;
						}
					}

					// Add event
					int eventID = ditty.addDittyEvent(new SFXInstrumentEvent(
							instrument.intValue(), sfxFilename, sfxName,
							sfxNameIncomplete, sfxNumber, tuning, -1, ditty
									.getDittyID(), 0));
					ditty.addMusicStringTokens(musicStringBuffer,
							SignTuneParser.TIMED_EVENT_TOKEN + eventID, false);
				} else if (currNodeName.equals("sfxInstOff")) {
					int instrument = 0;

					// Get the instrument number to turn off
					String instAttr = DOMUtil.getAttributeValue(currElement,
							"i");
					if (instAttr == null) {
						ditty.addErrorMessage("A <sfxInstOff> element lacks an 'i=' attribute (instrument to turn off).");
					} else {
						// Instrument number given
						String argument = instAttr;
						if (argument.trim().matches("\\d+")) {
							instrument = Integer.parseInt(argument);
						} else {
							ditty.addErrorMessage("A <sfxInstOff> element has a non-number in its 'i=' attribute.");
						}

						if (instrument < 0 || instrument > 127) {
							// Out of bounds
							ditty.addErrorMessage("A <sfxInstOff> element speficies an instrument that is out of range (0 to 127 are valid).");
						}
					}

					// Create event
					// Add keyword to schedule
					int eventID = ditty
							.addDittyEvent(new SFXInstrumentOffEvent(
									instrument, -1, ditty.getDittyID()));
					ditty.addMusicStringTokens(musicStringBuffer,
							SignTuneParser.TIMED_EVENT_TOKEN + eventID, false);
				}
			}
		}

		// Return finished MusicString
		// System.out.println("Parsed: " + musicStringBuffer.toString());
		return musicStringBuffer.toString();
	}

	// TODO: Parse music string tokens similarly to sign parsing
	// private static String parseMusicNode(Node parseNode, Ditty ditty,
	// NodeList allMusicNodes) {
	// if (parseNode == null) {
	// return null;
	// }
	//
	// NodeList musicNodes = parseNode.getChildNodes();
	//
	// StringBuffer musicStringBuffer = new StringBuffer();
	//
	// // Iterate over every part of a music node
	// for (int nodeIndex = 0; nodeIndex < musicNodes.getLength(); nodeIndex++)
	// {
	// Node node = musicNodes.item(nodeIndex);
	// String nodeName = node.getNodeName();
	//
	// // Handle nodes by name
	// if (nodeName.equalsIgnoreCase("#text")) {
	// // Node is musicstring
	// // Add to read musicString
	// musicStringBuffer.append(" ").append(node.getNodeValue());
	// } else if (node instanceof Element) {
	// Element element = (Element) node;
	// if (nodeName.equalsIgnoreCase("pattern")) {
	// // Parse a pattern by recursively calling this function on a
	// // node
	// String label = DOMUtil.getAttributeValue(element, "label");
	// if (label == null) {
	// // TODO: Error if no label given
	// continue;
	// }
	//
	// String patternMusicString = parseMusicNode(
	// DOMUtil.findFirstElementWithAttribute(
	// allMusicNodes, "music", "label", label),
	// ditty, allMusicNodes);
	// if (patternMusicString != null) {
	// // Add the labeled music section's musicString to the
	// // buffer "times" times
	// // TODO: Add times fucntion
	// musicStringBuffer.append(patternMusicString);
	// }
	// } else if (nodeName.equalsIgnoreCase("reset")) {
	// // Add reset token to musicString
	// musicStringBuffer.append(BlockSignMinetunes.getResetToken(ditty));
	// }
	// }
	// }
	//
	// // Return our read musicString
	// return musicStringBuffer.toString();
	// }

}
