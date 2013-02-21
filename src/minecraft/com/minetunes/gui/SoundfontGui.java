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

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.IOException;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.TileEntitySignRenderer;

import org.lwjgl.input.Keyboard;

import com.minetunes.config.MinetunesConfig;

/**
 * 
 */
public class SoundfontGui extends GuiScreen {

	private GuiScreen backGui;
	private GuiButton renderToggleTempButton;
	protected static String loadMessage = "";

	public SoundfontGui(GuiScreen backScreen) {
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
				height - 90, "Load SoundFont");
		controlList.add(renderToggleTempButton);
		controlList.add(new GuiButton(-200, width / 2 - 100, height - 60,
				"Restore Default"));
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
		drawCenteredString(fontRenderer, "Custom SoundFont", width / 2, 15,
				0xffff00);

		drawCenteredString(
				fontRenderer,
				"A custom SoundFont specifies different MIDI instrument sounds.",
				width / 2, 45, 0xffffff);
		drawCenteredString(fontRenderer,
				"Most SoundFonts ending in '.sf2' are supported by MineTunes.",
				width / 2, 60, 0xffffff);

		if (MinetunesConfig.customSF2.isSF2Loaded()) {
			drawCenteredString(fontRenderer, "Current SoundFont: "
					+ MinetunesConfig.customSF2.getCachedSoundbank().getName(),
					width / 2, 90, 0xaaffaa);
			drawCenteredString(fontRenderer, "Instruments in SoundFont: "
					+ MinetunesConfig.customSF2.getCachedSoundbank()
							.getInstruments().length, width / 2, 105, 0xaaaaff);
		} else {
			drawCenteredString(fontRenderer,
					"Current SoundFont: System Default", width / 2, 90,
					0xffffff);
		}

		drawCenteredString(fontRenderer, loadMessage, width / 2, 130, 0xaaaaff);

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
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					FileDialog d = new FileDialog((Frame) null,
							"Select a soundfont:", FileDialog.LOAD);
					d.show();
					String filename = d.getFile();
					if (filename == null) {
						return;
					} else {
						loadMessage = "Loading. . .";
						boolean successful = MinetunesConfig.customSF2
								.loadAndCacheSF2(d.getDirectory() + filename);
						if (successful) {
							loadMessage = "Loaded SoundFont from "
									+ MinetunesConfig.customSF2.getFilename();
						} else {
							loadMessage = MinetunesConfig.customSF2.getFilename()
									+ " is not a valid SoundFont2 file.";
						}
						try {
							MinetunesConfig.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
			t.setName("MineTunes SoundFont Loader");
			t.start();
		} else if (guibutton.id == -200) {
			// unload sound font
			MinetunesConfig.customSF2.unload();
			try {
				MinetunesConfig.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			loadMessage = "Restored defaults";
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
		return true;
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
