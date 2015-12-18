package csu.geom;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
public class TestForRotate extends JPanel {
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D graph = (Graphics2D)g;
		//Rectangle rect = new Rectangle(500,500,30,30);
		graph.setColor(Color.GREEN);
		graph.fillRect(200,200,30,100);
	
		graph.setColor(Color.pink);
		for(int i = 0; i < 16; i++) {
			graph.rotate(Math.toRadians(22.5), 215,200);
			graph.fillRect(200,200,30,100);
		}
		
		graph.setColor(Color.red);
		graph.fillOval(210, 190, 10, 10);
	}
	public static void main(String[] args) {
		JFrame frame = new JFrame("rectangle rotater");
		frame.setSize(new Dimension(800, 800));
		TestForRotate rotate = new TestForRotate();
		frame.add(rotate);
		rotate.repaint();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

}
