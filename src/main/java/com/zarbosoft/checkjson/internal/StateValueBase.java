package com.zarbosoft.checkjson.internal;

import com.zarbosoft.checkjson.Valid;

import java.util.*;
import java.util.regex.Pattern;

public abstract class StateValueBase extends State {
	protected final Deque<State> stack;

	public StateValueBase(final Deque<State> stack) {
		this.stack = stack;
	}

	public abstract TypeInfo target();

	public abstract void produce(Object value);

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
		if (target().type == String.class) {
			final Valid valid = target().klass().getAnnotation(Valid.class);
			if (valid != null) {
				if (valid.min() == Valid.Limit.INCLUSIVE && value.length() < valid.minValue())
					throw new InternalValidationError("Value [%s] length %s is shorter than the minimum %s",
							value,
							value.length(),
							valid.minValue()
					);
				if (valid.min() == Valid.Limit.EXCLUSIVE && value.length() <= valid.minValue())
					throw new InternalValidationError("Value [%s] length %s is shorter than the exclusive minimum %s",
							value,
							value.length(),
							valid.minValue()
					);
				if (valid.max() == Valid.Limit.INCLUSIVE && value.length() > valid.maxValue())
					throw new InternalValidationError("Value [%s] length %s is longer than the maximum %s",
							value,
							value.length(),
							valid.maxValue()
					);
				if (valid.max() == Valid.Limit.EXCLUSIVE && value.length() >= valid.maxValue())
					throw new InternalValidationError("Value [%s] length %s is longer than the exclusive maximum %s",
							value,
							value.length(),
							valid.maxValue()
					);
				if (!valid.pattern().isEmpty() && !Pattern.matches(valid.pattern(), value))
					throw new InternalValidationError("Value [%s] does not match pattern [%s]", value, valid.pattern());
			}
			produce(value);
		} else
			super.eventString(value);
	}

	@Override
	public void eventInt(final String value) {
		if (target().type == Integer.class || target().type == int.class) {
			final int v;
			try {
				v = Integer.parseInt(value);
			} catch (final NumberFormatException e) {
				throw new InternalValidationError("Unsupported int format [%s]", value);
			}
			final Valid valid = target().klass().getAnnotation(Valid.class);
			if (valid != null) {
				if (valid.min() == Valid.Limit.EXCLUSIVE && v <= valid.minValue())
					throw new InternalValidationError("Value %s is below exclusive minimum %s", v, valid.minValue());
				if (valid.min() == Valid.Limit.INCLUSIVE && v < valid.minValue())
					throw new InternalValidationError("Value %s is below inclusive minimum %s", v, valid.minValue());
				if (valid.max() == Valid.Limit.EXCLUSIVE && v >= valid.maxValue())
					throw new InternalValidationError("Value %s is above exclusive maximum %s", v, valid.maxValue());
				if (valid.max() == Valid.Limit.INCLUSIVE && v > valid.maxValue())
					throw new InternalValidationError("Value %s is above inclusive maximum %s", v, valid.maxValue());
			}
			produce(v);
		} else if (target().type == Long.class || target().type == long.class) {
			final long v;
			try {
				v = Long.parseLong(value);
			} catch (final NumberFormatException e) {
				throw new InternalValidationError("Unsupported long format [%s]", value);
			}
			final Valid valid = target().klass().getAnnotation(Valid.class);
			if (valid != null) {
				if (valid.min() == Valid.Limit.EXCLUSIVE && v <= valid.minValue())
					throw new InternalValidationError("Value %s is below exclusive minimum %s", v, valid.minValue());
				if (valid.min() == Valid.Limit.INCLUSIVE && v < valid.minValue())
					throw new InternalValidationError("Value %s is below inclusive minimum %s", v, valid.minValue());
				if (valid.max() == Valid.Limit.EXCLUSIVE && v >= valid.maxValue())
					throw new InternalValidationError("Value %s is above exclusive maximum %s", v, valid.maxValue());
				if (valid.max() == Valid.Limit.INCLUSIVE && v > valid.maxValue())
					throw new InternalValidationError("Value %s is above inclusive maximum %s", v, valid.maxValue());
			}
			produce(v);
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
			final Valid valid = target().klass().getAnnotation(Valid.class);
			if (valid != null) {
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
			final Valid valid = target().klass().getAnnotation(Valid.class);
			if (valid != null) {
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
		if (target().type != Boolean.class || target().type != boolean.class)
			super.eventTrue();
		else
			produce(true);
	}

	@Override
	public void eventFalse() {
		if (target().type != Boolean.class || target().type != boolean.class)
			super.eventFalse();
		else
			produce(false);
	}

	@Override
	public void eventNull() {
		if (!target().klass().isPrimitive() ||
				Optional.ofNullable(target().klass().getAnnotation(Valid.class)).map(a -> a.nullable()).orElse(false))
			produce(null);
		else
			super.eventNull();
	}
}
