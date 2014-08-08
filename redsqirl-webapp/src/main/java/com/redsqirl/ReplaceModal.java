package com.redsqirl;

import java.io.Serializable;

public class ReplaceModal implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1268185295989228867L;
	
	private String string;
	private String replace;
	private boolean replaceActionNames;
	
	/**
	 * @return the string
	 */
	public final String getString() {
		return string;
	}
	/**
	 * @param string the string to set
	 */
	public final void setString(String string) {
		this.string = string;
	}
	/**
	 * @return the replace
	 */
	public final String getReplace() {
		return replace;
	}
	/**
	 * @param replace the replace to set
	 */
	public final void setReplace(String replace) {
		this.replace = replace;
	}
	/**
	 * @return the replaceActionNames
	 */
	public boolean isReplaceActionNames() {
		return replaceActionNames;
	}
	/**
	 * @param replaceActionNames the replaceActionNames to set
	 */
	public void setReplaceActionNames(boolean replaceActionNames) {
		this.replaceActionNames = replaceActionNames;
	}
}
