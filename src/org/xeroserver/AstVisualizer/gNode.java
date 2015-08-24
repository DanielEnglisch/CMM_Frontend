package org.xeroserver.AstVisualizer;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

public class gNode extends JComponent {

	private static final long serialVersionUID = 1L;
	public String name = "?";
	public gNode next, left, right, parent;
	public Anchor a, l, r, n;

	public boolean listelement = false;
	public boolean isHead = false;
	public boolean isTail = false;

	public int height = 30, width = 80;

	public gNode(String name, Anchor a) {

		this.name = name;
		this.a = a;

		init();

	}

	public gNode(String name, Anchor a, gNode parent) {

		this.name = name;
		this.a = a;
		this.parent = parent;

		init();

	}

	public void setAnchorY(int y) {
		this.a.y = y;

		updateAnchor(a);
	}

	public void setAnchorX(int x) {
		this.a.x = x;

		updateAnchor(a);
	}

	public int getAnchorX() {
		return a.x;
	}

	public int getAnchorY() {
		return a.y;
	}

	private void init() {

		setLayout(null);
		setBounds(a.x + (width / 2), a.y, width, height);

		l = new Anchor(a.x - (width / 2), a.y + height);
		r = new Anchor(a.x + width - (width / 2), a.y + height);

		n = new Anchor(a.x + width, a.y);

	}

	public void updateXPos(int delta) {
		a.x -= delta;
		setBounds(a.x + (width / 2), a.y, width, height);

		if (left != null) {
			left.updateXPos(delta);
		}
		if (right != null) {
			right.updateXPos(delta);
		}
		if (next != null) {
			next.updateXPos(delta);
		}

	}

	public void updateAnchor(Anchor an) {

		this.a = an;

		init();

		if (left != null) {
			left.updateAnchor(l);
		}
		if (right != null) {
			right.updateAnchor(r);
		}
		if (next != null) {
			next.updateAnchor(n);
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.setColor(Color.LIGHT_GRAY);
		// g.fillRect(0, 0, width, height);

		g.setColor(Color.BLACK);

		if (this.listelement) {
			g.setColor(Color.RED);
		}

		if (this.isHead) {
			g.setColor(Color.BLUE);
		}

		if (this.isTail) {
			g.setColor(Color.GREEN);
		}

		g.drawString(name, (width / 2) - name.length() * 2, height / 2);

	}

}
