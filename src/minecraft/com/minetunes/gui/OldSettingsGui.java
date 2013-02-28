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

import java.awt.Color;
import java.io.IOException;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiTextField;

import org.lwjgl.input.Keyboard;

import com.minetunes.Color4f;
import com.minetunes.config.MidiVolumeMode;
import com.minetunes.config.MinetunesConfig;
import com.minetunes.config.NoPlayTokens;

public class OldSettingsGui extends GuiScreen {
	private GuiButton particlesOnButton;
	// private GuiButton oneParticleButton;
	private GuiButton onlyFirstErrorButton;
	private GuiButton signBlinkMSButton;
	private GuiButton saveMIDIButton;
	// private GuiButton blinkSignsButton;
	private GuiButton highlightButton;
	private GuiButton lyricsEnabledButton;
	private GuiButton debugButton;
	private GuiTextField endOfLineTuneTextField;
	// private GuiTextField noPlayTokensTextField;
	private GuiButton proxPadsEnabledButton;
	private GuiSlider signColorSlider;
	private GuiButton useMCMusicVolumeButton;
	private GuiButton midiMessageButton;
	private GuiButton noPlayTokensButton;
	private GuiButton noteBlockTooltipsButton;

	public OldSettingsGui() {

	}

	public void updateButtons() {
		if (MinetunesConfig.noteParticlesDisabled) {
			particlesOnButton.displayString = "§aOne Particle";
		} else if (MinetunesConfig.particlesEnabled) {
			particlesOnButton.displayString = "§aAll Particles";
		} else {
			particlesOnButton.displayString = "§cNo Particles";
		}

		// if (BlockSign.emitOnlyOneParticle) {
		// oneParticleButton.displayString = "§aOne Particle";
		// } else {
		// oneParticleButton.displayString = "§cNo";
		// }

		if (MinetunesConfig.getBoolean("signs.showErrors")) {
			onlyFirstErrorButton.displayString = "§aShow SignTune Errors";
		} else {
			onlyFirstErrorButton.displayString = "§cHide SignTune Errors";
		}

		signBlinkMSButton.displayString = "Blink Errors: §a"
				+ (MinetunesConfig.getInt("signs.errorBlinkMS") / 1000)
				+ " sec";

		// saveMIDIButton.displayString = "Save Midi Files: ";
		if (MinetunesConfig.getBoolean("signs.saveMidiEnabled")) {
			saveMIDIButton.displayString = "§aSaveMIDI On";
		} else {
			saveMIDIButton.displayString = "§cSaveMIDI Off";
		}

		// blinkSignsButton.displayString = "Blink Signs Red? ";
		// if (MCDittyConfig.blinkSignsTexturesEnabled) {
		// blinkSignsButton.displayString = "§aBlink Error Signs";
		// } else {
		// blinkSignsButton.displayString = "§aBlink Error Text";
		// }

		highlightButton.displayString = "Highlighting: ";
		if (MinetunesConfig.highlightEnabled) {
			highlightButton.displayString += "§aYes";
		} else {
			highlightButton.displayString += "§cNo";
		}

		// lyricsEnabledButton.displayString = "Lyrics Enabled? ";
		if (MinetunesConfig.getBoolean("lyrics.enabled")) {
			lyricsEnabledButton.displayString = "§aLyrics On";
		} else {
			lyricsEnabledButton.displayString = "§cLyrics Off";
		}

		if (MinetunesConfig.getBoolean("signs.proximityEnabled")) {
			proxPadsEnabledButton.displayString = "§aProximity On";
		} else {
			proxPadsEnabledButton.displayString = "§cProximity Off";
		}

		debugButton.displayString = "Debug: ";
		if (MinetunesConfig.DEBUG) {
			debugButton.displayString += "§aYes";
		} else {
			debugButton.displayString += "§cNo";
		}

		// if (MCDittyConfig.useMCMusicVolume) {
		// useMCMusicVolumeButton.displayString = "§aUse Music Volume";
		// } else {
		// useMCMusicVolumeButton.displayString = "§aUse Sound Volume";
		// }
		MidiVolumeMode volumeMode = MinetunesConfig.getVolumeMode();
		if (volumeMode == MidiVolumeMode.MAX) {
			useMCMusicVolumeButton.displayString = "§aVolume: 100%";
		} else if (volumeMode == MidiVolumeMode.MC_MUSIC) {
			useMCMusicVolumeButton.displayString = "§aVolume: MC Music";
		} else if (volumeMode == MidiVolumeMode.MC_SOUND) {
			useMCMusicVolumeButton.displayString = "§aVolume: MC Sound";
		} else {
			// Unknown mode
		}

		if (MinetunesConfig.getBoolean("midiSavedMessage")) {
			midiMessageButton.displayString = "§aSaveMIDI Message On";
		} else {
			midiMessageButton.displayString = "§cSaveMIDI Message Off";
		}

		if (MinetunesConfig.getBoolean("enableNoteblockTooltips")) {
			noteBlockTooltipsButton.displayString = "§aNoteblock Tags On";
		} else {
			noteBlockTooltipsButton.displayString = "§cNoteblock Tags Off";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#initGui()
	 */
	@Override
	public void initGui() {
		int yOffset = 30;

		// Add buttons
		controlList.add(new GuiButton(100, width / 3, height - 30, width / 3,
				20, "Exit"));

		particlesOnButton = new GuiButton(200, (width / 3) - 60, 10 + yOffset,
				120, 20, "");
		// oneParticleButton = new GuiButton(300, (width / 3) - 60, 80, 120, 20,
		// "");
		saveMIDIButton = new GuiButton(500, (width / 3) - 60, 70 + yOffset,
				120, 20, "");
		lyricsEnabledButton = new GuiButton(900, (width / 3) - 60,
				40 + yOffset, 120, 20, "");
		proxPadsEnabledButton = new GuiButton(2000, (width / 3 - 60),
				130 + yOffset, 120, 20, "");

		onlyFirstErrorButton = new GuiButton(400, (width / 3) * 2 - 60,
				10 + yOffset, 120, 20, "");
		signBlinkMSButton = new GuiButton(800, (width / 3) * 2 - 60,
				40 + yOffset, 120, 20, "");
		// blinkSignsButton = new GuiButton(600, (width / 3) * 2 - 60,
		// 70+yOffset, 120,
		// 20, "");
		highlightButton = new GuiButton(700, (width / 3) * 2 - 60,
				100 + yOffset, 120, 20, "");

		useMCMusicVolumeButton = new GuiButton(3000, (width / 3) * 2 - 60,
				70 + yOffset, 120, 20, "");
		midiMessageButton = new GuiButton(3100, (width / 3) - 60,
				100 + yOffset, 120, 20, "");
		noPlayTokensButton = new GuiButton(3200, (width / 3) * 2 - 60,
				100 + yOffset, 120, 20, "Edit 'No Play' Signs");

		noteBlockTooltipsButton = new GuiButton(3300, (width / 3) * 2 - 60,
				130 + yOffset, 120, 20, "Noteblock Tooltips");

		debugButton = new GuiButton(10, 0, height - 30, 100 + yOffset, 20, "");

		signColorSlider = new GuiSlider(5000, (width / 2) - 60, 155 + yOffset,
				"Sign Highlight Color",
				MinetunesConfig.getFloat("signs.playingColor.sliderPos"));
		controlList.add(signColorSlider);

		controlList.add(particlesOnButton);
		// controlList.add(oneParticleButton);
		controlList.add(onlyFirstErrorButton);
		controlList.add(signBlinkMSButton);
		controlList.add(saveMIDIButton);
		// controlList.add(blinkSignsButton);
		controlList.add(highlightButton);
		controlList.add(lyricsEnabledButton);
		controlList.add(debugButton);
		controlList.add(proxPadsEnabledButton);
		controlList.add(useMCMusicVolumeButton);
		controlList.add(midiMessageButton);
		controlList.add(noPlayTokensButton);
		controlList.add(noteBlockTooltipsButton);

		controlList.add(new MinetunesVersionGuiElement(100));

		debugButton.drawButton = false;
		highlightButton.drawButton = false;

		// Add text fields
		// endOfLineTuneTextField = new GuiTextField(fontRenderer, (width / 3) *
		// 2 - 60, 170, 120, 20);
		// endOfLineTuneTextField.setFocused(false);
		// endOfLineTuneTextField.setText(BlockSign.getEndOfLineTune());
		// noPlayTokensTextField = new GuiTextField(fontRenderer, (width / 3) *
		// 2 - 60, 230, 120, 20);
		// noPlayTokensTextField.setFocused(false);
		// noPlayTokensTextField.setText(BlockSign.getNoPlayTokensString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#drawScreen(int, int, float)
	 */
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		drawDefaultBackground();
		updateButtons();

		// Print version
		// drawString(fontRenderer,
		// "MineTunes Version " + BlockSign.CURRENT_VERSION, 0, 0, 0x444444);

		// Draw label at top of screen
		drawCenteredString(fontRenderer, "MineTunes Settings", width / 2, 20,
				0x4444bb);

		// System.out.println(signColorSlider.sliderValue);
		// for (float f : currColor) {
		// System.out.println(": " + (int)f);
		// }

		// Draw text boxes
		// drawString(fontRenderer, "End Of Line Sound:", width / 3 - 60 + 10,
		// 170 + 5, 0xffffff);
		// endOfLineTuneTextField.drawTextBox();
		// noPlayTokensTextField.drawTextBox();

		super.drawScreen(par1, par2, par3);

		// Show slider color
		float[] currColor = sineBowColor((float) (signColorSlider.sliderValue * 2f * Math.PI));
		drawRect((int) ((float) signColorSlider.xPosition + 1),
				signColorSlider.yPosition + 1,
				(int) ((float) signColorSlider.xPosition) + 120 - 1,
				signColorSlider.yPosition + 20 - 1, new Color(
						currColor[0] / 255f, currColor[1] / 255f,
						currColor[2] / 255f, 0.5f).getRGB());
	}

	private char lastChar = ' ';

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#keyTyped(char, int)
	 */
	@Override
	protected void keyTyped(char par1, int par2) {
		if (par2 == Keyboard.KEY_ESCAPE) {
			mc.displayGuiScreen(null);
			return;
		}

		// String oldNoPlayTokens = noPlayTokensTextField.getText();
		// noPlayTokensTextField.textboxKeyTyped(par1, par2);
		// if (!noPlayTokensTextField.getText().equals(oldNoPlayTokens)) {
		// // If text is entered into the noPlay field
		// BlockSign.setNoPlayTokensString(noPlayTokensTextField.getText());
		// writeConfigFile();
		// }

		// String oldTune = endOfLineTuneTextField.getText();
		// endOfLineTuneTextField.textboxKeyTyped(par1, par2);
		// // Update endof line tune
		// if (!endOfLineTuneTextField.getText().equals(oldTune)) {
		// String tune = endOfLineTuneTextField.getText();
		// tune = tune.replace("§c", "");
		// MusicStringParser p = new MusicStringParser();
		// String[] tokens = tune.split(" ");
		// boolean goodTune = true;
		// for (String t : tokens) {
		// try {
		// p.parseTokenStrict(t);
		// } catch (Exception e) {
		// e.printStackTrace();
		// goodTune = false;
		// break;
		// }
		// }
		//
		// if (goodTune) {
		// BlockSign.playMusicString(tune + " Rw");
		// BlockSign.setEndOfLineTune(tune);
		// try {
		// BlockSign.writeConfigFile();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// if (endOfLineTuneTextField.getText().startsWith("§c")) {
		// endOfLineTuneTextField.setText(endOfLineTuneTextField.getText().replace("§c",
		// ""));
		// }
		// } else {
		// if (!endOfLineTuneTextField.getText().startsWith("§c")) {
		// endOfLineTuneTextField.setText("§c" +
		// endOfLineTuneTextField.getText());
		// }
		// }
		// }
		// if (endOfLineTuneTextField.getText().length() <= 2 &&
		// endOfLineTuneTextField.getText().startsWith("§c")) {
		// endOfLineTuneTextField.setText(endOfLineTuneTextField.getText().replace("§c",
		// ""));
		// }

		// NOTE TO ANYBODY READING THE CODE:
		// THIS IS THE KIND OF THING YOU ALWAYS DREAM OF FINDING!!!
		// Unfortunately, this is a really really dull cheat code.
		// Turning on debug prints debug messages to the console, slowing the
		// game down.
		par1 = Character.toLowerCase(par1);
		if (par1 == 'd') {
			lastChar = 'd';
			System.out.print("d");
		} else if (par1 == 'e' && lastChar == 'd') {
			lastChar = 'e';
			System.out.print("e");
		} else if (par1 == 'b' && lastChar == 'e') {
			lastChar = 'b';
			System.out.print("b");
		} else if (par1 == 'u' && lastChar == 'b') {
			lastChar = 'u';
			System.out.print("u");
		} else if (par1 == 'g' && lastChar == 'u') {
			// DEBUG BUTTON ACTIVATE!
			System.out.println("g!");
			debugButton.drawButton = true;
			highlightButton.drawButton = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#mouseClicked(int, int, int)
	 */
	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		// endOfLineTuneTextField.mouseClicked(par1, par2, par3);
		// noPlayTokensTextField.mouseClicked(par1, par2, par3);
		super.mouseClicked(par1, par2, par3);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#mouseMovedOrUp(int, int, int)
	 */
	@Override
	protected void mouseMovedOrUp(int par1, int par2, int par3) {
		// TODO Auto-generated method stub
		boolean draggingSignColorSlider = signColorSlider.dragging;
		super.mouseMovedOrUp(par1, par2, par3);
		if (draggingSignColorSlider && signColorSlider.dragging == false) {
			// Color was selected
			MinetunesConfig
					.setString(
							"signs.playingColor",
							new Color4f(
									sineBowColor((float) (signColorSlider.sliderValue * 2 * Math.PI)))
									.toString());
			MinetunesConfig.setFloat("signs.playingColor.sliderPos",
					signColorSlider.sliderValue);
			flushConfigFile();
		}
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
			// Exit screen
			mc.displayGuiScreen(new MinetunesGui(null));
		} else if (guibutton.id == 200) {
			// Particles on button

			// Cycle options
			if (MinetunesConfig.particlesEnabled
					&& MinetunesConfig.noteParticlesDisabled) {
				// Disable
				MinetunesConfig.particlesEnabled = false;
				MinetunesConfig.noteParticlesDisabled = false;
			} else if (MinetunesConfig.particlesEnabled
					&& !MinetunesConfig.noteParticlesDisabled) {
				// One Particle
				MinetunesConfig.noteParticlesDisabled = true;
			} else {
				// Particles on
				MinetunesConfig.particlesEnabled = true;
				MinetunesConfig.noteParticlesDisabled = false;
			}

			flushConfigFile();
			// } else if (guibutton.id == 300) {
			// // One particle
			// BlockSign.emitOnlyOneParticle = !BlockSign.emitOnlyOneParticle;
			// writeConfigFile();
		} else if (guibutton.id == 400) {
			// Toggle errors shown
			MinetunesConfig.setBoolean("signs.showErrors",
					!MinetunesConfig.getBoolean("signs.showErrors"));
			flushConfigFile();
		} else if (guibutton.id == 500) {
			// Save midis
			MinetunesConfig.setBoolean("signs.saveMidiEnabled",
					!MinetunesConfig.getBoolean("signs.saveMidiEnabled"));
			flushConfigFile();
		} else if (guibutton.id == 600) {
			// Blink signs
			MinetunesConfig.setBoolean("signs.errorBlinkRed",
					!MinetunesConfig.getBoolean("signs.errorBlinkRed"));
			flushConfigFile();
		} else if (guibutton.id == 700) {
			// Highlighting
			MinetunesConfig.highlightEnabled = !MinetunesConfig.highlightEnabled;
			flushConfigFile();
		} else if (guibutton.id == 800) {
			// Cycle blink sign times
			int time = MinetunesConfig.getInt("signs.errorBlinkMS");
			if (time == 0) {
				time = 2000;
			} else if (time == 2000) {
				time = 4000;
			} else if (time == 4000) {
				time = 6000;
			} else if (time == 6000) {
				time = 8000;
			} else if (time == 8000) {
				time = 10000;
			} else if (time == 10000) {
				time = 15000;
			} else if (time == 15000) {
				time = 30000;
			} else {
				time = 0;
			}
			MinetunesConfig.setInt("signs.errorBlinkMS", time);
			flushConfigFile();
		} else if (guibutton.id == 900) {
			// Lyrics enabled
			MinetunesConfig.setBoolean("lyrics.enabled",
					!MinetunesConfig.getBoolean("lyrics.enabled"));
			flushConfigFile();
		} else if (guibutton.id == 10) {
			// Debug enabled
			MinetunesConfig.DEBUG = !MinetunesConfig.DEBUG;
			flushConfigFile();
		} else if (guibutton.id == 2000) {
			// ProxPads enabled
			MinetunesConfig.setBoolean("signs.proximityEnabled",
					!MinetunesConfig.getBoolean("signs.proximityEnabled"));
			flushConfigFile();
		} else if (guibutton.id == 3000) {
			// Use Music volume enabled
			if (MinetunesConfig.getVolumeMode() == MidiVolumeMode.MAX) {
				MinetunesConfig.setVolumeMode(MidiVolumeMode.MC_SOUND);
			} else if (MinetunesConfig.getVolumeMode() == MidiVolumeMode.MC_SOUND) {
				MinetunesConfig.setVolumeMode(MidiVolumeMode.MC_MUSIC);
			} else if (MinetunesConfig.getVolumeMode() == MidiVolumeMode.MC_MUSIC) {
				MinetunesConfig.setVolumeMode(MidiVolumeMode.MAX);
			} else {
				// Unknown volume mode
				MinetunesConfig.setVolumeMode(MidiVolumeMode.MC_SOUND);
			}
			flushConfigFile();
		} else if (guibutton.id == 3100) {
			// Midi message
			MinetunesConfig.setBoolean("midiSavedMessage",
					!MinetunesConfig.getBoolean("midiSavedMessage"));
		} else if (guibutton.id == 3200) {
			// Edit no play tokens
			NoPlayTokens.editNoPlayTokens();
		} else if (guibutton.id == 3300) {
			boolean noteBlockTooltips = !MinetunesConfig
					.getBoolean("enableNoteblockTooltips");
			MinetunesConfig.setBoolean("enableNoteblockTooltips",
					noteBlockTooltips);
			flushConfigFile();
		}
	}

	private void flushConfigFile() {
		try {
			MinetunesConfig.flush();
		} catch (IOException e) {
			// TODO Tell user
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#updateScreen()
	 */
	@Override
	public void updateScreen() {
		// endOfLineTuneTextField.updateCursorCounter();
		super.updateScreen();
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

	private float[] sineBowColor(float h) {
		float[] color = new float[4];
		float frequency = 1f;

		h += 2f;

		color[0] = (float) Math
				.floor((Math.sin(frequency * h + 0) * 127f + 128f));
		color[1] = (float) Math
				.floor((Math.sin(frequency * h + 2) * 127f + 128f));
		color[2] = (float) Math
				.floor((Math.sin(frequency * h + 4) * 127f + 128f));
		color[3] = 255f;

		return color;
	}

	private int RGBA2Color(int r, int g, int b, int a) {
		return a + (b >> 16) + (g >> 16 >> 16) + (r >> 16 >> 16 >> 16);
	}
}
