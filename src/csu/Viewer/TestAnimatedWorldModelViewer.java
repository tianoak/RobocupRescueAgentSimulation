package csu.Viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import csu.Viewer.layers.CSU_BuildingLayer;
import csu.Viewer.layers.CSU_ConvexHullLayer;
import csu.Viewer.layers.CSU_CriticalAreaLayer;
import csu.Viewer.layers.CSU_PartitionLayer;
//import csu.Viewer.layers.CSU_PovLayer;
import csu.Viewer.layers.CSU_RoadLayer;
import csu.Viewer.layers.CSU_ZonePolygonLayer;
import rescuecore2.standard.view.AnimatedHumanLayer;
import rescuecore2.standard.view.AreaIconLayer;
import rescuecore2.standard.view.AreaNeighboursLayer;
import rescuecore2.standard.view.CommandLayer;
import rescuecore2.standard.view.PositionHistoryLayer;
import rescuecore2.standard.view.RoadBlockageLayer;

/**
 * 
 * Date: Feb 23, 2014 Time: 3:48pm
 * 
 * @author appreciation-csu
 *
 */
@SuppressWarnings("serial")
public class TestAnimatedWorldModelViewer extends TestStandardWorldModelViewer{
	private static final int FRAME_COUNT = 10;									// frame count
	private static final int ANIMATION_TIME = 750;								// animation time
	private static final int FRAME_DELAY = ANIMATION_TIME / FRAME_COUNT;			// frame delay

	private AnimatedHumanLayer humans;
	private Timer timer;
	private final Object lock = new Object();
	private boolean done;
	
	public TestAnimatedWorldModelViewer() {
		super(); ///call addDefaultLayers() here
		timer = new Timer(FRAME_DELAY, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized (lock) {
					if (done) {
						return;
					}
					done = true;
					if (humans.nextFrame()) {
						done = false;
						repaint();
					}
				}
			}
		});
		timer.setRepeats(true);
		timer.start();
	}
	
	@Override
	public String getViewerName() {
		return "CSU Animated World Model Viewer";
	}
	
	@Override
	public void addDefaultLayers() {
		addLayer(new CSU_RoadLayer());
		addLayer(new CSU_BuildingLayer());
		addLayer(new CSU_ConvexHullLayer());
		addLayer(new CSU_ZonePolygonLayer());
		addLayer(new CSU_CriticalAreaLayer());
//		addLayer(new CSU_PovLayer());
		addLayer(new CSU_PartitionLayer());
		
//		addLayer(new BuildingLayer());						
//		addLayer(new RoadLayer());							
		addLayer(new AreaNeighboursLayer());				
		addLayer(new RoadBlockageLayer());					
		
		AreaIconLayer areaIcon = new AreaIconLayer();
		areaIcon.setVisible(true);
		addLayer(areaIcon);						
		
		humans = new AnimatedHumanLayer();		
		humans.setVisible(true);
		addLayer(humans);	
		
		CommandLayer commands = new CommandLayer();
		addLayer(commands);									
		commands.setRenderMove(false);
		addLayer(new PositionHistoryLayer());				
	}
	
	
	@Override
	public void view(Object... objects) {
		super.view(objects);
		synchronized (lock) {
			done = false;
//			humans.computeAnimation(FRAME_COUNT);
		}
	}
}
