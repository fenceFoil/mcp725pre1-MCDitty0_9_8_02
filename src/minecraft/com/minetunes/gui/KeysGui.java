/**
 * Copyright (c) 2012 William Karnavas 
 * All Rights Reserved
 */

/**
 * 
 * This file is part of MineTunes.
 * 
 * MineTunes is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * MineTunes is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MineTunes. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.minetunes.gui;

import java.io.IOException;
import java.util.LinkedList;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;

import org.lwjgl.input.Keyboard;

import com.minetunes.Minetunes;
import com.minetunes.config.MinetunesConfig;
import com.minetunes.keyboard.KeyBinding;

public class KeysGui extends GuiScreen {
	private LinkedList<GuiButton> keyButtons = new LinkedList<GuiButton>();
	private LinkedList<Boolean> settingKey = new LinkedList<Boolean>();
	private LinkedList<KeyBinding> keyBindings;
	private LinkedList<GuiButton> keyModButtons = new LinkedList<GuiButton>();
	private GuiScreen backScreen = null;

	public KeysGui(GuiScreen backScreen) {
		this.backScreen = backScreen;
	}

	public void updateButtonLabels() {
		for (int i = 0; i < keyBindings.size(); i++) {
			if (settingKey.get(i)) {
				keyButtons.get(i).displayString = "(Press Key)";
			} else {
				keyButtons.get(i).displayString = "§a "
						+ Keyboard.getKeyName(keyBindings.get(i).getMainKey());
			}

			String modLabel = "§a";
			int[] keyMods = keyBindings.get(i).getModifierKeys();
			if (keyMods.length == 2) {
				if (keyMods[0] == Keyboard.KEY_LCONTROL) {
					modLabel += "Ctrl";
				} else if (keyMods[0] == Keyboard.KEY_LSHIFT) {
					modLabel += "Shift";
				}
			} else if (keyMods.length == 1) {
				if (keyMods[0] == Keyboard.KEY_LCONTROL) {
					modLabel += "Left Ctrl";
				} else if (keyMods[0] == Keyboard.KEY_RCONTROL) {
					modLabel += "Right Ctrl";
				} else if (keyMods[0] == Keyboard.KEY_LSHIFT) {
					modLabel += "Left Shift";
				} else if (keyMods[0] == Keyboard.KEY_RSHIFT) {
					modLabel += "Right Shift";
				}
			} else {
				modLabel = "§7(Nothing)";
			}
			keyModButtons.get(i).displayString = modLabel;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#initGui()
	 */
	@Override
	public void initGui() {
		// Fetch key bindings
		keyBindings = Minetunes.keypressHandler.getBindings();
		// Reset buttons etc. if re-drawing gui
		keyButtons.clear();
		settingKey.clear();
		keyModButtons.clear();

		// Create buttons
		for (int i = 0; i < keyBindings.size(); i++) {
			KeyBinding b = keyBindings.get(i);
			GuiButton button = new GuiButton(i, (width / 2) + 80, 20 + 30 * i,
					70, 20, "");
			keyButtons.add(button);
			controlList.add(button);
			settingKey.add(false);

			GuiButton modButton = new GuiButton(i + 500, (width / 2),
					20 + 30 * i, 70, 20, "");
			keyModButtons.add(modButton);
			controlList.add(modButton);
		}
		updateButtonLabels();

		// Add other buttons
		controlList.add(new GuiButton(100, width / 8 * 3, height - 30,
				width / 4, 20, "Exit"));

		controlList.add(new MinetunesVersionGuiElement(100));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#drawScreen(int, int, float)
	 */
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		drawDefaultBackground();
		updateButtonLabels();

		// // Draw label at top of screen
		// drawCenteredString(fontRenderer, "Keyboard Commands", width / 2, 25,
		// 0x4444bb);

		// Draw button labels
		for (int i = 0; i < keyButtons.size(); i++) {
			String labelString = keyBindings.get(i).getDisplayName();

			// Workaround for upcoming book music
			if (keyBindings.get(i).getAction().equals("playBook")
					&& MinetunesConfig.CURRENT_VERSION.equals("0.9.6.02")) {
				labelString += " (Unused)";
			}

			int labelWidth = fontRenderer.getStringWidth(labelString);
			fontRenderer.drawString(labelString, width / 8 * 2
					- (labelWidth / 2), 26 + 30 * i, 0xffffff);

			// Draw + between mod button and main key button
			fontRenderer.drawString("+", (width / 2) + 72, 26 + 30 * i,
					0xffffff);
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

		for (int i = 0; i < keyBindings.size(); i++) {
			if (settingKey.get(i) && par2 != Keyboard.KEY_LCONTROL
					&& par2 != Keyboard.KEY_RCONTROL
					&& par2 != Keyboard.KEY_LSHIFT
					&& par2 != Keyboard.KEY_RSHIFT) {
				keyBindings.get(i).setMainKey(par2);
				settingKey.set(i, false);
				try {
					MinetunesConfig.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#mouseClicked(int, int, int)
	 */
	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		// TODO Auto-generated method stub
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
			// Exit screen
			mc.displayGuiScreen(backScreen);
		} else if (guibutton.id >= 0 && guibutton.id <= 99) {
			for (int i = 0; i < keyBindings.size(); i++) {
				if (guibutton.id == i) {
					settingKey.set(i, true);
				}
			}
		} else if (guibutton.id >= 500) {
			for (int i = 0; i < keyBindings.size(); i++) {
				if (guibutton.id - 500 == i) {
					cycleModButton(i);
				}
			}
		}
		try {
			MinetunesConfig.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Ctrl --> Shift --> LCtrl --> LShift --> RCtrl --> RShift --> Nothing
	 * 
	 * @param button
	 */
	private void cycleModButton(int button) {
		int[] keyMods = keyBindings.get(button).getModifierKeys();
		KeyBinding binding = keyBindings.get(button);
		if (keyMods.length == 2) {
			if (keyMods[0] == Keyboard.KEY_LCONTROL) {
				binding.setModifierKeys(KeyBinding.SHIFT_KEYS);
			} else if (keyMods[0] == Keyboard.KEY_LSHIFT) {
				int[] keys = { Keyboard.KEY_LCONTROL };
				binding.setModifierKeys(keys);
			}
		} else if (keyMods.length == 1) {
			if (keyMods[0] == Keyboard.KEY_LCONTROL) {
				int[] keys = { Keyboard.KEY_LSHIFT };
				binding.setModifierKeys(keys);
			} else if (keyMods[0] == Keyboard.KEY_RCONTROL) {
				int[] keys = { Keyboard.KEY_RSHIFT };
				binding.setModifierKeys(keys);
			} else if (keyMods[0] == Keyboard.KEY_LSHIFT) {
				int[] keys = { Keyboard.KEY_RCONTROL };
				binding.setModifierKeys(keys);
			} else if (keyMods[0] == Keyboard.KEY_RSHIFT) {
				int[] keys = {};
				binding.setModifierKeys(keys);
			}
		} else {
			binding.setModifierKeys(KeyBinding.CTRL_KEYS);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiScreen#updateScreen()
	 */
	@Override
	public void updateScreen() {
		// TODO Auto-generated method stub
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
}
