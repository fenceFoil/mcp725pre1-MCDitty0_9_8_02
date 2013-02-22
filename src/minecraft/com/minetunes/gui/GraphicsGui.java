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

import java.io.IOException;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;

import org.lwjgl.input.Keyboard;

import com.minetunes.config.MinetunesConfig;
import com.minetunes.signs.TileEntitySignRendererMinetunes;

/**
 * 
 */
public class GraphicsGui extends GuiScreen {

	private GuiScreen backGui;
	private GuiButton renderToggleTempButton;

	public GraphicsGui(GuiScreen backScreen) {
		backGui = backScreen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#initGui()
	 */
	@Override
	public void initGui() {
		controlList
				.add(new GuiButton(100, width / 2 - 100, height - 30, "Done"));
		controlList.add(new MinetunesVersionGuiElement(100));

		renderToggleTempButton = new GuiButton(-100, width / 2 - 100,
				height - 70, "Render Toggle");
		controlList.add(renderToggleTempButton);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#drawScreen(int, int, float)
	 */
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		drawDefaultBackground();

		// Draw label at top of screen
		drawCenteredString(fontRenderer, "MineTunes Graphics", width / 2, 15,
				0xffff00);

		drawCenteredString(
				fontRenderer,
				"Full Minecraft Signs: Renders every single sign and its text within about 100 blocks.",
				width / 2, 30, 0xffffff);
		drawCenteredString(
				fontRenderer,
				"Fast Signs: Renders only signs within 64 blocks, and text within 16 blocks; ",
				width / 2, 60, 0xffffff);
		drawCenteredString(fontRenderer,
				"Doesn't render signs behind your back;", width / 2, 70,
				0xffffff);
		drawCenteredString(
				fontRenderer,
				"Doesn't render text on hidden sides of signs or wall signs on other sides of walls.",
				width / 2, 80, 0xffffff);

		if (TileEntitySignRendererMinetunes.areSignsCurrentlyBeingRendered()) {
			drawCenteredString(fontRenderer, "Frame Counter: "
					+ TileEntitySignRendererMinetunes.fpsCounter, width / 2,
					130, 0xaaaaff);
			drawCenteredString(fontRenderer, "FPS: "
					+ TileEntitySignRendererMinetunes.currentFPS, width / 2,
					145, 0xaaaaff);
			drawCenteredString(fontRenderer, "Sign Count: "
					+ TileEntitySignRendererMinetunes.renderCountLastTick,
					width / 2, 160, 0xaaaaff);
		} else {
			// drawCenteredString(fontRenderer,
			// "FPS: No signs nearby, or game is paused.", width / 2, 145,
			// 0xffaaaa);
		}

		if (MinetunesConfig.isFullRenderingEnabled()) {
			renderToggleTempButton.displayString = "Full Minecraft Signs";
		} else {
			renderToggleTempButton.displayString = "Fast Signs";
		}

		super.drawScreen(par1, par2, par3);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.minecraft.src.GuiScreen#actionPerformed(net.minecraft.src.GuiButton)
	 */
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id == 100) {
			// Go back
			mc.displayGuiScreen(backGui);
		} else if (guibutton.id == -100) {
			// toggle full render
			MinetunesConfig.setFullRenderingEnabled(!MinetunesConfig
					.isFullRenderingEnabled());
			try {
				MinetunesConfig.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#updateScreen()
	 */
	@Override
	public void updateScreen() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#onGuiClosed()
	 */
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#doesGuiPauseGame()
	 */
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#mouseClicked(int, int, int)
	 */
	@Override
	protected void mouseClicked(int x, int y, int button) {
		super.mouseClicked(x, y, button);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#keyTyped(char, int)
	 */
	@Override
	protected void keyTyped(char par1, int par2) {
		if (par2 == Keyboard.KEY_ESCAPE) {
			mc.displayGuiScreen(backGui);
		}
		// super.keyTyped(par1, par2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#mouseMovedOrUp(int, int, int)
	 */
	@Override
	protected void mouseMovedOrUp(int par1, int par2, int par3) {
		// textPanel.mouseMovedOrUp(par1, par2, par3);
		super.mouseMovedOrUp(par1, par2, par3);
	}

}
