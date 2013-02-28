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
package com.minetunes.noteblocks;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.src.EnumOptions;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;

import org.jfugue.elements.Note;
import org.lwjgl.input.Keyboard;

import com.minetunes.Finder;
import com.minetunes.Point3D;
import com.minetunes.config.MinetunesConfig;
import com.minetunes.gui.GuiSlider;
import com.minetunes.gui.MinetunesGui;
import com.minetunes.gui.MinetunesVersionGuiElement;

public class NoteblockTunerGui extends GuiScreen {

	private static final String TRANSPOSE_SLIDER_LABEL = "Transpose Keys";

	private Point3D noteblockPoint;
	private TileEntityNoteMinetunes tile = new TileEntityNoteMinetunes();
	private static GuiSlider transpostionSlider;
	private static float persistantTranspose = 0.5f;

	private class PianoKeyboard {
		private class PianoKey {
			public static final int TALLEST_KEY_HEIGHT = 60;
			public int width;
			public int height;
			public int noteMIDIValue;
			public int keyNum;
			public boolean isBlackKey;
			public int color;
			public String label;

			public PianoKey(int note, int keyNumber) {
				noteMIDIValue = note;
				keyNum = keyNumber;

				String rawLabel = Note.getStringForNote((byte) note);
				// Choose dimensions of key
				isBlackKey = isNoteValueBlackKey(note);
				if (isBlackKey) {
					width = 15;
					height = 40;
					color = 0xff000000;
					// label = "#";
					label = rawLabel;
				} else {
					width = 25;
					height = TALLEST_KEY_HEIGHT;
					color = 0xaaffffff;
					label = rawLabel;
				}
			}

			public boolean isNoteValueBlackKey(int value) {
				String valueString = Note.getStringForNote((byte) value);
				if (valueString.contains("#") || valueString.contains("b")) {
					return true;
				} else {
					return false;
				}
			}

			public int getColor(boolean selected, boolean noteConfirmed,
					boolean isKeyMoused) {
				if (selected) {
					if (!noteConfirmed) {
						return 0x8877ff77;
					} else {
						return 0xffaaffff;
					}
				} else if (isKeyMoused) {
					return 0x887777ff;
				} else {
					return color;
				}
			}
		}

		// 12 and 2 are dimensions of keyboard, in keys
		private int keyboardWidth = 13;
		private int keyboardHeight = 2;
		private PianoKey[][] pianoKeys = new PianoKey[keyboardWidth][keyboardHeight];

		private int xMargins = 4;
		private int yMargins = 5;
		private int rowHeight = 80;

		// private int x = 0;
		private int y = 0;

		private int selectedKeyX = -1;
		private int selectedKeyY = 0;

		private int mouseKeyX = -1;
		private int mouseKeyY = 0;

		public PianoKeyboard(int startNoteValue, int y) {
			int currNoteValue = startNoteValue;
			int currKeyNum = 0;

			// this.x = x;
			this.y = y;

			for (int j = 0; j < keyboardHeight; j++) {
				for (int i = 0; i < keyboardWidth; i++) {
					// Create a new key
					PianoKey newKey = new PianoKey(currNoteValue, currKeyNum);

					// Put onto keyboard
					pianoKeys[i][j] = newKey;

					// Increment current note value
					currNoteValue++;
					currKeyNum++;
				}
				// Allow for note at end of one row to appear at start of next
				// row
				currNoteValue--;
				currKeyNum--;
			}
		}

		public void draw(GuiScreen screen, boolean noteConfirmed) {
			int startX = screen.width / 2 - getTotalWidth() / 2;

			int bgColor = 0x888888;
			if (noteConfirmed) {
				bgColor = bgColor | 0x44000000;
			} else {
				bgColor = bgColor | 0xFF000000;
			}
			screen.drawRect(startX - 5, y - 5, screen.width / 2
					+ getTotalWidth() / 2 + 5, y
					+ (rowHeight * (pianoKeys[0].length - 1))
					+ PianoKey.TALLEST_KEY_HEIGHT + 5, bgColor);

			int currY = y;
			for (int j = 0; j < keyboardHeight; j++) {
				int currX = startX;
				for (int i = 0; i < keyboardWidth; i++) {
					boolean isKeySelected = ((j == selectedKeyY) && (i == selectedKeyX));
					boolean isKeyMoused = ((j == mouseKeyY) && (i == mouseKeyX));

					int color = pianoKeys[i][j].getColor(isKeySelected,
							noteConfirmed, isKeyMoused);
					String text = pianoKeys[i][j].label;
					screen.drawRect(currX, currY,
							currX + pianoKeys[i][j].width, currY
									+ pianoKeys[i][j].height, color);

					// Arbitrary guess at font's height
					int fontHeight = 3;
					// double xVel = (double) pianoKeys[i][j].keyNum / 24d;
					// int fontColor = (int)
					// ((int)(EntityNoteFX.getRedForNote(xVel) * 0xFF) +
					// ((int)(EntityNoteFX.getGreenForNote(xVel) * 0xFF)) *
					// 0x100);
					// // + (EntityNoteFX.getGreenForNote(xVel) * 0xFF) +
					// (EntityNoteFX
					// // .getBlueForNote(xVel) * 0xFFFF));
					int fontColor = 0x00ff00;
					screen.drawCenteredString(fontRenderer, text, currX
							+ pianoKeys[i][j].width / 2, currY
							+ pianoKeys[i][j].height / 2 - fontHeight,
							fontColor);

					currX += pianoKeys[i][j].width + xMargins;
				}
				currY += rowHeight;
			}
		}

		/**
		 * Currently only guaranteed to work if up and right are within -1<=x<=1
		 * 
		 * @param up
		 * @param right
		 */
		public void moveSelectedKey(int up, int right) {
			// First movement only highlights first key and ignores all other
			// movement
			if (selectedKeyX < 0 || selectedKeyY < 0) {
				selectedKeyX = 0;
				selectedKeyY = 0;
				return;
			}

			// Move selection up with range checking and wrapping
			selectedKeyY = (selectedKeyY + up) % pianoKeys[0].length;

			if (selectedKeyY < 0) {
				selectedKeyY = keyboardHeight - 1;
			}

			// Move selection right with range checking and wrapping
			selectedKeyX = (selectedKeyX + right) % pianoKeys.length;

			if (selectedKeyX < 0) {
				selectedKeyX = keyboardWidth - 1;
			}
		}

		public void setSelectedKey(int x, int y) {
			selectedKeyX = x;
			selectedKeyY = y;
		}

		public void setMouseHighlightedKey(int x, int y) {
			mouseKeyX = x;
			mouseKeyY = y;
		}

		public void turnOffSelectedKey() {
			selectedKeyX = -1;
			selectedKeyY = 0;
		}

		public int getSelectedKeyX() {
			return selectedKeyX;
		}

		public int getSelectedKeyY() {
			return selectedKeyY;
		}

		public int getSelectedKeyNoteValue() {
			if (selectedKeyX < 0 || selectedKeyY < 0) {
				return -1;
			} else {
				return pianoKeys[selectedKeyX][selectedKeyY].noteMIDIValue;
			}
		}

		// Only accounts for top row's width; assumes all rows are equal width
		private int getTotalWidth() {
			int currWidth = 0;

			for (int i = 0; i < keyboardWidth; i++) {
				currWidth += pianoKeys[i][0].width;

				if (i != keyboardWidth - 1) {
					currWidth += xMargins;
				}
			}

			return currWidth;
		}

		public int getSelectedKeyNum() {
			if (selectedKeyX < 0 || selectedKeyY < 0) {
				return -1;
			} else {
				return pianoKeys[selectedKeyX][selectedKeyY].keyNum;
			}
		}

		public void setSelectedKeyNum(int num) {
			for (int i = 0; i < keyboardWidth; i++) {
				// Start at bottom: if on border between ocaves, position at
				// start of row not end
				for (int j = keyboardHeight - 1; j >= 0; j--) {
					if (num == pianoKeys[i][j].keyNum) {
						selectedKeyX = i;
						selectedKeyY = j;
						break;
					}
				}
			}
		}

		public boolean isKeySelected() {
			if (selectedKeyX >= 0 && selectedKeyY >= 0) {
				return true;
			} else {
				return false;
			}
		}

		public boolean isClickOnKeyboard(int x, int y, GuiScreen screen) {
			Point2D key = getMousePositionOnKeyboard(x, y, screen);
			if (key != null) {
				setSelectedKey((int) key.getX(), (int) key.getY());
				return true;
			} else {
				return false;
			}
		}

		private Point2D getMousePositionOnKeyboard(int mx, int my,
				GuiScreen screen) {
			// First, do not highlight a key if the mouse has not moved from
			// center of screen (as when gui is first opened)
			if (Math.abs((mx - screen.width / 2)) < 3
					&& Math.abs((my - screen.height / 2)) < 3) {
				return null;
			}

			int startX = screen.width / 2 - getTotalWidth() / 2;

			int currY = this.y;
			for (int j = 0; j < keyboardHeight; j++) {
				int currX = startX;
				for (int i = 0; i < keyboardWidth; i++) {
					boolean isKeySelected = ((j == selectedKeyY) && (i == selectedKeyX));

					Rectangle2D r = new Rectangle2D.Double(currX, currY,
							pianoKeys[i][j].width, pianoKeys[i][j].height);
					if (r.contains(new Point2D.Double(mx, my))) {
						return new Point2D.Double(i, j);
					}

					currX += pianoKeys[i][j].width + xMargins;
				}
				currY += rowHeight;
			}

			return null;
		}

		public void updateMouseHighlighting(int mx, int my, GuiScreen screen) {
			Point2D key = getMousePositionOnKeyboard(mx, my, screen);
			if (key != null) {
				setMouseHighlightedKey((int) key.getX(), (int) key.getY());
			} else {
				setMouseHighlightedKey(-1, 0);
			}
		}
	}

	private PianoKeyboard keyboard;

	private GuiButton muteToggleButton;

	public NoteblockTunerGui(Point3D hoverPoint) {
		super();

		noteblockPoint = hoverPoint;

		// Get the noteblock's tile entity
		TileEntity unknownEntity = Minecraft.getMinecraft().theWorld
				.getBlockTileEntity(noteblockPoint.x, noteblockPoint.y,
						noteblockPoint.z);
		if (unknownEntity instanceof TileEntityNoteMinetunes) {
			tile = (TileEntityNoteMinetunes) unknownEntity;
		}
	}

	@Override
	public void initGui() {
		// Add MineTunes version readout
		controlList.add(new MinetunesVersionGuiElement(0));

		// Add transpostion slider
		// Try to have it keep the same settings as the last time this screen
		// was shown, including position and text label
		String label = TRANSPOSE_SLIDER_LABEL;
		if (transpostionSlider != null) {
			label = transpostionSlider.displayString;
		}
		transpostionSlider = new GuiSlider(1000, width - 150, 40, label,
				persistantTranspose);
		controlList.add(transpostionSlider);

		muteToggleButton = new GuiButton(2000, width - 50 - 5, height - 25, 50,
				20, "");
		controlList.add(muteToggleButton);

		controlList.add(new GuiButton(3000, 5, height - 25, 50, 20, "Exit"));

		// Add keyboard
		resetPianoKeyboard();

		// Set keyboard to noteblock setting, if known
		if (tile.noteValueKnown) {
			keyboard.setSelectedKeyNum(tile.note);
		}

		// Mute sound as necessary at start
		muteSoundAccordingToButton();
	}

	private void resetPianoKeyboard() {

		int selectedX = -1;
		int selectedY = 0;
		if (keyboard != null) {
			selectedX = keyboard.getSelectedKeyX();
			selectedY = keyboard.getSelectedKeyY();
		}
		PianoKeyboard k = new PianoKeyboard(
				EntityNoteBlockTooltip.instrumentZeroNotes.get(BlockNoteMinetunes
						.getNoteTypeForBlock(mc.theWorld, tile.xCoord,
								tile.yCoord, tile.zCoord))
						+ noteModifier
						+ (BlockNoteMinetunes.getOctaveAdjust(tile.xCoord,
								tile.yCoord, tile.zCoord) * 12), 70);
		k.setSelectedKey(selectedX, selectedY);
		keyboard = k;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#drawScreen(int, int, float)
	 */
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		// drawDefaultBackground();

		// Draw label at top of screen
		// drawCenteredString(fontRenderer, "Tuning Noteblock", width / 2, 15,
		// 0xffffff);

		// Draw instrument type
		drawCenteredString(
				fontRenderer,
				"Instrument: §a"
						+ BlockNoteMinetunes.getScreenName(BlockNoteMinetunes
								.getNoteTypeForBlock(mc.theWorld, tile.xCoord,
										tile.yCoord, tile.zCoord)), width - 90,
				25, 0xffffff);

		// Draw instruction text
		drawString(fontRenderer, "Keyboard:", 50, 25, 0xffffff);
		String chooseKeyText = String
				.format("Move with %1$s, %2$s, %3$s, and %4$s",
						new Object[] {
								Keyboard.getKeyName(mc.gameSettings.keyBindForward.keyCode),
								Keyboard.getKeyName(mc.gameSettings.keyBindLeft.keyCode),
								Keyboard.getKeyName(mc.gameSettings.keyBindBack.keyCode),
								Keyboard.getKeyName(mc.gameSettings.keyBindRight.keyCode) });
		drawString(fontRenderer, chooseKeyText, 20, 40, 0xaaaaaa);
		String exitKeyText = String.format("Select with %1$s",
				new Object[] { Keyboard
						.getKeyName(mc.gameSettings.keyBindJump.keyCode) });
		drawString(fontRenderer, exitKeyText, 20, 50, 0xaaaaaa);

		// Draw keyboard diagram
		keyboard.draw(this, tuningAndExiting);

		// Update keyboard highlighting for mouse
		keyboard.updateMouseHighlighting(par1, par2, this);

		// Update muting button
		if (MinetunesConfig.getBoolean("noteblockTuner.mute")) {
			muteToggleButton.displayString = "Vol: Mute";
		} else {
			muteToggleButton.displayString = "Vol: 100%";
		}

		// // Draw a noteblock
		//
		// // TODO: Learn what the **** half of this stuff does.
		// int terrainTexture = this.mc.renderEngine.getTexture("/terrain.png");
		// zLevel = 0.0F;
		// GL11.glDepthFunc(GL11.GL_GEQUAL);
		// GL11.glPushMatrix();
		// GL11.glTranslatef(0.0F, 0.0F, -200.0F);
		// GL11.glEnable(GL11.GL_TEXTURE_2D);
		// GL11.glDisable(GL11.GL_LIGHTING);
		// GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		// GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		// mc.renderEngine.bindTexture(terrainTexture);
		//
		// GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0F);
		// int textureIndex = Block.music.blockIndexInTexture;
		//
		// drawTexturedModalRect(width / 2 - 8, 20, textureIndex % 16 << 4,
		// textureIndex >> 4 << 4, 16, 16);
		//
		// GL11.glEnable(GL11.GL_DEPTH_TEST);
		// GL11.glDepthFunc(GL11.GL_LEQUAL);
		// GL11.glDisable(GL11.GL_TEXTURE_2D);
		//
		// GL11.glDisable(GL11.GL_LIGHTING);
		// GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		// GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		//
		// GL11.glDisable(GL11.GL_DEPTH_TEST);
		// GL11.glEnable(GL11.GL_BLEND);
		// GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		// GL11.glPopMatrix();
		// this.zLevel = 0.0F;
		// GL11.glDepthFunc(GL11.GL_LEQUAL);
		// GL11.glDisable(GL11.GL_DEPTH_TEST);
		// GL11.glEnable(GL11.GL_TEXTURE_2D);
		//
		// GL11.glEnable(GL11.GL_DEPTH_TEST);
		// GL11.glEnable(GL11.GL_LIGHTING);

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
		if (guibutton.id == 0) {
			// Version
			Minecraft.getMinecraft().displayGuiScreen(new MinetunesGui(this));
		} else if (guibutton.id == 2000) {
			// Mute
			MinetunesConfig.setBoolean("noteblockTuner.mute",
					!MinetunesConfig.getBoolean("noteblockTuner.mute"));
			try {
				MinetunesConfig.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

			muteSoundAccordingToButton();
		} else if (guibutton.id == 3000) {
			// Exit
			closeGui();
		}
	}

	private void muteSoundAccordingToButton() {
		float volumeToSet = 0.00f;
		if (!MinetunesConfig.getBoolean("noteblockTuner.mute")) {
			volumeToSet = normalGameVolume;
		}
		Minecraft.getMinecraft().gameSettings.setOptionFloatValue(
				EnumOptions.SOUND, volumeToSet);
	}

	private float normalGameVolume = Minecraft.getMinecraft().gameSettings.soundVolume;
	private boolean muting = MinetunesConfig.getBoolean("noteblockTuner.mute");

	private void closeGui() {
		Minecraft.getMinecraft().gameSettings.setOptionFloatValue(
				EnumOptions.SOUND, normalGameVolume);

		Minecraft.getMinecraft().displayGuiScreen(null);
	}

	private float lastPersistantTranspose = persistantTranspose;

	private boolean tuningAndExiting = false;

	private static int noteModifier = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#updateScreen()
	 */
	@Override
	public void updateScreen() {
		// Update the transposition value from the slider
		persistantTranspose = transpostionSlider.sliderValue;

		int hoverX = mc.objectMouseOver.blockX;
		int hoverY = mc.objectMouseOver.blockY;
		int hoverZ = mc.objectMouseOver.blockZ;

		if (hoverX != tile.xCoord || hoverY != tile.yCoord
				|| hoverZ != tile.zCoord) {
			closeGui();
		}

		if (persistantTranspose != lastPersistantTranspose) {
			// If transpose value changed
			noteModifier = (int) (((persistantTranspose - 0.5d) * 2d) * 12d);
			resetPianoKeyboard();

			if (noteModifier != 0) {
				transpostionSlider.displayString = "(" + noteModifier + ")";
			} else {
				transpostionSlider.displayString = TRANSPOSE_SLIDER_LABEL;
			}
		}

		lastPersistantTranspose = persistantTranspose;

		// Handle tuning and exiting
		if (tuningAndExiting) {

			// Only update this every fourth frame
			if (frameCount % 2 == 0) {

				boolean readyToExit = false;

				if (keyboard.getSelectedKeyNum() < 0) {
					// If nothing is selected, don't tune
					readyToExit = true;
				} else {
					// IF something is selected

					// If the noteblock's value is unknown, pling it to find out
					if (!tile.noteValueKnown) {
						if (!mysteryBlockPolled) {
							clickNoteblock();
							mysteryBlockPolled = true;
						}
					} else {
						// If value is known, try to tune or, if finished, stop
						// tuning
						if (clicksToGo == Integer.MIN_VALUE) {
							// Calculate how many clicks needed to tune
							clicksToGo = keyboard.getSelectedKeyNum()
									- tile.note;
							if (clicksToGo < 0) {
								clicksToGo += 25;
							}

							if (clicksToGo == 0) {
								// Ordinarily, no noteblock sound would be
								// heard.
								// This is here to give player reassurance that
								// the tuning was successful
								BlockNoteMinetunes.chimeBlockAtPitch(tile,
										keyboard.getSelectedKeyNum());
							}
						} else if (clicksToGo > 0) {
							clickNoteblock();
							clicksToGo--;
						} else {
							readyToExit = true;
						}
					}
				}

				if (readyToExit && keyboard.getSelectedKeyNum() == tile.note) {
					closeGui();
				}
			}
		}

		frameCount++;
	}

	private int frameCount = 0;

	// Used when tuning and exiting
	// Negative infinity indicates "uncalculated"
	private int clicksToGo = Integer.MIN_VALUE;

	// Used when tuning and exiting
	private boolean mysteryBlockPolled = false;

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
		if (!tuningAndExiting) {
			tuningAndExiting = keyboard.isClickOnKeyboard(x, y, this);
		}

		super.mouseClicked(x, y, button);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#keyTyped(char, int)
	 */
	@Override
	protected void keyTyped(char keyChar, int keyCode) {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			closeGui();
		}

		if (!tuningAndExiting) {

			if (keyCode == mc.gameSettings.keyBindForward.keyCode) {
				keyboard.moveSelectedKey(-1, 0);
				BlockNoteMinetunes.chimeBlockAtPitch(tile,
						keyboard.getSelectedKeyNum());
			} else if (keyCode == mc.gameSettings.keyBindBack.keyCode) {
				keyboard.moveSelectedKey(1, 0);
				BlockNoteMinetunes.chimeBlockAtPitch(tile,
						keyboard.getSelectedKeyNum());
			} else if (keyCode == mc.gameSettings.keyBindLeft.keyCode) {
				keyboard.moveSelectedKey(0, -1);
				BlockNoteMinetunes.chimeBlockAtPitch(tile,
						keyboard.getSelectedKeyNum());
			} else if (keyCode == mc.gameSettings.keyBindRight.keyCode) {
				keyboard.moveSelectedKey(0, 1);
				BlockNoteMinetunes.chimeBlockAtPitch(tile,
						keyboard.getSelectedKeyNum());
			}

			if (keyCode == mc.gameSettings.keyBindJump.keyCode) {
				// Choose note, tune block, and exit gui
				tuningAndExiting = true;
			}
		}

		// super.keyTyped(par1, par2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#mouseMovedOrUp(int, int, int)
	 */
	@Override
	protected void mouseMovedOrUp(int x, int y, int button) {
		super.mouseMovedOrUp(x, y, button);
	}

	private void clickNoteblock() {
		ItemStack playerItem = mc.thePlayer.inventory.getCurrentItem();
		int blockX = mc.objectMouseOver.blockX;
		int blockY = mc.objectMouseOver.blockY;
		int blockZ = mc.objectMouseOver.blockZ;
		int side = mc.objectMouseOver.sideHit;

		if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld,
				playerItem, blockX, blockY, blockZ, side,
				mc.objectMouseOver.hitVec)) {
			mc.thePlayer.swingItem();
		}

		if (playerItem == null) {
			return;
		}

	}

}
