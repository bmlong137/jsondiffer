package org.alfresco.support.diff;

import org.alfresco.support.jsondiffer.ArrayIndexTracker;
import org.junit.Assert;
import org.junit.Test;

public class ArrayIndexTrackerUnitTest {
	
	@Test
	public void empty() {
		ArrayIndexTracker ait = new ArrayIndexTracker(0);
		try {
			ait.determineAdjustedIndex(0);
			Assert.fail();
		} catch (IllegalArgumentException iae) {
		}

		ait.recordInsertAtAdjustedIndex(0);

		try {
			ait.determineAdjustedIndex(0);
			Assert.fail();
		} catch (IllegalArgumentException iae) {
		}
	}
	
	@Test
	public void simple() {
		ArrayIndexTracker ait = new ArrayIndexTracker(8);
		for (int i = 0; i < 8; i++)
			Assert.assertEquals(i, ait.determineAdjustedIndex(i));
		
		ait.recordInsertAtOriginalIndex(0);
		for (int i = 0; i < 8; i++)
			Assert.assertEquals(i+1, ait.determineAdjustedIndex(i));

		ait.recordInsertAtOriginalIndex(4);
		for (int i = 0; i < 4; i++)
			Assert.assertEquals(i+1, ait.determineAdjustedIndex(i));
		for (int i = 4; i < 8; i++)
			Assert.assertEquals(i+2, ait.determineAdjustedIndex(i));

		ait.recordRemoveAtOriginalIndex(2);
		for (int i = 0; i < 3; i++)
			Assert.assertEquals(i+1, ait.determineAdjustedIndex(i));
		for (int i = 3; i < 4; i++)
			Assert.assertEquals(i, ait.determineAdjustedIndex(i));
		for (int i = 4; i < 8; i++)
			Assert.assertEquals(i+1, ait.determineAdjustedIndex(i));
	}
	
	@Test
	public void moveBack() {
		ArrayIndexTracker ait = new ArrayIndexTracker(8);
		
		ait.recordMoveAtOriginalIndex(0, 4);
		Assert.assertEquals(4, ait.determineAdjustedIndex(0));
		for (int i = 1; i < 5; i++)
			Assert.assertEquals(i-1, ait.determineAdjustedIndex(i));
		for (int i = 5; i < 8; i++)
			Assert.assertEquals(i, ait.determineAdjustedIndex(i));
	}
	
	@Test
	public void moveForward() {
		ArrayIndexTracker ait = new ArrayIndexTracker(8);
		
		ait.recordMoveAtOriginalIndex(7, 4);
		for (int i = 0; i < 4; i++)
			Assert.assertEquals(i, ait.determineAdjustedIndex(i));
		for (int i = 4; i < 7; i++)
			Assert.assertEquals(i+1, ait.determineAdjustedIndex(i));
		Assert.assertEquals(4, ait.determineAdjustedIndex(7));
	}
	
	@Test
	public void shiftBack() {
		ArrayIndexTracker ait = new ArrayIndexTracker(8);

		for (int i = 3; i >= 0; i--)
			ait.recordMoveAtOriginalIndex(i, i+1);
		for (int i = 0; i < 4; i++)
			Assert.assertEquals(i+1, ait.determineAdjustedIndex(i));
		Assert.assertEquals(0, ait.determineAdjustedIndex(4));
		for (int i = 5; i < 8; i++)
			Assert.assertEquals(i, ait.determineAdjustedIndex(i));
	}
	
	@Test
	public void shiftForward() {
		ArrayIndexTracker ait = new ArrayIndexTracker(8);

		for (int i = 0; i < 4; i++)
			ait.recordMoveAtOriginalIndex(i+1, i);
		Assert.assertEquals(4, ait.determineAdjustedIndex(0));
		for (int i = 1; i < 5; i++)
			Assert.assertEquals(i-1, ait.determineAdjustedIndex(i));
		for (int i = 5; i < 8; i++)
			Assert.assertEquals(i, ait.determineAdjustedIndex(i));
	}

}
