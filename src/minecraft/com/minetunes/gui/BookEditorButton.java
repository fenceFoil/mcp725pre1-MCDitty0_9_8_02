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

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiScreenBook;

/**
 * @author William
 *
 */
public class BookEditorButton extends GuiButtonL {
	
	private GuiScreenBook bookGui;

	/**
	 * @param action
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param iconTex
	 * @param iconIndex
	 */
	public BookEditorButton() {
		super("bookEditor", 5, 75, 20, 20, BookImportButton.tex, 32+2);
		id = 2390412;
	}

	public GuiScreenBook getBookGui() {
		return bookGui;
	}

	public void setBookGui(GuiScreenBook bookGui) {
		this.bookGui = bookGui;
	}

	@Override
	public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3) {
		boolean result = super.mousePressed(par1Minecraft, par2, par3);
		if (result) {
			Minecraft.getMinecraft().displayGuiScreen(new BookEditorGui(bookGui));
		}
		return result;
	}

}
