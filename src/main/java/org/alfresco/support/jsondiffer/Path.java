package org.alfresco.support.jsondiffer;

public interface Path<T> extends Iterable<T> {
	
	Path<T> add(T pathElement);
	
	Path<T> add(Path<T> relativePath);
	
	Path<T> getParent();
	
	T getPathElement(int index);
	
	boolean isPathElementNumeric(int index);
	
	Integer getPathElementAsInteger(int index);
	
	T getLastPathElement();
	
	boolean isLastPathElementNumeric();
	
	Integer getLastPathElementAsInteger();
	
	boolean startsWith(Path<T> path);

	boolean startsWith(T pathElement, @SuppressWarnings("unchecked") T... pathElements);

	boolean endsWith(Path<T> path);
	
	boolean endsWith(T pathElement, @SuppressWarnings("unchecked") T... pathElements);
	
	int elements();
	
	Path<T> subpath(int elementIndex);

	Path<T> subpath(int elementStartIndex, int elementEndIndex);
	
	boolean isPathElementNumeric(T pathElement);
	
	Integer getPathElementAsInteger(T pathElement);

}
