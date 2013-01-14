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

import java.io.File;
import java.io.IOException;
import java.net.URI;

import net.minecraft.client.Minecraft;
import net.minecraft.src.BlockSign;
import net.minecraft.src.EnumOS;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiMainMenu;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.StatList;
import net.minecraft.src.WorldClient;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.wikispaces.mcditty.GetMinecraft;
import com.wikispaces.mcditty.TutorialWorldDownloader;
import com.wikispaces.mcditty.config.MCDittyConfig;

public class GuiMCDitty extends GuiScreen {

	private static boolean outdated;
	private static boolean tutorialUpdated = false;
	private GuiButton turnedOffButton;

	public GuiMCDitty() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#drawScreen(int, int, float)
	 */
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		drawDefaultBackground();

		// // Print version
		// drawString(fontRenderer,
		// "MCDitty Version " + BlockSign.CURRENT_VERSION, 0, 0, 0x444444);

		// Draw label at top of screen
		// drawRect(width / 2 - 60, 20, width / 2 + 60, 70, 0xaaaaaa88);

		int bgTextureNumber = GetMinecraft.instance().renderEngine
				.getTexture("/com/wikispaces/mcditty/resources/textures/signBG2.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5f);
		GetMinecraft.instance().renderEngine.bindTexture(bgTextureNumber);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5f);
		drawTexturedModalRect(width / 2 - 60, 20, 0, 0, 120, 50);

		drawCenteredString(fontRenderer, "MCDitty Menu", width / 2, 25,
				0xccccccff);

		// If outdated, note this
		if (outdated) {
			drawCenteredString(fontRenderer, "Update Available!",
					width / 2, 40, 0x00ff00);
		}

		if (tutorialUpdated) {
			drawCenteredString(
					fontRenderer,
					"MCDittyLand: "
							+ TutorialWorldDownloader
									.downloadExampleWorldVersion(MCDittyConfig.MC_CURRENT_VERSION),
					width / 2, 55, 0x00ff00);
		}

		super.drawScreen(par1, par2, par3);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#keyTyped(char, int)
	 */
	@Override
	protected void keyTyped(char par1, int par2) {
		if (par2 == Keyboard.KEY_ESCAPE) {
			mc.displayGuiScreen(null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#mouseClicked(int, int, int) x, y, button
	 */
	@Override
	protected void mouseClicked(int x, int y, int button) {

		super.mouseClicked(x, y, button);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#mouseMovedOrUp(int, int, int)
	 */
	@Override
	protected void mouseMovedOrUp(int par1, int par2, int par3) {
		// TODO Auto-generated method stub
		super.mouseMovedOrUp(par1, par2, par3);
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
			// Check for updates
			mc.displayGuiScreen(new GuiMCDittyUpdates());
		} else if (guibutton.id == 200) {
			mc.displayGuiScreen(new GuiMCDittyKeys());
		} else if (guibutton.id == 300) {
			mc.displayGuiScreen(new GuiMCDittySettings());
		} else if (guibutton.id == 400) {
			// Exit
			mc.displayGuiScreen(null);
		} else if (guibutton.id == 500) {
			// Open midi folder
			openMidiFolder();
		} else if (guibutton.id == 600) {
			// Open sound check
			mc.displayGuiScreen(new GuiMCDittySoundTest(this));
		} else if (guibutton.id == 700) {
			// Open graphics
			mc.displayGuiScreen(new GuiMCDittyGraphics(this));
		} else if (guibutton.id == 800) {
			// Tutorial world menu
			mc.displayGuiScreen(new GuiMCDittyTutorial(this));
		} else if (guibutton.id == 900) {
			// Soundbank selection
			mc.displayGuiScreen(new GuiMCDittySoundfont(this));
		} else if (guibutton.id == 1000) {
			// Toggle mcditty on
			MCDittyConfig.incrementMCDittyOffState();
			turnedOffButton.displayString = MCDittyConfig.getMCDittyTurnedOffText();
			try {
				MCDittyConfig.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// New method uses code from MC 1.3.2's texture pack folder opener
	// private void openMidiFolderOld() {
	// // (Code from GuiTexturePacks)
	// String fileLocation = (new File(Minecraft.getMinecraftDir()
	// + "/MCDitty", "midi")).getAbsolutePath();
	// new File(Minecraft.getMinecraftDir() + "/MCDitty", "midi").mkdirs();
	//
	// // (Code from GuiTexturePacks)
	// boolean useSys = false;
	// try {
	// Class class1 = Class.forName("java.awt.Desktop");
	// Object obj = class1.getMethod("getDesktop", new Class[0]).invoke(
	// null, new Object[0]);
	// class1.getMethod("browse", new Class[] { java.net.URI.class })
	// .invoke(obj,
	// new Object[] { (new File(Minecraft
	// .getMinecraftDir().toString() + "/MCDitty",
	// "midi")).toURI() });
	// } catch (Throwable throwable) {
	// throwable.printStackTrace();
	// useSys = true;
	// }
	//
	// if (useSys) {
	// System.out.println("Opening via Sys class!");
	// Sys.openURL((new StringBuilder()).append("file://")
	// .append(fileLocation).toString());
	// }
	// }

	private void openMidiFolder() {
		String fileLocation = (new File(Minecraft.getMinecraftDir()
				+ "/MCDitty", "midi")).getAbsolutePath();
		new File(Minecraft.getMinecraftDir() + "/MCDitty", "midi").mkdirs();

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
					new Object[] { (new File(Minecraft.getMinecraftDir()
							.toString() + "/MCDitty", "midi")).toURI() });
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#initGui()
	 */
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
				20, "MCDittyLand"));

		controlList.add(new GuiButton(600, width / 3 * 2 - 55, height - 70,
				110, 20, "Sound Test"));
		controlList.add(new GuiButton(700, width / 3 * 2 - 55, height - 100,
				110, 20, "Graphics"));
		controlList.add(new GuiButton(500, width / 3 * 2 - 55, height - 130,
				110, 20, "MIDI Folder"));
		controlList.add(new GuiButton(900, width / 3 - 55, height - 40, 110,
				20, "SoundFonts"));

		turnedOffButton = new GuiButton(1000, 15, 15, 110, 20,
				MCDittyConfig.getMCDittyTurnedOffText());
		controlList.add(turnedOffButton);

		controlList.add(new MCDittyVersionReadoutGuiElement(100));

		// Check for updates
		// outdated = GuiMCDittyUpdates.checkForUpdates();
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				GuiMCDitty.setOutdated(GuiMCDittyUpdates.checkForUpdates());
				GuiMCDitty.setTutorialUpdated(TutorialWorldDownloader
						.checkForUpdates());
			}

		});
		t.setName("MCDitty Menu Update Checker");
		t.start();

		// Win achievement for opening the main menu
		// mc.field_71439_g.addStat(MCDitty.menuAchievement, 1);

		// Check config file to see that it's up to date
		MCDittyConfig.checkConfig(mc.theWorld);
	}

	protected static void setOutdated(boolean checkForUpdates) {
		outdated = checkForUpdates;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#onGuiClosed()
	 */
	@Override
	public void onGuiClosed() {
		// TODO Auto-generated method stub
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

	public static boolean isTutorialUpdated() {
		return tutorialUpdated;
	}

	public static void setTutorialUpdated(boolean tutorialUpdated) {
		GuiMCDitty.tutorialUpdated = tutorialUpdated;
	}
}
