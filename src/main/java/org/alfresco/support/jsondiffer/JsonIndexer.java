package org.alfresco.support.jsondiffer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonIndexer extends AbstractIndexer<ContainerNode<?>, String> {
	
	private final Set<KeyPath> uniqueKeyPaths;
	private final Map<Path<String>, Object> indexOfParentUniqueValues;
	private final Map<Path<String>, Integer> uniqueKeyPathCounts;
	private final Map<Path<String>, ObjectNode> indexOfObjects = new HashMap<Path<String>, ObjectNode>();
	private final Map<Path<String>, ArrayNode> indexOfArrays = new HashMap<Path<String>, ArrayNode>();
	private final Map<Path<String>, JsonNode> indexOfOthers = new HashMap<Path<String>, JsonNode>();
	
	public JsonIndexer() {
		super();
		this.uniqueKeyPaths = new HashSet<>(0);
		this.indexOfParentUniqueValues = new HashMap<>(0);
		this.uniqueKeyPathCounts = new HashMap<>(0);
	}
	
	public JsonIndexer(Set<KeyPath> uniqueKeyPaths) {
		super(uniqueKeyPaths);
		this.uniqueKeyPaths = uniqueKeyPaths;
		this.indexOfParentUniqueValues = new HashMap<>(uniqueKeyPaths.size() * 64);
		this.uniqueKeyPathCounts = new HashMap<>(uniqueKeyPaths.size());

		this.validateMutualExclusivity(uniqueKeyPaths);
	}
	
	public Map<Path<String>, Object> getIndexOfParentUniqueValues() {
		return this.indexOfParentUniqueValues;
	}
	
	public Map<Path<String>, Integer> getUniqueKeyPathCounts() {
		return this.uniqueKeyPathCounts;
	}
	
	public JsonNode findValue(Path<String> path) {
		if (this.indexOfObjects.containsKey(path)) {
			return this.indexOfObjects.get(path);
		} else if (this.indexOfArrays.containsKey(path)) {
			return this.indexOfArrays.get(path);
		} else if (this.indexOfOthers.containsKey(path)) {
			return this.indexOfOthers.get(path);
		} else {
			return null;
		}
	}
	
	public void index(ContainerNode<?> json) {
		KeyPath keyPath = new KeyPath();
		if (json.isObject()) {
			this.index(keyPath, (ObjectNode)json);
		} else if (json.isArray()) {
			this.index(keyPath, (ArrayNode)json);
		} else {
			throw new IllegalArgumentException();
		}
		
		this.indexUniqueKeyCounts();
	}
	
	private void index(KeyPath keyPath, ObjectNode json) {
		this.indexOfObjects.put(keyPath, json.deepCopy());
		
		Iterator<Entry<String, JsonNode>> f = json.fields();
		while (f.hasNext()) {
			Entry<String, JsonNode> field = f.next();
			KeyPath newKeyPath = keyPath.add(field.getKey());
			JsonNode value = field.getValue();

			boolean doIndex = this.uniqueKeyPaths.contains(newKeyPath);
			
			if (value.isObject()) {
				if (doIndex)
					throw new IllegalArgumentException();
				this.index(newKeyPath, (ObjectNode)value);
			} else if (value.isArray()) {
				if (doIndex)
					throw new IllegalArgumentException();
				this.index(newKeyPath, (ArrayNode)value);
			} else {
				this.indexOfOthers.put(newKeyPath, value);
				
				if (doIndex) {
					if (this.indexOfParentUniqueValues.containsKey(keyPath))
						throw new IllegalArgumentException();
					this.indexOfParentUniqueValues.put(keyPath, value);
				}
			}
		}
	}
	
	private void index(KeyPath keyPath, ArrayNode json) {
		this.indexOfArrays.put(keyPath, json.deepCopy());
		
		int i = 0;
		for (JsonNode value : json) {
			KeyPath newKeyPath = keyPath.add("[" + i + "]");

			boolean doIndex = this.uniqueKeyPaths.contains(newKeyPath);
			
			if (value.isObject()) {
				this.index(newKeyPath, (ObjectNode)value);
			} else if (value instanceof Collection) {
				this.index(newKeyPath, (ArrayNode)value);
			} else {
				this.indexOfOthers.put(newKeyPath, value);
				
				if (doIndex) {
					if (this.indexOfParentUniqueValues.containsKey(keyPath))
						throw new IllegalArgumentException();
					this.indexOfParentUniqueValues.put(keyPath, value);
				}
			}
			
			i++;
		}
	}
	
	private void indexUniqueKeyCounts() {
		for (Path<String> uniqueKeyPath : this.uniqueKeyPaths) {
			Path<String> grandparentKeyPath = uniqueKeyPath.getParent().getParent();
			if (grandparentKeyPath == null)
				grandparentKeyPath = new KeyPath();
			
			if (this.uniqueKeyPathCounts.containsKey(grandparentKeyPath))
				continue;
			
			int count = 0;
			
			ArrayNode grandparentArray = this.indexOfArrays.get(grandparentKeyPath);
			if (grandparentArray != null)
				count = grandparentArray.size();
			else {
				ObjectNode grandparentObject = this.indexOfObjects.get(grandparentKeyPath);
				if (grandparentObject != null)
					count = grandparentObject.size();
			}
			
			this.uniqueKeyPathCounts.put(grandparentKeyPath, count);
		}
	}

}
