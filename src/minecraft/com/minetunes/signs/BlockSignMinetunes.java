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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.BlockSign;
import net.minecraft.src.ChatAllowedCharacters;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityItemFrame;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntitySign;
import net.minecraft.src.World;

import org.jfugue.Player;
import org.jfugue.parsers.MusicStringParser;

import com.minetunes.CueScheduler;
import com.minetunes.Minetunes;
import com.minetunes.PlayDittyFromSignWorkThread;
import com.minetunes.Point3D;
import com.minetunes.RightClickCheckThread;
import com.minetunes.config.MinetunesConfig;
import com.minetunes.disco.DiscoFloor;
import com.minetunes.ditty.Ditty;
import com.minetunes.ditty.DittyPlayerThread;
import com.minetunes.ditty.event.CreateBotEvent;
import com.minetunes.ditty.event.CreateEmitterEvent;
import com.minetunes.ditty.event.FireworkEvent;
import com.minetunes.ditty.event.NoteStartEvent;
import com.minetunes.ditty.event.PlayMidiDittyEvent;
import com.minetunes.ditty.event.SFXEvent;
import com.minetunes.ditty.event.SFXInstrumentEvent;
import com.minetunes.ditty.event.SFXInstrumentOffEvent;
import com.minetunes.ditty.event.VolumeEvent;
import com.minetunes.gui.signEditor.GuiEditSignMinetunes;
import com.minetunes.particle.ParticleRequest;
import com.minetunes.signs.keywords.AccelerateKeyword;
import com.minetunes.signs.keywords.DiscoKeyword;
import com.minetunes.signs.keywords.EmitterKeyword;
import com.minetunes.signs.keywords.ExplicitGotoKeyword;
import com.minetunes.signs.keywords.FireworkKeyword;
import com.minetunes.signs.keywords.GotoKeyword;
import com.minetunes.signs.keywords.LyricKeyword;
import com.minetunes.signs.keywords.MaxPlaysKeyword;
import com.minetunes.signs.keywords.NewBotKeyword;
import com.minetunes.signs.keywords.OctavesKeyword;
import com.minetunes.signs.keywords.OctavesOffKeyword;
import com.minetunes.signs.keywords.ParsedKeyword;
import com.minetunes.signs.keywords.PattKeyword;
import com.minetunes.signs.keywords.PatternKeyword;
import com.minetunes.signs.keywords.PreLyricKeyword;
import com.minetunes.signs.keywords.RepeatKeyword;
import com.minetunes.signs.keywords.SFXInstKeyword;
import com.minetunes.signs.keywords.SFXInstOffKeyword;
import com.minetunes.signs.keywords.SFXKeyword;
import com.minetunes.signs.keywords.StaccatoKeyword;
import com.minetunes.signs.keywords.SyncWithKeyword;
import com.minetunes.signs.keywords.TransposeKeyword;
import com.minetunes.signs.keywords.VolumeKeyword;

/**
 * Contains members which were previously located in
 * net.minecraft.src.BlockSign. NOT used to replace the vanilla BlockSign for
 * the fields Block.signWall and Block.signPost, does NOT extend BlockSign
 * 
 */
public class BlockSignMinetunes {

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
	// /**
	// * Timeouts when dealing with infinite loops
	// */
	// private static int infiniteLoopTimeout = 6;
	// // private static int infiniteLoopDialog = 2;

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
			if ((held == 271) || MinetunesConfig.getMinetunesOff() || MinetunesConfig.getBoolean("signs.disabled")) {
				// Holding wooden axe or signs disabled: do nothing.
			} else if (Minetunes.isIDShovel(held)) {
				// Shovel! "Scoop up" sign text.
				GuiEditSignMinetunes
						.addTextToSavedSigns(((TileEntitySign) par1World
								.getBlockTileEntity(parX, parY, parZ)).signText);
				Minetunes.writeChatMessage(par1World,
						"§2Sign's text has been saved.");
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
				signsReadList, dittyProperties, 0, signWhitelist);

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
						saveMidiFile(dittyProperties.getMidiSaveFile(), ditty);
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
	 * @return null if there's a problem, or a musicstring if the reading was
	 *         successful.
	 */
	private static StringBuilder readPattern(Point3D startPoint, World world,
			LinkedList<SignLogPoint> signsReadList, SignDitty ditty,
			int subpatternLevel, LinkedList<Point3D> signWhitelist) {
		// MCDitty.slowDownMC(2);

		// Load the settings for lyrics
		boolean lyricsEnabled = MinetunesConfig.getBoolean("lyrics.enabled");

		// Contains musicstring read from pattern
		StringBuilder readMusicString = new StringBuilder();

		// Get an ID for this subpattern
		int subpatternID = getNextSubpatternID();

		// // Make a clone of the signLog list, so that the one passed in does
		// not
		// // get contaminated
		// LinkedList<Point3D> signLog = (LinkedList<Point3D>) signsReadList
		// .clone();

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

		// // If this is true, loop is trying to find beginning of the current
		// line
		// // of signs by going left until it can't any more
		// boolean forceReadNewLine = false;

		// Loop through signs
		while (true) {
			// Get info and text of a sign
			// On each line:
			// Check for keywords
			// If there is music, add it to the musicString

			// When done, figure out what sign to read next

			// ***

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
			} else {
				// No infinite loop. All is well.
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
						.println("In playDittyFromSigns: the tile entity of a sign we are attempting to read does not exist. Strange.");
				System.err.println("Ending pattern: cannot continue.");
				break;
			}

			// If sign turned out to be blank, we can simply apply natural focus
			// flow to it, saving a ton of time (?)
			if (signIsEmpty) {
				// System.out.println ("Sign was blank!");
				nextSignPoint = applyNaturalFocusFlow(currSignPoint,
						currSignFacing, world, signWhitelist);
				// Swap the next sign's position in as the new current sign's
				// position for next iteration
				currSignPoint = nextSignPoint;
				nextSignPoint = currSignPoint.clone();
				continue;
			}

			// Get sign id
			int currSignIDNum = ditty.registerSignForID(currSignPoint);
			// // Add sign playing start event
			// int eventId = dittyProperties
			// .addDittyEvent(new SignPlayingTimedEvent(true,
			// currSignIDNum, dittyProperties.getDittyID()));
			// addMusicStringTokens(readMusicString, dittyProperties,
			// TIMED_EVENT_TOKEN + eventId, true);
			// Add sign location token
			ditty.addMusicStringTokens(readMusicString, SIGN_START_TOKEN
					+ currSignIDNum, false);

			// Process each line of a sign
			// Unless this is turned off by a keyword, natural focus flow will
			// occur

			boolean naturalFocusFlowEnabled = true;
			boolean gotosOnSign = false;
			boolean musicOnSign = false;

			// If a pattern is more than one sign, this is true.
			boolean patternIsMoreThanOneSign = false;
			// In a pattern that IS more than one sign, this is obviously set to
			// true.
			if (!currSignPoint.equals(startPoint)) {
				patternIsMoreThanOneSign = true;
			}

			// Get sign text
			String[] signText = currSignTileEntity.getSignTextNoCodes();

			// In a pattern that doesn't start on a pattern sign, the pattern
			// must be more than one sign
			String isPatternLine = SignParser.recognizeKeyword(signText[0]);
			if (isPatternLine == null || !isPatternLine.equals("pattern")) {
				// not a pattern sign
				patternIsMoreThanOneSign = true;
			}

			// Loop lines of the current sign
			for (int line = 0; line < signText.length; line++) {
				// Get the line of text to process
				String currLine = signText[line];
				// Trim whitespace
				currLine = currLine.trim();
				// Create lowercase version
				String currLineLowercase = currLine.toLowerCase();

				// Attempt to recognize a keyword
				String keyword = SignParser.recognizeKeyword(currLine);

				// Handle comments
				if (Comment.isLineComment(currLine)) {
					// Line is a comment; ignore
					ditty.addHighlight(currSignPoint, line,
							COMMENT_HIGHLIGHT_CODE);
					continue;
				}
				if (keyword != null) {
					// Keywords count as tokens
					ditty.incrementTotalTokens();

					// Highlight line of keyword on sign
					ditty.addHighlight(currSignPoint, line,
							KEYWORD_HIGHLIGHT_CODE);

					// Check for keywords; only if there is no keyword, read the
					// line as music
					if (keyword.equals("pattern")) {
						// Pattern can only be on the first line of a
						// sign; cast an error if it is misused
						if (line != 0) {
							// Pattern can only be used on first line of a sign
							ditty.addErrorMessage("§cPattern keywords should be on first line of a sign.");
							ditty.addErrorHighlight(currSignPoint, line);
						} else {
							// If this is the pattern sign that started the
							// current pattern, ignore the keyword
							// Fix: If this is the main song, read the keyword!
							if (!currSignPoint.equals(startPoint)
									|| subpatternLevel == 0) {
								// Parse keyword
								PatternKeyword patternKeyword = PatternKeyword
										.parse(currLine);

								if (!patternKeyword.isGoodKeyword()) {
									showKeywordError(ditty, currSignPoint,
											currLine, line, patternKeyword);
									break;
								}

								// Add the subpattern to this pattern the
								// specified
								// number of times
								// TODO: Better trimming?
								for (int i = 0; i < patternKeyword
										.getRepeatCount(); i++) {

									// Remove the current sign from the sign log
									// to
									// prevent duplicates: readPattern will
									// re-add
									// it
									signLog.removeLast();
									// Read pattern
									LinkedList<Point3D> subPatternSignLog = (LinkedList<Point3D>) signLog
											.clone();
									StringBuilder subPatternMusicString = readPattern(
											currSignPoint, world, signLog,
											ditty, subpatternLevel + 1,
											signWhitelist);

									// If a subpattern fails due to an infinte
									// loop,
									// pass the failure on
									if (subPatternMusicString == null) {
										// simpleLog("PATTERN: null failure on pattern");
										return null;
									}

									// Do not check for errors
									// readMusicString.append(" ").append(subPatternMusicString);
									ditty.addMusicStringTokens(readMusicString,
											subPatternMusicString.toString(),
											false);
								}

								// Ignore the contents of this sign; it has been
								// read by readPattern already.
								break;
							}
						}
					} else if (keyword.equals("patt")) {
						// TODO: CHECK THAT THESE ARE NOT NECESSARY!!!!!!!!
						// // Goto keyword; disable normal focus flow
						// naturalFocusFlowEnabled = false;
						// Gotos are on sign (obviously)
						// gotosOnSign = true;
						// A goto keyword automatically means a pattern is more
						// than one sign
						// patternIsMoreThanOneSign = true;

						// Parse keyword
						PattKeyword pattKeyword = PattKeyword.parse(currLine);

						// Show any errors as necessary
						if (!pattKeyword.isGoodKeyword()) {
							showKeywordError(ditty, currSignPoint, currLine,
									line, pattKeyword);
							break;
						}

						// Try to jump to sign with the given comment
						Comment match = GotoKeyword.getNearestMatchingComment(
								currSignPoint, world, pattKeyword.getComment());

						Point3D pattLocation;
						if (match == null) {
							// Simulate an explicit goto pointing at thin air
							// TODO: This is a hack. Please come up with a more
							// explicit solution.
							pattLocation = new Point3D(0, -1, 0);
						} else {
							pattLocation = match.getLocation().clone();
						}

						// Add the subpattern to this pattern the
						// specified
						// number of times
						// TODO: Better trimming?
						for (int i = 0; i < pattKeyword.getRepeatCount(); i++) {
							// Read pattern
							LinkedList<SignLogPoint> subPatternSignLog = (LinkedList<SignLogPoint>) signLog
									.clone();
							StringBuilder subPatternMusicString = readPattern(
									pattLocation, world, subPatternSignLog,
									ditty, subpatternLevel + 1, signWhitelist);

							// If a subpattern fails due to an infinte loop,
							// pass the failure on
							if (subPatternMusicString == null) {
								// simpleLog("PATTERN: null failure on pattern");
								return null;
							}

							if (!pattKeyword.isGoodKeyword()) {
								showKeywordError(ditty, currSignPoint,
										currLine, line, pattKeyword);
								break;
							}

							// Do not check for errors
							ditty.addMusicStringTokens(readMusicString,
									subPatternMusicString.toString(), false);

							// Note that we are back on the original sign in the
							// musicstring
							ditty.addMusicStringTokens(readMusicString,
									SIGN_START_TOKEN + currSignIDNum, false);
						}

					} else if (keyword.equals("goto")) {
						// Goto keyword; disable normal focus flow
						naturalFocusFlowEnabled = false;
						// Gotos are on sign (obviously)
						gotosOnSign = true;
						// A goto keyword automatically means a pattern is more
						// than one sign
						patternIsMoreThanOneSign = true;

						// Parse keyword
						GotoKeyword gotoKeyword = GotoKeyword.parse(currLine);

						// Show any errors as necessary
						if (!gotoKeyword.isGoodKeyword()) {
							showKeywordError(ditty, currSignPoint, currLine,
									line, gotoKeyword);
							break;
						}

						// Try to jump to sign with the given comment
						Comment match = GotoKeyword.getNearestMatchingComment(
								currSignPoint, world, gotoKeyword.getComment());
						if (match == null) {
							// Simulate an explicit goto pointing at thin air
							// TODO: This is a hack. Please come up with a more
							// explicit solution.
							nextSignPoint = new Point3D(0, -1, 0);
						} else {
							nextSignPoint = match.getLocation().clone();
						}
					} else if (keyword.equals("right")
							|| keyword.equals("left") || keyword.equals("up")
							|| keyword.equals("down") || keyword.equals("in")
							|| keyword.equals("out")) {
						// Goto keyword; disable normal focus flow
						naturalFocusFlowEnabled = false;
						// Gotos are on sign (obviously)
						gotosOnSign = true;
						// A goto keyword automatically means a pattern is more
						// than one sign
						patternIsMoreThanOneSign = true;

						// Parse keyword
						ExplicitGotoKeyword gotoKeyword = ExplicitGotoKeyword
								.parse(currLine);

						// Show any errors as necessary
						if (!gotoKeyword.isGoodKeyword()) {
							showKeywordError(ditty, currSignPoint, currLine,
									line, gotoKeyword);
							break;
						}

						int amount = gotoKeyword.getAmountMove();

						Point3D pointedAtSign = nextSignPoint.clone();

						// Decide the direction to move
						if (keyword.equals("right") || keyword.equals("left")) {
							// Handle moving left or right
							if (keyword.equals("left")) {
								amount = -amount;
							}

							// Adjust next sign position based on the amount to
							// move and the current sign's facing
							pointedAtSign = getCoordsRelativeToSign(
									nextSignPoint, currSignFacing, amount, 0, 0);
						}

						if (keyword.equals("in") || keyword.equals("out")) {
							// Handle moving up or down
							if (keyword.equals("in")) {
								amount = -amount;
							}

							// Adjust next sign position based on the amount to
							// move and the current sign's facing
							pointedAtSign = getCoordsRelativeToSign(
									nextSignPoint, currSignFacing, 0, 0, amount);
						}

						if (keyword.equals("up") || keyword.equals("down")) {
							// Handle moving up or down
							if (keyword.equals("down")) {
								amount = -amount;
							}

							// Adjust next sign position based on the amount to
							// move and the current sign's facing
							pointedAtSign = getCoordsRelativeToSign(
									nextSignPoint, currSignFacing, 0, amount, 0);
						}

						nextSignPoint = pointedAtSign;
					} else if (currLineLowercase.startsWith("end")
							&& !currLineLowercase.contains("line")) {
						// If the keyword is end, but NOT "end line" or
						// "endline"
						// End pattern
						endPattern = true;
						// Stop reading lines from sign
						break;
					} else if (currLineLowercase.startsWith("end")
							&& currLineLowercase.contains("line")) {
						// If keyword is either "endline" or "end line"
						// Force a newline
						naturalFocusFlowEnabled = false;
						nextSignPoint = carriageReturn(world, currSignPoint,
								signWhitelist);
						// Stop reading lines from sign
						break;
					} else if (keyword.equals("mute")) {
						// Set mute to on in the song's properties
						ditty.setMuting(true);
					} else if (keyword.equals("proximity")
							|| keyword.equals("proxpad")
							|| keyword.equals("area")) {
						// Do nothing here; it does not affect ditty
						// Just prevent the keyword from being read as music
					} else if (keyword.equals("midi")
							|| keyword.equals("savemidi")
							|| keyword.equals("playmidi")) {
						if ((line == 0) && !ditty.getMidiAlreadySaved()) {
							// Line 2 contains the filename
							String givenFilename = signText[1].trim();

							if (SignParser.recognizeKeyword(signText[1]) != null) {
								// If a keyword is found beneath the midi sign,
								// notify user
								ditty.addErrorHighlight(currSignPoint, 1);
								ditty.addErrorMessage("§cA keyword (§b"
										+ SignParser
												.recognizeKeyword(signText[1])
										+ "§c) was found on the second line of a midi sign instead of a filename.");
								// Bad filename: non-alphanumeric characters
								simpleLog("Bad filename is keyword: "
										+ givenFilename);
								ditty.setMidiAlreadySaved(true);
								break;
							} else if (!givenFilename.matches("[\\d\\w]*")
									&& (!givenFilename.equals(""))) {
								ditty.addErrorHighlight(currSignPoint, 1);
								ditty.addErrorMessage("§cA midi file name should only contain letters and numbers (no spaces)");
								// Bad filename: non-alphanumeric characters
								simpleLog("Bad filename: " + givenFilename);
								ditty.setMidiAlreadySaved(true);
								break;
							} else if (givenFilename.equals("")) {
								// Empty filenames are frowned upon
								ditty.addErrorHighlight(currSignPoint, 0);
								ditty.addErrorMessage("§cNo file name was written on the second line of the midi sign");
							}

							// Otherwise, good filename. Note it, and move on.
							// Will save later, once signs are read.
							File midiSaveFile = new File(MinetunesConfig
									.getMinetunesDir().getPath()
									+ File.separator + "midi", givenFilename
									+ ".mid");
							simpleLog("Good filename: midi is "
									+ midiSaveFile.getPath());

							if (keyword.equals("midi")
									|| keyword.equals("savemidi")) {
								ditty.setMidiSaveFile(midiSaveFile);
								// No music on this sign
								// Saved as given filename: don't save any more
								// midis of this song
								ditty.setMidiAlreadySaved(true);
							} else if (keyword.equals("playmidi")) {
								// Play a midi
								int eventID = ditty
										.addDittyEvent(new PlayMidiDittyEvent(
												midiSaveFile, ditty
														.getDittyID()));
								ditty.addMusicStringTokens(readMusicString,
										TIMED_EVENT_TOKEN + eventID, false);
							}

							// Regardless of whether the sign is heeded:
							// Ignore line 1 of sign; read rest of lines

							// Will be incremented to 2 after the continue by
							// the for loop
							line = 1;
							continue;
						} else if (line != 0) {
							// Tell user that you can only use midi on the first
							// line of a sign
							ditty.addErrorHighlight(currSignPoint, line);
							ditty.addErrorMessage("§cA midi keyword must be at the top of a sign. The second line will have the name of the midi file to save.");
						}
					} else if (keyword.equals("loud")) {
						// Word currently has no effect; this could change
						// sometime
						ditty.setLoud(true);
					} else if (keyword.equals("oneline")) {
						// Smash all lines below this keyword onto the one
						// line
						// (the one after this keyword)
						// Throw an error if there are keywords below
						String comboLine = "";
						for (int i = line + 1; i < signText.length; i++) {
							String onelineLine = signText[i];

							// Check for keywords
							String keywordFromOnelineLine = SignParser
									.recognizeKeyword(onelineLine);
							if (keywordFromOnelineLine != null) {
								// Highlight both this keyword and the offending
								// keyword being processed
								ditty.addErrorHighlight(currSignPoint, i);
								ditty.addErrorHighlight(currSignPoint, line);
								ditty.addErrorMessage("§cOneline cannot deal with the keyword (§b"
										+ keywordFromOnelineLine
										+ "§c): oneline only works with MusicString tokens. Remove any keywords from beneath Oneline keywords.");
								break;
							}

							// Check for comments; if line is not a comment, add
							// it.
							if (!Comment.isLineComment(onelineLine)) {
								comboLine += onelineLine;

								// TODO: Check for music
								if (onelineLine.trim().length() > 0) {
									musicOnSign = true;
								}
							}
						}

						// Add comboline to the music buffer
						if (!ditty.addMusicStringTokens(readMusicString,
								comboLine.trim(), true)) {
							// If the comboline contained errors, highlight all
							// lines in comboLine
							for (int i = line + 1; i < signText.length; i++) {
								ditty.addErrorHighlight(currSignPoint, i);
							}
						}

						// Do not read any more lines from this sign
						break;
					} else if (keyword.equals("repeat")) {

						int repetitions = 2;

						RepeatKeyword repeatKeyword = RepeatKeyword
								.parse(currLine);
						repetitions = repeatKeyword.getRepeatCount();

						// Read all lines of music below this keyword, and
						// duplicate them as they are added to the musicstring
						// Throw an error if there are keywords mixed in
						String comboLine = "";
						for (int i = line + 1; i < signText.length; i++) {
							String repeatLine = signText[i];

							// Check for keywords
							String repeatLineKeyword = SignParser
									.recognizeKeyword(repeatLine);
							if (repeatLineKeyword != null) {
								// Highlight both this keyword and the offending
								// keyword being processed
								ditty.addErrorHighlight(currSignPoint, i);
								ditty.addErrorHighlight(currSignPoint, line);
								ditty.addErrorMessage("§cRepeat cannot deal with the keyword (§b"
										+ repeatLineKeyword
										+ "§c): Repeat only works with MusicString tokens. Remove any keywords from beneath Repeat keywords, or try \"Pattern\" instead.");
								break;
							}

							// Check for comments; if line is not a comment, add
							// it.
							if (!Comment.isLineComment(repeatLine)) {
								comboLine += repeatLine + " ";
							}
						}

						// Add comboline to the music buffer a number of times
						for (int rep = 0; rep < repetitions; rep++) {
							// Add comboline to the music buffer
							boolean checkForErrors = true;
							if (rep != 0) {
								// Only check for errors on first append; this
								// eliminates duplicate errors
								checkForErrors = false;
							}
							if (!ditty.addMusicStringTokens(readMusicString,
									comboLine.trim(), checkForErrors)) {
								// If the comboline contained errors, highlight
								// all lines in comboLine
								for (int i = line + 1; i < signText.length; i++) {
									ditty.addErrorHighlight(currSignPoint, i);
								}
							} else {
								// Music on sign added succesfully
								// TODO: Check for music
								if (comboLine.trim().length() > 0) {
									musicOnSign = true;
								}
							}
						}

						// Do not read any more lines from this sign
						break;
					} else if (keyword.equals("reset")) {
						// Reset keyword: replace with a more
						// musicstring-neutral token
						// Do not check for errors! The token is a
						// MineTunes-only
						// token.
						ditty.addMusicStringTokens(readMusicString,
								getResetToken(), false);
					} else if (keyword.equals("lyric")) {
						// Lyric keyword

						// Parse keyword's arguments
						LyricKeyword l = LyricKeyword.parse(currLine);

						// Check that this isn't on the last line (no lyric text
						// possible), after parsing arguments.
						if (line >= LINES_ON_A_SIGN - 1) {
							// If this is on the last line of a sign, don't
							// bother to continue.
							// Note that we did tell user about any errors
							// before giving up here.

							// Skip this line.
							continue;
						}

						// Get lyric's text
						String lyricText = readLyricFromSign(line + 1,
								signText, l.getColorCode());

						// Adding to existing lyric?
						if (lyricsEnabled) {
							CueScheduler lyrics = ditty.getLyricsStorage();
							lyrics.addLyricText(l.getLabel(), lyricText,
									l.getRepetition());
						}

						// No more keywords or music on a the sign.
						break;
					} else if (keyword.equals("prelyric")) {
						// PreLyric keyword

						// Parse keyword's arguments
						PreLyricKeyword l = PreLyricKeyword.parse(currLine);

						// Check that this isn't on the last line (no lyric text
						// possible), after parsing arguments.
						if (line >= LINES_ON_A_SIGN - 1) {
							// If this is on the last line of a sign, don't
							// bother to continue.
							// Note that we did tell user about any errors
							// before giving up here.

							// Skip this line.
							continue;
						}

						// Get lyric's text
						String lyricText = readLyricFromSign(line + 1,
								signText, l.getColorCode());

						// Adding to existing lyric?
						if (lyricsEnabled) {
							CueScheduler lyrics = ditty.getLyricsStorage();
							lyrics.addLyricPreText(l.getLabel(), lyricText);
						}

						// No more keywords or music on a the sign.
						break;
					} else if (keyword.equals("oneatatime")) {
						// Set a flag in the dittyproperties
						ditty.setOneAtATime(true);
					} else if (keyword.equals("isditty")) {
						// Set a flag in the dittyproperties
						ditty.setForceGoodDittyDetect(true);
					} else if (keyword.equals("syncvoices")) {
						// Add a token
						ditty.addMusicStringTokens(readMusicString,
								SYNC_VOICES_TOKEN, false);
					} else if (keyword.equals("syncwith")) {
						// Read arguments
						SyncWithKeyword k = SyncWithKeyword.parse(currLine);

						// Finally, add token
						if (k.getLayer() != -1000) {
							ditty.addMusicStringTokens(readMusicString,
									SYNC_WITH_TOKEN + "V" + k.getVoice() + "L"
											+ k.getLayer(), false);
						} else {
							ditty.addMusicStringTokens(
									readMusicString,
									SYNC_WITH_TOKEN + "V" + k.getVoice() + "Lu",
									false);
						}
					} else if (keyword.equals("sfx")) {
						// Add fx event and token to musicstring

						// Get argument
						SFXKeyword k = SFXKeyword.parse(currLine);

						// Add event
						int eventID = ditty.addDittyEvent(new SFXEvent(k
								.getEffectName(), -1, ditty.getDittyID()));
						// Add token
						ditty.addMusicStringTokens(readMusicString,
								TIMED_EVENT_TOKEN + eventID, false);
					} else if (keyword.equals("disco")) {
						// Handle disco floors

						DiscoKeyword k = DiscoKeyword.parse(currLine);
						// Register a disco floor into the ditty properties
						DiscoFloor newFloor = new DiscoFloor(
								currSignTileEntity, k.getVoices());
						ditty.addDiscoFloor(newFloor);
						// System.out.println("DISCO FLOOR ADDED");
					} else if (keyword.equals("tutorial")) {
						// STUFF FOR THE TUTORIAL LEVEL
						// Executes immediately, right here, for convenience
						String[] args = currLineLowercase.split(" ");
						if (args[1].equals("tex")) {
							// Does nothing for now
							// ModLoader.getMinecraftInstance().texturePackList.setTexturePack(new
							// MineTunesTexturePack());
						}
					} else if (keyword.equals("volume")) {
						// Inserts a volume token into the song
						VolumeKeyword k = VolumeKeyword.parse(currLine);
						ditty.addMusicStringTokens(readMusicString,
								getAdjustedVolumeToken(k.getVolume(), ditty),
								false);
					} else if (keyword.equals("emitter")) {
						// Creates a create emitter event in the ditty, with
						// corresponding token
						// TODO: Handle errors
						ParsedSign parsedSign = SignParser.parseSign(signText);
						SignParser.parseKeywordInContext(parsedSign, line);
						EmitterKeyword k = (EmitterKeyword) parsedSign
								.getLine(line);
						if (k.isGoodKeyword() == false) {
							// Bad emitter!
							// Highlight this keyword
							for (int i = 0; i < 4; i++) {
								ditty.addErrorHighlight(currSignPoint, i);
							}
							ditty.addErrorMessage("§c" + k.getErrorMessage());
							break;
						} else {
							int eventID = ditty
									.addDittyEvent(new CreateEmitterEvent(k,
											-1, ditty.getDittyID(),
											currSignPoint.clone()));
							ditty.addMusicStringTokens(readMusicString,
									TIMED_EVENT_TOKEN + eventID, false);
						}
						// No more music on sign
						break;
					} else if (keyword.equals("sfxinst")
							|| keyword.equals("sfxinst2")) {
						// TODO: Move sign parsing so that it happens just once
						// per sign
						// Creates a create emitter event in the ditty, with
						// corresponding token
						// TODO: Handle errors
						ParsedSign parsedSign = SignParser.parseSign(signText);
						SignParser.parseKeywordInContext(parsedSign, line);
						SFXInstKeyword k = (SFXInstKeyword) parsedSign
								.getLine(line);
						if (k.isGoodKeyword() == false) {
							// Bad keyword!
							// Highlight this keyword
							for (int i = line; i < line + 2; i++) {
								ditty.addErrorHighlight(currSignPoint, i);
							}
							ditty.addErrorMessage("§c" + k.getErrorMessage());
							break;
						} else {
							int eventID = ditty
									.addDittyEvent(new SFXInstrumentEvent(k,
											-1, ditty.getDittyID()));
							ditty.addMusicStringTokens(readMusicString,
									TIMED_EVENT_TOKEN + eventID, false);
						}
						// Skip ahead a couple of lines (keyword is 2 lines
						// long)
						line += 1;
					} else if (keyword.equals("sfxinstoff")) {
						SFXInstOffKeyword k = (SFXInstOffKeyword) SignParser
								.parseKeyword(currLine);

						// Add keyword to schedule
						int eventID = ditty
								.addDittyEvent(new SFXInstrumentOffEvent(k, -1,
										ditty.getDittyID()));
						ditty.addMusicStringTokens(readMusicString,
								TIMED_EVENT_TOKEN + eventID, false);
					} else if (keyword.equals("newbot")) {
						// TODO: Move sign parsing so that it happens just once
						// per sign
						// Creates a create emitter event in the ditty, with
						// corresponding token
						// TODO: Handle errors
						ParsedSign parsedSign = SignParser.parseSign(signText);
						SignParser.parseKeywordInContext(parsedSign, line);
						NewBotKeyword k = (NewBotKeyword) parsedSign
								.getLine(line);
						if (k.isGoodKeyword() == false) {
							// Bad keyword!
							// Highlight this keyword
							for (int i = line; i < line + 2; i++) {
								ditty.addErrorHighlight(currSignPoint, i);
							}
							ditty.addErrorMessage("§c" + k.getErrorMessage());
							break;
						} else {
							// If the position is null, search upwards for a
							// space for the bot
							boolean searchUp = (k.getPosition() == null);

							int yOffset = 0;
							if (k.getPosition() != null) {
								yOffset = k.getPosition();
							}

							// Create the bot event
							CreateBotEvent botEvent = new CreateBotEvent(
									currSignPoint.x + 0.5f, currSignPoint.y
											+ yOffset, currSignPoint.z + 0.5f,
									k.getType(), getSignFacingDegrees(
											currSignMetadata, currSignType),
									searchUp, k.getName(), ditty.getDittyID());

							// Add the event to the ditty
							int eventID = ditty.addDittyEvent(botEvent);
							ditty.addMusicStringTokens(readMusicString,
									TIMED_EVENT_TOKEN + eventID, false);
						}
						// Skip ahead a couple of lines (keyword is 2 lines
						// long)
						line += 1;
					} else if (keyword.equals("staccato")) {
						// Create and add a staccato note effect token
						StaccatoKeyword staccatoKeyword = (StaccatoKeyword) SignParser
								.parseKeyword(currLine);

						String staccatoToken = createNoteEffectToken(false,
								NOTE_EFFECT_STACCATO,
								staccatoKeyword.getEighths(),
								staccatoKeyword.getDuration());

						ditty.addMusicStringTokens(readMusicString,
								staccatoToken, false);
					} else if (keyword.equals("staccatooff")) {
						ditty.addMusicStringTokens(
								readMusicString,
								createNoteEffectToken(true,
										NOTE_EFFECT_STACCATO), false);
					} else if (keyword.equals("tran")) {
						// Add a transpose note effect token
						TransposeKeyword k = (TransposeKeyword) SignParser
								.parseKeyword(currLine);
						String token = createNoteEffectToken(false,
								NOTE_EFFECT_TRANSPOSE, k.getTones(),
								k.getDuration());
						ditty.addMusicStringTokens(readMusicString, token,
								false);
					} else if (keyword.equals("tranoff")) {
						ditty.addMusicStringTokens(
								readMusicString,
								createNoteEffectToken(true,
										NOTE_EFFECT_TRANSPOSE), false);
					} else if (keyword.equals("octaves")) {
						// Add an octaves note effect token
						OctavesKeyword k = (OctavesKeyword) SignParser
								.parseKeyword(currLine);
						Object[] octaves = k.getOctaves().toArray(
								new Integer[0]);
						String token = createNoteEffectToken(false,
								NOTE_EFFECT_OCTAVES, octaves);
						ditty.addMusicStringTokens(readMusicString, token,
								false);
					} else if (keyword.equals("octavesoff")) {
						// Add an octaves off note effect token
						OctavesOffKeyword k = (OctavesOffKeyword) SignParser
								.parseKeyword(currLine);
						Object[] octaves = k.getOctaves().toArray(
								new Integer[0]);
						String token = createNoteEffectToken(true,
								NOTE_EFFECT_OCTAVES, octaves);
						ditty.addMusicStringTokens(readMusicString, token,
								false);
					} else if (keyword.equals("accel")) {
						// Add a accelerate note effect token
						AccelerateKeyword k = (AccelerateKeyword) SignParser
								.parseKeyword(currLine);
						String token = createNoteEffectToken(false,
								NOTE_EFFECT_ACCELERATE, k.getBPM(),
								k.getDuration());
						ditty.addMusicStringTokens(readMusicString, token,
								false);
					} else if (keyword.equals("ditty")
							|| keyword.equals("[ditty]") || keyword.equals("[signtune]")) {
						// Do nothing here
					} else if (keyword.equals("maxplays")) {
						// Add maxplay lock point
						MaxPlaysKeyword k = (MaxPlaysKeyword) SignParser
								.parseKeyword(currLine);
						ditty.addMaxPlayLockPoint(currSignPoint,
								k.getMaxPlays());
					} else if (keyword.equals("playlast")) {
						// Set playlast to true
						ditty.setPlayLast(true);
					} else if (keyword.equals("firework")) {
						FireworkKeyword k = (FireworkKeyword) SignParser
								.parseKeyword(currLine);
						// Find nearby fireworks in frames
						LinkedList<ItemStack> fireworks = new LinkedList<ItemStack>();
						for (Object entityObj : world.loadedEntityList) {
							Entity entity = (Entity) entityObj;
							if (Math.abs(entity.posX - currSignPoint.x) <= 2
									&& Math.abs(entity.posY - currSignPoint.y) <= 2
									&& Math.abs(entity.posZ - currSignPoint.z) <= 2) {

								if (entity instanceof EntityItemFrame) {
									EntityItemFrame frame = (EntityItemFrame) entity;
									ItemStack framedItem = frame
											.getDisplayedItem();

									if (framedItem != null
											&& framedItem.itemID == 401) {
										fireworks.add(framedItem);
									}
								}
							}
						}

						if (fireworks.size() > 0) {
							// Choose a firework
							ItemStack fireworkItem = fireworks.get(random
									.nextInt(fireworks.size()));

							// Create the event
							int yOffset = k.getUp();
							FireworkEvent event = new FireworkEvent(
									currSignPoint.x + 0.5f, currSignPoint.y
											+ yOffset, currSignPoint.z + 0.5f,
									fireworkItem, ditty.getDittyID());

							// Add the event to the ditty
							int eventID = ditty.addDittyEvent(event);
							ditty.addMusicStringTokens(readMusicString,
									TIMED_EVENT_TOKEN + eventID, false);
						} else {
							// No fireworks :(
							ditty.addErrorMessage("A firework sign has no fireworks in Item Frames nearby.");
							ditty.addErrorHighlight(currSignPoint, line);
						}
					} else {
						// Unrecognized keyword; announce with error
						ditty.addErrorMessage("§b"
								+ keyword
								+ "§c was recognized as a keyword, but no action was given for it in readPattern. This is a bug in MineTunes.");
						ditty.addErrorHighlight(currSignPoint, line);
					}
				} else {
					// Line contians music
					boolean noErrors = ditty.addMusicStringTokens(
							readMusicString, currLine, true);
					if (!noErrors) {
						ditty.addErrorHighlight(currSignPoint, line);
					}
					ditty.addHighlight(currSignPoint, line,
							MUSICSTRING_HIGHLIGHT_CODE);

					// Confirm that sign contains music
					if (currLine.trim().length() > 0) {
						musicOnSign = true;
					}
				}
			}

			// Add sign hit end token
			ditty.addMusicStringTokens(readMusicString, SIGN_END_TOKEN
					+ currSignIDNum, false);
			// // Add sign hit end ditty event
			// int eventId2 = dittyProperties
			// .addDittyEvent(new SignPlayingTimedEvent(false,
			// currSignIDNum, dittyProperties.getDittyID()));
			// addMusicStringTokens(readMusicString, dittyProperties,
			// TIMED_EVENT_TOKEN + eventId2, false);

			// Account for a one-sign pattern with no gotos
			// (Does not have natural focus flow, and ends on the sign it
			// started at)
			// FIXED: If main song contains a pattern sign (which is the first
			// sign in a song)
			// that has no gotos on it, do not end song here
			if (!patternIsMoreThanOneSign && !(subpatternLevel == 0)) {
				break;
			}

			// If the pattern has ended, stop reading more signs.
			if (endPattern) {
				break;
			}

			// If natural focus flow hasn't been interrupted, flow.
			if (naturalFocusFlowEnabled) {
				nextSignPoint = applyNaturalFocusFlow(currSignPoint,
						currSignFacing, world, signWhitelist);
			}

			// If natural focus flow is not enabled (i.e. the song contains
			// gotos), throw an error if the gotos are pointing at thin air
			// And there isn't a whitelist
			if (gotosOnSign && !naturalFocusFlowEnabled
					&& signWhitelist == null) {
				// Save myself a null pointer exception by only coming up with
				// the next block type if nextSignPoint isn't null
				int nextBlockType = 0;
				if (nextSignPoint != null) {
					nextBlockType = world.getBlockId(nextSignPoint.x,
							nextSignPoint.y, nextSignPoint.z);
				}
				if (!(nextBlockType == Block.signPost.blockID || nextBlockType == Block.signWall.blockID)) {
					// Thin air. Throw error.
					String signText2 = "";
					for (String s : currSignTileEntity.signText) {
						if (s.trim().length() > 0) {
							signText2 += "/" + s.trim();
						}
					}
					// Remove first /
					try {
						signText2 = signText2.substring(1, signText2.length());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ditty.addErrorMessage("§b" + signText2
							+ "§c: The gotos don't point at a sign.");
					// Highlight all lines of the offending sign
					for (int i = 0; i < LINES_ON_A_SIGN; i++) {
						ditty.addErrorHighlight(currSignPoint, 0);
					}
				}
			}

			// Swap the next sign's position in as the new current sign's
			// position for next iteration
			currSignPoint = nextSignPoint;
			nextSignPoint = currSignPoint.clone();

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
			ParsedKeyword keyword) {
		dittyProperties.addErrorHighlight(currSignPoint, line);
		if (keyword.getErrorMessageType() == ParsedKeyword.ERROR) {
			dittyProperties.addErrorMessage("§b" + currLine + "§c: "
					+ keyword.getErrorMessage());
		} else if (keyword.getErrorMessageType() == ParsedKeyword.WARNING) {
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

	private static void saveMidiFile(File saveFile, String tune)
			throws IOException, Exception {
		// Create midi save dir
		File midiDir = saveFile.getParentFile();
		midiDir.mkdirs();

		// Clear old midis
		clearMidis(saveFile.getName().substring(0,
				saveFile.getName().lastIndexOf(".")));

		// Only one midi can be saved at any given instant
		// TODO: Consider delay
		// TODO: Remove delay
		// TODO: Consider moving the actual mutexed saving code to
		// JFuguePlayerThread
		synchronized (DittyPlayerThread.staticPlayerMutex) {
			// Save midi of tune to saveFile
			Player p = new Player();

			// Ding dong resets are dead!
			// Midi saving is sane again!
			p.saveMidi(tune, saveFile);

			// Close player used for saving midis
			p.close();

			// The oft-considered delay
			// Thread.sleep(25);
		}
	}

	/**
	 * This is not obsolete yet: old versions used to create tons of midi files,
	 * splitting at resets. This clears those legacy files.
	 * 
	 * @param dittyName
	 */
	private static void clearMidis(String dittyName) {
		File midiDir = new File(Minecraft.getMinecraftDir().getPath()
				+ "/midi/");
		// Clear first file, if it exists
		File firstFile = new File(midiDir.getPath() + File.separator
				+ dittyName + ".mid");
		simpleLog("Checking for file: " + firstFile.getPath());
		if (firstFile.exists()) {
			simpleLog("Clearing midi file: " + firstFile.getPath());
			firstFile.delete();

			// Look for any consecutive subsequent files
			for (int i = 1; true; i++) {
				File nextFile = new File(midiDir.getPath() + File.separator
						+ dittyName + "-" + String.format("%03d", i) + ".mid");
				simpleLog("Checking for file: " + nextFile.getPath());
				if (nextFile.exists()) {
					simpleLog("Clearing midi file: " + nextFile.getPath());
					nextFile.delete();
				} else {
					break;
				}
			}
		}
	}

	public static ArrayList<String[]> importSignsFromFile(File f)
			throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		// Check first line to see if this file is valid
		String firstLine = in.readLine();
		if (firstLine != null) {
			if (!firstLine.trim().equalsIgnoreCase("Exported Signs")) {
				in.close();
				return null;
			}
		} else {
			in.close();
			return null;
		}

		ArrayList<String[]> signTexts = new ArrayList<String[]>();
		String lineIn = "";
		while (lineIn != null) {
			String[] signText = new String[4];
			for (int i = 0; i < 4; i++) {
				lineIn = in.readLine();
				if (lineIn == null) {
					break;
				} else if (lineIn.equalsIgnoreCase("###############x")) {
					// Ignore line.
					i--;
					continue;
				} else if (lineIn.equalsIgnoreCase("End of Exported Signs")) {
					// End of file.
					lineIn = null;
					break;
				} else {
					signText[i] = lineIn;
					// Truncate string to length
					if (signText[i].length() > 15) {
						signText[i] = signText[i].substring(0, 15);
					}
					// Filter out extra characters
					for (int loc = 0; loc < signText[i].length(); loc++) {
						if (!ChatAllowedCharacters
								.isAllowedCharacter((signText[i].toCharArray()[loc]))) {
							signText[i].replace(signText[i].toCharArray()[loc],
									' ');
						}
					}
				}
			}
			signTexts.add(signText);
		}

		in.close();
		return signTexts;
	}

	public static void exportSignsToFile(List<String[]> signs, File f)
			throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(f));
		out.write("Exported Signs");
		out.newLine();
		for (String[] sign : signs) {
			out.write("###############X");
			out.newLine();
			for (String line : sign) {
				out.write(line);
				out.newLine();
			}
		}
		out.write("End of Exported Signs");
		out.close();
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
