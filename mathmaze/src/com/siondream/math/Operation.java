package com.siondream.math;

public class Operation {
	
	public enum Type {
		Addition,
		Substraction,
		Product,
		Division,
		Mod
	}
	
	private Type type;
	private int value;
	
	public Operation(String typeName, int value) {
		this(getTypeFor(typeName), value);
	}
	
	public Operation(Type type, int value) {
		this.type = type;
		this.value = value;
	}
	
	public Type getType() {
		return type;
	}
	
	public int getValue() {
		return value;
	}
	
	public int run(int other) {
		switch(type) {
		case Addition:
			return other + value;
		case Substraction:
			return other - value;
		case Product:
			return other * value;
		case Division:
			return other / value;
		case Mod:
			return other % value;
		default:
			return other;
		}
	}
	
	private static Type getTypeFor(String name) {
		if (name.equals("+")) {
			return Type.Addition;
		}
		else if (name.equals("-")) {
			return Type.Substraction;
		}
		else if (name.equals("*")) {
			return Type.Product;
		}
		else if (name.equals("/")) {
			return Type.Division;
		}
		else if (name.equals("%")) {
			return Type.Mod;
		}
		else {
			return Type.Addition;
		}
	}
}
