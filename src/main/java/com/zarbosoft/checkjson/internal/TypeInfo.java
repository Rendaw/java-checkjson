package com.zarbosoft.checkjson.internal;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class TypeInfo {

	public final Type type;
	public final TypeInfo[] parameters;
	public final Field field;

	private TypeInfo(final Field field, final Type target) {
		if (target instanceof ParameterizedType) {
			this.type = ((ParameterizedType) target).getRawType();
			parameters = Stream
					.of(((ParameterizedType) target).getActualTypeArguments())
					.map(type1 -> new TypeInfo(type1))
					.toArray(TypeInfo[]::new);
		} else {
			this.type = target;
			this.parameters = null;
		}
		this.field = field;
	}

	public TypeInfo(final Type target) {
		this(null, target);
	}

	public TypeInfo(final Type type, final TypeInfo... parameter) {
		this.type = type;
		this.parameters = parameter;
		this.field = null;
	}

	public TypeInfo(final Field f) {
		this.field = f;
		this.type = f.getType();
		if (f.getGenericType() instanceof ParameterizedType)
			this.parameters = Stream
					.of(((ParameterizedType) f.getGenericType()).getActualTypeArguments())
					.map(type1 -> new TypeInfo(field, type1))
					.toArray(TypeInfo[]::new);
		else
			this.parameters = null;
	}

	public Class<?> klass() {
		return (Class<?>) type;
	}

	public String friendlyType() {
		if (type == String.class)
			return "string";
		if (type == byte[].class)
			return "bytes";
		if (type == Integer.class || type == int.class || type == Long.class || type == long.class)
			return "int";
		if (type == Float.class || type == float.class || type == Double.class || type == double.class)
			return "float";
		if (type == Boolean.class || type == boolean.class)
			return "bool";
		if (List.class.isAssignableFrom(klass()) ||
				Set.class.isAssignableFrom(klass()) ||
				Deque.class.isAssignableFrom(klass()))
			return "array";
		if (!klass().isPrimitive())
			return "object";
		throw new AssertionError();
	}

}
