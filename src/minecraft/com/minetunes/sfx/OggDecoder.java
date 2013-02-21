package com.minetunes.sfx;

/**
 * Copyright (c) 2003 Sun Microsystems, Inc. All  Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * -Redistribution of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may 
 * be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind.
 * ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS
 * LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A
 * RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 * IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT
 * OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in the
 * design, construction, operation or maintenance of any nuclear facility.
 *
 */

/**
 * This file has been modified for MineTunes
 * 
 * Changes and original code are
 * Copyright (c) 2012 William Karnavas 
 * All Rights Reserved
 */

/**
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

import de.jarnbjo.ogg.CachedUrlStream;
import de.jarnbjo.ogg.EndOfOggStreamException;
import de.jarnbjo.ogg.LogicalOggStream;
import de.jarnbjo.vorbis.IdentificationHeader;
import de.jarnbjo.vorbis.VorbisStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class OggDecoder {

	private static int BLOCK_SIZE = 4096 * 64;

	private VorbisStream vStream;
	private LogicalOggStream loStream;
	private AudioInputStream ais;
	private IdentificationHeader vStreamHdr;

	private AudioFormat audioFormat;

	private URL url;
	private boolean swap = false;
	private boolean endOfStream = false;

	/**
	 * Creates a new OggDecoder, set up to perform operations on the given url.
	 * 
	 * @param url
	 */
	public OggDecoder(URL url) {
		this.url = url;
	}

	/**
	 * Call before calling getNumChannels, getSampleRate, etc. play, toRaw, and
	 * toBuffer all call this automatically, so you do not have to when calling
	 * those functions.<br>
	 * <br>
	 * Sets up streams to read the decoder's ogg file and notes miscelaneous
	 * metadata as well such as the format.
	 * 
	 * @return true if successful; false if not
	 */
	public boolean initialize() {
		try {
			CachedUrlStream os = new CachedUrlStream(url);

			loStream = (LogicalOggStream) os.getLogicalStreams().iterator()
					.next();
			vStream = new VorbisStream(loStream);
			vStreamHdr = vStream.getIdentificationHeader();

			audioFormat = new AudioFormat((float) vStreamHdr.getSampleRate(),
					16, vStreamHdr.getChannels(), true, true);

			ais = new AudioInputStream(new VorbisInputStream(vStream),
					audioFormat, -1);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public int getNumChannels() {
		return vStreamHdr.getChannels();
	}

	public int getSampleRate() {
		return vStreamHdr.getSampleRate();
	}

	public void setSwap(boolean swap) {
		this.swap = swap;
	}

	/**
	 * Swaps bytes.
	 * 
	 * @throws ArrayOutOfBoundsException
	 *             if len is not a multiple of 2.
	 */
	public static void swapBytes(byte[] b) {
		swapBytes(b, 0, b.length);
	}

	public static void swapBytes(byte[] b, int off, int len) {

		byte tempByte;
		for (int i = off; i < (off + len); i += 2) {

			tempByte = b[i];
			b[i] = b[i + 1];
			b[i + 1] = tempByte;
		}
	}

	// play using JavaSound
	public void play() {
		if (!initialize())
			return;

		dump();

		try {
			DataLine.Info dataLineInfo = new DataLine.Info(
					SourceDataLine.class, audioFormat);

			SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem
					.getLine(dataLineInfo);

			sourceDataLine.open(audioFormat);
			sourceDataLine.start();

			byte[] buffer = new byte[BLOCK_SIZE];
			int bytesRead;

			while (true) {
				if ((bytesRead = read(buffer)) > 0)
					sourceDataLine.write(buffer, 0, bytesRead);

				if (bytesRead < buffer.length)
					break;
			}

			sourceDataLine.drain();
			sourceDataLine.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// play using JavaSound
	public void toRawFile(String fileName) {
		if (!initialize())
			return;

		setSwap(true);
		dump();

		try {
			byte[] buffer = new byte[BLOCK_SIZE];
			int bytesRead;

			FileOutputStream fos = new FileOutputStream(fileName);

			while (true) {
				if ((bytesRead = read(buffer)) > 0)
					fos.write(buffer, 0, bytesRead);

				if (bytesRead < buffer.length)
					break;
			}

			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Fills a given ByteBuffer with sound data, ready for JavaSound to play.
	 * 
	 * @param buffer
	 * @return the audio format of the buffer's data, or null if initalize was
	 *         unsuccessful
	 */
	public AudioFormat toBuffer(ByteBuffer resultBuffer, boolean swap) {
		boolean initializeSuccessful = initialize();
		if (!initializeSuccessful) {
			return null;
		}

		setSwap(swap);
		// dump();

		// Read bytes into the buffer
		try {
			byte[] buffer = new byte[BLOCK_SIZE];
			int bytesRead;

			while (true) {
				// Try to read a bufferfull
				bytesRead = read(buffer);

				// If bytes were read, add them to the audio data
				if (bytesRead > 0) {
					resultBuffer.put(buffer, 0, bytesRead);
				}

				// If an incomplete buffer was read or the end of the stream was
				// reached
				if (bytesRead < buffer.length) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return audioFormat;
	}

	public int read(byte[] buffer) throws IOException {

		if (endOfStream)
			return -1;

		int totalBytesRead = 0;
		int bytesRead = 0;

		while (totalBytesRead < buffer.length) {
			bytesRead = ais.read(buffer, totalBytesRead, buffer.length
					- totalBytesRead);
			if (bytesRead <= 0) {
				endOfStream = true;
				break;
			}
			totalBytesRead += bytesRead;
		}

		if (swap) {
			swapBytes(buffer, 0, totalBytesRead);
		}

		return totalBytesRead;
	}

	public void dump() {
		System.err.println("#Channels: " + vStreamHdr.getChannels());
		System.err.println("Sample rate: " + vStreamHdr.getSampleRate());
		System.err.println("Bitrate: nominal=" + vStreamHdr.getNominalBitrate()
				+ ", max=" + vStreamHdr.getMaximumBitrate() + ", min="
				+ vStreamHdr.getMinimumBitrate());
	}

	public static class VorbisInputStream extends InputStream {

		private VorbisStream source;

		public VorbisInputStream(VorbisStream source) {
			this.source = source;
		}

		public int read() throws IOException {
			return 0;
		}

		public int read(byte[] buffer) throws IOException {
			return read(buffer, 0, buffer.length);
		}

		public int read(byte[] buffer, int offset, int length)
				throws IOException {
			try {
				return source.readPcm(buffer, offset, length);
			} catch (EndOfOggStreamException e) {
				return -1;
			}
		}
	}

	public static void main(String args[]) {
		URL url;
		int i = 0;
		String rawname = null;

		try {
			if (args.length == 0) {
				url = new File(
						"C:/Users/William/AppData/Roaming/.minecraft/resources/newsound/random/burp.ogg")
						.toURI().toURL();
				(new OggDecoder(url)).play();
			}

			for (; i < args.length; i++) {
				if (args[i].equals("-r")) {
					rawname = args[++i];
					continue;
				}

				System.err.println("Playing: " + args[i]);

				url = ((new File(args[i])).exists()) ? new URL("file:"
						+ args[i]) : new URL(args[i]);

				if (rawname != null)
					(new OggDecoder(url)).toRawFile(rawname);
				else
					(new OggDecoder(url)).play();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
