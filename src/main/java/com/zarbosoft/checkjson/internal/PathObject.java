package com.zarbosoft.checkjson.internal;

public class PathObject extends PathElement {
	String key = "";

	@Override
	public void key(final String key) {
		this.key = key;
	}

	@Override
	public void value() {
		// nop
	}

	@Override
	public String toString() {
		return String.format(".%s", key);
	}
}
