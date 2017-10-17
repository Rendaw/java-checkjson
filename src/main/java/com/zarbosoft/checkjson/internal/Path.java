package com.zarbosoft.checkjson.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Path {
	List<PathElement> path = new ArrayList<>();

	public Path() {
		path.add(new PathObject());
	}

	private PathElement top() {
		return path.get(path.size() - 1);
	}

	public void key(final String key) {
		top().key(key);
	}

	public void object() {
		path.add(new PathObject());
	}

	public void array() {
		path.add(new PathArray());
	}

	public void value() {
		top().value();
	}

	public void pop() {
		path.remove(path.size() - 1);
	}

	@Override
	public String toString() {
		return String.format("$%s", path.stream().map(e -> e.toString()).collect(Collectors.joining()));
	}

	public boolean isEmpty() {
		return path.isEmpty();
	}
}
