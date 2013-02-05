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
package com.wikispaces.mcditty;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

import net.minecraft.client.Minecraft;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.BlockSign;
import net.minecraft.src.DestroyBlockProgress;
import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.EntityFX;
import net.minecraft.src.EntityFireworkRocket;
import net.minecraft.src.EntityHeartFX;
import net.minecraft.src.GuiOptions;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiScreenBook;
import net.minecraft.src.GuiVideoSettings;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Packet;
import net.minecraft.src.RenderGlobal;
import net.minecraft.src.RenderManager;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityNote;
import net.minecraft.src.TileEntitySign;
import net.minecraft.src.TileEntitySignRenderer;
import net.minecraft.src.Vec3;
import net.minecraft.src.World;
import net.minecraft.src.WorldClient;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.xml.sax.SAXException;

import com.wikispaces.mcditty.blockTune.BlockTuneManager;
import com.wikispaces.mcditty.bot.Bot;
import com.wikispaces.mcditty.bot.VillagerBot;
import com.wikispaces.mcditty.bot.action.BotAction;
import com.wikispaces.mcditty.config.MCDittyConfig;
import com.wikispaces.mcditty.disco.DiscoFloor;
import com.wikispaces.mcditty.disco.DiscoFloorDoneListener;
import com.wikispaces.mcditty.disco.MeasureDiscoFloorThread;
import com.wikispaces.mcditty.ditty.DittyPlayerThread;
import com.wikispaces.mcditty.ditty.MIDISynthPool;
import com.wikispaces.mcditty.ditty.event.CreateBotEvent;
import com.wikispaces.mcditty.ditty.event.CreateEmitterEvent;
import com.wikispaces.mcditty.ditty.event.CueEvent;
import com.wikispaces.mcditty.ditty.event.DittyEndedEvent;
import com.wikispaces.mcditty.ditty.event.FireworkEvent;
import com.wikispaces.mcditty.ditty.event.HighlightSignPlayingEvent;
import com.wikispaces.mcditty.ditty.event.NoteStartEvent;
import com.wikispaces.mcditty.ditty.event.PlayMidiDittyEvent;
import com.wikispaces.mcditty.ditty.event.SFXMCDittyEvent;
import com.wikispaces.mcditty.ditty.event.TempoDittyEvent;
import com.wikispaces.mcditty.ditty.event.TimedDittyEvent;
import com.wikispaces.mcditty.ditty.event.VolumeEvent;
import com.wikispaces.mcditty.gui.GuiMCDittyBookExportButton;
import com.wikispaces.mcditty.gui.GuiMCDittyBookImportButton;
import com.wikispaces.mcditty.gui.GuiMCDittyMenuButton;
import com.wikispaces.mcditty.gui.GuiMCDittyVideoMenuButton;
import com.wikispaces.mcditty.keyboard.KeypressProcessor;
import com.wikispaces.mcditty.noteblocks.BlockNoteMCDitty;
import com.wikispaces.mcditty.noteblocks.EntityNoteBlockTooltip;
import com.wikispaces.mcditty.noteblocks.RenderNoteBlockTooltip;
import com.wikispaces.mcditty.noteblocks.TileEntityNoteMCDitty;
import com.wikispaces.mcditty.particle.BubbleParticleRequest;
import com.wikispaces.mcditty.particle.HeartParticleRequest;
import com.wikispaces.mcditty.particle.NoteParticleRequest;
import com.wikispaces.mcditty.particle.ParticleRequest;
import com.wikispaces.mcditty.resources.UpdateResourcesThread;
import com.wikispaces.mcditty.sfx.SFXManager;
import com.wikispaces.mcditty.signs.Comment;
import com.wikispaces.mcditty.signs.SignLine;
import com.wikispaces.mcditty.signs.SignParser;
import com.wikispaces.mcditty.signs.keywords.ParsedKeyword;
import com.wikispaces.mcditty.signs.keywords.ProxPadKeyword;

/**
 * The central class of the MCDitty mod. Houses code to play ditties, do things
 * as they play during the game loop, handle keyboard and mouse input, and hook
 * into the Minecraft game loop.
 * 
 * TODO: Move many ditty-playing related methods from BlockSign to MCDitty
 */
public class MCDitty implements TickListener {

	/**
	 * All unique signs in the world that have ever been created during this
	 * play-session. Used by the "recall" button in the sign editor: a filtered
	 * version of signindex.
	 */
	public static LinkedList<TileEntitySign> signRecoveryList = new LinkedList<TileEntitySign>();

	/**
	 * Cues that need to be executed during the next tick.
	 */
	private static LinkedList<CueEvent> lyricsQueue = new LinkedList<CueEvent>();

	/**
	 * Whether to insert a sleep into the next Minecraft tick to give other
	 * threads some breathing time.
	 */
	private static boolean slowMinecraft;

	/**
	 * How long to sleep on each tick; see slowMinecraft.
	 */
	private static long sleepOnTickTime;

	/**
	 * The current tempo for all ditties ever.
	 */
	private static HashMap<Integer, Float> dittyTempos = new HashMap<Integer, Float>();

	/**
	 * The current time known last check in all dittys ever.
	 */
	private static HashMap<Integer, Long> dittyTimes = new HashMap<Integer, Long>();

	/**
	 * Particles that need to be created during the game loop.
	 */
	private static LinkedList<ParticleRequest> particleRequestQueue = new LinkedList<ParticleRequest>();

	/**
	 * Signs that are currently highlighted because they are being played.
	 */
	private static LinkedList<HighlightSignPlayingEvent> onSigns = new LinkedList<HighlightSignPlayingEvent>();

	/**
	 * All proxpad bounding boxes.
	 */
	private static ArrayList<ProxPadBoundingBox> proxPadBBs = new ArrayList<ProxPadBoundingBox>();

	/**
	 * All signs ever created in the world, including ones that don't exist
	 * anymore.
	 */
	private static LinkedList<TileEntitySign> signIndex = new LinkedList<TileEntitySign>();

	/**
	 * All disco floors
	 */
	protected static LinkedList<DiscoFloor> discoFloors = new LinkedList<DiscoFloor>();

	/**
	 * Any ditties that have ended, by their ID
	 */
	private static LinkedList<Integer> endedDitties = new LinkedList<Integer>();

	/**
	 * All comment lines on signs in the world. Might be filtered (at any given
	 * time) so that only comments that currently exist are still in the list.
	 */
	private static LinkedList<Comment> comments = new LinkedList<Comment>();

	private boolean guiKeyPreviousPressedState = false;

	/**
	 * Whether the first tick of the game has yet to happen.
	 */
	private boolean firstTick = true;

	public static MIDISynthPool synthPool;

	/**
	 * 
	 */
	public static KeypressProcessor keypressHandler = new KeypressProcessor();

	/**
	 * General-purpose random generator, cached.
	 */
	private static Random rand = new Random();

	/**
	 * Currently picked signs (as in picked with a pickaxe).
	 */
	private static LinkedList<TileEntitySign> pickedSigns = new LinkedList<TileEntitySign>();

	/**
	 * The current entity used to get a hook into the main game loop's ticks.
	 */
	private static MCDittyTickHookEntity hookEntity;

	/**
	 * The guiscreen open last tick.
	 */
	private static GuiScreen lastTickGui = null;

	/**
	 * Used to avoid clicking an exit button in a gui activating something once
	 * the gui closes before the mouse button is lifted.
	 */
	public static boolean doNotCheckForClicks = false;

	/**
	 * The current and last lists of all blocks currently being damaged (as by
	 * punching).
	 */
	public static HashMap damageValues, lastDamageValues;

	/**
	 * Sequencers used by the PlayMidi keyword to play midi files
	 */
	private static LinkedList<Sequencer> playMidiSequencers = new LinkedList<Sequencer>();

	/**
	 * Contains the current track volume for each track in each played ditty
	 */
	private static HashMap<Integer, Float[]> dittyTrackVolumes = new HashMap<Integer, Float[]>();

	/**
	 * All current emitters
	 */
	private static LinkedList<Emitter> emitters = new LinkedList<Emitter>();

	/**
	 * All current bots, by ditty
	 */
	private static HashMap<Integer, LinkedList<Bot>> bots = new HashMap<Integer, LinkedList<Bot>>();

	/**
	 * Inserted into the ingame pause menu
	 */
	private static final GuiMCDittyMenuButton GUI_MCDITTY_BUTTON = new GuiMCDittyMenuButton();

	private static final GuiMCDittyVideoMenuButton GUI_MCDITTY_VIDEO_BUTTON = new GuiMCDittyVideoMenuButton(
			null);

	private static FireworkExploder fireworkExploder = new FireworkExploder();

	private static BlockTuneManager blockTuneManager = new BlockTuneManager();

	public MCDitty() {

		// Set up modified note block
		// First, the original Minecraft noteblock must be discarded
		try {
			BlockNoteMCDitty.removeNormalNoteBlockFromList();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Then the new noteblock can be created
		BlockNoteMCDitty newNoteBlock = new BlockNoteMCDitty(25);

		// Set up modified level sound packet
		// Put it into Packet's directories of packet types, replacing the
		// normal level sound packet
		// Packet.packetClassToIdMap.put(Packet62LevelSoundMCDitty.class,
		// Integer.valueOf(62));
		try {
			Object packetClassToIdMapObj = Finder
					.getUniqueTypedFieldFromClass(Packet.class, Map.class, null);
			if (packetClassToIdMapObj != null) {
				Map packetClassToIdMap = (Map) packetClassToIdMapObj;
				packetClassToIdMap.put(Packet62LevelSoundMCDitty.class,
						Integer.valueOf(62));

				// Also put into the other map of packets
				// Only do this if the first map was found and added to
				// successfully
				Packet.packetIdToClassMap.addKey(62,
						Packet62LevelSoundMCDitty.class);
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Add a TileEntity mapping for TileEntityNoteMCDitty
		try {
			Object[] maps = Finder.getAllUniqueTypedFieldsFromClass(
					TileEntity.class, Map.class, null);
			if (maps != null) {
				for (Object o : maps) {
					if (o instanceof Map) {
						Map m = (Map) o;

						if (m.get("Music") != null) {
							m.put("Music", TileEntityNoteMCDitty.class);
						} else {
							m.put(TileEntityNoteMCDitty.class, "Music");
						}
					}
				}
			}
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	/**
	 * Runs all the most costly parts of loading MCDitty. Called as early in the
	 * game's loading as possible.
	 */
	public void load() {
		// Enable calling onTickInGame each tick
		// TODO: REENABLE WHEN MODLOADER COMES BACK!!! REMOVED 1.3.1
		// ModLoader.setInGameHook(this, true, true);

		// Todo: Add achievement

		long startTime = System.currentTimeMillis();
		// Init the sound engine (with a Gervill synthesizer)
		// NOTE: JUDGED TOO EXPENSIVE
		// try {
		// Player p = new Player(new SoftSynthesizer());
		// p.play("Rwww");
		// } catch (MidiUnavailableException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// Cache a few synths
		setUpSynthPool();
		long jfugueLoadTime = System.currentTimeMillis() - startTime;

		// Init the SFX table
		startTime = System.currentTimeMillis();
		try {
			SFXManager.load();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		long sfxLoadTime = System.currentTimeMillis() - startTime;

		// Init the Sign Renderer Trig Tables
		startTime = System.currentTimeMillis();
		TileEntitySignRenderer.createTrigTables();
		long trigLoadTime = System.currentTimeMillis() - startTime;

		final MCDitty mcditty = this;

		// Start the entity checker update thread
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					Minecraft m = Minecraft.getMinecraft();
					if (m == null) {
						// System.out.println("mc is null");
					} else {
						mcditty.createHookEntity(mcditty);
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// Also register all entity renderers
					if (m != null && m.theWorld != null) {
						try {
							Map registeredRenderers = (Map) Finder
									.getUniqueTypedFieldFromClass(
											RenderManager.class, Map.class,
											RenderManager.instance);

							if (registeredRenderers != null) {
								// Add mcditty update tick hook entity renderer
								if (!(registeredRenderers
										.get(MCDittyTickHookEntity.class) instanceof RenderMCDittyUpdateHook)) {
									RenderMCDittyUpdateHook r = new RenderMCDittyUpdateHook();
									r.setRenderManager(RenderManager.instance);
									registeredRenderers.put(
											MCDittyTickHookEntity.class,
											r);
								}

								// Add note block tooltip renderer
								if (!(registeredRenderers
										.get(EntityNoteBlockTooltip.class) instanceof RenderNoteBlockTooltip)) {
									RenderNoteBlockTooltip r = new RenderNoteBlockTooltip();
									r.setRenderManager(RenderManager.instance);
									registeredRenderers.put(
											EntityNoteBlockTooltip.class, r);
								}

							}
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}

		});
		t.setName("MCDitty Tick Hook Checker");
		t.start();

		// Load the config file
		MCDittyConfig.checkConfig(null);

		// System.out.println("MCDitty Load: JFugue: "
		// + jfugueLoadTime + ", SFX: "
		// + sfxLoadTime+", SignRendererTrig: "+trigLoadTime);
	}

	/**
	 * Run on the first tick of the game.
	 */
	public void doFirstTickSetup() {
		// // Win installed achievement
		// REMOVED for 1.3.1 update
		// ModLoader.getMinecraftInstance().thePlayer.addStat(
		// installedAchievement, 1);
		// Check to see that the config file is up to date
		MCDittyConfig.checkConfig(Minecraft.getMinecraft().theWorld);
		// Show welcome message, if any
		if (MCDittyConfig.showWelcomeMessage) {
			// Show a welcome message
			Minecraft.getMinecraft().thePlayer
					.addChatMessage("§bMCDitty Installed! §ePress Ctrl+"
							+ Keyboard.getKeyName(keypressHandler
									.getBindingByAction("menu").getMainKey())
							+ "§a to open the MCDitty menu.");
			// Turn off show welcome message flag
			MCDittyConfig.showWelcomeMessage = false;
			try {
				MCDittyConfig.flushAll();
			} catch (IOException e) {
				// TODO Tell user
				e.printStackTrace();
			}
		}

		Thread t = new Thread(new Runnable() {

			public void run() {
				// Handle new version found alert
				String foundVersion = BlockSign
						.downloadCurrentVersion(MCDittyConfig.MC_CURRENT_VERSION);
				// If new version found is greater than the current AND is
				// greater
				// than the last time we checked
				if (CompareVersion.compareVersions(foundVersion,
						MCDittyConfig.CURRENT_VERSION) == CompareVersion.GREATER
						&& !MCDittyConfig.lastVersionFound.equals(foundVersion)) {
					// Show a message
					Minecraft.getMinecraft().thePlayer.addChatMessage("§aA new version of MCDitty (§b"
							+ foundVersion
							+ "§a) is available on Auto-Update! §ePress Ctrl+"
							+ Keyboard.getKeyName(keypressHandler
									.getBindingByAction("menu").getMainKey())
							+ " to open the MCDitty menu.");

					MCDittyConfig.lastVersionFound = foundVersion;
					try {
						MCDittyConfig.flushAll();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		t.setName("MCDitty Update Checker");
		t.start();

		// Get the HashMap with a list of the blocks currently being punched
		try {
			// Get Map storing which blocks are damaged
			RenderGlobal globalRenderer = Minecraft.getMinecraft().renderGlobal;
			Field[] globalRendererFields = globalRenderer.getClass()
					.getDeclaredFields();
			HashMap damageValues = null;
			for (Field renderField : globalRendererFields) {
				// System.out.println (renderField.getType().getName());
				if (renderField.getType() == Map.class) {
					renderField.setAccessible(true);
					damageValues = (HashMap) renderField.get(globalRenderer);
					break;
				}
			}

			if (damageValues != null) {
				MCDitty.damageValues = damageValues;
				lastDamageValues = damageValues;
			}
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Register any sound resources
		registerSoundResources();

	}

	/**
	 * Returns the mod's name: MCDitty Version xxxxxxxx. A leftover from the bad
	 * old days of ModLoader.
	 * 
	 * @return
	 */
	public String getName() {
		return "MCDitty Version " + getVersion();
	}

	/**
	 * Returns the current version of MCDitty. A leftover from ModLoader.
	 * 
	 * @return
	 */
	public String getVersion() {
		return MCDittyConfig.CURRENT_VERSION;
	}

	/**
	 * Called every tick in the gameloop. Updates MCDitty, checks for keyboard
	 * and mouse input, and handles things like collision checks and particle
	 * requests that have to be done during a tick. Holdover from ModLoader.
	 * 
	 * @param partialTick
	 *            the fractional tick, used for animations
	 * @param minecraft
	 * @return
	 */
	public boolean onTick(float partialTick, Minecraft minecraft) {
		// Do in any tick, not just in game (see below section)
		addButtonsToGuis();

		// Get the current gui
		GuiScreen currGui = minecraft.currentScreen;

		// Check whether a gui has just been closed
		if (currGui == null && lastTickGui != null) {
			// Just left a gui -- put a anti-click timer up to avoid the click
			// that closed the gui clicking something in the game that is
			// checked here.
			doNotCheckForClicks = true;
			MCDittyClickCheckThread t = new MCDittyClickCheckThread();
			t.start();
		}
		lastTickGui = currGui;

		if (firstTick) {
			doFirstTickSetup();
			// Mark the first tick as past
			firstTick = false;
		}

		// Update the block damage value for sign entities
		updateSignBlockDamages(minecraft);

		// TODO: Find a better way of doing this
		if (slowMinecraft) {
			try {
				// System.out.println (sleepOnTickTime);
				Thread.sleep(sleepOnTickTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Check all prox pads for collisions
		updateProxpads(minecraft);

		// If not in a gui...
		if (currGui == null) {
			// Do keyboard checks
			// TODO: Move to separate thread
			checkForKeyCommands();

			// Check for a sign to be clicked
			if (!doNotCheckForClicks) {
				handleMouseInput(minecraft);
			}
		}

		// Check queues for any events to process from songs
		processLyricsRequests();
		processParticleRequests(minecraft);

		// Finish and return
		lastTickGui = currGui;
		return true;
	}

	private void handleMouseInput(Minecraft minecraft) {
		if (minecraft != null && minecraft.objectMouseOver != null
				&& !MCDittyConfig.mcdittyOff) {
			// Handle the player looking at a noteblock
			// Get the block the mouse is pointing at
			Point3D hoverPoint = new Point3D(minecraft.objectMouseOver.blockX,
					minecraft.objectMouseOver.blockY,
					minecraft.objectMouseOver.blockZ);
			// Get the tile entity of the block the mouse is pointing at
			TileEntity hoverTileEntity = minecraft.theWorld.getBlockTileEntity(
					hoverPoint.x, hoverPoint.y, hoverPoint.z);
			if (hoverTileEntity != null
					&& hoverTileEntity instanceof TileEntityNoteMCDitty) {
				TileEntityNoteMCDitty noteTile = (TileEntityNoteMCDitty) hoverTileEntity;
				if (noteTile.noteValueKnown) {
					// Show tooltip if note value known
					showNoteblockTooltip(noteTile);
				}
			}
		}

		if (Mouse.isButtonDown(1) || Mouse.isButtonDown(0)) {
			if (minecraft != null && minecraft.objectMouseOver != null) {
				Point3D hoverPoint = new Point3D(
						minecraft.objectMouseOver.blockX,
						minecraft.objectMouseOver.blockY,
						minecraft.objectMouseOver.blockZ);
				// Get the item held by the player
				ItemStack heldStack = minecraft.thePlayer
						.getCurrentEquippedItem();
				int held = 0;
				if (heldStack != null) {
					held = heldStack.itemID;
				}

				if (BlockSign.getSignBlockType(hoverPoint, minecraft.theWorld) != null) {
					// A sign has been clicked!
					// Perform some functions of
					// BlockSign.blockActivated
					if (isIDPickaxe(held) && !BlockSign.clickHeld) {
						BlockSign.clickHeld = true;
						MCDittyRightClickCheckThread t = new MCDittyRightClickCheckThread();
						t.start();

						// Pickaxe! "Pick" a sign to test.
						TileEntity blockEntity = minecraft.theWorld
								.getBlockTileEntity(hoverPoint.x, hoverPoint.y,
										hoverPoint.z);
						if (blockEntity instanceof TileEntitySign) {
							((TileEntitySign) blockEntity).picked = !((TileEntitySign) blockEntity).picked;
							if (((TileEntitySign) blockEntity).picked) {
								pickedSigns.add((TileEntitySign) blockEntity);
								BlockSign.writeChatMessage(
										minecraft.theWorld,
										"§2Picked a sign. ("
												+ pickedSigns.size()
												+ " picked)");
							} else {
								pickedSigns
										.remove((TileEntitySign) blockEntity);
								BlockSign
										.writeChatMessage(minecraft.theWorld,
												"§2Un-picked a sign. ("
														+ pickedSigns.size()
														+ " left)");
							}
						}
					} else if (!isIDAxe(held)
							&& !minecraft.thePlayer.isSneaking()) {
						// Manually trigger blockActivated
						((BlockSign) Block.signPost).blockActivated(
								minecraft.theWorld, hoverPoint.x, hoverPoint.y,
								hoverPoint.z, minecraft.thePlayer);
					}
				} else {
					// If not aiming at a sign
					if ((held == 270 || held == 274 || held == 285
							|| held == 278 || held == 257)
							&& !BlockSign.clickHeld) {
						BlockSign.clickHeld = true;
						MCDittyRightClickCheckThread t = new MCDittyRightClickCheckThread();
						t.start();

						// Pickaxe! "Unpick" all signs.

						if (pickedSigns.size() > 0) {
							for (TileEntitySign t1 : pickedSigns) {
								t1.picked = false;
							}
							pickedSigns.clear();

							BlockSign.writeChatMessage(minecraft.theWorld,
									"§2Unpicked all signs.");
						}
					}
				}
			}
		}
	}

	private int getNoteBlockValue(Point3D hoverPoint, World world) {
		TileEntity tile = world.getBlockTileEntity(hoverPoint.x, hoverPoint.y,
				hoverPoint.z);
		if (tile != null && tile instanceof TileEntityNote) {
			TileEntityNote noteTile = (TileEntityNote) tile;
			byte note = noteTile.note;
			System.out.println(note);
			return note;
		} else {
			return 0;
		}
	}

	private void showProxpadCorners(Minecraft minecraft) {
		if (MCDittyConfig.debug) {
			for (ProxPadBoundingBox bb : proxPadBBs) {
				if (rand.nextDouble() > 0.7f) {
					if (bb.getBox() != null) {
						double xVel = rand.nextDouble();
						minecraft.theWorld.spawnParticle("note",
								bb.getBox().maxX, bb.getBox().maxY,
								bb.getBox().maxZ, xVel, 0D, 0D);
						minecraft.theWorld.spawnParticle("note",
								bb.getBox().minX, bb.getBox().minY,
								bb.getBox().minZ, xVel, 0D, 0D);
					}
				}
			}
		}
	}

	private void addButtonsToGuis() {
		GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
		if (currentScreen != null) {
			List mControlList = null;
			try {
				// long startNano = System.nanoTime();
				mControlList = (List) Finder
						.getUniqueTypedFieldFromClass(GuiScreen.class,
								List.class, currentScreen);

				// System.out.println ("profile gui check: "+(System.nanoTime()
				// - startNano));
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (mControlList != null) {
				if (currentScreen instanceof GuiOptions) {
					// Add a button
					GuiOptions m = (GuiOptions) currentScreen;
					if (!mControlList.contains(GUI_MCDITTY_BUTTON)) {
						mControlList.add(GUI_MCDITTY_BUTTON);
					}
				} else if (currentScreen instanceof GuiVideoSettings) {
					// Add a button
					GuiVideoSettings m = (GuiVideoSettings) currentScreen;
					if (!mControlList.contains(GUI_MCDITTY_VIDEO_BUTTON)) {
						GUI_MCDITTY_VIDEO_BUTTON.setBackscreen(m);
						mControlList.add(GUI_MCDITTY_VIDEO_BUTTON);
					}
				} else if (currentScreen instanceof GuiScreenBook) {
					// Add buttons
					MCDitty.updateBookEditorControls(
							(GuiScreenBook) currentScreen, mControlList);
				}
			}
		}
	}

	public void processLyricsRequests() {
		synchronized (lyricsQueue) {
			if (lyricsQueue.size() > 0) {
				CueEvent l = lyricsQueue.getFirst();
				lyricsQueue.removeFirst();

				// Show lyric
				if (MCDittyConfig.lyricsEnabled && l.getLyricText() != null
						&& l.getLyricText().trim().length() > 0) {
					BlockSign.writeChatMessage(
							Minecraft.getMinecraft().theWorld, l.getLyricText());
				}
			}
		}
	}

	public void processParticleRequests(Minecraft minecraft) {
		synchronized (particleRequestQueue) {
			LinkedList<ParticleRequest> delayed = new LinkedList<ParticleRequest>();
			while (particleRequestQueue.size() > 0) {
				// Emit particle
				ParticleRequest particleRequest = particleRequestQueue.pop();
				if (MCDittyConfig.particlesEnabled) {
					// Check to see if it needs to be delayed
					if (!(particleRequest.isInstant() || particleRequest
							.getTime() <= dittyTimes.get(particleRequest
							.getDittyID()))) {
						delayed.add(particleRequest);
						continue;
					}

					Point3D pos = particleRequest.getLocation();

					if (particleRequest instanceof NoteParticleRequest) {
						// Determines note color for some reason
						double xVel = 0d;
						if (((NoteParticleRequest) particleRequest)
								.isRandomColor()) {
							xVel = (float) rand.nextInt(24) / 24f;
						} else {
							xVel = ((NoteParticleRequest) particleRequest)
									.getNoteColor();
						}
						double partX = (double) pos.x + rand.nextDouble()
								* 0.4d + 0.2d;
						double partY = (double) pos.y + rand.nextDouble()
								* 0.4d + 0.2d;
						double partZ = (double) pos.z + rand.nextDouble()
								* 0.4d + 0.2d;
						Minecraft.getMinecraft().theWorld.spawnParticle("note",
								partX, partY, partZ, xVel, 0D, 0D);
						// // Manually spawn note particle to take advantage of
						// the
						// // faster constructor which doesn't check for note
						// // blocks
						// // 2.0 number copied from a EntityNoteFX constructor
						// GetMinecraft.instance().effectRenderer
						// .addEffect((EntityFX) new EntityNoteFX(
						// GetMinecraft.instance().theWorld,
						// partX, partY, partZ, xVel, 0d, 0d,
						// 2.0f));
					} else if (particleRequest instanceof HeartParticleRequest) {
						double var2 = rand.nextGaussian() * 0.02D;
						double var4 = rand.nextGaussian() * 0.02D;
						double var6 = rand.nextGaussian() * 0.02D;
						double partX = (double) pos.x + rand.nextDouble();
						double partY = (double) pos.y + rand.nextDouble();
						double partZ = (double) pos.z + rand.nextDouble();
						double height = 0.5;
						double width = 0.5;
						minecraft.theWorld.spawnParticle("heart", partX, partY,
								partZ, var2, var4, var6);
					} else if (particleRequest instanceof BubbleParticleRequest) {
						// double velX = (double) pos.x + r.nextDouble() * 0.4d
						// + 0.2d;
						// double velY = (double) pos.y + r.nextDouble() * 0.4d
						// + 0.2d;
						// double velZ = (double) pos.z + r.nextDouble() * 0.4d
						// + 0.2d;
						double velX = 0;
						double velY = 0;
						double velZ = 0;
						minecraft.theWorld.spawnParticle("bubble",
								pos.x + rand.nextDouble(),
								pos.y + rand.nextDouble(),
								pos.z + rand.nextDouble(), velX, velY, velZ);
					} else if (particleRequest.getParticleType()
							.equals("smoke")) {
						double partX = (double) pos.x + rand.nextDouble()
								* 0.4d + 0.2d;
						double partY = (double) pos.y + rand.nextDouble()
								* 0.3d + 0.1d;
						double partZ = (double) pos.z + rand.nextDouble()
								* 0.4d + 0.2d;
						double velX = 0;
						double velY = rand.nextDouble() * 0.1f;
						double velZ = 0;
						minecraft.theWorld.spawnParticle("smoke", partX, partY,
								partZ, velX, velY, velZ);
					} else if (particleRequest.getParticleType().equals(
							"villagerFace")) {
						// Generic particle
						// Determine location
						double locationVariance = particleRequest
								.getLocationVariance();
						double centerOffset = 0.5f;
						double partX = (double) pos.x + centerOffset
								+ locationVariance * getBoundedGaussian(1);
						double partY = (double) pos.y + centerOffset
								+ locationVariance * getBoundedGaussian(1);
						double partZ = (double) pos.z + centerOffset
								+ locationVariance * getBoundedGaussian(1);
						// Determine velocity
						double velX = 0;
						double velY = rand.nextDouble() * 0.3f;
						double velZ = 0;
						// Spawn custom particle
						// TODO: Check particle settings and distance
						EntityFX customFX = new EntityHeartFX(
								minecraft.theWorld, partX, partY, partZ, velX,
								velY, velZ);
						((EntityFX) customFX).setParticleTextureIndex(83);
						((EntityFX) customFX).setRBGColorF(1.0F, 1.0F, 1.0F);
						minecraft.effectRenderer.addEffect(customFX);
					} else {
						// Generic particle
						// Determine location
						double locationVariance = particleRequest
								.getLocationVariance();
						double centerOffset = 0.5f;
						double partX = (double) pos.x + centerOffset
								+ locationVariance * getBoundedGaussian(1);
						double partY = (double) pos.y + centerOffset
								+ locationVariance * getBoundedGaussian(1);
						double partZ = (double) pos.z + centerOffset
								+ locationVariance * getBoundedGaussian(1);
						// Fix angry villager particles being a block too high
						if (particleRequest.getParticleType().equals(
								"angryVillager")) {
							partY--;
						}
						// Determine velocity
						double velX = 0;
						double velY = rand.nextDouble() * 0.3f;
						double velZ = 0;
						minecraft.theWorld.spawnParticle(
								particleRequest.getParticleType(), partX,
								partY, partZ, velX, velY, velZ);
					}
				}
			}

			// Put delayed stuff back in queue for later
			particleRequestQueue.addAll(delayed);
		}
	}

	/**
	 * Returns a random gaussian value between rangePlusMinus and
	 * -rangePlusMinus, both inclusive.
	 * 
	 * @param rangePlusMinus
	 * @return
	 */
	private double getBoundedGaussian(double rangePlusMinus) {
		double candidateValue = rand.nextGaussian();
		if (candidateValue > rangePlusMinus) {
			return rangePlusMinus;
		} else if (candidateValue < -rangePlusMinus) {
			return -rangePlusMinus;
		} else {
			return candidateValue;
		}
	}

	public static boolean isIDPickaxe(int id) {
		// Order of checks: iron, wood, stone, diamond, gold
		return (id == 257 || id == 270 || id == 274 || id == 278 || id == 285);
	}

	public static boolean isIDAxe(int id) {
		return (id == 258 || id == 271 || id == 275 || id == 279 || id == 286);
	}

	public static boolean isIDShovel(int id) {
		return (id == 256 || id == 269 || id == 273 || id == 277 || id == 284);
	}

	public static boolean isIDHoe(int id) {
		return (id >= 290 && id <= 294);
	}

	public static boolean idIDSword(int id) {
		return (id == 267 || id == 268 || id == 272 || id == 276 || id == 283);
	}

	public void checkForKeyCommands() {
		keypressHandler.update();
	}

	/**
	 * Updates the list of proxpads (adding BBs to new, undefined pads) and
	 * checks for collisions with the player.
	 * 
	 * @param minecraft
	 */
	public void updateProxpads(Minecraft minecraft) {
		// Eschew all proxpad updates and activations if MCDitty is off
		if (MCDittyConfig.mcdittyOff) {
			return;
		}

		// Debug proxpads by showing corners with particles
		showProxpadCorners(minecraft);

		// Set up any prox pads that still need their bounding boxes defined
		addBBsToNewProxpads();

		// Find a point in the top and bottom block filled by the player
		Vec3[] playerPoints = getPlayerCollisionPoints();
		for (int i = 0; i < proxPadBBs.size(); i++) {
			ProxPadBoundingBox bb = proxPadBBs.get(i);
			// if (playerBB == null) {
			// // System.out.println("Player bb is null!");
			// continue;
			// }

			if (bb.getBox() == null) {
				// System.out.println("ProxPad bb is null in the collision checker!!!");
				continue;
			}

			boolean playerInsideBB = isPlayerCollidingWith(bb.getBox());
			if (playerInsideBB) {
				if (bb.getLockout() == false) {
					boolean proxPadValid = false;

					// Enable lockout
					bb.setLockout(true);
					// Read sign text, and if it is a proxpad sign, start
					// the
					// song
					TileEntity signEntity = Minecraft.getMinecraft().theWorld
							.getBlockTileEntity(bb.getPadX(), bb.getPadY(),
									bb.getPadZ());
					if (signEntity instanceof TileEntitySign) {
						TileEntitySign signTileEntity = (TileEntitySign) signEntity;
						if (signTileEntity != null) {
							for (String s : signTileEntity.signText) {
								// Check this line for the keyword proxpad
								ParsedKeyword candidateKeyword = SignParser
										.parseKeyword(s);
								if (!(candidateKeyword instanceof ProxPadKeyword)) {
									// If there really appears to be no proxpad,
									// continue
									continue;
								}

								ProxPadKeyword k = (ProxPadKeyword) candidateKeyword;
								// Is proxpad keyword: is the keyword for this
								// particular proxpad?
								if (k.getHeight() == bb.getHeight()
										&& k.getWidth() == bb.getWidth()
										&& k.getLength() == bb.getDepth()) {
									// Start tune from this proximity sign
									if (MCDittyConfig.proxPadsEnabled) {
										BlockSign
												.playDittyFromSigns(
														Minecraft.getMinecraft().theWorld,
														bb.getPadX(),
														bb.getPadY(),
														bb.getPadZ(), true);
									}
									proxPadValid = true;
									break;
								} else {

								}
							}
						}
					}

					// If the prox pad is attached to a sign that no longer
					// has
					// the prox pad keyword on it for this sign,
					// delete it's bounding box and don't check it again
					if (!proxPadValid) {
						proxPadBBs.remove(i);
						i--;
					}
				}

			}

			// Disable lockout if player has left bb
			// Could be rephrased as } else { to above if()
			if (!playerInsideBB) {
				// Disable lockout
				bb.setLockout(false);
			}
		}
	}

	/**
	 * Returns a pair of points, one in the player's head and one in his feet.
	 * If either of these points is inside a bounding box, the player is
	 * colliding with that box.
	 * 
	 * @return
	 */
	private Vec3[] getPlayerCollisionPoints() {
		Minecraft minecraft = Minecraft.getMinecraft();
		Vec3[] playerPoints = new Vec3[2];
		playerPoints[0] = Vec3.createVectorHelper(minecraft.thePlayer.posX,
				minecraft.thePlayer.posY - 0.5d, minecraft.thePlayer.posZ);
		playerPoints[1] = Vec3.createVectorHelper(minecraft.thePlayer.posX,
				minecraft.thePlayer.posY - 1.5d, minecraft.thePlayer.posZ);
		return playerPoints;
	}

	/**
	 * Returns true if the player is colliding with the given bounding box
	 * 
	 * @param bb
	 * @return
	 */
	private boolean isPlayerCollidingWith(AxisAlignedBB bb) {
		boolean playerInsideBB = false;
		Vec3[] playerPoints = getPlayerCollisionPoints();
		for (int currPoint = 0; currPoint < playerPoints.length; currPoint++) {
			if (bb.isVecInside(playerPoints[currPoint])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * When a proxpad is newly created from a new sign, its bounding box only
	 * has a position (it's originating sign); not a size. This checks every
	 * proxpad to see if it is new, and in this indeterminate state, and tries
	 * to define the proxpad from the keyword on the sign. Also removes
	 * duplicate proxpads.
	 * 
	 * TODO: Update proxpad keyword sign parsing to parse the whole sign, not
	 * individual lines.
	 */
	public void addBBsToNewProxpads() {
		for (int i = 0; i < proxPadBBs.size(); i++) {
			ProxPadBoundingBox bb = proxPadBBs.get(i);

			if (bb.getBox() == null) {
				// System.out.println("BB is null!");
				// Make bounding box

				// We can be pretty sure that x, y, z is a sign.
				// Get type of sign
				Block signType = BlockSign.getSignBlockType(
						new Point3D(bb.getPadX(), bb.getPadY(), bb.getPadZ()),
						Minecraft.getMinecraft().theWorld);
				if (signType == null) {
					continue;
				}
				// System.out.println(": " + signType.blockID);

				// Get sign facing
				int facing = BlockSign.getSignFacing(
						Minecraft.getMinecraft().theWorld.getBlockMetadata(
								bb.getPadX(), bb.getPadY(), bb.getPadZ()),
						signType);
				// System.out.println("= " + facing);

				boolean firstProxPadKeyword = true;
				for (String s : ((TileEntitySign) Minecraft.getMinecraft().theWorld
						.getBlockTileEntity(bb.getPadX(), bb.getPadY(),
								bb.getPadZ())).signText) {
					// Get proxpad size

					// Check this line for the keyword proxpad
					ParsedKeyword candidateKeyword = SignParser.parseKeyword(s);
					if (!(candidateKeyword instanceof ProxPadKeyword)) {
						// If there really appears to be no proxpad, continue
						continue;
					}

					// If it's there, process it.
					// Retrieve the width, length, and height of the proxpad
					ProxPadKeyword keyword = (ProxPadKeyword) candidateKeyword;
					double width = keyword.getWidth();
					double length = keyword.getLength();
					double height = keyword.getHeight();
					bb.setWidth((int) width);
					bb.setHeight((int) height);
					bb.setDepth((int) length);

					// Find corners of proxpad bounds
					double y1 = bb.getPadY() + 0.5d - (height / 2d);
					double y2 = bb.getPadY() + 0.5d + (height / 2d);

					double x1 = 0;
					double x2 = 0;

					double z1 = 0;
					double z2 = 0;
					if (facing == BlockSign.FACES_SOUTH) {
						// width is along x axis
						x1 = (double) bb.getPadX() + 0.5d - (width / 2d);
						x2 = (double) bb.getPadX() + 0.5d + (width / 2d);

						// length is along z axis
						z1 = bb.getPadZ();
						z2 = z1 + length;
					} else if (facing == BlockSign.FACES_NORTH) {
						// width is along x axis
						x1 = (double) bb.getPadX() + 0.5d - (width / 2d);
						x2 = (double) bb.getPadX() + 0.5d + (width / 2d);

						// length is along z axis
						z1 = bb.getPadZ() + 1;
						z2 = z1 - length;
					} else if (facing == BlockSign.FACES_EAST) {
						// width is along z axis
						z1 = (double) bb.getPadZ() + 0.5d - (width / 2d);
						z2 = (double) bb.getPadZ() + 0.5d + (width / 2d);

						// length is along x axis
						x1 = bb.getPadX();
						x2 = x1 + length;
					} else if (facing == BlockSign.FACES_WEST) {
						// width is along z axis
						z1 = (double) bb.getPadZ() + 0.5d - (width / 2d);
						z2 = (double) bb.getPadZ() + 0.5d + (width / 2d);

						// length is along x axis
						x1 = bb.getPadX() + 1;
						x2 = x1 - length;
					} else {
						// bad facing
						System.out
								.println("ProxPad called on a diagonal signpost.");
						// Shove bounding box into the wild blue yonder
						y1 = -100000;
						y2 = y1;
						x1 = y1;
						x2 = y1;
						z1 = y1;
						z2 = y1;
					}

					// Arrange values correctly by min and max
					if (x1 > x2) {
						double temp;
						temp = x1;
						x1 = x2;
						x2 = temp;
					}

					if (z1 > z2) {
						double temp;
						temp = z1;
						z1 = z2;
						z2 = temp;
					}

					// Put the new bounding box in to the proxpadbb
					// System.out.println("bb=" + x1 + ":" + y1 + ":" + z1 + ":"
					// + x2 + ":" + y2 + ":" + z2);

					if (firstProxPadKeyword) {
						bb.setBox(AxisAlignedBB.getBoundingBox(x1, y1, z1, x2,
								y2, z2));
						firstProxPadKeyword = false;
					} else {
						// System.out.println("Additional bounding box!");
						ProxPadBoundingBox newBB = new ProxPadBoundingBox();
						newBB.setPadX(bb.getPadX());
						newBB.setPadY(bb.getPadY());
						newBB.setPadZ(bb.getPadZ());
						newBB.setWidth(bb.getWidth());
						newBB.setHeight(bb.getHeight());
						newBB.setDepth(bb.getDepth());
						newBB.setBox(AxisAlignedBB.getBoundingBox(x1, y1, z1,
								x2, y2, z2));
						proxPadBBs.add(newBB);
					}
				}

				// // If for some unponderable reason, we come this far to have
				// the
				// // sign not be a proxpad sign, leaving the box field blank,
				// // remove this proxpad entry.
				// if (bb.getBox() == null) {
				// proxPadBBs.remove(bb);
				// i--;
				// }

				// After adding new boxes, remove any duplicates
				for (int b = 0; b < proxPadBBs.size(); b++) {
					for (int c = 0; c < proxPadBBs.size(); c++) {
						if (proxPadBBs.get(c).equals(proxPadBBs.get(b))
								&& b != c) {
							proxPadBBs.remove(c);
							c--;
						}
					}
				}
			}
		}
	}

	/**
	 * Checks minecraft's list of damaged blocks for signs, and updates the
	 * damage field in all affected TileEntitySigns.
	 * 
	 * @param minecraft
	 */
	public void updateSignBlockDamages(Minecraft minecraft) {
		// Show damage states on signs
		if (damageValues != null) {
			// Cycle through the damaged blocks, setting the damage on each sign
			// in the list from the damaged blocks list
			Iterator it = damageValues.values().iterator();
			while (it.hasNext()) {
				DestroyBlockProgress damage = (DestroyBlockProgress) it.next();
				int damBlockX = damage.getPartialBlockX();
				int damBlockY = damage.getPartialBlockY();
				int damBlockZ = damage.getPartialBlockZ();
				if (BlockSign.getSignBlockType(new Point3D(damBlockX,
						damBlockY, damBlockZ), minecraft.theWorld) != null) {
					TileEntity entity = Minecraft.getMinecraft().theWorld
							.getBlockTileEntity(damBlockX, damBlockY, damBlockZ);
					if (entity != null && entity instanceof TileEntitySign) {
						((TileEntitySign) entity).damage = damage
								.getPartialBlockDamage();
					}
				}
			}
			// Handle resetting the damage on a sign that is no longer being
			// damaged
			Iterator lastIt = lastDamageValues.values().iterator();
			while (lastIt.hasNext()) {
				DestroyBlockProgress lastDamage = (DestroyBlockProgress) lastIt
						.next();

				// Try to find lastDamage's equivalent in the current list of
				// block damages
				boolean stillInList = false;
				it = damageValues.values().iterator();
				while (it.hasNext()) {
					DestroyBlockProgress damage = (DestroyBlockProgress) it
							.next();
					if (lastDamage.getPartialBlockX() == damage
							.getPartialBlockX()
							&& lastDamage.getPartialBlockY() == damage
									.getPartialBlockY()
							&& lastDamage.getPartialBlockZ() == damage
									.getPartialBlockZ()) {
						stillInList = true;
						break;
					}
				}

				if (stillInList) {
					// If still being damaged, let it be
				} else {
					// If no longer being damaged, try to reset damage value
					TileEntity entity = Minecraft.getMinecraft().theWorld
							.getBlockTileEntity(lastDamage.getPartialBlockX(),
									lastDamage.getPartialBlockY(),
									lastDamage.getPartialBlockZ());
					if (entity != null && entity instanceof TileEntitySign) {
						((TileEntitySign) entity).damage = 0;
					}
				}
			}
			lastDamageValues = (HashMap) damageValues.clone();
		}
	}

	/**
	 * Tries to create a new entity to hook into Minecraft ticks with, removing
	 * any old ones.
	 * 
	 * Unfortunately, that last bit is a tad optimistic given a) Minecraft's
	 * wonky entity lists and b) my wonky understanding of them.
	 */
	public static void createHookEntity(MCDitty mcditty) {
		try {
			if (hookEntity != null) {
				// Check that it's still in the world
				if (Minecraft.getMinecraft().theWorld != null) {
					if (!Minecraft.getMinecraft().theWorld.loadedEntityList
							.contains(hookEntity)) {
						// Add to world
						hookEntity = new MCDittyTickHookEntity(
								Minecraft.getMinecraft().theWorld);
						Minecraft.getMinecraft().theWorld.addEntityToWorld(
								12345678, hookEntity);
						// System.out.println("Adding MCDitty tick hook entity");
					} else {
						// System.out.println
						// ("Tick hook entity already added!");
					}
				}
				return;
			} else {
				hookEntity = new MCDittyTickHookEntity(
						Minecraft.getMinecraft().theWorld);
				Minecraft mc1 = Minecraft.getMinecraft();
				if (mc1 != null && mc1.theWorld != null) {
					mc1.theWorld.addEntityToWorld(12345678, hookEntity);
				} else {
					// Failed
					hookEntity = null;
				}
			}
			
			// If possible, add the MCDitty as a tickListener
			if (hookEntity != null) {
				addTickListenersToHookEntity(hookEntity);
			}
		} catch (Exception e) {
			// Failed
			hookEntity = null;
		}
	}

	/**
	 * @param hookEntity2
	 */
	private static void addTickListenersToHookEntity(
			MCDittyTickHookEntity hookEntity2) {
		// Add MCDitty and the fireworks exploder
		hookEntity2.addTickListener(BlockSign.mcDittyMod);
		hookEntity2.addTickListener(fireworkExploder);
		hookEntity2.addTickListener(blockTuneManager);
	}

	/**
	 * Discouraged outside of MCDitty: should be internal-only. Use
	 * addDittyEventToQueue instead.
	 * 
	 * @param nextLyricToPlay
	 */
	@Deprecated
	public static void addLyricToQueue(CueEvent nextLyricToPlay) {
		synchronized (lyricsQueue) {
			lyricsQueue.add(nextLyricToPlay);
		}
	}

	/**
	 * Adds a TimedDittyEvent to a list of events that will be processed as soon
	 * as possible in real time, in the MC game loop.
	 * 
	 * @param nextEventToFire
	 */
	public static void executeTimedDittyEvent(TimedDittyEvent nextEventToFire) {

		Minecraft minecraft = Minecraft.getMinecraft();
		WorldClient world = minecraft.theWorld;
		EntityClientPlayerMP player = minecraft.thePlayer;

		if (nextEventToFire instanceof CueEvent) {
			// Cue
			CueEvent cue = (CueEvent) nextEventToFire;

			// Handle any bot actions
			if (cue.getBotActions() != null && cue.getBotActions().size() > 0) {
				// Execute bot actions
				for (BotAction action : cue.getBotActions()) {
					executeBotAction(action);
				}
			}

			// Handle any lyrics
			if (cue.getLyricText() != null && cue.getLyricText().length() > 0) {
				addLyricToQueue(cue);
			}
		} else if (nextEventToFire instanceof CreateBotEvent) {
			CreateBotEvent botEvent = (CreateBotEvent) nextEventToFire;

			Bot newBot;

			// First create the new bot
			if (botEvent.getType().equalsIgnoreCase("villager")) {
				newBot = new VillagerBot(botEvent.getName(),
						botEvent.getLocX(), botEvent.getLocY(),
						botEvent.getLocZ(), botEvent.getRotation());
			} else {
				// unrecognized
				return;
			}

			// Then add the bot to lists, creating a new list of bots for this
			// ditty if needed
			LinkedList<Bot> botsForDitty = bots.get(botEvent.getDittyID());
			if (botsForDitty == null) {
				botsForDitty = new LinkedList<Bot>();
				bots.put(botEvent.getDittyID(), botsForDitty);
			}

			botsForDitty.add(newBot);
		} else if (nextEventToFire instanceof NoteStartEvent) {
			NoteStartEvent noteEvent = (NoteStartEvent) nextEventToFire;
			if (noteEvent.getLocation() != null
					&& MCDittyConfig.particlesEnabled
					&& !MCDittyConfig.emitOnlyOneParticle) {
				synchronized (particleRequestQueue) {
					double noteColor = 0d;
					if (noteEvent.getNote() != null) {
						noteColor = (double) (noteEvent.getNote().getValue() % 24) / 24d;
					}
					particleRequestQueue.add(new NoteParticleRequest(noteEvent
							.getLocation(), noteColor, false));
				}
			}

			for (DiscoFloor d : discoFloors) {
				if (d.getDittyID() == nextEventToFire.getDittyID()
						&& d.getVoices().contains(noteEvent.getVoice())) {
					d.pulse(world, noteEvent);
				}
			}

			for (Emitter e : emitters) {
				if (e.getDittyID() == nextEventToFire.getDittyID()) {
					synchronized (particleRequestQueue) {
						particleRequestQueue
								.addAll(e
										.processNoteEvent((NoteStartEvent) nextEventToFire));
					}
				}
			}
		} else if (nextEventToFire instanceof HighlightSignPlayingEvent) {
			// System.out.println
			// ("Highlighting sign: "+((HighlightSignPlayingEvent)
			// nextEventToFire).getPos()+" ?"+((HighlightSignPlayingEvent)
			// nextEventToFire).isTurnOn());
			HighlightSignPlayingEvent highlightEvent = (HighlightSignPlayingEvent) nextEventToFire;
			if (highlightEvent.isTurnOn()) {
				TileEntity s = world.getBlockTileEntity(
						highlightEvent.getPos().x, highlightEvent.getPos().y,
						highlightEvent.getPos().z);
				if (s instanceof TileEntitySign) {
					((TileEntitySign) s)
							.setHighlightColor(MCDittyConfig.signPlayingHighlightColor);
					onSigns.add(highlightEvent);
				}
			} else {
				TileEntity s = world.getBlockTileEntity(
						highlightEvent.getPos().x, highlightEvent.getPos().y,
						highlightEvent.getPos().z);
				if (s instanceof TileEntitySign) {
					float[] noColor = new float[4];
					noColor[0] = -1;
					((TileEntitySign) s).setHighlightColor(noColor);
					for (int i = 0; i < onSigns.size(); i++) {
						HighlightSignPlayingEvent h = onSigns.get(i);
						if (h.getDittyID() == nextEventToFire.getDittyID()) {
							if (h.getPos()
									.equals(((HighlightSignPlayingEvent) nextEventToFire)
											.getPos())) {
								onSigns.remove(i);
								i--;
							}
						}
					}
				}
			}
		} else if (nextEventToFire instanceof FireworkEvent) {
			EntityFireworkRocket fireworkEntity = new EntityFireworkRocket(
					world, ((FireworkEvent) nextEventToFire).getX(),
					((FireworkEvent) nextEventToFire).getY(),
					((FireworkEvent) nextEventToFire).getZ(),
					((FireworkEvent) nextEventToFire).getFireworkItem());
			world.spawnEntityInWorld(fireworkEntity);

			NBTTagCompound tag = ((FireworkEvent) nextEventToFire)
					.getFireworkItem().stackTagCompound
					.getCompoundTag("Fireworks");
			fireworkExploder.add(fireworkEntity,
					((FireworkEvent) nextEventToFire).getY(),
					tag.getByte("Flight"));
		} else if (nextEventToFire instanceof DittyEndedEvent) {
			// System.out.println ("Time to wrap up.");
			// Note that this ditty has ended
			synchronized (endedDitties) {
				endedDitties.add(nextEventToFire.getDittyID());
			}
			// De-highglight signs
			for (int i = 0; i < onSigns.size(); i++) {
				HighlightSignPlayingEvent h = onSigns.get(i);
				// System.out.println
				// ("SignOn! "+nextEventToFire.getDittyID()+"vs."+h.getDittyID());
				if (h.getDittyID() == nextEventToFire.getDittyID()) {
					// System.out.println ("Switching off");
					HighlightSignPlayingEvent e = onSigns.get(i);
					onSigns.remove(i);
					i--;

					float[] noColor = new float[4];
					noColor[0] = -1;
					TileEntity s = world.getBlockTileEntity(e.getPos().x,
							e.getPos().y, e.getPos().z);
					if (s instanceof TileEntitySign) {
						((TileEntitySign) s).setHighlightColor(noColor);
					}
				}
			}
			// Deactivate any attached discofloors
			for (int i = 0; i < discoFloors.size(); i++) {
				DiscoFloor d = discoFloors.get(i);
				if (d.getDittyID() == nextEventToFire.getDittyID()) {
					d.turnOff(world);

					// Remove from lists
					discoFloors.remove(i);
					i--;
				}
			}

			// Deactivate any emitters
			for (int i = 0; i < emitters.size(); i++) {
				Emitter e = emitters.get(i);
				if (e.getDittyID() == nextEventToFire.getDittyID()) {
					// Remove from lists
					emitters.remove(i);
					i--;
				}
			}

			// Kill any bots
			// Don't tell PETE (People for the Ethical Treatment of Entities)
			executeBotAction(new DestroyAction("*",
					nextEventToFire.getDittyID()));
		} else if (nextEventToFire instanceof SFXMCDittyEvent) {
			if (((SFXMCDittyEvent) nextEventToFire).getSoundEffect() != null) {
				double x = player.posX;
				double y = player.posY;
				double z = player.posZ;
				// System.out.println (((FXMCDittyEvent)
				// nextEventToFire).getSoundEffect());

				// Try to account for ditty track volume
				float trackVolume = 1.0f;
				Float[] currTrackVols = dittyTrackVolumes.get(nextEventToFire
						.getDittyID());
				if (currTrackVols != null) {
					trackVolume = currTrackVols[nextEventToFire.getVoice()];
				}

				// Play sound
				minecraft.sndManager.playSound(
						((SFXMCDittyEvent) nextEventToFire).getSoundEffect(),
						(float) x, (float) y, (float) z, 1.0f * trackVolume,
						1.0f);
			}
		} else if (nextEventToFire instanceof PlayMidiDittyEvent) {
			PlayMidiDittyEvent playMidiEvent = (PlayMidiDittyEvent) nextEventToFire;
			playMidiFile(playMidiEvent.getMidiFile());
		} else if (nextEventToFire instanceof VolumeEvent) {
			VolumeEvent volumeEvent = (VolumeEvent) nextEventToFire;

			// Retrieve the old track volumes for the ditty
			Float[] currDittyVols = dittyTrackVolumes.get(nextEventToFire
					.getDittyID());
			if (currDittyVols == null) {
				// If they aren't yet noted, set up the volumes for this ditty
				currDittyVols = new Float[16];
				Arrays.fill(currDittyVols, 1.0f);
			}
			// Set the new volume
			currDittyVols[volumeEvent.getVoice()] = volumeEvent.getVolume();
			// Save track volumes
			dittyTrackVolumes.put(volumeEvent.getDittyID(), currDittyVols);
		} else if (nextEventToFire instanceof TempoDittyEvent) {
			// Update tempo for ditty
			TempoDittyEvent tempoEvent = (TempoDittyEvent) nextEventToFire;
			updateDittyTempo(tempoEvent.getDittyID(), tempoEvent.getTempo());
		} else if (nextEventToFire instanceof CreateEmitterEvent) {
			// Create a new emitter
			CreateEmitterEvent emitterEvent = (CreateEmitterEvent) nextEventToFire;
			emitters.add(new Emitter(emitterEvent));
		}
	}

	/**
	 * Forwards a BotAction to its destination bot to be executed immediately
	 * 
	 * @param action
	 *            null is okay
	 */
	private static void executeBotAction(BotAction action) {
		if (action == null) {
			return;
		}

		// Get a list of all bots for the ditty this action is a part of
		LinkedList<Bot> dittyBots = bots.get(action.getDittyID());

		if (dittyBots == null) {
			return;
		}

		// Try to find the adressed bot
		// First, convert MS-DOS wildcards to regex
		String address = BotAction.convertWildcardsToRegex(action.getAddress());
		for (Bot b : dittyBots) {
			if (b.getName().matches(address)) {
				// Send action to bot
				b.doAction(action);
			}
		}
	}

	public static void onDittyTick(int dittyID, long time) {
		// Update the ditty's current time
		dittyTimes.put(dittyID, time);
	}

	/**
	 * Currently assumes that midiFile ends with ".mid"
	 * 
	 * @param midiFile
	 */
	public static void playMidiFile(File midiFile) {
		if (midiFile.exists()) {
			try {
				// From file
				Sequence sequence = MidiSystem.getSequence(midiFile);

				// Create a sequencer for the sequence
				Sequencer sequencer = MidiSystem.getSequencer();
				sequencer.open();
				sequencer.setSequence(sequence);

				// Start playing
				sequencer.start();

				// Add to list of sequencers, in case of muting
				playMidiSequencers.add(sequencer);
			} catch (IOException e) {
			} catch (MidiUnavailableException e) {
			} catch (InvalidMidiDataException e) {
			}
		}
	}

	/**
	 * Requests that MC's main thread sleep l milliseconds each tick.
	 * 
	 * @param l
	 */
	public static void slowDownMC(long l) {
		slowMinecraft = true;
		sleepOnTickTime = l;
	}

	/**
	 * Stops sleeping each tick.
	 */
	public static void stopMCSlowdown() {
		slowMinecraft = false;
	}

	/**
	 * Set the current tempo noted for a given ditty.
	 * 
	 * @param dittyID
	 * @param tempo
	 */
	public static void updateDittyTempo(int dittyID, float tempo) {
		dittyTempos.put(dittyID, tempo);
	}

	/**
	 * Gets the last noted tempo for a given ditty. Does not directly poll the
	 * synth or anything like that.
	 * 
	 * @param dittyID
	 * @return
	 */
	public static float getDittyTempo(int dittyID) {
		return dittyTempos.get(dittyID);
	}

	/**
	 * Adds a new proxpad to the list, with an undefined bounding box (save for
	 * the source sign).
	 * 
	 * @param xCoord
	 * @param yCoord
	 * @param zCoord
	 */
	public static void queueProxPadSign(int xCoord, int yCoord, int zCoord) {
		// System.out.println("Prox pad sign at " + xCoord + ":" + yCoord + ":"
		// + zCoord);
		ProxPadBoundingBox proxPad = new ProxPadBoundingBox();
		proxPad.setPadX(xCoord);
		proxPad.setPadY(yCoord);
		proxPad.setPadZ(zCoord);
		proxPadBBs.add(proxPad);
	}

	public static void onSignLoaded(int x, int y, int z, String[] text) {
		TileEntitySign sign = new TileEntitySign();
		sign.xCoord = x;
		sign.yCoord = y;
		sign.zCoord = z;
		sign.signText = text;

		onSignLoaded(sign);
	}

	/**
	 * Processes text on signs as they are loaded, checking for keywords ect.
	 * 
	 * @param entitySign
	 */
	public static void onSignLoaded(TileEntitySign entitySign) {
		// Look for keywords that need "activating" on load -- like proxpads
		for (int i = 0; i < entitySign.signText.length; i++) {
			// Note any comments on signs
			String[] signText = entitySign.getSignTextNoCodes();
			if (Comment.isLineComment(signText[i])) {
				Comment newComment = new Comment(new SignLine(
						entitySign.xCoord, entitySign.yCoord,
						entitySign.zCoord, i), signText[i]);
				addCommentToList(newComment);

				// line is a comment; no keywords possible
				continue;
			}

			String keyword = SignParser.recognizeKeyword(signText[i]);
			if (keyword != null
					&& (keyword.equals("proxpad")
							|| keyword.equals("proximity") || keyword
								.equals("area"))) {
				MCDitty.queueProxPadSign(entitySign.xCoord, entitySign.yCoord,
						entitySign.zCoord);
			}
		}

		// Add sign to an index
		synchronized (signIndex) {
			signIndex.add(entitySign);
		}
	}

	/**
	 * Adds a Comment to the list of all loaded comments. Does not add if
	 * another comment is in the list such that
	 * existingComment.equals(newComment) is true.
	 * 
	 * @param newComment
	 */
	private static void addCommentToList(Comment newComment) {
		boolean newCommentIsDuplicate = false;
		for (Comment c : comments) {
			if (c.equals(newComment)) {
				newCommentIsDuplicate = true;
				break;
			}
		}

		if (!newCommentIsDuplicate) {
			comments.add(newComment);
		}
	}

	/**
	 * Clears comments that don't exist anymore in the world from the comment
	 * list.
	 */
	public static void optimizeCommentList(World world) {
		for (int i = 0; i < comments.size(); i++) {
			Comment c = comments.get(i);
			if (!c.stillExistsInWorld(world)) {
				comments.remove(i);
				i--;
				continue;
			}
		}
	}

	/**
	 * Returns the signs that have been loaded in a particular block, in
	 * chronological order.
	 * 
	 * Synchronized
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static TileEntitySign[] getSignsForPos(int x, int y, int z) {
		synchronized (signIndex) {
			ArrayList<TileEntitySign> signsFound = new ArrayList<TileEntitySign>();
			for (TileEntitySign t : signIndex) {
				if (t.xCoord == x && t.yCoord == y && t.zCoord == z) {
					signsFound.add(t);
				}
			}
			return signsFound.toArray(new TileEntitySign[signsFound.size()]);
		}
	}

	// /**
	// * Returns either the latest sign loaded in a given position, or null if
	// * none are recorded. BUGGY WITH 1.3 BLANK SIGN FIXES/HACKS
	// *
	// * @param x
	// * @param y
	// * @param z
	// * @return
	// */
	// public static TileEntitySign getLatestSignForPos(int x, int y, int z) {
	// TileEntitySign[] signs = getSignsForPos(x, y, z);
	// if (signs.length >= 1) {
	// return signs[signs.length - 1];
	// } else {
	// return null;
	// }
	// }

	/**
	 * Returns the non-blank signs that have been loaded in a particular block,
	 * in chronological order. Only the last copy of a particular duplicate sign
	 * text is kept.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param ignoreBlanks
	 *            Whether to return the last blank tile entity of this location
	 *            at the approprite index.
	 * @return
	 */
	public static TileEntitySign[] getUniqueSignsForPos(int x, int y, int z,
			boolean ignoreBlanks) {
		TileEntitySign[] rawForPos = getSignsForPos(x, y, z);
		LinkedList<TileEntitySign> filtered = new LinkedList<TileEntitySign>();
		// Filter duplicates
		for (int i = rawForPos.length - 1; i >= 0; i--) {
			// Working from end backwards
			// Insert new texts at the head of the list
			String[] consideredText = rawForPos[i].signText;

			if (ignoreBlanks) {
				// Ignore any empty texts right off
				int emptyLines = 0;
				for (int line = 0; line < consideredText.length; line++) {
					if (consideredText[line].length() <= 0) {
						emptyLines++;
					}
				}
				if (emptyLines >= consideredText.length) {
					// Text is empty; ignore it
					continue;
				}
			}

			// TODO: Confused mess down there. Need a flow chart and clear head.
			boolean isDuplicate = false;
			for (TileEntitySign comparisonEntity : filtered) {
				String[] alreadyInListText = comparisonEntity.signText;
				int sameLines = 0;
				for (int line = 0; line < alreadyInListText.length; line++) {
					if (consideredText[line].equals(alreadyInListText[line])) {
						sameLines++;
					}
				}
				if (sameLines >= alreadyInListText.length) {
					isDuplicate = true;
					break;
				}
			}
			if (!isDuplicate) {
				filtered.push(rawForPos[i]);
			}
		}
		return filtered.toArray(new TileEntitySign[filtered.size()]);
	}

	/**
	 * See addDiscoFloor
	 * 
	 * @param discoFloors
	 */
	public static void addDiscoFloors(LinkedList<DiscoFloor> discoFloors) {
		for (DiscoFloor d : discoFloors) {
			addDiscoFloor(d);
		}
	}

	/**
	 * Sets up a disco floor to start working for a ditty. After the floor is
	 * measured in a seperate thread, it will be added to the list of disco
	 * floors to blink to ditties.
	 * 
	 * @param d
	 */
	public static void addDiscoFloor(final DiscoFloor d) {
		// It is here that a disco floor is measured and readied to be used.
		MeasureDiscoFloorThread t = new MeasureDiscoFloorThread(d,
				Minecraft.getMinecraft().theWorld);
		t.addDiscoFloorDoneListener(new DiscoFloorDoneListener() {
			public void discoFloorDoneMeasuring() {
				synchronized (endedDitties) {
					if (!endedDitties.contains(d.getDittyID())) {
						discoFloors.add(d);
						// System.out.println("Added disco floor to list");
					} else {
						// System.out.println("Disco floor too late");
					}
				}
			}
		});
		t.start();
		// System.out.println("Started measuring disco floor");
	}

	public static LinkedList<Comment> getCommentsSortedByDistFrom(
			final Point3D point3d) {
		LinkedList<Comment> list = (LinkedList<Comment>) comments.clone();
		Collections.sort(list, new Comparator<Comment>() {
			@Override
			public int compare(Comment c1, Comment c2) {
				double d = c1.getLocation().distanceToRel(point3d)
						- c2.getLocation().distanceToRel(point3d);
				if (d > 0) {
					return 1;
				} else if (d < 0) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		return list;
	}

	/**
	 * 
	 * @return all sequencers created merely to play midi files.
	 */
	public static LinkedList<Sequencer> getPlayMidiSequencers() {
		return playMidiSequencers;
	}

	/**
	 * As in picked with a pickaxe.
	 * 
	 * @return
	 */
	public static LinkedList<TileEntitySign> getPickedSigns() {
		return pickedSigns;
	}

	/**
	 * Calls Thread.sleep and automatically catches any exceptions, doing
	 * nothing.
	 * 
	 * Use where you would do this anyway, and want cleaner code.
	 * 
	 * @param ms
	 *            time to sleep in 1/1000ths of a sec.
	 */
	public static void caughtSleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Request for a particle to be emitted during the next tick.
	 * 
	 * @param r
	 */
	public static void requestParticle(ParticleRequest r) {
		particleRequestQueue.add(r);
	}

	private static final GuiMCDittyBookImportButton BOOKGUI_IMPORT_BUTTON = new GuiMCDittyBookImportButton();
	private static final GuiMCDittyBookExportButton BOOKGUI_EXPORT_BUTTON = new GuiMCDittyBookExportButton();

	/**
	 * Adds custom MCDitty buttons to the Book Editor.
	 */
	public static void updateBookEditorControls(GuiScreenBook gui,
			List controlList) {
		if (!controlList.contains(BOOKGUI_IMPORT_BUTTON)) {
			BOOKGUI_IMPORT_BUTTON.setBookGui(gui);
			controlList.add(BOOKGUI_IMPORT_BUTTON);
			BOOKGUI_EXPORT_BUTTON.setBookGui(gui);
			controlList.add(BOOKGUI_EXPORT_BUTTON);
		}
	}

	public static void showNoteblockTooltip(TileEntityNote noteTile) {
		// Create tooltip
		if (MCDittyConfig.getBoolean("enableNoteblockTooltips")) {
			noteTile.getWorldObj().spawnEntityInWorld(
					new EntityNoteBlockTooltip(noteTile));
		}
	}

	public static void updateResources() {
		UpdateResourcesThread t = new UpdateResourcesThread();
		t.start();
	}
	
	public static void registerSoundResources() {
		registerSoundResources(MCDittyConfig.resourcesDir, "");
	}

	private static void registerSoundResources(File resourcesDir, String prefix) {
		// Iterate through resources directory files, and register any .ogg
		// sounds
		if (resourcesDir != null && resourcesDir.exists()
				&& resourcesDir.listFiles() != null) {
			for (File f : resourcesDir.listFiles()) {
				if (f.getName().endsWith(".ogg")) {
					// Register as a sound
					String soundName = prefix+f.getName();
					//System.out.println ("Registering sound effect: "+soundName);
					Minecraft.getMinecraft().sndManager.addSound(soundName, f);
				} else if (f.isDirectory()) {
					registerSoundResources(f, f.getName()+"/");
				}
			}
		}
//
//		// Test
//		GetMinecraft.instance().sndManager.playSoundFX("note.harmonica", 1f, 1f);
	}
	
	public static void setUpSynthPool() {
		if (synthPool == null) {
			synthPool = new MIDISynthPool();
			synthPool.start();
		}
	}
	
	public static MIDISynthPool getSynthPool (){
		return synthPool;
	}
}
