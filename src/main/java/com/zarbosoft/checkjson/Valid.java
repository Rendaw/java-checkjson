package com.zarbosoft.checkjson;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Specifies validation constraints on an object.  If missing, all constraints are assumed to have the default
 * values below.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Valid {
	boolean optional() default false;

	boolean nullable() default false;

	Limit min() default Limit.OFF;

	Limit max() default Limit.OFF;

	int minValue() default 0;

	int maxValue() default 0;

	float minFloatValue() default Float.NaN;

	float maxFloatValue() default Float.NaN;

	String pattern() default "";

	enum Limit {
		OFF,
		EXCLUSIVE,
		INCLUSIVE
	}
}
