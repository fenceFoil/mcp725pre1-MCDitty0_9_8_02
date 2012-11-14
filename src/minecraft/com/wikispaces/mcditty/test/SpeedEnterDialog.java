///**
// * 
// * Copyright (c) 2012 William Karnavas All Rights Reserved
// * 
// */
//
///**
// * 
// * This file is part of MCDitty.
// * 
// * MCDitty is free software: you can redistribute it and/or modify it under
// * the terms of the GNU Lesser General Public License as published by the
// * Free Software Foundation, either version 3 of the License, or (at your
// * option) any later version.
// * 
// * MCDitty is distributed in the hope that it will be useful, but WITHOUT
// * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
// * License for more details.
// * 
// * You should have received a copy of the GNU Lesser General Public License
// * along with MCDitty. If not, see <http://www.gnu.org/licenses/>.
// * 
// */
//package com.wikispaces.mcditty.test;
//
//import javax.swing.JDialog;
//import java.awt.event.KeyAdapter;
//import java.awt.event.KeyEvent;
//import javax.swing.JLabel;
//import java.awt.BorderLayout;
//import java.awt.FlowLayout;
//import javax.swing.BoxLayout;
//import java.awt.GridBagLayout;
//import java.awt.GridBagConstraints;
//import java.awt.Insets;
//import javax.swing.GroupLayout;
//import javax.swing.GroupLayout.Alignment;
//import java.awt.GridLayout;
//
///**
// * @author William
// * 
// */
//public class SpeedEnterDialog extends JDialog {
//	private JLabel label1;
//	private JLabel label2;
//	
//	private int state = 0;
//	private int sharpFlatNatState = 0;
//	private int octave = 0;
//
//	public SpeedEnterDialog(TestFastMusicEntry test) {
//
//		label1 = new JLabel("New label");
//
//		label2 = new JLabel("New label");
//
//		getContentPane().setLayout(new GridLayout(0, 2, 0, 0));
//		getContentPane().add(label1);
//		getContentPane().add(label2);
//		getContentPane().addKeyListener(new KeyAdapter() {
//			@Override
//			public void keyPressed(KeyEvent e) {
//			}
//
//			@Override
//			public void keyReleased(KeyEvent e) {
//			}
//
//			@Override
//			public void keyTyped(KeyEvent e) {
//			}
//
//		});
//	}
//
//}
