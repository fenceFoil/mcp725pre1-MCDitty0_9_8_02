package net.minecraft.src;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import net.minecraft.client.Minecraft;

import org.jfugue.JFugueException;
import org.jfugue.Player;
import org.jfugue.parsers.MusicStringParser;

import com.wikispaces.mcditty.CompareVersion;
import com.wikispaces.mcditty.GetMinecraft;
import com.wikispaces.mcditty.CueScheduler;
import com.wikispaces.mcditty.MCDitty;
import com.wikispaces.mcditty.MCDittyRightClickCheckThread;
import com.wikispaces.mcditty.MuteDittyThread;
import com.wikispaces.mcditty.PlayDittyFromSignWorkThread;
import com.wikispaces.mcditty.Point3D;
import com.wikispaces.mcditty.autoUpdate.RunCommandThread;
import com.wikispaces.mcditty.config.MCDittyConfig;
import com.wikispaces.mcditty.disco.DiscoFloor;
import com.wikispaces.mcditty.ditty.Ditty;
import com.wikispaces.mcditty.ditty.DittyPlayerThread;
import com.wikispaces.mcditty.ditty.event.CreateBotEvent;
import com.wikispaces.mcditty.ditty.event.CreateEmitterEvent;
import com.wikispaces.mcditty.ditty.event.CueEvent;
import com.wikispaces.mcditty.ditty.event.NoteStartEvent;
import com.wikispaces.mcditty.ditty.event.PlayMidiDittyEvent;
import com.wikispaces.mcditty.ditty.event.SFXInstrumentEvent;
import com.wikispaces.mcditty.ditty.event.SFXInstrumentOffEvent;
import com.wikispaces.mcditty.ditty.event.SFXMCDittyEvent;
import com.wikispaces.mcditty.ditty.event.VolumeEvent;
import com.wikispaces.mcditty.gui.GuiMCDittyChangelog;
import com.wikispaces.mcditty.particle.NoteParticleRequest;
import com.wikispaces.mcditty.particle.ParticleRequest;
import com.wikispaces.mcditty.resources.MCDittyResourceManager;
import com.wikispaces.mcditty.signs.Comment;
import com.wikispaces.mcditty.signs.ParsedSign;
import com.wikispaces.mcditty.signs.SignDitty;
import com.wikispaces.mcditty.signs.SignLine;
import com.wikispaces.mcditty.signs.SignLineHighlight;
import com.wikispaces.mcditty.signs.SignParser;
import com.wikispaces.mcditty.signs.keywords.DiscoKeyword;
import com.wikispaces.mcditty.signs.keywords.EmitterKeyword;
import com.wikispaces.mcditty.signs.keywords.ExplicitGotoKeyword;
import com.wikispaces.mcditty.signs.keywords.GotoKeyword;
import com.wikispaces.mcditty.signs.keywords.LyricKeyword;
import com.wikispaces.mcditty.signs.keywords.NewBotKeyword;
import com.wikispaces.mcditty.signs.keywords.ParsedKeyword;
import com.wikispaces.mcditty.signs.keywords.PatternKeyword;
import com.wikispaces.mcditty.signs.keywords.RepeatKeyword;
import com.wikispaces.mcditty.signs.keywords.SFXInstKeyword;
import com.wikispaces.mcditty.signs.keywords.SFXInstOffKeyword;
import com.wikispaces.mcditty.signs.keywords.SFXKeyword;
import com.wikispaces.mcditty.signs.keywords.StaccatoKeyword;
import com.wikispaces.mcditty.signs.keywords.SyncWithKeyword;
import com.wikispaces.mcditty.signs.keywords.TransposeKeyword;
import com.wikispaces.mcditty.signs.keywords.VolumeKeyword;
import com.wikispaces.mcditty.test.SignLogPoint;
import com.wikispaces.mcditty.test.TileEntitySkullRenderer2;

public class BlockSign extends BlockContainer {
	private Class signEntityClass;

	/** Whether this is a freestanding sign or a wall-mounted sign */
	private boolean isFreestanding;

	private static boolean isMCDittyLoaded = false;

	protected BlockSign(int par1, Class par2Class, boolean par3) {
		super(par1, Material.wood);
		isFreestanding = par3;
		blockIndexInTexture = 4;
		signEntityClass = par2Class;
		float f = 0.25F;
		float f1 = 1.0F;
		setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f1, 0.5F + f);

		// As this is called during game load, init MCDitty here as well
		if (!isMCDittyLoaded) {
			initMCDittyMod();
		}
	}

	/**
	 * Returns a bounding box from the pool of bounding boxes (this means this
	 * box can change after the pool has been cleared to be reused)
	 */
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World,
			int par2, int par3, int i) {
		return null;
	}

	/**
	 * Returns the bounding box of the wired rectangular prism to render.
	 */
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World par1World,
			int par2, int par3, int par4) {
		setBlockBoundsBasedOnState(par1World, par2, par3, par4);
		return super
				.getSelectedBoundingBoxFromPool(par1World, par2, par3, par4);
	}

	/**
	 * Updates the blocks bounds based on its current state. Args: world, x, y,
	 * z
	 */
	public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess,
			int par2, int par3, int par4) {
		if (isFreestanding) {
			return;
		}

		int i = par1IBlockAccess.getBlockMetadata(par2, par3, par4);
		float f = 0.28125F;
		float f1 = 0.78125F;
		float f2 = 0.0F;
		float f3 = 1.0F;
		float f4 = 0.125F;
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

		if (i == 2) {
			setBlockBounds(f2, f, 1.0F - f4, f3, f1, 1.0F);
		}

		if (i == 3) {
			setBlockBounds(f2, f, 0.0F, f3, f1, f4);
		}

		if (i == 4) {
			setBlockBounds(1.0F - f4, f, f2, 1.0F, f1, f3);
		}

		if (i == 5) {
			setBlockBounds(0.0F, f, f2, f4, f1, f3);
		}
	}

	/**
	 * The type of render function that is called for this block
	 */
	public int getRenderType() {
		return -1;
	}

	/**
	 * If this block doesn't render as an ordinary block it will return False
	 * (examples: signs, buttons, stairs, etc)
	 */
	public boolean renderAsNormalBlock() {
		return false;
	}

	public boolean getBlocksMovement(IBlockAccess par1IBlockAccess, int par2,
			int par3, int i) {
		return true;
	}

	/**
	 * Is this block (a) opaque and (b) a full 1m cube? This determines whether
	 * or not to render the shared face of two adjacent blocks and also whether
	 * the player can attach torches, redstone wire, etc to this block.
	 */
	public boolean isOpaqueCube() {
		return false;
	}

	// /**
	// * Returns the TileEntity used by this block.
	// */
	// public TileEntity getBlockEntity() {
	// MCP 1.3.1 naming quirk has rendered this method's name into gibberish
	public TileEntity createNewTileEntity(World par1World) {
		try {
			return (TileEntity) signEntityClass.newInstance();
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * Returns the ID of the items to drop on destruction.
	 */
	public int idDropped(int par1, Random par2Random, int par3) {
		return Item.sign.shiftedIndex;
	}

	public int func_71922_a(World par1World, int par2, int par3, int par4) {
		return Item.sign.shiftedIndex;
	}

	/**
	 * 
	 * Following code (excluding code above this notice):
	 * 
	 * Copyright (c) 2012 William Karnavas All Rights Reserved
	 * 
	 */

	/**
	 * 
	 * This file is part of MCDitty.
	 * 
	 * MCDitty is free software: you can redistribute it and/or modify it under
	 * the terms of the GNU Lesser General Public License as published by the
	 * Free Software Foundation, either version 3 of the License, or (at your
	 * option) any later version.
	 * 
	 * MCDitty is distributed in the hope that it will be useful, but WITHOUT
	 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
	 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
	 * License for more details.
	 * 
	 * You should have received a copy of the GNU Lesser General Public License
	 * along with MCDitty. If not, see <http://www.gnu.org/licenses/>.
	 * 
	 */

	/**
	 * Following code is for the MCDitty mod, and is (mostly) not code written
	 * by Mojang AB
	 */

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
	public static LinkedList<Point3D> oneAtATimeSignsBlocked = new LinkedList<Point3D>();

	/**
	 * Lets the block know when one of its neighbor changes. Doesn't know which
	 * neighbor changed (coordinates passed are their own) Args: x, y, z,
	 * neighbor blockID
	 */
	@Override
	public void onNeighborBlockChange(World par1World, int par2, int par3,
			int par4, int par5) {
		// Redstone properties similar to BlockNote: Check whether this sign is
		// powered by redstone
		if (par5 > 0) {
			boolean isBeingPoweredNow = par1World
					.isBlockIndirectlyGettingPowered(par2, par3, par4);
			TileEntitySign tileEntitySign = (TileEntitySign) par1World
					.getBlockTileEntity(par2, par3, par4);

			boolean lastRedstoneState;
			if (tileEntitySign != null) {
				lastRedstoneState = tileEntitySign.redstoneState;
			} else {
				// Just say that lastRedstoneState is on.
				lastRedstoneState = true;
			}

			if (tileEntitySign != null
					&& lastRedstoneState != isBeingPoweredNow) {
				if (isBeingPoweredNow) {
					playDittyFromSigns(par1World, par2, par3, par4);
				}

				// Set the lastRedstoneState flag in the TileEntitySign
				tileEntitySign.redstoneState = isBeingPoweredNow;
			}
		}

		// Rest of method is by Mojang AB: Check whether the block the sign is
		// mounted on has broken.
		boolean flag = false;

		if (isFreestanding) {
			if (!par1World.getBlockMaterial(par2, par3 - 1, par4).isSolid()) {
				flag = true;
			}
		} else {
			int i = par1World.getBlockMetadata(par2, par3, par4);
			flag = true;

			if (i == 2
					&& par1World.getBlockMaterial(par2, par3, par4 + 1)
							.isSolid()) {
				flag = false;
			}

			if (i == 3
					&& par1World.getBlockMaterial(par2, par3, par4 - 1)
							.isSolid()) {
				flag = false;
			}

			if (i == 4
					&& par1World.getBlockMaterial(par2 + 1, par3, par4)
							.isSolid()) {
				flag = false;
			}

			if (i == 5
					&& par1World.getBlockMaterial(par2 - 1, par3, par4)
							.isSolid()) {
				flag = false;
			}
		}

		if (flag) {
			dropBlockAsItem(par1World, par2, par3, par4,
					par1World.getBlockMetadata(par2, par3, par4), 0);
			par1World.setBlockWithNotify(par2, par3, par4, 0);
		}

		super.onNeighborBlockChange(par1World, par2, par3, par4, par5);
	}

	public void onEntityCollidedWithBlock(World world, int x, int y, int z,
			Entity entity) {
		// In MCDitty 0.9.5.01+, Proximity is handled by creating a 1x1 proxpad.
	}

	private void playDittyFromSigns(World world, int x, int y, int z) {
		playDittyFromSigns(world, x, y, z, false);
	}

	public static boolean clickHeld = false;

	/**
	 * In Minecraft 1.2.5 and below, this was called when a player clicked a
	 * block. It has been moved to the server's side in 1.3.1, and is no longer
	 * called except by MCDitty functions.
	 * 
	 * @param par1World
	 * @param parX
	 * @param parY
	 * @param parZ
	 * @param entityplayer
	 * @return
	 */
	public boolean blockActivated(final World par1World, final int parX,
			final int parY, final int parZ, EntityPlayer entityplayer) {
		// This is to prevent multiple activations on one click
		if (!clickHeld) {
			clickHeld = true;
			MCDittyRightClickCheckThread t = new MCDittyRightClickCheckThread();
			t.start();

			// System.out.println ("BlockActivated");
			// If player is not holding a shovel... (or wooden axe)
			ItemStack heldStack = entityplayer.getCurrentEquippedItem();
			int held = 0;
			if (heldStack != null) {
				held = heldStack.itemID;
				// System.out.println (held);
			}
			if ((held == 271)) {
				// Holding wooden axe: do nothing.
			} else if (held == 256 || held == 269 || held == 273 || held == 277
					|| held == 284) {
				// Shovel! "Scoop up" sign text.
				GuiEditSign.addTextToSavedSigns(((TileEntitySign) par1World
						.getBlockTileEntity(parX, parY, parZ)).signText);
				writeChatMessage(par1World, "§2Sign's text has been saved.");
			} else {
				playDittyFromSigns(par1World, parX, parY, parZ);
			}
		}
		return true;
	}

	// Loud signs send a chat message starting with this:
	// Odd case reduces odds of a normal person chatting this
	// public static final String MCDITTY_LOUD_CHAT_START = "McDiTtY:";

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
		Thread t = new PlayDittyFromSignWorkThread(world, x, y, z,
				oneAtATimeOn, false, null);
		t.setPriority(Thread.MAX_PRIORITY);
		t.start();
	}

	/**
	 * Performs the hard work of reading signs from a world, generating a
	 * DittyProperties and musicString from them, and playing them with JFugue.
	 * 
	 * The Core and Star Method of MCDitty, in other words.
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

		long startTime = System.nanoTime();

		// Update config file
		MCDittyConfig.checkConfig(world);

		// First, check to see if this sign is blocked from activating by a
		// OneAtATime keyword
		synchronized (oneAtATimeSignsBlocked) {
			if (oneAtATimeSignsBlocked.contains(new Point3D(x, y, z))) {
				// Blocked.
				return null;
			}
		}

		// TODO: If signs have been picked and there isn't a whitelist given,
		// set the whitelist to all picked signs
		if (signWhitelist == null && MCDitty.getPickedSigns().size() > 0) {
			signWhitelist = new LinkedList<Point3D>();
			for (TileEntitySign t : MCDitty.getPickedSigns()) {
				signWhitelist.add(new Point3D(t.xCoord, t.yCoord, t.zCoord));
			}
		}

		// Calculate the start point
		Point3D startPoint = new Point3D(x, y, z);

		// Check that this first sign hit is on the whitelist
		if (signWhitelist != null) {
			boolean isOnList = false;
			Point3D tempPoint = new Point3D();
			for (TileEntitySign t : MCDitty.getPickedSigns()) {
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
					writeChatMessage(world, "§2This sign is unpicked.");
					return null;
				}
			}
		}

		// Read in a MusicString and SongProperties to play from signs
		LinkedList<SignLogPoint> signsReadList = new LinkedList<SignLogPoint>();
		SignDitty dittyProperties = new SignDitty();
		StringBuilder musicStringToPlay = readPattern(startPoint, world,
				signsReadList, dittyProperties, 0, signWhitelist);

		// If this ditty was started from something like a proximity sign, turn
		// on the "oneAtATime" keyword's property by default
		dittyProperties.setOneAtATime(oneAtATimeOn);

		// Set the start point in the ditty properties
		dittyProperties.setStartPoint(startPoint);

		// Check for a null result; indicates infinite loop
		// Also, last sign in sign log is the starting position of the infinite
		// loop
		if (musicStringToPlay == null) {
			// Infinite loop found; display errors, and do not play song
			if (!silent && MCDittyConfig.showErrors) {
				writeChatMessage(
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
					// Mute: stop all playing music
					mutePlayingDitties();
				}

				// If applicable, save midi of song
				if (dittyProperties.getMidiSaveFile() != null
						&& MCDittyConfig.midiSavingEnabled) {
					try {
						saveMidiFile(dittyProperties.getMidiSaveFile(), ditty);
						if (!silent && MCDittyConfig.showMidiMessageEnabled) {
							// Show midi message
							writeChatMessage(world, "§dSaved Midi: "
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

				// Register any disco floors with MCDitty
				MCDitty.addDiscoFloors(dittyProperties.getDiscoFloors());

				// Maestro, commence!
				dittyProperties.setMusicString(ditty);
				playDitty(dittyProperties);

				// Emit single particle, if necessary
				if ((MCDittyConfig.emitOnlyOneParticle)
						|| MCDittyConfig.particlesEnabled) {
					MCDitty.executeTimedDittyEvent(new NoteStartEvent(
							startPoint, 0, 0, 0, null, dittyProperties
									.getDittyID()));
				}
			}

			if (!silent && MCDittyConfig.showErrors) {
				// Show chat messages: first handle the buffer, cutting it down
				// to
				// one if that option is enabled
				LinkedList<String> chatMessageBuffer = dittyProperties
						.getErrorMessages();

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
					writeChatMessage(world, s);
				}

				if (chatMessageBuffer.size() > 0) {
					// Emit error particles
					if (MCDittyConfig.particlesEnabled) {
						for (int i = 0; i < 3; i++) {
							MCDitty.requestParticle(new ParticleRequest(
									startPoint, "smoke"));
						}
					}
				}
			}

			// Add lines to blink
			if (MCDittyConfig.showErrorsOnSigns) {
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
				mutePlayingDitties();
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
		MCDitty.slowDownMC(2);

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
		TileEntitySign currSignTileEntity = null;

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
			if (currBlockTileEntity instanceof TileEntitySign) {
				currSignTileEntity = (TileEntitySign) currBlockTileEntity;
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
			addMusicStringTokens(readMusicString, ditty, SIGN_START_TOKEN
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
								// Remove the current sign from the sign log to
								// prevent duplicates: readPattern will re-add
								// it
								signLog.removeLast();
								// Read pattern
								LinkedList<Point3D> subPatternSignLog = (LinkedList<Point3D>) signLog
										.clone();
								StringBuilder subPatternMusicString = readPattern(
										currSignPoint, world, signLog, ditty,
										subpatternLevel + 1, signWhitelist);

								// If a subpattern fails due to an infinte loop,
								// pass the failure on
								if (subPatternMusicString == null) {
									// simpleLog("PATTERN: null failure on pattern");
									return null;
								}

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
									// Do not check for errors
									// TODO: Use addMusicStringTokens here
									readMusicString.append(" ");
									readMusicString
											.append(subPatternMusicString);
								}

								// Ignore the contents of this sign; it has been
								// read by readPattern already.
								break;
							}
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
						Comment match = gotoKeyword.getNearestMatchingComment(
								currSignPoint, world);
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
							File minecraftDir = Minecraft.getMinecraftDir();
							File midiSaveFile = new File(minecraftDir.getPath()
									+ "/MCDitty/midi/" + givenFilename + ".mid");
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
								addMusicStringTokens(readMusicString, ditty,
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
						if (!addMusicStringTokens(readMusicString, ditty,
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
							if (!addMusicStringTokens(readMusicString, ditty,
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
						// Do not check for errors! The token is a MCDitty-only
						// token.
						addMusicStringTokens(readMusicString, ditty,
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
						String lyricText = "";
						int startLine = line + 1;
						for (int lyricTextLine = startLine; lyricTextLine < LINES_ON_A_SIGN; lyricTextLine++) {
							if (signText[lyricTextLine].trim().endsWith("-")) {
								// Handle split words
								lyricText += signText[lyricTextLine].substring(
										0, signText[lyricTextLine]
												.lastIndexOf("-"));
							} else if (signText[lyricTextLine].trim().length() > 0) {
								String lyricLineFromSign = signText[lyricTextLine];

								// Trim whitespace off of JUST THE END of a line
								// Also remove lines that consist wholly of
								// whitespace
								while (lyricLineFromSign
										.charAt(lyricLineFromSign.length() - 1) == ' ') {
									lyricLineFromSign = lyricLineFromSign
											.substring(
													0,
													lyricLineFromSign.length() - 1);
								}

								lyricText += lyricLineFromSign + " ";
							}
						}
						// Add color code
						lyricText = l.getColorCode().replace('&', '§')
								+ lyricText;

						// Replace inline color codes
						for (String s : colorCodeChars) {
							lyricText = lyricText.replace("&" + s, "§" + s);
						}

						// Adding to existing lyric?
						if (MCDittyConfig.lyricsEnabled) {
							CueScheduler lyrics = ditty.getLyricsStorage();
							lyrics.addLyricText(l.getLabel(), lyricText,
									l.getRepetition());
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
						addMusicStringTokens(readMusicString, ditty,
								SYNC_VOICES_TOKEN, false);
					} else if (keyword.equals("syncwith")) {
						// Read arguments
						SyncWithKeyword k = SyncWithKeyword.parse(currLine);

						// Finally, add token
						if (k.getLayer() != -1000) {
							addMusicStringTokens(readMusicString, ditty,
									SYNC_WITH_TOKEN + "V" + k.getVoice() + "L"
											+ k.getLayer(), false);
						} else {
							addMusicStringTokens(
									readMusicString,
									ditty,
									SYNC_WITH_TOKEN + "V" + k.getVoice() + "Lu",
									false);
						}
					} else if (keyword.equals("sfx")) {
						// Add fx event and token to musicstring

						// Get argument
						SFXKeyword k = SFXKeyword.parse(currLine);

						// Add event
						int eventID = ditty.addDittyEvent(new SFXMCDittyEvent(k
								.getEffectName(), -1, ditty.getDittyID()));
						// Add token
						addMusicStringTokens(readMusicString, ditty,
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
							// MCDittyTexturePack());
						}
					} else if (keyword.equals("volume")) {
						// Inserts a volume token into the song
						VolumeKeyword k = VolumeKeyword.parse(currLine);
						addMusicStringTokens(readMusicString, ditty,
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
							addMusicStringTokens(readMusicString, ditty,
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
							addMusicStringTokens(readMusicString, ditty,
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
						addMusicStringTokens(readMusicString, ditty,
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
							addMusicStringTokens(readMusicString, ditty,
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

						addMusicStringTokens(readMusicString, ditty,
								staccatoToken, false);
					} else if (keyword.equals("staccatooff")) {
						addMusicStringTokens(
								readMusicString,
								ditty,
								createNoteEffectToken(true,
										NOTE_EFFECT_STACCATO), false);
					} else if (keyword.equals("tran")) {
						// Add a transpose note effect token
						TransposeKeyword k = (TransposeKeyword) SignParser.parseKeyword(currLine);
						String token = createNoteEffectToken(false,
								NOTE_EFFECT_TRANSPOSE, k.getTones(),
								k.getDuration());
						addMusicStringTokens(readMusicString, ditty, token,
								false);
					} else if (keyword.equals("tranoff")) {
						addMusicStringTokens(
								readMusicString,
								ditty,
								createNoteEffectToken(true,
										NOTE_EFFECT_TRANSPOSE), false);
					} else {
						// Unrecognized keyword; announce with error
						ditty.addErrorMessage("§b"
								+ keyword
								+ "§c was recognized as a keyword, but no action was given for it in readPattern. This is a bug in MCDitty.");
						ditty.addErrorHighlight(currSignPoint, line);
					}
				} else {
					// Line contians music
					boolean noErrors = addMusicStringTokens(readMusicString,
							ditty, currLine, true);
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
			addMusicStringTokens(readMusicString, ditty, SIGN_END_TOKEN
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

		MCDitty.stopMCSlowdown();

		return readMusicString;
	}

	private static String getMinecraftAdjustedVolumeToken(int volumePercent) {
		int sixteenBitVolume = 16383; // 16383 is the highest volume value

		// Factor in the Minecraft volume, if possible (can be null if this code
		// is called as Minecraft loads)
		if (GetMinecraft.instance() != null) {
			if (GetMinecraft.instance().gameSettings != null) {
				float mcVolume;

				// Get the appropriate MC volume.
				if (MCDittyConfig.getVolumeMode() == MCDittyConfig.USE_MUSIC_VOLUME) {
					mcVolume = GetMinecraft.instance().gameSettings.musicVolume;
				} else if (MCDittyConfig.getVolumeMode() == MCDittyConfig.USE_SOUND_VOLUME) {
					mcVolume = GetMinecraft.instance().gameSettings.soundVolume;
				} else {
					// Ignore MC volume
					mcVolume = 1.0f;
				}

				if (mcVolume == 0f) {
					sixteenBitVolume = 0;
				} else if (mcVolume >= 0.75f) {
					// No change; leave at max
				} else {
					sixteenBitVolume = (int) ((float) sixteenBitVolume * (mcVolume + 0.25f));
				}
			}
		}

		// Factor in volume in argument
		if (volumePercent == 100) {
			// Make sure that volume is always highest with no rounding errors
			// No change
		} else if (volumePercent == 0) {
			// Make sure that volume is always 0 with no rounding errors
			// Mute volume
			sixteenBitVolume = 0;
		} else {
			sixteenBitVolume = (int) ((float) sixteenBitVolume * ((float) volumePercent / 100f));
		}

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
	 * Instructs all playing mcditty songs to mute themselves.
	 */
	public static void mutePlayingDitties() {
		MuteDittyThread t = new MuteDittyThread(
				DittyPlayerThread.jFuguePlayerThreads,
				MCDitty.getPlayMidiSequencers());
		t.start();
	}

	/**
	 * For addMusicStringTokens: used to check validity of a MusicString token.
	 * Static so that it only has to be created once.
	 * 
	 * Public so that the DittyXML parser can share.
	 */
	public static MusicStringParser musicStringParser = new MusicStringParser();

	public static final String SYNC_VOICES_TOKEN = "~syncC";
	public static final String SYNC_WITH_TOKEN = "~syncW";
	// public static final String STACCATO_TOKEN = "~Mstac";
	// public static final String STACCATO_OFF_TOKEN = "~MstacOff";
	public static final String NOTE_EFFECT_TOKEN = "~M";
	public static final String NOTE_EFFECT_OFF_TOKEN = "~N";
	public static final String NOTE_EFFECT_STACCATO = "stac";
	public static final String NOTE_EFFECT_TRANSPOSE = "tran";
	public static final String NOTE_EFFECT_OCTAVES = "octa";
	public static final String TIMED_EVENT_TOKEN = "~E";
	public static final String SIGN_START_TOKEN = "~A";
	public static final String SIGN_END_TOKEN = "~B";

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
					+ createNoteEffectToken(true, NOTE_EFFECT_STACCATO);
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
					+ getAdjustedVolumeToken(100, dittyProperties);
		}
		tokens += " T120";
		return tokens;
	}

	// public static final String RESET_TOKEN = "~Reset";

	/**
	 * Adds musicString tokens to a musicString token buffer, checking them for
	 * errors as they are added. Any error messages found are registered with
	 * dittyProperties; error highlights are left to the calling method to add.
	 * 
	 * Also checks for noPlayTokens
	 * 
	 * @param buffer
	 * @param ditty
	 * @param tokens
	 * @param checkForErrors
	 * @return true if added without errors; false if added wtih errors.
	 */
	public static boolean addMusicStringTokens(StringBuilder buffer,
			Ditty ditty, String musicString, boolean checkForErrors) {
		boolean errorFree = true;
		String[] tokens = musicString.split(" ");
		for (String token : tokens) {
			// Only bother adding non-blank tokens
			if (token.trim().length() > 0) {
				// Check against NoPlayTokens
				if (ditty instanceof SignDitty) {
					for (String noPlayToken : MCDittyConfig.getNoPlayTokens()) {
						// Check for equality, stripping color codes
						if (noPlayToken.equalsIgnoreCase(token.replaceAll("§.",
								""))) {
							// Is a no play token!
							((SignDitty) ditty).setContainsNoPlayTokens(true);
							break;
						}
					}
				}

				// Check tokens for errors
				if (checkForErrors) {
					try {
						musicStringParser.parseTokenStrict(token);
					} catch (JFugueException e) {
						// Token is not a valid token
						ditty.addErrorMessage("§b" + token + "§c: "
								+ e.getMessage());
						ditty.incrementBadTokens();
						errorFree = false;
						simpleLog("addMusicStringTokens: Bad token found ("
								+ token + "):");
						if (MCDittyConfig.debug) {
							e.printStackTrace();
						}
					} catch (Exception e) {
						// Token is a really screwed up token!
						ditty.addErrorMessage("§cMCDitty cannot figure out this token: §b"
								+ token);
						ditty.incrementBadTokens();
						errorFree = false;
						simpleLog("addMusicStringTokens: Really bad token found ("
								+ token + "):");
						if (MCDittyConfig.debug) {
							e.printStackTrace();
						}
					}
				}

				ditty.incrementTotalTokens();
				buffer.append(" " + token);
			}
		}
		return errorFree;
	}

	private static void highlightSignErrorLine(World world, SignLine signLine) {
		TileEntity t = world.getBlockTileEntity(signLine.x, signLine.y,
				signLine.z);
		if (t instanceof TileEntitySign) {
			TileEntitySign t2 = (TileEntitySign) t;
			t2.startBlinking = true;
			t2.errorBlinkLine[signLine.getLine()] = true;
			simpleLog("Starting sign error blinking");
		}
	}

	private static void highlightSignLine(World world,
			SignLineHighlight signLine) {
		if (MCDittyConfig.highlightEnabled) {
			TileEntity t = world.getBlockTileEntity(signLine.x, signLine.y,
					signLine.z);
			if (t instanceof TileEntitySign) {
				TileEntitySign t2 = (TileEntitySign) t;
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
						if (BlockSign.isSign(currPoint.x, currPoint.y - 1,
								currPoint.z, world)
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

	/**
	 * If argument world is null, queues up message in Mcditty.class
	 * 
	 * @param world
	 * @param message
	 */
	public static void writeChatMessage(World world, String message) {
		if (world != null) {
			List entities = world.playerEntities;
			for (Object p : entities) {
				if (p instanceof EntityPlayerSP) {
					((EntityPlayerSP) p).addChatMessage(message);
					break;
				}
			}
		} else {
			MCDitty.addLyricToQueue(new CueEvent(message));
		}
	}

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

	public static void playMusicString(String tune) {
		// Convenience method for playDitty
		Ditty d = new Ditty();
		d.setMusicString(tune);
		playDitty(d);
	}

	public static void playDitty(Ditty prop) {
		DittyPlayerThread playerThread = new DittyPlayerThread(prop);
		if (MCDittyConfig.debug) {
			simpleLog("Playing MusicString: " + prop.getMusicString());
		}
		playerThread.setPriority(Thread.MAX_PRIORITY);
		playerThread.start();
	}

	// Mutex for saving midis
	private static Object saveMidiPlayerMutex = new Object();

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

	private static String currentVersionBuffer = null;

	/**
	 * Attempts to retrieve the current version of MCDitty from a file on the
	 * internet.
	 * 
	 * Note: If it successfully downloads the current version, it will remember
	 * it and not actually check the internet on future calls.
	 * 
	 * @return If successful, the version from the file. If not, it will return
	 *         a string that starts with "§c"
	 */
	public static String downloadCurrentVersion(String mcVersion) {
		if (currentVersionBuffer != null) {
			return currentVersionBuffer;
		} else {
			simpleLog("downloadCurrentVersion called");

			// Create a url pointing at the current version file on dropbox
			URL currentVersionURL = null;
			try {
				// currentVersionURL = new
				// URL("http://mcditty.wikispaces.com/file/view/MCDitty_Current_Version.txt");
				String url;
				url = "http://dl.dropbox.com/s/lwl4uvift9e1tvp/MCDitty_Current_Version.txt";
				currentVersionURL = new URL(url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				// If the person (fenceFoil, hopefully) editing this has half a
				// wit
				// about his person, this will never happen.
				return "§cVersion File URL Malformed";
			}

			// Open connection to file, and download it.
			String currVersionLegacy = null;
			ArrayList<String> currentVersionLines = new ArrayList<String>();
			try {
				// Open connection
				BufferedReader currVersionIn = new BufferedReader(
						new InputStreamReader(currentVersionURL.openStream()));
				// Read the first line of the file: this is a straight version
				// number.
				// This version number is outdated, as it does not take the
				// current
				// MC version into account.
				currVersionLegacy = currVersionIn.readLine();
				// Read subsequent lines until the words "END Versions"
				while (true) {
					String lineIn = currVersionIn.readLine();
					if (lineIn == null) {
						break;
					} else if (lineIn.equalsIgnoreCase("end versions")) {
						// End of versions keys
						// Stop reading
						break;
					} else {
						currentVersionLines.add(lineIn);
					}
				}
				currVersionIn.close();
			} catch (IOException e) {
				e.printStackTrace();
				return "§cCouldn't Download Version File";
			}

			// Find the current version of MCDitty for the current version of
			// Minecraft

			// Process the downloaded strings into a map
			HashMap<String, String> versionKeys = new HashMap<String, String>();
			for (String s : currentVersionLines) {
				String[] parts = s.split(":");
				if (parts.length != 2) {
					// Inavlid key: too many parts
					return "§cVersion File Has Illegible Key: " + s;
				}

				// Check each version number for proper syntax
				for (String part : parts) {
					// Proxper syntax: One to four parts, separated by dots:
					// each
					// part can
					// be numbers or a *
					if (!CompareVersion.isVersionNumber(part)) {
						return "§cInvalid Number in Version File: " + s;
					}
				}

				// Finally, add key
				versionKeys.put(parts[0], parts[1]);
			}

			// Get the version for this version of Minecraft
			String foundVersion = versionKeys.get(mcVersion);

			if (foundVersion == null) {
				// No version of MCDitty given in file for this version of MC
				return "§cNo Version for MC " + mcVersion;
			}

			simpleLog("downloadCurrentVersion successfully returned "
					+ foundVersion);
			currentVersionBuffer = foundVersion;
			return foundVersion;
		}
	}

	/**
	 * Opens this mod's MCForums thread in a browser
	 * 
	 * @return null if successful, or a message if there's an error
	 */
	public static String openMCForumsThread() {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop()
						.browse(new URI(
								"http://www.minecraftforum.net/topic/1146661-wip-086-125-mcditty-make-music-with-vanilla-signs/"));
			} catch (IOException e) {
				e.printStackTrace();
				return "Can't open browser for some reason";
			} catch (URISyntaxException e) {
				e.printStackTrace();
				return "Bug in MCDitty: MCForums Thread URL is Invalid";
			}
		} else {
			return "Can't open browser for some reason";
		}
		return null;
	}

	/**
	 * Opens this mod's changelog in a text editor.
	 * 
	 * @return null if successful, or a message if there's an error
	 */
	public static String downloadAndShowChangelog(GuiScreen backScreen) {
		URL changeLogURL = null;
		try {
			// changeLogURL = new
			// URL("http://mcditty.wikispaces.com/file/view/MCDitty_Changelog.txt");
			changeLogURL = new URL(
					"http://dl.dropbox.com/s/td2etwtujs5635n/MCDitty_Changelog.txt");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			// If the person (fenceFoil, hopefully) editing this has half a wit
			// about his person, this will never happen.
			return "Malformed URL for the Changelog Address";
		}

		File changeLogTempFile;
		try {
			changeLogTempFile = File.createTempFile("MCDittyChangelogTemp",
					".txt");
			changeLogTempFile.deleteOnExit();
			BufferedReader changeLogIn = new BufferedReader(
					new InputStreamReader(changeLogURL.openStream()));
			BufferedWriter changeLogOut = new BufferedWriter(new FileWriter(
					changeLogTempFile));
			while (true) {
				String lineIn = changeLogIn.readLine();
				if (lineIn == null) {
					break;
				} else {
					changeLogOut.write(lineIn);
					changeLogOut.newLine();
				}
			}
			changeLogOut.close();
			changeLogIn.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "Changelog File No Longer Exists :(";
		} catch (IOException e) {
			e.printStackTrace();
			return "Could Not Download Changelog";
		}

		// if (Desktop.isDesktopSupported()) {
		// try {
		// Desktop.getDesktop().open(changeLogTempFile);
		// } catch (IOException e) {
		// e.printStackTrace();
		// return "Can't open text editor for some reason";
		// }
		// } else {
		// return "Can't open text editor for some reason";
		// }

		GuiMCDittyChangelog changelogGui = new GuiMCDittyChangelog(backScreen,
				changeLogTempFile, "MCDitty Changelog");
		GetMinecraft.instance().displayGuiScreen(changelogGui);
		return null;
	}

	public static ArrayList<String[]> importSignsFromFile(File f)
			throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		// Check first line to see if this file is valid
		String firstLine = in.readLine();
		if (firstLine != null) {
			if (!firstLine.trim().equalsIgnoreCase("MCDitty Exported Signs")) {
				return null;
			}
		} else {
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
				} else if (lineIn
						.equalsIgnoreCase("End of MCDitty Exported Signs")) {
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
		out.write("MCDitty Exported Signs");
		out.newLine();
		for (String[] sign : signs) {
			out.write("###############X");
			out.newLine();
			for (String line : sign) {
				out.write(line);
				out.newLine();
			}
		}
		out.write("End of MCDitty Exported Signs");
		out.close();
	}

	public static void simpleLog(String logString) {
		if (MCDittyConfig.debug) {
			System.out.println(logString);
		}
	}

	/**
	 * Signals that MCDitty has run autoupdate already, and should not be run
	 * again
	 */
	public static boolean autoUpdating = false;

	public static String autoUpdate() {
		if (autoUpdating == true) {
			return "§bCan't auto update twice! Restart Minecraft before trying again.";
		}
		autoUpdating = true;
		simpleLog("downloadCurrentVersion called");

		showTextAsLyricNow("§aAuto-Updating MCDitty to the latest version...");
		showTextAsLyricNow("Checking for a new version...");

		String newVersion = downloadCurrentVersion(MCDittyConfig.MC_CURRENT_VERSION);
		if (CompareVersion.isVersionNumber(newVersion)) {
			showTextAsLyricNow("§aUpdating to version " + newVersion
					+ " for Minecraft " + MCDittyConfig.MC_CURRENT_VERSION);
		} else {
			showTextAsLyricNow("Error getting new version info: " + newVersion);
		}

		showTextAsLyricNow("§aDownloading new version...");

		// Create a url pointing at the current version download file on dropbox
		URL currentDownloadVersionsURL = null;
		try {
			// currentVersionURL = new
			// URL("http://mcditty.wikispaces.com/file/view/MCDitty_Current_Version.txt");
			String url;
			url = "http://dl.dropbox.com/s/2vi0z7om4kotsx1/MCDitty_Download_Latest.txt";
			currentDownloadVersionsURL = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			// If the person (fenceFoil, hopefully) editing this has half a wit
			// about his person, this will never happen.
			return "§cVersion Download File URL Malformed";
		}

		// Open connection to download versions file, and download it.
		ArrayList<String> currentVersionLines = new ArrayList<String>();
		try {
			// Open connection
			BufferedReader currVersionIn = new BufferedReader(
					new InputStreamReader(
							currentDownloadVersionsURL.openStream()));
			// Read lines until the words "END Versions"
			while (true) {
				String lineIn = currVersionIn.readLine();
				if (lineIn == null) {
					break;
				} else if (lineIn.equalsIgnoreCase("end versions")) {
					// End of versions keys
					// Stop reading
					break;
				} else {
					currentVersionLines.add(lineIn);
				}
			}
			currVersionIn.close();
		} catch (IOException e) {
			e.printStackTrace();
			return "§cCouldn't Download 'New Version Download URLs' File";
		}

		// Find the latest version download URL for the current version of
		// Minecraft

		// Process the downloaded urls into a map
		HashMap<String, String> versionDownloadURLKeys = new HashMap<String, String>();
		for (String s : currentVersionLines) {
			String[] parts = s.split(":");
			if (parts.length <= 1) {
				// Inavlid key: too few parts
				return "§cVersion Download URL Table Has Illegible Key: " + s;
			}

			// Check each MC version number for proper syntax
			if (!CompareVersion.isVersionNumber(parts[0])) {
				return "§cInvalid Version Number in Version Download URL Table: "
						+ s;
			}

			// Check url validity (from the end of the version number and first
			// colon)
			String url = s.substring((parts[0].length() - 1) + 2);
			// Check that url is valid
			if (!url.matches("(http|https)://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?")) {
				return "§cInvalid Download URL: " + url;
			}

			// // URL Must be on dropbox
			// if (!url.toLowerCase().startsWith("http://dl.dropbox.com/")) {
			// return "§cDownload URL is not on DropBox";
			// }

			// Finally, add key
			versionDownloadURLKeys.put(parts[0], url);

			simpleLog("Version Download URL Key Read: " + parts[0] + " : "
					+ url);
		}

		// Get the download url for this version of Minecraft
		String foundVersionURL = versionDownloadURLKeys
				.get(MCDittyConfig.MC_CURRENT_VERSION);

		if (foundVersionURL == null) {
			// No version of MCDitty given in file for this version of MC
			return "§cNo Download URL for MC "
					+ MCDittyConfig.MC_CURRENT_VERSION;
		}

		// Download new version of MCDitty!
		URL versionDownloadURL;
		try {
			versionDownloadURL = new URL(foundVersionURL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return "§cDownload URL for new version cannot be read: "
					+ foundVersionURL;
		}

		// Create new file to download to
		File downloadDir = new File(GetMinecraft.instance().getMinecraftDir()
				+ File.separator + "MCDitty/Versions/");
		if (!downloadDir.exists()) {
			downloadDir.mkdirs();
		}
		File newVersionFile = new File(downloadDir,
				foundVersionURL.substring(foundVersionURL.lastIndexOf("/") + 1));
		simpleLog("Saving new version as " + newVersionFile.getPath());
		try {
			newVersionFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			return "§cCould not create file: " + newVersionFile.getPath();
		}

		// Download file
		try {
			ReadableByteChannel downloadByteChannel = Channels
					.newChannel(versionDownloadURL.openStream());
			FileOutputStream newVersionZipFileOutputStream = new FileOutputStream(
					newVersionFile);
			// TODO: Show download progress
			newVersionZipFileOutputStream.getChannel().transferFrom(
					downloadByteChannel, 0, 1 << 24);
		} catch (IOException e) {
			e.printStackTrace();
			return "§cCould not download new version.";
		}

		showTextAsLyricNow("§aDownload successful!");

		showTextAsLyricNow("§aReading minecraft.jar...");

		File minecraftFile = new File(GetMinecraft.instance().getMinecraftDir()
				+ File.separator + "bin/minecraft.jar");
		JarFile minecraftJarFile;
		LinkedList<JarEntry> minecraftJarEntries = new LinkedList<JarEntry>();
		try {
			// Set up to read minecraft.jar
			minecraftJarFile = new JarFile(minecraftFile);
			JarInputStream minecraftJarInputStream = new JarInputStream(
					new FileInputStream(minecraftFile));
			// Read in a list of the entries in minecraft.jar
			while (true) {
				JarEntry entry = minecraftJarInputStream.getNextJarEntry();
				if (entry == null) {
					break;
				}
				simpleLog("Minecraft Jar: Found entry: " + entry.getName());
				minecraftJarEntries.add(entry);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return "§cCould not read minecraft.jar. You can still install the update yourself: it is in "
					+ newVersionFile.getPath();
		}

		showTextAsLyricNow("§aReading new MCDitty zip...");

		ZipFile newVersionZipFile;
		LinkedList<ZipEntry> newVersionZipEntries = new LinkedList<ZipEntry>();
		try {
			// Set up to read new version zip
			newVersionZipFile = new ZipFile(newVersionFile);
			ZipInputStream newVersionZipInputStream = new ZipInputStream(
					new FileInputStream(newVersionFile));
			// Read in a list of entries in the new version's zip file
			while (true) {
				ZipEntry entry = newVersionZipInputStream.getNextEntry();
				if (entry == null) {
					break;
				}
				newVersionZipEntries.add(entry);
				simpleLog("NewVersion Jar: Found entry: " + entry.getName());
			}
		} catch (ZipException e1) {
			e1.printStackTrace();
			return "§cCould not read new version's zip file (ZipException). You can still install the update yourself: it is in "
					+ newVersionFile.getPath();
		} catch (IOException e1) {
			e1.printStackTrace();
			return "§cCould not read new version's zip file. You can still install the update yourself: it is in "
					+ newVersionFile.getPath();
		}

		// Decide what files to put in updated minecraft.jar
		LinkedList<ZipEntry> updatedMinecraftEntries = new LinkedList<ZipEntry>();

		// Start with every file in the old minecraft jar
		updatedMinecraftEntries.addAll(minecraftJarEntries);

		// Remove all org/jfugue files
		// Remove all com/wikispaces/mcditty files

		// (Leave all other files; if some are MCDitty they will be copied over
		// hopefully)
		// TODO: Make this seperation of MCDitty and MC files better (with
		// MC1.3, change?)
		// The way this mod is currently arranged in minecraft.jar, this is the
		// best we can do towards clearing it entirely.
		for (int i = 0; i < updatedMinecraftEntries.size(); i++) {
			JarEntry e = (JarEntry) updatedMinecraftEntries.get(i);
			if (e.getName().startsWith("org/jfugue/")) {
				simpleLog("Removing entry: " + e.getName());
				updatedMinecraftEntries.remove(i);
				i--;
			} else if (e.getName().startsWith("com/wikispaces/mcditty/")) {
				simpleLog("Removing Entry: " + e.getName());
				updatedMinecraftEntries.remove(i);
				i--;
			} else if (e.getName().startsWith("com/weebly/mcditty/")) {
				simpleLog("Removing Entry: " + e.getName());
				updatedMinecraftEntries.remove(i);
				i--;
			} else if (e.getName().startsWith("de/jarnbjo/")) {
				simpleLog("Removing Entry: " + e.getName());
				updatedMinecraftEntries.remove(i);
				i--;
			}
		}

		// Add new files from zip, replacing any entries from minecraft.jar
		for (int i = 0; i < newVersionZipEntries.size(); i++) {
			ZipEntry z = newVersionZipEntries.get(i);

			if (z.getName().toLowerCase().contains("mcdittysrc.zip")
					|| z.getName().toLowerCase().contains("readme.txt")
					|| z.getName().toLowerCase().contains("lgpl.txt")
					|| z.getName().toLowerCase().contains("license.txt")) {
				simpleLog("NOT ADDING FILE FROM NEW ZIP: " + z.getName());
				continue;
			}

			// Remove any files with the same name as this from the updated
			// minecraft files
			for (int f = 0; f < updatedMinecraftEntries.size(); f++) {
				if (updatedMinecraftEntries.get(f).getName()
						.equals(z.getName())) {
					simpleLog("Overwriting Entry: " + z.getName());
					updatedMinecraftEntries.remove(f);
					break;
				}
			}

			// Add zip file to updated minecraft's files
			simpleLog("Adding entry from zip: " + z.getName());
			updatedMinecraftEntries.add(z);
		}

		// Note: this code relies on the files from the old minecraft.jar being
		// "jarEntry"s, and files form the zip being "ZipEntry"s to
		// differentiate the two sources of files.

		// Write the files into a new minecraft jar file
		showTextAsLyricNow("§aWriting updated minecraft.jar... 0 Percent");

		try {
			File newMinecraftFile = new File(GetMinecraft.instance()
					.getMinecraftDir()
					+ File.separator
					+ "bin"
					+ File.separator + "minecraft.updatedMCDitty.jar");
			if (newMinecraftFile.exists()) {
				newMinecraftFile.delete();
			}
			newMinecraftFile.createNewFile();
			// JarFile newMinecraftJar = new JarFile(newMinecraftFile);
			JarOutputStream newMinecraftJarOut = new JarOutputStream(
					new FileOutputStream(newMinecraftFile));
			for (int i = 0; i < updatedMinecraftEntries.size(); i++) {
				ZipEntry e = updatedMinecraftEntries.get(i);

				newMinecraftJarOut.putNextEntry(new JarEntry(e.getName()));
				InputStream entryDataIn;
				if (e instanceof JarEntry) {
					// Read from minecraft.jar
					entryDataIn = minecraftJarFile.getInputStream(e);
				} else {
					// Instance of zipentry
					// Read from new version's zip
					entryDataIn = newVersionZipFile.getInputStream(e);
				}
				byte[] buffer = new byte[4096];
				int bytesRead = 0;
				while ((bytesRead = entryDataIn.read(buffer)) != -1) {
					newMinecraftJarOut.write(buffer, 0, bytesRead);
				}
				entryDataIn.close();
				newMinecraftJarOut.flush();
				newMinecraftJarOut.closeEntry();

				simpleLog("Wrote to new jar from "
						+ ((e instanceof JarEntry) ? "JAR" : "ZIP") + ": "
						+ e.getName());

				if (i % 200 == 0) {
					showTextAsLyricNow("§aWriting "
							+ (int) ((double) i
									/ (double) updatedMinecraftEntries.size() * 100d)
							+ " Percent...");
				}
			}
			newMinecraftJarOut.flush();
			newMinecraftJarOut.finish();
			newMinecraftJarOut.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return "§cCould not create/find an updated minecraft.jar. You can still install the update yourself: it is in "
					+ newVersionFile.getPath();
		} catch (IOException e1) {
			e1.printStackTrace();
			return "§cCould not write an updated minecraft.jar. You can still install the update yourself: it is in "
					+ newVersionFile.getPath();
		}

		showTextAsLyricNow("§a100 Percent: Wrote Updated Minecraft.jar!");

		// Extract renaming script to file
		showTextAsLyricNow("§aExtracting Swapper...");
		File jarSwapperFile = new File(GetMinecraft.instance()
				.getMinecraftDir()
				+ File.separator
				+ "bin/MCDittyJarSwapper.jar");
		try {
			ReadableByteChannel jarSwapperChannel = Channels
					.newChannel(MCDittyResourceManager
							.getResource("autoUpdate/swapperJar/AutoUpdateJarSwapper.jar"));
			FileOutputStream jarSwapperFileOutputStream = new FileOutputStream(
					jarSwapperFile);
			// TODO: Show extract progress
			jarSwapperFileOutputStream.getChannel().transferFrom(
					jarSwapperChannel, 0, 1 << 24);
		} catch (IOException e) {
			e.printStackTrace();
			return "§cCould not extract jar swapper. §bYou can still copy the jar file yourself: rename minecraft.updatedMCDitty.jar to minecraft.jar in the .minecraft/bin folder.";
		} catch (NullPointerException e) {
			e.printStackTrace();
			return "§cCould not extract jar swapper. §bYou can still copy the jar file yourself: rename minecraft.updatedMCDitty.jar to minecraft.jar in the .minecraft/bin folder.";
		}

		showTextAsLyricNow("§aSwapper Extracted!");
		try {
			initJarSwapperToRun(jarSwapperFile);
		} catch (IOException e) {
			e.printStackTrace();
			return "§cCould not start jar swapper. §bYou can still copy the jar file yourself: rename minecraft.updatedMCDitty.jar to minecraft.jar in the .minecraft/bin folder.";
		}
		

		showTextAsLyricNow("§bBackup of old minecraft.jar saved.");
		return "§bUpdate successful! Next time you start Minecraft, MCDitty will be updated to version "
				+ downloadCurrentVersion(MCDittyConfig.MC_CURRENT_VERSION);
	}

	/**
	 * Restart the current Java application Adapted From
	 * http://java.dzone.com/articles/programmatically-restart-java
	 * 
	 * @param runBeforeRestart
	 *            some custom code to be run before restarting
	 * @throws IOException
	 */
	private static void initJarSwapperToRun(File jarToRun) throws IOException {
		try {
			// java binary
			String java = System.getProperty("java.home") + "/bin/java";
			// init the command to execute, add the vm args
			final StringBuffer cmd = new StringBuffer("\"" + java + "\" ");

			// program main and program arguments
			String mainCommand = jarToRun.getPath();
			// program main is a jar
			if (mainCommand.endsWith(".jar")) {
				// if it's a jar, add -jar mainJar
				cmd.append("-jar " + "\"" + new File(mainCommand).getPath()
						+ "\"");
			} else {
				// else it's a .class, add the classpath and mainClass
				cmd.append("-cp \"" + System.getProperty("java.class.path")
						+ "\" " + mainCommand);
			}

			// Add argument containing the location of this minecraft.jar
			cmd.append(" " + Minecraft.getMinecraftDir() + File.separator
					+ "bin" + File.separator);

			// execute the command in a shutdown hook, to be sure that all the
			// resources have been disposed before running the new file
			Runtime.getRuntime().addShutdownHook(
					new RunCommandThread(cmd.toString()));

			System.out.println("Going to run on shutdown: " + cmd.toString());
		} catch (Exception e) {
			// something went wrong
			throw new IOException(
					"Error while trying to restart the application", e);
		}
	}

	/**
	 * This method is a bit on the blunt side. Should be removed or
	 * reconsidered.
	 */
	public static void showTextAsLyricNow(String text) {
		CueEvent lyric = new CueEvent(text);
		MCDitty.addLyricToQueue(lyric);
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

	public static MCDitty mcDittyMod = null;

	public static void initMCDittyMod() {
		if (mcDittyMod == null) {
			// TODO: Replace skull renderer
			try {
				Map map = (Map) GetMinecraft.getUniqueTypedFieldFromClass(
						TileEntityRenderer.class, Map.class,
						TileEntityRenderer.instance);
				map.put(TileEntitySkull.class, new TileEntitySkullRenderer2());
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Start MCDitty!
			mcDittyMod = new MCDitty();
			mcDittyMod.load();
		}
	}

	/**
	 * only called by clickMiddleMouseButton , and passed to
	 * inventory.setCurrentItem (along with isCreative)
	 */
	public int idPicked(World par1World, int par2, int par3, int par4) {
		return Item.sign.shiftedIndex;
	}
}
