package com.oracle.truffle.cmm.parser;

/*--------------------------------------------------------------------------------
NodeList   Builds lists of AST nodes
========   =========================
--------------------------------------------------------------------------------*/

public class NodeList {
	Node head, tail;
	
	int entries;
	boolean hasReturnStatement = false;

	// Append x to the list
	public void add(Node x) {
		
		
		if(x.kind == Node.RETURN){
			hasReturnStatement = true;
		}
		
		if (x != null) {
			if (head == null)
				head = x;
			else
				tail.next = x;
			tail = x;
		}

		entries++;
	}

	// Retrieve the head of the list
	public Node get() {
		return head;
	}
}