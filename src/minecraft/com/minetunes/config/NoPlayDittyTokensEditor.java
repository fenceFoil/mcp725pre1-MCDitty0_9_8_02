/**
 * Source: http://www.java.happycodings.com/Core_Java/code52.html
 * 
 * Modified for the singular purpose of editing noPlayTokens.txt
 */
package com.minetunes.config;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.TextArea;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;



public class NoPlayDittyTokensEditor extends Frame {
	String filename;
	TextArea tx;
	Clipboard clip = getToolkit().getSystemClipboard();

	public NoPlayDittyTokensEditor() {
		setLayout(new GridLayout(1, 1));
		tx = new TextArea();
		add(tx);
		MenuBar mb = new MenuBar();
		Menu F = new Menu("File");
//		MenuItem n = new MenuItem("New");
//		MenuItem o = new MenuItem("Open");
		MenuItem s = new MenuItem("Save");
		MenuItem e = new MenuItem("Exit");
//		n.addActionListener(new New());
//		F.add(n);
//		o.addActionListener(new Open());
//		F.add(o);
		s.addActionListener(new Save());
		F.add(s);
		e.addActionListener(new Exit());
		F.add(e);
		mb.add(F);
		Menu E = new Menu("Edit");
		MenuItem cut = new MenuItem("Cut");
		MenuItem copy = new MenuItem("Copy");
		MenuItem paste = new MenuItem("Paste");
		cut.addActionListener(new Cut());
		E.add(cut);
		copy.addActionListener(new Copy());
		E.add(copy);
		paste.addActionListener(new Paste());
		E.add(paste);
		mb.add(E);
		setMenuBar(mb);

		mylistener mylist = new mylistener();
		addWindowListener(mylist);
	}

	class mylistener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			//System.exit(0);
			dispose();
		}
	}

	class New implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			tx.setText(" ");
			setTitle(filename);
		}
	}

//	class Open implements ActionListener {
//		public void actionPerformed(ActionEvent e) {
//			openFile();
//		}
//	}

	class Save implements ActionListener {
		public void actionPerformed(ActionEvent e) {
//			FileDialog fd = new FileDialog(Notepad.this, "Save File",
//					FileDialog.SAVE);
//			fd.show();
			if (NoPlayTokens.noPlayTokensFile != null) {
				try {
					DataOutputStream d = new DataOutputStream(
							new FileOutputStream(NoPlayTokens.noPlayTokensFile));
					String line = tx.getText();
					BufferedReader br = new BufferedReader(new StringReader(
							line));
					while ((line = br.readLine()) != null) {
						d.writeBytes(line + "\r\n");
					}
					d.close();
				} catch (Exception ex) {
					ex.printStackTrace();
					System.out.println("File not found");
				}
				tx.requestFocus();
				NoPlayTokens.reloadNoPlayTokens();
			}
		}
	}

	class Exit implements ActionListener {
		public void actionPerformed(ActionEvent e) {
//			System.exit(0);
			dispose();
		}
	}

	void ReadFile() {
		BufferedReader d;
		StringBuffer sb = new StringBuffer();
		try {
			d = new BufferedReader(new FileReader(filename));
			String line;
			while ((line = d.readLine()) != null)
				sb.append(line + "\n");
			tx.setText(sb.toString());
			d.close();
		} catch (FileNotFoundException fe) {
			System.out.println("File not Found");
		} catch (IOException ioe) {
		}
	}

	public void openFile() {
		FileDialog fd = new FileDialog(NoPlayDittyTokensEditor.this, "select File",
				FileDialog.LOAD);
		fd.show();
		if (fd.getFile() != null) {
			filename = fd.getDirectory() + fd.getFile();
			setTitle(filename);
			ReadFile();
		}
		tx.requestFocus();
	}
	
	public void openFile(File file) {
		if (file != null) {
			filename = file.getPath();
			setTitle(file.getName());
			ReadFile();
		}
		tx.requestFocus();
	}

	class Cut implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String sel = tx.getSelectedText();
			StringSelection ss = new StringSelection(sel);
			clip.setContents(ss, ss);
			tx.replaceRange(" ", tx.getSelectionStart(), tx.getSelectionEnd());
		}
	}

	class Copy implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String sel = tx.getSelectedText();
			StringSelection clipString = new StringSelection(sel);
			clip.setContents(clipString, clipString);
		}
	}

	class Paste implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Transferable cliptran = clip.getContents(NoPlayDittyTokensEditor.this);
			try {
				String sel = (String) cliptran
						.getTransferData(DataFlavor.stringFlavor);
				tx.replaceRange(sel, tx.getSelectionStart(),
						tx.getSelectionEnd());
			} catch (Exception exc) {
				System.out.println("not string flavour");
			}
		}
	}

	public static void main(String args[]) {
		final Frame f = new NoPlayDittyTokensEditor();
		f.setSize(500, 400);
		f.setVisible(true);
		f.show();
		
		java.awt.EventQueue.invokeLater(new Runnable() {
		    @Override
		    public void run() {
		    	try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        f.toFront();
		        f.repaint();
		    }
		});
	}
}