package csu.agent.fb.tools;

import csu.model.AdvancedWorldModel;
import rescuecore2.config.Config;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;

/**
 * Skeletal implementation of a utility function.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public abstract class MAAbstractUtility implements MAUtilityFunction {
    protected AdvancedWorldModel world;
    protected Config config;
    int KEY_AREA_COVERED_BY_FIRE_BRIGADE = 200;
    int KEY_UTIL_TRADEOFF = 10;

    @Override
    public void setWorld(AdvancedWorldModel world) {
        this.world = world;
    }

    @Override
    public void setConfig(Config config) {
        this.config = config;
    }

    @Override
    public double getPoliceUtility(EntityID policeAgent, EntityID blockade) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
