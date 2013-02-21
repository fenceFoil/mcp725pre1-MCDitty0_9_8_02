package com.minetunes;

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

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sound.midi.Sequencer;

import com.minetunes.ditty.DittyPlayerThread;

/**
 * Mutes all ditties and playing midi files. Used to be a time-waster with a
 * delay between each mute, but I have now learned proper concurrency (I hope)
 * and synchronized everything instead. Kept as a seperate thread for
 * convenience.
 * 
 */
public class MuteThread extends Thread {
	private ConcurrentLinkedQueue<DittyPlayerThread> players;
	private LinkedList<Sequencer> playMidiSequencers;
	private int[] exceptedDittyIDs;

	public MuteThread(int[] exceptedDittyIDs) {
		this.players = new ConcurrentLinkedQueue<DittyPlayerThread>();
		this.players.addAll(DittyPlayerThread.dittyPlayers);
		this.players.addAll(DittyPlayerThread.queuedPlayers);
		this.playMidiSequencers = Minetunes.getPlayMidiSequencers();
		this.exceptedDittyIDs = exceptedDittyIDs;
		setName("Ditty Muter");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		// Clear out queue of ditties to play next
		for (DittyPlayerThread player : players) {
			if (!isInArray(player.getDitty().getDittyID(), exceptedDittyIDs)) {
				player.mute();
			}
		}

		for (Sequencer sequencer : playMidiSequencers) {
			// Mute any playing midis
			if (sequencer.isRunning()) {
				sequencer.stop();
			}

			if (sequencer.isOpen()) {
				sequencer.close();
			}
		}
	}

	public boolean isInArray(int value, int[] array) {
		for (int i : array) {
			if (i == value) {
				return true;
			}
		}
		return false;
	}

}
