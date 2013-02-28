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
package com.minetunes.gui.help;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;

import com.minetunes.config.MinetunesConfig;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;

/**
 * @author William
 * 
 */
public class GuiHelpTopics extends GuiScreen {

	private static final int HEADER_Y = 30;
	private String topic;
	private GuiScreen backScreen;
	private Properties props;
	private File topicDir;
	private HashMap<String, String> subNames = new HashMap<String, String>();
	private int nextButtonID = 0;
	private HashMap<String, GuiButton> subButtons = new HashMap<String, GuiButton>();
	private HashMap<Integer, String> buttonIDToSub = new HashMap<Integer, String>();
	private boolean noSubs;

	public GuiHelpTopics(String topic, GuiScreen backScreen) {
		setTopic(topic);
		setBackScreen(backScreen);

		topicDir = new File(MinetunesConfig.getResourcesDir().getPath()
				+ File.separator + "help" + File.separator + topic);

		props = new Properties();
		try {
			props.load(new FileInputStream(new File(topicDir, "subs.txt")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		File[] subFolders = topicDir.listFiles();
		if (subFolders != null) {
			for (File f : subFolders) {
				if (f.isDirectory()) {
					subNames.put(f.getName(),
							props.getProperty(f.getName() + ".name"));
				}
			}
		} else {
			noSubs = true;
		}
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public GuiScreen getBackScreen() {
		return backScreen;
	}

	public void setBackScreen(GuiScreen backScreen) {
		this.backScreen = backScreen;
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		if (noSubs) {
			mc.displayGuiScreen(new GuiSimpleMessage(backScreen,
					"There's no help yet for " + topic + ", good luck!",
					0xffbb00));
		}

		drawDefaultBackground();

		// Draw header
		drawCenteredString(fontRenderer, "Help: " + props.getProperty("name"),
				width / 2, HEADER_Y, 0xffffff);

		// Draw buttons
		super.drawScreen(par1, par2, par3);
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		super.actionPerformed(par1GuiButton);

		if (par1GuiButton.id == -1) {
			mc.displayGuiScreen(backScreen);
		} else {
			String subTopic = buttonIDToSub.get(par1GuiButton.id);
			if (subTopic != null) {
				mc.displayGuiScreen(new GuiHelp(topic + File.separator
						+ subTopic, this));
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();

		nextButtonID = 1;
		for (String s : subNames.keySet()) {
			int id = ++nextButtonID;
			subButtons.put(s, new GuiButton(id, width / 2 - 100, HEADER_Y * id,
					subNames.get(s)));
			controlList.add(subButtons.get(s));
			buttonIDToSub.put(id, s);
		}

		controlList.add(new GuiButton(-1, width / 2 - 100, height - HEADER_Y,
				"Back"));
	}
}
