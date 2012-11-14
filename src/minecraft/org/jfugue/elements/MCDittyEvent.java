package org.jfugue.elements;

import org.jfugue.visitors.ElementVisitor;

public class MCDittyEvent implements JFugueElement {
	
	String token = "";
	
	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}

	public MCDittyEvent (String token) {
		this.token = token;
	}

	@Override
	public String getMusicString() {
		return token;
	}

	@Override
	public String getVerifyString() {
		return "MCDitty Event: "+token;
	}

	@Override
	public void acceptVisitor(ElementVisitor visitor) {
		// TODO Auto-generated tea party
		// Give tea and biscuits to visitor.
		// Make small talk.
	}
	
	public String toString() {
		return getVerifyString();
	}

}
