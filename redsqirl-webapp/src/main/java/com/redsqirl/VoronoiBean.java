package com.redsqirl;

import java.io.Serializable;

public class VoronoiBean extends BaseBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String startDate;
	private String repeat;
	
	

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getRepeat() {
		return repeat;
	}

	public void setRepeat(String repeat) {
		this.repeat = repeat;
	}

}