package com.oracle.truffle.cmm.parser;

/*--------------------------------------------------------------------------------
Struct   Structure of a C-- type
======   =======================
A Struct holds information about a C-- type. There are 3 primitive types
(int, float, char) and 2 structured types (arrays, structures). The type Bool
results from compare operations, but there is no boolean type in C--.
--------------------------------------------------------------------------------*/

public class Struct {
	public static final int // structure kinds
	NONE = 0, INT = 1, FLOAT = 2, CHAR = 3, BOOL = 4, STRING = 5, ARR = 6, DOUBLE = 7;

	public int kind; // NONE, INT, FLOAT, CHAR, ARR, STRING

	public int elements; // ARR: number of elements
	public Struct elemType; // ARR: element type

	public Struct(int kind) {
		this.kind = kind;
	}

	public Struct(int kind, int elements, Struct elemType) {
		this.kind = kind;
		this.elements = elements;
		this.elemType = elemType;
	}

	// Checks whether this type is a primitive type
	public boolean isPrimitive() {
		return kind == INT || kind == FLOAT || kind == CHAR || kind == DOUBLE || kind == BOOL;
	}

	public String getKindName(int kindID) {
		switch (kindID) {
		case 0:
			return "NONE";
		case 1:
			return "INT";
		case 2:
			return "FLOAT";
		case 3:
			return "CHAR";
		case 4:
			return "BOOL";
		case 5:
			return "STRING";
		case 6:
			return "ARRAY";
		case 7:
			return "DOUBLE";
			
		}

		return "ID:" + kindID;
	}

}