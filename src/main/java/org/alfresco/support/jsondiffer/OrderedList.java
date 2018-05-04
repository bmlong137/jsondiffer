package org.alfresco.support.jsondiffer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class OrderedList<T extends Comparable<T>> implements List<T> {

	private List<T> list = new LinkedList<T>();
	
	@Override
	public boolean isEmpty() {
		return this.list.isEmpty();
	}
	
	@Override
	public int size() {
		return this.list.size();
	}
	
	public synchronized int countLessThan(T e, boolean inclusive) {
		int index = Collections.binarySearch(this.list, e);
		if (index >= 0) {
			ListIterator<T> i = this.list.listIterator(index);
			if (inclusive) {
				while (i.hasNext() && i.next().equals(e))
					index++;
			} else {
				while (i.hasPrevious() && i.previous().equals(e))
					index--;
			}
			return index;
		} else { // found an insertion point
			return -(index+1);
		}
	}
	
	public synchronized int countGreaterThan(T e, boolean inclusive) {
		int index = Collections.binarySearch(this.list, e);
		if (index >= 0) {
			// find the index after the last index
			ListIterator<T> i = this.list.listIterator(index);
			if (inclusive) {
				while (i.hasPrevious() && i.previous().equals(e))
					index--;
			} else {
				while (i.hasNext() && i.next().equals(e))
					index++;
			}
			return this.list.size() - index;
		} else { // found an insertion point
			return this.list.size() + (index+1);
		}
	}
	
	@Override
	public void clear() {
		this.list.clear();
	}

	public boolean contains(T o) {
		return Collections.binarySearch(this.list, o) >= 0;
	}
	
	@Override
	public boolean contains(Object o) {
		return this.list.contains(o);
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		return this.list.containsAll(c);
	}

	public synchronized int indexOf(T o) {
		int index = Collections.binarySearch(this.list, o);
		if (index < 0)
			return -1;
		ListIterator<T> i = this.list.listIterator(index);
		while (i.hasPrevious() && i.previous().equals(o))
			index--;
		return index;
	}
	
	@Override
	public int indexOf(Object o) {
		return this.list.indexOf(o);
	}

	public synchronized int lastIndexOf(T o) {
		int index = Collections.binarySearch(this.list, o);
		if (index < 0)
			return -1;
		ListIterator<T> i = this.list.listIterator(index);
		while (i.hasNext() && i.next().equals(o))
			index++;
		return index-1;
	}
	
	@Override
	public int lastIndexOf(Object o) {
		return this.list.lastIndexOf(o);
	}
	
	@Override
	public T get(int index) {
		return this.list.get(index);
	}
	
	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return this.list.subList(fromIndex, toIndex);
	}
	
	@Override
	public Object[] toArray() {
		return this.list.toArray();
	}
	
	public <U extends Object> U[] toArray(U[] a) {
		return this.list.toArray(a);
	}
	
	@Override
	public Iterator<T> iterator() {
		return this.list.iterator();
	}
	
	@Override
	public ListIterator<T> listIterator() {
		return this.listIterator(0);
	}
	
	@Override
	public ListIterator<T> listIterator(final int index) {
		return new ListIterator<T>() {
			
			ListIterator<T> i = list.listIterator(index);
			
			@Override
			public boolean hasNext() {
				return this.i.hasNext();
			}
			
			@Override
			public boolean hasPrevious() {
				return this.i.hasPrevious();
			}
			
			@Override
			public int nextIndex() {
				return this.i.nextIndex();
			}
			
			@Override
			public int previousIndex() {
				return this.i.previousIndex();
			}
			
			@Override
			public T next() {
				return this.i.next();
			}
			
			@Override
			public T previous() {
				return this.i.previous();
			}
			
			@Override
			public void remove() {
				this.i.remove();
			}
			
			@Override
			public void add(T e) {
				throw new UnsupportedOperationException("This list is ordered, so direct insertions are not supported");
			}
			
			@Override
			public void set(T e) {
				throw new UnsupportedOperationException("This list is ordered, so direct updates are not supported");
			}
		};
	}
	
	@Override
	public synchronized boolean add(T e) {
		if (e == null)
			throw new IllegalArgumentException();
		
		int index = Collections.binarySearch(this.list, e);
		if (index < 0)
			index = -(index+1);

		this.list.add(index, e);
		return true;
	}
	
	@Override
	public void add(int index, T e) {
		throw new UnsupportedOperationException("This list is ordered, so direct insertions are not supported");
	}
	
	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean success = true;
		for (T t : c)
			if (!this.add(t))
				success = false;
		return success;
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException("This list is ordered, so direct insertions are not supported");
	}
	
	@Override
	public T remove(int index) {
		return this.list.remove(index);
	}

	public synchronized boolean remove(T o) {
		int index = Collections.binarySearch(this.list, o);
		if (index < 0)
			return false;
		this.list.remove(index);
		return true;
	}
	
	@Override
	public boolean remove(Object o) {
		return this.list.remove(o);
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		return this.list.removeAll(c);
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		return this.list.retainAll(c);
	}
	
	@Override
	public T set(int index, T e) {
		throw new UnsupportedOperationException("This list is ordered, so direct updates are not supported");
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Collection))
			return false;
		
		Collection<?> c = (Collection<?>)obj;
		if (this.size() != c.size())
			return false;
		
		Iterator<T> i = this.iterator();
		Iterator<?> j = c.iterator();
		while (i.hasNext() && j.hasNext()) {
			T t = i.next();
			Object q = j.next();
			if (!t.equals(q))
				return false;
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(this.list.toArray());
	}

}
