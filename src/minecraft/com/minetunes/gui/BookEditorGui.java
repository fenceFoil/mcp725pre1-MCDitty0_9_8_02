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
package com.minetunes.gui;

import java.util.LinkedList;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiScreenBook;
import net.minecraft.src.ItemStack;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.minetunes.Finder;
import com.minetunes.books.BookDittyXMLParser;
import com.minetunes.books.BookWrapper;

/**
 * @author William
 * 
 */
public class BookEditorGui extends GuiScreen {

	private GuiScreen backScreen;
	private BookWrapper book;

	public int tex = Minecraft.getMinecraft().renderEngine
			.getTexture("/com/minetunes/resources/textures/signEditor1.png");

	/**
	 * @param bookGui
	 */
	public BookEditorGui(GuiScreenBook backScreen) {
		setBackScreen(backScreen);
		ItemStack bookItem = null;
		try {
			bookItem = (ItemStack) Finder.getUniqueTypedFieldFromClass(
					GuiScreenBook.class, ItemStack.class, backScreen);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (bookItem != null) {
			book = BookWrapper.wrapBook(bookItem);
		}

		// Parse book
		LinkedList<Element> foundContainers = null;
		try {
			foundContainers = BookDittyXMLParser.getDittyXMLContainers(book);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}

	}

	public GuiScreen getBackScreen() {
		return backScreen;
	}

	public void setBackScreen(GuiScreen backScreen) {
		this.backScreen = backScreen;
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		drawDefaultBackground();

	}

	@Override
	protected void keyTyped(char par1, int par2) {
		// TODO Auto-generated method stub
		super.keyTyped(par1, par2);
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		// TODO Auto-generated method stub
		super.mouseClicked(par1, par2, par3);
	}

	@Override
	protected void mouseMovedOrUp(int par1, int par2, int par3) {
		// TODO Auto-generated method stub
		super.mouseMovedOrUp(par1, par2, par3);
	}

	@Override
	public void initGui() {
		super.initGui();
	}

	@Override
	public void updateScreen() {
		// TODO Auto-generated method stub
		super.updateScreen();
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();

		mc.displayGuiScreen(backScreen);
	}

}
