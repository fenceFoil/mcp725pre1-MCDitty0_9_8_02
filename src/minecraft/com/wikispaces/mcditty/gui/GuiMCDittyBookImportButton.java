/**
 * Copyright (c) 2012 William Karnavas 
 * All Rights Reserved
 */

/**
 * 
 * This file is part of MCDitty.
 * 
 * MCDitty is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MCDitty is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MCDitty. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.wikispaces.mcditty.gui;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.swing.JOptionPane;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiScreenBook;
import net.minecraft.src.ItemStack;
import net.minecraft.src.StatCollector;

import com.wikispaces.mcditty.GetMinecraft;
import com.wikispaces.mcditty.MCDitty;
import com.wikispaces.mcditty.books.WrappedBook;

/**
 * A button that appears in the upper left corner of a gui. Opens the MCDitty
 * menu when pressed. To add to a vanilla gui screen, put a new instance of this
 * into its controlList. No need to modify vanilla gui to do this!
 * 
 */
public class GuiMCDittyBookImportButton extends GuiButton {

	private GuiScreenBook bookGui;

	public GuiMCDittyBookImportButton() {
		// Arbitrary id number, not likely to conflict with vanilla gui
		super(190834533, 5, 100, 70, 20, "Import .txt");
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
			GetMinecraft.instance().displayGuiScreen(null);
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					FileDialog d = new FileDialog((Frame) null,
							"Import .txt into a book:", FileDialog.LOAD);
					d.show();
					String importFileName = d.getFile();
					if (importFileName == null) {
						return;
					} else {
						File importFile = new File(d.getDirectory()+importFileName);

						// Ask user for more info
						boolean overstuffPages = false;
						
						// Removed dialog
//						String[] options = { "Normal", "Overstuffed", "Cancel" };
//						int choice = JOptionPane.showOptionDialog(null,
//								"How full should each page be?",
//								"Importing Book", JOptionPane.OK_OPTION,
//								JOptionPane.QUESTION_MESSAGE, null, options, 0);
//						if (choice == JOptionPane.CLOSED_OPTION || choice == 2) {
//							// Cancel
//							return;
//						} if (choice == 1) {
//							overstuffPages = true;
//						}

						// First, get book to fill
						ItemStack editorBook = null;
						try {
							editorBook = (ItemStack) GetMinecraft
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
									.println("Could not get the book out of the book editor screen. No import.");
							return;
						}
						// Wrap it
						WrappedBook book = WrappedBook.wrapBook(editorBook);
						if (book == null) {
							System.err
									.println("Book in book editor screen is not wrappable (not a writable book). No import.");
							return;
						}
						StringBuffer txtFileBuffer = new StringBuffer();
						// Load the .txt file
						if (importFile.length() > 1000000) {
							// If more than 1MB, don't even try
							txtFileBuffer
									.append("File is too big to import: More than 1 megabyte.");
						} else {
							try {
								BufferedReader txtFileIn = new BufferedReader(
										new FileReader(importFile));
								while (true) {
									String lineIn = txtFileIn.readLine();
									if (lineIn == null) {
										break;
									} else {
										txtFileBuffer.append(lineIn).append(
												"\n");
									}
								}
								txtFileIn.close();
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						// Fill the wrapped book
						book.fillWithText(txtFileBuffer.toString(), !overstuffPages,
								false, true);
						// Save the imported book to the server
						book.sendBookToServer(false);
						// Reopen book gui
						//MCDitty.reopenBookGui(bookGui, book);
					}
				}

			});
			t.start();
			// Import txt file
		}
		return result;
	}
}
