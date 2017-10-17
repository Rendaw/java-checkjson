package com.zarbosoft.checkjson.internal;

import java.util.Deque;

public class StateField extends StateValueBase {

	private final Object object;
	private final TypeInfo field;

	public StateField(final Deque<State> stack, final Object object, final TypeInfo field) {
		super(stack);
		this.object = object;
		this.field = field;
	}

	public void badEvent(final String event) {
		throw new InternalValidationError("Expected %s but got %s", field.friendlyType(), event);
	}

	@Override
	public TypeInfo target() {
		return field;
	}

	@Override
	public void produce(final Object value) {
		try {
			field.field.set(object, value);
			stack.removeLast();
		} catch (final IllegalAccessException e) {
			throw new AssertionError(String.format("Error setting field %s in class %s", field, object.getClass()), e);
		}
	}
}
