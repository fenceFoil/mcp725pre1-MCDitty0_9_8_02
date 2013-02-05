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

import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.src.TileEntityRecordPlayer;
import net.minecraft.src.WorldClient;

import com.wikispaces.mcditty.Point3D;
import com.wikispaces.mcditty.TickListener;

/**
 * TODO: Add auto-time-limiter that stops scanning for stuff if it takes too
 * long
 * 
 * @author William
 * 
 */
public class BlockTuneManager implements TickListener {

	private LinkedList<BlockTune> trackedNodes = new LinkedList<BlockTune>();

	int tickCounter = 0;

	@Override
	public boolean onTick(float partialTick, Minecraft minecraft) {
		if (tickCounter % 5 == 0) {
			scanForNodes(minecraft.theWorld);
		}
		
		LinkedList<BlockTune> removedNodes = new LinkedList<BlockTune>();
		for (BlockTune n : trackedNodes) {
			if (n.isRemoved()) {
				removedNodes.add(n);
			} else {
				n.update(minecraft.theWorld);
			}
		}
		trackedNodes.removeAll(removedNodes);

		tickCounter++;
		return true;
	}

	/**
	 * 
	 */
	private void scanForNodes(WorldClient world) {
		List l = world.loadedTileEntityList;
		for (Object o : l) {
			if (o instanceof TileEntityRecordPlayer) {
				TileEntityRecordPlayer tile = (TileEntityRecordPlayer) o;
				if (!tileEntityAlreadyNode(tile)) {
					if (BlockTune.isTileEntityNode(tile, world)) {
						BlockTune node = new BlockTune(tile);
						//if (!trackedNodes.contains(node)) {
							trackedNodes.add(node);
						//}
					}
				}
			}
		}
	}

	/**
	 * @param tile
	 * @return
	 */
	private boolean tileEntityAlreadyNode(TileEntityRecordPlayer tile) {
		Point3D tilePoint = Point3D.getTileEntityPos(tile);
		for (BlockTune n:trackedNodes) {
			if (n.getNodePoint().equals(tilePoint)) {
				return true;
			}
		}
		return false;
	}
}
