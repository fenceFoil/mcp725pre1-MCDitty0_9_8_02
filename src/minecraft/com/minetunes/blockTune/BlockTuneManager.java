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

import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.src.TileEntityRecordPlayer;
import net.minecraft.src.WorldClient;
import aurelienribon.tweenengine.TweenManager;

import com.minetunes.Point3D;
import com.minetunes.TickListener;
import com.minetunes.config.MinetunesConfig;

/**
 * TODO: Add auto-time-limiter that stops scanning for stuff if it takes too
 * long
 * 
 * @author William
 * 
 */
public class BlockTuneManager implements TickListener {

	private LinkedList<BlockTune> trackedNodes = new LinkedList<BlockTune>();

	public static TweenManager manager = new TweenManager();

	int tickCounter = 0;

	private long lastUpdateMillis = System.currentTimeMillis();

	@Override
	public boolean onTick(float partialTick, Minecraft minecraft) {
		// If disabled, clear any block tunes and don't do anything else
		if (MinetunesConfig.getBoolean("blockTunes.disabled")) {
			if (trackedNodes.size() > 0) {
				for (BlockTune t : trackedNodes) {
					t.setRemoved();
				}
				trackedNodes.clear();
			}
			return true;
		}

		if (tickCounter % 5 == 0) {
			Minecraft.getMinecraft().mcProfiler.startSection("scan");
			scanForNodes(minecraft.theWorld);
			Minecraft.getMinecraft().mcProfiler.endSection();
		}

		LinkedList<BlockTune> removedNodes = new LinkedList<BlockTune>();
		for (BlockTune n : trackedNodes) {
			if (n.isRemoved()) {
				removedNodes.add(n);
			} else {
				Minecraft.getMinecraft().mcProfiler.startSection("update");
				n.update(minecraft.theWorld);
				Minecraft.getMinecraft().mcProfiler.endSection();
			}
		}
		trackedNodes.removeAll(removedNodes);

		long nowMillis = System.currentTimeMillis();
		manager.update(nowMillis - lastUpdateMillis);
		lastUpdateMillis = nowMillis;

		tickCounter++;
		return true;
	}

	/**
	 * 
	 */
	private void scanForNodes(WorldClient world) {
		List l = world.loadedTileEntityList;
		for (int i = 0; i < l.size(); i++) {
			Object o = l.get(i);
			if (o instanceof TileEntityRecordPlayer) {
				TileEntityRecordPlayer tile = (TileEntityRecordPlayer) o;
				if (!tileEntityAlreadyNode(tile)) {
					if (BlockTune.isTileEntityNode(tile, world)) {
						BlockTune node = new BlockTune(tile);
						// if (!trackedNodes.contains(node)) {
						trackedNodes.add(node);
						// }
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
		for (BlockTune n : trackedNodes) {
			if (n.getNodePoint().equals(tilePoint)) {
				return true;
			}
		}
		return false;
	}
}
