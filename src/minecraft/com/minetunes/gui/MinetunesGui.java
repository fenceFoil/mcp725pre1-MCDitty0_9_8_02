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

import java.io.File;
import java.io.IOException;
import java.net.URI;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EnumOS;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.minetunes.Finder;
import com.minetunes.Minetunes;
import com.minetunes.autoUpdate.TutorialWorldUpdater;
import com.minetunes.config.MinetunesConfig;

public class MinetunesGui extends GuiScreen {

	private static boolean outdated;
	private static boolean tutorialUpdated = false;
	private GuiButton turnedOffButton;

	public MinetunesGui() {
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		drawDefaultBackground();


		int bgTextureNumber = Minecraft.getMinecraft().renderEngine
				.getTexture("/com/minetunes/resources/textures/signBG2.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5f);
		Minecraft.getMinecraft().renderEngine.bindTexture(bgTextureNumber);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5f);
		drawTexturedModalRect(width / 2 - 60, 20, 0, 0, 120, 50);

		drawCenteredString(fontRenderer, "MineTunes Menu", width / 2, 25,
				0xccccccff);

		// If outdated, note this
		if (outdated) {
			drawCenteredString(fontRenderer, "Update Available!", width / 2,
					40, 0x00ff00);
		}

		if (tutorialUpdated) {
			drawCenteredString(
					fontRenderer,
					"MineTunesLand: "
							+ Minetunes.tutorialUpdater
									.getLatestVersion(MinetunesConfig.MC_CURRENT_VERSION),
					width / 2, 55, 0x00ff00);
		}

		super.drawScreen(par1, par2, par3);
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		if (par2 == Keyboard.KEY_ESCAPE) {
			mc.displayGuiScreen(null);
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int button) {

		super.mouseClicked(x, y, button);
	}

	@Override
	protected void mouseMovedOrUp(int par1, int par2, int par3) {
		// TODO Auto-generated method stub
		super.mouseMovedOrUp(par1, par2, par3);
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id == 100) {
			// Check for updates
			mc.displayGuiScreen(new MinetunesUpdateGui());
		} else if (guibutton.id == 200) {
			mc.displayGuiScreen(new KeysGui());
		} else if (guibutton.id == 300) {
			mc.displayGuiScreen(new SettingsGui());
		} else if (guibutton.id == 400) {
			// Exit
			mc.displayGuiScreen(null);
		} else if (guibutton.id == 500) {
			// Open midi folder
			openMidiFolder();
		} else if (guibutton.id == 600) {
			// Open sound check
			mc.displayGuiScreen(new SoundTestGui(this));
		} else if (guibutton.id == 700) {
			// Open graphics
			mc.displayGuiScreen(new GraphicsGui(this));
		} else if (guibutton.id == 800) {
			// Tutorial world menu
			mc.displayGuiScreen(new TutorialGui(this));
		} else if (guibutton.id == 900) {
			// Soundbank selection
			mc.displayGuiScreen(new SoundfontGui(this));
		} else if (guibutton.id == 1000) {
			// Toggle MineTunes on
			MinetunesConfig.incrementMCDittyOffState();
			turnedOffButton.displayString = MinetunesConfig
					.getMCDittyTurnedOffText();
			try {
				MinetunesConfig.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void openMidiFolder() {
		String fileLocation = (new File(MinetunesConfig.getMinetunesDir()
				.getPath(), "midi")).getAbsolutePath();
		new File(MinetunesConfig.getMinetunesDir().getPath(), "midi").mkdirs();

		// (Code from GuiTexturePacks)
		if (Minecraft.getOs() == EnumOS.MACOS) {
			try {
				System.out.println(fileLocation);
				Runtime.getRuntime().exec(
						new String[] { "/usr/bin/open", fileLocation });
				return;
			} catch (IOException var7) {
				var7.printStackTrace();
			}
		} else if (Minecraft.getOs() == EnumOS.WINDOWS) {
			String var2 = String.format(
					"cmd.exe /C start \"Open file\" \"%s\"",
					new Object[] { fileLocation });

			try {
				Runtime.getRuntime().exec(var2);
				return;
			} catch (IOException var6) {
				var6.printStackTrace();
			}
		}

		boolean useSys = false;

		try {
			Class var3 = Class.forName("java.awt.Desktop");
			Object var4 = var3.getMethod("getDesktop", new Class[0]).invoke(
					(Object) null, new Object[0]);
			var3.getMethod("browse", new Class[] { URI.class }).invoke(
					var4,
					new File(MinetunesConfig.getMinetunesDir(), "midi").toURI());
		} catch (Throwable var5) {
			var5.printStackTrace();
			useSys = true;
		}

		if (useSys) {
			System.out.println("Opening via system class!");
			Sys.openURL((new StringBuilder()).append("file://")
					.append(fileLocation).toString());
		}
	}

	@Override
	public void initGui() {
		// Add buttons
		controlList.add(new GuiButton(400, width / 3 * 2 - 55, height - 40,
				110, 20, "§aEXIT"));
		controlList.add(new GuiButton(100, width / 3 - 55, height - 70, 110,
				20, "Auto-Update"));
		controlList.add(new GuiButton(200, width / 3 - 55, height - 100, 110,
				20, "Keyboard"));
		controlList.add(new GuiButton(300, width / 3 - 55, height - 130, 110,
				20, "Settings"));

		controlList.add(new GuiButton(800, width / 2 - 55, height - 160, 110,
				20, "MineTunesLand"));

		controlList.add(new GuiButton(600, width / 3 * 2 - 55, height - 70,
				110, 20, "Sound Test"));
		controlList.add(new GuiButton(700, width / 3 * 2 - 55, height - 100,
				110, 20, "Graphics"));
		controlList.add(new GuiButton(500, width / 3 * 2 - 55, height - 130,
				110, 20, "MIDI Folder"));
		controlList.add(new GuiButton(900, width / 3 - 55, height - 40, 110,
				20, "SoundFonts"));

		turnedOffButton = new GuiButton(1000, 15, 15, 110, 20,
				MinetunesConfig.getMCDittyTurnedOffText());
		controlList.add(turnedOffButton);

		controlList.add(new MinetunesVersionGuiElement(100));

		// Check for updates
		// outdated = GuiMineTunesUpdates.checkForUpdates();
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				MinetunesGui.setOutdated(MinetunesUpdateGui.checkForUpdates());
				MinetunesGui.setTutorialUpdated(Minetunes.tutorialUpdater
						.checkForUpdates(MinetunesConfig.MC_CURRENT_VERSION));
			}

		});
		t.setName("MineTunes Menu Update Checker");
		t.start();

		// Check config file to see that it's up to date
		MinetunesConfig.loadAndUpdateSettings();
	}

	protected static void setOutdated(boolean checkForUpdates) {
		outdated = checkForUpdates;
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
	}

	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}

	public static boolean isTutorialUpdated() {
		return tutorialUpdated;
	}

	public static void setTutorialUpdated(boolean tutorialUpdated) {
		MinetunesGui.tutorialUpdated = tutorialUpdated;
	}
}
