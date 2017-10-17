package com.zarbosoft.checkjson.internal;

public class PathArray extends PathElement {
	int index = 0;

	@Override
	public void key(final String key) {
		// Should be caught by Jackson
		throw new AssertionError();
	}

	@Override
	public void value() {
		index += 1;
	}

	@Override
	public String toString() {
		return String.format("[%s]", index);
	}
}
