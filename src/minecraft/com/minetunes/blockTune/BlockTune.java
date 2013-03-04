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
package com.minetunes.blockTune;

import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.BiomeGenBase;
import net.minecraft.src.Block;
import net.minecraft.src.GuiMainMenu;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityNote;
import net.minecraft.src.TileEntityRecordPlayer;
import net.minecraft.src.TileEntitySign;
import net.minecraft.src.Vec3;
import net.minecraft.src.World;
import net.minecraft.src.WorldClient;

import org.jfugue.elements.Note;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.TweenUtils;
import aurelienribon.tweenengine.equations.Sine;

import com.minetunes.Minetunes;
import com.minetunes.Point3D;
import com.minetunes.resources.ResourceManager;

/**
 * @author William
 * 
 */
public class BlockTune implements BlockTuneAccess {
	private Point3D nodePoint = null;

	private NodeState state = NodeState.ACTIVE;
	private CornerSet corners = null;

	private AxisAlignedBB bounds = null;

	private HashMap<Point3D, EntityItemDisplay> blockDisplays = new HashMap<Point3D, EntityItemDisplay>();

	private Scale scale = Scale.PENTATONIC_MAJOR.clone();

	private static Random rand = new Random();

	private BlockTunePlayer player = new BlockTunePlayer(this,
			Minetunes.getSynthPool());

	private int updateCount = 0;

	private World world = null;

	private LinkedList<PreciseParticleRequest> particleRequests = new LinkedList<BlockTune.PreciseParticleRequest>();

	private BiomeGenBase biome;

	private double beatsPerSecond = 8;

	private boolean wasRainingBefore = false;

	private int[] instrumentTranspositions = new int[4];

	/**
	 * Set up tween accessor
	 */
	static {
		Tween.registerAccessor(BlockTune.class, new BlockTuneTweenAccessor());
	}

	/**
	 * @param tile
	 */
	public BlockTune(TileEntity tile) {
		// Set up world
		world = tile.getWorldObj();

		//
		nodePoint = Point3D.getTileEntityPos(tile);
		corners = findCorners(nodePoint, tile.getWorldObj());
		if (corners == null) {
			state = NodeState.REMOVED;
		} else {
			playSoundFromCorners("tile.piston.in", 1.0f, 1.0f);
		}

		// Set up scale
		// Base note ranges over 1 octave for y=0-255, with D4 at y=64
		scale.setBaseNote((int) (Note.createNote("D4").getValue() + ((((double) nodePoint.y - 64) * (12d / 256d)))));

		// Set up bouding box
		// Point3D bbCorner1 = corners.getInteriorPoint(-5, -5);
		// Point3D bbCorner2 = corners
		// .getInteriorPoint(corners.getInteriorWidth() + 4,
		// corners.getInteriorHeight() + 4);
		Point3D bbCorner1 = corners.startCorner;
		Point3D bbCorner2 = corners.farCorner;
		bounds = AxisAlignedBB.getBoundingBox(bbCorner1.x, bbCorner1.y,
				bbCorner1.z, bbCorner2.x, bbCorner2.y + 3, bbCorner2.z);

		// Set up block displays
		updateBlockDisplays(world);

		// Start player
		player.start();

		// Set up instruments
		setUpInstruments();
	}

	/**
	 * 
	 */
	private void setUpInstruments() {
		biome = world.getBiomeGenForCoords(nodePoint.x, nodePoint.z);
		if (biome == BiomeGenBase.mushroomIsland
				|| biome == BiomeGenBase.mushroomIslandShore) {
			for (int i = 0; i < 4; i++) {
				// Random leads
				player.setInstrument(i, rand.nextInt(8) + 80);
				instrumentTranspositions[i] = 12 * (rand.nextInt(5) - 1);
			}
		} else {
			try {
				Properties biomeInstruments = new Properties();
				biomeInstruments.load(new ByteArrayInputStream(ResourceManager
						.loadCached("blockTune/biomeInstruments.txt")
						.getBytes()));
				String instrumentsCSV = biomeInstruments.getProperty("biome"
						+ biome.biomeID);
				if (instrumentsCSV == null || instrumentsCSV.length() <= 0) {
					setUpDefaultInstruments();
				} else {
					String[] instruments = instrumentsCSV.split(",");
					for (int i = 0; i < 4; i++) {
						int instrumentNum = Integer.parseInt(instruments[i]);
						player.setInstrument(i, instrumentNum);
						instrumentTranspositions[i] = getDefaultTransposition(instrumentNum);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Sets up a basic, defaultish set of instruments for when a biome is
	 * unlisted in the instruments config file.
	 */
	private void setUpDefaultInstruments() {
		Properties biomeInstruments = new Properties();
		try {
			biomeInstruments.load(new ByteArrayInputStream(ResourceManager
					.loadCached("blockTune/biomeInstruments.txt").getBytes()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		String instrumentsCSV = biomeInstruments.getProperty("default");
		if (instrumentsCSV == null || instrumentsCSV.length() <= 0) {
			return;
		} else {
			String[] instruments = instrumentsCSV.split(",");
			for (int i = 0; i < 4; i++) {
				int instrumentNum = Integer.parseInt(instruments[i]);
				player.setInstrument(i, instrumentNum);
				instrumentTranspositions[i] = getDefaultTransposition(instrumentNum);
			}
		}
	}

	/**
	 * Returns the transposition in semitones from C4 (middle C - 1 octave)
	 * required to make the given instrument sound decent playing across 3
	 * octaves upwards from C4. After three octaves, I figure you're just asking
	 * for it.
	 * 
	 * @param instrumentNum
	 * @return
	 */
	private int getDefaultTransposition(int instrumentNum) {
		switch (instrumentNum) {
		// TODO: Temp transpositions until I finish complete list
		case 22:
			return -24;
		case 114:
			return 12;
		case 58:
			// Tuba
			return -24;
		case 60:
			// French horn
			return -12;
		case 79:
			// ocarina
			return 24;

			// GM Level 1 Instrument Families
			// 0-7: Piano
			// 8-15: Chromatic Percussion
			// Glockenspiel: F6-C9
		case 9:
			return 24;
			// Music box
		case 10:
			return 12;
			// 16-23: Organ
			// TODO: Classes after
		default:
			return 0;
		}
	}

	public void update(WorldClient world) {
		// Save time by not checking anything on removed nodes
		if (state == NodeState.REMOVED) {
			return;
		}

		// Ensure that the node's block exists
		if (getNodeTileEntity(world) == null) {
			prepareForRemoval();

			// Play sound
			playSoundFromCorners("mob.irongolem.death", 1.0f, 1.0f);

			return;
		}

		// Once in a while, check to ensure that the smallest possible structure
		// is being used
		if (updateCount % 5 == 0) {
			CornerSet newestFoundCorners = findCorners(nodePoint, world);
			if (newestFoundCorners == null
					|| !newestFoundCorners.equals(corners)) {
				prepareForRemoval();
				playSoundFromCorners("tile.piston.out", 1.0f, 1.0f);
				return;
			}
		}

		// Make sure that the tune's structure exists
		if (!checkExistingStructure(world)) {
			prepareForRemoval();

			// Play sound
			playSoundFromCorners("mob.irongolem.death", 1.0f, 1.0f);
			return;
		}

		// Update the floating blocks above each corner
		updateBlockDisplays(world);

		// Check that the correct scale for the time of day is being used
		// Major for day, minor for night
		if (world.getWorldTime() % 24000 > 12000) {
			// Night time
			if (scale.getSteps() == scale.PENTATONIC_STEPS
					&& scale.getMode() == 0) {
				scale.setMode(4);
				Tween.to(this, BlockTuneTweenAccessor.TWEEN_TYPE_TEMPO,
						10 * 1000).target((float) (beatsPerSecond * 0.6f))
						.ease(Sine.INOUT).start(BlockTuneManager.manager);
			}
		} else {
			// Day time
			if (scale.getSteps() == scale.PENTATONIC_STEPS
					&& scale.getMode() == 4) {
				scale.setMode(0);
				Tween.to(this, BlockTuneTweenAccessor.TWEEN_TYPE_TEMPO,
						10 * 1000)
						.target((float) (beatsPerSecond * (1f / 0.6f)))
						.ease(Sine.INOUT).start(BlockTuneManager.manager);
			}
		}

		// Adjust for weather
		if (world.getRainStrength(0) > 0.5 && !wasRainingBefore) {
			wasRainingBefore = true;
			// Raining
			// Move key down 6 semitones
			Tween.to(this, BlockTuneTweenAccessor.TWEEN_TYPE_BASE_NOTE,
					5 * 1000).target(scale.getBaseNote() - 6).ease(Sine.IN)
					.start(BlockTuneManager.manager);
		} else if (world.getRainStrength(0) <= 0.5 && wasRainingBefore) {
			wasRainingBefore = false;
			// Not raining
			// Raining
			// Move key up 6 semitones
			Tween.to(this, BlockTuneTweenAccessor.TWEEN_TYPE_BASE_NOTE,
					5 * 1000).target(scale.getBaseNote() + 6).ease(Sine.IN)
					.start(BlockTuneManager.manager);
		}

		if (updateCount % 2 == 0) {
			// Pulse border particles
			if (Minecraft.getMinecraft().gameSettings.fancyGraphics) {
				LinkedList<Point3D> particlePoints = corners
						.getRandomBorderBlocks(0.1f);
				for (Point3D p : particlePoints) {
					world.spawnParticle("reddust", p.x + 0.5, p.y + 1,
							p.z + 0.5, 0, 0.02, 0);
				}
			}
		}

		// Update text on adjacent sign
		if (Point3D.getNumAdjacent(world, Block.signWall.blockID, nodePoint) > 0) {
			TileEntitySign tileEntitySign = getAdjacentSignTileEntity(nodePoint);
			if (tileEntitySign != null) {
				if (updateCount % 200 == 0) {
					clearSign(tileEntitySign);
					tileEntitySign.signText[0] = "Lowest Note: ";
					tileEntitySign.signText[1] = new Note(
							(byte) scale.getBaseNote()).getMusicString();
					if (tileEntitySign.signText[1].length() >= 2) {
						tileEntitySign.signText[1] = tileEntitySign.signText[1]
								.substring(0,
										tileEntitySign.signText[1].length() - 1);
					}
				} else if (updateCount % 200 == 50) {
					clearSign(tileEntitySign);
					tileEntitySign.signText[0] = "Scale:";
					if (scale.getNumSteps() == 5) {
						tileEntitySign.signText[1] = "Pentatonic";
					} else if (scale.getNumSteps() == 7) {
						tileEntitySign.signText[1] = "Diatonic";
					} else if (scale.getNumSteps() == 12) {
						tileEntitySign.signText[1] = "Chromatic";
					} else {
						tileEntitySign.signText[1] = "?";
					}
				} else if (updateCount % 200 == 100) {
					clearSign(tileEntitySign);
					tileEntitySign.signText[0] = "Mode:";
					if (scale.getMode() == 0) {
						tileEntitySign.signText[1] = "Major";
					} else if ((scale.getNumSteps() == 5)
							&& (scale.getMode() == 4)) {
						tileEntitySign.signText[1] = "Minor";
					} else if ((scale.getNumSteps() == 7)
							&& (scale.getMode() == 5)) {
						tileEntitySign.signText[1] = "Minor";
					} else {
						tileEntitySign.signText[1] = scale.getMode()
								+ " Semitones";
					}
				} else if (updateCount % 200 == 150) {
					clearSign(tileEntitySign);
					tileEntitySign.signText[0] = "FloorTune";
					tileEntitySign.signText[2] = "Length:";
					tileEntitySign.signText[3] = Integer
							.toString(getFrameCount());
				}
			}
		}

		// Increment update count
		updateCount++;
	}

	/**
	 * 
	 * @param tileEntitySign
	 * @return the array containing the sign's text, where changes will be
	 *         reflected on the sign
	 */
	public static String[] clearSign(TileEntitySign tileEntitySign) {
		String[] blankSignText = { "", "", "", "" };
		tileEntitySign.signText = blankSignText;
		return tileEntitySign.signText;
	}

	private TileEntitySign getAdjacentSignTileEntity(Point3D point) {
		for (Point3D p : Point3D.getAdjacentBlocks(point)) {
			TileEntity t = world.getBlockTileEntity(p.x, p.y, p.z);
			if (t != null && t instanceof TileEntitySign) {
				return (TileEntitySign) t;
			}
		}
		return null;
	}

	/**
	 * 
	 */
	private void flushParticleRequests() {
		for (PreciseParticleRequest p : particleRequests) {
			world.spawnParticle(p.type, p.location.xCoord, p.location.yCoord,
					p.location.zCoord, p.velocity.xCoord, p.velocity.yCoord,
					p.velocity.zCoord);
		}
		particleRequests.clear();
	}

	public static boolean isTileEntityNode(TileEntity block, WorldClient world) {
		if (block instanceof TileEntityRecordPlayer) {
			if (searchForStructure(world, block)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return
	 */
	public boolean isRemoved() {
		return state == NodeState.REMOVED;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BlockTune) {
			BlockTune node = (BlockTune) obj;
			if (nodePoint.equals(node.nodePoint)) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public Point3D getNodePoint() {
		return nodePoint;
	}

	/**
	 * 
	 */
	private void updateBlockDisplays(World world) {
		if (blockDisplays.size() <= 0) {
			for (Point3D p : corners.getCorners()) {
				// Set up block displays
				EntityItemDisplay item = new EntityItemDisplay(world,
						p.x + 0.5, p.y + 1.4, p.z + 0.5,
						getItemstackForWorldBlock(world, p.x, p.y - 1, p.z));
				world.loadedEntityList.add(item);
				blockDisplays.put(p, item);
			}
		} else {
			// Update block displays
			for (Point3D p : corners.getCorners()) {
				EntityItemDisplay item = blockDisplays.get(p);
				// Set itemstack
				ItemStack itemStack = getItemstackForWorldBlock(world, p.x,
						p.y - 1, p.z);
				item.func_92058_a(itemStack);
				// Set spin speed
				if (isAdjacentSwitchOn(world, corners.startCorner)) {
					item.setAgeMultiplier(1);
				} else {
					item.setAgeMultiplier(0);
				}
			}
		}
	}

	/**
	 * @param world
	 * @param x
	 * @param i
	 * @param z
	 * @return
	 */
	private ItemStack getItemstackForWorldBlock(World world, int x, int y, int z) {
		return new ItemStack(world.getBlockId(x, y, z), 0,
				world.getBlockMetadata(x, y, z));
	}

	private void spawnNoteParticleAbove(Point3D location, float color,
			float variance, World world) {
		float offset = ((1f - variance) / 2f);
		world.spawnParticle("note", (float) location.x + offset
				+ (variance * rand.nextFloat()), (float) location.y + 1f,
				(float) location.z + offset + (variance * rand.nextFloat()),
				color, 0, 0);
	}

	/**
	 * Whether a switch adjacent to the given corner is turned on.
	 * 
	 * @param world
	 * @param cornerNum
	 * @return
	 */
	private boolean isAdjacentSwitchOn(World world, int cornerNum) {
		Point3D corner = corners.getCorners().get(cornerNum);
		return isAdjacentSwitchOn(world, corner);
	}

	/**
	 * Whether a switch adjacent to the given block is turned on.
	 * 
	 * @param world
	 * @param block
	 * @return
	 */
	private boolean isAdjacentSwitchOn(World world, Point3D block) {
		for (Point3D p : Point3D.getAdjacentBlocks(block)) {
			if (world.getBlockId(p.x, p.y, p.z) == Block.lever.blockID) {
				if ((world.getBlockMetadata(p.x, p.y, p.z) & 0x8) == 0x8) {
					// Lever is on
					return true;
				}
			}
		}
		return false;
	}

	private void prepareForRemoval() {
		state = NodeState.REMOVED;

		for (EntityItemDisplay e : blockDisplays.values()) {
			e.setDead();
		}

		player.close();
	}

	private boolean checkExistingStructure(World world) {
		if (Point3D.getNumAdjacent(world, Block.lever.blockID, nodePoint) < 1) {
			return false;
		}

		if (world.getBlockId(corners.startCorner.x, corners.startCorner.y,
				corners.startCorner.z) != Block.jukebox.blockID) {
			return false;
		}

		for (Point3D cornerPoint : corners.getCornersExceptStart()) {
			if (world.getBlockId(cornerPoint.x, cornerPoint.y, cornerPoint.z) != Block.music.blockID) {
				return false;
			}
		}

		return true;
	}

	private void playSoundFromCorners(String sound, float volume, float pitch) {
		// 20 is arbitrary; 16 is range of sounds in MC (?)
		if (corners.getInteriorHeight() > 20 || corners.getInteriorWidth() > 20) {
			for (Point3D point : corners.getCorners()) {
				Minecraft.getMinecraft().sndManager
						.playSound(
								sound,
								point.x + 0.5f,
								point.y + 0.5f,
								point.z + 0.5f,
								volume
										* Minecraft.getMinecraft().gameSettings.soundVolume,
								pitch);
			}
		} else {
			// small enough that we can get away just playing one sound
			Point3D point = getNodePoint();
			Minecraft.getMinecraft().sndManager.playSound(sound,
					point.x + 0.5f, point.y + 0.5f, point.z + 0.5f,
					volume * Minecraft.getMinecraft().gameSettings.soundVolume,
					pitch);
		}
	}

	private TileEntityRecordPlayer getNodeTileEntity(World world) {
		if (nodePoint == null) {
			return null;
		} else {
			TileEntity blockEntity = world.getBlockTileEntity(nodePoint.x,
					nodePoint.y, nodePoint.z);
			if (blockEntity instanceof TileEntityRecordPlayer) {
				return (TileEntityRecordPlayer) blockEntity;
			} else {
				return null;
			}
		}
	}

	private static boolean searchForStructure(WorldClient world,
			TileEntity nodeTileEntity) {
		int x = nodeTileEntity.xCoord;
		int y = nodeTileEntity.yCoord;
		int z = nodeTileEntity.zCoord;
		Point3D nodePoint = new Point3D(x, y, z);
		return searchForStructure(world, nodePoint);
	}

	/**
	 * Checks for a lever on the jukebox and two redstone wires leading out.
	 * 
	 * If those check out, tries to validate redstone loop around the playing
	 * field.
	 * 
	 * @param world
	 * @param block
	 * @return
	 */
	private static boolean searchForStructure(WorldClient world,
			Point3D nodePoint) {
		// Check that the basic components are attached before verifying the
		// corners
		int leversFound = Point3D.getNumAdjacent(world, Block.lever.blockID,
				nodePoint);
		if (leversFound <= 0) {
			return false;
		}

		// Verify perimeter
		boolean perimeterValid = findCorners(nodePoint, world) != null;
		if (perimeterValid) {
			world.spawnParticle("note", nodePoint.x, nodePoint.y + 1,
					nodePoint.z, 0, 0, 0);
		}
		return perimeterValid;
	}

	/**
	 * Checks for three noteblocks positioned in a horizontal rectangle, with
	 * startPoint as one corner
	 * 
	 * @param startPoint
	 * @param world
	 * @return
	 */
	private static CornerSet findCorners(Point3D startPoint, World world) {
		// Find all noteblocks on same y-level as startPoint
		LinkedList<TileEntityNote> candidates = new LinkedList<TileEntityNote>();
		for (Object o : world.loadedTileEntityList) {
			if (o instanceof TileEntityNote) {
				// Is a noteblock...
				TileEntityNote e = (TileEntityNote) o;
				if (e.yCoord == startPoint.y) {
					candidates.add(e);
				}
			}
		}

		// Now check for noteblocks on the same x & z axis to find two corners
		// For each possible pair, look for a third noteblock in the third
		// corner as well

		// Look for x & z corner candidates
		LinkedList<TileEntityNote> xCandidates = new LinkedList<TileEntityNote>();
		LinkedList<TileEntityNote> zCandidates = new LinkedList<TileEntityNote>();
		for (TileEntityNote e : candidates) {
			if (e.xCoord == startPoint.x) {
				xCandidates.add(e);
			}

			if (e.zCoord == startPoint.z) {
				zCandidates.add(e);
			}
		}

		if (xCandidates.size() <= 0 || zCandidates.size() <= 0) {
			// Not enough potential corners found
			return null;
		}

		// Look for pairs of x & z corners that have a third corner as well
		LinkedList<CornerSet> goodPairs = new LinkedList<CornerSet>();
		for (TileEntityNote xCorner : xCandidates) {
			for (TileEntityNote zCorner : zCandidates) {
				TileEntity farCornerTileEntity = world.getBlockTileEntity(
						zCorner.xCoord, startPoint.y, xCorner.zCoord);
				if (farCornerTileEntity != null
						&& farCornerTileEntity instanceof TileEntityNote) {
					// Score!
					goodPairs
							.add(new CornerSet(Point3D
									.getTileEntityPos(xCorner), Point3D
									.getTileEntityPos(zCorner), Point3D
									.getTileEntityPos(farCornerTileEntity),
									startPoint));
				}
			}
		}

		// Check that a good set of corners has been found
		if (goodPairs.size() <= 0) {
			return null;
		} else if (goodPairs.size() == 1) {
			return goodPairs.getFirst();
		} else {
			// Multiple found. Have to choose smallest.
			CornerSet smallestArea = goodPairs.getFirst();
			for (CornerSet cs : goodPairs) {
				if (cs.getAreaInside() < smallestArea.getAreaInside()) {
					smallestArea = cs;
				}
			}
			return smallestArea;
		}
	}

	@Override
	public int getFrameCount() {
		return corners.getInteriorWidth();
	}

	@Override
	public boolean isPaused() {
		try {
			if (!isAdjacentSwitchOn(world, 0)) {
				return true;
			} else if (Minecraft.getMinecraft().isGamePaused) {
				return true;
			} else if (Minecraft.getMinecraft().currentScreen instanceof GuiMainMenu) {
				// In fact, if we're in the main menu...
				prepareForRemoval();

				// Then return
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Check for player distance
		if (getPlayerDistance() > 32 * 32) {
			return true;
		}

		return false;
	}

	@Override
	public boolean isLooping() {
		return true;
	}

	@Override
	public double getMasterVolume() {
		double base = (Minetunes.getMinecraftAdjustedSixteenBitVolume(100) >> 7) / 127d;

		// Find distance from closest corner to player
		double shortestDistance = getPlayerDistance();

		// Factor into master volume
		// If player is within 16 blocks, make no change
		// Decrease linearly for the following 16 blocks away
		if (shortestDistance <= 16 * 16) {
			return base;
		} else {
			return (1 - ((shortestDistance - 16d * 16d) / (16d * 16d))) * base;
		}
	}

	/**
	 * Value is squared; actual distance in blocks is the square root of the
	 * result of this method
	 * 
	 * @return
	 */
	private double getPlayerDistance() {
		Point3D playerPoint = new Point3D(
				(int) Minecraft.getMinecraft().thePlayer.posX,
				(int) Minecraft.getMinecraft().thePlayer.posY,
				(int) Minecraft.getMinecraft().thePlayer.posZ);

		double shortestDistance = Double.MAX_VALUE;
		try {

			for (Point3D corner : corners.getCorners()) {
				double cornerDist = corner.distanceToRel(playerPoint);
				if (cornerDist < shortestDistance) {
					shortestDistance = cornerDist;
				}
			}
		} catch (Exception e) {
			shortestDistance = 89098909292d;
		}
		return shortestDistance;
	}

	@Override
	public Frame getFrame(int frameNum) {
		Frame frame = new Frame(1);

		// Climb upwards through the column
		for (int y = 0; y < corners.getInteriorHeight(); y++) {
			// Get point referenced
			Point3D readPoint = corners.getInteriorPoint(frameNum, y);

			int pointID = world.getBlockId(readPoint.x, readPoint.y,
					readPoint.z);
			if (pointID != 0) {
				int pointMeta = world.getBlockMetadata(readPoint.x,
						readPoint.y, readPoint.z);
				int cornerNote = -1;
				LinkedList<Point3D> cornersList = corners.getCorners();
				for (int j = 0; j < cornersList.size(); j++) {
					Point3D corner = cornersList.get(j);
					ItemStack cornerItem = blockDisplays.get(corner)
							.func_92059_d();
					if (cornerItem == null) {
						continue;
					}

					if (blockAreSimilar(cornerItem, pointID, pointMeta)) {
						// Switches on corners are redundant: can break
						// block below!
						// if ((getNumAdjacent(world, Block.lever.blockID,
						// corner) <= 0)
						// || isAdjacentSwitchOn(world, j)) {
						cornerNote = j;
						// }
						break;
					}
				}

				if (cornerNote >= 0) {
					byte noteValue = (byte) scale.getNoteForStep(y);
					noteValue += instrumentTranspositions[cornerNote];
					frame.addNoteStart(cornerNote, noteValue);

					// Queue up particles
					spawnNoteParticleAbove(readPoint,
							(float) cornerNote * 0.2f, 0.2f, world);
					spawnNoteParticleAbove(cornersList.get(cornerNote),
							(float) cornerNote * 0.2f, 1f, world);
				}
			}
		}

		return frame;
	}

	/**
	 * Block IDs that cannot be judged as the same block (for purposes of
	 * instrument blocks) by their block ID alone. MUST BE SORTED.
	 */
	private static final int[] idsWhereMetaMatters = { 6, 17, 18, 34, 35, 43,
			44, 59, 69, 70, 72, 78, 84, 92, 93, 94, 97, 98, 104, 105, 107, 115,
			118, 120, 125, 126, 127, 139, 140, 141, 142, 147, 148, 155 };

	/**
	 * Like idsWhereMetaMatters except that there is nothing special about
	 * comparing the meta values: if they're different, the blocks are
	 * different, period. Contrast with saplings (2 bits specify types, 1 is
	 * just a counter for their growth that should be ignored) and several other
	 * blocks. MUST BE SORTED.
	 */
	private static final int[] idsWhereMetaMattersStrictly = { 24, 35, 43, 59,
			70, 72, 78, 84, 92, 97, 98, 104, 105, 115, 118, 125, 139, 140, 141,
			142, 147, 148, 155 };

	private static final HashMap<Integer, Integer> metaCompareMasks = new HashMap<Integer, Integer>();
	static {
		// 6, 17, 18, 44, 69, 93, 94, 107, 120, 126, 127

		// Only care about first two bits
		metaCompareMasks.put(6, 0x3);
		metaCompareMasks.put(17, 0x3);
		metaCompareMasks.put(18, 0x3);

		// Only care about first three bits
		metaCompareMasks.put(44, 0x7);
		metaCompareMasks.put(126, 0x7);

		// Only care about last bit
		metaCompareMasks.put(69, 0x8);

		// Only care about top two bits
		metaCompareMasks.put(93, 0xC);
		metaCompareMasks.put(94, 0xC);
		metaCompareMasks.put(127, 0xC);

		// Only care about third bit
		metaCompareMasks.put(107, 0x4);
		metaCompareMasks.put(120, 0x4);
	}

	/**
	 * @param cornerItem
	 * @param pointID
	 * @param pointMeta
	 * @return
	 */
	private boolean blockAreSimilar(ItemStack cornerItem, int pointID,
			int pointMeta) {
		// If ids are dissimilar, then duh, the blocks are different
		if (cornerItem.itemID != pointID) {
			return false;
		}

		if (contains(idsWhereMetaMatters, pointID)) {
			// Meta matters; can we get away with a meta == meta?
			if (contains(idsWhereMetaMattersStrictly, pointID)) {
				// Yes!
				return cornerItem.getItemDamage() == pointMeta;
			} else {
				// No, there's a special comparison
				Integer mask = metaCompareMasks.get(pointID);
				if (mask == null) {
					mask = 0xf;
				}
				// Mask metadata values and compare remaining bits
				if ((cornerItem.getItemDamage() & mask) == (pointMeta & mask)) {
					return true;
				} else {
					return false;
				}
			}
		} else {
			// Just compare ids (was done already at top of method)
			return true;
		}
	}

	/**
	 * True if passed array contains v
	 * 
	 * @param array
	 * @param v
	 * @return
	 */
	public static boolean contains(int[] array, int v) {
		for (int i : array) {
			if (i == v) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onFramePlayed(Frame frame, int frameNum) {
		// Show queued up particles
		flushParticleRequests();
	}

	private static class CornerSet {
		public Point3D xCorner;
		public Point3D zCorner;
		public Point3D farCorner;
		public Point3D startCorner;

		public CornerSet(Point3D x, Point3D z, Point3D far, Point3D start) {
			xCorner = x;
			zCorner = z;
			farCorner = far;
			startCorner = start;
		}

		public int getAreaInside() {
			return Math.abs((xCorner.z - startCorner.z)
					* (zCorner.x - startCorner.x));
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof CornerSet)) {
				return false;
			}

			CornerSet otherSet = (CornerSet) obj;
			if (xCorner.equals(otherSet.xCorner)
					&& zCorner.equals(otherSet.zCorner)
					&& farCorner.equals(otherSet.farCorner)
					&& startCorner.equals(otherSet.startCorner)) {
				return true;
			} else {
				return false;
			}
		}

		/**
		 * Gets corners, going clockwise
		 * 
		 * @return
		 */
		public LinkedList<Point3D> getCorners() {
			LinkedList<Point3D> cornerList = new LinkedList<Point3D>();
			cornerList.add(startCorner);
			if (isXBottomAxis()) {
				cornerList.add(xCorner);
			} else {
				cornerList.add(zCorner);
			}
			cornerList.add(farCorner);
			if (isXBottomAxis()) {
				cornerList.add(zCorner);
			} else {
				cornerList.add(xCorner);
			}
			return cornerList;
		}

		public LinkedList<Point3D> getCornersExceptStart() {
			LinkedList<Point3D> cornerList = new LinkedList<Point3D>();
			if (isXBottomAxis()) {
				cornerList.add(xCorner);
			} else {
				cornerList.add(zCorner);
			}
			cornerList.add(farCorner);
			if (isXBottomAxis()) {
				cornerList.add(zCorner);
			} else {
				cornerList.add(xCorner);
			}
			return cornerList;
		}

		/**
		 * Given that "startPoint" is the lower-lefthand corner of a square
		 * drawn on the ground, find the "bottom" axis of the square.
		 */
		public boolean isXBottomAxis() {
			boolean xPositive = (zCorner.x - startCorner.x > 0);
			boolean zPositive = (xCorner.z - startCorner.z > 0);
			if (xPositive == zPositive) {
				return false;
			} else {
				return true;
			}
		}

		/**
		 * Get width of rectangle inside the corners
		 * 
		 * @return
		 */
		public int getInteriorWidth() {
			if (isXBottomAxis()) {
				return Math.abs(zCorner.x - startCorner.x) - 1;
			} else {
				return Math.abs(xCorner.z - startCorner.z) - 1;
			}
		}

		public int getInteriorHeight() {
			if (!isXBottomAxis()) {
				return Math.abs(zCorner.x - startCorner.x) - 1;
			} else {
				return Math.abs(xCorner.z - startCorner.z) - 1;
			}
		}

		/**
		 * Gets a point inside the corners, given a set of interior coordinates.
		 * Interior is a 2D plane with 0,0 being right inside the startCorner
		 * and getInteriorWidth()-1,getInteriorHeight()-1 being right inside the
		 * farCorner. X goes along the bottom of the area, Y moves upwards.
		 * 
		 * @param intX
		 *            not range checked
		 * @param intY
		 *            not range checked
		 * @return
		 */
		public Point3D getInteriorPoint(int intX, int intY) {
			// The Minecraft World coordinates that will be returned
			int realX = startCorner.x;
			int realZ = startCorner.z;

			if (!isXBottomAxis()) {
				// Swap interior coordinates
				int tmpX = intX;
				intX = intY;
				intY = tmpX;
			}

			if (startCorner.x < zCorner.x) {
				realX += intX + 1;
			} else {
				realX -= intX + 1;
			}

			if (startCorner.z < xCorner.z) {
				realZ += intY + 1;
			} else {
				realZ -= intY + 1;
			}

			return new Point3D(realX, startCorner.y, realZ);
		}

		private static Random rand;

		public LinkedList<Point3D> getRandomBorderBlocks(float amountPerSide) {
			// Set up random generator
			if (rand == null) {
				rand = new Random();
			}

			LinkedList<Point3D> picks = new LinkedList<Point3D>();

			// Choose from each border
			int topCount = (int) (amountPerSide * (double) getInteriorWidth());
			if (rand.nextBoolean()) {
				topCount++;
			}
			for (int i = 0; i < topCount; i++) {
				picks.add(getInteriorPoint(rand.nextInt(Math.max(0, getInteriorWidth())), -1));
				picks.add(getInteriorPoint(rand.nextInt(Math.max(0, getInteriorWidth())),
						getInteriorHeight()));
			}

			// Choose from side borders
			int count = (int) (amountPerSide * (double) getInteriorHeight());
			if (rand.nextBoolean()) {
				count++;
			}
			for (int i = 0; i < count; i++) {
				picks.add(getInteriorPoint(-1,
						rand.nextInt(getInteriorHeight())));
				picks.add(getInteriorPoint(getInteriorWidth(),
						rand.nextInt(getInteriorHeight())));
			}

			return picks;
		}
	}

	private static class PreciseParticleRequest {
		public Vec3 location;
		public Vec3 velocity;
		public String type;
	}

	@Override
	public double getBeatsPerSecond() {
		return beatsPerSecond;
	}

	/**
	 * @param f
	 */
	public void setBeatsPerSecond(float f) {
		beatsPerSecond = f;
	}

	/**
	 * @return
	 */
	public Scale getScale() {
		return scale;
	}

	public void setRemoved() {
		prepareForRemoval();
	}
}
