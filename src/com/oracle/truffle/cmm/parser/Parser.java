package com.oracle.truffle.cmm.parser;

import java.util.HashMap;


public class Parser {
	public static final int _EOF = 0;
	public static final int _ident = 1;
	public static final int _intCon = 2;
	public static final int _floatCon = 3;
	public static final int _doubleCon = 4;
	public static final int _charCon = 5;
	public static final int _stringCon = 6;
	public static final int _lpar = 7;
	public static final int _rpar = 8;
	public static final int _semicolon = 9;
	public static final int _assign = 10;
	public static final int _eql = 11;
	public static final int _neq = 12;
	public static final int _lss = 13;
	public static final int _leq = 14;
	public static final int _gtr = 15;
	public static final int _geq = 16;
	public static final int _bang = 17;
	public static final int maxT = 44;

	static final boolean _T = true;
	static final boolean _x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	public Tab			tab;                     // symbol table
	public boolean[]	debug = new boolean[10]; // debug switches
	public Obj			currProc = null;
  
  
//---------- LL(1) conflict resolvers ---------------------------//

	// Returns true if a VarDecl comes next in the input
	boolean isVarDecl() { 
		if (la.kind == _ident || la.val.equals("int") || la.val.equals("float") || la.val.equals("char")) {
			Token x = scanner.Peek();
			while (x.kind != _semicolon) {
				if (x.kind == _EOF || x.kind == _lpar || x.kind == _assign) return false;
				x = scanner.Peek();
			}
			return true;
		}
		return false;
	}
	
	// Returns true if the next input is an Expr and not a '(' Condition ')'
	boolean isExpr() { 
		if (la.kind == _bang) return false;
		else if (la.kind == _lpar) {
			Token x = scanner.Peek();
			while (x.kind != _rpar && x.kind != _EOF) {
				if (x.kind == _eql || x.kind == _neq || x.kind == _lss || x.kind == _leq || x.kind == _gtr || x.kind == _geq) return false;
				x = scanner.Peek();
			}
			return x.kind == _rpar;
		} else return true;
	}
	

	// Returns true if the next input is a type cast (requires symbol table)
	// Returns true if the next input is a type cast (requires symbol table)
	boolean isCast() {
		Token x = scanner.Peek();
		if (x.kind != _ident) return false;
		Obj obj = tab.find(x.val);
		return obj.kind == Obj.TYPE;
	}
	
//---------- Method to get the number of errors ------------------------//
	
	public int getErrorCount() {
		return errors.count;
	}
	
	public HashMap<Integer,String> getErrorList()
	{
		return errors.errorList;
	}
	
	
//---------------- conversion of strings to constants ----------------//

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

	public char charVal(String s) {
		return s.replaceAll("\'", "").charAt(0);
	}
	
	public String stringVal(String s) {
		String res = s.substring(1);
		return res.substring(0, res.length() - 1);
	}
	
// ---------------- Casting -------------------------//

	public Node cast(Struct type, Node fac_n) {

		if (type.kind == Struct.INT) {
			if (fac_n.type.kind == Struct.FLOAT) {
				return new Node(Node.F2I, fac_n, null, type);

			} else if (fac_n.type.kind == Struct.DOUBLE) {
				return new Node(Node.D2I, fac_n, null, type);

			} else if (fac_n.type.kind == Struct.CHAR) {
				return new Node(Node.C2I, fac_n, null, type);
			} else
				SemErr("Can't cast " + fac_n.getKindName(fac_n.kind) + " to INT!");

		} else if (type.kind == Struct.CHAR) {
			if (fac_n.type.kind == Struct.INT) {
				return new Node(Node.I2C, fac_n, null, type);
			} else
				SemErr("Can't cast " + fac_n.getKindName(fac_n.kind) + " to CHAR!");
		} else if (type.kind == Struct.FLOAT) {
			if (fac_n.type.kind == Struct.INT) {
				return new Node(Node.I2F, fac_n, null, type);
			} else if (fac_n.type.kind == Struct.CHAR) {
				return new Node(Node.C2F, fac_n, null, type);
			} else
				SemErr("Can't cast " + fac_n.getKindName(fac_n.kind) + " to FLOAT!");
		} else if (type.kind == Struct.DOUBLE) {
			if (fac_n.type.kind == Struct.INT) {
				return new Node(Node.I2D, fac_n, null, type);
			} else if (fac_n.type.kind == Struct.FLOAT) {
				return new Node(Node.F2D, fac_n, null, type);
			} else if (fac_n.type.kind == Struct.CHAR) {
				return new Node(Node.C2D, fac_n, null, type);
			} else
				SemErr("Can't cast " + fac_n.getKindName(fac_n.kind) + " to DOUBLE!");
		} else if (type.kind == Struct.STRING) {

			if (fac_n.type.kind == Struct.ARR && fac_n.type.elemType.kind == Struct.CHAR) {
				// Cast Char to String
				return new Node(Node.A2S, fac_n, null, type);
			} else if (fac_n.type.kind == Struct.CHAR) {
				return new Node(Node.C2S, fac_n, null, type);
			} else
				SemErr("Array must be of type CHAR to be casted to STRING!");

		} else
			SemErr("Can't cast " + fac_n.getKindName(fac_n.kind) + " to " + type.getKindName(type.kind));

		return new Node(Node.UKN, null, null, null);

	}
	
	//----------- Zuweisungskompatiblität --------- //
	public boolean isCompatible(int des, int exp)
	{
		//INT = X
		if(des == Struct.INT && exp == Struct.INT)
			return true;
		else if(des == Struct.INT && exp == Struct.FLOAT)
			return true;
		else if(des == Struct.INT && exp == Struct.DOUBLE)
			return true;
		else if(des == Struct.INT && exp == Struct.CHAR)
			return true;
			
		//FLOAT = X
		else if(des == Struct.FLOAT && exp == Struct.FLOAT)
			return true;
		else if(des == Struct.FLOAT && exp == Struct.INT)
			return true;
			else if(des == Struct.FLOAT && exp == Struct.CHAR)
			return true;
			
		//DOUBLE = X
		else if(des == Struct.DOUBLE && exp == Struct.DOUBLE)
			return true;
		else if(des == Struct.DOUBLE && exp == Struct.INT)
			return true;
		else if(des == Struct.DOUBLE && exp == Struct.FLOAT)
			return true;
		else if(des == Struct.DOUBLE && exp == Struct.CHAR)
			return true;
			
		//CHAR = X
		else if(des == Struct.CHAR && exp == Struct.CHAR)
			return true;
		else if(des == Struct.CHAR && exp == Struct.INT)
			return true;
		
		//STRING = X
		else if(des == Struct.STRING && exp == Struct.STRING)
			return true;
		else if(des == Struct.STRING && exp == Struct.CHAR)
			return true;
		
		//BOOL = X
		else if(des == Struct.BOOL && exp == Struct.BOOL)
			return true;
		
		return false;
	}
	
/*-------------------------------------------------------------------------*/



	public Parser(Scanner scanner) {
		this.scanner = scanner;
		errors = new Errors();
	}

	void SynErr (int n) {
		if (errDist >= minErrDist) errors.SynErr(la.line, la.col, n);
		errDist = 0;
	}

	public void SemErr (String msg) {
		if (errDist >= minErrDist) errors.SemErr(t.line, t.col, msg);
		errDist = 0;
	}
	
	void Get () {
		for (;;) {
			t = la;
			la = scanner.Scan();
			if (la.kind <= maxT) {
				++errDist;
				break;
			}

			la = t;
		}
	}
	
	void Expect (int n) {
		if (la.kind==n) Get(); else { SynErr(n); }
	}
	
	boolean StartOf (int s) {
		return set[s][la.kind];
	}
	
	void ExpectWeak (int n, int follow) {
		if (la.kind == n) Get();
		else {
			SynErr(n);
			while (!StartOf(follow)) Get();
		}
	}
	
	boolean WeakSeparator (int n, int syFol, int repFol) {
		int kind = la.kind;
		if (kind == n) { Get(); return true; }
		else if (StartOf(repFol)) return false;
		else {
			SynErr(n);
			while (!(set[syFol][kind] || set[repFol][kind] || set[0][kind])) {
				Get();
				kind = la.kind;
			}
			return StartOf(syFol);
		}
	}
	
	public int Errors() {
		return errors.count;
	}
	
	void CMM() {
		tab = new Tab(this);
		tab.openScope();   
		while (la.kind == 1 || la.kind == 18 || la.kind == 20) {
			if (la.kind == 18) {
				ConstDecl();
			} else if (isVarDecl()) {
				VarDecl();
			} else {
				NodeList proc_nl = ProcDecl();
				currProc.ast = new Node(Node.STATSEQ,proc_nl.get(), null, null ); 
				currProc.ast.line = proc_nl.head.line;
				if (debug[1]) Node.dump(currProc.ast,0);
				
			}
		}
		if (debug[0]) tab.dumpScope(tab.curScope.locals, 0); 
		if(tab.find("Main") == Tab.noObj)
		{SemErr("Main method not found!");}
		
	}

	void ConstDecl() {
		Struct type; 
		Expect(18);
		type = Type();
		Expect(1);
		Obj obj = tab.insert(Obj.CON, t.val, type); 
		Expect(10);
		switch (la.kind) {
		case 2: {
			Get();
			obj.val = intVal(t.val);
			if (!isCompatible(type.kind, Struct.INT))
			SemErr("Incompatible type of initialization value!");
			
			break;
		}
		case 3: {
			Get();
			obj.fVal = floatVal(t.val);
			if (!isCompatible(type.kind, Struct.FLOAT))
			SemErr("Incompatible type of initialization value!");
			
			break;
		}
		case 4: {
			Get();
			obj.dVal = doubleVal(t.val);
			if (!isCompatible(type.kind, Struct.DOUBLE))
			SemErr("Incompatible type of initialization value!");
			
			break;
		}
		case 5: {
			Get();
			obj.cVal = charVal(t.val);
			if (!isCompatible(type.kind, Struct.CHAR))
			SemErr("Incompatible type of initialization value!");
			
			break;
		}
		case 6: {
			Get();
			obj.sVal = stringVal(t.val);
			if (!isCompatible(type.kind, Struct.STRING))
			SemErr("Incompatible type of initialization value!");
			
			break;
		}
		case 34: case 35: {
			boolean boolVal = BoolExp();
			obj.bVal = boolVal;
			if (!isCompatible(type.kind, Struct.BOOL))
			SemErr("Incompatible type of initialization value!");
			
			break;
		}
		default: SynErr(45); break;
		}
		Expect(9);
	}

	void VarDecl() {
		Struct type; 
		type = Type();
		Expect(1);
		tab.insert(Obj.VAR, t.val, type); 
		while (la.kind == 19) {
			Get();
			Expect(1);
			tab.insert(Obj.VAR, t.val, type); 
		}
		Expect(9);
	}

	NodeList  ProcDecl() {
		NodeList  proc_nl;
		Struct type = null; int num; Node stm_n; proc_nl = new NodeList(); 
		if (la.kind == 1) {
			type = Type();
			if(!type.isPrimitive())
			{
			if(type.kind != Struct.STRING)
			{
			SemErr("Method return type must be a primitive or a string!");
			}
			}
			
		} else if (la.kind == 20) {
			Get();
			type = Tab.noType; 
		} else SynErr(46);
		Expect(1);
		currProc = tab.insert(Obj.PROC, t.val, type);
		tab.openScope(); 
		
		Expect(7);
		if (la.kind == 1) {
			num = FormPars();
			currProc.nPars = num; 
		}
		Expect(8);
		Expect(21);
		while (StartOf(1)) {
			if (la.kind == 18) {
				ConstDecl();
			} else if (isVarDecl()) {
				VarDecl();
			} else {
				stm_n = Statement();
				proc_nl.add(stm_n); 
			}
		}
		Expect(22);
		Node trap = new Node(Node.TRAP, null,null,Tab.noType);
		trap.line = t.line;
		proc_nl.add(trap);
		
		if(proc_nl.tail == null || proc_nl.head == null)
		{SemErr("Invalid statement (maybe 'return' needs a value after it!)");}
		else if(proc_nl.hasReturnStatement == false && currProc.type != Tab.noType)
		{SemErr("Missing return statement!");}
			currProc.locals = tab.curScope.locals; tab.closeScope();
		
		return proc_nl;
	}

	Struct  Type() {
		Struct  type;
		Expect(1);
		Obj obj = tab.find(t.val);
		if(obj.kind != Obj.TYPE)
		{SemErr("Unknown type " + t.val + "!");}
		type = obj.type; 
		
		if (la.kind == 23) {
			Get();
			Expect(2);
			int len = intVal(t.val); 
			if(len <= 0) {SemErr("Lenght of array can't be less than 1!");}
			type = new Struct(Struct.ARR, len, type);
			
			Expect(24);
		}
		return type;
	}

	boolean  BoolExp() {
		boolean  boolVal;
		boolVal = false; 
		if (la.kind == 34) {
			Get();
			boolVal = true; 
		} else if (la.kind == 35) {
			Get();
			boolVal = false; 
		} else SynErr(47);
		return boolVal;
	}

	int  FormPars() {
		int  num;
		Struct type; 
		type = Type();
		Expect(1);
		num = 1; tab.insert(Obj.VAR, t.val, type);
		if(!type.isPrimitive()&&type.kind != Struct.STRING)
		{SemErr("Parameter must be a primitive or a String!");}
		
		while (la.kind == 19) {
			Get();
			type = Type();
			Expect(1);
			num++; tab.insert(Obj.VAR, t.val, type);
			if(!type.isPrimitive()&&type.kind != Struct.STRING)
			{SemErr("Parameter must be a primitive or a String!");}
			
		}
		return num;
	}

	Node  Statement() {
		Node  stm_n;
		NodeList act_nl; 
		int line = -1; Node des_n, exp_n, con_n, stm_n1, stm_n2, stm_n3, stm_n4; stm_n = null;
		
		switch (la.kind) {
		case 1: {
			des_n = Designator();
			if (la.kind == 10) {
				Get();
				line = t.line; 
				exp_n = Expr();
				if(des_n.kind == Node.INDEX)
				{
				if(des_n.left.type.elemType.kind == Struct.STRING)
				{
				SemErr("Designator array element can't be a string!");
				}
				}
				else
				{
				if(des_n.obj.kind != Obj.VAR)
				{
				SemErr("Designator must be a variable!");
				}
				}
																		if(exp_n.type.kind != Struct.STRING && !exp_n.type.isPrimitive())
				{
				SemErr("Expression must be primitive or a string!");
				}
				else
				if(!isCompatible(des_n.type.kind, exp_n.type.kind))
				{
				SemErr("Designator and expression aren't compatible!");
				}
				
				
				stm_n = new Node(Node.ASSIGN, des_n, exp_n, Tab.noType);
				stm_n.line = line;
				
			} else if (la.kind == 7) {
				line = la.line; 
				act_nl = ActPars();
				if(des_n.type != Tab.noType) SemErr("Designator must be void!");
				stm_n = new Node(Node.CALL, act_nl.get(), null, Tab.noType);
				stm_n.line = line;
				stm_n.obj = des_n.obj;  
				
			} else SynErr(48);
			Expect(9);
			break;
		}
		case 25: {
			Get();
			line = t.line; 
			Expect(7);
			con_n = Condition();
			Expect(8);
			stm_n1 = Statement();
			stm_n = new Node(Node.IF, con_n, stm_n1, Tab.noType);
			stm_n.line = line;
			
			if (la.kind == 26) {
				Get();
				stm_n2 = Statement();
				stm_n = new Node(Node.IFELSE, stm_n, stm_n2, Tab.noType);
				stm_n.line = line;
				
			}
			break;
		}
		case 27: {
			Get();
			line = t.line; 
			Expect(7);
			con_n = Condition();
			Expect(8);
			stm_n3 = Statement();
			stm_n = new Node(Node.WHILE , con_n, stm_n3, Tab.noType);
			stm_n.line = line;
			
			break;
		}
		case 28: {
			Get();
			line = t.line; 
			Expect(7);
			Node ass_n = Statement();
			con_n = Condition();
			Expect(9);
			Node op_n = Statement();
			if(ass_n.kind != Node.ASSIGN || ass_n.kind == Node.STATSEQ){SemErr("Invalid for loop! Check Initializer!");}
			if(op_n.kind == Node.STATSEQ){SemErr("Operator can't be a block!");}
			Node head = new Node(Node.FORHEAD, ass_n, con_n, Tab.noType);
			head.line = line;
			
			Expect(8);
			Node for_stat_n = Statement();
			Node f = for_stat_n;
			
			while(true)
			{
			if(f.next == null)
			{
			f.next = op_n;
			break;
			}
			
			f = f.next;
			
			
			}
																		
																	
			stm_n = new Node(Node.FOR , head, for_stat_n, Tab.noType);
			stm_n.line = line;
			
			break;
		}
		case 29: {
			Get();
			line = t.line; 
			Expect(7);
			exp_n = Expr();
			stm_n = new Node(Node.PRINT, exp_n, null, Tab.noType);
			stm_n.line = line;
			
			Expect(8);
			Expect(9);
			break;
		}
		case 30: {
			Get();
			line = t.line; 
			Expect(7);
			exp_n = Expr();
			stm_n = new Node(Node.PRINTLN, exp_n, null, Tab.noType);
			stm_n.line = line;
			
			Expect(8);
			Expect(9);
			break;
		}
		case 21: {
			Get();
			NodeList block = new NodeList(); line = t.line; 
			while (StartOf(2)) {
				stm_n4 = Statement();
				block.add(stm_n4); 
			}
			if(block.entries != 0)line = block.head.line; 
			Expect(22);
			stm_n = new Node(Node.STATSEQ, block.get(), null, Tab.noType);
			stm_n.line = line;
			
			break;
		}
		case 31: {
			Get();
			line = t.line; 
			exp_n = Expr();
			Expect(9);
			if(currProc.type.kind == Struct.NONE)
			{
			SemErr("Only non-void functions can return values!");
			
			}if(exp_n == null)
			{
			SemErr("'return' needs a value!");
			}
			else
			
			if(exp_n.type.kind != currProc.type.kind)
			{
			SemErr("Return value must be of the same type as the function!");
			}else
			
			stm_n = new Node(Node.RETURN, exp_n, null, Tab.noType); 
			stm_n.line = line;
			
			break;
		}
		case 9: {
			Get();
			break;
		}
		default: SynErr(49); break;
		}
		return stm_n;
	}

	Node  Designator() {
		Node  des_n;
		Node exp_n; 
		Expect(1);
		Obj o = tab.find(t.val);
		des_n = new Node(o); 
		if (la.kind == 23) {
			Get();
			exp_n = Expr();
			Struct type = null;
			if(o.type.kind == Struct.STRING) {type =  Tab.charType;}
			else if (o.type.kind == Struct.ARR) {type = o.type.elemType;}
			else {SemErr("Invalid Designator Type (must be string or array)!");}
			if(exp_n.type.kind != Struct.INT) {SemErr("Index must be of type INT!");}
			des_n = new Node(Node.INDEX, des_n, exp_n, type ); 
			Expect(24);
		} else if (StartOf(3)) {
		} else SynErr(50);
		return des_n;
	}

	Node  Expr() {
		Node  exp_n;
		Node trm_n1, trm_n2; int operator; 
		trm_n1 = Term();
		exp_n = trm_n1; 
		while (la.kind == 39 || la.kind == 40) {
			operator = Addop();
			trm_n2 = Term();
			if(	trm_n1.type.isPrimitive() == true && trm_n2.type.isPrimitive() == true)
			{	
				exp_n = new Node(operator, trm_n1, trm_n2, trm_n1.type);
				trm_n1 = exp_n;
					
			}else
			if(trm_n1.type.kind == Struct.STRING && trm_n2.type.kind == Struct.STRING)
			{
				exp_n = new Node(operator, trm_n1, trm_n2, trm_n1.type);
				trm_n1 = exp_n;
			}else
				SemErr("Both terms must be a primitive or strings!");
			
			
			
			
		}
		return exp_n;
	}

	NodeList  ActPars() {
		NodeList  act_nl;
		Node exp_n; act_nl = new NodeList();
		Expect(7);
		if (StartOf(4)) {
			exp_n = Expr();
			act_nl.add(exp_n); 
			while (la.kind == 19) {
				Get();
				exp_n = Expr();
				act_nl.add(exp_n); 
			}
		}
		Expect(8);
		return act_nl;
	}

	Node  Condition() {
		Node  con_n;
		Node conTerm_n1, conTerm_n2; con_n = null; 
		conTerm_n1 = CondTerm();
		con_n = conTerm_n1; 
		while (la.kind == 32) {
			Get();
			conTerm_n2 = CondTerm();
			con_n = new Node(Node.OR, conTerm_n1, conTerm_n2, Tab.boolType); 
			conTerm_n1 = con_n;
			
		}
		return con_n;
	}

	Node  CondTerm() {
		Node  conTerm_n;
		Node conFac_n1, conFac_n2; conTerm_n = null; 
		conFac_n1 = CondFact();
		conTerm_n = conFac_n1; 
		while (la.kind == 33) {
			Get();
			conFac_n2 = CondFact();
			conTerm_n = new Node(Node.AND, conFac_n1, conFac_n2, Tab.boolType); 
			conFac_n1 = conTerm_n;
			
		}
		return conTerm_n;
	}

	Node  CondFact() {
		Node  conFac_n;
		Node exp_n1, exp_n2, con_n; int operator; conFac_n = null;
		if (isExpr()) {
			exp_n1 = Expr();
			operator = Relop();
			exp_n2 = Expr();
			if(exp_n1.type.isPrimitive() == false && exp_n2.type.isPrimitive() == false )
			{
			if(exp_n1.type.kind != Struct.STRING && exp_n2.type.kind != Struct.STRING)
			{SemErr("Both terms must be a primitive or string!");}
			}
			
			conFac_n = new Node(operator, exp_n1, exp_n2, Tab.boolType);
			
		} else if (la.kind == 17) {
			Get();
			Expect(7);
			con_n = Condition();
			conFac_n = new Node(Node.NOT, con_n, null, Tab.boolType ); 
			Expect(8);
		} else if (la.kind == 7) {
			Get();
			con_n = Condition();
			conFac_n = con_n; 
			Expect(8);
		} else SynErr(51);
		return conFac_n;
	}

	int  Relop() {
		int  operator;
		operator = Node.UKN; 
		switch (la.kind) {
		case 11: {
			Get();
			operator = Node.EQL; 
			break;
		}
		case 12: {
			Get();
			operator = Node.NEQ; 
			break;
		}
		case 15: {
			Get();
			operator = Node.GTR; 
			break;
		}
		case 16: {
			Get();
			operator = Node.GEQ; 
			break;
		}
		case 13: {
			Get();
			operator = Node.LSS; 
			break;
		}
		case 14: {
			Get();
			operator = Node.LEQ; 
			break;
		}
		default: SynErr(52); break;
		}
		return operator;
	}

	Node  Term() {
		Node  trm_n;
		Node fac_n1, fac_n2; int operator; 
		fac_n1 = Factor();
		trm_n = fac_n1; 
		while (la.kind == 41 || la.kind == 42 || la.kind == 43) {
			operator = Mulop();
			fac_n2 = Factor();
			if((!fac_n1.type.isPrimitive() && !fac_n2.type.isPrimitive()))
			if((fac_n1.type.kind != Struct.STRING && fac_n2.type.kind != Struct.STRING))
			{SemErr("Both factors must be a primitive or strings!");}
			
			trm_n = new Node(operator, fac_n1, fac_n2, fac_n1.type);
			fac_n1 = trm_n;
			
		}
		return trm_n;
	}

	int  Addop() {
		int  operator;
		operator = Node.UKN; 
		if (la.kind == 40) {
			Get();
			operator = Node.PLUS; 
		} else if (la.kind == 39) {
			Get();
			operator = Node.MINUS; 
		} else SynErr(53);
		return operator;
	}

	Node  Factor() {
		Node  fac_n;
		Struct type; Node des_n,exp_n; NodeList act_nl; fac_n = null; 
		if (la.kind == 1) {
			des_n = Designator();
			fac_n = des_n; int line = t.line; 
			if (la.kind == 7) {
				act_nl = ActPars();
				if(des_n.kind != Node.IDENT){SemErr("Designator must have no structure!");}
				else
				{
				if(des_n.obj.kind != Obj.PROC){SemErr("Designator must be of kind PROC but " + des_n.obj.name +" is" + des_n.obj.getKindName(des_n.obj.kind) + "!");}
				else if(des_n.obj.type.kind == Struct.NONE){SemErr("Designator can't be of type void!");}
					else 
						
						
							if(act_nl.entries != des_n.obj.nPars)
							{
								SemErr("Parameter count mismatch!");
							}else
							{
								fac_n = new Node(Node.CALL, act_nl.get(), null, des_n.type);
								fac_n.line = line;
							}
						
																
				fac_n.obj = des_n.obj; //To know the method name
				
				}
			}
		} else if (la.kind == 2) {
			Get();
			fac_n = new Node(intVal(t.val)); 
		} else if (la.kind == 3) {
			Get();
			fac_n = new Node(floatVal(t.val)); 
		} else if (la.kind == 4) {
			Get();
			fac_n = new Node(doubleVal(t.val)); 
		} else if (la.kind == 34 || la.kind == 35) {
			boolean boolVal = BoolExp();
			fac_n = new Node(boolVal); 
		} else if (la.kind == 5) {
			Get();
			fac_n = new Node(charVal(t.val)); 
		} else if (la.kind == 6) {
			Get();
			fac_n = new Node(stringVal(t.val)); 
		} else if (la.kind == 36) {
			Get();
			Expect(7);
			Expect(8);
			fac_n = new Node(Node.READ, null, null, Tab.charType); 
		} else if (la.kind == 37) {
			Get();
			Expect(7);
			Expect(8);
			fac_n = new Node(Node.READLN, null, null, Tab.stringType); 
		} else if (la.kind == 38) {
			Get();
			Expect(7);
			exp_n = Expr();
			Expect(8);
			fac_n = new Node(Node.LENGHT, exp_n, null, Tab.intType); 
			
			if(exp_n.type.kind != Struct.STRING)
			{
			SemErr("lenght() parameter must be a string!");
			}
			
		} else if (la.kind == 39) {
			Get();
			fac_n = Factor();
			if(fac_n.type.kind != Struct.INT && fac_n.type.kind != Struct.FLOAT)
			{SemErr("Factor must be INT or FLOAT!");}
			fac_n = new Node(Node.MINUS, fac_n, null, fac_n.type); 
		} else if (isCast()) {
			Expect(7);
			type = Type();
			Expect(8);
			fac_n = Factor();
			fac_n = cast(type, fac_n);
			
		} else if (la.kind == 7) {
			Get();
			exp_n = Expr();
			Expect(8);
			fac_n = exp_n; 
		} else SynErr(54);
		return fac_n;
	}

	int  Mulop() {
		int  operator;
		operator = Node.UKN; 
		if (la.kind == 41) {
			Get();
			operator = Node.TIMES; 
		} else if (la.kind == 42) {
			Get();
			operator = Node.DIV; 
		} else if (la.kind == 43) {
			Get();
			operator = Node.REM; 
		} else SynErr(55);
		return operator;
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		CMM();
		Expect(0);

	}

	private static final boolean[][] set = {
		{_T,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x},
		{_x,_T,_x,_x, _x,_x,_x,_x, _x,_T,_x,_x, _x,_x,_x,_x, _x,_x,_T,_x, _x,_T,_x,_x, _x,_T,_x,_T, _T,_T,_T,_T, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x},
		{_x,_T,_x,_x, _x,_x,_x,_x, _x,_T,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_T,_x,_x, _x,_T,_x,_T, _T,_T,_T,_T, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x},
		{_x,_x,_x,_x, _x,_x,_x,_T, _T,_T,_T,_T, _T,_T,_T,_T, _T,_x,_x,_T, _x,_x,_x,_x, _T,_x,_x,_x, _x,_x,_x,_x, _T,_T,_x,_x, _x,_x,_x,_T, _T,_T,_T,_T, _x,_x},
		{_x,_T,_T,_T, _T,_T,_T,_T, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_T,_T, _T,_T,_T,_T, _x,_x,_x,_x, _x,_x}

	};
} // end Parser


class Errors {
	public int count = 0;                                    // number of errors detected
	public java.io.PrintStream errorStream = System.out;     // error messages go to this stream
	public String errMsgFormat = "-- line {0} col {1}: {2}"; // 0=line, 1=column, 2=text
	public HashMap<Integer,String> errorList = new HashMap<Integer,String>();
	
	public HashMap<Integer,String> getErrorList()
	{
		return errorList;
	}
	
	
	protected void printMsg(int line, int column, String msg) {
		StringBuffer b = new StringBuffer(errMsgFormat);
		int pos = b.indexOf("{0}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, line); }
		pos = b.indexOf("{1}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, column); }
		pos = b.indexOf("{2}");
		if (pos >= 0) b.replace(pos, pos+3, msg);
		errorStream.println(b.toString());
		errorList.put(line,b.toString()); //x0
	}
	
	public void SynErr (int line, int col, int n) {
		String s;
		switch (n) {
			case 0: s = "EOF expected"; break;
			case 1: s = "ident expected"; break;
			case 2: s = "intCon expected"; break;
			case 3: s = "floatCon expected"; break;
			case 4: s = "doubleCon expected"; break;
			case 5: s = "charCon expected"; break;
			case 6: s = "stringCon expected"; break;
			case 7: s = "lpar expected"; break;
			case 8: s = "rpar expected"; break;
			case 9: s = "semicolon expected"; break;
			case 10: s = "assign expected"; break;
			case 11: s = "eql expected"; break;
			case 12: s = "neq expected"; break;
			case 13: s = "lss expected"; break;
			case 14: s = "leq expected"; break;
			case 15: s = "gtr expected"; break;
			case 16: s = "geq expected"; break;
			case 17: s = "bang expected"; break;
			case 18: s = "\"const\" expected"; break;
			case 19: s = "\",\" expected"; break;
			case 20: s = "\"void\" expected"; break;
			case 21: s = "\"{\" expected"; break;
			case 22: s = "\"}\" expected"; break;
			case 23: s = "\"[\" expected"; break;
			case 24: s = "\"]\" expected"; break;
			case 25: s = "\"if\" expected"; break;
			case 26: s = "\"else\" expected"; break;
			case 27: s = "\"while\" expected"; break;
			case 28: s = "\"for\" expected"; break;
			case 29: s = "\"print\" expected"; break;
			case 30: s = "\"printLine\" expected"; break;
			case 31: s = "\"return\" expected"; break;
			case 32: s = "\"||\" expected"; break;
			case 33: s = "\"&&\" expected"; break;
			case 34: s = "\"true\" expected"; break;
			case 35: s = "\"false\" expected"; break;
			case 36: s = "\"read\" expected"; break;
			case 37: s = "\"readLine\" expected"; break;
			case 38: s = "\"lenght\" expected"; break;
			case 39: s = "\"-\" expected"; break;
			case 40: s = "\"+\" expected"; break;
			case 41: s = "\"*\" expected"; break;
			case 42: s = "\"/\" expected"; break;
			case 43: s = "\"%\" expected"; break;
			case 44: s = "??? expected"; break;
			case 45: s = "invalid ConstDecl"; break;
			case 46: s = "invalid ProcDecl"; break;
			case 47: s = "invalid BoolExp"; break;
			case 48: s = "invalid Statement"; break;
			case 49: s = "invalid Statement"; break;
			case 50: s = "invalid Designator"; break;
			case 51: s = "invalid CondFact"; break;
			case 52: s = "invalid Relop"; break;
			case 53: s = "invalid Addop"; break;
			case 54: s = "invalid Factor"; break;
			case 55: s = "invalid Mulop"; break;
			default: s = "error " + n; break;
		}
		printMsg(line, col, s);
		count++;
	}

	public void SemErr (int line, int col, String s) {	
		printMsg(line, col, s);
		count++;
	}
	
	public void SemErr (String s) {
		errorStream.println(s);
		count++;
	}
	
	public void Warning (int line, int col, String s) {	
		printMsg(line, col, s);
	}
	
	public void Warning (String s) {
		errorStream.println(s);
	}
} // Errors


class FatalError extends RuntimeException {
	public static final long serialVersionUID = 1L;
	public FatalError(String s) { super(s); }
}
