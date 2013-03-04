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
package com.minetunes.gui.settings;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JComboBox;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.Timer;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Bounce;
import aurelienribon.tweenengine.equations.Linear;
import aurelienribon.tweenengine.equations.Quart;

import com.minetunes.Finder;
import com.minetunes.gui.GuiButtonRect;
import com.minetunes.gui.TuneTileGuiTweenAccessor;

/**
 * @author William
 * 
 */
public class GuiSettings extends GuiScreen {
	public static final LinkedList<Setting> SIGNTUNES_SETTINGS = new LinkedList<Setting>();
	static {
		SIGNTUNES_SETTINGS.add(new Setting("Playing Sign Color",
				"signs.playingColor", SettingType.COLOR));
		SIGNTUNES_SETTINGS.add(new Setting("MIDI Volume", "",
				SettingType.MIDI_VOLUME));
		SIGNTUNES_SETTINGS.add(new Setting("Show Errors on Chat",
				"signs.showErrors", SettingType.BOOLEAN_ON_OFF));
		SIGNTUNES_SETTINGS.add(new Setting("Note Particles",
				"particles.noteParticlesDisabled", SettingType.BOOLEAN_ON_OFF,
				true));
		SIGNTUNES_SETTINGS.add(new Setting("Lyrics", "lyrics.enabled",
				SettingType.BOOLEAN_ON_OFF));
		SIGNTUNES_SETTINGS.add(new Setting("Proximity",
				"signs.proximityEnabled", SettingType.BOOLEAN_ON_OFF));
		SIGNTUNES_SETTINGS.add(new Setting("SaveMIDI", "signs.saveMidiEnabled",
				SettingType.BOOLEAN_ON_OFF));
		SIGNTUNES_SETTINGS.add(new Setting("Disco Floors",
				"signs.disco.disabled", SettingType.BOOLEAN_ON_OFF, true));
		SIGNTUNES_SETTINGS.add(new Setting("'MIDI Saved' Message",
				"midiSavedMessage", SettingType.BOOLEAN_ON_OFF));
		SIGNTUNES_SETTINGS.add(new Setting("All Particles",
				"particles.enabled", SettingType.BOOLEAN_ON_OFF));
		SIGNTUNES_SETTINGS.add(new Setting("Sign Tags To Ignore",
				"noPlayTokens", SettingType.NO_PLAY_TOKENS));
		SIGNTUNES_SETTINGS.add(new Setting("Error Blink Time (Sec)",
				"signs.errorBlinkMS", SettingType.INTEGER_SHORT_TIME));
	}

	public static final LinkedList<Setting> NOTEBLOCKS_SETTINGS = new LinkedList<Setting>();
	static {
		NOTEBLOCKS_SETTINGS.add(new Setting("Show Notes",
				"enableNoteblockTooltips", SettingType.BOOLEAN_ON_OFF));
		NOTEBLOCKS_SETTINGS.add(new Setting("Mute Noteblocks",
				"noteblock.mute", SettingType.BOOLEAN_YES_NO));
		NOTEBLOCKS_SETTINGS.add(new Setting("Attached Signs",
				"noteblock.signsDisabled",
				SettingType.BOOLEAN_ENABLED_DISABLED, true));
		// NOTEBLOCKS_SETTINGS.add(new Setting("Attached Signs: Lyrics"
	}

	public static final LinkedList<Setting> BLOCKTUNES_SETTINGS = new LinkedList<Setting>();
	static {
		BLOCKTUNES_SETTINGS.add(new Setting("MIDI Volume", "",
				SettingType.MIDI_VOLUME));
	}

	public static final LinkedList<Setting> MINETUNES_SETTINGS = new LinkedList<Setting>();
	static {
		MINETUNES_SETTINGS.add(new Setting("MIDI Volume", "",
				SettingType.MIDI_VOLUME));
		MINETUNES_SETTINGS.add(new Setting("Help Images: High Quality",
				"slides.highQuality", SettingType.BOOLEAN_ENABLED_DISABLED));
	}

	private LinkedList<Setting> settings;
	private GuiScreen backScreen;
	private int settingsPerPage = 0;
	private int currPageStartSetting = 0;
	private int currPageEndSetting = 0;
	private int currPage = -1;
	private final int SPACE_BETWEEN_SETTINGS = 25;
	private final int SPACE_ABOVE_TOP_SETTING = 40;
	private final int SPACE_BELOW_SETTINGS = 40;

	private LinkedList<GuiButtonRect> rectButtons = new LinkedList<GuiButtonRect>();
	private String title;

	/**
	 * @param thisGui
	 * @param settingsList
	 */
	public GuiSettings(GuiScreen back, LinkedList<Setting> settingsList,
			String title) {
		backScreen = back;
		settings = settingsList;
		this.title = title;
	}

	@Override
	public void drawScreen(int mx, int my, float par3) {
		drawMinetunesBackground(width, height);

		// Update settings
		for (Setting s : settings) {
			s.draw(mx, my);
		}

		// Update tweens
		updateTweens();

		// Draw rect buttons
		for (GuiButtonRect r : rectButtons) {
			r.draw(mx, my, par3, fontRenderer);
		}

		// Draw borders
		// drawRect(PAGE_BUTTON_WIDTH, 0, width - PAGE_BUTTON_WIDTH,
		// SPACE_ABOVE_TOP_SETTING - 5, 0xff000000);
		// drawRect(PAGE_BUTTON_WIDTH, height - SPACE_BELOW_SETTINGS - 5, width
		// - PAGE_BUTTON_WIDTH, height, 0xff000000);

		// Draw labels
		drawCenteredString(fontRenderer, (currPage + 1) + " of "
				+ getMaxPages(), width - 90, 15, 0xa0a0a0);
		drawCenteredString(fontRenderer, title, width / 2, 15, 0xffff00);

		// Draw exit button
		for (Object o : controlList) {
			if (o instanceof GuiButton) {
				// GL11.glPopMatrix();
				((GuiButton) o).drawButton(mc, mx, my);
				// GL11.glRotatef(50, 0, 50, 0);
				// GL11.glPushMatrix();
			}
		}
	}

	/**
	 * Copied from GuiMinetunes
	 * 
	 * @param width
	 * @param height
	 */
	public void drawMinetunesBackground(int width, int height) {
		drawGradientRect(0, 0, width, height / 2, 0x40111111, 0xa0ffffff);
		drawGradientRect(0, height / 2, width, height, 0xa0ffffff, 0xff101010);
	}

	/**
	 * @return
	 */
	private int getMaxPages() {
		if (settingsPerPage != 0) {
			return ((settings.size() - 1) / settingsPerPage) + 1;
		} else {
			return 1;
		}
	}

	@Override
	protected void mouseClicked(int mx, int my, int button) {
		for (Setting s : settings) {
			s.onMousePressed(mx, my, button);
		}
		for (GuiButtonRect r : rectButtons) {
			r.onMousePressed(mx, my, button);
		}
		super.mouseClicked(mx, my, button);
	}

	@Override
	protected void mouseMovedOrUp(int mx, int my, int button) {
		for (Setting s : settings) {
			s.onMouseMovedOrUp(mx, my, button);
		}
		super.mouseMovedOrUp(mx, my, button);
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		if (par1GuiButton.id == 0) {
			mc.displayGuiScreen(backScreen);
		}
	}

	@Override
	public void initGui() {
		// Calculate how many settings will fit on one page
		int totalSpace = height - SPACE_ABOVE_TOP_SETTING
				- SPACE_BELOW_SETTINGS;
		settingsPerPage = totalSpace / SPACE_BETWEEN_SETTINGS;

		// Set up settings
		for (Setting s : settings) {
			s.setX(width / 2);
			s.setY(height + 25);
		}

		// Create page buttons
		for (GuiButtonRect r : rectButtons) {
			r.removeActionListeners();
		}
		rectButtons.clear();
		if (getMaxPages() > 1) {
			final GuiSettings thisGui = this;
			GuiButtonRect nextPageButton = new GuiButtonRect(new Rectangle(
					width - PAGE_BUTTON_WIDTH, 0, PAGE_BUTTON_WIDTH, height),
					"Next");
			nextPageButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					thisGui.nextPage();
				}

			});
			rectButtons.add(nextPageButton);
			GuiButtonRect prevPageButton = new GuiButtonRect(new Rectangle(0,
					0, PAGE_BUTTON_WIDTH, height), "Prev");
			prevPageButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					thisGui.prevPage();
				}
			});
			rectButtons.add(prevPageButton);
		}

		// Pull up first page
		currPage = -1;
		setPage(0);

		// Add exit button
		controlList.add(new GuiButton(0, width / 2 - 100, height - 10
				- SPACE_BELOW_SETTINGS / 2, "Back"));
	}

	/**
	 * 
	 */
	protected void prevPage() {
		setPage(Math.max(0, currPage - 1));
	}

	/**
	 * 
	 */
	protected void nextPage() {
		setPage(Math.min(getMaxPages() - 1, currPage + 1));
	}

	/**
	 * @param i
	 */
	private void setPage(int page) {
		if (page == currPage) {
			return;
		}

		// Decide what settings to show
		currPageStartSetting = Math.max(0,
				Math.min(settings.size() - 1, page * settingsPerPage));
		currPageEndSetting = Math.min(settings.size() - 1, currPageStartSetting
				+ settingsPerPage - 1);

		// Retract any old settings
		for (Setting s : settings) {
			Tween.to(s, TuneTileGuiTweenAccessor.TWEEN_TYPE_Y, 300)
					.target(height + 25).ease(Linear.INOUT).start(tweenManager);
		}

		// Put down new settings
		int count = 0;
		for (int i = currPageStartSetting; i <= currPageEndSetting; i++) {
			// settings.get(i).setY(-25);
			Tween.to(settings.get(i), TuneTileGuiTweenAccessor.TWEEN_TYPE_Y,
					500)
					.target(SPACE_ABOVE_TOP_SETTING
							+ (SPACE_BETWEEN_SETTINGS * count))
					.delay(count * 100).ease(Quart.OUT).start(tweenManager);
			count++;
		}

		currPage = page;

	}

	@Override
	public void updateScreen() {
	}

	@Override
	public void onGuiClosed() {
	}

	private TweenManager tweenManager = new TweenManager();
	private long lastTweenUpdateTime = System.currentTimeMillis();
	private int PAGE_BUTTON_WIDTH = 60;

	private void updateTweens() {
		Timer t = Finder.getMCTimer();
		tweenManager.update(System.currentTimeMillis() - lastTweenUpdateTime);
		lastTweenUpdateTime = System.currentTimeMillis();
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		if (par2 == 1) {
			mc.displayGuiScreen(backScreen);
		}
	}
}
