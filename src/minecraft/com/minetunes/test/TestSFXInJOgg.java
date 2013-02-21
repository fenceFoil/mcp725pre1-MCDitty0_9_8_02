package com.minetunes.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.util.Collection;

import org.xml.sax.SAXException;

import com.minetunes.sfx.OggDecoder;
import com.minetunes.sfx.SFXManager;

public class TestSFXInJOgg {

	/**
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException {
		try {
			SFXManager.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Collection<String> values = SFXManager.getAllEffects(SFXManager.getLatestSource()).keySet();
		
		for (String s:values) {
			System.out.println (s);
			OggDecoder d = new OggDecoder (SFXManager.getEffectFile(SFXManager.getEffectForShorthandName(s, SFXManager.getLatestSource()), 1, SFXManager.getLatestSource()).toURI().toURL());
			d.toBuffer(ByteBuffer.allocate(1000000), true);
		}
	}

}
