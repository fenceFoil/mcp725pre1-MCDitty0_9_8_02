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
package com.wikispaces.mcditty.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;

import com.wikispaces.mcditty.config.MCDittyConfig;

/**
 * Displays a MCDitty version number on the upper left corner of the screen.
 * 
 * It can be clicked to go to MCDitty settings
 * 
 */
public class MCDittyVersionReadoutGuiElement extends GuiButton {

	public MCDittyVersionReadoutGuiElement(int id) {
		super(id, 0, 0, "");

	}

	public static boolean checkedForUpdates = false;
	public static boolean outdated = false;
	private String string;

	public void drawButton(Minecraft mc, int mx, int my) {
		FontRenderer fontRenderer = mc.fontRenderer;

		// Check for updates if not already done
		if (!checkedForUpdates) {
			checkedForUpdates = true;
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					MCDittyVersionReadoutGuiElement.outdated = GuiMCDittyUpdates
							.checkForUpdates();
				}
			});
			t.setName("MCDitty Version Label Outdated Checker");
			t.start();
		}

		string = "MCDitty Version " + MCDittyConfig.CURRENT_VERSION;
		int stringColor = 0x444444;

		if (outdated) {
			string += " (Outdated)";
		}
		if (mx >= 0 && mx <= fontRenderer.getStringWidth(string) && my >= 0
				&& my <= 10) {
			// Hovering
			string = "§n" + string;
			stringColor = 0xbbbbbb;
		}

		fontRenderer.drawStringWithShadow(string, 0, 0, stringColor);
	}

	/**
	 * Returns true if the mouse has been pressed on this control. Equivalent of
	 * MouseListener.mousePressed(MouseEvent e).
	 */
	public boolean mousePressed(Minecraft mc, int mx, int my) {
		return mx >= 0 && mx <= mc.fontRenderer.getStringWidth(string)
				&& my >= 0 && my <= 10;
	}
}
