package com.oracle.truffle.cmm.parser;

/*--------------------------------------------------------------------------------
Node   Node of the abstract syntax tree (AST) of a C-- program
====   =======================================================
Every node has a left and a right child. Some nodes (such as statements or parameters)
can also be linked by a "next" pointer.
Nodes representing a statement have a line number, whereas nodes representing
a part of an expression have a type.
--------------------------------------------------------------------------------*/

public final class Node {

	public static final int // node kinds -> 49
	// ----------- statements
	STATSEQ = 0, // statement sequence
			ASSIGN = 1, // assignment
			CALL = 2, // procedure or function call
			IF = 3, // if statement without else branch
			IFELSE = 4, // if statement with else branch
			WHILE = 5, // while statement
			FORHEAD = 40, FOR = 41, RETURN = 6, BREAK = 49, // return statement
			TRAP = 7, // trap if a function reaches its end without a return
			// ------------ leaf expressions
			IDENT = 8, // identifier
			INTCON = 9, // int constant
			FLOATCON = 10, // float constant
			DOUBLECON = 37, // double constant
			CHARCON = 11, // char constant
			STRINGCON = 12, BOOLCON = 39,
			// ------------ designators
			INDEX = 13, // array element (a[i])
			// ------------ expressions
			PLUS = 14, // +
			MINUS = 15, // -
			TIMES = 16, // *
			DIV = 17, // /
			REM = 18, // %
			// Cast:
	F2I = 20, D2I = 38, C2I = 22, S2I = 50,

	I2F = 19, C2F = 42, S2F = 51,

	I2D = 43, F2D = 44, C2D = 45 , S2D = 52,

	I2C = 21, S2C = 53,

	A2S = 23, C2S = 46, I2S = 54,F2S = 55,D2S = 56,

	// ------------ conditionals
			EQL = 24, // ==
			NEQ = 25, // !=
			LSS = 26, // <
			LEQ = 27, // <=
			GTR = 28, // >
			GEQ = 29, // >=
			NOT = 30, // !
			OR = 31, // ||
			AND = 32, // &&
			READ = 33, // read();
			READLN = 47, UKN = 34, // Unknown
			PRINT = 35, // print();
			PRINTLN = 48, LENGTH = 36; // lenght(string);
	
	
	public static String[] name = { "STATSEQ", "ASSIGN", "CALL", "IF", "IFELSE", "WHILE", "RETURN", "TRAP", "IDENT",
			"INTCON", "FLOATCON", "CHARCON", "STRINGCON", "INDEX", "PLUS", "MINUS", "MUL", "DIV", "REM", "I2F", "F2I",
			"I2C", "C2I", "A2S", "EQL", "NEQ", "LSS", "LEQ", "GTR", "GEQ", "NOT", "OR", "AND", "READ", "?Unknown?",
			"PRINT", "LENGTH", "DOUBLE", "D2I", "BOOLCON", "FORHEAD", "FOR", "C2F", "I2D", "F2D", "C2D", "C2S",
			"READLN", "PRINTLN", "BREAK", "S2I" , "S2F", "S2D", "S2C", "I2S", "F2S", "D2S"};

	public String getKindName(int kindID) {
		switch (kindID) {
		case 9:
			return "INT";
		case 10:
			return "FLOAT";
		case 11:
			return "CHAR";
		case 12:
			return "STRING";
		case 37:
			return "DOUBLE";
		case 39:
			return "BOOL";
		}

		return "ID:" + kindID;
	}

	public int kind; // STATSEQ, ASSIGN, ...
	public Struct type; // only used in expressions
	public int line; // only used in statement nodes

	public Node left; // left son
	public Node right; // right son
	public Node next; // for linking statements, parameters, ...

	public Obj obj; // object node of an IDENT
	public int val; // value of an INTCON or CHARCON
	public float fVal; // value of a FLOATCON
	public String sVal; // value of a STRINGCON
	public double dVal;
	public boolean bVal;

	// for expression nodes
	public Node(int kind, Node left, Node right, Struct type) {
		this.kind = kind;
		this.left = left;
		this.right = right;
		this.type = type;
	}

	// for statement nodes
	public Node(int kind, Node left, Node right, int line) {
		this(kind, left, right, null);
		this.line = line;
	}

	// --- for leaf nodes----

	public Node(Obj obj) {
		this.kind = IDENT;
		this.type = obj.type;
		this.obj = obj;
	}

	public Node(int val) {
		this.kind = INTCON;
		this.type = Tab.intType;
		this.val = val;
	}

	public Node(float fValue) {
		this.kind = FLOATCON;
		this.type = Tab.floatType;
		this.fVal = fValue;
	}

	public Node(double dValue) {
		this.kind = DOUBLECON;
		this.type = Tab.doubleType;
		this.dVal = dValue;
	}

	public Node(boolean bValue) {
		this.kind = BOOLCON;
		this.type = Tab.boolType;
		this.bVal = bValue;
	}

	public Node(char ch) {
		this.kind = CHARCON;
		this.type = Tab.charType;
		this.val = ch;
	}

	public Node(String s) {
		this.kind = STRINGCON;
		this.type = Tab.stringType;
		this.sVal = s;
	}

	public Node(int kind, Struct type, int val) {
		this.kind = kind;
		this.type = type;
		this.val = val;
	}

	// ---------------- conversion of strings to constants ----------------//

	// Convert a digit string into an int
	public int intVal(String s) {
		return Integer.parseInt(s);
	}

	// Convert a string representation of a float constant into a float value
	public float floatVal(String s) {
		return Float.parseFloat(s);
	}

	public double doubleVal(String s) {
		return Double.parseDouble(s);
	}

	public boolean boolVal(String s) {
		return Boolean.parseBoolean(s);
	}

	public char charVal(String s) {
		return s.replaceAll("\'", "").charAt(0);
	}

	public String stringVal(String s) {
		String res = s.substring(1);
		return res.substring(0, res.length() - 1);
	}

	/*-------------------------------------------------------------------------*/

	// ----------------------- for dumping ASTs
	// -----------------------------------



	private static String[] typ = { "None", "Int", "Float", "Char", "Bool", "String", "Arr", "Double" };

	public static void dump(Node x, int indent) {
		for (int i = 0; i < indent; i++)
			System.out.print("  ");
		if (x == null)
			System.out.println("-null-");
		else {
			System.out.print(name[x.kind]);
			if (x.kind == IDENT)
				System.out.print(" " + x.obj.name + " level=" + x.obj.level);
			else if (x.kind == INTCON)
				System.out.print(" " + x.val);
			else if (x.kind == FLOATCON)
				System.out.print(" " + x.fVal);
			else if (x.kind == DOUBLECON)
				System.out.print(" " + x.dVal);
			else if (x.kind == BOOLCON)
				System.out.print(" " + x.bVal);
			else if (x.kind == CHARCON)
				System.out.print(" \'" + (char) x.val + "\'");
			else if (x.kind == STRINGCON)
				System.out.print(" \"" + x.sVal + "\"");
			else if (x.kind == CALL && x.obj != null)
				System.out.print(" " + x.obj.name);
			if (x.type != null)
				System.out.print(" type=" + typ[x.type.kind]);
			//if (x.kind >= STATSEQ && x.kind <= TRAP)
				System.out.print(" line=" + x.line);
			System.out.println();
			if (x.left != null || x.right != null) {
				dump(x.left, indent + 1);
				dump(x.right, indent + 1);
			}
			if (x.next != null) {
				for (int i = 0; i < indent; i++)
					System.out.print("  ");
				System.out.println("--- next ---");
				dump(x.next, indent);
			}
		}
	}

}
