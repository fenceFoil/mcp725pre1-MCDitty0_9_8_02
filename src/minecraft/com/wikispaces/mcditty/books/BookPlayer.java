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
package com.wikispaces.mcditty.books;

import java.util.LinkedList;

import net.minecraft.src.BlockSign;
import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.ItemStack;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.wikispaces.mcditty.GetMinecraft;
import com.wikispaces.mcditty.MCDitty;
import com.wikispaces.mcditty.Point3D;
import com.wikispaces.mcditty.config.MCDittyConfig;
import com.wikispaces.mcditty.ditty.Ditty;
import com.wikispaces.mcditty.ditty.DittyPlayerThread;
import com.wikispaces.mcditty.ditty.event.SFXMCDittyEvent;
import com.wikispaces.mcditty.dittyXML.DittyXMLParser;
import com.wikispaces.mcditty.dittyXML.DittyXMLPartElement;
import com.wikispaces.mcditty.dittyXML.MissingContainerException;
import com.wikispaces.mcditty.dittyXML.UnnamedLyricsException;
import com.wikispaces.mcditty.dittyXML.UnnumberedLyricException;
import com.wikispaces.mcditty.particle.ParticleRequest;

/**
 * Provides methods to play music from the texts of writable books.
 * 
 * 
 */
public class BookPlayer {
	public static void playHeldBook(final EntityClientPlayerMP player) {
		Thread t = new Thread (new Runnable() {

			@Override
			public void run() {
				doPlayHeldBook(player);
			}
			
		});
		t.setName("MCDitty Held Book Player");
		t.start();
	}
	
	public static void doPlayHeldBook(EntityClientPlayerMP player) {
		//System.out.println("PlayBookInHand called.");

		// Get book in hand
		ItemStack heldBookItem = player.getCurrentEquippedItem();

		// Wrap the book for easier access to text
		WrappedBook book = WrappedBook.wrapBook(heldBookItem);

		if (book == null) {
			// item given was not a writable book
			return;
		}
		
		// Give confirmation click
		MCDitty.executeTimedDittyEvent(new SFXMCDittyEvent("random.click", 0, 0));

		//System.out.println("Parsing book...");

		// Attempt to parse the book's DittyXML container
		LinkedList<Element> heldBookDittyXMLContainers;
		try {
			heldBookDittyXMLContainers = BookDittyXMLParser
					.getDittyXMLContainers(book);
		} catch (SAXException e1) {
			// Error parsing XML.
			e1.printStackTrace();
			BlockSign.writeChatMessage(
					GetMinecraft.instance().theWorld,
					"Book contains XML problems. This might help: "
							+ e1.getMessage());
			return;
		}

		// Handle any issues
		if (heldBookDittyXMLContainers == null) {
			// Error parsing XML
			BlockSign.writeChatMessage(GetMinecraft.instance().theWorld,
					"Book contains XML problems.");
			return;
		} else if (heldBookDittyXMLContainers.size() <= 0) {
			// No DittyXML containers in book
			BlockSign.writeChatMessage(GetMinecraft.instance().theWorld,
					"Book does not contain a Ditty.");
			return;
		}

		// TODO: Check version, check whether book is in a set, etc.
		DittyXMLPartElement firstBookPart = DittyXMLParser
				.getPartElementFromDittyXMLContainer(heldBookDittyXMLContainers
						.get(0));

		// Set up the list of every DittyXML container in the ditty.
		LinkedList<Element> allElementsInDitty = new LinkedList<Element>();
		allElementsInDitty.add(heldBookDittyXMLContainers.get(0));

		if (firstBookPart == null) {
			// Assume set is complete, as it is only one container long
			//System.out
			//		.println("Assuming no more containers: firstBook's part == null");
		} else {
			// Part is present. Try to figure out whether we need more parts to
			// the set.
			if (firstBookPart.getOf() > 1) {
				// Multiple parts required. Keep searching!

				// Make a list of the parts that still need finding
				Element[] partsFound = new Element[firstBookPart.getOf()];
				// Mark off the part noted in the starting book
				if (firstBookPart.getPart() > firstBookPart.getOf()) {
					// This is impossible in theory... well, it will be now.
					// Throw an error.
					BlockSign
							.writeChatMessage(
									GetMinecraft.instance().theWorld,
									"Book claims to be part "
											+ firstBookPart.getPart()
											+ " of "
											+ firstBookPart.getOf()
											+ ". Fix this inconsistency before playing the book.");
				}
				partsFound[firstBookPart.getPart() - 1] = heldBookDittyXMLContainers
						.get(0);

				// Search for the remaining parts in books from the player's
				// inventory
				ItemStack[] inventory = player.inventory.mainInventory;
				for (ItemStack itemStack : inventory) {
					// Has this item already been read as it was in your hand?
					if (itemStack == heldBookItem) {
						//System.out.println("*** Book in hand being skipped...");
						continue;
					}

					// Is the item a book?
					WrappedBook invBook = WrappedBook.wrapBook(itemStack);
					if (invBook == null) {
						continue;
					}

					// Parse for DittyXML containers
					LinkedList<Element> invBookDittyXMLContainers = null;
					try {
						invBookDittyXMLContainers = BookDittyXMLParser
								.getDittyXMLContainers(invBook);
					} catch (SAXException e) {
						// TODO: Error parsing XML. Do not show a message.
						e.printStackTrace();
					}

					if (invBookDittyXMLContainers == null
							|| invBookDittyXMLContainers.size() < 1) {
						// No valid DittyXML containers found in book
						continue;
					}

					// Try to find Part elements
					DittyXMLPartElement invBookPartElement = DittyXMLParser
							.getPartElementFromDittyXMLContainer(invBookDittyXMLContainers
									.get(0));
					if (invBookPartElement == null) {
						// No part element in DittyXML container
						continue;
					}

					// Finally, try to decide if this new book is a part of the
					// current ditty
					if (invBookPartElement.getSet().equals(
							firstBookPart.getSet())) {
						// Is part of current set
						if (invBookPartElement.getVersion() == firstBookPart
								.getVersion()) {
							// Is of the same version
							if (invBookPartElement.getOf() != firstBookPart
									.getOf()) {
								// New book makes a conflicting claim that the
								// ditty is split across a different number of
								// parts.

								// Error
								BlockSign
										.writeChatMessage(
												GetMinecraft.instance().theWorld,
												"A book in your inventory, a part of this ditty, claims that there are "
														+ invBookPartElement
																.getOf()
														+ " books in the ditty while the book in your hand claims that there are "
														+ firstBookPart.getOf()
														+ ". Fix this inconsistency before playing the ditty.");
								return;
							} else {
								// New book is a part of the current ditty
								// Check that getPart is in range
								if (invBookPartElement.getPart() > invBookPartElement
										.getOf()) {
									// Out of range. Tsk tsk.
									BlockSign
											.writeChatMessage(
													GetMinecraft.instance().theWorld,
													"A book in your inventory, a part of this ditty, claims to be part "
															+ firstBookPart
																	.getPart()
															+ " of "
															+ firstBookPart
																	.getOf()
															+ ". Fix this inconsistency before playing the book.");
									return;
								}
								// Check that part is not overwriting a
								// previously found part
								if (partsFound[invBookPartElement.getPart() - 1] != null) {
									// Part would overwrite an existing part!
									BlockSign
											.writeChatMessage(
													GetMinecraft.instance().theWorld,
													"Two books in your inventory both claim to be part "
															+ firstBookPart
																	.getPart()
															+ " of "
															+ firstBookPart
																	.getOf()
															+ ". Fix this inconsistency before playing the ditty.");
									return;
								}
								// Mark part as found
								partsFound[invBookPartElement.getPart() - 1] = invBookDittyXMLContainers
										.get(0);

								// Check whether all parts have been found
								if (arrayHasNoNulls(partsFound)) {
									// Stop looking
									break;
								}
							}
						}
					}
				}

				// Finished searching inventory.
				// Have all parts been found?
				if (arrayHasNoNulls(partsFound)) {
					// All have been found! Hurrah!
					//System.out.println("All parts found!");
					// Clear the item in your hand from the list if it has
					// already been added
					allElementsInDitty.clear();
					// Fill list
					for (int i = 0; i < partsFound.length; i++) {
						// Add all parts of ditty
						allElementsInDitty.add(partsFound[i]);
					}
				} else {
					// Not all have been found. Show error
					// Show error, noting which are missing
					LinkedList<Integer> missingParts = new LinkedList<Integer>();
					for (int i = 0; i < partsFound.length; i++) {
						if (partsFound[i] == null) {
							// Missing part
							missingParts.add(i + 1);
						}
					}
					// Show an error according to the number missing
					if (missingParts.size() == 1) {
						BlockSign.writeChatMessage(
								GetMinecraft.instance().theWorld,
								"You are missing one book in your ditty: Part "
										+ missingParts.get(0) + ".");
						return;
					} else if (missingParts.size() == 2) {
						BlockSign.writeChatMessage(
								GetMinecraft.instance().theWorld,
								"You are missing two books in your ditty: Parts "
										+ missingParts.get(0) + " and "
										+ missingParts.get(1) + ".");
						return;
					} else {
						StringBuffer message = new StringBuffer();
						message.append("You are missing ")
								.append(Integer.toString(missingParts.size()))
								.append(" books in your ditty: Parts ");
						for (int i = 0; i < missingParts.size() - 1; i++) {
							message.append(
									Integer.toString(missingParts.get(i)))
									.append(", ");
						}
						message.append("and ")
								.append(missingParts.get(
										missingParts.size() - 1).toString())
								.append(".");
						BlockSign.writeChatMessage(
								GetMinecraft.instance().theWorld,
								message.toString());
						return;
					}
				}
			}
		}

		//System.out.println("No errors: showing success...");
		// Show success
		// GetMinecraft.instance().displayGuiScreen(
		// new ParsedBookErrorGui(parsedBook));

		// // Play ditty
		// if (parsedBook.getDitty() != null) {
		// DittyPlayerThread t = new DittyPlayerThread(parsedBook.getDitty());
		// t.start();
		// }

		// Parse ditty from containers
		Ditty ditty = null;
		try {
			ditty = DittyXMLParser.parseDittyXMLContainers(allElementsInDitty);
		} catch (MissingContainerException e) {
			// TODO Auto-generated catch block
			// This had sure as hell better not be happening after all that code
			// I wrote above.
			e.printStackTrace();
			return;
		} catch (UnnamedLyricsException e) {
			// TODO Announce to player
			e.printStackTrace();
			return;
		} catch (UnnumberedLyricException e) {
			// TODO Announce to player
			e.printStackTrace();
			return;
		}

		// Play ditty
		if (ditty != null) {
			// Check for errors
			if (ditty.getErrorMessages().size() > 0) {
				// Uh oh: errors!
				// Show chat messages: first handle the buffer, cutting it down
				// to
				// one if that option is enabled
				LinkedList<String> chatMessageBuffer = ditty.getErrorMessages();

				if (MCDittyConfig.onlyFirstErrorShown
						&& chatMessageBuffer.size() > 0) {
					// If we only show the first, discard the rest and create a
					// new
					// buffer with just the first in it
					String firstMessage = chatMessageBuffer.get(0);
					chatMessageBuffer = new LinkedList<String>();
					chatMessageBuffer.add(firstMessage);
				}

				// Then find the player, and empty the message buffer into his
				// chat.
				for (String s : chatMessageBuffer) {
					BlockSign.writeChatMessage(
							GetMinecraft.instance().theWorld, s);
				}

				if (chatMessageBuffer.size() > 0) {
					// Emit error particles
					if (MCDittyConfig.particlesEnabled) {
						for (int m = 0; m < 9; m++) {
							for (int i = 0; i < 5; i++) {
								EntityClientPlayerMP p = GetMinecraft
										.instance().thePlayer;
								MCDitty.requestParticle(new ParticleRequest(
										new Point3D((int) p.posX + (m % 3 - 1),
												(int) p.posY, (int) p.posZ
														+ (m / 3 - 1)), "smoke"));
							}
						}
					}
				}
				// Don't play after all :(
				return;
			}

			// Let 'er rip!
			DittyPlayerThread t = new DittyPlayerThread(ditty);
			t.start();
		}
	}

	/**
	 * Checks that an array is completely composed of non-null elements.
	 * 
	 * @param partsFound
	 * @return
	 */
	private static boolean arrayHasNoNulls(Object[] partsFound) {
		for (int i = 0; i < partsFound.length; i++) {
			if (partsFound[i] == null) {
				return false;
			}
		}
		return true;
	}
}
