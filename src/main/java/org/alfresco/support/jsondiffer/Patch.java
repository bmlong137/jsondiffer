package org.alfresco.support.jsondiffer;

public class Patch<T> {
	
	public enum Operation {
		Add,
		Remove,
		Replace,
		Move,
		Copy
	}
	
	private final Operation operation;
	private Path<T> path;
	private Path<T> toPath;
	private final Object value;
	
	public Patch(Operation operation, Path<T> path) {
		this(operation, path, null);
	}
	
	public Patch(Operation operation, Path<T> path, Object value) {
		this.operation = operation;
		this.path = path;
		this.toPath = null;
		this.value = value;
	}
	
	public Patch(Path<T> path, Path<T> toPath) {
		this(Operation.Move, path, toPath);
	}
	
	public Patch(Operation operation, Path<T> path, Path<T> toPath) {
		this.operation = operation;
		this.path = path;
		this.toPath = toPath;
		this.value = null;
	}
	
	public Operation getOperation() {
		return this.operation;
	}
	
	public Path<T> getPath() {
		return this.path;
	}
	
	public Path<T> getToPath() {
		return toPath;
	}
	
	public Object getValue() {
		return this.value;
	}
	
	public void setPath(Path<T> path) {
		this.path = path;
	}
	
	public void setToPath(Path<T> toPath) {
		this.toPath = toPath;
	}
	
	@Override
	public String toString() {
		switch (this.operation) {
			case Add :
			case Replace :
				return this.operation + ": " + this.path + ": " + this.value;
			case Remove :
				return this.operation + ": " + this.path;
			case Move :
			case Copy :
				return this.operation + ": " + this.path + ": " + this.toPath;
			default :
				throw new IllegalStateException();
		}
	}

}
