package csu.common.test;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;

public class Test_menu {
	public static final int MAXIMUM_MENUBAR = 5;
	public static final int MAXIMUM_MENUITEM = 10;
	
	/**
	 * Constructor a Test_menu object and initialise it.
	 */
	public Test_menu() {
		initialise();
	}
	
	/**
	 * Do the initialise work for this Test_menu object.
	 */
	public void initialise() {
		JFrame frame = new JFrame("Test Menu Item");
		frame.setPreferredSize(new Dimension(500, 500));
		
		JMenuBar menuBar = new JMenuBar();
		for (int i = 0; i < MAXIMUM_MENUBAR; i++) {
			JMenu menu = new JMenu("menu_" + i);
			for (int j = 0; j < MAXIMUM_MENUITEM; j++) {
				JMenuItem menuItem = new JMenuItem("menuItem_" + j);
				menu.add(menuItem);
			} 
			
			if (i % 2 == 0) {
				menu.addSeparator();
				JMenu subMenu = new JMenu("sub-menu");
				for (int j = 0; j < MAXIMUM_MENUITEM; j++) {
					JMenuItem menuItem = new JMenuItem("subMenuItem_" + j);
					subMenu.add(menuItem);
				} 
				menu.add(subMenu);
			}
			
			menuBar.add(menu);
		}
		frame.setJMenuBar(menuBar);
		frame.pack();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		new Test_menu();
	}
}
