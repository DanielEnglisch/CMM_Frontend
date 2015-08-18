package com.oracle.truffle.cmm.parser;

import java.util.ArrayList;

/*--------------------------------------------------------------------------------
Tab   Symbol table for C--
===   ====================
The symbol table is a stack of scopes
- universe: contains predeclared names
- global scope: contains the globally declared names
- local scope: contains the local names of a procedure

The symbol table has methods for
- opening and closing scopes
- inserting and retrieving named objects
- checking of forward declarations
- utilities for converting strings to constants
--------------------------------------------------------------------------------*/

public class Tab {

	public Scope curScope; // current scope
	public int curLevel; // nesting level of current scope

	public static Struct intType; // predefined types
	public static Struct floatType;
	public static Struct charType;
	public static Struct boolType;
	public static Struct stringType;
	public static Struct doubleType;

	public static Struct noType;
	
	public static Obj noObj; // predefined objects

	private Parser parser; // for error messages
	
	// ------------------ scope management ---------------------

	public void openScope() {
		Scope s = new Scope();
		s.outer = curScope;
		curScope = s;
		curLevel++;
	}

	public void closeScope() {
		curScope = curScope.outer;
		curLevel--;
	}

	// ------------- Object insertion and retrieval --------------

	// Create a new object with the given kind, name and type
	// and insert it into the current scope.

	public Obj insert(int kind, String name, Struct type) {

		int x  = 0;
		
		for(;x==2; )
		{
			x++;
		}
		
		Obj obj = new Obj(kind, name, type);

		if (kind == Obj.VAR) {
			obj.level = curLevel;
		}

		Obj p = curScope.locals, last = null;
		while (p != null) {
			if (p.name.equals(name)) {
				parser.SemErr("Object " + name + " declared twice! \n");
			}
			last = p;
			p = p.next;
		}
		if (last == null)
			curScope.locals = obj;
		else
			last.next = obj;
		return obj;
	}

	// Look up the object with the given name in all open scopes.
	// Report an error if not found.
	public Obj find(String name) {

		for (Scope s = curScope; s != null; s = s.outer) {

			for (Obj o = s.locals; o != null; o = o.next) {

				if (o.name.equals(name)) {
					return o;
				}
			}

		}
		
		if(!name.equals("Main"))
			parser.SemErr("Undeclared Object: " + name);
		
		return noObj;
	}
	
	public ArrayList<Node> getPROCTrees()
	{
		
		ArrayList<Node> nodes = new ArrayList<Node>();
		
		for (Scope s = curScope; s != null; s = s.outer) {

			for (Obj o = s.locals; o != null; o = o.next) {

				if (o.kind == Obj.PROC) {
					nodes.add(o.ast);
				}
			}

		}
		
		return nodes;
	}
	
	public ArrayList<Obj> getVars()
	{
		
		ArrayList<Obj> vars = new ArrayList<Obj>();
		
		for (Scope s = curScope; s != null; s = s.outer) {

			for (Obj o = s.locals; o != null; o = o.next) {

				if (o.kind == Obj.VAR) {
					vars.add(o);
				}
			}

		}
		
		return vars;
	}

	public ArrayList<String> getProcNames()
	{
		
		ArrayList<String> names = new ArrayList<String>();
		
		for (Scope s = curScope; s != null; s = s.outer) {

			for (Obj o = s.locals; o != null; o = o.next) {

				if (o.kind == Obj.PROC) {
					names.add(o.name);
				}
			}

		}
		
		return names;
	}
	
	public ArrayList<Obj> getConsts()
	{
		
		ArrayList<Obj> consts = new ArrayList<Obj>();
		
		for (Scope s = curScope; s != null; s = s.outer) {

			for (Obj o = s.locals; o != null; o = o.next) {

				if (o.kind == Obj.CON) {
					consts.add(o);
				}
			}

		}
		
		return consts;
	}

	// ---------------- methods for dumping the symbol table --------------//

	// Print a type
	public void dumpStruct(Struct type, int indent) {

		switch (type.kind) {
		case Struct.INT:
			System.out.print("Int");
			break;
		case Struct.FLOAT:
			System.out.print("Float");
			break;
		case Struct.DOUBLE:
			System.out.print("Double");
			break;
		case Struct.BOOL:
			System.out.print("Bool");
			break;
		case Struct.CHAR:
			System.out.print("Char");
			break;
		case Struct.STRING:
			System.out.println("String");
			break;
		case Struct.ARR:
			System.out.print("Arr[" + type.elements + "] of ");
			dumpStruct(type.elemType, indent);
			break;
		default:
			System.out.print("None");
			break;
		}
	}

	// Print an object
	public void dumpObj(Obj o, int indent) {

		for (int i = 0; i < indent; i++)
			System.out.print("  ");
		switch (o.kind) {
		case Obj.CON:
			System.out.print("Con " + o.name);
			if (o.type == Tab.floatType)
				System.out.print(" fVal=" + o.fVal);
			else if (o.type == Tab.doubleType)
				System.out.print(" dVal=" + o.dVal);
			else if (o.type == Tab.boolType)
				System.out.print(" bVal=" + o.bVal);
			else if (o.type == Tab.stringType)
				System.out.print(" sVal=" + o.sVal);
			else if (o.type == Tab.charType)
				System.out.print(" cVal=" + o.cVal); // This line was missing!
			else
				System.out.print(" val=" + o.val);
			break;
		case Obj.VAR:
			System.out.print("Var " + o.name + " level=" + o.level);
			break;
		case Obj.TYPE:
			System.out.print("Type " + o.name);
			break;
		case Obj.PROC:
			System.out.println("Proc " + o.name + " nPars=" + o.nPars + " {");
			dumpScope(o.locals, indent + 1);
			System.out.print("}");
			break;
		default:
			System.out.print("None " + o.name);
			break;
		}
		System.out.print(" ");
		dumpStruct(o.type, indent);
		System.out.println();
	}

	// Dump the scope
	public void dumpScope(Obj head, int indent) {
		for (Obj o = head; o != null; o = o.next)
			dumpObj(o, indent);
	}

	// -------------- initialization of the symbol table ------------

	public Tab(Parser parser) {

		this.parser = parser;
		curScope = new Scope();
		curScope.outer = null;
		curLevel = -1;

		// create predeclared types
		intType = new Struct(Struct.INT);
		floatType = new Struct(Struct.FLOAT);
		doubleType = new Struct(Struct.DOUBLE);
		charType = new Struct(Struct.CHAR);
		stringType = new Struct(Struct.STRING);
		boolType = new Struct(Struct.BOOL);
		noType = new Struct(Struct.NONE);
		noObj = new Obj(Obj.VAR, "???", noType);

		// insert predeclared types into universe
		insert(Obj.TYPE, "int", intType);
		insert(Obj.TYPE, "float", floatType);
		insert(Obj.TYPE, "double", doubleType);
		insert(Obj.TYPE, "char", charType);
		insert(Obj.TYPE, "string", stringType);
		insert(Obj.TYPE, "bool", boolType);


	}
}
