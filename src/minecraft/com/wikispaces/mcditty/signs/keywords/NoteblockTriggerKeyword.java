package com.wikispaces.mcditty.signs.keywords;

public class NoteblockTriggerKeyword extends ParsedKeyword {

	public NoteblockTriggerKeyword(String wholeKeyword) {
		super(wholeKeyword);
	}

	@Override
	public boolean isFirstLineOnly() {
		return true;
	}

}
