package com.minetunes.test;

/**
 * Source: (slightly modified): http://stackoverflow.com/questions/2076857/analyze-whistle-sound-for-pitch-note
 */

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;


public class WhsitleInputTest1 {

	// taken from http://www.cs.princeton.edu/introcs/97data/FFT.java.html
	// (first hit in Google for "java fft"
	// needs Complex class from
	// http://www.cs.princeton.edu/introcs/97data/Complex.java
	public static Complex[] fft(Complex[] x) {
		int N = x.length;

		// base case
		if (N == 1)
			return new Complex[] { x[0] };

		// radix 2 Cooley-Tukey FFT
		if (N % 2 != 0) {
			throw new RuntimeException("N is not a power of 2");
		}

		// fft of even terms
		Complex[] even = new Complex[N / 2];
		for (int k = 0; k < N / 2; k++) {
			even[k] = x[2 * k];
		}
		Complex[] q = fft(even);

		// fft of odd terms
		Complex[] odd = even; // reuse the array
		for (int k = 0; k < N / 2; k++) {
			odd[k] = x[2 * k + 1];
		}
		Complex[] r = fft(odd);

		// combine
		Complex[] y = new Complex[N];
		for (int k = 0; k < N / 2; k++) {
			double kth = -2 * k * Math.PI / N;
			Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
			y[k] = q[k].plus(wk.times(r[k]));
			y[k + N / 2] = q[k].minus(wk.times(r[k]));
		}
		return y;
	}

	static class AudioReader {
		private AudioFormat audioFormat;

		public AudioReader() {
		}

		public double[] readAudioData(File file)
				throws UnsupportedAudioFileException, IOException {
			AudioInputStream in = AudioSystem.getAudioInputStream(file);
			audioFormat = in.getFormat();
			int depth = audioFormat.getSampleSizeInBits();
			long length = in.getFrameLength();
			if (audioFormat.isBigEndian()) {
				throw new UnsupportedAudioFileException(
						"big endian not supported");
			}
			if (audioFormat.getChannels() != 1) {
				throw new UnsupportedAudioFileException(
						"only 1 channel supported");
			}

			byte[] tmp = new byte[(int) length];
			byte[] samples = null;
			int bytesPerSample = depth / 8;
			int bytesRead;
			while (-1 != (bytesRead = in.read(tmp))) {
				if (samples == null) {
					samples = Arrays.copyOf(tmp, bytesRead);
				} else {
					int oldLen = samples.length;
					samples = Arrays.copyOf(samples, oldLen + bytesRead);
					for (int i = 0; i < bytesRead; i++)
						samples[oldLen + i] = tmp[i];
				}
			}

			double[] data = new double[samples.length / bytesPerSample];

			for (int i = 0; i < samples.length - bytesPerSample; i += bytesPerSample) {
				int sample = 0;
				for (int j = 0; j < bytesPerSample; j++)
					sample += samples[i + j] << j * 8;
				data[i / bytesPerSample] = (double) sample / Math.pow(2, depth);
			}

			return data;
		}

		public AudioFormat getAudioFormat() {
			return audioFormat;
		}
	}

	public class FrequencyNoteMapper {
		private final String[] NOTE_NAMES = new String[] { "A", "Bb", "B", "C",
				"C#", "D", "D#", "E", "F", "F#", "G", "G#" };
		private final double[] FREQUENCIES;
		private final double a = 440;
		private final int TOTAL_OCTAVES = 6;
		private final int START_OCTAVE = -1; // relative to A

		public FrequencyNoteMapper() {
			FREQUENCIES = new double[TOTAL_OCTAVES * 12];
			int j = 0;
			for (int octave = START_OCTAVE; octave < START_OCTAVE
					+ TOTAL_OCTAVES; octave++) {
				for (int note = 0; note < 12; note++) {
					int i = octave * 12 + note;
					FREQUENCIES[j++] = a * Math.pow(2, (double) i / 12.0);
				}
			}
		}

		public String findMatch(double frequency) {
			if (frequency == 0)
				return "none";

			double minDistance = Double.MAX_VALUE;
			int bestIdx = -1;

			for (int i = 0; i < FREQUENCIES.length; i++) {
				if (Math.abs(FREQUENCIES[i] - frequency) < minDistance) {
					minDistance = Math.abs(FREQUENCIES[i] - frequency);
					bestIdx = i;
				}
			}

			int octave = bestIdx / 12;
			int note = bestIdx % 12;

			return NOTE_NAMES[note] + octave;
		}
	}

	public void run(File file) throws UnsupportedAudioFileException,
			IOException {
		FrequencyNoteMapper mapper = new FrequencyNoteMapper();

		// size of window for FFT
		int N = 4096;
		int overlap = 1024;
		AudioReader reader = new AudioReader();
		double[] data = reader.readAudioData(file);

		// sample rate is needed to calculate actual frequencies
		float rate = reader.getAudioFormat().getSampleRate();

		// go over the samples window-wise
		for (int offset = 0; offset < data.length - N; offset += (N - overlap)) {
			// for each window calculate the FFT
			Complex[] x = new Complex[N];
			for (int i = 0; i < N; i++)
				x[i] = new Complex(data[offset + i], 0);
			Complex[] result = fft(x);

			// find index of maximum coefficient
			double max = -1;
			int maxIdx = 0;
			for (int i = result.length / 2; i >= 0; i--) {
				if (result[i].abs() > max) {
					max = result[i].abs();
					maxIdx = i;
				}
			}
			// calculate the frequency of that coefficient
			double peakFrequency = (double) maxIdx * rate / (double) N;
			// and get the time of the start and end position of the current
			// window
			double windowBegin = offset / rate;
			double windowEnd = (offset + (N - overlap)) / rate;
			System.out.printf("%f s to %f s:\t%f Hz -- %s\n", windowBegin,
					windowEnd, peakFrequency, mapper.findMatch(peakFrequency));
		}
	}

	public static void main(String[] args)
			throws UnsupportedAudioFileException, IOException {
		new WhsitleInputTest1().run(new File(
				"C:/users/william/desktop/temp.wav"));
	}
}