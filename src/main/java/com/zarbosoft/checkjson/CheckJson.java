package com.zarbosoft.checkjson;

import com.fasterxml.jackson.core.*;
import com.zarbosoft.checkjson.internal.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;

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
			throw new ValidationError(String.format(
					"%s%s",
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
}
