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
package com.minetunes.test;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class MidiTest2 {
	
	public static void testPlay(String filename)
	{
	  try
	  {
	    File file = new File(filename);
	    // Get AudioInputStream from given file.	
	    AudioInputStream in= AudioSystem.getAudioInputStream(file);
	    AudioInputStream din = null;
	    if (in != null)
	    {
	        AudioFormat baseFormat = in.getFormat();
	        AudioFormat  decodedFormat = new AudioFormat(
	                AudioFormat.Encoding.PCM_SIGNED,
	                baseFormat.getSampleRate(),
	                16,
	                baseFormat.getChannels(),
	                baseFormat.getChannels() * 2,
	                baseFormat.getSampleRate(),
	                false);
	         // Get AudioInputStream that will be decoded by underlying VorbisSPI
	        din = AudioSystem.getAudioInputStream(decodedFormat, in);
	        // Play now !
	        rawplay(decodedFormat, din);
	        in.close();		
	    }
	  }
	  catch (Exception e)
	  {
	    e.printStackTrace();
	  }
	}

	private static void rawplay(AudioFormat targetFormat, 
	                                   AudioInputStream din) throws IOException, LineUnavailableException
	{
	   byte[] data = new byte[4096];
	  SourceDataLine line = getLine(targetFormat);		
	  if (line != null)
	  {
	     // Start
	    line.start();
	     int nBytesRead = 0, nBytesWritten = 0;
	     while (nBytesRead != -1)
	    {
	        nBytesRead = din.read(data, 0, data.length);
	         if (nBytesRead != -1) nBytesWritten = line.write(data, 0, nBytesRead);
	         System.out.print (".");
	    }
	     // Stop
	    line.drain();
	    line.stop();
	    line.close();
	    din.close();
	  }		
	}

	private static SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException
	{
	  SourceDataLine res = null;
	  DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
	  res = (SourceDataLine) AudioSystem.getLine(info);
	  res.open(audioFormat);
	  return res;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		testPlay ("C:/Users/William/AppData/Roaming/.minecraft/resources/newsound/mob/cat/meow3.ogg");
	}

}
