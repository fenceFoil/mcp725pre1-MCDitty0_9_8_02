package net.minecraft.src;

import java.util.HashMap;
import java.util.LinkedList;

import com.wikispaces.mcditty.GetMinecraft;
import com.wikispaces.mcditty.MCDitty;
import com.wikispaces.mcditty.Point3D;

/**
 * Changes to Mojang AB code are copyrighted:
 * 
 * Copyright (c) 2012 William Karnavas All Rights Reserved
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

public class TileEntitySign extends TileEntity {
	public static final String INVALID_PACKET_LINE = "BUGGY-PACKET";

	public boolean redstoneState = false;

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

	public String signText[] = { "", "", "", "" };

	/**
	 * The index of the line currently being edited. Only used on client side,
	 * but defined on both. Note this is only really used when the > < are going
	 * to be visible.
	 */
	public int lineBeingEdited;
	public int charBeingEdited = 0;
	public boolean alwaysRender = false;
	private boolean isEditable;

	private LinkedList<String[]> pastTexts = null;

	public TileEntitySign() {
		lineBeingEdited = -1;
		isEditable = true;

		errorBlinkLine = new boolean[4];
		for (int i = 0; i < errorBlinkLine.length; i++) {
			errorBlinkLine[i] = false;
		}

		highlightLine = new String[4];
		for (int i = 0; i < highlightLine.length; i++) {
			highlightLine[i] = "";
		}

		// Add this sign to MCDitty's list of all signs ever for possible
		// recovery
		MCDitty.signRecoveryList.add(this);
	}

	private String[] lastSignText = { "", "", "", "" };

	/**
	 * Notably, this method is called by NetClientHandler when it changes the
	 * text of a sign after reading a packet.
	 * 
	 * Here, it checks to see if the sign's text has been changed by a buggy
	 * packet, and resets it if it has been.
	 */
	@Override
	public void onInventoryChanged() {
//		if (signText[0].equals(INVALID_PACKET_LINE)) {
//			signText = lastSignText;
//		}

//		lastSignText = copySignText();
//
		super.onInventoryChanged();
	}

	/**
	 * Writes a tile entity to NBT.
	 * 
	 * You may note that this is completely unaltered from an unmodified
	 * TileEntitySign's code.
	 */
	public void writeToNBT(NBTTagCompound par1NBTTagCompound) {
		super.writeToNBT(par1NBTTagCompound);
		par1NBTTagCompound.setString("Text1", signText[0]);
		par1NBTTagCompound.setString("Text2", signText[1]);
		par1NBTTagCompound.setString("Text3", signText[2]);
		par1NBTTagCompound.setString("Text4", signText[3]);
	}

	/**
	 * Reads a tile entity from NBT. Again, same as a vanilla TileEntitySign
	 */
	public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
		// System.out.println ("Reading sign from NBT...");
		isEditable = false;
		super.readFromNBT(par1NBTTagCompound);

		for (int i = 0; i < 4; i++) {
			signText[i] = par1NBTTagCompound.getString((new StringBuilder())
					.append("Text").append(i + 1).toString());

			if (signText[i].length() > 15) {
				signText[i] = signText[i].substring(0, 15);
			}
		}

		// // MCDitty: Check for any proxpads
		MCDitty.onSignLoaded(this);
	}

	public boolean isEditable() {
		return isEditable;
	}

	/**
	 * Sets the sign's isEditable flag to the specified parameter.
	 */
	public void setEditable(boolean par1) {
		isEditable = par1;
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
				signColorCode = TileEntitySignRenderer
						.getSignColorCode(signText);
			}
		} else {
			signColorCode = null;
		}

		// // Reset in case of invalid packet
		// if (checkForInvalidText) {
		// if (signText[0].equals(INVALID_PACKET_LINE)) {
		// // An invalid packet has changed this sign's text!
		// // Reset to last valid saved text
		// String[] lastKnownGoodText = null;
		// if (pastTexts != null) {
		// while (pastTexts.size() > 0) {
		// String[] s = pastTexts.pollLast();
		//
		// if (s != null && s[0].equals(INVALID_PACKET_LINE)) {
		// // Keep checking
		// continue;
		// } else if (s != null) {
		// lastKnownGoodText = s;
		// break;
		// }
		// }
		// }
		//
		// if (lastKnownGoodText != null) {
		// signText = lastKnownGoodText;
		// }
		//
		// pastTexts = null;
		// checkForInvalidText = false;
		// }
		// }
	}

	/**
	 * New in 1.3.1 Release
	 */
	@Override
	public Packet getDescriptionPacket() {
		// System.out.println ("func 70319 called");
		String as[] = new String[4];
		System.arraycopy(signText, 0, as, 0, 4);
		return new Packet130UpdateSign(xCoord, yCoord, zCoord, as);
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

	private boolean opaqueAnchorCalculated = false;
	private boolean isAnchorOpaque = false;

	public boolean isAnchorBlockOpaque() {
		if (!opaqueAnchorCalculated) {
			try {
				Point3D blockBehindSign = BlockSign.getBlockAttachedTo(this);
				if (Block.blocksList[GetMinecraft.instance().theWorld
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

	public String[] copySignText() {
		String[] s = new String[4];
		System.arraycopy(signText, 0, s, 0, 4);
		return s;
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
		signColorCode = TileEntitySignRenderer.getSignColorCode(signText);
		if (signColorCode != null) {
			// Save code
			// Return a stripped copy of sign text
			String[] s = copySignText();
			TileEntitySignRenderer.removeSignColorCodes(s);
			return s;
		} else {
			return signText;
		}
	}

	/**
	 * MCDitty: Copies an array of strings
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

	private static HashMap<String, String> names = new HashMap<String, String>();
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
		names.put("9", "L. Blue");
		names.put("a", "Lime");
		names.put("b", "Sky");
		names.put("c", "L. Red");
		names.put("d", "Pink");
		names.put("e", "Yellow");
		names.put("f", "White");
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

	private static String codeCycle = "0123456789abcdef";

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

	// public void setInvalidPacketIncoming(boolean b) {
	// lastText = copyOfSignText(signText);
	// invalidPacketIncoming = b;
	// }

	// public void prepareToResetIncomingInvalidPacket() {
	// if (pastTexts == null) {
	// pastTexts = new LinkedList<String[]>();
	// }
	//
	// pastTexts.add(copyOfSignText(signText));
	// checkForInvalidText = true;
	// }
}
