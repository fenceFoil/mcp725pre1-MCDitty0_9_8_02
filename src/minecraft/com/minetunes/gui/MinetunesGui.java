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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EnumOS;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.Timer;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Linear;
import aurelienribon.tweenengine.equations.Quart;

import com.minetunes.Finder;
import com.minetunes.Minetunes;
import com.minetunes.autoUpdate.FileUpdater;
import com.minetunes.config.MinetunesConfig;
import com.minetunes.gui.help.GuiHelpTopics;
import com.minetunes.resources.UpdateResourcesThread;

public class MinetunesGui extends GuiScreen {

	private static final int TILE_RETRACT_Y = -100;
	private static boolean outdated;
	private static boolean tutorialUpdated = false;
	private GuiButton turnedOffButton;
	private GuiButton autoUpdateButton;
	private GuiButton tutorialButton;
	private GuiButton cancelTutorialButton;
	private LinkedList<TuneTileGui> tiles = new LinkedList<TuneTileGui>();
	private String typedKeys = "";
	private boolean showingTiles = true;
	private GuiButton graphicsButton;
	private GuiButton soundfontsButton;
	private GuiButton midiButton;

	public MinetunesGui() {
	}

	@Override
	public void drawScreen(int mx, int my, float par3) {
		updateTweens();

		// draw background
		drawGradientRect(0, 0, width, height / 2, 0x40111111, 0xa0ffffff);
		drawGradientRect(0, height / 2, width, height, 0xa0ffffff, 0xff101010);

		// Draw logo
		int bgTextureNumber = Minecraft.getMinecraft().renderEngine
				.getTexture("/com/minetunes/resources/textures/mineTunesLogo.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5f);
		Minecraft.getMinecraft().renderEngine.bindTexture(bgTextureNumber);
		// drawTexturedModalRect(width / 2 - 64, 10, 100, 0, 128, 64);
		drawRect(width / 2 - 55, 5, width / 2 + 55, 55, 0xddffffff);
		drawTexturedModalRect(width / 2 - 50, 10, 0, 0, 100, 36);

		// Update buttons
		updateButtonLabels();
		addTutorialCancel();

		// Update tiles
		for (TuneTileGui t : tiles) {
			t.draw(mc, mx, my);
		}

		// Draw buttons
		super.drawScreen(mx, my, par3);
	}

	/**
	 * 
	 */
	private void updateButtonLabels() {
		autoUpdateButton.displayString = "Updates";
		if (outdated) {
			autoUpdateButton.displayString = "§aUpdate: "
					+ Minetunes.autoUpdater
							.getLatestVersion(MinetunesConfig.MC_CURRENT_VERSION);
		}

		tutorialButton.displayString = "MCDittyLand";
		if (tutorialUpdated) {
			tutorialButton.displayString = "§aNew: "
					+ Minetunes.tutorialUpdater
							.getLatestVersion(MinetunesConfig.MC_CURRENT_VERSION);
		}
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		if (par2 == Keyboard.KEY_ESCAPE) {
			mc.displayGuiScreen(null);
		}

		// Check for combos
		typedKeys += par1;
		if (typedKeys.toLowerCase().endsWith("resources")) {
			// Re-download resources
			Minecraft.getMinecraft().sndManager.playSoundFX("note.snare", 1.0F,
					1.0f);
			MinetunesConfig.setInt("resources.lastZipDownloaded", -1);
			UpdateResourcesThread t = new UpdateResourcesThread();
			t.start();
		} else if (typedKeys.toLowerCase().endsWith("debug")) {
			// Toggle debug
			MinetunesConfig.DEBUG = !MinetunesConfig.DEBUG;
			if (MinetunesConfig.DEBUG) {
				Minecraft.getMinecraft().sndManager.playSoundFX("note.pling",
						1.0F, 2.0f);
			} else {
				Minecraft.getMinecraft().sndManager.playSoundFX("note.bass",
						1.0F, 0.5f);
			}
		} else if (typedKeys.toLowerCase().endsWith("clearcache")) {
			// Clear cache for updating things
			Minecraft.getMinecraft().sndManager.playSoundFX("note.snare", 1.0F,
					1.0f);
			FileUpdater.clearStaticCache();
			Minetunes.autoUpdater.clearCache();
			Minetunes.tutorialUpdater.clearCache();
		} else if (typedKeys.toLowerCase().endsWith("getdev")) {
			// Try to look for dev versions of minetunes on auto-update
			Minecraft.getMinecraft().sndManager.playSoundFX("note.snare", 1.0F,
					1.0f);
			Minetunes.autoUpdater.setSpecialMode("dev");
			checkForUpdates();
		} else if (typedKeys.toLowerCase().endsWith("gettest")) {
			// Try to look for test versions of minetunes on auto-update
			Minecraft.getMinecraft().sndManager.playSoundFX("note.snare", 1.0F,
					1.0f);
			Minetunes.autoUpdater.setSpecialMode("test");
			checkForUpdates();
		} else if (typedKeys.toLowerCase().endsWith("shutupandautoupdate")) {
			// Try to look for test versions of minetunes on auto-update
			Minecraft.getMinecraft().sndManager.playSoundFX("note.snare", 1.0F,
					1.0f);
			Minetunes.autoUpdater.clearAlreadyTriedFlag();
		} else if (typedKeys.toLowerCase().endsWith("graphics")) {
			mc.displayGuiScreen(new GraphicsGui(this));
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int button) {
		super.mouseClicked(x, y, button);

		for (TuneTileGui t : tiles) {
			t.mouseClicked(x, y, button);
		}
	}

	@Override
	protected void mouseMovedOrUp(int par1, int par2, int par3) {
		super.mouseMovedOrUp(par1, par2, par3);

		for (TuneTileGui t : tiles) {
			t.mouseMovedOrUp(par1, par2, par3);
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id == 100) {
			// Check for updates
			mc.displayGuiScreen(new MinetunesUpdateGui());
		} else if (guibutton.id == 200) {
			mc.displayGuiScreen(new KeysGui());
		} else if (guibutton.id == 300) {
			mc.displayGuiScreen(new OldSettingsGui());
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
		} else if (guibutton.id == 1100) {
			// Reset tutorial updated button
			MinetunesConfig
					.setString(
							"tutorial.lastDownload",
							Minetunes.tutorialUpdater
									.getLatestVersion(MinetunesConfig.MC_CURRENT_VERSION));
			tutorialUpdated = false;
			try {
				MinetunesConfig.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (guibutton.id == 2000) {
			// Switch out the center
			showingTiles = !showingTiles;
			tweenTiles(showingTiles);
			tweenButtons(!showingTiles);
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
			var3.getMethod("browse", new Class[] { URI.class })
					.invoke(var4,
							new File(MinetunesConfig.getMinetunesDir(), "midi")
									.toURI());
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
		// controlList.add(new GuiButton(400, width / 3 * 2 - 55, height - 40,
		// 110, 20, "§aEXIT"));

		// controlList.add(new GuiButton(300, width / 3 - 55, height - 130, 110,
		// 20, "Settings"));

		controlList.add(new GuiButton(200, width / 6 - 55, height - 30, 110,
				20, "Keyboard"));
		autoUpdateButton = new GuiButton(100, width / 2 - 55, height - 30, 110,
				20, "Auto-Update");
		controlList.add(autoUpdateButton);
		tutorialButton = new GuiButton(800, width / 6 * 5 - 55, height - 30,
				100, 20, "MineTunesLand");
		controlList.add(tutorialButton);

		controlList.add(new GuiButton(2000, width - 85, 5, 80, 20, "Switch"));

		addTutorialCancel();

		// controlList.add(new GuiButton(600, width / 3 * 2 - 55, height - 70,
		// 110, 20, "Sound Test"));
		graphicsButton = new GuiButton(700, width / 6 * 3 - 55, TILE_RETRACT_Y,
				110, 20, "Graphics");
		controlList.add(graphicsButton);
		midiButton = new GuiButton(500, width / 6 * 5 - 55, TILE_RETRACT_Y,
				 110, 20, "MIDI Folder");
		 controlList.add(midiButton);
		soundfontsButton = new GuiButton(900, width / 6 - 55, TILE_RETRACT_Y,
				110, 20, "SoundFonts");
		controlList.add(soundfontsButton);
		// turnedOffButton = new GuiButton(1000, 15, 15, 110, 20,
		// MinetunesConfig.getMCDittyTurnedOffText());
		// controlList.add(turnedOffButton);

		controlList.add(new MinetunesVersionGuiElement(100));

		// Check for updates
		// outdated = GuiMineTunesUpdates.checkForUpdates();
		checkForUpdates();

		// Add tiles
		tiles.clear();

		final MinetunesGui thisGui = this;

		final TuneTileGui noteblockTile = new TuneTileGui(width / 4 - 32,
				TILE_RETRACT_Y, TuneTileType.NOTEBLOCKS,
				!MinetunesConfig.getBoolean("noteblock.signsDisabled"), false);
		noteblockTile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				switch (e.getID()) {
				case 0:
					// Tile pressed
					noteblockTile.setOn(!MinetunesConfig
							.toggleBoolean("noteblock.signsDisabled"));
					try {
						MinetunesConfig.flush();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					return;
				case 1:
					// Help pressed
					mc.displayGuiScreen(new GuiHelpTopics("noteblocks", thisGui));
					return;
				case 2:
					// Settings pressed
					return;
				}
			}
		});
		Tween.to(noteblockTile, TuneTileGuiTweenAccessor.TWEEN_TYPE_Y, 2000)
				.target((float) (height / 2 - 32 + 30)).ease(Quart.OUT)
				.start(tweenManager);
		tiles.add(noteblockTile);

		final TuneTileGui blockTunesTile = new TuneTileGui(width / 2 - 32,
				TILE_RETRACT_Y, TuneTileType.BLOCKTUNES,
				!MinetunesConfig.getBoolean("blockTunes.disabled"), false);
		blockTunesTile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				switch (e.getID()) {
				case 0:
					// Tile pressed
					blockTunesTile.setOn(!MinetunesConfig
							.toggleBoolean("blockTunes.disabled"));
					try {
						MinetunesConfig.flush();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					return;
				case 1:
					// Help pressed
					mc.displayGuiScreen(new GuiHelpTopics("blocktunes", thisGui));
					return;
				case 2:
					// Settings pressed
					return;
				}
			}
		});
		Tween.to(blockTunesTile, TuneTileGuiTweenAccessor.TWEEN_TYPE_Y, 2000)
				.target((float) (height / 2 - 32 + 0)).ease(Quart.OUT)
				.start(tweenManager);
		tiles.add(blockTunesTile);

		final TuneTileGui signTunesTile = new TuneTileGui(width / 4 * 3 - 32,
				TILE_RETRACT_Y, TuneTileType.SIGNTUNES,
				!MinetunesConfig.getBoolean("signs.disabled"));
		signTunesTile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				switch (e.getID()) {
				case 0:
					// Tile pressed
					signTunesTile.setOn(!MinetunesConfig
							.toggleBoolean("signs.disabled"));
					if (MinetunesConfig.getBoolean("signs.disabled")) {
						// mute signs when disabling
						Minetunes.mutePlayingDitties();
					}
					try {
						MinetunesConfig.flush();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					return;
				case 1:
					// Help pressed
					mc.displayGuiScreen(new GuiHelpTopics("signtunes", thisGui));
					return;
				case 2:
					// Settings pressed
					mc.displayGuiScreen(new OldSettingsGui());
					return;
				}
			}
		});
		Tween.to(signTunesTile, TuneTileGuiTweenAccessor.TWEEN_TYPE_Y, 2000)
				.target((float) (height / 2 - 32 - 30)).ease(Quart.OUT)
				.start(tweenManager);
		tiles.add(signTunesTile);

		tweenTiles(showingTiles);
		tweenButtons(!showingTiles);

		// Check config file to see that it's up to date
		MinetunesConfig.loadAndUpdateSettings();
	}

	/**
	 * 
	 */
	private void tweenTiles(boolean down) {
		int currTile = 0;
		for (TuneTileGui tile : tiles) {
			if (down) {
				Tween.to(tile, TuneTileGuiTweenAccessor.TWEEN_TYPE_Y, 2000)
						.target((float) (height / 2 - 32 - 60 + (30 * (3 - currTile))))
						.ease(Quart.OUT).start(tweenManager);
			} else {
				Tween.to(tile, TuneTileGuiTweenAccessor.TWEEN_TYPE_Y, 2000)
						.target((float) TILE_RETRACT_Y).ease(Quart.OUT)
						.start(tweenManager);
			}
			currTile++;
		}
	}

	private void tweenButtons(boolean down) {
		int currTile = 0;
		GuiButton[] buttons = new GuiButton[3];
		buttons[1] = graphicsButton;
		buttons[0] = soundfontsButton;
		buttons[2] = midiButton;
		for (GuiButton button : buttons) {
			if (down) {
				Tween.to(button, GuiButtonTweenAccessor.TWEEN_TYPE_Y, 2000).delay(300)
						.target((float) (height / 2 - 32 - 40 + (30 * (3 - currTile))))
						.ease(Linear.INOUT).start(tweenManager);
			} else {
				Tween.to(button, GuiButtonTweenAccessor.TWEEN_TYPE_Y, 500)
						.target((float) TILE_RETRACT_Y).ease(Linear.INOUT)
						.start(tweenManager);
			}
			currTile++;
		}
	}

	/**
	 * 
	 */
	private void checkForUpdates() {
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
	}

	/**
	 * 
	 */
	private void addTutorialCancel() {
		controlList.remove(cancelTutorialButton);
		cancelTutorialButton = null;
		if (tutorialUpdated) {
			cancelTutorialButton = new GuiButton(1100,
					tutorialButton.xPosition + 100 + 2,
					tutorialButton.yPosition, 20, 20, "§cX");
			controlList.add(cancelTutorialButton);
		}
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

	private TweenManager tweenManager = new TweenManager();
	private long lastTweenUpdateTime = System.currentTimeMillis();

	private void updateTweens() {
		Timer t = Finder.getMCTimer();
		tweenManager.update(System.currentTimeMillis() - lastTweenUpdateTime);
		lastTweenUpdateTime = System.currentTimeMillis();
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
	}
}
