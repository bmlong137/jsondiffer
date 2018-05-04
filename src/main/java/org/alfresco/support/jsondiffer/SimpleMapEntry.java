package org.alfresco.support.jsondiffer;

import java.util.Map.Entry;

public class SimpleMapEntry<E1, E2> implements Entry<E1, E2> {
	
	private final E1 entry1;
	private final E2 entry2;
	
	public SimpleMapEntry(E1 entry1, E2 entry2) {
		this.entry1 = entry1;
		this.entry2 = entry2;
	}
	
	@Override
	public E1 getKey() {
		return this.entry1;
	}
	
	@Override
	public E2 getValue() {
		return this.entry2;
	}
	
	@Override
	public E2 setValue(E2 value) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int hashCode() {
		return this.entry1.hashCode() + this.entry2.hashCode();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SimpleMapEntry))
			return false;
		
		SimpleMapEntry<E1, E2> entry = (SimpleMapEntry<E1, E2>)obj;
		return this.entry1.equals(entry.entry1) && this.entry2.equals(entry.entry2);
	}
	
	@Override
	public String toString() {
		return this.entry1 + "-" + this.entry2;
	}

}
