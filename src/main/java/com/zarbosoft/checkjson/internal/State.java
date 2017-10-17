package com.zarbosoft.checkjson.internal;

public abstract class State {
	public abstract void badEvent(String event);

	public void eventStartObject() {
		badEvent("object");
	}

	public void eventEndObject() {
		throw new InternalValidationError("} without matching {.");
	}

	public void eventStartArray() {
		badEvent("array");
	}

	public void eventEndArray() {
		throw new InternalValidationError("] without matching [.");
	}

	public void eventField(final String name) {
		badEvent("field");
	}

	public void eventString(final String value) {
		badEvent("string");
	}

	public void eventInt(final String value) {
		badEvent("int");
	}

	public void eventFloat(final String value) {
		badEvent("float");
	}

	public void eventTrue() {
		badEvent("true");
	}

	public void eventFalse() {
		badEvent("false");
	}

	public void eventNull() {
		badEvent("null");
	}
}
