package org.xeroserver.cmm;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.xeroserver.AstVisualizer.AstVisualizer;

import com.oracle.truffle.cmm.parser.Node;
import com.oracle.truffle.cmm.parser.Parser;
import com.oracle.truffle.cmm.parser.Scanner;

public class CMM_Frontend {

	private boolean debugging = false;
	private File f = null;
	private Scanner scanner = null;
	private Parser parser = null;

	public CMM_Frontend(File f) {
		this.f = f;
	}

	public void setDebugmode(boolean mode) {
		this.debugging = mode;
	}

	public void parse() {

		scanner = new Scanner(f.getAbsolutePath());
		parser = new Parser(scanner);

		if (debugging) {
			parser.debug[0] = true;
			parser.debug[1] = true;
		}

		parser.Parse();

	}

	public int getErrorCount() {
		return parser.getErrorCount();
	}

	public Parser getParser() {
		return parser;
	}

	public HashMap<Integer, String> getErrorList() {
		return parser.getErrorList();
	}

	public ArrayList<String> getProcedures() {
		return parser.tab.getProcNames();
	}

	public void visualize(String method) {
		new AstVisualizer(parser.tab.find(method).ast, method);
	}

	public Node getMainNode() {
		return parser.tab.find("Main").ast;
	}

	public static void main(String[] args) {

		CMM_Frontend front = new CMM_Frontend(new File(args[0]));
		front.setDebugmode(true);
		front.parse();

		HashMap<Integer, String> errors = front.getErrorList();

		for (String s : errors.values()) {
			System.out.println("err: " + s);
		}

		front.visualize("Main");

	}

}