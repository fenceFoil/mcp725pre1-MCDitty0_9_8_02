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
package com.minetunes.signs;

import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.BlockSign;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntitySign;

import com.minetunes.Minetunes;
import com.minetunes.Point3D;

/**
 * Contains additional data for rendering particular signs
 * 
 */
public class TileEntitySignMinetunes extends TileEntitySign {

	public boolean startBlinking = false;
	public boolean blinking = false;
	public long blinkingEndTime = 0;
	public boolean[] errorBlinkLine;
	public String signColorCode = null;
	/**
	 * Text that is appended to the beginning of each line of the sign before
	 * display.
	 */
	public String[] highlightLine;
	/**
	 * How much the sign is damaged, as caused by a player punching it.
	 */
	public int damage = 0;
	/**
	 * Whether the player has picked this sign to play in a test
	 */
	public boolean picked = false;
	private float[] highlightColor = { -1, -1, -1, -1 };
	public int charBeingEdited = 0;
	public boolean alwaysRender = false;
	private LinkedList<String[]> pastTexts = null;
	private String[] lastSignText = { "", "", "", "" };
	private boolean opaqueAnchorCalculated = false;
	private boolean isAnchorOpaque = false;
	private static HashMap<String, String> names = new HashMap<String, String>();
	private static String codeCycle = "0123456789abcdef";
	static {
		names.put("0", "Black");
		names.put("1", "Blue");
		names.put("2", "Green");
		names.put("3", "Aqua");
		names.put("4", "Red");
		names.put("5", "Purple");
		names.put("6", "Orange");
		names.put("7", "Grey");
		names.put("8", "Stone");
		names.put("9", "Lt Blue");
		names.put("a", "Lime");
		names.put("b", "Sky");
		names.put("c", "Lt Red");
		names.put("d", "Pink");
		names.put("e", "Yellow");
		names.put("f", "White");
	}

	public TileEntitySignMinetunes() {
		super();

		errorBlinkLine = new boolean[4];
		for (int i = 0; i < errorBlinkLine.length; i++) {
			errorBlinkLine[i] = false;
		}

		highlightLine = new String[4];
		for (int i = 0; i < highlightLine.length; i++) {
			highlightLine[i] = "";
		}

		// Add this sign to MineTunes's list of all signs ever for possible
		// recovery
		Minetunes.signRecoveryList.add(this);
	}

	/**
	 * @param par1TileEntitySign
	 */
	public TileEntitySignMinetunes(TileEntitySign t) {
		super();

		// Clone settings from t
		xCoord = t.xCoord;
		yCoord = t.yCoord;
		zCoord = t.zCoord;
		signText = t.signText;
		blockType = t.blockType;
		blockMetadata = t.blockMetadata;
		lineBeingEdited = t.lineBeingEdited;
		tileEntityInvalid = t.isInvalid();
		worldObj = t.getWorldObj();

		System.out.println("Upconverting a tileentitysign");

		errorBlinkLine = new boolean[4];
		for (int i = 0; i < errorBlinkLine.length; i++) {
			errorBlinkLine[i] = false;
		}

		highlightLine = new String[4];
		for (int i = 0; i < highlightLine.length; i++) {
			highlightLine[i] = "";
		}

		// Add this sign to MineTunes's list of all signs ever for possible
		// recovery
		Minetunes.signRecoveryList.add(this);
	}

	/**
	 * @return the highlightColor
	 */
	public float[] getHighlightColor() {
		return highlightColor;
	}

	/**
	 * @param highlightColor
	 *            the highlightColor to set
	 */
	public void setHighlightColor(float[] highlightColor) {
		this.highlightColor = highlightColor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.TileEntity#updateEntity()
	 */
	@Override
	public void updateEntity() {
		if (signText[2].length() >= 2
				&& signText[2].toCharArray()[signText[2].length() - 2] == '%') {
			if (signColorCode == null) {
				signColorCode = TileEntitySignRendererMinetunes
						.getSignColorCode(signText);
			}
		} else {
			signColorCode = null;
		}
	}

	public void clearHighlightedLines() {
		for (int i = 0; i < signText.length; i++) {
			highlightLine[i] = "";
		}
	}

	public Point3D posToPoint3D() {
		return new Point3D(xCoord, yCoord, zCoord);
	}

	public Point3D posToPoint3D(Point3D changeThisPoint) {
		changeThisPoint.x = xCoord;
		changeThisPoint.y = yCoord;
		changeThisPoint.z = zCoord;
		return changeThisPoint;
	}

	public boolean isAnchorBlockOpaque() {
		if (!opaqueAnchorCalculated) {
			try {
				Point3D blockBehindSign = BlockSign.getBlockAttachedTo(this);
				if (Block.blocksList[Minecraft.getMinecraft().theWorld
						.getBlockId(blockBehindSign.x, blockBehindSign.y,
								blockBehindSign.z)].isOpaqueCube()) {
					isAnchorOpaque = true;
				}
				opaqueAnchorCalculated = true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				opaqueAnchorCalculated = false;
				return false;
			}
		}
		return isAnchorOpaque;
	}

	/**
	 * Returns a copy of signText, or, if there is a color code in the sign, a
	 * copy without the color code in line 3.
	 * 
	 * TODO: Efficiency, more of it!
	 * 
	 * @return
	 */
	public String[] getSignTextNoCodes() {
		signColorCode = TileEntitySignRendererMinetunes.getSignColorCode(signText);
		if (signColorCode != null) {
			// Save code
			// Return a stripped copy of sign text
			String[] s = copySignText();
			TileEntitySignRendererMinetunes.removeSignColorCodes(s);
			return s;
		} else {
			return signText;
		}
	}

	public String[] copySignText() {
		String[] s = new String[4];
		System.arraycopy(signText, 0, s, 0, 4);
		return s;
	}

	/**
	 * MineTunes: Copies an array of strings
	 * 
	 * @param strings
	 *            Input array
	 * @return Copy array
	 */
	public static String[] copyOfSignText(String[] strings) {
		String[] copy = new String[strings.length];
		for (int i = 0; i < strings.length; i++) {
			copy[i] = String.copyValueOf(strings[i].toCharArray());
		}
		return copy;
	}

	public static String getNameForSignColorCode(String code) {
		if (code == null) {
			return "";
		}
		String name = names.get(code.toLowerCase());
		if (name == null) {
			return "Other";
		} else {
			return name;
		}
	}

	public static String nextCodeInCycle(String currCode) {
		if (currCode.length() != 1) {
			return currCode;
		}

		int currIndex = codeCycle.indexOf(currCode);
		currIndex++;
		if (currIndex >= codeCycle.length()) {
			currIndex = 0;
		}

		return "" + codeCycle.toCharArray()[currIndex];
	}

	public static String prevCodeInCycle(String currCode) {
		if (currCode.length() != 1) {
			return currCode;
		}

		int currIndex = codeCycle.indexOf(currCode);
		currIndex--;
		if (currIndex < 0) {
			currIndex = codeCycle.length() - 1;
		}

		return "" + codeCycle.toCharArray()[currIndex];
	}

	@Override
	public void readFromNBT(NBTTagCompound par1nbtTagCompound) {
		super.readFromNBT(par1nbtTagCompound);

		// Check for any proxpads
		Minetunes.onSignLoaded(this);
	}

}
