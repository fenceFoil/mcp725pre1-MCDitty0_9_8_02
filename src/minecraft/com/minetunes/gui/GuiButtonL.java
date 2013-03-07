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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiButton;

import org.lwjgl.opengl.GL11;

import com.minetunes.Minetunes;

/**
 * An extended version of Minecraft's GuiButton, with icon (alone, or with text)
 * labels and an ActionListener for pressed events.
 * 
 */
public class GuiButtonL extends GuiButton {

	private boolean iconShown = false;
	private int iconTex = 0;
	private int iconIndex = 0;
	private String action = null;

	private static final Random rand = new Random();

	private HashSet<ActionListener> listeners = new HashSet<ActionListener>();

	public GuiButtonL(String action, int x, int y, int width, int height,
			String label) {
		super(99999 + Minetunes.rand.nextInt(10000000), x, y, width, height,
				label);
		setAction(action);
	}

	public GuiButtonL(String action, int x, int y, int width, int height,
			int iconTex, int iconIndex) {
		super(99999 + Minetunes.rand.nextInt(10000000), x, y, width, height, "");
		setAction(action);
		setIconTex(iconTex);
		setIconIndex(iconIndex);
		setIconShown(true);
	}

	public GuiButtonL(String action, int x, int y, int width, int height,
			int iconTex, int iconIndex, String label) {
		this(action, x, y, width, height, iconTex, iconIndex);
		displayString = label;
	}

	public int getIconTex() {
		return iconTex;
	}

	public void setIconTex(int iconTex) {
		this.iconTex = iconTex;
	}

	public int getIconIndex() {
		return iconIndex;
	}

	public void setIconIndex(int iconIndex) {
		this.iconIndex = iconIndex;
	}

	public boolean isIconShown() {
		return iconShown;
	}

	public void setIconShown(boolean iconShown) {
		this.iconShown = iconShown;
	}

	@Override
	public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3) {
		boolean mouseOver = super.mousePressed(par1Minecraft, par2, par3);
		if (mouseOver) {
			fireActionEvent();
		}
		return mouseOver;
	}

	public void addListener(ActionListener a) {
		listeners.add(a);
	}

	public void removeListener(ActionListener a) {
		listeners.remove(a);
	}

	public void clearListeners() {
		listeners.clear();
	}

	/**
	 * 
	 */
	protected void fireActionEvent() {
		ActionEvent e = new ActionEvent(this,
				(int) (System.currentTimeMillis() * 100 + rand.nextInt(100)),
				action);
		for (ActionListener a : listeners) {
			a.actionPerformed(e);
		}
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	@Override
	public void drawButton(Minecraft mc, int mx, int my) {
		if (drawButton) {
			// Check for hover
			field_82253_i = isMouseOver(mx, my);

			// Set up texture
			GL11.glBindTexture(GL11.GL_TEXTURE_2D,
					mc.renderEngine.getTexture("/gui/gui.png"));
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

			// Draw texture, grabbing a half from each end of the button texture
			// and moving them closer or farther to match the button's width
			int hoverState = getHoverState(isMouseOver(mx, my));
			drawTexturedModalRect(xPosition, yPosition, 0,
					46 + hoverState * 20, width / 2, height);
			drawTexturedModalRect(xPosition + width / 2, yPosition,
					200 - width / 2, 46 + hoverState * 20, width / 2, height);

			// Fire a mouse dragged event
			mouseDragged(mc, mx, my);

			// Decide the label's color
			int labelColor = 0xE0E0E0;
			if (!enabled) {
				labelColor = 0xFFA0A0A0;
			} else if (field_82253_i) {
				labelColor = 0xFFFFA0;
			}

			// Draw label
			int labelOffset = 0;
			int textWidth = mc.fontRenderer.getStringWidth(displayString);
			if (isIconShown() && width < textWidth + 40) {
				labelOffset += 9;
			}
			drawCenteredString(mc.fontRenderer, displayString, xPosition
					+ width / 2 + labelOffset, yPosition + (height - 8) / 2,
					labelColor);

			// Draw icon
			if (isIconShown()) {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, iconTex);
				drawTexturedModalRect(xPosition + 2, yPosition + 2,
						(iconIndex % 16) * 16, (iconIndex / 16) * 16, 16, 16);
			}
		}
	}

	protected boolean isMouseOver(int mx, int my) {
		return mx >= this.xPosition && my >= this.yPosition
				&& mx < this.xPosition + this.width
				&& my < this.yPosition + this.height;
	}

}
