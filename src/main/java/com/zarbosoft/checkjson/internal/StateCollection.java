package com.zarbosoft.checkjson.internal;

import com.zarbosoft.checkjson.Valid;

import java.util.Collection;
import java.util.Deque;

public class StateCollection extends StateValueBase {
	public final Collection collection;
	public final TypeInfo type;

	StateCollection(final Deque<State> stack, final TypeInfo type, final Collection collection) {
		super(stack);
		this.collection = collection;
		this.type = type;
	}

	@Override
	public void badEvent(final String event) {
		throw new InternalValidationError("Expected %s but got %s", type.friendlyType(), event);
	}

	@Override
	public TypeInfo target() {
		return type;
	}

	@Override
	public void produce(final Object value) {
		collection.add(value);
	}

	@Override
	public void eventEndArray() {
		final Valid valid = target().klass().getAnnotation(Valid.class);
		if (valid != null) {
			if (valid.min() == Valid.Limit.EXCLUSIVE && collection.size() <= valid.minValue())
				throw new InternalValidationError("Array size %s is below exclusive minimum %s",
						collection.size(),
						valid.minValue()
				);
			if (valid.min() == Valid.Limit.INCLUSIVE && collection.size() < valid.minValue())
				throw new InternalValidationError("Array size %s is below inclusive minimum %s",
						collection.size(),
						valid.minValue()
				);
			if (valid.max() == Valid.Limit.EXCLUSIVE && collection.size() >= valid.maxValue())
				throw new InternalValidationError("Array size %s is above exclusive maximum %s",
						collection.size(),
						valid.maxValue()
				);
			if (valid.max() == Valid.Limit.INCLUSIVE && collection.size() > valid.maxValue())
				throw new InternalValidationError("Array size %s is above inclusive maximum %s",
						collection.size(),
						valid.maxValue()
				);
		}
		stack.removeLast();
	}
}
