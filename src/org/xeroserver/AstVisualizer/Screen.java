package org.xeroserver.AstVisualizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class Screen extends JFrame {

	private static final long serialVersionUID = 1L;
	public JPanel panel = null;

	public ArrayList<Line> lines = new ArrayList<Line>();

	public Screen(String s) {

		this.setTitle("AST Visualizer - " + s);
		this.setSize(1080, 720);
		this.setLayout(null);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		panel = new JPanel() {

			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);

				g.setColor(Color.BLACK);
				for (Line l : lines) {
					g.setColor(l.c);
					g.drawLine(l.x1, l.y1, l.x2, l.y2);
				}

			}

		};

		// panel.setBorder(BorderFactory.createLineBorder(Color.red));
		panel.setPreferredSize(new Dimension(5000, 2000));
		panel.setLayout(null);

		final JScrollPane scroll = new JScrollPane(panel);

		setLayout(new BorderLayout());

		add(scroll, BorderLayout.CENTER);

	}

}
