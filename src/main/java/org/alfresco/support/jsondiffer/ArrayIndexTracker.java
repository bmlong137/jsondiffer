package org.alfresco.support.jsondiffer;

import java.util.HashMap;
import java.util.Map;

public class ArrayIndexTracker {
	
	private Map<Integer, Integer> indexMap = new HashMap<>();
	
	public ArrayIndexTracker(int originalArraySize) {
		for (int i = 0; i < originalArraySize; i++)
			this.indexMap.put(i, i);
	}
	
	public void recordInsertAtOriginalIndex(int origIndex) {
		int adjIndex = this.determineAdjustedIndex(origIndex);
		this.recordInsertAtAdjustedIndex(adjIndex);
	}
	
	public void recordInsertAtAdjustedIndex(int adjIndex) {
		for (int originalIndex : this.indexMap.keySet()) {
			int currentIndex = this.indexMap.get(originalIndex);
			if (adjIndex <= currentIndex)
				this.indexMap.put(originalIndex, currentIndex+1);
		}
	}
	
	public void recordRemoveAtOriginalIndex(int origIndex) {
		int adjIndex = this.determineAdjustedIndex(origIndex);
		this.recordRemoveAtAdjustedIndex(adjIndex);
	}
	
	public void recordRemoveAtAdjustedIndex(int adjIndex) {
		for (int originalIndex : this.indexMap.keySet()) {
			int currentIndex = this.indexMap.get(originalIndex);
			if (adjIndex < currentIndex)
				this.indexMap.put(originalIndex, currentIndex-1);
		}
	}
	
	public void recordMoveAtOriginalIndex(int fromOrigIndex, int toIndex) {
		int fromAdjIndex = this.determineAdjustedIndex(fromOrigIndex);
		
		if (fromAdjIndex == toIndex)
			return;
		
		if (fromAdjIndex < toIndex) {
			for (int originalIndex : this.indexMap.keySet()) {
				int currentIndex = this.indexMap.get(originalIndex);
				if (currentIndex < fromAdjIndex) {
					// do nothing
				} else if (fromAdjIndex == currentIndex) {
					// do nothing
				} else if (fromAdjIndex < currentIndex) {
					if (currentIndex < toIndex) {
						this.indexMap.put(originalIndex, currentIndex-1);
					} else if (currentIndex == toIndex) {
						this.indexMap.put(originalIndex, currentIndex-1);
					} else {
						// do nothing
					}
				}
			}
		} else {
			for (int originalIndex : this.indexMap.keySet()) {
				int currentIndex = this.indexMap.get(originalIndex);
				if (currentIndex < toIndex) {
					// do nothing
				} else if (toIndex == currentIndex) {
					this.indexMap.put(originalIndex, currentIndex+1);
				} else if (toIndex < currentIndex) {
					if (currentIndex < fromAdjIndex) {
						this.indexMap.put(originalIndex, currentIndex+1);
					} else if (currentIndex == fromAdjIndex) {
						// do nothing
					} else {
						// do nothing
					}
				}
			}
		}
		
		this.indexMap.put(fromOrigIndex, toIndex);
	}
	
	public int determineAdjustedIndex(int origIndex) {
		if (this.indexMap.containsKey(origIndex))
			return this.indexMap.get(origIndex);
		throw new IllegalArgumentException();
	}

}
