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
package com.minetunes.blockTune.testgui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.border.EmptyBorder;

import org.jfugue.elements.Instrument;

import com.minetunes.blockTune.BlockTuneAccess;
import com.minetunes.blockTune.BlockTunePlayer;
import com.minetunes.blockTune.Frame;
import com.minetunes.blockTune.Scale;
import com.minetunes.ditty.MIDISynthPool;
import com.minetunes.ditty.NoNewSynthIndicator;

/**
 * @author William
 * 
 */
public class BlockTuneTestGui extends JFrame implements BlockTuneAccess,
		NoNewSynthIndicator {
	
	public static final int[] dist = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3};

	private JPanel contentPane;
	private JPanel gridPanel;
	private JPanel instrumentPanel;
	private JLabel keyLabel;
	private JSpinner[][] grid = new JSpinner[16][16];
	private JComboBox<String>[] instrumentBoxes = new JComboBox[4];
	private Scale scale = Scale.PENTATONIC_MAJOR;

	private BlockTunePlayer player = new BlockTunePlayer(this,
			new MIDISynthPool(this));

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BlockTuneTestGui frame = new BlockTuneTestGui();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public BlockTuneTestGui() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		gridPanel = new JPanel();
		contentPane.add(gridPanel, BorderLayout.CENTER);
		gridPanel.setLayout(new GridLayout(16, 16, 0, 0));

		// Fill gridPanel
		Random rand = new Random();
		for (int x = 0; x < 16; x++) {
			for (int y = 0; y < 16; y++) {
				JSpinner spinner = new JSpinner();
				spinner.setValue(dist[rand.nextInt(dist.length)]);
				grid[x][y] = spinner;
				gridPanel.add(spinner);
			}
		}

		instrumentPanel = new JPanel();
		contentPane.add(instrumentPanel, BorderLayout.SOUTH);
		for (int i = 0; i < 4; i++) {
			JComboBox<String> box = new JComboBox<String>(
					Instrument.INSTRUMENT_NAME);
			instrumentPanel.add(box);
			instrumentBoxes[i] = box;

			box.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					JComboBox b = (JComboBox) arg0.getSource();
					for (int i = 0; i < 4; i++) {
						if (instrumentBoxes[i] == b) {
							player.setInstrument(i, b.getSelectedIndex());
						}
					}
				}
			});
		}

		JPanel scalePanel = new JPanel();
		contentPane.add(scalePanel, BorderLayout.NORTH);

		JComboBox comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] { "Penta Maj",
				"Penta Min", "Dia Maj", "Dia Min", "Chromatic" }));
		scalePanel.add(comboBox);
		comboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				switch (((JComboBox) arg0.getSource()).getSelectedIndex()) {
				case 0:
					scale = Scale.PENTATONIC_MAJOR;
					break;
				case 1:
					scale = Scale.PENTATONIC_MINOR;
					break;
				case 2:
					scale = Scale.DIATONIC_MAJOR;
					break;
				case 3:
					scale = Scale.DIATONIC_MINOR;
					break;
				case 4:
					scale = Scale.CHROMATIC;
					break;
				}
			}
		});

		JSpinner spinner = new JSpinner();
		scalePanel.add(spinner);

		keyLabel = new JLabel("New label");
		scalePanel.add(keyLabel);

		// Start player
		player.start();
	}

	@Override
	public boolean getNewSynthsAllowed() {
		return true;
	}
	@Override
	public int getFrameCount() {
		// TODO Auto-generated method stub
		return 16;
	}
	@Override
	public boolean isPaused() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean isLooping() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public double getMasterVolume() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public Frame getFrame(int frameNum) {
		Frame frame = new Frame(1);
		for (int y = 0; y < 16; y++) {
			if ((Integer) grid[y][frameNum].getValue() >= 0
					&& (Integer) (grid[y][frameNum].getValue()) <= 4) {
				frame.addNoteStart((Integer) grid[y][frameNum].getValue(),
						(byte) scale.getNoteForStep(grid[y].length - y));
			}
		}
		return frame;
	}

	@Override
	public void onFramePlayed(Frame frame, int frameNum) {
		// TODO Auto-generated method stub

	}

	@Override
	public double getBeatsPerSecond() {
		return 8;
	}

}
