package csu.geom;

import java.awt.Graphics;
import java.awt.Window;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import csu.common.clustering.TestForSetTriangle;

import math.geom2d.line.Line2D;

public class TestForLine extends JPanel {
	public void paintComponent(Graphics g) {
//		g.drawLine(0,0,1400,1100);
		
	}
	public static void main(String[] args) {
		new TestForLine();
		JFrame frame = new JFrame("Line Angle");
        System.out.println("height:  " + frame.getBounds().height);
        System.out.println("width:    " + frame.getBounds().width);
		frame.add(new TestForLine());
    	frame.setSize(800, 800);
    	frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    	frame.setVisible(true);
		Line2D anchor = new Line2D(50,10,30,30);
		double anchorAngle = anchor.getHorizontalAngle();
		double degree = Math.toDegrees(anchorAngle);
		System.out.println(degree
				);
	}
	

}
