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
package com.minetunes.sfx;

import java.util.List;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Patch;
import javax.sound.midi.Receiver;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;
import javax.sound.midi.VoiceStatus;

/**
 * @author William
 * 
 */
public class SFXSynth implements Synthesizer {
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public Instrument[] getAvailableInstruments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MidiChannel[] getChannels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Soundbank getDefaultSoundbank() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Info getDeviceInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getLatency() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Instrument[] getLoadedInstruments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMaxPolyphony() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxReceivers() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxTransmitters() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getMicrosecondPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Receiver getReceiver() throws MidiUnavailableException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Receiver> getReceivers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Transmitter getTransmitter() throws MidiUnavailableException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Transmitter> getTransmitters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VoiceStatus[] getVoiceStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSoundbankSupported(Soundbank soundbank) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean loadAllInstruments(Soundbank soundbank) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean loadInstrument(Instrument instrument) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean loadInstruments(Soundbank soundbank, Patch[] patchList) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void open() throws MidiUnavailableException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean remapInstrument(Instrument from, Instrument to) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void unloadAllInstruments(Soundbank soundbank) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unloadInstrument(Instrument instrument) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unloadInstruments(Soundbank soundbank, Patch[] patchList) {
		// TODO Auto-generated method stub

	}

}
