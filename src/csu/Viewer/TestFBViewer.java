package csu.Viewer;

import static rescuecore2.misc.java.JavaTools.instantiate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.jfree.ui.tabbedui.VerticalLayout;

import csu.Viewer.layers.CSU_BuildingLayer;
import csu.model.object.CSUBuilding;

import rescuecore2.Constants;
import rescuecore2.Timestep;
import rescuecore2.messages.control.KVTimestep;
import rescuecore2.score.ScoreFunction;
import rescuecore2.standard.components.StandardViewer;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.view.RenderedObject;
import rescuecore2.view.ViewComponent;
import rescuecore2.view.ViewLayer;
import rescuecore2.view.ViewListener;
import rescuecore2.worldmodel.EntityID;

public class TestFBViewer extends StandardViewer {
	private static final int DEFAULT_FONT_SIZE = 20;
	private static final int PRECISION = 3;
	private static final int MAXIMUM_PROPERTIES = 21;

	private ScoreFunction scoreFunction;
	private TestFBWorldModelViewer viewer;
	private JLabel timeLabel;
	private JLabel waterLable;
	private JLabel fitnessLable;
	private NumberFormat format;
	
	
	JPanel top_panel = new JPanel(new GridLayout(1, 2));
	JPanel control_panel = new JPanel(new BorderLayout());
	JPanel bottom_panel = new JPanel(new BorderLayout());
	
	/**
	 * An combox which contains all FBs.
	 */
	JComboBox<StandardEntity> agentCombox = new JComboBox<>();
	/**
	 * The place to shown the properties of mouse selected entity.
	 */
//	JTable propertyTable = new JTable(new Object[MAXIMUM_PROPERTIES][2], new String[] {"Property", "Value"});
	/**
	 * The mouse selected entity.
	 */
	StandardEntity selectedObject;
	/**
	 * When a human is selected, then the human's location is selected too.
	 */
	JPopupMenu all_selected_object = new JPopupMenu("All Selected Object");
	/**
	 * The current selected agent in {@link #agentCombox agentCombox}. 
	 */
	EntityID selectedAgent;
	/**
	 * A flag to determines whether to pause this viewer or not.
	 */
	boolean pauseFlag = false;
	public static long randomValue;
	
	boolean shouldUpdateAgentData = false;
	JCheckBox updateAgentDataCheckBox = new JCheckBox("Update Agent Data       ", shouldUpdateAgentData);
	
	
/* ----------------------------- initialise this Viewer, mainly handles UI -------------------------------- */	
	
	@Override
	protected void postConnect() {
		super.postConnect();
		JFrame frame = new JFrame("Viewer " + getViewerID() 
				+ " (" + model.getAllEntities().size() + "entities)");
		
		viewer = new TestFBWorldModelViewer();
		viewer.initialise(config);
		viewer.view(model);
		viewer.setPreferredSize(new Dimension(500, 500));
		
		initialTopPanel();
		initialControlPanel();
		initialBottomPanel();
		
		final JSplitPane main_panel = new JSplitPane();
		main_panel.setLeftComponent(viewer);
		main_panel.setRightComponent(control_panel);
		main_panel.setDividerSize(5);
		main_panel.setDividerLocation(0.99);
		
		frame.add(top_panel, BorderLayout.NORTH);
		frame.add(main_panel, BorderLayout.CENTER);
		frame.add(bottom_panel, BorderLayout.SOUTH);	
		frame.pack();
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		frame.addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent e) {

			}

			@Override
			public void componentResized(ComponentEvent e) {
				main_panel.setDividerLocation(0.8);
			}

			@Override
			public void componentMoved(ComponentEvent e) {

			}

			@Override
			public void componentHidden(ComponentEvent e) {

			}
		});
		
		viewer.addViewListener(new ViewListener() {
			
			@Override
			public void objectsRollover(ViewComponent view, List<RenderedObject> objects) {
//				for (RenderedObject next: objects)
//					System.out.println(next);
			}
			
			@Override
			public void objectsClicked(ViewComponent view, List<RenderedObject> objects) {
				if (objects.isEmpty()) {
					SelectedObject.selectedObject = null;
					selectedObject = null;
					objectSelected(null);
					viewer.repaint();
				} else if (objects.size() == 1) {
					selectedObject = (StandardEntity)objects.get(0).getObject();
					objectSelected(selectedObject.getID().getValue());
					viewer.repaint();
				} else {
					int humanCount = 0;
					int humanId = -1;
					all_selected_object.removeAll();
					for (RenderedObject next : objects) {
						StandardEntity entity = (StandardEntity)next.getObject();
						if (entity instanceof Human) {
							humanCount ++;
							if (humanCount == 1) {
								humanId = entity.getID().getValue();
							}
						}
						JMenuItem menuItem = new JMenuItem(entity.getID().getValue() + "-" + entity);
						menuItem.addActionListener(new ActionListener() {
							
							@Override
							public void actionPerformed(ActionEvent e) {
								JMenuItem source = (JMenuItem) e.getSource();
								StringTokenizer st = new StringTokenizer(source.getText(), "-");
								String stringId = st.nextToken();
								int entityId = Integer.parseInt(stringId);
								objectSelected(new Integer(entityId));
							}
						});
						all_selected_object.add(menuItem);
					}
					if (humanCount != 1) {
	                    double x = MouseInfo.getPointerInfo().getLocation().getX();
	                    double y = MouseInfo.getPointerInfo().getLocation().getY();

	                    all_selected_object.show(viewer, (int) Math.round(x), (int) Math.round(y));
	                } else if (humanId != -1) {
	                    objectSelected(new Integer(humanId));
	                }
				}
			}
		});
	}
	
	private void initialTopPanel() {
		timeLabel = new JLabel("Time: Not Started", JLabel.CENTER);
		timeLabel.setBackground(Color.WHITE);
		timeLabel.setOpaque(true);
		timeLabel.setFont(timeLabel.getFont().deriveFont(Font.PLAIN, DEFAULT_FONT_SIZE));
		
		scoreFunction = makeScoreFunction();
		format = NumberFormat.getInstance();
		format.setMaximumFractionDigits(PRECISION);
		waterLable = new JLabel("Water: Unknown", JLabel.CENTER);
		waterLable.setBackground(Color.WHITE);
		waterLable.setOpaque(true);
		waterLable.setFont(timeLabel.getFont().deriveFont(Font.PLAIN, DEFAULT_FONT_SIZE));
		
	    fitnessLable = new JLabel("fitness: Unknown", JLabel.CENTER);
		fitnessLable.setBackground(Color.WHITE);
		fitnessLable.setOpaque(true);
		fitnessLable.setFont(timeLabel.getFont().deriveFont(Font.PLAIN, DEFAULT_FONT_SIZE));
		
		
		top_panel.add(timeLabel);
		top_panel.add(waterLable);
		top_panel.add(fitnessLable);
	}
	
	private void initialControlPanel() {
		addAgentToCombox();
		agentCombox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				@SuppressWarnings("unchecked")
				JComboBox<StandardEntity> combox = (JComboBox<StandardEntity>)e.getSource();
				StandardEntity selected = (StandardEntity)combox.getSelectedItem();
				selectedAgent = selected.getID();
				SelectedObject.selectedAgent = selectedAgent;
			}
		});

		JScrollPane combox_panel = new JScrollPane(agentCombox);
		combox_panel.setBorder(null);

	//	JScrollPane property_panel = new JScrollPane(propertyTable);
	//	property_panel.setSize(new Dimension(85, 195));
	//	property_panel.setBorder(null);

		JScrollPane action_panel = initialActionPanel();

		JSplitPane split_pane_1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split_pane_1.setDividerSize(5);
		split_pane_1.setDividerLocation(230);
//		split_pane_1.setTopComponent(property_panel);
		split_pane_1.setBottomComponent(action_panel);

		JSplitPane split_pane_2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split_pane_2.setDividerSize(5);
		split_pane_2.setDividerLocation(30);
		split_pane_2.setTopComponent(combox_panel);
		split_pane_2.setBottomComponent(split_pane_1);

		control_panel.add(split_pane_2);
		control_panel.setPreferredSize(new Dimension(80, 500));
	}
	
	private void addAgentToCombox() {
		for (StandardEntity next : model.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE)) {
			agentCombox.addItem(next);
		}

		selectedAgent = ((StandardEntity)agentCombox.getSelectedItem()).getID();
		SelectedObject.selectedAgent = selectedAgent;
	}
	
	private JScrollPane initialActionPanel() {
		final JMenu menu = new JMenu();
		menu.setLayout(new VerticalLayout());
		menu.setBorder(null);
		menu.setEnabled(false);
		
		//List<ViewLayer> layers = new ArrayList<ViewLayer>();
		//layers = viewer.getLayers();
		//layers.clear();
		//layers.add(new CSU_BuildingLayer());
		//layers.add(new CSU_ConvexHullLayer());
		//layers.add(new CSU_PartitionLayer());
 		for (ViewLayer next : viewer.getLayers()) {
			Action action = viewer.getLayersActions().get(next);
			
			final JMenuBar checkBoxMenuItem = new JMenuBar();
			final JMenuItem menuItem = new JMenuItem(action);
			checkBoxMenuItem.add(next.getName(), menuItem);
			final JPopupMenu popupMenu = new JPopupMenu();
			
			List<JMenuItem> items = next.getPopupMenuItems();
			if (items != null && !items.isEmpty()) {
				for (JMenuItem item : items) {
					popupMenu.add(item);
				}
			}
			
			menuItem.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {
					
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					
				}
				
				@Override
				public void mouseClicked(MouseEvent e) {
					
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
					if (e.getSource() instanceof JMenuItem) {
						// the current popup menu is showing and the mouse out of this menu
						if (popupMenu.isShowing() && !popupMenu.contains(e.getPoint())) {
							popupMenu.setVisible(false);
						}
					}
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {
					if (e.getSource() instanceof JMenuItem) {
						if (!popupMenu.isShowing()) {
							int over = 0;
							if (popupMenu.getComponents().length > 0)
								over = menuItem.getText().length() * 10;
							popupMenu.show(checkBoxMenuItem, 
									menuItem.getLocation().x + over, menuItem.getLocation().y);
						}
					}
				}
			});
			menu.add(next.getName(), checkBoxMenuItem);
			menu.addSeparator();
		}
		
		JScrollPane menu_panel = new JScrollPane(menu);
		menu_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		menu_panel.setMinimumSize(new Dimension(80, 500));
		return menu_panel;
	}

	/**
	 * The bottom panel is the place where you can control this viewer manually.
	 */
	private void initialBottomPanel() {
		Border outsideBorder = new EmptyBorder(new Insets(5, 5, 5, 5));
		Border insideBorder = new EtchedBorder(EtchedBorder.LOWERED);
		Border compoundBorder = new CompoundBorder(outsideBorder, insideBorder);
		// time text field
		JTextField timeTextField = new JTextField();
		timeTextField.setBorder(compoundBorder);
		timeTextField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {	
				JTextField textField = (JTextField) e.getSource();
				try {
					manualTimestep(Integer.parseInt(textField.getText()));
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		});
		JLabel timeLabel = new JLabel("Time: ", JLabel.RIGHT);
		timeLabel.setBackground(timeTextField.getBackground());
		timeLabel.setOpaque(true);
		// id text field
		JTextField idTextField = new JTextField();
		idTextField.setBorder(compoundBorder);
		idTextField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JTextField textField = (JTextField) e.getSource();
				try {
					objectSelected(Integer.parseInt(textField.getText()));
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		});
		JLabel idLabel = new JLabel("Id: ", JLabel.RIGHT);
		idLabel.setBackground(idTextField.getBackground());
		idLabel.setOpaque(true);
		// pause check box
		JCheckBox pauseCheckBox = new JCheckBox("pause");
		pauseCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pauseFlag = !pauseFlag;
			}
		});
		// refresh color button
		JButton refreshColorButton = new JButton("refreshColor");
		refreshColorButton.setContentAreaFilled(false);
		refreshColorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				randomValue = System.currentTimeMillis();
				viewer.repaint();
			}
		});
		
		JPanel panel_1 = new JPanel(new GridLayout(1, 6));
		panel_1.add(timeLabel);
		panel_1.add(timeTextField);
		
		panel_1.add(idLabel);
		panel_1.add(idTextField);
		
		panel_1.add(pauseCheckBox);
		panel_1.add(refreshColorButton);
		
		updateAgentDataCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				shouldUpdateAgentData = !shouldUpdateAgentData;
			}
		});
		
		bottom_panel.setBackground(timeTextField.getBackground());
		bottom_panel.add(panel_1, BorderLayout.WEST);
		bottom_panel.add(updateAgentDataCheckBox, BorderLayout.EAST);
	}
	
	
/* ------------------------------- show the properties of mouse selected entity ---------------------------- */
	
	/**
	 * Show the properties of the entity which is clicked by mouse. And those
	 * properties will be shown at an <code>JTable</code>.
	 * 
	 * @param objectId
	 *            the id of the selected object. Null or -1 when nothing was selected
	 */
	private void objectSelected(Integer objectId) {}
	
	/**
	 * When the mouse click the area which is not an entity, this method will
	 * show some system level properties. Such as channel informs.
	 */
	private void defaultCase() {}
	
	/**
	 * Some commom properties for all entities, such as Id, X, Y and etc.
	 * 
	 * @return the number of commom properties which will be used to find next
	 *         property's location in a JTable
	 */
	private void commomCase() {}

	/**
	 * The case when a human was selected.
	 * 
	 * @param base
	 *            the position of human's first property in the JTable
	 */
	private void humanCase(int base) {}
	
	/**
	 * The case when a building was selected.
	 * 
	 * @param base
	 *            the position of building's first property in the JTable
	 * @param agent
	 *            the agent whose CSUBuilding related properties will be shown
	 */
	private void buildingCase(int base, EntityID agent) {}
	
	/**
	 * The case when a road was selected.
	 * 
	 * @param base
	 *            the position of road's first property in the JTable
	 */
	private void roadCase(int base) {}
	
	/**
	 * The case when a blockade was selected.
	 * 
	 * @param base
	 *            the position of blockade's first property in the JTable
	 */
	private void blockadeCase(int base) {}
	
	
	
/* --------------------------------------------------------------------------------------------------------- */	
	/**
	 * Update time step and score.
	 */ 
	@Override
	protected void handleTimestep(final KVTimestep t) {
		super.handleTimestep(t);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				timeLabel.setText("Time: " + t.getTime());
				//double score = scoreFunction.score(model, new Timestep(t.getTime()));
				FireBrigade fb = (FireBrigade) (agentCombox.getSelectedItem());
				
				if(fb != null) {
				int water = fb.getWater();
				waterLable.setText("Water: " + format.format(water));
                fitnessLable.setText("Fitness: " + format.format(0)); 
				}
				
				viewer.view(model, t.getCommands());
				viewer.repaint();
			} 
		});
	}
	
	private void manualTimestep(int time) {
		
	}
	
//	private void handleTimestepProcess(final KVTimestep t) {
//		if (pauseFlag)
//			return;
//		super.handleTimestep(t);
//		
//		SwingUtilities.invokeLater(new Runnable() {
//			
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				
//			}
//		});
//	}
	
	@Override
	public String toString() {
		return "CSU Test Viewer";
	}

	/**
	 * Construct a ScoreFunction to get the score of this simulation.
	 * 
	 * @return the ScoreFunction
	 */
	private ScoreFunction makeScoreFunction() {
		String className = config.getValue(Constants.SCORE_FUNCTION_KEY);
		ScoreFunction result = instantiate(className, ScoreFunction.class);
		result.initialise(model, config);
		return result;
	}
	
}
