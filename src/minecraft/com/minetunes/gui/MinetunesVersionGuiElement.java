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
package com.minetunes.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.GuiButton;

import com.minetunes.Minetunes;
import com.minetunes.config.MinetunesConfig;

/**
 * Displays a MineTunes version number on the upper left corner of the screen.
 * 
 * It can be clicked to go to MineTunes settings
 * 
 */
public class MinetunesVersionGuiElement extends GuiButton {

	public MinetunesVersionGuiElement(int id) {
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
					MinetunesVersionGuiElement.outdated = MinetunesUpdateGui
							.checkForUpdates();
				}
			});
			t.setName("MineTunes Version Label Outdated Checker");
			t.start();
		}

		string = "MineTunes " + MinetunesConfig.CURRENT_VERSION;
		int stringColor = 0x444488;

		if (outdated) {
			string += " (Outdated)";
		} else if (Minetunes.forgeMode) {
			string += " [Forge]";
		}
		if (mx >= 0 && mx <= fontRenderer.getStringWidth(string) && my >= 0
				&& my <= 10) {
			// Hovering
			string = "§n" + string;
			stringColor = 0xaa99ff;
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
