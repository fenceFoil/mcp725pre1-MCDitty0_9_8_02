/**
 * 
 * Changes from Mojang AB Code (all changes are below this notice):
 * 
 * Copyright (c) 2012 William Karnavas All Rights Reserved
 * 
 */

/**
 * 
 * This file is part of MCDitty.
 * 
 * MCDitty is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * MCDitty is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MCDitty. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.minecraft.src;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.client.Minecraft;

import com.wikispaces.mcditty.GetMinecraft;
import com.wikispaces.mcditty.MCDitty;
import com.wikispaces.mcditty.Point3D;
import com.wikispaces.mcditty.config.MCDittyConfig;

/**
 * Note: incorporates a fix for a petty bug with MC 1.3.1 signs, involving
 * flickering and the text of the sign sometimes being arbitrarily discarded.
 * 
 */
public class Packet130UpdateSign extends Packet {
	public int xPosition;
	public int yPosition;
	public int zPosition;
	public String signLines[];

	// DEBUG
	public int id = 0;
	public static int nextID = 0;

	public Packet130UpdateSign() {
		isChunkDataPacket = true;
		id = nextID;
		nextID++;
		// System.out.println("Packet130 " + id + " Created! (Empty)");
	}

	// private static LinkedList<Packet130Info> recentPackets = new
	// LinkedList<Packet130Info>();

	public Packet130UpdateSign(int par1, int par2, int par3, String signLines[]) {
		isChunkDataPacket = true;
		xPosition = par1;
		yPosition = par2;
		zPosition = par3;
		this.signLines = new String[] { signLines[0], signLines[1],
				signLines[2], signLines[3] };
		id = nextID;
		nextID++;

		// IF YOU ARE READING THIS AND THE SIGN CLEARING BUG IS GONE, DELETE
		// THIS SOURCE

		// // Handle color codes
		// if (signColorCode != null) {
		// signLines[2] += "%" + signColorCode;
		// }

		// System.out.println("Packet130 " + id + " Created! " + par1 + ":" +
		// par2
		// + ":" + par3 + ":" + signLines[0] + "," + signLines[1] + ","
		// + signLines[2] + "," + signLines[3]);

		Throwable stackTraceGetter = new Throwable();
		//stackTraceGetter.printStackTrace(System.out);
		StackTraceElement[] stackTrace = stackTraceGetter.getStackTrace();
		for (StackTraceElement s : stackTrace) {
			if (!MCDittyConfig.MC_CURRENT_VERSION.equals("1.4.4")) {
				System.out
						.println("MCDitty Mod: UPDATE WARNING (to coder): Update the class name for PlayerInstance in the constructor for Packet130UpdateSign.class, used in filtering buggy SSP packets.");
			}
			/*
			 * if (s.getClassName().endsWith("PlayerInstance") ||
			 * s.getClassName().endsWith("il")) { if (signIsBlank(signLines)) {
			 * // System.out.println("*****  Glitchy glitchy packet! *****"); //
			 * System.out.println("*****  Intercepting. *****");
			 * 
			 * // Minecraft 1.4.4 added sign debug messages that foil my // old
			 * // interception method of //
			 * "throw the packet's x, y, and z into the Hinterlands."
			 * 
			 * // System.out.println("Original line was " + signLines[0]);
			 * 
			 * // Mark the text in this packet as buggy crap signLines[0] =
			 * TileEntitySign.INVALID_PACKET_LINE;
			 * 
			 * // System.out.println("Replacing with " + signLines[0]); }
			 * 
			 * return; }
			 */
		}

		//
		// // Check that this is not a buggy duplicate packet
		// // Still necessary for SMP: the above code doesn't work for a
		// // non-integrated server
		// // This attacks the symptoms of buggy packets; the above code
		// attacked
		// // the source
		//
		// // First, discard expired (1.5 sec) packets from recentPackets
		// for (int i = 0; i < recentPackets.size(); i++) {
		// Packet130Info inf = recentPackets.get(i);
		// if (System.currentTimeMillis() - inf.timeConstructed > 1500) {
		// // old packet
		// recentPackets.remove(i);
		// i--;
		// }
		// }
		//
		// // Then, check to see if this packet is a bugged out duplicate
		// // If its text is blank and the previous packets were not blank, it
		// is a
		// // bugged out sign packet.
		//
		// // Check blankness
		// int blankLines = 0;
		// for (int i = 0; i < 4; i++) {
		// if (signLines[i].trim().equals("")) {
		// blankLines++;
		// }
		// }
		// if (blankLines == 4) {
		// // System.out.println("Sign is blank; glitchiness testing starting");
		// // Sign packet is blanked; check to see if it is a glitch
		// // Has a previous packet been sent for this sign recently with text
		// // in it?
		// for (Packet130Info i : recentPackets) {
		// if (i.packet.xPosition == xPosition
		// && i.packet.yPosition == yPosition
		// && i.packet.zPosition == zPosition) {
		// //System.out.println("Previous recent packets exist");
		// // In same block
		// // Was previous packet blank too, or did it have text?
		// boolean previousSignPacketWasBlank = true;
		// for (String l : i.packet.signLines) {
		// //System.out.println(l);
		// if (l.trim().length() > 0) {
		// previousSignPacketWasBlank = false;
		// break;
		// }
		// }
		// if (previousSignPacketWasBlank) {
		// // This packet is fine. Let it be.
		// //System.out
		// // .println("Previous packet was blank too; false alarm");
		// } else {
		// // This packet would've caused the sign to flicker
		// // and/or lose its text. Replace the glitchy blank text
		// // with the correct text.
		// //signLines = i.packet.signLines;
		//
		// // This is crap. Delete it if you see it again looking
		// // at this code.
		// // TileEntity t =
		// // GetMinecraft.instance().theWorld.getBlockTileEntity(xPosition,
		// // yPosition, zPosition);
		// // if (t instanceof TileEntitySign) {
		// // TileEntitySign ts = (TileEntitySign) t;
		// // for (int c=0;c<4;c++) {
		// // ts.signText[c] = i.packet.signLines[c];
		// // }
		// // }
		//
		// System.out.println("Fixed a glitched sign packet!");
		// xPosition = Integer.MAX_VALUE;
		// yPosition = Integer.MAX_VALUE;
		// zPosition = Integer.MAX_VALUE;
		// }
		// }
		// }
		// }
		//
		// recentPackets.add(new Packet130Info((Packet130UpdateSign)
		// this.clone(),
		// System.currentTimeMillis()));
	}

	private static boolean signIsBlank(String[] sArr) {
		int linesBlank = 0;
		int totalLines = 0;
		for (String s : sArr) {
			totalLines++;
			if (s == null || s.equals("")) {
				linesBlank++;
			}
		}

		if (linesBlank == totalLines) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected Object clone() {
		Packet130UpdateSign p = new Packet130UpdateSign();
		p.xPosition = xPosition;
		p.yPosition = yPosition;
		p.zPosition = zPosition;
		p.signLines = new String[4];
		p.id = id;
		System.arraycopy(signLines, 0, p.signLines, 0, 4);
		return p;
	}

	/**
	 * Abstract. Reads the raw packet data from the data stream.
	 */
	@Override
	public void readPacketData(DataInputStream par1DataInputStream)
			throws IOException {
		xPosition = par1DataInputStream.readInt();
		yPosition = par1DataInputStream.readShort();
		zPosition = par1DataInputStream.readInt();
		signLines = new String[4];

		// System.out.println("Packet130 " + id + " Read! " + xPosition + ":"
		// + yPosition + ":" + zPosition + ":" + signLines[0] + ","
		// + signLines[1] + "," + signLines[2] + "," + signLines[3]);

		for (int i = 0; i < 4; i++) {
			signLines[i] = readString(par1DataInputStream, 15);
		}
		MCDitty.onSignLoaded(xPosition, yPosition, zPosition, signLines);
	}

	/**
	 * Abstract. Writes the raw packet data to the data stream.
	 */
	@Override
	public void writePacketData(DataOutputStream par1DataOutputStream)
			throws IOException {
		par1DataOutputStream.writeInt(xPosition);
		par1DataOutputStream.writeShort(yPosition);
		par1DataOutputStream.writeInt(zPosition);

		// System.out.println("Packet130 " + id + " Written! " + xPosition + ":"
		// + yPosition + ":" + zPosition + ":" + signLines[0] + ","
		// + signLines[1] + "," + signLines[2] + "," + signLines[3]);

		for (int i = 0; i < 4; i++) {
			writeString(signLines[i], par1DataOutputStream);
		}
	}

	/**
	 * Passes this Packet on to the NetHandler for processing.
	 */
	public void processPacket(NetHandler par1NetHandler) {
		par1NetHandler.handleUpdateSign(this);
	}

	/**
	 * Abstract. Return the size of the packet (not counting the header).
	 */
	public int getPacketSize() {
		int i = 0;

		for (int j = 0; j < 4; j++) {
			i += signLines[j].length();
		}

		return i;
	}
}
