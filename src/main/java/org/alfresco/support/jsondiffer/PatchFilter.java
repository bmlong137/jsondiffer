package org.alfresco.support.jsondiffer;

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.support.jsondiffer.Patch.Operation;

public class PatchFilter<T> {
	
	private Operation operation;
	private Pattern path;
	private Boolean valueNull;
	
	public PatchFilter(Operation operation, Pattern path) {
		this.operation = operation;
		this.path = path;
	}
	
	public PatchFilter(Operation operation, Pattern path, boolean valueIsNull) {
		this(operation, path);
		this.valueNull = valueIsNull;
	}
	
	public void doExclude(Collection<Patch<T>> patches) {
		Iterator<Patch<T>> i = patches.iterator();
		while (i.hasNext()) {
			Patch<T> patch = i.next();
			if (this.doExclude(patch))
				i.remove();
		}
	}
	
	public boolean doExclude(Patch<T> patch) {
		if (this.operation != null) {
			if (!this.operation.equals(patch.getOperation()))
				return false;
		}
		
		if (this.path != null) {
			if (patch.getPath() == null)
				throw new IllegalArgumentException();
			Matcher matcher = this.path.matcher(patch.getPath().toString());
			if (!matcher.matches())
				return false;
		}
		
		if (this.valueNull != null) {
			if (patch.getValue() == null) return this.valueNull.booleanValue();
			else return !this.valueNull.booleanValue();
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		return "{operation: " + this.operation + "; path: " + this.path + "}";
	}

}
