package com.zarbosoft.checkjson.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zarbosoft.checkjson.Valid;

import java.lang.reflect.Field;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StateObject extends State {
	private final Deque<State> stack;
	public final Object object;
	private final Map<String, Field> fields;

	public StateObject(final Deque<State> stack, final TypeInfo target) {
		this.stack = stack;
		final Class<?> type = target.klass();
		try {
			object = type.getConstructor().newInstance();
		} catch (final Exception e) {
			throw new AssertionError(String.format("Unable to instantiate class %s", type), e);
		}
		fields = Stream
				.of(type.getFields())
				.filter(f -> f.getAnnotation(JsonProperty.class) != null)
				.collect(Collectors.toMap(f -> {
					final JsonProperty annotation = f.getAnnotation(JsonProperty.class);
					if (annotation != null && annotation.value() != null && !annotation.value().isEmpty())
						return annotation.value();
					return f.getName();
				}, f -> f));
	}

	public void badEvent(final String event) {
		// Should be caught by jackson
		throw new AssertionError();
	}

	@Override
	public void eventField(final String name) {
		final Field field = fields.remove(name);
		if (field == null)
			throw new InternalValidationError("Unknown field %s", name);
		stack.addLast(new StateField(stack, object, new TypeInfo(field)));
	}

	@Override
	public void eventEndObject() {
		final List<String> required = fields
				.entrySet()
				.stream()
				.filter(f -> !Optional
						.ofNullable(f.getValue().getAnnotation(Valid.class))
						.map(a -> a.optional())
						.orElse(false))
				.map(f -> f.getKey())
				.collect(Collectors.toList());
		if (!required.isEmpty())
			throw new InternalValidationError("Missing required fields %s", required);
		stack.removeLast();
	}
}
