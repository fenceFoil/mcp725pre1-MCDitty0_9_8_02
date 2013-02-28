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
package com.minetunes.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.Gui;

/**
 * A flat-colored rectangle button. Can be of any size, and highlights when
 * rolled over. Actually uses ActionEvents and a listener model instead of the
 * half-assed "GuiScreen.buttonPressed" methods.
 * 
 */
public class GuiButtonRect extends Gui {
	private Rectangle size;
	private int color;
	private String label;
	private HashSet<ActionListener> listeners = new HashSet<ActionListener>();

	public GuiButtonRect(Rectangle rect, String label) {
		size = rect;
		this.label = label;
		this.color = 0x88ffffff;
	}
	
	public GuiButtonRect(Rectangle rect, String label, int color) {
		size = rect;
		this.label = label;
		this.color = color;
	}

	public void draw(int mx, int my, float par3, FontRenderer fr) {
		int drawColor = color;
		drawRect(size.x, size.y, size.x + size.width, size.y + size.height,
				drawColor);
		if (isMouseOver(mx, my)) {
			drawRect(size.x, size.y, size.x + size.width, size.y + size.height,
					0x44ffffff);
		}
		int textColor = 0xffffa0;
		if (!isMouseOver(mx, my)) {
			textColor = 0xffffff;
		}
		drawCenteredString(fr, label, (size.x) + (size.width / 2),
				(size.y) + (size.height / 2) - (8/2), textColor);
	}

	public void onMousePressed(int mx, int my, int button) {
		if (isMouseOver(mx, my) && button == 0) {
			Minecraft.getMinecraft().sndManager.playSoundFX("random.click",
					1.0F, 1.0f);
			fireActionEvent(new ActionEvent(this, 0, "clicked"));
		}
	}

	/**
	 * 
	 */
	protected void fireActionEvent(ActionEvent event) {
		for (ActionListener l : listeners) {
			l.actionPerformed(event);
		}
	}

	private boolean isMouseOver(int mx, int my) {
		Point p = new Point(mx, my);
		return size.contains(p);
	}

	public void addActionListener(ActionListener l) {
		listeners.add(l);
	}

	/**
	 * 
	 */
	public void removeActionListeners() {
		listeners.clear();
	}
}
