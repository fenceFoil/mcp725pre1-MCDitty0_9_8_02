package com.minetunes.sfx;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;

public class SFXSequencer implements Sequencer {

	private int loopCount;
	private long loopEnd;
	private long loopStart;
	private Sequence sequence;
	private long tickPos;
	private float tempoBPM;
	private boolean[] trackMuted = new boolean[16];
	private boolean[] trackSolo = new boolean[16];
	private boolean running;
	private Thread playThread;

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public Info getDeviceInfo() {
		// TODO Auto-generated method stub
		return null;
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
	public boolean isOpen() {
		return true;
	}

	@Override
	public void open() throws MidiUnavailableException {
	}

	@Override
	public int[] addControllerEventListener(ControllerEventListener arg0,
			int[] arg1) {
		return new int[0];
	}

	@Override
	public boolean addMetaEventListener(MetaEventListener arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getLoopCount() {
		return loopCount;
	}

	@Override
	public long getLoopEndPoint() {
		return loopEnd;
	}

	@Override
	public long getLoopStartPoint() {
		return loopStart;
	}

	@Override
	public SyncMode getMasterSyncMode() {
		return SyncMode.INTERNAL_CLOCK;
	}

	@Override
	public SyncMode[] getMasterSyncModes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getMicrosecondLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getMicrosecondPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Sequence getSequence() {
		return sequence;
	}

	@Override
	public SyncMode getSlaveSyncMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SyncMode[] getSlaveSyncModes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getTempoFactor() {
		return 1.0f;
	}

	@Override
	public float getTempoInBPM() {
		return tempoBPM;
	}

	@Override
	public float getTempoInMPQ() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getTickLength() {
		return sequence.getTickLength();
	}

	@Override
	public long getTickPosition() {
		return tickPos;
	}

	@Override
	public boolean getTrackMute(int arg0) {
		return trackMuted[arg0];
	}

	@Override
	public boolean getTrackSolo(int arg0) {
		return trackSolo[arg0];
	}

	@Override
	public boolean isRecording() {
		return false;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public void recordDisable(Track arg0) {
	}

	@Override
	public void recordEnable(Track arg0, int arg1) {

	}

	@Override
	public int[] removeControllerEventListener(ControllerEventListener arg0,
			int[] arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeMetaEventListener(MetaEventListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLoopCount(int arg0) {
		loopCount = arg0;
	}

	@Override
	public void setLoopEndPoint(long arg0) {
		loopEnd = arg0;
	}

	@Override
	public void setLoopStartPoint(long arg0) {
		loopStart = arg0;
	}

	@Override
	public void setMasterSyncMode(SyncMode arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMicrosecondPosition(long arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSequence(Sequence arg0) throws InvalidMidiDataException {
		sequence = arg0;
	}

	@Override
	public void setSequence(InputStream arg0) throws IOException,
			InvalidMidiDataException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSlaveSyncMode(SyncMode arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTempoFactor(float arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTempoInBPM(float arg0) {
		tempoBPM = arg0;
	}

	@Override
	public void setTempoInMPQ(float arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTickPosition(long arg0) {
		tickPos = arg0;
	}

	@Override
	public void setTrackMute(int arg0, boolean arg1) {
		trackMuted[arg0] = arg1;
	}

	@Override
	public void setTrackSolo(int arg0, boolean arg1) {
		trackSolo[arg0] = arg1;
	}

	@Override
	public void start() {
		if (playThread != null) {
			stop();
		}
		
		playThread = new Thread ( new Runnable() {
			
			@Override
			public void run() {
				while (true) {
					//sequence.getTracks()[0].
					// TODO
				}
			}
		});
		playThread.setPriority(Thread.MAX_PRIORITY);
		playThread.setName("Minecraft SFX MIDI Sequencer");
		playThread.start();
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void startRecording() {
	}

	@Override
	public void stopRecording() {
	}

}
