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
import java.io.FileFilter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiTextField;

import org.lwjgl.input.Keyboard;

import com.minetunes.Minetunes;
import com.minetunes.MidiFileFilter;
import com.minetunes.ditty.DittyPlayerThread;
import com.minetunes.ditty.event.PlayMidiDittyEvent;
import com.minetunes.ditty.event.SFXEvent;
import com.minetunes.sfx.SFXManager;

/**
 *
 */
public class SoundTestGui extends GuiScreen {

	/**
	 * Screen pressing the exit button returns you to.
	 */
	private GuiScreen backScreen;

	private GuiTextField sfxField;
	private GuiButton testSFXButton;
	private GuiButton changeSFXSourceButton;
	private GuiScrollingTextPanel sfxTextPanel;

	private int currSFXSource = SFXManager.getLatestSource();
	private int currSFXSourceIndex = 0;
	private String sfxSourceName = "Change Source";

	private GuiButton testMusicStringSampleButton;

	private GuiTextField midiField;
	private GuiButton testMidiButton;
	private GuiScrollingTextPanel midiTextPanel;

	private String currentMatchingSFX = null;
	private File currentMatchingMIDI = null;

	private GuiButton muteButton;

	private GuiButton exitButton;

	/**
	 * @param backScreen
	 * 
	 */
	public SoundTestGui(GuiScreen backScreen) {
		this.backScreen = backScreen;
	}

	@Override
	public void initGui() {
		controlList.add(new MinetunesVersionGuiElement(100));

		int panelMargins = 10;
		int topMargin = 30;

		// Left third of screen: sfx tests
		testSFXButton = new GuiButton(200, (width / 3) - 35, topMargin, 30, 20,
				"Play");
		controlList.add(testSFXButton);
		sfxField = new GuiTextField(fontRenderer, panelMargins, topMargin,
				width / 3 - (2 * panelMargins) - 30, 20);
		sfxTextPanel = new GuiScrollingTextPanel(panelMargins, topMargin + 30,
				width / 3 - (2 * panelMargins), height - panelMargins
						- topMargin - 30 - 30, false, fontRenderer, true);
		changeSFXSourceButton = new GuiButton(2000, panelMargins, height - 20
				- panelMargins, 100, 20, "Alpha to 1.3");
		controlList.add(changeSFXSourceButton);

		// Center third of screen: MusicString tests
		testMusicStringSampleButton = new GuiButton(300, width / 2 - 50,
				topMargin, 100, 20, "Test MusicString");
		controlList.add(testMusicStringSampleButton);

		// Also, a mute button
		muteButton = new GuiButton(500, width / 2 - 50, topMargin + 60, 100,
				20, "Mute All");
		controlList.add(muteButton);

		// Finally, an exit button
		exitButton = new GuiButton(100, width / 2 - 50, height - panelMargins
				- 20, 100, 20, "Exit");
		controlList.add(exitButton);

		// Right third of screen: midi file tests
		testMidiButton = new GuiButton(400, width - 35, topMargin, 30, 20,
				"Play");
		controlList.add(testMidiButton);
		midiField = new GuiTextField(fontRenderer, (width / 3) * 2
				+ panelMargins, topMargin, (width / 3) - (2 * panelMargins)
				- 30, 20);
		midiTextPanel = new GuiScrollingTextPanel((width / 3) * 2
				+ panelMargins, topMargin + 30, width / 3 - (2 * panelMargins),
				height - panelMargins - topMargin - 30, false, fontRenderer,
				true);

		updateMidiTestPanel();
		updateSFXTestPanel();

		sfxField.setFocused(true);
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		drawDefaultBackground();

		// Draw label at top of screen
		drawCenteredString(fontRenderer, "MineTunes Sound Tests", width / 2, 10,
				0x4444bb);

		// Draw elements that are not buttons
		sfxTextPanel.draw(par1, par2);
		sfxField.drawTextBox();
		midiTextPanel.draw(par1, par2);
		midiField.drawTextBox();

		super.drawScreen(par1, par2, par3);
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		if (par1GuiButton.id == 100) {
			// Exit screen
			mc.displayGuiScreen(backScreen);
		} else if (par1GuiButton.id == 200) {
			// Play SFX
			testSFX();
			sfxField.setFocused(true);
		} else if (par1GuiButton.id == 2000) {
			// Change sfx source
			int[] sfxSourceNums = SFXManager.getSourceNums();

			currSFXSourceIndex++;
			if (currSFXSourceIndex >= sfxSourceNums.length) {
				currSFXSourceIndex = 0;
			}

			currSFXSource = sfxSourceNums[currSFXSourceIndex];

			changeSFXSourceButton.displayString = SFXManager
					.getSourceName(currSFXSource);
			sfxSourceName = changeSFXSourceButton.displayString;

			updateSFXTestPanel();
		} else if (par1GuiButton.id == 300) {
			// Test MusicString
			testMusicString();
		} else if (par1GuiButton.id == 400) {
			// Test Midi
			testMidi();
			midiField.setFocused(true);
		} else if (par1GuiButton.id == 500) {
			// Mute
			Minetunes.mutePlayingDitties();
		}
	}

	private void testMidi() {
		if (currentMatchingMIDI != null) {
			Minetunes.executeTimedDittyEvent(new PlayMidiDittyEvent(
					currentMatchingMIDI, 0));
		}
	}

	private void testMusicString() {
		DittyPlayerThread.playMusicString("IOrgan Cmajw Rw");
	}

	private void testSFX() {
		if (currentMatchingSFX != null) {
			Minetunes.executeTimedDittyEvent(new SFXEvent(SFXManager
					.getEffectForShorthandName(currentMatchingSFX,
							currSFXSource), 0, 0));
		}
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		sfxField.mouseClicked(par1, par2, par3);
		midiField.mouseClicked(par1, par2, par3);
		sfxTextPanel.mouseClicked(par1, par2, par3);
		midiTextPanel.mouseClicked(par1, par2, par3);
		super.mouseClicked(par1, par2, par3);
	}

	@Override
	protected void mouseMovedOrUp(int par1, int par2, int par3) {
		sfxTextPanel.mouseMovedOrUp(par1, par2, par3);
		midiTextPanel.mouseMovedOrUp(par1, par2, par3);
		super.mouseMovedOrUp(par1, par2, par3);
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		sfxField.textboxKeyTyped(par1, par2);
		midiField.textboxKeyTyped(par1, par2);
		super.keyTyped(par1, par2);

		// Update based on any text field changes
		updateMidiTestPanel();
		updateSFXTestPanel();

		if (sfxField.isFocused() && par2 == Keyboard.KEY_RETURN) {
			// press play sfx button
			testSFX();
		}

		if (midiField.isFocused() && par2 == Keyboard.KEY_RETURN) {
			// Test midi
			testMidi();
		}
	}

	private void updateMidiTestPanel() {
		File[] midiFileList = new File(
				Minecraft.getMinecraftDir() + "/MCDitty", "midi")
				.listFiles(new MidiFileFilter());

		LinkedList<File> matchingMidis = new LinkedList<File>();

		if (midiFileList != null) {
			for (File f : midiFileList) {
				if (f.getName().toLowerCase()
						.startsWith(midiField.getText().toLowerCase())) {
					matchingMidis.add(f);
				}
			}
		}

		StringBuilder midiMatchListText = new StringBuilder();
		midiMatchListText.append("§bMIDIs:§r\n");
		boolean firstMatch = true;
		for (File midiFile : matchingMidis) {
			if (firstMatch) {
				midiMatchListText.append("§a>  ");
			}
			midiMatchListText.append(midiFile.getName());
			if (firstMatch) {
				midiMatchListText.append("  <§r");
				firstMatch = false;
			}
			midiMatchListText.append("\n");
		}
		midiTextPanel.setText(midiMatchListText.toString());

		if (matchingMidis.size() > 0) {
			currentMatchingMIDI = matchingMidis.get(0);
		} else {
			currentMatchingMIDI = null;
		}

		if (currentMatchingMIDI == null) {
			testMidiButton.enabled = false;
		} else {
			testMidiButton.enabled = true;
		}
	}

	private void updateSFXTestPanel() {
		LinkedList<String> matchingSFX = new LinkedList<String>();
		Set<String> keys = SFXManager.getAllEffects(currSFXSource).keySet();
		for (String key : SFXManager.getAllEffects(currSFXSource).keySet()) {
			if (key.toLowerCase().startsWith(sfxField.getText().toLowerCase())) {
				matchingSFX.add(key);
			}
		}
		Collections.sort(matchingSFX);

		StringBuilder sfxMatchListText = new StringBuilder();
		sfxMatchListText.append("§bSFX:§r\n");
		boolean firstMatch = true;
		for (String sfx : matchingSFX) {
			if (firstMatch) {
				sfxMatchListText.append("§a>  ");
			}
			sfxMatchListText.append(sfx);
			if (firstMatch) {
				sfxMatchListText.append("  <§r");
				firstMatch = false;
			}
			sfxMatchListText.append("\n");
		}
		sfxTextPanel.setText(sfxMatchListText.toString());

		if (matchingSFX.size() > 0) {
			currentMatchingSFX = matchingSFX.get(0);
		} else {
			currentMatchingSFX = null;
		}

		if (currentMatchingSFX == null) {
			testSFXButton.enabled = false;
		} else {
			testSFXButton.enabled = true;
		}
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
	}

}