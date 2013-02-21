/**
 * Copyright (c) 2012 William Karnavas 
 * All Rights Reserved
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
package com.minetunes;

import java.util.HashSet;

import net.minecraft.client.Minecraft;
import net.minecraft.src.BlockSign;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityClientPlayerMP;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Vec3;
import net.minecraft.src.World;

/**
 * Fires a tick event to all registered TickListeners every tick.
 * Self-contained, handles refreshing itself.<br>
 * <br>
 * To use, add listeners with addTickListener(), then call startChecker() to
 * activate. startChecker() can be called at any time when loading your mod, or
 * multiple times (will only have an effect on the first call).<br>
 * <br>
 * This class also works with the Minecraft profiler! View profiler results for
 * each registered listener's update under
 * root.tick.level.entities.regular.tick.mod_*, where * is the name of the
 * registered class.
 */
public class TickHookEntity extends Entity {

	private static HashSet<TickListener> tickListeners = new HashSet<TickListener>();
	private static TickHookEntity hookEntity;
	private static Thread checkerThread;
	private static boolean stopped = false;

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (isDead) {
			return;
		}

		// Call the hook method in MineTunes
		fireTickEvent();
	}

	/**
	 * Kinda nothing to init in this thing...
	 */
	@Override
	protected void entityInit() {

	}

	/**
	 * Intentionally blank -- no need to save this fella. Minecraft, you didn't
	 * see *anything*...
	 */
	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {

	}

	/**
	 * Intentionally blank -- no need to save this fella. Minecraft, you didn't
	 * see *anything*...
	 */
	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
	}

	/**
	 * Always true.
	 */
	@Override
	public boolean isInRangeToRenderVec3D(Vec3 par1Vec3) {
		return true;
	}

	/**
	 * Still always true.
	 */
	@Override
	public boolean isInRangeToRenderDist(double par1) {
		return true;
	}

	/**
	 * @param par1World
	 */
	private TickHookEntity(World par1World) {
		super(par1World);
		moveToPlayer();
	}

	private void moveToPlayer() {
		// Follow player
		if (Minecraft.getMinecraft() != null
				&& Minecraft.getMinecraft().thePlayer != null) {
			EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
			posX = player.posX;
			posY = player.posY + 25;
			posZ = player.posZ;
		}
	}

	private void fireTickEvent() {
		if (stopped) {
			return;
		}

		// Profile each listener's tick as their onTick methods are called
		// Remove them if they signal to by returning "false" from their ontick
		// methods
		// Profiling appears under root.tick.level.entity.regular.*
		// Or something like that.
		for (TickListener l : tickListeners) {
			try {
				Minecraft.getMinecraft().mcProfiler
						.startSection("mod_"
								+ l.getClass()
										.getName()
										.substring(
												l.getClass().getName()
														.lastIndexOf(".") + 1));
			} catch (Exception e) {
				e.printStackTrace();
			}
			boolean result = true;
			try {
				result = l.onTick(Finder.getMCTimer().elapsedPartialTicks,
						Minecraft.getMinecraft());
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!result) {
				tickListeners.remove(l);
			}
			Minecraft.getMinecraft().mcProfiler.endSection();
		}
	}

	/**
	 * Tries to create a new entity to hook into Minecraft ticks with. Refreshes
	 * each time to ensure that switching to a new world does not remove our
	 * hook.
	 */
	private static void createHookEntity() {
		Minecraft mc = Minecraft.getMinecraft();

		// Check the player, since otherwise calling addEntityToWorld will
		// throw a null p exception
		if (mc == null || mc.theWorld == null || mc.thePlayer == null) {
			return;
		}

		try {
			if (TickHookEntity.hookEntity != null) {
				TickHookEntity.hookEntity.setDead();
			}

			TickHookEntity.hookEntity = new TickHookEntity(
					Minecraft.getMinecraft().theWorld);
			mc.theWorld.addEntityToWorld((int) System.currentTimeMillis(),
					TickHookEntity.hookEntity);
		} catch (Exception e) {
			// Failed
			TickHookEntity.hookEntity = null;
			e.printStackTrace();
		}
	}

	/**
	 * Add a new listener for tick events. An event is fired every time
	 * onupdate() is called on the tick hook entity.
	 */
	public static void addTickListener(TickListener t) {
		tickListeners.add(t);
	}

	/**
	 * Begins placing this hook into the minecraft world. Launches a thread that
	 * manages checking and making sure the tick entity is almost always loaded
	 * and throwing tick events.
	 */
	public static void start() {
		stopped = false;
		if (checkerThread == null) {
			checkerThread = new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						TickHookEntity.createHookEntity();

						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							break;
						}
					}
				}

			});
			checkerThread.setName("MineTunes TickHookEntity Refresher");
			checkerThread.start();
		}
	}

	/**
	 * Stops making sure the hook entity is in the world. Does not remove entity
	 * from world, but does stop it from throwing events.
	 */
	public static void stop() {
		if (checkerThread != null) {
			checkerThread.interrupt();
			checkerThread = null;
		}
		stopped = true;
	}
}
