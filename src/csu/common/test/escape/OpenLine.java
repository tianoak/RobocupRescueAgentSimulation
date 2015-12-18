package csu.common.test.escape;

import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;


@SuppressWarnings("serial")
public class OpenLine extends JPanel{
	
	// private EscapeData escapeData;
	
	
	
	
	@Override
	public void paintComponent(Graphics g) {
		
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Open Part");
		JPanel panel = new OpenLine();
		frame.add(panel);
		frame.setSize(1100, 800);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
