package org.xeroserver.AstVisualizer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;

import com.oracle.truffle.cmm.parser.Node;

public class NodeIterator {

	private ArrayList<gNode> gnodes = new ArrayList<gNode>();
	public Screen screen = null;
	private Node mainNode = null;

	private boolean currentHead = false;

	public NodeIterator(Node n, Screen s) {
		this.screen = s;
		this.mainNode = n;

		iterate();

	}

	private void iterate() {

		// Build gNode Tree:
		conv(mainNode, new Anchor(800, 100));

		Collections.reverse(gnodes);

		// Arrange Anchors (Spacing)
		arrange();
		
			//Collision Count (DEBUG)
			//System.out.println(getCollisionCount() + " collisions between nodes!");

		// Connect Nodes with lines
		drawLines();

		// Add final gNodes to JFrame:
		addNodes();

	}

	public void arrange() {
						
		int listSpace =  -60;
		int branchSpace = -20;
		int ySpace = 0;
		
		ArrayList<gNode> listElements = new ArrayList<gNode>();
				
		
		//System.out.println("Arranging " + gnodes.size() + " nodes...");
		
		
		for(gNode gn: gnodes)
		{
			if(gn.listelement && !gn.name.equals("STATSEQ"))
			{
				listElements.add(gn);
			}
			else
			{
				if(gn.name.equals("STATSEQ"))
				{
					if(gn.parent != null)
					{
						int overlapp =getRightest(gn.parent).x - getLeftest(gn).x;
						gn.setAnchorX(gn.getAnchorX() + overlapp + listSpace *-5);
						gn.setAnchorY(gn.getAnchorY() + ySpace);
						ySpace+= 100;

					}
					
				}
				
				
				if(gn.left != null)
				{
					gn.left.setAnchorX(gn.left.getAnchorX() - (getChildCount(gn)*2) - branchSpace);
				}
				
				if(gn.right != null)
				{
					gn.right.setAnchorX(gn.right.getAnchorX() + (getChildCount(gn)*2) + branchSpace);
				}
			}
			
		}


		
		
		for(gNode gn: listElements)
		{
			
				if(gn.next != null)
				{
					int overlapp =   getRightest(gn).x - getLeftest(gn.next).x;
					
					//gn.next.setAnchorX(gn.next.getAnchorX() + overlapp + listSpace);
					gn.next.updateXPos(-overlapp + listSpace);

				}

		
		}
		
		
		
		
		
		
		
			 
	}
	
	public int getCollisionCount()
	{
		int collisions = 0;
		
		  for(gNode g1 : gnodes)
		  {
			  
			  for(gNode g2 : gnodes)
			  {
				  if(g1.getBounds().intersects(g2.getBounds()))
				  {
					 collisions++;
				  }
					  
			  }	  
		  }
		  
		 return collisions;
	}

	
	public void drawLines() {
		// Node Lines
		for (gNode gn : gnodes) {
			if (gn.listelement) {
				if (gn.next != null) {
					screen.lines.add(new Line(gn.a.x + (gn.width), gn.a.y, gn.next.a.x + (gn.width), gn.next.a.y,Color.GRAY));
				}
			}

			if (gn.left != null) {
				
					
				Line l = new Line(gn.a.x + (gn.width), gn.a.y + (gn.height / 2)+5, gn.left.a.x + (gn.width), gn.left.a.y, Color.BLACK);
				
				if(gn.name.equals("STATSEQ"))
					l.c = Color.GRAY;
					
				screen.lines.add(l);
			}

			if (gn.right != null) {
				screen.lines.add(new Line(gn.a.x + (gn.width), gn.a.y + (gn.height / 2)+5, gn.right.a.x + (gn.width),
						gn.right.a.y, Color.BLACK));
			}
		}

	}
	
	public ArrayList<gNode> getChildren(gNode n, boolean checkNext)
	{
		ArrayList<gNode> childs = new ArrayList<gNode>();
		
		if(n.left != null)
		{
			childs.addAll(getChildren(n.left, true));
			childs.add(n.left);
		}
	
		
		
		if(n.right != null)
		{
			childs.addAll(getChildren(n.right, true));
			childs.add(n.right);
		}
	
		if(checkNext)
		{
			if(n.next != null)
			{
				childs.addAll(getChildren(n.next, true));
				childs.add(n.next);
			}
		}
		
		
		return childs;
		
	}

	public Anchor getRightest(gNode n)
	{
		ArrayList<gNode> childs = getChildren(n, false);
		
		Anchor a = n.a;
		
		for(gNode n1: childs)
		{
			Anchor current = n1.a;
			
			if(current.x > a.x)
			{
				a = current;
			}
			
		}
		
		return a;
	}
	
	public Anchor getLeftest(gNode n)
	{
		ArrayList<gNode> childs = getChildren(n, false);
		
		Anchor a = n.a;
		
		
		for(gNode n1: childs)
		{
			Anchor current = n1.a;
			
			if(current.x < a.x)
			{
				a = current;
			}
			
		}
		

		return a;
	}
	
	
	public gNode getLastRight(gNode n)
	{
		gNode last = null;
		
		for(gNode g = n; true; g = g.right)
		{
			if(g != null)
			{
				last = g;
			}else
			{
				break;
			}
		}
		
		return last;
	}
	
	public gNode getLastLeft(gNode n)
	{
		gNode last = null;
		
		for(gNode g = n; true; g = g.left)
		{
			if(g != null)
			{
				last = g;
			}else
			{
				break;
			}
		}
		
		return last;
	}

	public int getChildCount(gNode n)
	{
		return getChildren(n, false).size();
	}

	private gNode conv(Node n, Anchor a) {

		if (n == null) {
			System.out.println("Node NULL");
			return null;
		}

		// Standartfall
		gNode g = new gNode((name[n.kind]), a);

		// -----Spezielles:

		if (n.kind == Node.INTCON) {
			g.name = n.val + "";
		} else if (n.kind == Node.FLOATCON) {
			g.name = n.fVal + "";
			
		} else if (n.kind == Node.DOUBLECON) {
			g.name = n.dVal + "";
			
		} else 
			if (n.kind == Node.BOOLCON) {
				g.name = n.bVal + "";
				
			} else if (n.kind == Node.STRINGCON) {
			g.name = n.sVal;
		} else if (n.kind == Node.CHARCON) {
			g.name = Character.toChars(n.val)[0] + "";
		} else if (n.kind == Node.IDENT) {
			g.name = n.obj.name + "";
		}
		// ------------------------------------------------

		if (n.next != null) {
			g.listelement = true;

			if (!currentHead) {
				g.isHead = true;
				currentHead = true;
			}

			g.next = conv(n.next, g.n);

			g.next.listelement = true;
			g.next.parent = g;


		} else {
			if (currentHead) {
				g.isTail = true;
			}

			currentHead = false;


		}

		if (n.right != null) {
			g.right = conv(n.right, g.r);
			g.right.parent = g;
		}

		if (n.left != null) {
			g.left = conv(n.left, g.l);
			g.left.parent = g;
		}

		gnodes.add(g);

		
		
		return g;

	}

	private void addNodes() {

		for (gNode n : gnodes) {
			screen.panel.add(n);
		}
	}

	public String[] name = Node.name;

}
