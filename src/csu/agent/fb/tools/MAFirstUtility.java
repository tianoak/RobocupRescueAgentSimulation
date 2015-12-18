package csu.agent.fb.tools;

import csu.model.AdvancedWorldModel;
import csu.util.DistanceSorter;
//import RSLBench.Constants;
//import RSLBench.Helpers.Distance;
import rescuecore2.standard.entities.Building;
import rescuecore2.worldmodel.EntityID;

/**
 * Utility function that mimicks the pre-utility functions evaluation.
 * 
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class MAFirstUtility extends MAAbstractUtility {
    
//	AdvancedWorldModel world;
    @Override
    public double getFireUtility(EntityID agent, EntityID target) {
        Building b = (Building) world.getEntity(target);
        double f = b.getFieryness();
        double utility = 1.0;
        if (f == 1.0) {
            utility = 1E9;
        } else if (f == 2.0) {
            utility = 1E6;
        } else if (f == 3.0) {
            utility = 100.0;
        }
        // Trade-off between building utility and distance utility
        // The bigger the value the bigger the influence of distance.
        double distance = world.getDistance(agent, target);
        double tradeoff = KEY_UTIL_TRADEOFF;
        utility = utility / Math.pow(distance * tradeoff, 2.0);
        return utility;
    }

    @Override
    public int getRequiredAgentCount(EntityID target) {
        Building b = (Building) world.getEntity(target);
        
//        # Area covered by a single fire brigade.
//        # This is the major parameter when deciding the maximum number of agents
//        # to assign to a single fire.
        int area = b.getTotalArea();
        double neededAgents = Math.ceil(area / KEY_AREA_COVERED_BY_FIRE_BRIGADE);
        
        if (b.getFieryness() == 1) {
            neededAgents *= 1.5;
        } else if (b.getFieryness() == 2) {
            neededAgents *= 3.0;
        }
        //Logger.debugColor("BASE: " + base + " | FIERYNESS: " + b.getFieryness() + " | NEEEDED AGENTS: " + neededAgents, Logger.BG_RED);

        return (int) Math.round(neededAgents);
    }
    
}