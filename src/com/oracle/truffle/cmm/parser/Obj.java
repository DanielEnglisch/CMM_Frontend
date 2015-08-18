package com.oracle.truffle.cmm.parser;

/*--------------------------------------------------------------------------------
Obj   Object node of the C-- symbol table
===   ===================================
Every declared name in a C-- program is represented by an Obj node holding
information about this object.
--------------------------------------------------------------------------------*/

public class Obj {
	public static final int // object kinds
	CON = 0, VAR = 1, TYPE = 2, PROC = 3;
	public int kind; // CON, VAR, TYPE, PROC
	public String name; // object name
	public Struct type; // object type
	public Obj next; // next local object in this scope (locals)

	public int val; // CON: int or char value
	public float fVal; // CON: float value
	public char cVal; // CON: char value
	public String sVal; // CON: String value
	public double dVal; // CON: Double Value
	public boolean bVal; //CON: Boolean Value

	public int level; // VAR: declaration level

	public int nPars = 0; // PROC: number of formal parameters
	public Obj locals; // PROC: parameters and local objects
	public Node ast; // PROC: AST of this procedure

	public Obj(int kind, String name, Struct type) {
		this.kind = kind;
		this.name = name;
		this.type = type;

	}

	public String getKindName(int kind) {
		switch (kind) {
		case 0:
			return "CON";
		case 1:
			return "VAR";
		case 2:
			return "TYPE";
		case 3:
			return "PROC";
		}

		return "" + kind;
	}

}