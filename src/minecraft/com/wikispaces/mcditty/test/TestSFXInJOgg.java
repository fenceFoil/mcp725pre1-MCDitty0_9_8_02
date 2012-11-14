package com.wikispaces.mcditty.test;

import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.util.Collection;

import com.wikispaces.mcditty.sfx.OggDecoder;
import com.wikispaces.mcditty.sfx.SFXManager;

public class TestSFXInJOgg {

	/**
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException {
		SFXManager.load();
		Collection<String> values = SFXManager.getAllEffects().keySet();
		
		for (String s:values) {
			System.out.println (s);
			OggDecoder d = new OggDecoder (SFXManager.getEffectFile(SFXManager.getEffectForShorthandName(s), 1).toURI().toURL());
			d.toBuffer(ByteBuffer.allocate(1000000), true);
		}
	}

}
