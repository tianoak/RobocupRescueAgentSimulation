package csu;

//import java.util.Iterator;
import java.lang.String;
//import rescuecore2.standard.entities.Human;
//import csu.model.AgentConstants;
import java.io.*;

/**
 *@author oak
 *@version Mar 15, 2015 9:11:21 AM
 *
 *how about change the array copying methods from for loop to System.arraycopy() to operate more quickly?
 *getMapBorderBuildings()
 *
 *
 */
/**
 *@author oak
 *@version Mar 18, 2015 8:25:35 AM
 *if contains the machine self-learning module ? if not, add it!
 */

public class Advice {
           public static void main(String[] args){
       // 	   FOR:for (Iterator<Human> itor = coincidentBuriedAgent.iterator(); itor.hasNext(); ) {
       	//		Human human = itor.next();
       	//		String string;
       	//		if (AgentConstants.PRINT_TEST_DATA_PF) {
       		//		if (string == null) {
       		//			string = human.getID().getValue() + "";
       		//		} else {
       			//		string = string + ", " + human.getID().getValue();
       		//		}
       		//	}
       		//	System.out.println();
         //  }
//}
// study package agent completely
// thinktime, beforeact(), act(), afteract()
// compute and decide action strategy every timestep (1s means 1 minutes in reality).
// extinguish fire, need to decide agents' position, the optimal target building, water volume according to the energy 
// gained by every timestep dynamically
// map  modules/map/mapeditor, modules/gis2/scenarioeditor, finally compute
// how does blockages appear?

///the water volume decision, according to energy
///the triangle when extinguishing
///sort buildings
///extinguish direction: the farthest point, achieved; the amount of buildings in this direction, not achieved.
///if target is null, use the last target, which still need considering
///compute in thinktime every period
///area=building+road, convert to get clusters by zones instead of buildings
///if two clusters have common border, small  then merge
///the hesitation of fbs
	File file = new File(args[0]);
	System.out.println(file.getAbsoluteFile());
	
}
}