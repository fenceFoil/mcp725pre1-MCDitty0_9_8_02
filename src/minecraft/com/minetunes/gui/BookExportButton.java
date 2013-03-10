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

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiScreenBook;
import net.minecraft.src.ItemStack;

import com.minetunes.Finder;
import com.minetunes.Minetunes;
import com.minetunes.books.BookWrapper;

/**
 * A button that appears in the upper left corner of a gui. Opens the MineTunes
 * menu when pressed. To add to a vanilla gui screen, put a new instance of this
 * into its controlList. No need to modify vanilla gui to do this!
 * 
 */
public class BookExportButton extends GuiButtonL {

	private GuiScreenBook bookGui;

	public BookExportButton() {
		// Arbitrary id number, not likely to conflict with vanilla gui
		super("exportBook", 30, 100, 20, 20, BookImportButton.tex, 32 + 0);
		id = 1398233128;
		// this.bookGui = bookGui;
	}

	public void setBookGui(GuiScreenBook bookGui) {
		this.bookGui = bookGui;
	}

	@Override
	public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3) {
		boolean result = super.mousePressed(par1Minecraft, par2, par3);
		if (result) {
			// Show a file selector
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					FileDialog d = new FileDialog((Frame) null,
							"Save book as .txt", FileDialog.SAVE);
					d.show();
					String exportFileName = d.getFile();
					if (exportFileName == null) {
						return;
					} else {
						File exportFile = new File(d.getDirectory()
								+ exportFileName);

						// First, get book to export
						ItemStack editorBook = null;
						try {
							editorBook = (ItemStack) Finder
									.getUniqueTypedFieldFromClass(
											GuiScreenBook.class,
											ItemStack.class, bookGui);
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						if (editorBook == null) {
							System.err
									.println("Could not get the book out of the book editor screen. No export.");
							return;
						}
						// Wrap it
						BookWrapper book = BookWrapper.wrapBook(editorBook);
						if (book == null) {
							System.err
									.println("Book in book editor screen is not wrappable (not a writable book). No export.");
							return;
						}

						// Ask player for export options
						String[] options = { "A space", "A new line",
								"[PageEnd]", "|", "Nothing" };
						int choice = JOptionPane
								.showOptionDialog(
										null,
										"What should be put between pages in the text file?",
										"Export Book", JOptionPane.OK_OPTION,
										JOptionPane.QUESTION_MESSAGE, null,
										options, 0);
						String separator = "";
						if (choice == JOptionPane.CLOSED_OPTION) {
							// Cancelled
							return;
						} else {
							// Set separator choice
							switch (choice) {
							case 0:
								separator = " ";
								break;
							case 1:
								separator = "\n";
								break;
							case 2:
								separator = "[PageEnd]";
								break;
							case 3:
								separator = "|";
								break;
							case 4:
							default:
								separator = "";
								break;
							}
						}

						// Get the book's text
						String bookText = book
								.getAllTextWithPageSeperator(separator);

						// Export the book's text
						try {
							exportFile.delete();
							exportFile.createNewFile();
							BufferedWriter writer = new BufferedWriter(
									new FileWriter(exportFile));
							writer.write(bookText);
							writer.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Minetunes.writeChatMessage(
									Minecraft.getMinecraft().theWorld,
									"Could not export book.");
						}
					}
				}

			});
			t.start();
			t.setName("MineTunes Book Exporter");
			// Import txt file
		}
		return result;
	}
}
