/**
 * Copyright (c) 2012-2013 William Karnavas 
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
package com.wikispaces.mcditty.blockTune;

import java.util.HashMap;
import java.util.LinkedList;

import javax.sound.midi.MidiUnavailableException;

import org.jfugue.elements.Note;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityNote;
import net.minecraft.src.TileEntityRecordPlayer;
import net.minecraft.src.World;
import net.minecraft.src.WorldClient;

import com.sun.media.sound.SoftSynthesizer;
import com.wikispaces.mcditty.MCDitty;
import com.wikispaces.mcditty.Point3D;
import com.wikispaces.mcditty.ditty.MIDISynthPool;

/**
 * @author William
 * 
 */
public class BlockTune {
	private Point3D nodePoint = null;

	private NodeState state = NodeState.ACTIVE;
	private CornerSet corners = null;

	private HashMap<Point3D, EntityItemDisplay> blockDisplays = new HashMap<Point3D, EntityItemDisplay>();

	// TODO: Inefficient
	private SoftSynthesizer mySynth = MCDitty.getSynthPool().getOpenedSynth();

	private Scale scale = new Scale();

	/**
	 * @param tile
	 */
	public BlockTune(TileEntity tile) {
		nodePoint = Point3D.getTileEntityPos(tile);
		corners = findCorners(nodePoint, tile.getWorldObj());
		if (corners == null) {
			state = NodeState.REMOVED;
		} else {
			playSound("tile.piston.in", nodePoint, 1.0f, 1.0f);
		}
	}

	/**
	 * 
	 */
	private void updateBlockDisplays(World world) {
		if (blockDisplays.size() <= 0) {
			for (Point3D p : corners.getCorners()) {
				// Set up block displays
				EntityItemDisplay item = new EntityItemDisplay(world,
						p.x + 0.5, p.y + 1.5, p.z + 0.5,
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

	private int updateCount = 0;
	private int currNoteColumn = 0;

	public void update(WorldClient world) {
		// Save time by not checking anything on removed nodes
		if (state == NodeState.REMOVED) {
			return;
		}

		// Ensure that the node's block exists
		if (getNodeTileEntity(world) == null) {
			prepareForRemoval();

			// Play sound
			playSound("mob.irongolem.death", nodePoint, 1.0f, 1.0f);

			return;
		}

		// Once in a while, check to ensure that the smallest possible structure
		// is being used
		CornerSet newestFoundCorners = findCorners(nodePoint, world);
		if (newestFoundCorners == null || !newestFoundCorners.equals(corners)) {
			prepareForRemoval();
			playSound("tile.piston.out", nodePoint, 1.0f, 1.0f);
			return;
		}

		// Check structure
		if (!checkExistingStructure(world)) {
			prepareForRemoval();

			// Play sound
			playSound("mob.irongolem.death", nodePoint, 1.0f, 1.0f);
			return;
		}

		// Update the floating blocks above each corner
		updateBlockDisplays(world);

		// Play notes if necessary
		if (updateCount % 4 == 0) {
			// Slience any previous notes
			mySynth.getChannels()[0].allNotesOff();

			System.out.println ("Reading notes...");
			// Read and play new notes
			for (int i = 0; i < corners.getInteriorHeight(); i++) {
				//System.out.println ("Reading i="+i);
				Point3D readPoint = corners.getInteriorPoint(currNoteColumn, i);
				world.spawnParticle("note", readPoint.x + 0.5f, readPoint.y+0.5f, readPoint.z+0.5f, 0.5, 0, 0);
				if (world.getBlockId(readPoint.x, readPoint.y, readPoint.z) != 0) {
					int noteValue = scale.getNoteForStep(i);
					System.out.println (Note.getStringForNote((byte) noteValue));
					mySynth.getChannels()[0].noteOn(noteValue, 127);
				}
			}

			// Increment current column
			currNoteColumn = (currNoteColumn + 1) % corners.getInteriorWidth();
		}

		// Increment update count
		updateCount++;
	}

	private void prepareForRemoval() {
		state = NodeState.REMOVED;

		for (EntityItemDisplay e : blockDisplays.values()) {
			e.setDead();
		}

		MCDitty.getSynthPool().returnUsedSynth(mySynth, null, null);
	}

	private boolean checkExistingStructure(World world) {
		if (getNumAdjacent(world, Block.lever.blockID, nodePoint) < 1) {
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

	private static void playSound(String sound, Point3D point, float volume,
			float pitch) {
		Minecraft.getMinecraft().sndManager.playSound(sound, point.x + 0.5f,
				point.y + 0.5f, point.z + 0.5f,
				volume * Minecraft.getMinecraft().gameSettings.soundVolume,
				pitch);
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
		int leversFound = getNumAdjacent(world, Block.lever.blockID, nodePoint);
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

	private static int getNumAdjacent(World world, int blockID,
			Point3D blockPoint) {
		int found = 0;
		for (Point3D p : getAdjacentBlocks(blockPoint)) {
			int id = world.getBlockId(p.x, p.y, p.z);
			if (id == blockID) {
				found++;
			}
		}
		return found;
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

		public LinkedList<Point3D> getCorners() {
			LinkedList<Point3D> cornerList = new LinkedList<Point3D>();
			cornerList.add(startCorner);
			cornerList.add(xCorner);
			cornerList.add(farCorner);
			cornerList.add(zCorner);
			return cornerList;
		}

		public LinkedList<Point3D> getCornersExceptStart() {
			LinkedList<Point3D> cornerList = new LinkedList<Point3D>();
			cornerList.add(xCorner);
			cornerList.add(farCorner);
			cornerList.add(zCorner);
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

	/**
	 * Returns the 6 points directly adjacent to a block in 3D-space. If a block
	 * is at y=0 or y=255, the returned list of points will only be 5 elements
	 * long.
	 * 
	 * @param point
	 *            if null, method returns null
	 * @return a 5 or 6 element array of Point3D
	 */
	public static Point3D[] getAdjacentBlocks(Point3D point) {
		if (point == null) {
			return null;
		}

		LinkedList<Point3D> returns = new LinkedList<Point3D>();

		// Find the x/z adjacent blocks
		returns.add(new Point3D(point.x, point.y, point.z - 1));
		returns.add(new Point3D(point.x, point.y, point.z + 1));
		returns.add(new Point3D(point.x - 1, point.y, point.z));
		returns.add(new Point3D(point.x + 1, point.y, point.z));

		// Find the y adjacent blocks
		if (point.y > 0) {
			returns.add(new Point3D(point.x, point.y - 1, point.z));
		}

		if (point.y < 255) {
			returns.add(new Point3D(point.x, point.y + 1, point.z));
		}

		return returns.toArray(new Point3D[returns.size()]);
	}

	/**
	 * Returns the 4 points directly adjacent to a block in 2D space, on the x
	 * and z coordinates.
	 * 
	 * @param point
	 *            if null, method returns null
	 * @return a 4 element array of Point3D
	 */
	public static Point3D[] getAdjacentBlocksXZ(Point3D point) {
		if (point == null) {
			return null;
		}

		LinkedList<Point3D> returns = new LinkedList<Point3D>();

		// Find the x/z adjacent blocks
		returns.add(new Point3D(point.x, point.y, point.z - 1));
		returns.add(new Point3D(point.x, point.y, point.z + 1));
		returns.add(new Point3D(point.x - 1, point.y, point.z));
		returns.add(new Point3D(point.x + 1, point.y, point.z));

		return returns.toArray(new Point3D[returns.size()]);
	}

	public Point3D getNodePoint() {
		return nodePoint;
	}
}
