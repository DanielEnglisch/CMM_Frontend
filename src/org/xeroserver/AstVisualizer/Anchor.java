package org.xeroserver.AstVisualizer;

public class Anchor {

	@Override
	public String toString() {
		return "Anchor [" + x + "|" + y + "]";
	}

	public int x, y;

	public Anchor(int x, int y) {
		this.x = x;
		this.y = y;
	}

}
