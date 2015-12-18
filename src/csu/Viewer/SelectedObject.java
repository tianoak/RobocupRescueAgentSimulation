package csu.Viewer;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

/**
 * This class bridages the Viewer and its layers.
 * Date: Mar 1, 2014 Time: 11:58pm
 * 
 * @author Appreciation - csu
 */
public class SelectedObject {
	/**
	 * The selected entity by the mouse when you click the viewer.
	 */
	public static StandardEntity selectedObject;
	
	/**
	 * The selected Agent in the combox.
	 */
	public static EntityID selectedAgent;
	
	public static boolean renderBuildingValueKey = false;
}
