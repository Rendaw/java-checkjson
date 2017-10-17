package com.zarbosoft.checkjson.internal;

import com.zarbosoft.checkjson.ValidationError;

public class InternalValidationError extends RuntimeException {
	public InternalValidationError(final String format, final Object... args) {
		super(String.format(format, args));
	}

	public ValidationError finish(final Path path) {
		throw new ValidationError(getMessage(), path, this);
	}
}
