package com.siondream.math;

public class Condition {
	
	public enum Type {
		Equals,
		NotEquals,
		Greater,
		Lesser,
		GreaterOrEquals,
		LesserOrEquals,
	}
	
	private Type type;
	private int value;
	
	public Condition(String typeName, int value) {
		this(Condition.getTypeFor(typeName), value);
	}
	
	public Condition(Type type, int value) {
		this.type = type;
		this.value = value;
	}
	
	public Type getType() {
		return type;
	}
	
	public int getValue() {
		return value;
	}
	
	public boolean check(int other) {
		switch(type) {
		case Equals:
			return other == value;
		case NotEquals:
			return other != value;
		case Greater:
			return other > value;
		case Lesser:
			return other < value;
		case GreaterOrEquals:
			return other >= value;
		case LesserOrEquals:
			return other <= value;
		default:
			return false;
		}
	}
	
	private static Type getTypeFor(String name) {
		if (name.equals("=")) {
			return Type.Equals;
		}
		else if (name.equals("!=")) {
			return Type.NotEquals;
		}
		else if (name.equals("&gt;")) {
			return Type.Greater;
		}
		else if (name.equals("&lt;")) {
			return Type.Lesser;
		}
		else if (name.equals("&gt;=")) {
			return Type.GreaterOrEquals;
		}
		else if (name.equals("&lt;=")) {
			return Type.LesserOrEquals;
		}
		else {
			return Type.Equals;
		}
	}
}
