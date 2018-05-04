package org.alfresco.support.json;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.alfresco.support.jsondiffer.Differ;
import org.alfresco.support.jsondiffer.JsonIndexedDiffer;
import org.alfresco.support.jsondiffer.KeyPath;
import org.alfresco.support.jsondiffer.Patch;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ContainerNode;

public class JsonDifferRealWorldUnitTest {
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	private Differ<ContainerNode<?>, String> differ;
	
	@Before
	public void setupDiffer() {
		this.differ = new JsonIndexedDiffer(
				new HashSet<KeyPath>(Arrays.asList(
						new KeyPath("propertyPackages", "[]", "name"),
						new KeyPath("stencils", "[]", "id"),
						new KeyPath("rules", "cardinalityRules", "[]", "role"),
						new KeyPath("rules", "connectionRules", "[]", "role"),
						new KeyPath("rules", "containmentRules", "[]", "role"),
						new KeyPath("rules", "morphingRules", "[]", "role"),
						new KeyPath("{}", "id"))));
	}
	
	@Test
	public void salesforceStencil1() throws IOException {
		ContainerNode<?> source = (ContainerNode<?>)this.objectMapper.readTree(new File("src/test/resources/salesforce-stencil-1.json"));
		ContainerNode<?> target = (ContainerNode<?>)this.objectMapper.readTree(new File("src/test/resources/salesforce-stencil-2.json"));
		this.diffAndPatch(source, target);
	}
	
	@Test
	public void salesforceStencilA() throws IOException {
		ContainerNode<?> source = (ContainerNode<?>)this.objectMapper.readTree(new File("src/test/resources/salesforce-stencil-a-orig.json"));
		ContainerNode<?> target = (ContainerNode<?>)this.objectMapper.readTree(new File("src/test/resources/salesforce-stencil-a-export.json"));
		this.diffAndPatch(source, target);
	}
	
	@Test
	public void keyChange() throws IOException {
		ContainerNode<?> source = (ContainerNode<?>)this.objectMapper.readTree(new File("src/test/resources/model-stencil-definition-1.json"));
		ContainerNode<?> target = (ContainerNode<?>)this.objectMapper.readTree(new File("src/test/resources/model-stencil-definition-2.json"));
		this.diffAndPatch(source, target);
	}
	
	@Test
	public void salesforceMergeA() throws IOException {
		ContainerNode<?> source = (ContainerNode<?>)this.objectMapper.readTree(new File("src/test/resources/Default.json"));
		ContainerNode<?> target = (ContainerNode<?>)this.objectMapper.readTree(new File("src/test/resources/salesforce-stencil-patch-a.json"));
		this.diffAndPatch(source, target);
	}
	
	public void diffAndPatch(ContainerNode<?> source, ContainerNode<?> target) throws IOException {
		List<Patch<String>> patches = this.differ.diff(source, target);
		ContainerNode<?> patched = this.differ.patch(source, patches);
		Assert.assertEquals(target, patched);
	}

}
