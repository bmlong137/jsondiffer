package org.alfresco.support.diff;

import java.util.Iterator;
import java.util.ListIterator;

import org.alfresco.support.jsondiffer.OrderedList;
import org.junit.Assert;
import org.junit.Test;

public class OrderedListUnitTest {
	
	@Test
	public void empty() {
		OrderedList<Integer> list = new OrderedList<Integer>();
		Assert.assertTrue(list.isEmpty());
		Assert.assertEquals(0, list.size());
		Assert.assertEquals(-1, list.indexOf(new Object()));
		Assert.assertEquals(-1, list.lastIndexOf(new Object()));
		Assert.assertEquals(-1, list.indexOf(0));
		Assert.assertEquals(-1, list.lastIndexOf(0));
		Assert.assertFalse(list.iterator().hasNext());
		Assert.assertFalse(list.listIterator().hasNext());
	}
	
	@Test
	public void single() {
		OrderedList<Integer> list = new OrderedList<Integer>();
		list.add(256);
		
		Assert.assertFalse(list.isEmpty());
		Assert.assertEquals(1, list.size());
		Assert.assertEquals(-1, list.indexOf(new Object()));
		Assert.assertEquals(-1, list.lastIndexOf(new Object()));
		Assert.assertEquals(-1, list.indexOf(0));
		Assert.assertEquals(-1, list.lastIndexOf(0));
		Assert.assertEquals(0, list.indexOf(256));
		Assert.assertEquals(0, list.lastIndexOf(256));
		
		Iterator<Integer> i = list.iterator();
		Assert.assertTrue(i.hasNext());
		Assert.assertEquals(256, i.next().intValue());
		
		ListIterator<Integer> li = list.listIterator();
		Assert.assertTrue(li.hasNext());
		Assert.assertFalse(li.hasPrevious());
		Assert.assertEquals(256, li.next().intValue());
		
		li = list.listIterator(1);
		Assert.assertFalse(li.hasNext());
		Assert.assertTrue(li.hasPrevious());
		Assert.assertEquals(256, li.previous().intValue());
	}
	
	@Test
	public void balanced() {
		OrderedList<Integer> list = new OrderedList<Integer>();
		list.add(256);
		list.add(128);
		list.add(64);
		list.add(192);
		list.add(512);
		list.add(384);
		list.add(640);
		
		this.assertSame(list);
	}
	
	@Test
	public void unbalanced() {
		OrderedList<Integer> list = new OrderedList<Integer>();
		list.add(64);
		list.add(128);
		list.add(192);
		list.add(256);
		list.add(384);
		list.add(512);
		list.add(640);

		this.assertSame(list);
	}
	
	@Test
	public void copies() {
		OrderedList<Integer> list = new OrderedList<Integer>();
		list.add(1);
		list.add(2);
		list.add(2);
		list.add(3);
		list.add(3);
		list.add(3);
		
		Assert.assertFalse(list.isEmpty());
		Assert.assertEquals(6, list.size());
		Assert.assertEquals(-1, list.indexOf(new Object()));
		Assert.assertEquals(-1, list.lastIndexOf(new Object()));
		Assert.assertEquals(-1, list.indexOf(0));
		Assert.assertEquals(-1, list.lastIndexOf(0));
		Assert.assertEquals(0, list.indexOf(1));
		Assert.assertEquals(1, list.indexOf(2));
		Assert.assertEquals(3, list.indexOf(3));
		Assert.assertEquals(0, list.lastIndexOf(1));
		Assert.assertEquals(2, list.lastIndexOf(2));
		Assert.assertEquals(5, list.lastIndexOf(3));

		Assert.assertEquals(0, list.countLessThan(0, false));
		Assert.assertEquals(0, list.countLessThan(1, false));
		Assert.assertEquals(1, list.countLessThan(2, false));
		Assert.assertEquals(3, list.countLessThan(3, false));
		Assert.assertEquals(6, list.countLessThan(4, false));
		Assert.assertEquals(0, list.countLessThan(0, true));
		Assert.assertEquals(1, list.countLessThan(1, true));
		Assert.assertEquals(3, list.countLessThan(2, true));
		Assert.assertEquals(6, list.countLessThan(3, true));
		Assert.assertEquals(6, list.countLessThan(4, true));
		Assert.assertEquals(6, list.countGreaterThan(0, false));
		Assert.assertEquals(5, list.countGreaterThan(1, false));
		Assert.assertEquals(3, list.countGreaterThan(2, false));
		Assert.assertEquals(0, list.countGreaterThan(3, false));
		Assert.assertEquals(0, list.countGreaterThan(4, false));
		Assert.assertEquals(6, list.countGreaterThan(0, true));
		Assert.assertEquals(6, list.countGreaterThan(1, true));
		Assert.assertEquals(5, list.countGreaterThan(2, true));
		Assert.assertEquals(3, list.countGreaterThan(3, true));
		Assert.assertEquals(0, list.countGreaterThan(4, true));
	}
	
	private void assertSame(OrderedList<Integer> list) {
		Assert.assertFalse(list.isEmpty());
		Assert.assertEquals(7, list.size());
		Assert.assertEquals(-1, list.indexOf(new Object()));
		Assert.assertEquals(-1, list.lastIndexOf(new Object()));
		Assert.assertEquals(-1, list.indexOf(0));
		Assert.assertEquals(-1, list.lastIndexOf(0));
		Assert.assertEquals(3, list.indexOf(256));
		Assert.assertEquals(3, list.lastIndexOf(256));
		Assert.assertEquals(1, list.indexOf(128));
		Assert.assertEquals(6, list.indexOf(640));
		
		Iterator<Integer> i = list.iterator();
		Assert.assertTrue(i.hasNext());
		Assert.assertEquals(64, i.next().intValue());
		Assert.assertEquals(128, i.next().intValue());
		Assert.assertEquals(192, i.next().intValue());
		Assert.assertEquals(256, i.next().intValue());
		Assert.assertEquals(384, i.next().intValue());
		Assert.assertEquals(512, i.next().intValue());
		Assert.assertEquals(640, i.next().intValue());
		
		Assert.assertEquals(0, list.countLessThan(0, false));
		Assert.assertEquals(3, list.countLessThan(255, true));
		Assert.assertEquals(3, list.countLessThan(256, false));
		Assert.assertEquals(4, list.countLessThan(256, true));
		Assert.assertEquals(4, list.countLessThan(257, false));
		Assert.assertEquals(7, list.countLessThan(1024, false));
		Assert.assertEquals(7, list.countGreaterThan(0, false));
		Assert.assertEquals(4, list.countGreaterThan(255, true));
		Assert.assertEquals(3, list.countGreaterThan(256, false));
		Assert.assertEquals(4, list.countGreaterThan(256, true));
		Assert.assertEquals(3, list.countGreaterThan(257, false));
		Assert.assertEquals(0, list.countGreaterThan(1024, false));
	}

}
