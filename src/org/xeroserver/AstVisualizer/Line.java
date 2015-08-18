package org.xeroserver.AstVisualizer;

import java.awt.Color;

public class Line {
	@Override
	public String toString() {
		return "Line [x1=" + x1 + ", x2=" + x2 + ", y1=" + y1 + ", y2=" + y2 + "]";
	}

	public int x1, x2, y1, y2;
	public Color c;

	public Line(int x1, int y1, int x2, int y2, Color c) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.c = c;
	}
}