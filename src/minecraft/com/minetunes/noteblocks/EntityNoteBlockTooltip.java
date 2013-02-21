/**
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
import java.util.Map;

import net.minecraft.src.Entity;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntityNote;
import net.minecraft.src.Block;

import org.jfugue.elements.Note;

import com.minetunes.Point3D;

/**
 *
 */
public class EntityNoteBlockTooltip extends Entity {
	private float life = 3.0f;

	private Point3D noteBlockPoint;

	private static HashMap<TileEntityNote, EntityNoteBlockTooltip> activeTooltips = new HashMap<TileEntityNote, EntityNoteBlockTooltip>();

	private TileEntityNote noteTile;

	public static Map<String, Integer> instrumentZeroNotes = new HashMap<String, Integer>();

	static {
		// Known and tested
		instrumentZeroNotes.put("harp", 42); // F#3
		instrumentZeroNotes.put("bassattack", 30); // F#2
		instrumentZeroNotes.put("hat", 57); // A4
		instrumentZeroNotes.put("bd", (int) Note.createNote("D1").getValue());
		instrumentZeroNotes.put("snare", (int) Note.createNote("F#3")
				.getValue());
	}

	private static String getNoteName(int noteBlockValue, String instrument,
			int x, int y, int z) {
		Integer baseValue = instrumentZeroNotes.get(instrument);
		if (baseValue == null) {
			// Pick the "default" instrument for noteblocks: piano
			baseValue = instrumentZeroNotes.get("harp");
		}

		int noteValue = noteBlockValue + baseValue
				+ (12 * BlockNoteMinetunes.getOctaveAdjust(x, y, z));
		if (noteValue < 0) {
			noteValue = 0;
		} else if (noteValue > 127) {
			noteValue = 127;
		}

		return Note.getStringForNote((byte) (noteValue));
	}

	public EntityNoteBlockTooltip(TileEntityNote noteTile) {
		super(noteTile.getWorldObj());
		posX = noteTile.xCoord;
		posY = noteTile.yCoord;
		posZ = noteTile.zCoord;
		ignoreFrustumCheck = true;
		setPosition(posX, posY, posZ);
		setNoteTile(noteTile);
		setNoteBlockPoint(new Point3D(noteTile.xCoord, noteTile.yCoord,
				noteTile.zCoord));

		// Prevent duplicates
		// TODO: Revive that one, dispose of this one. For efficiency: REDUCE
		// NUMBER OF THROWN OUT TOOLTIPS!
		if (activeTooltips.get(noteTile) != null) {
			EntityNoteBlockTooltip t = activeTooltips.get(noteTile);
			t.life = 0;
			t.setDead();
		}

		// No point if value is not known
		if (noteTile instanceof TileEntityNoteMinetunes) {
			TileEntityNoteMinetunes noteTileMineTunes = (TileEntityNoteMinetunes) noteTile;
			if (!noteTileMineTunes.noteValueKnown) {
				life = 0;
				setDead();
			}
		}

		activeTooltips.put(noteTile, this);
	}

	public float getOpacity() {
		if (life > 1) {
			return 1;
		} else if (life < 0) {
			return 0;
		} else {
			return life;
		}
	}

	@Override
	protected void entityInit() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound var1) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound var1) {
		// TODO Auto-generated method stub

	}

	public Point3D getNoteBlockPoint() {
		return noteBlockPoint;
	}

	public void setNoteBlockPoint(Point3D noteBlockPoint) {
		this.noteBlockPoint = noteBlockPoint;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		life -= 0.05f;

		if (life <= 0
				|| worldObj.getBlockId(noteBlockPoint.x, noteBlockPoint.y,
						noteBlockPoint.z) != Block.music.blockID) {
			setDead();
		}

		// System.out.println ("Tooltip updated! "+life);
	}

	@Override
	public boolean isInRangeToRenderDist(double par1) {
		return true;
	}

	public String getText() {
		String noteInstrument = BlockNoteMinetunes.getNoteTypeForBlock(worldObj,
				noteBlockPoint.x, noteBlockPoint.y, noteBlockPoint.z);
		return getNoteName(noteTile.note, noteInstrument, noteBlockPoint.x,
				noteBlockPoint.y, noteBlockPoint.z);
	}

	public TileEntityNote getNoteTile() {
		return noteTile;
	}

	public void setNoteTile(TileEntityNote noteTile) {
		this.noteTile = noteTile;
	}
}
