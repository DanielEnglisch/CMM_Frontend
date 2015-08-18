package org.xeroserver.AstVisualizer;

import java.util.HashMap;
import java.util.Map.Entry;

import com.oracle.truffle.cmm.parser.Node;

public class AstVisualizer {

	public AstVisualizer(Node mainNode, String name) {
		Screen screen = new Screen(name);
		new NodeIterator(mainNode, screen);

		screen.setVisible(true);
	}

	public AstVisualizer(HashMap<String, Node> nodes) {

		for (Entry<String, Node> e : nodes.entrySet()) {

			System.out.println("Creating Vis: " + e.getKey());

			Screen screen = new Screen(e.getKey());
			new NodeIterator(e.getValue(), screen);

			screen.setVisible(true);
		}

	}

}
