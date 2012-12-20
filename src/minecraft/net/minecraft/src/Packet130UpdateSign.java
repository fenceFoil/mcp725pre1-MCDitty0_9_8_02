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

		// // Handle color codes
		// if (signColorCode != null) {
		// signLines[2] += "%" + signColorCode;
		// }

		// System.out.println("Packet130 " + id + " Created! " + par1 + ":" +
		// par2
		// + ":" + par3 + ":" + signLines[0] + "," + signLines[1] + ","
		// + signLines[2] + "," + signLines[3]);
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
