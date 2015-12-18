/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package csu.agent.fb.tools;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Building;
import rescuecore2.worldmodel.EntityID;

/**
 * Utility function that mimicks the pre-utility functions evaluation.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class MAThirdUtility extends MAAbstractUtility {

    private Double maxDistance = null;

    @Override
    public double getFireUtility(EntityID agent, EntityID target) {
        if (maxDistance == null) {
            maxDistance = getMaxDistance();
        }

        Building b = (Building) world.getEntity(target);
        double f = b.getFieryness();
        double utility = 1.0;
        if (f == 1.0) {
            utility = 3;
        } else if (f == 2.0) {
            utility = 2;
        } else if (f == 3.0) {
            utility = 1;
        }

        double distance = world.getDistance(agent, target);
        double threshold = world.getConfig().extinguishableDistance;
        if (distance < threshold) {
            distance = 0;
        }
        double factor = distance/maxDistance;
        factor = Math.pow(factor, 2);

        // Add some noise to break ties
        factor += config.getRandom().nextDouble()/10000;

        double tradeoff = KEY_UTIL_TRADEOFF;
        utility = utility - factor * tradeoff;

        //Logger.warn("Distance {}, factor {}, utility {}", distance, factor, utility);
        return utility;
    }

/*    @Override
    public double getPoliceUtility(EntityID policeAgent, EntityID blockade) {
        if (maxDistance == null) {
            maxDistance = getMaxDistance();
        }

        double threshold = world.getConfig().repairDistance;
        double distance = Distance.humanToBlockade(policeAgent, blockade, world, threshold);

        double utility = 1-distance/maxDistance;
        utility = Math.pow(utility, 2);

        // Add some noise to break ties
        utility += config.getRandom().nextDouble()/1000;

        // Downscale police utilities to subjugate them to fire agents
        utility /= 1000;

        return utility;
    }*/

    public double getMaxDistance() {
        Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> bounds = world.getWorldBounds();
        final int xmin = bounds.first().first();
        final int ymin = bounds.first().second();
        final int xmax = bounds.second().first();
        final int ymax = bounds.second().second();
        final double dx = xmax - xmin;
        final double dy = ymax - ymin;
        return Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    }

    @Override
    public int getRequiredAgentCount(EntityID target) {
        Building b = (Building) world.getEntity(target);

        int area = b.getTotalArea();
        double neededAgents = Math.ceil(area / KEY_AREA_COVERED_BY_FIRE_BRIGADE);

        if (b.getFieryness() == 1) {
            neededAgents *= 2;
        } else if (b.getFieryness() == 2) {
            neededAgents *= 1.5;
        } else if (b.getFieryness() == 3) {
            neededAgents *= 1;
        }
        //Logger.debugColor("BASE: " + base + " | FIERYNESS: " + b.getFieryness() + " | NEEEDED AGENTS: " + neededAgents, Logger.BG_RED);

        return (int) Math.round(neededAgents);
    }

}