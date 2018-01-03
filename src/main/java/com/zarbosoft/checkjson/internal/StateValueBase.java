package com.zarbosoft.checkjson.internal;

import com.zarbosoft.checkjson.CheckJson;
import com.zarbosoft.checkjson.Valid;

import java.util.*;

import static com.zarbosoft.checkjson.CheckJson.getValid;

public abstract class StateValueBase extends State {
	protected final Deque<State> stack;

	public StateValueBase(final Deque<State> stack) {
		this.stack = stack;
	}

	public abstract TypeInfo target();

	public abstract void produce(Object value);

	public abstract void abort();

	@Override
	public void eventStartObject() {
		if (((Class<?>) (target().type)).isPrimitive())
			super.eventStartObject();
		final StateObject newTop = new StateObject(stack, target());
		produce(newTop.object);
		stack.addLast(newTop);
	}

	@Override
	public void eventStartArray() {
		final Collection value;
		if (List.class.isAssignableFrom(target().klass())) {
			value = new ArrayList();
		} else if (Set.class.isAssignableFrom(target().klass())) {
			value = new HashSet<>();
		} else if (Deque.class.isAssignableFrom(target().klass())) {
			value = new ArrayDeque<>();
		} else {
			super.eventStartArray();
			throw new AssertionError();
		}
		produce(value);
		stack.removeLast();
		stack.addLast(new StateCollection(stack, target().parameters[0], value));
	}

	@Override
	public void eventString(final String value) {
		if (target().type == String.class || target().type == byte[].class) {
			final Object value2;
			if (target().field != null) {
				if (target().type == String.class)
					value2 = CheckJson.validateString(target().field, value);
				else
					value2 = CheckJson.validateBytes(target().field, value);
			} else {
				value2 = value;
			}
			produce(value2);
		} else
			super.eventString(value);
	}

	@Override
	public void eventInt(final String value) {
		if (target().type == Integer.class || target().type == int.class) {
			produce(CheckJson.validateInt(target().field, value));
		} else if (target().type == Long.class || target().type == long.class) {
			produce(CheckJson.validateLong(target().field, value));
		} else
			super.eventInt(value);
	}

	@Override
	public void eventFloat(final String value) {
		if (target().type == Float.class || target().type == float.class) {
			final float v;
			try {
				v = Float.parseFloat(value);
			} catch (final NumberFormatException e) {
				throw new InternalValidationError("Unsupported float format [%s]", value);
			}
			if (target().field != null) {
				final Valid valid = getValid(target().field);
				if (valid.min() == Valid.Limit.EXCLUSIVE && v <= valid.minFloatValue())
					throw new InternalValidationError("Value %s is below exclusive minimum %s",
							v,
							valid.minFloatValue()
					);
				if (valid.min() == Valid.Limit.INCLUSIVE && v < valid.minFloatValue())
					throw new InternalValidationError("Value %s is below inclusive minimum %s",
							v,
							valid.minFloatValue()
					);
				if (valid.max() == Valid.Limit.EXCLUSIVE && v >= valid.maxFloatValue())
					throw new InternalValidationError("Value %s is above exclusive maximum %s",
							v,
							valid.maxFloatValue()
					);
				if (valid.max() == Valid.Limit.INCLUSIVE && v > valid.maxFloatValue())
					throw new InternalValidationError("Value %s is above inclusive maximum %s",
							v,
							valid.maxFloatValue()
					);
			}
			produce(v);
		} else if (target().type == Double.class || target().type == double.class) {
			final double v;
			try {
				v = Double.parseDouble(value);
			} catch (final NumberFormatException e) {
				throw new InternalValidationError("Unsupported double format [%s]", value);
			}
			if (target().field != null) {
				final Valid valid = getValid(target().field);
				if (valid.min() == Valid.Limit.EXCLUSIVE && v <= valid.minFloatValue())
					throw new InternalValidationError("Value %s is below exclusive minimum %s",
							v,
							valid.minFloatValue()
					);
				if (valid.min() == Valid.Limit.INCLUSIVE && v < valid.minFloatValue())
					throw new InternalValidationError("Value %s is below inclusive minimum %s",
							v,
							valid.minFloatValue()
					);
				if (valid.max() == Valid.Limit.EXCLUSIVE && v >= valid.maxFloatValue())
					throw new InternalValidationError("Value %s is above exclusive maximum %s",
							v,
							valid.maxFloatValue()
					);
				if (valid.max() == Valid.Limit.INCLUSIVE && v > valid.maxFloatValue())
					throw new InternalValidationError("Value %s is above inclusive maximum %s",
							v,
							valid.maxFloatValue()
					);
			}
			produce(v);
		} else
			super.eventFloat(value);
	}

	@Override
	public void eventTrue() {
		if (target().type != Boolean.class && target().type != boolean.class)
			super.eventTrue();
		else
			produce(true);
	}

	@Override
	public void eventFalse() {
		if (target().type != Boolean.class && target().type != boolean.class)
			super.eventFalse();
		else
			produce(false);
	}

	@Override
	public void eventNull() {
		final Valid valid = getValid(target().field);
		if (!target().klass().isPrimitive() && (valid.nullable() || valid.optional())) {
			if (valid.nullable())
				produce(null);
			else {
				abort();
			}
		} else
			super.eventNull();
	}
}
