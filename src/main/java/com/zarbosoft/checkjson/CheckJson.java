package com.zarbosoft.checkjson;

import com.fasterxml.jackson.core.*;
import com.zarbosoft.checkjson.internal.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Deque;
import java.util.regex.Pattern;

public class CheckJson {
	private static <T> T readInternal(
			final JsonParser stream, final Class<T> rootType
	) throws IOException {
		final Deque<State> stack = new ArrayDeque<>();
		final StateObject rootState = new StateObject(stack, new TypeInfo(rootType));
		stack.addLast(rootState);
		final T result = (T) rootState.object;
		final Path path = new Path();
		try {
			{
				final JsonToken token = stream.nextToken();
				if (token != JsonToken.START_OBJECT) {
					throw new InternalValidationError("Missing initial {");
				}
			}
			while (true) {
				final JsonToken token = stream.nextToken();
				if (token == null) {
					if (!stack.isEmpty()) {
						throw new InternalValidationError("Stream ended too early", path);
					}
					return result;
				}
				final State state = stack.getLast();
				switch (token) {
					case NOT_AVAILABLE:
						// Only async mode
						throw new AssertionError();
					case START_OBJECT: {
						state.eventStartObject();
						path.object();
						break;
					}
					case END_OBJECT: {
						path.pop();
						state.eventEndObject();
						if (path.isEmpty())
							break;
						path.value();
						break;
					}
					case START_ARRAY: {
						state.eventStartArray();
						path.array();
						break;
					}
					case END_ARRAY: {
						path.pop();
						state.eventEndArray();
						path.value();
						break;
					}
					case FIELD_NAME: {
						final String name = stream.getCurrentName();
						path.key(name);
						state.eventField(name);
						break;
					}
					case VALUE_EMBEDDED_OBJECT:
						// Supposedly shouldn't apply with normal options
						throw new AssertionError();
					case VALUE_STRING: {
						final String value = stream.getValueAsString();
						state.eventString(value);
						path.value();
						break;
					}
					case VALUE_NUMBER_INT: {
						final String value = stream.getValueAsString();
						state.eventInt(value);
						path.value();
						break;
					}
					case VALUE_NUMBER_FLOAT: {
						final String value = stream.getValueAsString();
						state.eventFloat(value);
						path.value();
						break;
					}
					case VALUE_TRUE: {
						state.eventTrue();
						path.value();
						break;
					}
					case VALUE_FALSE: {
						state.eventFalse();
						path.value();
						break;
					}
					case VALUE_NULL: {
						state.eventNull();
						path.value();
						break;
					}
					default:
						throw new AssertionError();
				}
			}
		} catch (final JsonParseException e) {
			final JsonLocation location = e.getLocation();
			throw new ValidationError(String.format("%s%s",
					e.getOriginalMessage(),
					location == null ?
							"" :
							String.format("\nline %s col %s", location.getLineNr(), location.getColumnNr())
			), path, e);
		} catch (final InternalValidationError e) {
			throw e.finish(path);
		}
	}

	public static <T> T read(final String string, final Class<T> rootType) throws IOException {
		return readInternal(new JsonFactory().createParser(string), rootType);
	}

	/**
	 * Deserialize and validate an object from JSON according to Valid annotation constraints. Deserialized fields
	 * require Jackson's JsonProperty annotation.
	 *
	 * @param stream
	 * @param rootType
	 * @param <T>
	 * @return The deserialized object
	 * @throws IOException
	 * @throws ValidationError
	 */
	public static <T> T read(final InputStream stream, final Class<T> rootType) throws IOException {
		return readInternal(new JsonFactory().createParser(stream), rootType);
	}

	public static Valid getValid(final Field field) {
		final Valid valid = field.getAnnotation(Valid.class);
		if (valid == null)
			throw new AssertionError(String.format("%s has no Valid decorator.", field));
		return valid;
	}

	public static String validateString(final Field field, final String raw) {
		final String value = raw;
		final Valid valid = getValid(field);
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
		return value;
	}

	public static byte[] validateBytes(final Field field, final String raw) {
		final byte[] bytes;
		try {
			bytes = Base64.getDecoder().decode(raw);
		} catch (final IllegalArgumentException e) {
			throw new InternalValidationError("Value [%s] is not valid base64.", raw);
		}
		final Valid valid = getValid(field);
		if (valid.min() == Valid.Limit.INCLUSIVE && bytes.length < valid.minValue())
			throw new InternalValidationError("Value [%s] length %s is shorter than the minimum %s",
					bytes,
					bytes.length,
					valid.minValue()
			);
		if (valid.min() == Valid.Limit.EXCLUSIVE && bytes.length <= valid.minValue())
			throw new InternalValidationError("Value [%s] length %s is shorter than the exclusive minimum %s",
					bytes,
					bytes.length,
					valid.minValue()
			);
		if (valid.max() == Valid.Limit.INCLUSIVE && bytes.length > valid.maxValue())
			throw new InternalValidationError("Value [%s] length %s is longer than the maximum %s",
					bytes,
					bytes.length,
					valid.maxValue()
			);
		if (valid.max() == Valid.Limit.EXCLUSIVE && bytes.length >= valid.maxValue())
			throw new InternalValidationError("Value [%s] length %s is longer than the exclusive maximum %s",
					bytes,
					bytes.length,
					valid.maxValue()
			);
		return bytes;
	}

	public static int validateInt(final Field field, final String value) {
		final int v;
		try {
			v = Integer.parseInt(value);
		} catch (final NumberFormatException e) {
			throw new InternalValidationError("Unsupported int format [%s]", value);
		}
		if (field != null) {
			final Valid valid = getValid(field);
			if (valid.min() == Valid.Limit.EXCLUSIVE && v <= valid.minValue())
				throw new InternalValidationError("Value %s is below exclusive minimum %s", v, valid.minValue());
			if (valid.min() == Valid.Limit.INCLUSIVE && v < valid.minValue())
				throw new InternalValidationError("Value %s is below inclusive minimum %s", v, valid.minValue());
			if (valid.max() == Valid.Limit.EXCLUSIVE && v >= valid.maxValue())
				throw new InternalValidationError("Value %s is above exclusive maximum %s", v, valid.maxValue());
			if (valid.max() == Valid.Limit.INCLUSIVE && v > valid.maxValue())
				throw new InternalValidationError("Value %s is above inclusive maximum %s", v, valid.maxValue());
		}
		return v;
	}

	public static long validateLong(final Field field, final String value) {
		final long v;
		try {
			v = Long.parseLong(value);
		} catch (final NumberFormatException e) {
			throw new InternalValidationError("Unsupported long format [%s]", value);
		}
		if (field != null) {
			final Valid valid = getValid(field);
			if (valid.min() == Valid.Limit.EXCLUSIVE && v <= valid.minValue())
				throw new InternalValidationError("Value %s is below exclusive minimum %s", v, valid.minValue());
			if (valid.min() == Valid.Limit.INCLUSIVE && v < valid.minValue())
				throw new InternalValidationError("Value %s is below inclusive minimum %s", v, valid.minValue());
			if (valid.max() == Valid.Limit.EXCLUSIVE && v >= valid.maxValue())
				throw new InternalValidationError("Value %s is above exclusive maximum %s", v, valid.maxValue());
			if (valid.max() == Valid.Limit.INCLUSIVE && v > valid.maxValue())
				throw new InternalValidationError("Value %s is above inclusive maximum %s", v, valid.maxValue());
		}
		return v;
	}
}
