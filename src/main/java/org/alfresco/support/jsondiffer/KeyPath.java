package org.alfresco.support.jsondiffer;

import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeyPath implements Path<String> {
	
	private final Pattern arrayPattern = Pattern.compile("\\[([0-9]*)\\]");
	
	private final KeyPath parent;
	private final String key;
	private final String broadKey;
	private final int depth;
	private final int parentHashCode;
	
	public KeyPath() {
		this.parent = null;
		this.key = null;
		this.broadKey = null;
		this.depth = 0;
		this.parentHashCode = 0;
	}
	
	public KeyPath(String... keys) {
		KeyPath parent = null;
		if (keys.length > 1) {
			parent = new KeyPath(keys[0]);
			for (int i = 1; i < keys.length-1; i++)
				parent = new KeyPath(parent, keys[i]);
		}
		
		this.parent = parent;
		this.key = keys[keys.length-1];
		this.broadKey = this.toBroadKey(this.key);
		this.depth = (parent == null ? 0 : parent.depth) + 1;
		this.parentHashCode = parent == null ? 0 : parent.hashCode();
	}
	
	private KeyPath(KeyPath parent, String key) {
		this.parent = parent.key == null ? null : parent;
		this.key = key;
		this.broadKey = this.toBroadKey(key);
		this.depth = parent.depth + 1;
		this.parentHashCode = parent.hashCode();
	}

	@Override
	public KeyPath add(String key) {
		return new KeyPath(this, key);
	}
	
	@Override
	public KeyPath add(Path<String> relativePath) {
		KeyPath path = this;
		if (relativePath.elements() > 0)
			for (String pathElement : relativePath)
				path = path.add(pathElement);
		return path;
	}

	@Override
	public KeyPath getParent() {
		return this.parent;
	}
	
	@Override
	public String getPathElement(int index) {
		if (index <= 0) return this.key;
		else return this.getParent().getPathElement(index-1);
	}
	
	@Override
	public boolean isPathElementNumeric(int index) {
		String key = this.getPathElement(index);
		return this.isPathElementNumeric(key);
	}

	@Override
	public Integer getPathElementAsInteger(int index) {
		String key = this.getPathElement(index);
		return this.getPathElementAsInteger(key);
	}
	
	@Override
	public String getLastPathElement() {
		return this.key;
	}
	
	@Override
	public boolean isLastPathElementNumeric() {
		String key = this.getLastPathElement();
		return this.isPathElementNumeric(key);
	}

	@Override
	public Integer getLastPathElementAsInteger() {
		String key = this.getLastPathElement();
		return this.getPathElementAsInteger(key);
	}
	
	public String getBroadKey() {
		return this.broadKey;
	}
	
	@Override
	public boolean startsWith(Path<String> path) {
		if (path.elements() == 0)
			return true;
		int ignoreElements = this.elements() - path.elements();
		if (ignoreElements < 0)
			return false;
		
		KeyPath compareKeyPath = this;
		for (int i = 0; i < ignoreElements; i++)
			compareKeyPath = compareKeyPath.getParent();
		
		return path.equals(compareKeyPath); // equals is reverse or for contains/containsKey functionality
	}
	
	@Override
	public boolean startsWith(String key, String... keys) {
		int ignoreElements = this.elements() - keys.length - 1;
		if (ignoreElements < 0)
			return false;

		KeyPath compareKeyPath = this;
		for (int i = 0; i < ignoreElements; i++)
			compareKeyPath = compareKeyPath.getParent();

		return compareKeyPath.equals(key, keys);
	}
	
	@Override
	public boolean endsWith(Path<String> path) {
		int ignoreElements = this.elements() - path.elements();
		if (ignoreElements < 0)
			return false;
		
		KeyPath keyPath = (KeyPath)path;
		if (keyPath.key == null) return true;
		else if (keyPath.getParent() == null) return this.key.equals(keyPath.key) || this.key.equals(keyPath.broadKey);
		else return (this.key.equals(keyPath.key) || this.key.equals(keyPath.broadKey)) && this.parent.endsWith(keyPath.parent);
	}
	
	@Override
	public boolean endsWith(String key, String... keys) {
		int ignoreElements = this.elements() - keys.length - 1;
		if (ignoreElements < 0)
			return false;

		KeyPath compareKeyPath = this;
		for (int i = keys.length-1; i >= 0; i--) {
			if (!compareKeyPath.key.equals(keys[i]) && !compareKeyPath.key.equals(this.toBroadKey(keys[i])))
				return false;
			compareKeyPath = compareKeyPath.getParent();
		}

		return compareKeyPath.key.equals(key) || compareKeyPath.key.equals(this.toBroadKey(key));
	}
	
	@Override
	public int elements() {
		return this.depth;
	}
	
	@Override
	public Iterator<String> iterator() {
		final Stack<String> stack = new Stack<String>();
		KeyPath path = this;
		while (path != null) {
			stack.push(path.key);
			path = path.getParent();
		}
		
		return new Iterator<String>() {
			@Override
			public boolean hasNext() {
				return !stack.isEmpty();
			}
			
			@Override
			public String next() {
				try {
					return stack.pop();
				} catch (EmptyStackException ese) {
					throw new NoSuchElementException();
				}
			}
		};
	}
	
	@Override
	public Path<String> subpath(int elementIndex) {
		if (elementIndex < 0 || elementIndex > this.depth)
			throw new IndexOutOfBoundsException();
		if (elementIndex == this.depth)
			return new KeyPath();
		if (elementIndex == this.depth - 1)
			return new KeyPath(this.key);
		
		if (this.parent == null)
			throw new IndexOutOfBoundsException();
		
		Path<String> path = this.parent.subpath(elementIndex);
		return path.add(this.key);
	}
	
	@Override
	public Path<String> subpath(int elementStartIndex, int elementEndIndex) {
		if (elementStartIndex < 0 || elementStartIndex > this.depth)
			throw new IndexOutOfBoundsException();
		if (elementStartIndex == this.depth)
			return new KeyPath();
		if (elementStartIndex == this.depth - 1)
			return new KeyPath(this.key);
		
		if (this.parent == null)
			throw new IndexOutOfBoundsException();
		
		Path<String> path = this.parent.subpath(elementStartIndex, elementEndIndex);
		if (elementEndIndex <= this.depth - 1)
			return path;
		
		return path.add(this.key);
	}
	
	@Override
	public int hashCode() {
		return (this.key == null ? 0 : this.broadKey.hashCode()) + this.depth * this.parentHashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof KeyPath))
			return false;
		
		KeyPath path = (KeyPath)obj;
		if (this.depth == 0) return this.depth == path.depth;
		else if (this.key == null) return this.depth == path.depth && this.key == path.key;
		else if (this.parent == null) return this.depth == path.depth && (this.key.equals(path.key) || this.broadKey.equals(path.key)) && this.parent == path.parent;
		else return this.depth == path.depth && (this.key.equals(path.key) || this.broadKey.equals(path.key)) && this.parent.equals(path.parent);
	}
	
	public boolean equals(String key, String... keys) {
		if (this.depth != keys.length + 1)
			return false;
		
		KeyPath compareKeyPath = this;
		for (int i = keys.length-1; i >= 0; i--) {
			if (!compareKeyPath.key.equals(keys[i]) && !compareKeyPath.key.equals(this.toBroadKey(keys[i])))
				return false;
			compareKeyPath = compareKeyPath.getParent();
		}
		
		return compareKeyPath.key.equals(key) || compareKeyPath.key.equals(this.toBroadKey(key));
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		KeyPath path = this;
		while (path != null && path.key != null) {
			str.insert(0, path.key).insert(0, '/');
			path = path.getParent();
		}
		if (str.length() > 0)
			str.deleteCharAt(0);
		return str.toString();
	}
	
	public boolean isPathElementNumeric(String pathElement) {
		Matcher matcher = this.arrayPattern.matcher(pathElement);
		return matcher.find();
	}

	public Integer getPathElementAsInteger(String pathElement) {
		Matcher matcher = this.arrayPattern.matcher(pathElement);
		if (!matcher.find())
			return null;
		String arrayIndexStr = matcher.group(1);
		return arrayIndexStr.length() == 0 ? null : Integer.parseInt(arrayIndexStr);
	}
	
	private String toBroadKey(String narrowKey) {
		return this.arrayPattern.matcher(narrowKey).matches() ? "[]" : "{}";
	}

}
