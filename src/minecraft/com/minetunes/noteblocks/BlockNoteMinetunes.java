/**
 * CHANGES FROM MOJANG CODE
 * Copyright (c) 2012 William Karnavas 
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
package com.minetunes.noteblocks;

import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.BlockNote;
import net.minecraft.src.BlockSign;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityNote;
import net.minecraft.src.TileEntitySign;
import net.minecraft.src.World;

import com.minetunes.Finder;
import com.minetunes.Minetunes;
import com.minetunes.Point3D;
import com.minetunes.config.MinetunesConfig;
import com.minetunes.ditty.event.CueEvent;
import com.minetunes.particle.ParticleRequest;
import com.minetunes.signs.SignTuneParser;
import com.minetunes.signs.SignParser;
import com.minetunes.signs.TileEntitySignMinetunes;
import com.minetunes.signs.keywords.NoteblockTriggerKeyword;

/**
 * The Herobrine of NoteBlock: hijacked into the list of blocks by id instead of
 * net.minecraft.src.BlockNote. Contains modified methods.
 * 
 * TODO: Move hacky solution for finding pitch of noteblock from EntityFXNote to
 * this class's onBlockEventReceived()
 * 
 */
public class BlockNoteMinetunes extends BlockNote {
	public static HashMap<String, String> screenNames = new HashMap<String, String>();

	static {
		screenNames.put("harp", "Piano");
		screenNames.put("bd", "Bass Drum");
		screenNames.put("bassattack", "Double Bass");
		screenNames.put("hat", "Hi-Hat");
		screenNames.put("snare", "Snare Drum");
	}

	public static void removeNormalNoteBlockFromList()
			throws IllegalArgumentException, IllegalAccessException {
		Object blockListObj = Finder.getUniqueTypedFieldFromClass(Block.class,
				Block[].class, null);
		if (blockListObj != null && blockListObj instanceof Block[]) {
			Block[] blockList = (Block[]) blockListObj;
			blockList[25] = null;
		}
	}

	public BlockNoteMinetunes(int par1) {
		super(par1);
	}

	// /**
	// * Returns the block texture based on the side being looked at. Args: side
	// */
	// public int getBlockTextureFromSide(int par1) {
	// return this.blockIndexInTexture;
	// }
	//
	// /**
	// * Lets the block know when one of its neighbor changes. Doesn't know
	// which
	// * neighbor changed (coordinates passed are their own) Args: x, y, z,
	// * neighbor blockID
	// */
	// public void onNeighborBlockChange(World par1World, int par2, int par3,
	// int par4, int par5) {
	// if (par5 > 0) {
	// boolean var6 = par1World.isBlockIndirectlyGettingPowered(par2,
	// par3, par4);
	// TileEntityNote var7 = (TileEntityNote) par1World
	// .getBlockTileEntity(par2, par3, par4);
	//
	// if (var7 != null && var7.previousRedstoneState != var6) {
	// if (var6) {
	// var7.triggerNote(par1World, par2, par3, par4);
	// }
	//
	// var7.previousRedstoneState = var6;
	// }
	// }
	// }
	//
	// /**
	// * Called upon block activation (right click on the block.)
	// */
	// public boolean onBlockActivated(World par1World, int par2, int par3,
	// int par4, EntityPlayer par5EntityPlayer, int par6, float par7,
	// float par8, float par9) {
	// if (par1World.isRemote) {
	// return true;
	// } else {
	// TileEntityNote var10 = (TileEntityNote) par1World
	// .getBlockTileEntity(par2, par3, par4);
	//
	// if (var10 != null) {
	// var10.changePitch();
	// var10.triggerNote(par1World, par2, par3, par4);
	// }
	//
	// return true;
	// }
	// }
	//
	// /**
	// * Called when the block is clicked by a player. Args: x, y, z,
	// entityPlayer
	// */
	// public void onBlockClicked(World par1World, int par2, int par3, int par4,
	// EntityPlayer par5EntityPlayer) {
	// if (!par1World.isRemote) {
	// TileEntityNote var6 = (TileEntityNote) par1World
	// .getBlockTileEntity(par2, par3, par4);
	//
	// if (var6 != null) {
	// var6.triggerNote(par1World, par2, par3, par4);
	// }
	// }
	// }
	//
	// /**
	// * Returns a new instance of a block's tile entity class. Called on
	// placing
	// * the block.
	// */
	// public TileEntity createNewTileEntity(World par1World) {
	// return new TileEntityNote();
	// }

	/**
	 * Called when the block receives a BlockEvent - see World.addBlockEvent. By
	 * default, passes it on to the tile entity at this location. Args: world,
	 * x, y, z, blockID, EventID, event parameter
	 */
	public void onBlockEventReceived(World world, int x, int y, int z,
			int noteTypeNum, int noteBlockSetting) {

		// Save note block setting
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (tile instanceof TileEntityNote) {
			// Decide if noteblock is being tuned
			boolean isTuning = false;
			if (tile instanceof TileEntityNoteMinetunes) {
				TileEntityNoteMinetunes mcdittyNoteTile = (TileEntityNoteMinetunes) tile;
				if (mcdittyNoteTile.noteValueKnown
						&& noteBlockSetting != mcdittyNoteTile.note) {
					// Is tuning
					isTuning = true;
				}
			}

			((TileEntityNote) tile).note = (byte) noteBlockSetting;

			// Try to specify that the value is known
			if (tile instanceof TileEntityNoteMinetunes) {
				((TileEntityNoteMinetunes) tile).noteValueKnown = true;
			}

			if (!MinetunesConfig.getMinetunesOff()) {
				// While we're here and have the tile entity, show a tooltip
				// over it
				Minetunes.showNoteblockTooltip((TileEntityNote) tile);

				if (!isTuning
						&& !MinetunesConfig
								.getBoolean("noteblock.signsDisabled")) {
					// And show a lyric too
					activateAnyAdjacentSigns((TileEntityNote) tile);
				}
			}
		}

		float pitchMultiplier = (float) Math.pow(2.0D,
				(double) (noteBlockSetting - 12) / 12.0D);

		String noteType = intToNoteType(noteTypeNum);

		if (!MinetunesConfig.getBoolean("noteblock.mute")) {
			// Handle noteblock octave adjustment
			int adjust = BlockNoteMinetunes.getOctaveAdjust(x, y, z);
			if (adjust != 0) {
				noteType = noteType + "_" + adjust + "o";
			}

			world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D,
					(double) z + 0.5D, "note." + noteType, 3.0F,
					pitchMultiplier);
		}

		world.spawnParticle("note", (double) x + 0.5D, (double) y + 1.2D,
				(double) z + 0.5D, (double) noteBlockSetting / 24.0D, 0.0D,
				0.0D);
	}

	private void activateAnyAdjacentSigns(TileEntityNote tile) {
		// Don't activate signs if MineTunes is off or a gui is open
		if (MinetunesConfig.getMinetunesOff()
				|| Minecraft.getMinecraft().currentScreen != null) {
			return;
		}

		// First, look for signs
		// Start @ north go counterclockwise
		LinkedList<Point3D> nearby = new LinkedList<Point3D>();
		nearby.add(new Point3D(tile.xCoord, tile.yCoord, tile.zCoord - 1));
		nearby.add(new Point3D(tile.xCoord - 1, tile.yCoord, tile.zCoord));
		nearby.add(new Point3D(tile.xCoord, tile.yCoord, tile.zCoord + 1));
		nearby.add(new Point3D(tile.xCoord + 1, tile.yCoord, tile.zCoord));

		// Sort out non-signs and wall signs not attached to noteblock
		int lastNull = 0;
		for (int i = 0; i < nearby.size(); i++) {
			// If point has no sign, ignore it
			Block blockType = SignTuneParser.getSignBlockType(
					nearby.get(i), tile.getWorldObj());
			if (blockType == null || blockType == Block.signPost) {
				// Not a sign, or definitely not attached to noteblock
				nearby.set(i, null);
				lastNull = i;
			} else if (blockType == signWall) {
				// Ignore wall signs not attached to noteblock
				TileEntity signTile = (TileEntity) tile.getWorldObj()
						.getBlockTileEntity(nearby.get(i).x, nearby.get(i).y,
								nearby.get(i).z);
				if (signTile == null || !(signTile instanceof TileEntitySign)) {
					// If the sign's tile entity can't be found, ignore sign
					nearby.set(i, null);
					lastNull = i;
					continue;
				}

				if (!SignTuneParser.getBlockAttachedTo(
						(TileEntitySign) signTile).equals(
						new Point3D(tile.xCoord, tile.yCoord, tile.zCoord))) {
					// Sign isn't attached to noteblock. Ignore.
					nearby.set(i, null);
					lastNull = i;
				}
			}
		}

		// Start activating signs beginning at the last empty spot found
		for (int i = 0; i < nearby.size(); i++) {
			// Offset lastNull by i
			int currPoint = (lastNull + i) % nearby.size();

			// Activate
			Point3D point = nearby.get(currPoint);
			if (point != null) {
				activateAdjacentSign(point, tile);
			}
		}

		// Combine and display any lyrics
		flushActivatedLyrics();
	}

	private void flushActivatedLyrics() {
		StringBuilder lyric = new StringBuilder();
		for (String s : lyricBuffer) {
			lyric.append(s);
		}
		Minetunes.addLyricToQueue(new CueEvent(lyric.toString()));
		lyricBuffer.clear();
	}

	private boolean areSignsAtOpposites(LinkedList<Point3D> nearby) {
		return (nearby.get(0) == null) && (nearby.get(2) == null);
	}

	private LinkedList<String> lyricBuffer = new LinkedList<String>();

	private void activateAdjacentSign(Point3D signPoint, TileEntityNote noteTile) {
		TileEntity tileEntity = noteTile.getWorldObj().getBlockTileEntity(
				signPoint.x, signPoint.y, signPoint.z);
		if (tileEntity instanceof TileEntitySignMinetunes) {
			TileEntitySignMinetunes tile = (TileEntitySignMinetunes) tileEntity;

			if (SignParser.parseKeyword(tile.getSignTextNoCodes()[0]) instanceof NoteblockTriggerKeyword) {
				// Start ditty
				SignTuneParser.playDittyFromSigns(noteTile.getWorldObj(),
						signPoint.x, signPoint.y, signPoint.z, true);
			} else {
				// Read lyric
				lyricBuffer.add(SignTuneParser.readLyricFromSign(0,
						tile.getSignTextNoCodes(), ""));
			}
		}

		for (int i = 0; i < 8; i++) {
			Minetunes.requestParticle(new ParticleRequest(signPoint,
					"snowshovel"));
		}
	}

	/**
	 * Chimes the given noteblock at the specified pitch without changing
	 * anything about the noteblock.
	 * 
	 * @param tile
	 * @param noteblockValueToSimulate
	 */
	public static void chimeBlockAtPitch(TileEntityNoteMinetunes tile,
			int noteblockValueToSimulate) {
		float pitchMultiplier = (float) Math.pow(2.0D,
				(double) (noteblockValueToSimulate - 12) / 12.0D);
		String noteType = getNoteTypeForBlock(tile.getWorldObj(), tile.xCoord,
				tile.yCoord, tile.zCoord);
		int octaveAdjust = getOctaveAdjust(tile.xCoord, tile.yCoord,
				tile.zCoord);

		String soundName = "note." + noteType;
		if (octaveAdjust != 0) {
			soundName += "_" + octaveAdjust + "o";
		}
		// System.out.println (soundName);

		Minecraft.getMinecraft().sndManager.playSoundFX(soundName, 5.0F,
				pitchMultiplier);
	}

	/**
	 * Assumes that x, y, z IS a noteblock and doesn't check.
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static String getNoteTypeForBlock(World world, int x, int y, int z) {
		Material baseMaterial = world.getBlockMaterial(x, y - 1, z);

		byte instrumentNum = 0;
		if (baseMaterial == Material.rock) {
			instrumentNum = 1;
		} else if (baseMaterial == Material.sand) {
			instrumentNum = 2;
		} else if (baseMaterial == Material.glass) {
			instrumentNum = 3;
		} else if (baseMaterial == Material.wood) {
			instrumentNum = 4;
		}

		return intToNoteType(instrumentNum);
	}

	private static String intToNoteType(int num) {
		switch (num) {
		case 0:
			return "harp";
		case 1:
			return "bd";
		case 2:
			return "snare";
		case 3:
			return "hat";
		case 4:
			return "bassattack";
		default:
			return "harp";
		}
	}

	@Override
	public TileEntity createNewTileEntity(World par1World) {
		return new TileEntityNoteMinetunes();
	}

	public static String getScreenName(String instrumentName) {
		return screenNames.get(instrumentName);
	}

	public static int getOctaveAdjust(int x, int y, int z) {
		int adjust = 0;

		// Check blocks adjacent to instrument block
		adjust += getBlockOctaveAdjust(x + 1, y - 1, z);
		adjust += getBlockOctaveAdjust(x - 1, y - 1, z);
		adjust += getBlockOctaveAdjust(x, y - 1, z + 1);
		adjust += getBlockOctaveAdjust(x, y - 1, z - 1);

		// Check block under instrument block
		adjust += getBlockOctaveAdjust(x, y - 2, z);

		// Range check
		if (adjust < -2) {
			adjust = -2;
		} else if (adjust > 2) {
			adjust = 2;
		}

		return adjust;
	}

	private static int getBlockOctaveAdjust(int x, int y, int z) {
		if (Minecraft.getMinecraft() == null
				|| Minecraft.getMinecraft().theWorld == null) {
			return 0;
		}

		int blockID = Minecraft.getMinecraft().theWorld.getBlockId(x, y, z);
		// System.out.println (blockID);
		if (blockID == Block.netherrack.blockID) {
			return -1;
		} else if (blockID == Block.whiteStone.blockID) {
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public void onBlockDestroyedByExplosion(World par1World, int par2,
			int par3, int par4) {
		for (int i = 0; i < 10; i++) {
			dropBlockAsItem(par1World, par2, par3, par4, blockID, 0);
		}
	}

	@Override
	public int getBlockTextureFromSide(int par1) {
		return super.getBlockTextureFromSide(par1);
	}

}
