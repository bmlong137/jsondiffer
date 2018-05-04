package org.alfresco.support.jsondiffer;

import java.util.HashMap;
import java.util.Map;

public class PathIndex<T, PathElementType> extends HashMap<Path<PathElementType>, Map<Object, T>> {
	
	private static final long serialVersionUID = 1L;
	
	public PathIndex() {
		super();
	}
	
	public PathIndex(int initialCapacity) {
		super(initialCapacity);
	}
	
	public void merge(PathIndex<T, PathElementType> indexes) {
		for (Entry<Path<PathElementType>, Map<Object, T>> index : indexes.entrySet()) {
			if (this.containsKey(index.getKey())) {
				this.get(index.getKey()).putAll(index.getValue());
			} else {
				this.put(index.getKey(), index.getValue());
			}
		}
	}

}
