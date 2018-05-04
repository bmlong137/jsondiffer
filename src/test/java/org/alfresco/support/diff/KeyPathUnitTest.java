package org.alfresco.support.diff;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.support.jsondiffer.KeyPath;
import org.junit.Assert;
import org.junit.Test;

public class KeyPathUnitTest {
	
	@Test
	public void testToString() {
		Assert.assertEquals("", new KeyPath().toString());
		Assert.assertEquals("", new KeyPath("").toString());
		Assert.assertEquals("key1", new KeyPath("key1").toString());
		Assert.assertEquals("key1/key2", new KeyPath("key1", "key2").toString());
		Assert.assertEquals("key1//key3", new KeyPath("key1", "", "key3").toString());
		Assert.assertEquals("[]", new KeyPath("[]").toString());
		Assert.assertEquals("[1]", new KeyPath("[1]").toString());
		Assert.assertEquals("{}", new KeyPath("{}").toString());
		Assert.assertEquals("{}/[]", new KeyPath("{}", "[]").toString());
	}
	
	@Test
	public void testHashCode() {
		Assert.assertEquals(new KeyPath().hashCode(), new KeyPath().hashCode());
		Assert.assertEquals(new KeyPath("[]").hashCode(), new KeyPath("[]").hashCode());
		Assert.assertEquals(new KeyPath("[]").hashCode(), new KeyPath("[1]").hashCode());
		Assert.assertEquals(new KeyPath("[1]").hashCode(), new KeyPath("[]").hashCode());
		Assert.assertEquals(new KeyPath("[1]").hashCode(), new KeyPath("[1]").hashCode());
		Assert.assertEquals(new KeyPath("{}").hashCode(), new KeyPath("{}").hashCode());
		Assert.assertEquals(new KeyPath("{}").hashCode(), new KeyPath("key1").hashCode());
		Assert.assertEquals(new KeyPath("key1").hashCode(), new KeyPath("{}").hashCode());
		Assert.assertEquals(new KeyPath("key1").hashCode(), new KeyPath("key1").hashCode());
		Assert.assertEquals(new KeyPath("key1", "key2").hashCode(), new KeyPath("key1", "key2").hashCode());
	}
	
	@Test
	public void testEquals() {
		Assert.assertTrue(new KeyPath().equals(new KeyPath()));
		Assert.assertTrue(new KeyPath("[]").equals(new KeyPath("[]")));
		Assert.assertFalse(new KeyPath("[]").equals(new KeyPath("[1]")));
		Assert.assertTrue(new KeyPath("[1]").equals(new KeyPath("[]")));
		Assert.assertTrue(new KeyPath("[1]").equals(new KeyPath("[1]")));
		Assert.assertTrue(new KeyPath("{}").equals(new KeyPath("{}")));
		Assert.assertFalse(new KeyPath("{}").equals(new KeyPath("key1")));
		Assert.assertTrue(new KeyPath("key1").equals(new KeyPath("{}")));
		Assert.assertTrue(new KeyPath("key1").equals(new KeyPath("key1")));
		Assert.assertTrue(new KeyPath("key1", "key2").equals(new KeyPath("key1", "key2")));
	}
	
	@Test
	public void testContains() {
		Set<KeyPath> set = new HashSet<>();
		set.add(new KeyPath("array", "wildcard", "[]"));
		set.add(new KeyPath("array", "specific", "[1]"));
		set.add(new KeyPath("object", "wildcard", "{}"));
		set.add(new KeyPath("object", "specific", "key1"));
		
		Assert.assertFalse(set.contains(new KeyPath("array", "wildcard")));
		Assert.assertTrue(set.contains(new KeyPath("array", "wildcard", "[]")));
		Assert.assertTrue(set.contains(new KeyPath("array", "wildcard", "[1]")));
		Assert.assertFalse(set.contains(new KeyPath("array", "wildcard", "[]", "more")));
		Assert.assertFalse(set.contains(new KeyPath("array", "specific")));
		Assert.assertFalse(set.contains(new KeyPath("array", "specific", "[]")));
		Assert.assertTrue(set.contains(new KeyPath("array", "specific", "[1]")));
		Assert.assertFalse(set.contains(new KeyPath("array", "specific", "[]", "more")));
		
		Assert.assertFalse(set.contains(new KeyPath("object", "wildcard")));
		Assert.assertTrue(set.contains(new KeyPath("object", "wildcard", "{}")));
		Assert.assertTrue(set.contains(new KeyPath("object", "wildcard", "key1")));
		Assert.assertFalse(set.contains(new KeyPath("object", "wildcard", "{}", "more")));
		Assert.assertFalse(set.contains(new KeyPath("object", "specific")));
		Assert.assertFalse(set.contains(new KeyPath("object", "specific", "{}")));
		Assert.assertTrue(set.contains(new KeyPath("object", "specific", "key1")));
		Assert.assertFalse(set.contains(new KeyPath("object", "specific", "{}", "more")));
	}

	@Test
	public void testMap() {
		Map<KeyPath, Boolean> map = new HashMap<>(5);
		map.put(new KeyPath("array", "wildcard", "[]", "more"), true);
		map.put(new KeyPath("array", "specific", "[1]", "more"), true);
		map.put(new KeyPath("object", "wildcard", "{}", "more"), true);
		map.put(new KeyPath("object", "specific", "key1", "more"), true);
		
		Assert.assertNull(map.get(new KeyPath("array", "wildcard")));
		Assert.assertNull(map.get(new KeyPath("array", "wildcard", "[]")));
		Assert.assertNotNull(map.get(new KeyPath("array", "wildcard", "[]", "more")));
		Assert.assertNotNull(map.get(new KeyPath("array", "wildcard", "[1]", "more")));
		Assert.assertNull(map.get(new KeyPath("array", "specific")));
		Assert.assertNull(map.get(new KeyPath("array", "specific", "[]")));
		Assert.assertNull(map.get(new KeyPath("array", "specific", "[]", "more")));
		Assert.assertNotNull(map.get(new KeyPath("array", "specific", "[1]", "more")));
		
		Assert.assertNull(map.get(new KeyPath("object", "wildcard")));
		Assert.assertNull(map.get(new KeyPath("object", "wildcard", "{}")));
		Assert.assertNotNull(map.get(new KeyPath("object", "wildcard", "{}", "more")));
		Assert.assertNotNull(map.get(new KeyPath("object", "wildcard", "key1", "more")));
		Assert.assertNull(map.get(new KeyPath("object", "specific")));
		Assert.assertNull(map.get(new KeyPath("object", "specific", "{}")));
		Assert.assertNull(map.get(new KeyPath("object", "specific", "{}", "more")));
		Assert.assertNotNull(map.get(new KeyPath("object", "specific", "key1", "more")));
	}
	
	@Test
	public void testStartsWith() {
		KeyPath path = new KeyPath("path1", "path2", "[2]", "path3", "path4");

		Assert.assertFalse(path.startsWith(new KeyPath("path1", "path2", "[2]", "path3", "path4", "path5")));
		Assert.assertTrue(path.startsWith(new KeyPath("path1", "path2", "[2]", "path3", "path4")));
		Assert.assertFalse(path.startsWith(new KeyPath("path2", "[2]", "path3", "path4")));
		Assert.assertTrue(path.startsWith(new KeyPath("path1", "path2", "[2]", "path3")));
		Assert.assertTrue(path.startsWith(new KeyPath("path1", "path2", "[2]")));
		Assert.assertTrue(path.startsWith(new KeyPath("path1", "path2")));
		Assert.assertTrue(path.startsWith(new KeyPath("path1")));
		Assert.assertTrue(path.startsWith(new KeyPath()));
	}
	
	@Test
	public void testEndsWith() {
		KeyPath path = new KeyPath("path1", "path2", "[2]", "path3", "path4");

		Assert.assertFalse(path.endsWith(new KeyPath("path1", "path2", "[2]", "path3", "path4", "path5")));
		Assert.assertTrue(path.endsWith(new KeyPath("path1", "path2", "[2]", "path3", "path4")));
		Assert.assertFalse(path.endsWith(new KeyPath("path1", "path2", "[2]", "path3")));
		Assert.assertTrue(path.endsWith(new KeyPath("path2", "[2]", "path3", "path4")));
		Assert.assertTrue(path.endsWith(new KeyPath("[2]", "path3", "path4")));
		Assert.assertTrue(path.endsWith(new KeyPath("path3", "path4")));
		Assert.assertTrue(path.endsWith(new KeyPath("path4")));
		Assert.assertTrue(path.endsWith(new KeyPath()));
	}

	@Test
	public void testSubpath() {
		KeyPath path = new KeyPath("path1", "path2", "[2]", "path3", "path4");
		
		try {
			path.subpath(6);
			Assert.fail();
		} catch (IndexOutOfBoundsException ioobe) {
		}

		try {
			path.subpath(-1);
			Assert.fail();
		} catch (IndexOutOfBoundsException ioobe) {
		}
		
		Assert.assertEquals(new KeyPath("path1", "path2", "[2]", "path3", "path4"), path.subpath(0));
		Assert.assertEquals(new KeyPath("path2", "[2]", "path3", "path4"), path.subpath(1));
		Assert.assertEquals(new KeyPath("[2]", "path3", "path4"), path.subpath(2));
		Assert.assertEquals(new KeyPath("path3", "path4"), path.subpath(3));
		Assert.assertEquals(new KeyPath("path4"), path.subpath(4));
		Assert.assertEquals(new KeyPath(), path.subpath(5));
		
		Assert.assertEquals(new KeyPath("path1", "path2", "[2]", "path3", "path4"), path.subpath(0, 5));
		Assert.assertEquals(new KeyPath("path2", "[2]", "path3", "path4"), path.subpath(1, 5));
		Assert.assertEquals(new KeyPath("[2]", "path3", "path4"), path.subpath(2, 5));
		Assert.assertEquals(new KeyPath("path3", "path4"), path.subpath(3, 5));
		Assert.assertEquals(new KeyPath("path4"), path.subpath(4, 5));
		Assert.assertEquals(new KeyPath(), path.subpath(5, 5));
		
		Assert.assertEquals(new KeyPath("[2]", "path3"), path.subpath(2, 4));
		Assert.assertEquals(new KeyPath("path2"), path.subpath(1, 2));
	}

}
