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
package com.minetunes.books;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.src.ChatAllowedCharacters;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.NBTTagString;
import net.minecraft.src.Packet;
import net.minecraft.src.Packet250CustomPayload;

import com.minetunes.Finder;
import com.minetunes.config.MinetunesConfig;

/**
 * An abstraction of an ItemStack representing a book & quill or a signed book
 * which provides easy access to pages and the combined text of all pages, among
 * other data.<br>
 * <br>
 * The BookWrapper contains a copy of the book's pages and data, which can be
 * manipulated independently of the wrapped book's data.<br>
 * <br>
 * BookWrapper does not directly change the properties of the ItemStack passed
 * into it; the only way to do actually "change the actual book" wrapped is to
 * send the changes to the server, in the same way as GuiScreenBook.<br>
 * <br>
 * The mechanism for doing the above might be added to this class someday.<br>
 * <br>
 * Cannot be instantiated by constructor; use wrapBook(ItemStack) instead which
 * will also tell you if a stack cannot be wrapped as a book.<br>
 */
public class BookWrapper {
	/**
	 * The Minecraft ID numbers for book items of different kinds
	 */
	public static final int ITEM_ID_BOOK_AND_QUILL = 386;
	public static final int ITEM_ID_SIGNED_BOOK = 387;

	public static final int MAX_BOOK_PAGES = 50;

	/**
	 * The book wrapped by this object.
	 */
	private ItemStack bookItem;
	private NBTTagList pagesTagList;
	private int totalPages;
	private boolean bookUnsigned = true;
	private ArrayList<String> pages = new ArrayList<String>(MAX_BOOK_PAGES);

	/**
	 * The version of valid page length detector to use
	 */
	private static int usePageLengthValidatorVersion = 1;

	// Determines whether I forgot to add a new "validPageLength" method for
	// this version of MC
	static {
		if (MinetunesConfig.MC_CURRENT_VERSION.equalsIgnoreCase("1.4.6")) {
			// All is right
			usePageLengthValidatorVersion = 1;
		} else {
			// Have not yet updated the page length validator
			// Warn person updating
			System.err
					.println("MineTunes Mod: WARNING TO CODER: Please confirm that the page length validator (in GuiScreenBook.appendToCurrPage(String)) has not changed from pervious versions of Minecraft.");
		}
	}

	/**
	 * Do not call directly -- use getWrappedBook or similar.
	 * 
	 * @param itemStack
	 */
	protected BookWrapper(ItemStack itemStack) {
		setBookItem(itemStack);

		// Try to load the pages of the book
		if (itemStack.hasTagCompound()) {
			// Get the list of page data tags
			NBTTagCompound bookCompoundTag = itemStack.getTagCompound();
			pagesTagList = bookCompoundTag.getTagList("pages");

			if (pagesTagList == null) {
				totalPages = 0;
			} else {
				pagesTagList = (NBTTagList) pagesTagList.copy();
				totalPages = pagesTagList.tagCount();

				if (totalPages < 0) {
					totalPages = 0;
				}
			}
		} else {
			// The item doesn't have any enchantment information, so no book
			// text either.
			totalPages = 0;
		}

		// With the page tags at hand, try to load the pages of the book into
		// Strings
		for (int i = 0; i < totalPages; i++) {
			String pageText = pagesTagList.tagAt(i).toString();

			// To maintain compatability with older music books,
			// Replace MCDitty tags with BookTune references
			pageText = replaceOldMCDittyReferences(pageText);

			pages.add(pageText);
		}
	}

	/**
	 * Replaces old BookTune books' main tags ("<MCDitty>") with ("<BookTune")
	 * tags
	 * 
	 * @param in
	 * @return
	 */
	private String replaceOldMCDittyReferences(String in) {
		return in.replaceAll("<MCDitty", "<bookTune")
				.replaceAll("<mcditty", "<bookTune")
				.replaceAll("<mcDitty", "<bookTune")
				.replaceAll("</MCDitty", "</bookTune")
				.replaceAll("</mcditty", "</bookTune")
				.replaceAll("</mcDitty", "</bookTune");
	}

	/**
	 * Syncs any modified pages saved in the WrappedBook into the book that this
	 * object wraps as NBT tags.
	 * 
	 * DOES NOT CURRENTLY CHECK THAT PAGES ARE < 256 chracters, DOES check for
	 * limit 50 pages.
	 */
	public void flushPages() {
		NBTTagList pageList = new NBTTagList("pages");
		int currPage = 1;
		for (String s : pages) {
			// Save page
			NBTTagString pageTag = new NBTTagString(Integer.toString(currPage),
					"");
			pageTag.data = replaceOldMCDittyReferences(s);
			pageList.appendTag(pageTag);

			if (currPage >= 50) {
				break;
			} else {
				currPage++;
			}
		}

		if (bookItem.getTagCompound() == null) {
			bookItem.setTagCompound(new NBTTagCompound());
		}
		bookItem.getTagCompound().setTag("pages", pageList);
		pagesTagList = bookItem.getTagCompound().getTagList("pages");
	}

	/**
	 * Does processing to set various fields depending on what kind of book is
	 * set
	 * 
	 * @param itemStack
	 */
	protected void setBookItem(ItemStack itemStack) {
		if (itemStack.itemID == ITEM_ID_SIGNED_BOOK) {
			bookUnsigned = false;
		} else {
			bookUnsigned = true;
		}
		bookItem = itemStack;
	}

	/**
	 * Gets the book this is wrapping.
	 */
	public ItemStack getBookItem() {
		return bookItem;
	}

	/**
	 * Use instead of a constructor.
	 * 
	 * @param itemStack
	 * @return a BookWrapper of the itemStack if the stack if a writable book of
	 *         some kind, or null.
	 */
	public static BookWrapper wrapBook(ItemStack itemStack) {
		if (isItemStackBook(itemStack)) {
			return new BookWrapper(itemStack);
		} else {
			return null;
		}
	}

	/**
	 * @return true if the stack is a book and quill or writable book, otherwise
	 *         false
	 */
	public static boolean isItemStackBook(ItemStack itemStack) {
		if (itemStack == null) {
			return false;
		}

		if (itemStack.itemID == ITEM_ID_BOOK_AND_QUILL
				|| itemStack.itemID == ITEM_ID_SIGNED_BOOK) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns a string with the text of one page of a book.
	 * 
	 * @param pageNum
	 * @return
	 */
	public String getPageText(int pageNum) {
		return pages.get(pageNum);
	}

	/**
	 * Returns a shallow copy of a list of the book's pages.
	 * 
	 * @return
	 */
	public ArrayList<String> getAllPages() {
		return (ArrayList<String>) pages.clone();
	}

	/**
	 * Returns all the text in a book. All pages are combined and returned. If a
	 * page ends without a newline or a space, a newline is added to the end
	 * before appending the next page's text.
	 * 
	 * Removes all whitespace at end of book.
	 * 
	 * @return all the text in the book. If there is none, returns an empty
	 *         string ("").
	 */
	public String getAllText(boolean ensureANewlineBetweenPages) {
		if (ensureANewlineBetweenPages) {
			return getAllTextWithPageSeperator("\n");
		} else {
			return getAllTextWithPageSeperator("");
		}
	}

	public String getAllTextWithPageSeperator(String string) {
		StringBuffer allTextInBook = new StringBuffer();

		// Add each page to the buffer
		for (String s : pages) {
			if (s.length() > 0) {
				allTextInBook.append(s);
				if (s.substring(s.length() - 1).matches("\\s")) {
					// If the last character is whitespace, do not add a space
					// to the end
				} else {
					// Add a space
					if (string != null) {
						allTextInBook.append(string);
					}
				}
			} else {
				// Nothing on page. Do nothing.
			}
		}

		// Remove all whitespace at end of text
		while (allTextInBook.length() > 0
				&& (allTextInBook.substring(allTextInBook.length() - 1)
						.matches("\\s"))) {
			// while there is still a whitespace character at end of text,
			// remove it.
			allTextInBook.deleteCharAt(allTextInBook.length() - 1);
		}

		return allTextInBook.toString();
	}

	/**
	 * Attempts to fit as much of the given text into the wrapped book as
	 * possible.
	 * 
	 * Wipes all current book text stored in this wrapper.
	 * 
	 * The normal call should probably have vanillaCompatible=true and
	 * allowSplitWords=false to make a normal-looking book.
	 * 
	 * @param text
	 *            the text to try to fit
	 * @param vanillaCompatible
	 *            if true, the resulting book's contents will look like a
	 *            product of the vanilla book editor to servers and the casual
	 *            reader. Otherwise, will use the full 255 character limit to
	 *            eek the last drop of storage out of the book (an could send up
	 *            a red flag to a server) making the book look strange in the
	 *            vanilla reader.
	 * 
	 *            Also, this method is probably much faster when this is false.
	 * @param allowSplitWords
	 *            if true, may break words across multiple pages (to a minimum
	 *            result of one very, very long word per page).
	 * @return all text that didn't fit into this book, or null if the text did
	 *         fit.
	 */
	public String fillWithText(String text, boolean vanillaCompatible,
			boolean allowSplitWords, boolean filterChatChars) {
		StringBuffer textBuffer = new StringBuffer(text);

		// Clear out the old book text
		totalPages = 0;
		pages.clear();

		// Iterate over pages until the end of the book is reached or the text
		// to fill with has run out
		int currPage = 0;
		while (currPage < 50 && textBuffer.length() > 0) {
			text = ChatAllowedCharacters.filerAllowedCharacters(text);

			// TODO: Respect this command
			if (allowSplitWords || !allowSplitWords) {
				// Try to cram as much text as possible onto page irrespective
				// of word breaks
				if (vanillaCompatible) {
					// Put as much on as the valid length detector will allow
					// TODO: make more efficient?
					for (int tryLength = 255; tryLength >= 0; tryLength--) {
						if (isPageValidLength(textBuffer.substring(0,
								Math.min(tryLength, textBuffer.length())))) {
							// Valid length found!
							// Fill page, move on.
							if (pages.size() <= currPage) {
								pages.add("");
							}
							pages.set(
									currPage,
									textBuffer.substring(
											0,
											Math.min(tryLength,
													textBuffer.length())));
							textBuffer.delete(0,
									Math.min(tryLength, textBuffer.length()));
							break;
						} else {
							// Not found. Try again next loop...
						}
					}
				} else {
					// Put as much on as 255 characters will hold.
					String currPageText;
					if (textBuffer.length() > 255) {
						// cannot fit all on this page. Take as much as can fit
						// on this page from the text.
						currPageText = textBuffer.substring(0, 255);
						textBuffer.delete(0, 255);
						// System.out
						// .println("BookWrapper.fillWithText a pagetextlength = "
						// + currPageText.length());
					} else {
						// All remaining text fits on page
						currPageText = textBuffer.toString();
						textBuffer.delete(0, textBuffer.length());
					}

					// With pageText loaded, put the text on page
					if (pages.size() <= currPage) {
						pages.add("");
					}
					pages.set(currPage, currPageText);
				}
			} else {
				// Make sure that words are not broken across multiple pages if
				// possible
				// TODO
				if (vanillaCompatible) {
					// Put as much on as the valid length detector will allow
				} else {
					// Put as much on as 255 characters will hold.
					// Minimum one word (or word chunk)
				}
			}

			// Move on to next page
			currPage++;
		}

		if (textBuffer.length() > 0) {
			return textBuffer.toString();
		} else {
			return null;
		}
	}

	/**
	 * Tries to use the correct valid page checker for this versino of
	 * minecraft, otherwise returns true automatically.
	 * 
	 * 1.3.2 defines this as being "less than 256 characters AND whether a
	 * fontRenderer's word wrapping function says this string (with a underscore
	 * cursor) fits in an area 118x118 pixels".
	 * 
	 * Bugs in 1.3.2 include multiple newlines not being rendered/counted for in
	 * this method.
	 * 
	 * This and any future variants of this method should be named by Minecraft
	 * version, since I have a suspicion that this arbitrary method of
	 * determining validity may be subject to change.
	 * 
	 * @param pageText
	 * @return
	 */
	public boolean isPageValidLength(String pageText) {
		if (usePageLengthValidatorVersion == 1) {
			return isPageValidLengthFor1_3_2(pageText);
		} else {
			return true;
		}
	}

	/**
	 * Returns whether the given String would be accepted by the vanilla book
	 * editor as a valid page length.
	 * 
	 * 1.3.2 defines this as being "less than 256 characters AND whether a
	 * fontRenderer's word wrapping function says this string (with a underscore
	 * cursor) fits in an area 118x118 pixels".
	 * 
	 * This and any future variants of this method should be named by Minecraft
	 * version, since I have a suspicion that this arbitrary method of
	 * determining validity may be subject to change.
	 * 
	 * @param pageText
	 * @return
	 */
	private boolean isPageValidLengthFor1_3_2(String pageText) {
		// 118 is width of area of text here
		int textHeightOnPage = Minecraft.getMinecraft().fontRenderer
				.splitStringWidth(pageText + "\u00a70_", 118);

		// 118 is height of text area here
		if (textHeightOnPage <= 118 && pageText.length() < 256) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isBookUnsigned() {
		return bookUnsigned;
	}

	/**
	 * Automatically flushes book if possible. Sends book to the server, saving
	 * pages if the book is unsigned. Removes null and empty pages from book, IN
	 * A WAY THAT IS NOT SYNCED PROPERLY WITH THIS WRAPPEDBOOK'S COPY OF THE
	 * BOOK'S PAGES.
	 * 
	 * @param signingBook
	 *            ONLY FALSE CURRENTLY SUPPORTED
	 */
	public void sendBookToServer(boolean signingBook) {
		flushPages();

		if (isBookUnsigned()) {
			if (pagesTagList != null) {
				while (pagesTagList.tagCount() > 1) {
					NBTTagString currPageTag = (NBTTagString) pagesTagList
							.tagAt(pagesTagList.tagCount() - 1);

					if (currPageTag.data != null
							&& currPageTag.data.length() != 0) {
						break;
					} else {
						pagesTagList.removeTag(pagesTagList.tagCount() - 1);
					}
				}

				if (bookItem.hasTagCompound()) {
					NBTTagCompound bookItemTag = this.bookItem.getTagCompound();
					bookItemTag.setTag("pages", pagesTagList);
				} else {
					// Remove page tag
					// TODO: Sync this with WrappedBook's pages.
					bookItem.setTagInfo("pages", pagesTagList);
				}

				String serverInstructionHeader = "MC|BEdit";

				// if (signingBook) {
				// var8 = "MC|BSign";
				// this.bookItem.func_77983_a("author", new NBTTagString(
				// "author", GetMinecraft.instance().thePlayer.username));
				// this.bookItem.func_77983_a("title", new NBTTagString(
				// "title", this.bookTitle.trim()));
				// this.bookItem.itemID = Item.writtenBook.shiftedIndex;
				// }

				ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				DataOutputStream dataOut = new DataOutputStream(byteOut);

				try {
					Packet.writeItemStack(this.bookItem, dataOut);
					Minecraft
							.getMinecraft()
							.getSendQueue()
							.addToSendQueue(
									new Packet250CustomPayload(
											serverInstructionHeader, byteOut
													.toByteArray()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
