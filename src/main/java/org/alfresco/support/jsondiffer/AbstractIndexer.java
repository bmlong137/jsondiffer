package org.alfresco.support.jsondiffer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public abstract class AbstractIndexer<T, PathElementType> {
	
	public AbstractIndexer() {
	}
	
	public AbstractIndexer(Set<? extends Path<PathElementType>> uniqueKeyPaths) {
		this.validateMutualExclusivity(uniqueKeyPaths);
	}
	
	protected void validateMutualExclusivity(Set<? extends Path<PathElementType>> uniqueKeyPaths) {
		// validate unique key paths
		for (Path<PathElementType> uniqueKeyPath : uniqueKeyPaths) {
			for (Path<PathElementType> uniqueKeyPath2 : uniqueKeyPaths)
				if (uniqueKeyPath.startsWith(uniqueKeyPath2) && !uniqueKeyPath.equals(uniqueKeyPath2))
					throw new IllegalArgumentException("The unique key paths must be mutually exclusive");
		}
	}
	
	public List<Entry<Path<String>, Path<String>>> computeUniqueKeyPathMapping(AbstractIndexer<T, PathElementType> indexer) {
		Map<Path<String>, Map<Object, Path<String>>> valueFromPaths = new HashMap<>();
		List<Entry<Path<String>, Path<String>>> paths = new LinkedList<>();
		
		for (Entry<Path<String>, Object> keyValue : this.getIndexOfParentUniqueValues().entrySet()) {
			Path<String> grandparent = keyValue.getKey().getParent();
			if (grandparent == null)
				grandparent = new KeyPath();
			Map<Object, Path<String>> valueFromPath = valueFromPaths.get(grandparent);
			if (valueFromPath == null)
				valueFromPaths.put(grandparent, valueFromPath = new HashMap<>());
			valueFromPath.put(keyValue.getValue(), keyValue.getKey());
		}
		
		for (Entry<Path<String>, Object> keyValue : indexer.getIndexOfParentUniqueValues().entrySet()) {
			Path<String> grandparent = keyValue.getKey().getParent();
			if (grandparent == null)
				grandparent = new KeyPath();
			Map<Object, Path<String>> valueFromPath = valueFromPaths.get(grandparent);
			if (valueFromPath != null && valueFromPath.containsKey(keyValue.getValue()))
				paths.add(new SimpleMapEntry<Path<String>, Path<String>>(valueFromPath.get(keyValue.getValue()), keyValue.getKey()));
		}
		
		return paths;
	}
	
	public abstract Map<Path<String>, Object> getIndexOfParentUniqueValues();
	
	public abstract void index(T data);

}
