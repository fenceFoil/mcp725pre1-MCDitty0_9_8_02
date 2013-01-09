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
package com.wikispaces.mcditty.noteblocks;

import java.util.HashMap;

import net.minecraft.src.Entity;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntityNote;

import com.wikispaces.mcditty.Point3D;

/**
 *
 */
public class EntityNoteBlockTooltip extends Entity {
	private float life = 4.0f;

	private Point3D noteBlockPoint;

	private static HashMap<TileEntityNote, EntityNoteBlockTooltip> activeTooltips = new HashMap<TileEntityNote, EntityNoteBlockTooltip>();

	private TileEntityNote noteTile;

	private static final String[] textsPiano = { "F#3", "G3", "G#3", "A3",
			"A#3", "B3", "C4", "C#4", "D4", "D#4", "E4", "F4", "F#4", "G4",
			"G#4", "A4", "A#4", "B4", "C5", "C#5", "D5", "D#5", "E5", "F5",
			"F#5" };

	private static final String[] textsBass = { "F#2", "G2", "G#2", "A2",
			"A#2", "B2", "C3", "C#3", "D3", "D#3", "E3", "F3", "F#3", "G3",
			"G#3", "A3", "A#3", "B3", "C4", "C#4", "D4", "D#4", "E4", "F4",
			"F#4" };

	private static final String[] textsHiHat = { "A4", "A#4", "B4", "C5",
			"C#5", "D5", "D#5", "E5", "F5", "F#5", "G5", "G#5", "A5", "A#5",
			"B5", "C6", "C#6", "D6", "D#6", "E6", "F6", "F#6", "G6", "G#6",
			"A6" };

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
		if (activeTooltips.get(noteTile) != null) {
			EntityNoteBlockTooltip t = activeTooltips.get(noteTile);
			t.life = 0;
			t.setDead();
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

		if (life <= 0) {
			setDead();
		}

		// System.out.println ("Tooltip updated! "+life);
	}

	@Override
	public boolean isInRangeToRenderDist(double par1) {
		return true;
	}

	public String getText() {
		String noteType = BlockNoteMCDitty.getNoteTypeForBlock(worldObj,
				noteBlockPoint.x, noteBlockPoint.y, noteBlockPoint.z);
		if (noteType.equalsIgnoreCase("bassattack")) {
			return textsBass[noteTile.note];
		} else if (noteType.equalsIgnoreCase("hat")) {
			return textsHiHat[noteTile.note];
		} else {
			return textsPiano[noteTile.note];
		}
	}

	public TileEntityNote getNoteTile() {
		return noteTile;
	}

	public void setNoteTile(TileEntityNote noteTile) {
		this.noteTile = noteTile;
	}
}
