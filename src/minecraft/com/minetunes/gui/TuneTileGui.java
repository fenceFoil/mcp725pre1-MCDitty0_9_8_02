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

import net.minecraft.client.Minecraft;
import net.minecraft.src.Gui;
import net.minecraft.src.GuiButton;
import net.minecraft.src.Timer;

import org.lwjgl.opengl.GL11;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

import com.minetunes.Finder;
import com.minetunes.Minetunes;

/**
 * @author William
 * 
 */
public class TuneTileGui extends Gui {
	private HashSet<ActionListener> listeners = new HashSet<ActionListener>();

	private TuneTileType type = TuneTileType.NOTEBLOCKS;

	private int x = 0;
	private int y = 0;

	private boolean on;

	private GuiButtonL settingsButton;
	private GuiButtonL helpButton;

	private boolean mouseOver;

	private boolean hasSettingsButton = true;

	private static float lastElapsedTicks = 0;
	private static float lastElapsedPartialTicks = 0;

	static {
		Tween.registerAccessor(TuneTileGui.class,
				new TuneTileGuiTweenAccessor());
	}

	public TuneTileGui(int x, int y, TuneTileType type, boolean on) {
		this.type = type;
		this.on = on;

		int tex = Minecraft.getMinecraft().renderEngine
				.getTexture("/com/minetunes/resources/textures/mineTunesLogo.png");
		
		//helpButton = new GuiButton(1, x + 32 - 10, 0, 20, 20, "§b?");
		helpButton = new GuiButtonL("help", x+32 - 10, 0, 20, 20, tex, 16+15);
		//settingsButton = new GuiButton(2, x, 0, 64, 20, "Settings");
		settingsButton = new GuiButtonL ("settings", x, 0, 64, 20, /*tex, 32+15,*/ "Settings");

		setY(y);
		setX(x);
	}

	/**
	 * @param i
	 * @param j
	 * @param blocktunes
	 * @param b
	 * @param c
	 */
	public TuneTileGui(int x, int y, TuneTileType type, boolean on,
			boolean settingsButton) {
		this(x, y, type, on);
		hasSettingsButton = settingsButton;
		if (!hasSettingsButton) {
			this.settingsButton = null;
		}
	}

	public void draw(Minecraft mc, int mx, int my) {
		int tex = Minecraft.getMinecraft().renderEngine
				.getTexture("/com/minetunes/resources/textures/mineTunesLogo.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0f);
		Minecraft.getMinecraft().renderEngine.bindTexture(tex);
		drawTexturedModalRect(x, y, getUForType(type), 64, 64, 64);
		if (on) {
			GL11.glColor4f(0, 1.0F, 0, 1.0f);
		} else {
			GL11.glColor4f(1.0f, 0, 0, 1.0f);
		}
		drawTexturedModalRect(x, y, 0, 128, 64, 64);
		if (isMouseOver(mx, my)) {
			drawRect(x, y, x + 64, y + 64, 0x20b0b0ff);
		}

		helpButton.drawButton(mc, mx, my);
		if (settingsButton != null) {
			settingsButton.drawButton(mc, mx, my);
		}
	}

	private static int getUForType(TuneTileType t) {
		switch (t) {
		case NOTEBLOCKS:
			return 64 * 0;
		case BLOCKTUNES:
			return 64 * 1;
		case SIGNTUNES:
			return 64 * 2;
		case BOOKTUNES:
			return 64 * 3;
		}
		return 0;
	}

	public void addActionListener(ActionListener l) {
		listeners.add(l);
	}

	public void removeActionListener(ActionListener l) {
		listeners.remove(l);
	}

	private void fireActionEvent(ActionEvent e) {
		if (e.getID() != 0) {
			Minecraft.getMinecraft().sndManager.playSoundFX("random.click",
					1.0F, 1.0F);
		} else {
			float pitch = 1.0f;
			if (on) {
				pitch /= 2;
			}
			Minecraft.getMinecraft().sndManager.playSoundFX("note.harp", 1.0F,
					pitch);
		}
		for (ActionListener l : listeners) {
			l.actionPerformed(e);
		}
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
		helpButton.yPosition = y - 23;
		if (settingsButton != null) {
			settingsButton.yPosition = y + 64 + 3;
		}
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
	}

	/**
	 * @param x2
	 * @param y2
	 * @param button
	 */
	public void mouseClicked(int mx, int my, int button) {
		if (isMouseOver(mx, my) && button == 0) {
			fireActionEvent(new ActionEvent(this, 0, ""));
		}

		if (helpButton.mousePressed(Minecraft.getMinecraft(), mx, my)) {
			fireActionEvent(new ActionEvent(helpButton, 1, ""));
		}

		if (settingsButton != null
				&& settingsButton
						.mousePressed(Minecraft.getMinecraft(), mx, my)) {
			fireActionEvent(new ActionEvent(settingsButton, 2, ""));
		}
	}

	private boolean isMouseOver(int mx, int my) {
		if (mx >= x && mx <= x + 64) {
			if (my >= y && my <= y + 64) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param par1
	 * @param par2
	 * @param par3
	 */
	public void mouseMovedOrUp(int mx, int my, int button) {
		mouseOver = isMouseOver(mx, my);
	}

}
