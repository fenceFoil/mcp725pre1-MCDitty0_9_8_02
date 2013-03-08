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
package com.minetunes.signs;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntitySign;
import net.minecraft.src.World;

import org.jfugue.Player;
import org.jfugue.parsers.MusicStringParser;

import com.minetunes.Minetunes;
import com.minetunes.PlayDittyFromSignWorkThread;
import com.minetunes.Point3D;
import com.minetunes.RightClickCheckThread;
import com.minetunes.config.MinetunesConfig;
import com.minetunes.ditty.Ditty;
import com.minetunes.ditty.DittyPlayerThread;
import com.minetunes.ditty.event.NoteStartEvent;
import com.minetunes.ditty.event.VolumeEvent;
import com.minetunes.gui.signEditor.GuiEditSignBase;
import com.minetunes.particle.MinetunesParticleRequest;
import com.minetunes.particle.ParticleRequest;
import com.minetunes.signs.keywords.EndKeyword;
import com.minetunes.signs.keywords.EndLineKeyword;
import com.minetunes.signs.keywords.GotoKeyword;
import com.minetunes.signs.keywords.PattKeyword;
import com.minetunes.signs.keywords.PatternKeyword;
import com.minetunes.signs.keywords.SignTuneKeyword;

/**
 * Contains members which were previously located in
 * net.minecraft.src.BlockSign. NOT used to replace the vanilla BlockSign for
 * the fields Block.signWall and Block.signPost, does NOT extend BlockSign
 * 
 */
public class SignTuneParser {

	private static boolean isMinetunesLoaded = false;
	private static Random random = new Random();
	private static LinkedList<MaxPlaysLockPoint> maxPlaysLockPoints = new LinkedList<MaxPlaysLockPoint>();

	/** Chars that can be parts of color codes */
	private final static String[] colorCodeChars = { "0", "1", "2", "3", "4",
			"5", "6", "7", "8", "9", "a", "A", "b", "B", "c", "C", "d", "D",
			"e", "E", "f", "F", "k", "K", "l", "L", "m", "M", "n", "N", "o",
			"O", "r", "R" };
	/**
	 * The ID of the next subpattern to be played; for use in finding infinite
	 * loops and identifying unique subpatterns
	 */
	private static int nextSubpatternID = 0;
	private static final int LINES_ON_A_SIGN = 4;

	/**
	 * List of signs under a OneAtATime block from being activated.
	 */
	public static final LinkedList<Point3D> oneAtATimeSignsBlocked = new LinkedList<Point3D>();
	public static final int FACES_SOUTH = 0;
	public static final int FACES_WEST = 1;
	public static final int FACES_NORTH = 2;
	public static final int FACES_EAST = 3;
	public static final int FACES_NON_CARDINAL = FACES_NORTH; // TODO: Does
	// this make
	// sense? No. It
	// simply averts
	// a glitch
	// temporarily.

	public static final String KEYWORD_HIGHLIGHT_CODE = "§a";
	public static final String COMMENT_HIGHLIGHT_CODE = "§b";
	public static final String MUSICSTRING_HIGHLIGHT_CODE = "";

	/**
	 * For addMusicStringTokens: used to check validity of a MusicString token.
	 * Static so that it only has to be created once.
	 * 
	 * Public so that the DittyXML parser can share.
	 */
	public static final MusicStringParser musicStringParser = new MusicStringParser();

	public static final String SYNC_VOICES_TOKEN = "~syncC";
	public static final String SYNC_WITH_TOKEN = "~syncW";
	public static final String NOTE_EFFECT_TOKEN = "~M";
	public static final String NOTE_EFFECT_OFF_TOKEN = "~N";
	public static final String NOTE_EFFECT_STACCATO = "stac";
	public static final String NOTE_EFFECT_TRANSPOSE = "tran";
	public static final String NOTE_EFFECT_OCTAVES = "octv";
	public static final String NOTE_EFFECT_ACCELERATE = "accl";
	public static final String NOTE_EFFECT_DECELLERATE = "decl";
	public static final String NOTE_EFFECT_CRESCENDO = "cresc";
	public static final String NOTE_EFFECT_DECRESCENDO = "decr";
	public static final String TIMED_EVENT_TOKEN = "~E";
	public static final String SIGN_START_TOKEN = "~A";
	public static final String SIGN_END_TOKEN = "~B";

	// Mutex for saving midis
	private static Object saveMidiPlayerMutex = new Object();

	public static boolean clickHeld = false;

	/**
	 * In Minecraft 1.2.5 and below, this was called when a player clicked a
	 * block. It has been moved to the server's side in 1.3.1, and is no longer
	 * called except by Minetunes functions.
	 * 
	 * @param par1World
	 * @param parX
	 * @param parY
	 * @param parZ
	 * @param entityplayer
	 * @return
	 */
	public static boolean blockActivated(final World par1World, final int parX,
			final int parY, final int parZ, EntityPlayer entityplayer) {
		// This is to prevent multiple activations on one click

		if (!clickHeld) {
			clickHeld = true;
			RightClickCheckThread t = new RightClickCheckThread();
			t.start();

			// System.out.println ("BlockActivated");
			// If player is not holding a shovel... (or wooden axe)
			ItemStack heldStack = entityplayer.getCurrentEquippedItem();
			int held = 0;
			if (heldStack != null) {
				held = heldStack.itemID;
				// System.out.println (held);
			}
			if ((held == 271) || MinetunesConfig.getMinetunesOff()
					|| MinetunesConfig.getBoolean("signs.disabled")) {
				// Holding wooden axe or signs disabled: do nothing.
			} else if (Minetunes.isIDShovel(held)) {
				// Shovel! "Scoop up" sign text.
				GuiEditSignBase.addTextToSavedSigns(((TileEntitySign) par1World
						.getBlockTileEntity(parX, parY, parZ)).signText);
				// Minetunes.writeChatMessage(par1World,
				// "§2Sign's text has been saved.");
				for (int i = 0; i < 20; i++) {
					// Minecraft.getMinecraft().renderGlobal.spawnParticle(
					// "enchantmenttable",
					// (float) parX + Minetunes.rand.nextFloat(),
					// (float) parY + Minetunes.rand.nextFloat(),
					// (float) parZ + Minetunes.rand.nextFloat(),
					// Minetunes.rand.nextFloat() - 0.5f,
					// Minetunes.rand.nextFloat() - 0.5f,
					// Minetunes.rand.nextFloat() - 0.5f);

					// par1World
					// .spawnParticle(
					// "enchantmenttable",
					// (double) parX + 0.5D,
					// (double) parY + 2.0D,
					// (double) parZ + 0.5D,
					// (double) ((float)
					// (Minecraft.getMinecraft().thePlayer.posX - parX) +
					// Minetunes.rand
					// .nextFloat()) - 1.0D,
					// (double) ((float) (Minecraft.getMinecraft().thePlayer
					// .getEyeHeight()
					// + Minecraft.getMinecraft().thePlayer.posY - parY)
					// - Minetunes.rand.nextFloat() - 2.0F),
					// (double) ((float)
					// (Minecraft.getMinecraft().thePlayer.posZ - parZ) +
					// Minetunes.rand
					// .nextFloat()) - 1.0D);

					EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;

					par1World
							.spawnParticle(
									"enchantmenttable",
									player.posX,
									player.posY + player.getEyeHeight() + 0.9,
									player.posZ,
									(double) -((float) (Minecraft
											.getMinecraft().thePlayer.posX - parX) + Minetunes.rand
											.nextFloat()) + 1.0,
									(double) -((float) (Minecraft
											.getMinecraft().thePlayer
											.getEyeHeight()
											+ Minecraft.getMinecraft().thePlayer.posY - parY)
											- Minetunes.rand.nextFloat() + 1.0),
									(double) -((float) (Minecraft
											.getMinecraft().thePlayer.posZ - parZ) + Minetunes.rand
											.nextFloat()) + 1.0);
				}
			} else {
				playDittyFromSigns(par1World, parX, parY, parZ);
			}
		}
		return true;
	}

	private static void playDittyFromSigns(World world, int x, int y, int z) {
		playDittyFromSigns(world, x, y, z, false);
	}

	/**
	 * Like normal play from signs, except that errors are not shown on chat and
	 * are returned instead.
	 * 
	 * Note that this method does not utilize a thread to read the signs -- this
	 * method blocks until all signs are read and all errors found.
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param oneAtATimeOn
	 * @param signWhitelist
	 *            only play these signs; use null to denote no limit
	 * @return a list of error messages, which could be null (unspecified)
	 */
	public static LinkedList<String> playDittyFromSignsQuietly(
			final World world, final int x, final int y, final int z,
			final boolean oneAtATimeOn, LinkedList<Point3D> signWhitelist) {
		SignDitty ditty = playDittyFromSignsDoWork(world, x, y, z,
				oneAtATimeOn, true, signWhitelist);
		if (ditty != null) {
			LinkedList<String> errors = ditty.getErrorMessages();
			return errors;
		} else {
			return null;
		}
	}

	public static void playDittyFromSigns(final World world, final int x,
			final int y, final int z, final boolean oneAtATimeOn) {
		// First, check to see if this sign is blocked from activating by a
		// OneAtATime keyword
		synchronized (oneAtATimeSignsBlocked) {
			if (oneAtATimeSignsBlocked.contains(new Point3D(x, y, z))) {
				// Blocked.
				return;
			}
		}

		Thread t = new PlayDittyFromSignWorkThread(world, x, y, z,
				oneAtATimeOn, false, null);
		// t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	/**
	 * Performs the hard work of reading signs from a world, generating a
	 * DittyProperties and musicString from them, and playing them with JFugue.
	 * 
	 * The Core and Star Method of Minetunes, in other words.
	 * 
	 * @param world
	 *            world that signs are contained in
	 * @param x
	 *            location of first sign
	 * @param y
	 * @param z
	 * @param oneAtATimeOn
	 *            if true, the ditty starting at (x y z) will have to stop
	 *            before it is allowed to play again
	 * @param silent
	 *            if false, errors are automatically written to the player's
	 *            chat area as well as the DittyProperties returned. Otherwise,
	 *            they are just included in the returned DittyProperties. NOTE:
	 *            errored signs are highlighted even if their errors are not
	 *            shown on chat/
	 * @return a DittyProperties if the song is read (with or without errors),
	 *         or null (if nothing was successfully read at all, or oneAtATime
	 *         blocked play). Returns after reading the song and kicking off the
	 *         playback.
	 */
	public static SignDitty playDittyFromSignsDoWork(World world, int x, int y,
			int z, boolean oneAtATimeOn, boolean silent,
			LinkedList<Point3D> signWhitelist) {
		// System.out.println("PlayDittyFromSigns called on :" + x + ":" + y +
		// ":"
		// + z);
		//
		// // First, check to see if this sign is blocked from activating by a
		// // OneAtATime keyword
		// // NOTE: CODE COPIED FROM playDittyFromSigns()
		// // Bad Dobby, Bad Dobby!
		// synchronized (oneAtATimeSignsBlocked) {
		// if (oneAtATimeSignsBlocked.contains(new Point3D(x, y, z))) {
		// // Blocked.
		// return null;
		// }
		//
		// // If oneAtATimeOn is true, go ahead and instantly add this ditty to
		// // the
		// // block list
		// // Will happen again later, but it should take effect as soon as
		// // possible (this call) so that ditties read in close succession
		// // don't
		// // both start
		// if (oneAtATimeOn) {
		// oneAtATimeSignsBlocked.add(new Point3D(x, y, z));
		// }
		// }

		long startTime = System.nanoTime();

		// TODO: If signs have been picked and there isn't a whitelist given,
		// set the whitelist to all picked signs
		if (signWhitelist == null && Minetunes.getPickedSigns().size() > 0) {
			signWhitelist = new LinkedList<Point3D>();
			for (TileEntitySign t : Minetunes.getPickedSigns()) {
				signWhitelist.add(new Point3D(t.xCoord, t.yCoord, t.zCoord));
			}
		}

		// Calculate the start point
		Point3D startPoint = new Point3D(x, y, z);

		// Check that this first sign hit is on the whitelist
		if (signWhitelist != null) {
			boolean isOnList = false;
			Point3D tempPoint = new Point3D();
			for (TileEntitySign t : Minetunes.getPickedSigns()) {
				tempPoint.x = t.xCoord;
				tempPoint.y = t.yCoord;
				tempPoint.z = t.zCoord;
				if (tempPoint.equals(startPoint)) {
					isOnList = true;
					break;
				}
			}
			if (!isOnList) {
				// Bad start point
				if (!silent) {
					Minetunes.writeChatMessage(world,
							"§2This sign is unpicked.");
					return null;
				}
			}
		}

		// Read in a MusicString and SongProperties to play from signs
		LinkedList<SignLogPoint> signsReadList = new LinkedList<SignLogPoint>();
		SignDitty dittyProperties = new SignDitty();
		StringBuilder musicStringToPlay = readPattern(startPoint, world,
				signsReadList, dittyProperties, 0, signWhitelist, 0, false);

		// If MaxPlays is exceeded somewhere in the ditty, cancel play
		for (MaxPlaysLockPoint p : dittyProperties.getMaxPlayLockPoints()) {
			boolean foundOnList = false;
			for (MaxPlaysLockPoint pointList : maxPlaysLockPoints) {
				if (pointList.point.equals(p.point)) {
					// Found!
					pointList.maxPlays++;
					foundOnList = true;
					if (pointList.maxPlays > p.maxPlays) {
						return null;
					}
				}
			}

			if (!foundOnList) {
				maxPlaysLockPoints.add(new MaxPlaysLockPoint(p.point, 1));
			}
		}

		// If this ditty was started from something like a proximity sign, turn
		// on the "oneAtATime" keyword's property by default
		if (oneAtATimeOn) {
			dittyProperties.setOneAtATime(true);
		}

		// Set the start point in the ditty properties
		dittyProperties.setStartPoint(startPoint);

		// Check for a null result; indicates infinite loop
		// Also, last sign in sign log is the starting position of the infinite
		// loop
		if (musicStringToPlay == null) {
			// Infinite loop found; display errors, and do not play song
			if (!silent && MinetunesConfig.getBoolean("signs.showErrors")) {
				Minetunes
						.writeChatMessage(
								world,
								"§cThere is an infinite loop in this song. It is probably caused by signs arranged in a circle, or gotos that point back at each other.");
			}
			dittyProperties
					.addErrorMessage("§cThere is an infinite loop in this song. It is probably caused by signs arranged in a circle, or gotos that point back at each other.");
			// Highlight all signs with the error
			// First, find the start of the loop. It is the same as the end.
			Point3D infiniteLoopStart = signsReadList.getLast();
			Integer infiniteLoopStartIndex = detectInfiniteLoop(signsReadList);
			if (infiniteLoopStartIndex != null) {
				// Then do the highlighting
				for (int i = infiniteLoopStartIndex; i < signsReadList.size(); i++) {
					for (int line = 0; line < LINES_ON_A_SIGN; line++) {
						highlightSignErrorLine(world, new SignLine(
								signsReadList.get(i), line));
					}
				}
			}
			// Do not play song
			return null;
		}

		// Check for no play tokens
		if (dittyProperties.isContainsNoPlayTokens()) {
			return null;
		}

		// Play the ditty

		// Add a reset to the start of the ditty to formally init volume ect.
		musicStringToPlay.insert(0, getResetToken() + " ");
		// Convert buffer to string
		String ditty = musicStringToPlay.toString();

		// If there's a song to play that isn't empty, and more than half good
		// tokens:
		// Also, always detect a song if the keyword isDitty is in song
		int totalTokensFound = dittyProperties.getTotalTokens();
		int validTokensFound = totalTokensFound
				- dittyProperties.getBadTokens();
		simpleLog("Total tokens: " + totalTokensFound + " Bad Tokens: "
				+ dittyProperties.getBadTokens() + " Ratio: "
				+ ((double) validTokensFound) / (double) totalTokensFound);
		if ((totalTokensFound > 0 && ((double) validTokensFound)
				/ (double) totalTokensFound > 0.8d)
				|| dittyProperties.isForceGoodDittyDetect()) {

			// If there are no errors...
			if (dittyProperties.getErrorMessages().size() <= 0) {
				// Handle muting before playing a song
				if (dittyProperties.getMuting()) {
					// Mute: stop all playing music except this music
					Minetunes.mutePlayingDitties(dittyProperties.getDittyID());
				}

				// If applicable, save midi of song
				if (dittyProperties.getMidiSaveFile() != null
						&& MinetunesConfig.getBoolean("signs.saveMidiEnabled")) {
					try {

						saveMidiFile(dittyProperties.getMidiSaveFile(), ditty,
								dittyProperties.getMidiSavePoint());
						if (!silent
								&& MinetunesConfig
										.getBoolean("midiSavedMessage")) {
							// Show midi message
							Minetunes.writeChatMessage(world, "§dSaved Midi: "
									+ dittyProperties.getMidiSaveFile()
											.getName());
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				// Register any disco floors with Minetunes
				Minetunes.addDiscoFloors(dittyProperties.getDiscoFloors());

				// Maestro, commence!
				dittyProperties.setMusicString(ditty);
				DittyPlayerThread.playDitty(dittyProperties);

				// Emit single particle, if necessary
				if ((MinetunesConfig.noteParticlesDisabled)
						|| MinetunesConfig.particlesEnabled) {
					Minetunes.executeTimedDittyEvent(new NoteStartEvent(
							startPoint, 0, 0, 0, null, dittyProperties
									.getDittyID()));
				}
			}

			if (!silent && MinetunesConfig.getBoolean("signs.showErrors")) {
				// Show chat messages: first handle the buffer, cutting it down
				// to
				// one if that option is enabled
				LinkedList<String> chatMessageBuffer = dittyProperties
						.getErrorMessages();

				// Removed setting "firstErrorOnly"
				if (chatMessageBuffer.size() > 0) {
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
					Minetunes.writeChatMessage(world, s);
				}

				if (chatMessageBuffer.size() > 0) {
					// Emit error particles
					if (MinetunesConfig.particlesEnabled) {
						for (int i = 0; i < 3; i++) {
							Minetunes.requestParticle(new ParticleRequest(
									startPoint, "smoke"));
						}
					}
				}
			}

			// Add lines to blink
			if (MinetunesConfig.getBoolean("signs.showErrors")) {
				for (SignLine signLine : dittyProperties
						.getHighlightedErrorLines()) {
					highlightSignErrorLine(world, signLine);
				}
			}

			// Highlight lines
			for (SignLineHighlight signLineHighlight : dittyProperties
					.getHighlightedLines()) {
				highlightSignLine(world, signLineHighlight);
			}
		} else {
			// If there are no errors, allow the possibility of an empty "mute"
			// sign.
			if (dittyProperties.getErrorMessages().size() <= 0
					&& dittyProperties.getMuting()) {
				// Mute: stop all playing music
				Minetunes.mutePlayingDitties();
			}
		}

		// Print total time required for processing
		long endTime = System.nanoTime();
		long totalTime = endTime - startTime;
		double totalTimeSeconds = (double) totalTime / 1000000000D;
		// simpleLog("Time to process: " + Double.toString(totalTimeSeconds));
		// System.out.println("Time to Process Ditty: "
		// + Double.toString(totalTimeSeconds));

		return dittyProperties;
	}

	/**
	 * Reads a set of signs, starting with the given one, and returns the
	 * MusicString read of of this pattern and any patterns in this pattern.
	 * 
	 * @param startPoint
	 *            Sign to start reading at
	 * @param world
	 *            World signs are contained in
	 * @param signsReadList
	 *            List of signs already read; used in instantly detecting
	 *            infinite loops
	 * @param ditty
	 *            Metadata for a ditty.
	 * @param subpatternLevel
	 *            Call with 0 if this is the "Main song".
	 * @param signWhitelist
	 *            only signs in this list will be acknowledged as existing: all
	 *            other signs will not be played.
	 * @param startLine
	 *            Used with patterns, to start after the pattern keyword on the
	 *            first sign. Note that behaviour for starting in the middle of
	 *            multi-line keywords is undefined.
	 * @return null if there's a problem, or a musicstring if the reading was
	 *         successful.
	 */
	private static StringBuilder readPattern(Point3D startPoint, World world,
			LinkedList<SignLogPoint> signsReadList, SignDitty ditty,
			int subpatternLevel, LinkedList<Point3D> signWhitelist,
			int startLine, boolean pattStarted) {
		// Contains musicstring read from pattern
		StringBuilder readMusicString = new StringBuilder();

		// Get an ID for this subpattern
		int subpatternID = getNextSubpatternID();

		// Do not make clone of log
		LinkedList<SignLogPoint> signLog = signsReadList;
		LinkedList<LinkedList<SignLogPoint>> subPatternSignLogs = new LinkedList<LinkedList<SignLogPoint>>();

		// Set up variables for loop through signs

		// The point that the sign read loop is currently reading from
		Point3D currSignPoint = startPoint.clone();
		// The point of the next sign that the sign read loop will read
		Point3D nextSignPoint = startPoint.clone();

		// Info about the current sign being read
		int currSignMetadata = 0;
		int currSignFacing = -1;
		Block currSignType = null;
		TileEntitySignMinetunes currSignTileEntity = null;

		// If this is set to true, the pattern has ended due to a keyword (such
		// as end)
		boolean endPattern = false;

		// Loop through signs
		while (true) {
			// Get info and text of a sign
			currSignType = getSignBlockType(currSignPoint, world);
			// Check to see if current block is really a sign
			if (currSignType == null) {
				// Not a sign! End of pattern.
				break;
			}

			// Now we know that this block is a sign. Time to check for any
			// infinite loops.
			SignLogPoint currLogPoint = new SignLogPoint(currSignPoint,
					subpatternLevel, subpatternID);
			signLog.add(currLogPoint);
			if (detectInfiniteLoop(signLog) != null) {
				// return null to indicate subpattern cannot be read further.
				return null;
			}

			// Continue reading more sign info
			currSignMetadata = world.getBlockMetadata(currSignPoint.x,
					currSignPoint.y, currSignPoint.z);
			currSignFacing = getSignFacing(currSignMetadata, currSignType);
			// Called currBLOCK because we don't yet know if the tileentity is a
			// sign for sure
			TileEntity currBlockTileEntity = world.getBlockTileEntity(
					currSignPoint.x, currSignPoint.y, currSignPoint.z);
			// Flag denoting this as an empty sign
			boolean signIsEmpty = false;
			if (currBlockTileEntity instanceof TileEntitySignMinetunes) {
				currSignTileEntity = (TileEntitySignMinetunes) currBlockTileEntity;
				int emptyLineTally = 0;
				String[] signText = currSignTileEntity.getSignTextNoCodes();
				for (String s : signText) {
					if (s.trim().length() <= 0) {
						emptyLineTally++;
					}
				}
				if (emptyLineTally >= signText.length) {
					// Empty sign! yay! We can be lazy with it!
					signIsEmpty = true;
				}
			} else {
				// Current sign's tile entity... is not a sign entity or does
				// not exist.
				System.err
						.println("In playDittyFromSigns: the tile entity of a sign we are attempting to read does not exist or is not a MineTunes Sign Entity. Strange.");
				System.err.println("Ending pattern.");
				break;
			}

			// If sign turned out to be blank, we can simply apply natural focus
			// flow to it, saving a ton of time (?)
			if (signIsEmpty) {
				nextSignPoint = applyNaturalFocusFlow(currSignPoint,
						currSignFacing, world, signWhitelist);
				// Swap the next sign's position in as the new current sign's
				// position for next iteration
				currSignPoint = nextSignPoint.clone();
				continue;
			}

			// Get sign id
			int currSignIDNum = ditty.registerSignForID(currSignPoint);
			// Add sign location token
			ditty.addMusicStringTokens(readMusicString, SIGN_START_TOKEN
					+ currSignIDNum, false);

			// Process each line of a sign

			// If a pattern is more than one sign, this is true.
			// This affects whether a pattern is merely the rest of this sign or
			// whether it is the start of a chain of signs
			boolean applyFocusFlow = false;

			// In a pattern that IS more than one sign, this is obviously set to
			// true.
			if (!currSignPoint.equals(startPoint)) {
				applyFocusFlow = true;
			}

			// Get sign text
			String[] signText = currSignTileEntity.getSignTextNoCodes();

			// Parse sign for keywords
			ParsedSign parsedSign = SignParser.parseSign(signText);

			// In a pattern that doesn't start on a pattern sign, the pattern
			// must be more than one sign
			if (pattStarted) {
				applyFocusFlow = true;
			} else {
				boolean patternKeywordFound = false;
				for (int i = 0; i < LINES_ON_A_SIGN; i++) {
					if (parsedSign.getLine(i) instanceof PatternKeyword) {
						patternKeywordFound = true;
						break;
					}
				}
				if (!patternKeywordFound) {
					applyFocusFlow = true;
				}
			}

			// "Memory" for not calling multi-line keywords twice
			Object lastLineContents = null;

			int lineStartValue = 0;
			if (currSignPoint.equals(startPoint)) {
				lineStartValue = startLine;
			}
			// Loop lines of the current sign, starting from the indicated line
			for (int line = lineStartValue; line < signText.length; line++) {
				Object lineContents = parsedSign.getLine(line);

				// Avoid hitting multiline keywords twice
				if (lineContents == lastLineContents) {
					continue;
				} else {
					lastLineContents = lineContents;
				}

				// Handle line based on type
				if (lineContents == null) {
					// Do nothing. Duh.
				} else if (lineContents instanceof Comment) {
					// Line is a comment; ignore
					ditty.addHighlight(currSignPoint, line,
							COMMENT_HIGHLIGHT_CODE);
					continue;
				} else if (lineContents instanceof String) {
					// Line contians music
					boolean noErrors = ditty.addMusicStringTokens(
							readMusicString, (String) lineContents, true);
					if (!noErrors) {
						ditty.addErrorHighlight(currSignPoint, line);
					}
					ditty.addHighlight(currSignPoint, line,
							MUSICSTRING_HIGHLIGHT_CODE);
				} else if (lineContents instanceof SignTuneKeyword) {
					SignTuneKeyword keyword = (SignTuneKeyword) lineContents;

					// Keywords count as tokens
					ditty.incrementTotalTokens();

					// Highlight line of keyword on sign
					ditty.addHighlight(currSignPoint, line,
							KEYWORD_HIGHLIGHT_CODE);

					// Check for a bad keyword
					if (!keyword.isGoodKeyword()) {
						showKeywordError(ditty, currSignPoint,
								keyword.getWholeKeyword(), line, keyword);
						break;
					}

					// Execute keyword
					Point3D keywordNextSign = keyword.execute(ditty,
							currSignPoint.clone(), currSignTileEntity,
							nextSignPoint.clone(), world, readMusicString);
					if (keywordNextSign != null) {
						applyFocusFlow = true;
						nextSignPoint = keywordNextSign;
					}

					// Execute any special behaviours of the keyword
					if (keyword.hasSpecialExecution()) {
						// By type
						if (keyword instanceof PatternKeyword) {
							// If the pattern isn't on the last line of a sign
							// (nothing below to repeat!)
							if (line < LINES_ON_A_SIGN - 1) {
								// Parse keyword
								PatternKeyword patternKeyword = (PatternKeyword) keyword;

								// Add the subpattern to this pattern the
								// specified
								// number of times
								for (int i = 0; i < patternKeyword
										.getRepeatCount(); i++) {
									// readPattern will re-add it
									signLog.removeLast();
									// Read pattern, starting with line after
									// this keyword
									LinkedList<Point3D> subPatternSignLog = (LinkedList<Point3D>) signLog
											.clone();
									StringBuilder subPatternMusicString = readPattern(
											currSignPoint, world, signLog,
											ditty, subpatternLevel + 1,
											signWhitelist, line + 1, false);

									// If a subpattern fails due to an infinte
									// loop, pass the failure on
									if (subPatternMusicString == null) {
										return null;
									}

									// Do not check for errors
									ditty.addMusicStringTokens(readMusicString,
											subPatternMusicString.toString(),
											false);
								}

								// Ignore the contents of this sign; it has been
								// read by readPattern already.
								break;
							}
						} else if (keyword instanceof PattKeyword) {
							// Parse keyword
							PattKeyword pattKeyword = (PattKeyword) keyword;

							// Try to jump to sign with the given comment
							Comment match = GotoKeyword
									.getNearestMatchingComment(currSignPoint,
											world, pattKeyword.getComment());

							Point3D pattLocation;
							if (match == null) {
								// Simulate an explicit goto pointing at nothing
								// TODO: This is a hack. Please come up with a
								// more
								// explicit solution.
								pattLocation = new Point3D(0, -1, 0);
							} else {
								pattLocation = match.getLocation().clone();
							}

							// Add the subpattern to this pattern the
							// specified
							// number of times
							for (int i = 0; i < pattKeyword.getRepeatCount(); i++) {
								// Read pattern
								LinkedList<SignLogPoint> subPatternSignLog = (LinkedList<SignLogPoint>) signLog
										.clone();
								StringBuilder subPatternMusicString = readPattern(
										pattLocation, world, subPatternSignLog,
										ditty, subpatternLevel + 1,
										signWhitelist, 0, true);

								// If a subpattern fails due to an infinte loop,
								// pass the failure on
								if (subPatternMusicString == null) {
									// simpleLog("PATTERN: null failure on pattern");
									return null;
								}

								// Do not check for errors
								ditty.addMusicStringTokens(readMusicString,
										subPatternMusicString.toString(), false);

								// Note that we are back on the original sign in
								// the
								// musicstring
								ditty.addMusicStringTokens(readMusicString,
										SIGN_START_TOKEN + currSignIDNum, false);
							}
						} else if (keyword instanceof EndKeyword) {
							// If the keyword is end, but NOT "end line" or
							// "endline"
							if (((EndKeyword) keyword).isEndLineReally()) {
								nextSignPoint = carriageReturn(world,
										currSignPoint, signWhitelist);
							} else {
								// End pattern
								endPattern = true;
							}
							// Stop reading lines from sign
							break;
						} else if (keyword instanceof EndLineKeyword) {
							// If keyword is either "endline" or "end line"
							// Force a newline
							// naturalFocusFlowEnabled = false;
							nextSignPoint = carriageReturn(world,
									currSignPoint, signWhitelist);
							// Stop reading lines from sign
							break;
						}
					}
				}
			}

			// Add sign hit end token
			ditty.addMusicStringTokens(readMusicString, SIGN_END_TOKEN
					+ currSignIDNum, false);

			// Account for a one-sign pattern with no gotos
			// (Does not have natural focus flow, and ends on the sign it
			// started at)
			// FIXED: If main song contains a pattern sign (which is the
			// first
			// sign in a song)
			// that has no gotos on it, do not end song here
			if (!applyFocusFlow && !(subpatternLevel == 0)) {
				break;
			}

			// If the pattern has ended, stop reading more signs.
			if (endPattern) {
				break;
			}

			// If natural focus flow hasn't been interrupted, flow.
			if (nextSignPoint.equals(currSignPoint)) {
				nextSignPoint = applyNaturalFocusFlow(currSignPoint.clone(),
						currSignFacing, world, signWhitelist);
			}
			// else if (!nextSignPoint.equals(currSignPoint)
			// && signWhitelist == null) {
			// // If natural focus flow is not enabled (i.e. the song
			// // contains
			// // gotos), throw an error if the gotos are pointing at thin
			// // air
			// // And there isn't a whitelist
			//
			// // Save myself a null pointer exception by only coming up
			// // with
			// // the next block type if nextSignPoint isn't null
			// int nextBlockType = 0;
			// if (nextSignPoint != null) {
			// nextBlockType = world.getBlockId(nextSignPoint.x,
			// nextSignPoint.y, nextSignPoint.z);
			// }
			//
			// if (!(nextBlockType == Block.signPost.blockID || nextBlockType ==
			// Block.signWall.blockID)) {
			// // Thin air. Throw error.
			// String signText2 = "";
			// for (String s : currSignTileEntity.signText) {
			// if (s.trim().length() > 0) {
			// signText2 += "/" + s.trim();
			// }
			// }
			// // Remove first /
			// try {
			// signText2 = signText2.substring(1, signText2.length());
			// } catch (Exception e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// ditty.addErrorMessage("§b" + signText2
			// + "§c: The gotos don't point at a sign.");
			// // Highlight all lines of the offending sign
			// for (int i = 0; i < LINES_ON_A_SIGN; i++) {
			// ditty.addErrorHighlight(currSignPoint, 0);
			// }
			// }
			// }

			// Swap the next sign's position in as the new current sign's
			// position for next iteration
			currSignPoint = nextSignPoint;
			nextSignPoint = nextSignPoint.clone();

			// Enforce the sign whitelist, signWhitelist
			if (signWhitelist != null) {
				if (!signWhitelist.contains(currSignPoint)) {
					return readMusicString;
				}
			}
		}

		Minetunes.stopMCSlowdown();
		return readMusicString;
	}

	/**
	 * Combines text below startLine into a single-line lyric. Handles hyphens
	 * and whitespace.
	 * 
	 * @param startLine
	 * @param signText
	 * @param colorCode
	 * @return
	 */
	public static String readLyricFromSign(int startLine, String[] signText,
			String colorCode) {
		String lyricText = "";

		for (int lyricTextLine = startLine; lyricTextLine < LINES_ON_A_SIGN; lyricTextLine++) {
			if (signText[lyricTextLine].trim().endsWith("-")) {
				// Handle split words
				lyricText += signText[lyricTextLine].substring(0,
						signText[lyricTextLine].lastIndexOf("-"));
			} else if (signText[lyricTextLine].trim().length() > 0) {
				String lyricLineFromSign = signText[lyricTextLine];

				// Trim whitespace off of JUST THE END of a line
				// Also remove lines that consist wholly of
				// whitespace
				while (lyricLineFromSign.charAt(lyricLineFromSign.length() - 1) == ' ') {
					lyricLineFromSign = lyricLineFromSign.substring(0,
							lyricLineFromSign.length() - 1);
				}

				lyricText += lyricLineFromSign + " ";
			}
		}
		// Add color code
		lyricText = colorCode.replace('&', '§') + lyricText;

		// Replace inline color codes
		for (String s : colorCodeChars) {
			lyricText = lyricText.replace("&" + s, "§" + s);
		}

		return lyricText;
	}

	private static String getMinecraftAdjustedVolumeToken(int volumePercent) {
		int sixteenBitVolume = Minetunes
				.getMinecraftAdjustedSixteenBitVolume(volumePercent);

		return "X[Volume]=" + sixteenBitVolume;
	}

	public static String getAdjustedVolumeToken(int volumePercent,
			Ditty dittyProperties) {
		int volumeEventID = dittyProperties.addDittyEvent(new VolumeEvent(
				((float) volumePercent / 100f), dittyProperties.getDittyID()));
		return getMinecraftAdjustedVolumeToken(volumePercent) + " ~E"
				+ volumeEventID;
	}

	private static void showKeywordError(SignDitty dittyProperties,
			Point3D currSignPoint, String currLine, int line,
			SignTuneKeyword keyword) {
		dittyProperties.addErrorHighlight(currSignPoint, line);
		if (keyword.getErrorMessageType() == SignTuneKeyword.ERROR) {
			dittyProperties.addErrorMessage("§b" + currLine + "§c: "
					+ keyword.getErrorMessage());
		} else if (keyword.getErrorMessageType() == SignTuneKeyword.WARNING) {
			dittyProperties.addErrorMessage("§b" + currLine + "§e: "
					+ keyword.getErrorMessage());
		}
	}

	/**
	 * Note: assumes, for time efficiency, that only one new sign has been added
	 * to sign log
	 * 
	 * @param signLog
	 * @return index of the start of the infinite loop; otherwise null
	 */
	private static Integer detectInfiniteLoop(LinkedList<SignLogPoint> signLog) {
		// Look for references to signs already read earlier in the same
		// subpattern or a higher level of subpattern

		// Check the newest point in the log...
		SignLogPoint newPoint = signLog.getLast();

		// Against all but the newest point.
		for (int i = 0; i < signLog.size() - 1; i++) {
			SignLogPoint p = signLog.get(i);
			// If this point is in a higher level subpattern or in the same
			// subpattern as the newest point
			if (p.getId() == newPoint.getId()
					|| p.getLevel() < newPoint.getLevel()) {
				// If the points are the same
				if (p.equals(newPoint)) {
					return i;
				}
			}
		}

		// If all previous tests have failed, there is no infinite loop
		return null;
	}

	private static int getNextSubpatternID() {
		nextSubpatternID++;
		return nextSubpatternID;
	}

	private static void addSubpatternSignLogsToSignLog(
			LinkedList<Point3D> signLog,
			LinkedList<LinkedList<Point3D>> subPatternSignLogs) {
		// Combine and add all subpatterns's signs to log
		for (LinkedList<Point3D> subPatternSignLog : subPatternSignLogs) {
			for (Point3D sign : subPatternSignLog) {
				signLog.add(sign);
			}
		}
	}

	/**
	 * Constructs a note effect token with arguments. Result will resemble<br>
	 * <br>
	 * ~Mstac~1~-1
	 * 
	 * @param offToken
	 * @param type
	 * @param args
	 * @return
	 */
	public static String createNoteEffectToken(boolean offToken, String type,
			Object... args) {
		StringBuilder token = new StringBuilder();
		if (offToken) {
			token.append(NOTE_EFFECT_OFF_TOKEN);
		} else {
			token.append(NOTE_EFFECT_TOKEN);
		}
		token.append(type);
		for (Object o : args) {
			token.append("~").append(o.toString());
		}
		return token.toString();
	}

	public static boolean isNoteEffectToken(String token) {
		if (token.toLowerCase().startsWith(NOTE_EFFECT_OFF_TOKEN.toLowerCase())) {
			return true;
		} else if (token.toLowerCase().startsWith(
				NOTE_EFFECT_TOKEN.toLowerCase())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns true if the given string begins with the Note Effect Off Token
	 * beginning (~N as of this writing).
	 * 
	 * @param token
	 * @return
	 */
	public static boolean getNoteEffectTokenOff(String token) {
		if (token.toLowerCase().startsWith(NOTE_EFFECT_OFF_TOKEN.toLowerCase())) {
			return true;
		} else {
			return false;
		}
	}

	public static String getNoteEffectTokenType(String token) {
		if (token.length() < 3) {
			return "";
		}

		token = token.substring(2);

		int endIndex = token.indexOf("~");
		if (endIndex > 0) {
			token = token.substring(0, endIndex);
		}
		return token;
	}

	/**
	 * Returns the arguments in a note effect token.
	 * 
	 * @param token
	 * @return null if none
	 */
	public static String[] getNoteEffectTokenArgs(String token) {
		String[] tokenParts = token.split("~");
		if (tokenParts.length < 2) {
			return null;
		} else {
			String[] arguments = new String[tokenParts.length - 2];
			System.arraycopy(tokenParts, 2, arguments, 0, arguments.length);
			return arguments;
		}
	}

	// public static final String NOTE_HIT_TOKEN = "~N";
	// public static final String SFX_TOKEN = "~F";

	/**
	 * In MCDitty 0.9.1.02+, this is a set of tokens inserted into the
	 * musicstring that simulate the effects of an old-style MCDitty reset.
	 * 
	 * TODO: Handle this in a method lower down, in JFugue, without using long
	 * strings of extra tokens; just changing what needs to be changed right
	 * there.
	 */
	public static String getResetToken() {
		String tokens = SYNC_VOICES_TOKEN;

		// Add other tokens to reset_token
		for (int v = 15; v >= 0; v--) {
			// TODO: Really require a KCmaj on EVERY voice?
			tokens += " V" + v + " L0 I[Piano] KCmaj +0 "
					+ getMinecraftAdjustedVolumeToken(100) + " "
					+ createNoteEffectToken(true, NOTE_EFFECT_STACCATO) + " "
					+ createNoteEffectToken(true, NOTE_EFFECT_TRANSPOSE) + " "
					+ createNoteEffectToken(true, NOTE_EFFECT_OCTAVES);
		}
		tokens += " T120";
		return tokens;
	}

	/**
	 * In MCDitty 0.9.1.02+, this is a set of tokens inserted into the
	 * musicstring that simulate the effects of an old-style MCDitty reset.
	 * 
	 * @param dittyProperties
	 *            This allows getResetToken to add volume change events to the
	 *            ditty as well as midi controller volume tokens.
	 * 
	 *            TODO: Handle this in a method lower down, in JFugue, without
	 *            using long strings of extra tokens; just changing what needs
	 *            to be changed right there.
	 */
	public static String getResetToken(Ditty dittyProperties) {
		String tokens = SYNC_VOICES_TOKEN;

		// Add other tokens to reset_token
		for (int v = 15; v >= 0; v--) {
			// TODO: Really require a KCmaj on EVERY voice?
			tokens += " V" + v + " L0 I[Piano] KCmaj +0 "
					+ getAdjustedVolumeToken(100, dittyProperties) + " "
					+ createNoteEffectToken(true, NOTE_EFFECT_STACCATO) + " "
					+ createNoteEffectToken(true, NOTE_EFFECT_TRANSPOSE) + " "
					+ createNoteEffectToken(true, NOTE_EFFECT_OCTAVES);
		}
		tokens += " T120";
		return tokens;
	}

	// public static final String RESET_TOKEN = "~Reset";

	private static void highlightSignErrorLine(World world, SignLine signLine) {
		TileEntity t = world.getBlockTileEntity(signLine.x, signLine.y,
				signLine.z);
		if (t instanceof TileEntitySignMinetunes) {
			TileEntitySignMinetunes t2 = (TileEntitySignMinetunes) t;
			t2.startBlinking = true;
			t2.errorBlinkLine[signLine.getLine()] = true;
			simpleLog("Starting sign error blinking");
		}
	}

	private static void highlightSignLine(World world,
			SignLineHighlight signLine) {
		if (MinetunesConfig.highlightEnabled) {
			TileEntity t = world.getBlockTileEntity(signLine.x, signLine.y,
					signLine.z);
			if (t instanceof TileEntitySignMinetunes) {
				TileEntitySignMinetunes t2 = (TileEntitySignMinetunes) t;
				t2.highlightLine[signLine.getLine()] = signLine
						.getHighlightCode();
			}
		}
	}

	/**
	 * 
	 * @param startPoint
	 *            is not modified as this runs
	 * @param signFacing
	 * @param world
	 * @return
	 */
	private static Point3D applyNaturalFocusFlow(Point3D startPoint,
			int signFacing, World world, LinkedList<Point3D> signWhitelist) {
		Point3D rightSign = findSignToRight(startPoint.clone(), signFacing,
				world, signWhitelist);
		if (rightSign != null) {
			// Return the sign that was found to the right
			return rightSign;
		} else {
			// No more signs to right; try to move the the start of the next
			// line
			Point3D newLineStart = carriageReturn(world, startPoint.clone(),
					signWhitelist);
			if (newLineStart == null) {
				// No new line available either; return null
				return null;
			} else {
				// Return start of new line
				return newLineStart;
			}
		}
	}

	/**
	 * TODO: Almost redundant with the more powerful getCoordsRelative to sign?
	 * 
	 * @param startPoint
	 *            (not modified by this method)
	 * @param signFacing
	 * @param world
	 * @param signWhitelist
	 * @return
	 */
	private static Point3D findSignToRight(Point3D startSign, int signFacing,
			World world, LinkedList<Point3D> whitelist) {
		Point3D rightPoint = getCoordsRelativeToSign(startSign.clone(),
				signFacing, 1, 0, 0);
		if (isSign(rightPoint, world)
				&& (whitelist == null || whitelist.contains(rightPoint))) {
			// There is a sign to the right! Return its location.
			return rightPoint;
		} else {
			// No sign to right; return null
			return null;
		}
	}

	/**
	 * Tries to find the start of the next "line" of signs, given a starting
	 * sign.
	 * 
	 * @param world
	 * @param startPoint
	 *            (not modified during this method's operation)
	 * @return
	 */
	private static Point3D carriageReturn(World world, Point3D startPoint,
			LinkedList<Point3D> whitelist) {
		// System.out.println("CR Called");
		// This point will be the sign farthest to the left in the line
		// We will go down one from this to find the start of a new line
		Point3D newLineStart = startPoint.clone();
		// A list of all signs in the first row
		LinkedList<Point3D> signsInFirstRow = new LinkedList<Point3D>();
		// Add first sign, because loop below only adds additional signs
		signsInFirstRow.add(startPoint);
		while (true) {
			// Loop trying to find the farthest block to the left of the line

			// Get location of block to left of sign

			// Find the facing of the current sign being examined
			int carriageReturnSignFacing = getSignFacing(
					world.getBlockMetadata(newLineStart.x, newLineStart.y,
							newLineStart.z),
					getSignBlockType(newLineStart, world));

			// Find the coords of the block to the left
			Point3D leftPoint = getCoordsRelativeToSign(newLineStart,
					carriageReturnSignFacing, -1, 0, 0);

			if (isSign(leftPoint.x, leftPoint.y, leftPoint.z, world)
					&& (whitelist == null || whitelist.contains(leftPoint))) {
				// There is a sign to the left! Focus on it, and
				// start loop again
				signsInFirstRow.add(leftPoint);
				newLineStart = leftPoint;
				continue;
			} else {
				// No more signs to left:
				// Technically done "carriage returning"; now do a "newline"

				// Look down from the first sign of the first row
				if (isSign(newLineStart.x, newLineStart.y - 1, newLineStart.z,
						world)
						&& (whitelist == null || whitelist
								.contains(newLineStart))) {
					// There is a new line below
					newLineStart.y--;

					// Find the start of this second row
					while (true) {
						// Get location of block to left of sign

						// Find the facing of the current sign being examined
						carriageReturnSignFacing = getSignFacing(
								world.getBlockMetadata(newLineStart.x,
										newLineStart.y, newLineStart.z),
								getSignBlockType(newLineStart, world));

						// Find the coords of the block to the left
						leftPoint = getCoordsRelativeToSign(newLineStart,
								carriageReturnSignFacing, -1, 0, 0);

						if (isSign(leftPoint.x, leftPoint.y, leftPoint.z, world)
								&& (whitelist == null || whitelist
										.contains(leftPoint))) {
							// There is a sign to the left!
							// Make sure that this sign is facing the same way
							// as the last one
							if (getCoordsRelativeToSign(
									leftPoint,
									getSignFacing(
											world.getBlockMetadata(leftPoint.x,
													leftPoint.y, leftPoint.z),
											getSignBlockType(newLineStart,
													world)), 1, 0, 0).equals(
									newLineStart)) {
								// Focus on it, and
								// start loop again
								newLineStart = leftPoint;
								continue;
							} else {
								// No more signs in row: break loop
								break;
							}
						} else {
							// No more signs to left: break loop
							break;
						}
					}

					// And break loop; this found sign is the start
					// of the next line
					break;
				} else {
					// TODO: Does not directly alert anything that this pattern
					// is over

					// Nothing under the first sign of the row.
					// Search under each sign in row
					// Note that this list omits the first sign in the first
					// row,
					// which has already been checked
					for (int i = signsInFirstRow.size() - 1; i >= 0; i--) {
						Point3D currPoint = signsInFirstRow.get(i);
						if (isSign(currPoint.x, currPoint.y - 1, currPoint.z,
								world)
								&& (whitelist == null || whitelist
										.contains(currPoint))) {
							// There is a sign beneath this sign! Return it as
							// start of next row
							return new Point3D(currPoint.x, currPoint.y - 1,
									currPoint.z);
						}
					}

					// If still nothing has been found, return a invalid
					// coordinate as sign of failure
					return new Point3D(0, -1, 0);
				}
			}
		}
		return newLineStart;
	}

	public static int getSignFacing(int signMetadata, Block signType) {
		if (signType == Block.signPost) {
			if (signMetadata == 0x0F || signMetadata == 0x00
					|| signMetadata == 0x01 || signMetadata == 0x02) {
				// South
				return FACES_SOUTH;
			} else if (signMetadata == 0x03 || signMetadata == 0x04
					|| signMetadata == 0x05 || signMetadata == 0x06) {
				// West
				return FACES_WEST;
			} else if (signMetadata == 0x07 || signMetadata == 0x08
					|| signMetadata == 0x09 || signMetadata == 0x0A) {
				// North
				return FACES_NORTH;
			} else if (signMetadata == 0x0B || signMetadata == 0x0C
					|| signMetadata == 0x0D || signMetadata == 0x0E) {
				// East
				return FACES_EAST;
			} else {
				// Non-cardinal angle
				return FACES_NON_CARDINAL;
			}
		} else {
			// Attached to wall. Note the different ordering.
			if (signMetadata == 0x02) {
				// North
				return FACES_NORTH;
			} else if (signMetadata == 0x03) {
				// South
				return FACES_SOUTH;
			} else if (signMetadata == 0x04) {
				// West
				return FACES_WEST;
			} else if (signMetadata == 0x05) {
				// East
				return FACES_EAST;
			} else {
				// Cannot happen. Something is wrong.
				// System.out
				// .println("SOMETHING IS WRONG: getSignFacing sees a wall sign facing at a non-right angle. Returning north. Value="
				// + signMetadata);
				return FACES_NORTH;
				// So return north, I guess.
			}
		}
	}

	/**
	 * Get the sign's facing in degrees, as if the sign were an entity's head.
	 * 
	 * @param signMetadata
	 * @param signType
	 * @return
	 */
	public static int getSignFacingDegrees(int signMetadata, Block signType) {
		if (signType == Block.signPost) {
			return (int) ((360f / 16f) * (float) signMetadata);
		} else {
			// Attached to wall. Note the different ordering.
			if (signMetadata == 0x02) {
				// North
				return 180;
			} else if (signMetadata == 0x03) {
				// South
				return 0;
			} else if (signMetadata == 0x04) {
				// West
				return 90;
			} else if (signMetadata == 0x05) {
				// East
				return 270;
			} else {
				// Cannot happen. Something is wrong.
				// System.out
				// .println("SOMETHING IS WRONG: getSignFacing sees a wall sign facing at a non-right angle. Returning north. Value="
				// + signMetadata);
				return FACES_NORTH;
				// So return north, I guess.
			}
		}
	}

	/**
	 * Returns the Block that represents the type of sign at a location
	 * 
	 * @param world
	 * @param point
	 * @return Block.signpost, Block.signWall, or null
	 */
	public static Block getSignBlockType(Point3D point, World world) {
		int blockId = world.getBlockId(point.x, point.y, point.z);
		if (blockId == 63) {
			// Signpost
			return Block.signPost;
		} else if (blockId == 68) {
			// Wall-sign
			return Block.signWall;
		} else {
			// Not a sign - return null
			return null;
		}
	}

	// /**
	// * Returns the Block that represents the type of sign at a location
	// *
	// * @param world
	// * @param point
	// * @param whitelist
	// * Can be null
	// * @return Block.signpost, Block.signWall, or null. Also, null if block is
	// * not on whitelist
	// */
	// public static Block getSignBlockTypeIfWhitelisted(Point3D point,
	// World world, LinkedList<Point3D> whitelist) {
	// // Check point against whitelist
	// if (whitelist != null) {
	// if (!whitelist.contains(point)) {
	// return null;
	// }
	// }
	//
	// int blockId = world.getBlockId(point.x, point.y, point.z);
	// if (blockId == 63) {
	// // Signpost
	// return Block.signPost;
	// } else if (blockId == 68) {
	// // Wall-sign
	// return Block.signWall;
	// } else {
	// // Not a sign - return null
	// return null;
	// }
	// }

	private static boolean isSign(Point3D point, World world) {
		return isSign(point.x, point.y, point.z, world);
	}

	private static boolean isSign(int x, int y, int z, World world) {
		int blockID = world.getBlockId(x, y, z);
		if (blockID == 63 || blockID == 68) {
			return true;
		} else {
			return false;
		}
	}

	private static void saveMidiFile(File saveFile, String tune,
			Point3D location) throws IOException, Exception {
		// Create midi save dir
		File midiDir = saveFile.getParentFile();
		midiDir.mkdirs();

		// Only one midi can be saved at any given instant
		synchronized (DittyPlayerThread.staticPlayerMutex) {
			// Save midi of tune to saveFile
			Player p = new Player();
			p.saveMidi(tune, saveFile);
			p.close();
		}

		// Particle
		for (int i = 0; i < 1; i++) {
			Minetunes.requestParticle(new MinetunesParticleRequest(location,
					"save", 0.0, 0.03, 0.03, 0.05, 0.2));
		}
	}

	public static void simpleLog(String logString) {
		if (MinetunesConfig.DEBUG) {
			System.out.println(logString);
		}
	}

	/**
	 * Finds the coords of the block this sign is attached to.
	 * 
	 * @param anchor
	 * @return the coords of the attached block: MAY RETURN INVALID COORDS!!! (y
	 *         = -1, ect).
	 */
	public static Point3D getBlockAttachedTo(TileEntitySign anchor) {
		Block blockType = anchor.blockType;
		Point3D returnBlock = new Point3D(anchor.xCoord, anchor.yCoord,
				anchor.zCoord);
		if (blockType == Block.signPost) {
			// Block is below sign
			returnBlock.y--;
		} else if (blockType == Block.signWall) {
			// Block is behind sign
			returnBlock = getCoordsRelativeToSign(anchor, 0, 0, -1);
		}
		return returnBlock;
	}

	/**
	 * Finds the coords of a block releative to a sign. Performs no range
	 * checking on result
	 */
	public static Point3D getCoordsRelativeToSign(TileEntitySign sign,
			int right, int up, int out) {
		Point3D newCoords = new Point3D(sign.xCoord, sign.yCoord, sign.zCoord);

		return getCoordsRelativeToSign(newCoords,
				getSignFacing(sign.blockMetadata, sign.blockType), right, up,
				out);
	}

	/**
	 * Finds the coords of a block releative to a sign. Performs no range
	 * checking on result
	 * 
	 * @param startCoords
	 * @param startSignFacing
	 * @param right
	 * @param up
	 * @param out
	 * @return
	 */
	public static Point3D getCoordsRelativeToSign(Point3D startCoords,
			int startSignFacing, int right, int up, int out) {
		Point3D newCoords = startCoords.clone();
		// Handle moving left or right

		// Focus on designated sign
		// Move right "right" blocks
		if (startSignFacing == FACES_NORTH) {
			// Right is west
			newCoords.x -= right;
		} else if (startSignFacing == FACES_SOUTH) {
			// Right is east
			newCoords.x += right;
		} else if (startSignFacing == FACES_EAST) {
			// Right is north
			newCoords.z -= right;
		} else if (startSignFacing == FACES_WEST) {
			// Right is south
			newCoords.z += right;
		}

		// Handle moving up or down

		// Move out "out" blocks
		// Focus on designated sign
		if (startSignFacing == FACES_NORTH) {
			// Out is north
			newCoords.z -= out;
		} else if (startSignFacing == FACES_SOUTH) {
			// Out is south
			newCoords.z += out;
		} else if (startSignFacing == FACES_EAST) {
			// Out is east
			newCoords.x += out;
		} else if (startSignFacing == FACES_WEST) {
			// Out is west
			newCoords.x -= out;
		}

		// Move up "up" blocks
		newCoords.y += up;

		return newCoords;
	}

}
