package csu.common.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import math.geom2d.Point2D;

@SuppressWarnings("serial")
public class RotatePoint extends JPanel{
	private Color sta_color = Color.BLACK;
	private Color tar_color = Color.GREEN;
	private Color rot_color = Color.RED;
	
	private int theta = 45;
	
	private Point2D standardPoint = new Point2D(500, 500);
	
	private Point2D targetPoint = new Point2D(200, 200);
	
	private Point2D rotatePoint = null;

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D gra_2d = (Graphics2D)g;
		gra_2d.setColor(sta_color);
		gra_2d.fillOval((int)standardPoint.x - 3, (int)standardPoint.y - 3, 6, 6);
		
		gra_2d.drawLine((int)standardPoint.x, (int)standardPoint.y, (int)targetPoint.x, (int)targetPoint.y);
		
		gra_2d.setColor(tar_color);
		gra_2d.fillOval((int)targetPoint.x - 3, (int)targetPoint.y - 3, 6, 6);
		
		rotatePoint = targetPoint.rotate(standardPoint, Math.toRadians(theta));
		gra_2d.setColor(rot_color);
		gra_2d.fillOval((int)rotatePoint.x - 3, (int)rotatePoint.y - 3, 6, 6);
		
		gra_2d.drawLine((int)standardPoint.x, (int)standardPoint.y, (int)rotatePoint.x, (int)rotatePoint.y);
	}
	
	public static void main(String[] args) {
		
		System.out.println("atan(1.0) = " + Math.atan(1.0));
		System.out.println("PI / 4 = " + Math.PI / 4);
		
		JFrame frame = new JFrame("Rotate Point");
		frame.setSize(new Dimension(1000, 800));
		RotatePoint test = new RotatePoint();
		frame.add(test);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
