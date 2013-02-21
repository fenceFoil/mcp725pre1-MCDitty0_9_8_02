package com.minetunes.test;
///**
// * 
// * Copyright (c) 2012 William Karnavas All Rights Reserved
// * 
// */
//
///**
// * 
// * This file is part of MineTunes.
// * 
// * MineTunes is free software: you can redistribute it and/or modify it under
// * the terms of the GNU Lesser General Public License as published by the
// * Free Software Foundation, either version 3 of the License, or (at your
// * option) any later version.
// * 
// * MineTunes is distributed in the hope that it will be useful, but WITHOUT
// * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
// * License for more details.
// * 
// * You should have received a copy of the GNU Lesser General Public License
// * along with MineTunes. If not, see <http://www.gnu.org/licenses/>.
// * 
// */
//package com.wikispaces.MineTunes.test;
//
//import javax.swing.JFrame;
//import javax.swing.JEditorPane;
//import java.awt.BorderLayout;
//import javax.swing.JCheckBox;
//import javax.swing.border.BevelBorder;
//import javax.swing.border.MatteBorder;
//import java.awt.Color;
//import javax.swing.JTextField;
//import javax.swing.JLabel;
//import java.awt.Font;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//
//import javax.swing.SwingConstants;
//import javax.swing.JButton;
//
///**
// * A simple test of faster ways to enter MusicStrings, before creating a full
// * Ditty Editor.
// * 
// * @author William
// * 
// */
//public class TestFastMusicEntry extends JFrame {
//	private JTextField textField;
//
//	public TestFastMusicEntry() {
//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//		JEditorPane editorPane = new JEditorPane();
//		editorPane.setFont(new Font("Monospaced", Font.PLAIN, 16));
//		editorPane.setBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(0,
//				0, 0)));
//		getContentPane().add(editorPane, BorderLayout.CENTER);
//
//		JButton chckbxSpeedenter = new JButton("SpeedEnter");
//		getContentPane().add(chckbxSpeedenter, BorderLayout.SOUTH);
//		chckbxSpeedenter.addActionListener(new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				SpeedEnterDialog d = new SpeedEnterDialog(this);
//				d.setVisible(true);
//			}
//		});
//
//		textField = new JTextField();
//		textField.setHorizontalAlignment(SwingConstants.CENTER);
//		textField.setFont(new Font("Monospaced", Font.PLAIN, 25));
//		getContentPane().add(textField, BorderLayout.NORTH);
//		textField.setColumns(10);
//
//		JLabel leftLabel = new JLabel("New label");
//		getContentPane().add(leftLabel, BorderLayout.WEST);
//
//		JLabel rightLabel = new JLabel("New label");
//		getContentPane().add(rightLabel, BorderLayout.EAST);
//	}
//
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		TestFastMusicEntry t = new TestFastMusicEntry();
//		t.setSize(700, 700);
//		t.setVisible(true);
//	}
//
//}
