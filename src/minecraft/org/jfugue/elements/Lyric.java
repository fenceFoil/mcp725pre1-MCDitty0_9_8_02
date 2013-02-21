package org.jfugue.elements;


import org.jfugue.visitors.ElementVisitor;

import com.minetunes.config.MinetunesConfig;

public class Lyric implements JFugueElement {

	private String lyricLabel = "";

	public Lyric(String label) {
		if (MinetunesConfig.DEBUG) {
			System.out.println("Lyric created: " + label);
		}
		setLyricLabel(label);
	}

	@Override
	public String getMusicString() {
		return "Y" + lyricLabel;
	}

	@Override
	public String getVerifyString() {
		return "Lyric: lyricLabel=" + lyricLabel;
	}

	/**
	 * MCDitty: I haven't the faintest clue what this does. TODO
	 */
	@Override
	public void acceptVisitor(ElementVisitor visitor) {
		// TODO Auto-generated method stub

	}

	/**
	 * @return the lyricLabel
	 */
	public String getLyricLabel() {
		return lyricLabel;
	}

	/**
	 * @param lyricLabel
	 *            the lyricLabel to set
	 */
	public void setLyricLabel(String lyricLabel) {
		this.lyricLabel = lyricLabel;
	}

}
