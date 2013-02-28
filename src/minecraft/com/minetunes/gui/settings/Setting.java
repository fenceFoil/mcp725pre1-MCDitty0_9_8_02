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

import java.io.IOException;

import aurelienribon.tweenengine.Tween;

import com.minetunes.Color4f;
import com.minetunes.config.MinetunesConfig;
import com.minetunes.gui.GuiSlider;
import com.minetunes.gui.SettingsGui;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Gui;
import net.minecraft.src.GuiButton;

/**
 * @author William
 * 
 */
public class Setting extends Gui {
	private static final int CENTER_SPACE = 10;
	private String labelText;
	private String settingKey;
	private SettingType type;
	private int x = 0;
	private int y = 0;
	private GuiButton button;
	private boolean valueInverted;
	
	static {
		Tween.registerAccessor(Setting.class, new SettingTweenAccessor());
	}

	public Setting(String label, String key, SettingType settingType) {
		setLabelText(label);
		setSettingKey(key);
		setType(settingType);
		valueInverted = false;

		button = setUpButton(settingType);
	}

	public Setting(String label, String key, SettingType settingType,
			boolean invert) {
		this(label, key, settingType);
		setValueInverted(invert);
	}

	public void draw(int mx, int my) {
		Minecraft mc = Minecraft.getMinecraft();

		if (button != null) {
			if (button instanceof GuiSlider) {
				GuiSlider s = (GuiSlider) button;
				s.sliderValue = MinetunesConfig.getFloat(settingKey
						+ ".sliderPos");
			} else {
				button.displayString = SettingType.getButtonLabel(type,
						getValueObj(), valueInverted);
			}
			button.drawButton(mc, mx, my);
		}

		int stringWidth = mc.fontRenderer.getStringWidth(labelText);
		drawString(mc.fontRenderer, labelText, x - CENTER_SPACE - stringWidth,
				y + 4, 0xffffff);
	}

	/**
	 * @return
	 */
	private Object getValueObj() {
		switch (type) {
		case BOOLEAN:
		case BOOLEAN_ENABLED_DISABLED:
		case BOOLEAN_ON_OFF:
		case BOOLEAN_YES_NO:
			return MinetunesConfig.getBoolean(settingKey);
		case COLOR:
			return MinetunesConfig.getFloat(settingKey + ".sliderPos");
		case INTEGER_SHORT_TIME:
			return MinetunesConfig.getInt(settingKey);
		default:
			break;

		}
		return null;
	}

	public void onMousePressed(int mx, int my, int mb) {
		if (button != null) {
			boolean clicked = button.mousePressed(Minecraft.getMinecraft(), mx,
					my);
			if (clicked) {
				Minecraft.getMinecraft().sndManager.playSoundFX("random.click",
						1.0F, 1.0f);
				switch (type) {
				case BOOLEAN:
				case BOOLEAN_ENABLED_DISABLED:
				case BOOLEAN_ON_OFF:
				case BOOLEAN_YES_NO:
					MinetunesConfig.toggleBoolean(settingKey);
					break;
				case COLOR:
					// TODO
					break;
				case INTEGER_SHORT_TIME:
					MinetunesConfig.setInt(settingKey, SettingType
							.nextShortTimeIntValue(MinetunesConfig
									.getInt(settingKey)));
					break;
				default:
					break;
				}
				try {
					MinetunesConfig.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void onMouseReleased(int mx, int my) {
		if (button != null) {
			button.mouseReleased(mx, my);
		}
	}

	public void onMouseMovedOrUp(int mx, int my, int mb) {
		if (button instanceof GuiSlider) {
			GuiSlider signColorSlider = (GuiSlider) button;
			boolean draggingSignColorSlider = signColorSlider.dragging;
			if (draggingSignColorSlider && signColorSlider.dragging == false) {
				// Color was selected
				MinetunesConfig
						.setString(
								settingKey,
								new Color4f(
										sineBowColor((float) (signColorSlider.sliderValue * 2 * Math.PI)))
										.toString());
				MinetunesConfig.setFloat(settingKey + ".sliderPos",
						signColorSlider.sliderValue);
				try {
					MinetunesConfig.flush();
				} catch (IOException e) {
					//e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @param settingType
	 * @return
	 */
	private GuiButton setUpButton(SettingType type) {
		GuiButton button = null;
		switch (type) {
		case BOOLEAN:
		case BOOLEAN_ENABLED_DISABLED:
		case BOOLEAN_ON_OFF:
		case BOOLEAN_YES_NO:
			button = new GuiButton(settingKey.hashCode(), 0, -50, 100, 20,
					type.getButtonLabel(type,
							MinetunesConfig.getBoolean(settingKey),
							valueInverted));
			break;
		case COLOR:
			button = new GuiSlider(settingKey.hashCode(), 0, -50, "",
					MinetunesConfig.getFloat(settingKey + ".sliderPos"));
			break;
		case INTEGER_SHORT_TIME:
			button = new GuiButton(settingKey.hashCode(), 0, -50, 100, 20,
					type.getButtonLabel(type,
							MinetunesConfig.getInt(settingKey), valueInverted));
			break;
		default:
			break;
		}
		return button;
	}

	public String getLabelText() {
		return labelText;
	}

	public void setLabelText(String labelText) {
		this.labelText = labelText;
	}

	public String getSettingKey() {
		return settingKey;
	}

	public void setSettingKey(String settingKey) {
		this.settingKey = settingKey;
	}

	public SettingType getType() {
		return type;
	}

	public void setType(SettingType type) {
		this.type = type;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
		if (button != null) {
			button.xPosition = x + CENTER_SPACE;
		}
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
		if (button != null) {
			button.yPosition = y;
		}
	}

	public boolean isValueInverted() {
		return valueInverted;
	}

	public void setValueInverted(boolean valueInverted) {
		this.valueInverted = valueInverted;
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
