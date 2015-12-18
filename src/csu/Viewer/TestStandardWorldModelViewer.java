package csu.Viewer;

import rescuecore2.standard.view.AreaIconLayer;
import rescuecore2.standard.view.AreaNeighboursLayer;
import rescuecore2.standard.view.BuildingLayer;
import rescuecore2.standard.view.CommandLayer;
import rescuecore2.standard.view.HumanLayer;
import rescuecore2.standard.view.PositionHistoryLayer;
import rescuecore2.standard.view.RoadBlockageLayer;
import rescuecore2.standard.view.RoadLayer;

/**
 * A viewer for StandardWorldModel. Mainly borrowed from RCRSS, and with same
 * small change. All changes are marked by "TODO".
 * 
 * Date: Feb 23, 2014 Time 3:38pm
 * 
 * @author Appreciation - csu
 * 
 */
@SuppressWarnings("serial")
public class TestStandardWorldModelViewer extends TestLayerViewComponent {
	/**
	 * Construct a standard world model viewer.
	 */
	public TestStandardWorldModelViewer() {
		addDefaultLayers();
	}
	
	// TODO the return was changed
	@Override
	public String getViewerName() {
		return "CSU Standard World Model Viewer";
	}

	public void addDefaultLayers() {
		addLayer(new BuildingLayer());
		addLayer(new RoadLayer());
		addLayer(new AreaNeighboursLayer());
		addLayer(new RoadBlockageLayer());
		addLayer(new AreaIconLayer());
		addLayer(new HumanLayer());
		addLayer(new CommandLayer());
		addLayer(new PositionHistoryLayer());
	}
}
