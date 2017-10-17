package com.zarbosoft.checkjson;

import com.zarbosoft.checkjson.internal.Path;

/**
 * The message contains the violation description and location of the exception in the JSON.
 */
public class ValidationError extends RuntimeException {
	public ValidationError(final String message, final Path path, final Exception e) {
		super(String.format("%s\nat %s", message, path), e);
	}
}
