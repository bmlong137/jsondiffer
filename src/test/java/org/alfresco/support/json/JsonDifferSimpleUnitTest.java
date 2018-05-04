package org.alfresco.support.json;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.alfresco.support.jsondiffer.Differ;
import org.alfresco.support.jsondiffer.JsonIndexedDiffer;
import org.alfresco.support.jsondiffer.KeyPath;
import org.alfresco.support.jsondiffer.Patch;
import org.alfresco.support.jsondiffer.Patch.Operation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ContainerNode;

public class JsonDifferSimpleUnitTest {
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	private Differ<ContainerNode<?>, String> differ;
	
	@Before
	public void setupDiffer() {
		this.differ = new JsonIndexedDiffer();
	}
	
	@Test
	public void emptyArrayToEmptyArray() throws IOException {
		ContainerNode<?> emptyArray = (ContainerNode<?>)this.objectMapper.readTree("[]");
		List<Patch<String>> patches = this.differ.diff(emptyArray, emptyArray);
		Assert.assertTrue(patches.isEmpty());
		
		ContainerNode<?> patched = this.differ.patch(emptyArray, patches);
		Assert.assertTrue(patched.isArray());
		Assert.assertEquals(0, patched.size());
	}
	
	@Test
	public void emptyObjectToEmptyObject() throws IOException {
		ContainerNode<?> emptyObject = (ContainerNode<?>)this.objectMapper.readTree("{}");
		List<Patch<String>> patches = this.differ.diff(emptyObject, emptyObject);
		Assert.assertTrue(patches.isEmpty());
		
		ContainerNode<?> patched = this.differ.patch(emptyObject, patches);
		Assert.assertTrue(patched.isObject());
		Assert.assertEquals(0, patched.size());
	}
	
	@Test
	public void emptyArrayToEmptyObject() throws IOException {
		ContainerNode<?> emptyArray = (ContainerNode<?>)this.objectMapper.readTree("[]");
		ContainerNode<?> emptyObject = (ContainerNode<?>)this.objectMapper.readTree("{}");
		List<Patch<String>> patches = this.differ.diff(emptyArray, emptyObject);
		Assert.assertEquals(1, patches.size());
		
		Iterator<Patch<String>> i = patches.iterator();
		
		Patch<String> patch1 = i.next();
		Assert.assertEquals(Operation.Replace, patch1.getOperation());
		Assert.assertEquals(new KeyPath(), patch1.getPath());
		Assert.assertEquals(emptyObject, patch1.getValue());
	}
	
	@Test
	public void emptyObjectToEmptyArray() throws IOException {
		ContainerNode<?> emptyArray = (ContainerNode<?>)this.objectMapper.readTree("[]");
		ContainerNode<?> emptyObject = (ContainerNode<?>)this.objectMapper.readTree("{}");
		List<Patch<String>> patches = this.differ.diff(emptyObject, emptyArray);
		Assert.assertEquals(1, patches.size());
		
		Iterator<Patch<String>> i = patches.iterator();
		
		Patch<String> patch1 = i.next();
		Assert.assertEquals(Operation.Replace, patch1.getOperation());
		Assert.assertEquals(new KeyPath(), patch1.getPath());
		Assert.assertEquals(emptyArray, patch1.getValue());
	}
	
	@Test
	public void emptyArrayToNonEmptyArray() throws IOException {
		ContainerNode<?> emptyArray = (ContainerNode<?>)this.objectMapper.readTree("[]");
		ContainerNode<?> nonemptyArray = (ContainerNode<?>)this.objectMapper.readTree("[{}]");
		List<Patch<String>> patches = this.differ.diff(emptyArray, nonemptyArray);
		Assert.assertEquals(1, patches.size());
		
		ContainerNode<?> patched = this.differ.patch(emptyArray, patches);
		Assert.assertTrue(patched.isArray());
		Assert.assertEquals(1, patched.size());
		
		JsonNode json = patched.get(0);
		Assert.assertTrue(json.isObject());
		Assert.assertEquals(0, json.size());
	}
	
	@Test
	public void nonEmptyArrayToEmptyArray() throws IOException {
		ContainerNode<?> emptyArray = (ContainerNode<?>)this.objectMapper.readTree("[]");
		ContainerNode<?> nonemptyArray = (ContainerNode<?>)this.objectMapper.readTree("[{}]");
		List<Patch<String>> patches = this.differ.diff(nonemptyArray, emptyArray);
		Assert.assertEquals(1, patches.size());
		
		ContainerNode<?> patched = this.differ.patch(nonemptyArray, patches);
		Assert.assertTrue(patched.isArray());
		Assert.assertEquals(0, patched.size());
	}
	
	@Test
	public void unorderedObject() throws IOException {
		ContainerNode<?> source = (ContainerNode<?>)this.objectMapper.readTree("{\"textType\": \"text\", \"numType\": 0, \"boolType\": \"false\", \"objType\": {}, \"arrayType\": []}");
		ContainerNode<?> target = (ContainerNode<?>)this.objectMapper.readTree("{\"boolType\": \"false\", \"textType\": \"text\", \"arrayType\": [], \"numType\": 0, \"objType\": {}}");
		List<Patch<String>> patches = this.differ.diff(source, target);
		Assert.assertEquals(0, patches.size());
		
		ContainerNode<?> patched = this.differ.patch(source, patches);
		Assert.assertTrue(patched.isObject());
		Assert.assertEquals(5, patched.size());
		Assert.assertEquals("textType", patched.fieldNames().next());
	}
	
	@Test
	public void insertIntoObject() throws IOException {
		ContainerNode<?> source = (ContainerNode<?>)this.objectMapper.readTree("{\"field1\": \"field1\"}");
		ContainerNode<?> target = (ContainerNode<?>)this.objectMapper.readTree("{\"field2\": \"field2\", \"field1\": \"field1\"}");
		List<Patch<String>> patches = this.differ.diff(source, target);
		Assert.assertEquals(1, patches.size());
		
		ContainerNode<?> patched = this.differ.patch(source, patches);
		Assert.assertTrue(patched.isObject());
		Assert.assertEquals(2, patched.size());
		Assert.assertEquals("field1", patched.fieldNames().next());
	}
	
	@Test
	public void unorderedArray1() throws IOException {
		ContainerNode<?> source = (ContainerNode<?>)this.objectMapper.readTree("[1,2,3,4,5]");
		ContainerNode<?> target = (ContainerNode<?>)this.objectMapper.readTree("[5,4,3,2,1]");
		List<Patch<String>> patches = this.differ.diff(source, target);
		Assert.assertEquals(4, patches.size());
		
		ContainerNode<?> patched = this.differ.patch(source, patches);
		Assert.assertTrue(patched.isArray());
		Assert.assertEquals(5, patched.size());
		Assert.assertEquals(5, patched.get(0).asInt());
	}
	
	@Test
	public void unorderedArray2() throws IOException {
		ContainerNode<?> source = (ContainerNode<?>)this.objectMapper.readTree("[{\"id\":1},{\"id\":2},{\"id\":3},{\"id\":4}]");
		ContainerNode<?> target = (ContainerNode<?>)this.objectMapper.readTree("[{\"id\":3},{\"id\":2},{\"id\":1},{\"id\":4}]");
		List<Patch<String>> patches = this.differ.diff(source, target);
		Assert.assertEquals(2, patches.size());
		
		ContainerNode<?> patched = this.differ.patch(source, patches);
		Assert.assertTrue(patched.isArray());
		Assert.assertEquals(target.size(), patched.size());
		Assert.assertEquals(target, patched);
	}
	
	@Test
	public void keyChange() throws IOException {
		ContainerNode<?> source = (ContainerNode<?>)this.objectMapper.readTree("{\"1\":{\"id\": 1, \"desc\": \"Here is some text\"}}");
		ContainerNode<?> target = (ContainerNode<?>)this.objectMapper.readTree("{\"11\":{\"id\": 1, \"desc\": \"Here is some text\"}}");
		List<Patch<String>> patches = this.differ.diff(source, target);
		Assert.assertEquals(1, patches.size());
		
		ContainerNode<?> patched = this.differ.patch(source, patches);
		Assert.assertTrue(patched.isObject());
		Assert.assertEquals(target.size(), patched.size());
		Assert.assertNull(patched.get("1"));
		Assert.assertEquals("Here is some text", patched.get("11").get("desc").asText());
	}
	
	public void output(ContainerNode<?> source, ContainerNode<?> target) throws IOException {
		List<Patch<String>> patches = this.differ.diff(source, target);
		System.out.println(patches);
		
		JsonNode patched = this.differ.patch(source, patches);
		this.objectMapper.writerWithDefaultPrettyPrinter().writeValue(System.out, patched);
	}

}
