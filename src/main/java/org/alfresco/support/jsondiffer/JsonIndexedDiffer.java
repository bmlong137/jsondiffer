package org.alfresco.support.jsondiffer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.support.jsondiffer.Patch.Operation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.JsonDiff;

public class JsonIndexedDiffer extends AbstractDiffer<ContainerNode<?>, String> {
	
	//private final Logger logger = LoggerFactory.getLogger(JsonIndxedDiffer.class);
	
	private final Set<KeyPath> uniqueKeyPaths;
	
	public JsonIndexedDiffer() {
		this.uniqueKeyPaths = new HashSet<>(0);
	}
	
	public JsonIndexedDiffer(Set<KeyPath> uniqueKeyPaths) {
		this.uniqueKeyPaths = uniqueKeyPaths;
	}
	
	public List<Patch<String>> diff(ContainerNode<?> sourceJson, ContainerNode<?> targetJson) {
		ContainerNode<?> alignedJson = sourceJson;
		
		List<Patch<String>> allPatches = new LinkedList<Patch<String>>();
		
		// first diff the jsons considering only unique key move/add operations
		// this will result in 0...many MOVE/ADD operations
		// a MOVE can be an array index or an object key change
		// all extras (to be removed) will be at the end of an array or object
		List<Patch<String>> patches = this.diffUniqueKeyAddsMoves(alignedJson, targetJson);
		while (!patches.isEmpty()) {
			// apply those MOVE operations to the source to align better with the target
			alignedJson = this.patch(alignedJson, patches);
			
			// try it again to make sure we caught everything
			allPatches.addAll(patches);
			patches = this.diffUniqueKeyAddsMoves(alignedJson, targetJson);
		}
		
		ArrayNode patchesAsJson = (ArrayNode)JsonDiff.asJson(alignedJson, targetJson);
		for (JsonNode patchAsJson : patchesAsJson)
			allPatches.add(this.fromZjsonPatch(patchAsJson, alignedJson, targetJson));
		
		return allPatches;
	}
	
	/**
	 * Perform diff between JSON structures with respect to array index and
	 * object key moves and additions only.
	 * 
	 * @param sourceJson The source/original JSON
	 * @param targetJson The target/resultant JSON
	 * @return A list of move/add patches
	 */
	private List<Patch<String>> diffUniqueKeyAddsMoves(ContainerNode<?> sourceJson, ContainerNode<?> targetJson) {
		JsonIndexer sourceIndexer = new JsonIndexer(this.uniqueKeyPaths);
		sourceIndexer.index(sourceJson);
		
		JsonIndexer targetIndexer = new JsonIndexer(this.uniqueKeyPaths);
		targetIndexer.index(targetJson);
		
		// find matching paths based on unique key paths
		List<Entry<Path<String>, Path<String>>> uniqueKeyPaths = sourceIndexer.computeUniqueKeyPathMapping(targetIndexer);
		
		// sort the paths by target index value; 0, 1, 2, etc...
		// if there are multiple key paths, all 0s will be 1st, then 1s, etc...
		Collections.sort(uniqueKeyPaths, new Comparator<Entry<Path<String>, Path<String>>>() {
			@Override
			public int compare(Entry<Path<String>, Path<String>> uniqueKeyPath1, Entry<Path<String>, Path<String>> uniqueKeyPath2) {
				int targetArrayIndex1 = uniqueKeyPath1.getValue().getLastPathElementAsInteger();
				int targetArrayIndex2 = uniqueKeyPath2.getValue().getLastPathElementAsInteger();
				return targetArrayIndex1 - targetArrayIndex2;
			}
		});
		
		// prepare index tracker
		Map<Path<String>, ArrayIndexTracker> ancestorIndexTracker = new HashMap<>();
		for (Entry<Path<String>, Integer> uniqueKeyPathCount : sourceIndexer.getUniqueKeyPathCounts().entrySet())
			ancestorIndexTracker.put(uniqueKeyPathCount.getKey(), new ArrayIndexTracker(uniqueKeyPathCount.getValue()));

		List<Patch<String>> patches = new LinkedList<>();
		
		// move source locations to the target locations
		for (Entry<Path<String>, Path<String>> uniqueKeyPath : uniqueKeyPaths) {
			int sourceArrayIndex = uniqueKeyPath.getKey().getLastPathElementAsInteger();
			int targetArrayIndex = uniqueKeyPath.getValue().getLastPathElementAsInteger();
			
			if (sourceArrayIndex < 0 && targetArrayIndex < 0) {
				// not an array; probably an object key move
				patches.add(new Patch<String>(uniqueKeyPath.getKey(), uniqueKeyPath.getValue()));
			} else if (sourceArrayIndex < 0 || targetArrayIndex < 0) {
				throw new IllegalArgumentException();
			} else {
				Path<String> ancestor = uniqueKeyPath.getKey().getParent();
				if (ancestor == null)
					ancestor = new KeyPath();
				ArrayIndexTracker indexTracker = ancestorIndexTracker.get(ancestor);
				
				int adjSourceArrayIndex = indexTracker.determineAdjustedIndex(sourceArrayIndex);
				if (adjSourceArrayIndex == targetArrayIndex)
					continue;
				
				if (targetArrayIndex > adjSourceArrayIndex) {
					Path<String> sourcePath = ancestor.add("[" + adjSourceArrayIndex + "]");
					patches.add(new Patch<String>(Operation.Add, sourcePath, sourceJson.nullNode()));
					
					indexTracker.recordInsertAtAdjustedIndex(adjSourceArrayIndex);
					adjSourceArrayIndex++;
					if (adjSourceArrayIndex == targetArrayIndex)
						continue;
				}

				Path<String> sourcePath = ancestor.add("[" + adjSourceArrayIndex + "]");
				patches.add(new Patch<String>(sourcePath, uniqueKeyPath.getValue()));

				indexTracker.recordMoveAtOriginalIndex(sourceArrayIndex, targetArrayIndex);
			}
		}

		return patches;
	}

	public ContainerNode<?> patch(ContainerNode<?> sourceJson, Collection<Patch<String>> patches) {
		ContainerNode<?> targetJson = sourceJson.deepCopy();
		
		for (Patch<String> patch : patches) {
			ContainerNode<?> patchJson = this.getPathParent(patch.getPath(), targetJson);
			ContainerNode<?> toPatchJson = patch.getToPath() == null ? null : this.getPathParent(patch.getToPath(), targetJson);
			
			String key = patch.getPath().getLastPathElement();
			
			if (patchJson == null && key == null) {
				switch (patch.getOperation()) {
					case Add :
					case Replace :
						targetJson = (ContainerNode<?>)this.toJsonNode(patch.getValue());
						break;
					case Remove :
						targetJson = null;
						break;
					default :
						throw new UnsupportedOperationException();
				}
			} else if (patchJson == null) {
				// patch path is at the root
				
				if (targetJson.isObject()) {
					this.patchDescendant(patch, (ObjectNode)targetJson);
				} else if (targetJson.isArray()) {
					this.patchDescendant(patch, (ArrayNode)targetJson);
				} else {
					throw new RuntimeException("This should never happen!");
				}
			} else if (patchJson.isObject()) {
				this.patchDescendant(patch, (ObjectNode)patchJson, (ContainerNode<?>)toPatchJson);
			} else if (patchJson.isArray()) {
				this.patchDescendant(patch, (ArrayNode)patchJson, (ContainerNode<?>)toPatchJson);
			} else {
				throw new RuntimeException("This should never happen!");
			}
		}
		
		return targetJson;
	}
	
	private ContainerNode<?> getPathParent(Path<String> path, ContainerNode<?> targetJson) {
		Path<String> parentPath = path.getParent();
		if (parentPath == null)
			return null;
		
		ObjectNode targetObjChild = targetJson.isObject() ? (ObjectNode)targetJson : null;
		ArrayNode targetArrChild = targetJson.isArray() ? (ArrayNode)targetJson : null;
		JsonNode targetValue = null;
		
		for (String key : parentPath) {
			Integer arrayIndex = null;
			targetValue = null;
			
			// get value of new path
			// this could be a primitive value, but also could be a map or collection
			if (targetObjChild != null) {
				targetValue = targetObjChild.get(key);
			} else if (targetArrChild != null) {
				arrayIndex = path.getPathElementAsInteger(key);
				if (arrayIndex != null && arrayIndex >= 0 && arrayIndex < targetArrChild.size())
					targetValue = targetArrChild.get(arrayIndex);
			} else {
				throw new RuntimeException("This should never happen!");
			}
			
			if (targetValue == null) {
				if (targetObjChild != null) {
					ObjectNode newTargetObjChild = targetObjChild.objectNode();
					targetObjChild.set(key, newTargetObjChild);
					targetObjChild = newTargetObjChild;
				} else {
					ArrayNode newTargetArrChild = targetArrChild.arrayNode();
					targetArrChild.set(arrayIndex, newTargetArrChild);
					targetArrChild = newTargetArrChild;
				}
			} else if (targetValue.isArray()) {
				// if the new path value is a collection, always make a deep copy to avoid mutable source/target issues
				// the "deepness" of the copy is a virtue of the recursion
				
				ArrayNode newTargetArrChild = (ArrayNode)targetValue; //.deepCopy();
				if (targetObjChild != null) {
					targetObjChild.set(key, newTargetArrChild);
					targetObjChild = null;
				} else if (arrayIndex != null && arrayIndex >= 0 && arrayIndex < targetArrChild.size()) {
					targetArrChild.set(arrayIndex, newTargetArrChild);
				} else {
					targetArrChild.add(newTargetArrChild);
				}
				targetArrChild = newTargetArrChild;
			} else if (targetValue.isObject()) {
				// if the new path value is a map, always make a deep copy to avoid mutable source/target issues
				// the "deepness" of the copy is a virtue of the recursion
				
				ObjectNode newTargetObjChild = (ObjectNode)targetValue; //.deepCopy();
				if (targetObjChild != null) {
					targetObjChild.set(key, newTargetObjChild);
				} else if (arrayIndex != null && arrayIndex >= 0 && arrayIndex < targetArrChild.size()) {
					targetArrChild.set(arrayIndex, newTargetObjChild);
					targetArrChild = null;
				} else {
					targetArrChild.add(newTargetObjChild);
					targetArrChild = null;
				}
				targetObjChild = newTargetObjChild;
			} else {
				// the value is already shallow copied to target; hopefully it is immutable
				targetObjChild = null;
				targetArrChild = null;
			}
		}
		
		return targetObjChild != null ? targetObjChild : targetArrChild;
	}
	
	private void patchDescendant(Patch<String> patch, ObjectNode json) {
		this.patchDescendant(patch, json, json);
	}
	
	private void patchDescendant(Patch<String> patch, ObjectNode fromJson, ContainerNode<?> toJson) {
		String key = patch.getPath().getLastPathElement();
		String toKey;
		
		switch (patch.getOperation()) {
			case Add :
			case Replace :
				fromJson.set(key, this.toJsonNode(patch.getValue()));
				break;
			case Remove :
				fromJson.remove(key);
				break;
			case Move :
				toKey = patch.getToPath().getLastPathElement();
				if (toJson == null) {
					throw new IllegalArgumentException();
				} else if (toJson.isArray()) {
					int toIndex = patch.getPath().getPathElementAsInteger(toKey);
					((ArrayNode)toJson).set(toIndex, fromJson.remove(key));
				} else if (toJson.isObject()) {
					((ObjectNode)toJson).set(toKey, fromJson.remove(key));
				}
				break;
			case Copy :
				toKey = patch.getToPath().getLastPathElement();
				if (toJson == null) {
					throw new IllegalArgumentException();
				} else if (toJson.isArray()) {
					int toIndex = patch.getPath().getPathElementAsInteger(toKey);
					((ArrayNode)toJson).set(toIndex, fromJson.get(key).deepCopy());
				} else if (toJson.isObject()) {
					((ObjectNode)toJson).set(toKey, fromJson.get(key).deepCopy());
				}
				break;
			default :
				throw new RuntimeException("This should never happen!");
		}
	}
	
	private void patchDescendant(Patch<String> patch, ArrayNode json) {
		this.patchDescendant(patch, json, json);
	}
	
	private void patchDescendant(Patch<String> patch, ArrayNode fromJson, ContainerNode<?> toJson) {
		int fromIndex = patch.getPath().getLastPathElementAsInteger();
		String toKey;
		
		switch (patch.getOperation()) {
			case Add :
				if (fromIndex >= 0 && fromIndex < fromJson.size()) {
					fromJson.insert(fromIndex, this.toJsonNode(patch.getValue()));
				} else {
					fromJson.add(this.toJsonNode(patch.getValue()));
				}
			case Replace :
				if (fromIndex >= 0 && fromIndex < fromJson.size()) {
					fromJson.set(fromIndex, this.toJsonNode(patch.getValue()));
				} else {
					throw new IllegalArgumentException("The replacement of an array element requires an array index");
				}
				break;
			case Remove :
				if (fromIndex >= 0 && fromIndex < fromJson.size()) {
					fromJson.remove(fromIndex);
				} else {
					throw new IllegalArgumentException("The removal of an array element requires an array index");
				}
				break;
			case Move :
				toKey = patch.getToPath().getLastPathElement();

				if (toJson == null) {
					throw new IllegalArgumentException();
				} else if (toJson.isArray()) {
					int toIndex = patch.getToPath().getPathElementAsInteger(toKey);
					
					if (fromIndex >= 0 && fromIndex < fromJson.size() && toIndex >= 0 && toIndex < toJson.size()) {
						((ArrayNode)toJson).insert(toIndex, fromJson.remove(fromIndex));
					} else {
						throw new IllegalArgumentException("The translation of an array element requires array indexes");
					}
				} else if (toJson.isObject()) {
					if (fromIndex >= 0 && fromIndex < fromJson.size()) {
						((ObjectNode)toJson).set(toKey, fromJson.remove(fromIndex));
					} else {
						throw new IllegalArgumentException("The translation of an array element requires array indexes");
					}
				}
				break;
			case Copy :
				toKey = patch.getToPath().getLastPathElement();

				if (toJson == null) {
					throw new IllegalArgumentException();
				} else if (toJson.isArray()) {
					int toIndex = patch.getToPath().getPathElementAsInteger(toKey);
					
					if (fromIndex >= 0 && fromIndex < fromJson.size() && toIndex >= 0 && toIndex < toJson.size()) {
						((ArrayNode)toJson).insert(toIndex, fromJson.get(fromIndex).deepCopy());
					} else {
						throw new IllegalArgumentException("The translation of an array element requires array indexes");
					}
				} else if (toJson.isObject()) {
					if (fromIndex >= 0 && fromIndex < fromJson.size()) {
						((ObjectNode)toJson).set(toKey, fromJson.get(fromIndex).deepCopy());
					} else {
						throw new IllegalArgumentException("The translation of an array element requires array indexes");
					}
				}
				break;
			default :
				throw new RuntimeException("This should never happen!");
		}
	}
	
	private JsonNode toJsonNode(Object obj) {
		if (obj == null)
			return JsonNodeFactory.instance.nullNode();
		if (obj instanceof JsonNode)
			return (JsonNode)obj;
		
		if (obj instanceof Object[]) {
			ArrayNode jsonArray = JsonNodeFactory.instance.arrayNode();
			for (Object o : (Object[])obj)
				jsonArray.add(this.toJsonNode(o));
			return jsonArray;
		} else if (obj instanceof Map) {
			ObjectNode jsonObj = JsonNodeFactory.instance.objectNode();
			for (Entry<?, ?> e : ((Map<?, ?>)obj).entrySet())
				jsonObj.set(e.getKey().toString(), this.toJsonNode(e.getValue()));
			return jsonObj;
		} else if (obj instanceof Boolean) {
			return JsonNodeFactory.instance.booleanNode((Boolean)obj);
		} else if (obj instanceof CharSequence) {
			return JsonNodeFactory.instance.textNode(((CharSequence)obj).toString());
		} else if (obj instanceof Long) {
			return JsonNodeFactory.instance.numberNode((Long)obj);
		} else if (obj instanceof Integer) {
			return JsonNodeFactory.instance.numberNode((Integer)obj);
		} else if (obj instanceof Short) {
			return JsonNodeFactory.instance.numberNode((Short)obj);
		} else if (obj instanceof Byte) {
			return JsonNodeFactory.instance.numberNode((Byte)obj);
		} else if (obj instanceof Float) {
			return JsonNodeFactory.instance.numberNode((Float)obj);
		} else if (obj instanceof Double) {
			return JsonNodeFactory.instance.numberNode((Double)obj);
		} else if (obj instanceof BigInteger) {
			return JsonNodeFactory.instance.numberNode((BigInteger)obj);
		} else if (obj instanceof BigDecimal) {
			return JsonNodeFactory.instance.numberNode((BigDecimal)obj);
		} else {
			return JsonNodeFactory.instance.pojoNode(obj);
		}
	}
	
	private Patch<String> fromZjsonPatch(JsonNode patchAsJson, ContainerNode<?> sourceJsonRef, ContainerNode<?> targetJsonRef) {
		if (!patchAsJson.has("op"))
			throw new IllegalArgumentException();
		
		Path<String> path, fromPath;
		
		switch (patchAsJson.get("op").asText().toLowerCase()) {
			case "replace" :
				if (!patchAsJson.has("path") || !patchAsJson.has("value"))
					throw new IllegalArgumentException();
				path = this.fromZjsonPath(patchAsJson.get("path").asText(), targetJsonRef);
				return new Patch<String>(Operation.Replace, path, patchAsJson.get("value").deepCopy());
			case "move" :
				if (!patchAsJson.has("path") || !patchAsJson.has("from"))
					throw new IllegalArgumentException();
				fromPath = this.fromZjsonPath(patchAsJson.get("from").asText(), sourceJsonRef);
				path = this.fromZjsonPath(patchAsJson.get("path").asText(), targetJsonRef);
				return new Patch<String>(Operation.Move, fromPath, path);
			case "add" :
				if (!patchAsJson.has("path") || !patchAsJson.has("value"))
					throw new IllegalArgumentException();
				path = this.fromZjsonPath(patchAsJson.get("path").asText(), targetJsonRef);
				return new Patch<String>(Operation.Add, path, patchAsJson.get("value").deepCopy());
			case "remove" :
				if (!patchAsJson.has("path"))
					throw new IllegalArgumentException();
				path = this.fromZjsonPath(patchAsJson.get("path").asText(), targetJsonRef);
				return new Patch<String>(Operation.Remove, path);
			case "copy" :
				if (!patchAsJson.has("path") || !patchAsJson.has("from"))
					throw new IllegalArgumentException();
				fromPath = this.fromZjsonPath(patchAsJson.get("from").asText(), sourceJsonRef);
				path = this.fromZjsonPath(patchAsJson.get("path").asText(), targetJsonRef);
				return new Patch<String>(Operation.Copy, fromPath, path);
			default :
				throw new IllegalArgumentException("The '" + patchAsJson.get("op").asText() + "' operation is not expected");
		}
	}
	
	private Path<String> fromZjsonPath(String zjsonPath, ContainerNode<?> jsonRef) {
		KeyPath path = new KeyPath();
		
		int i = 0;
		if (zjsonPath.charAt(0) == '/')
			i++;
		while (i < zjsonPath.length()) {
			int nextSlash = zjsonPath.indexOf('/', i);
			if (nextSlash < 0)
				nextSlash = zjsonPath.length();
			
			String pathElement = zjsonPath.substring(i, nextSlash);
			JsonNode childJsonRef = null;
			if (jsonRef.isArray()) {
				try {
					int num = Integer.parseInt(pathElement);
					// is a number; treating as array index
					pathElement = "[" + num + "]";

					childJsonRef = jsonRef.get(num);
				} catch (NumberFormatException nfe) {
					// not a number; no worries, treating as normal
					childJsonRef = jsonRef.get(pathElement);
				}
			} else if (jsonRef.isObject()) {
				childJsonRef = jsonRef.get(pathElement);
			} else {
				throw new IllegalArgumentException();
			}			
			
			path = path.add(pathElement);
			if (childJsonRef != null && childJsonRef.isContainerNode())
				jsonRef = (ContainerNode<?>)childJsonRef;
			
			i = nextSlash+1;
		}
		
		return path;
	}

}
