package csu.model.flow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import csu.model.AdvancedWorldModel;

import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityConstants;
import rescuecore2.standard.entities.StandardEntityConstants.Fieryness;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.misc.geometry.Line2D;

public class EnergyFlow {

	private final AdvancedWorldModel world;
	private static final double LINE_LENGTH_SCALE = 1000.0 * 2.0;
	private static final double K = 1000000.0;// = k / (4 * PI)
	private static final int rayCount = 32;
	
	private final Map<Building, FlowLine[]> flowGraph;
	private final Map<Building, Double> outFlow;
	private final Map<Building, Double> inFlow;
	private final Map<Building, Double> inTotal;
	private ArrayList<Building> affectedRanking;

	public EnergyFlow(AdvancedWorldModel world) {
		this.world = world;
		final Collection<StandardEntity> buildings = world.getEntitiesOfType(StandardEntityURN.BUILDING);
		
		Map<Building, FlowLine[]> modifiableFlowGraph = new HashMap<Building, FlowLine[]>(buildings.size());
		for (StandardEntity se : buildings) {
			final Building target = (Building) se;

			final double r = Math.sqrt(target.getGroundArea()) * LINE_LENGTH_SCALE;
			FlowLine[] lineArray = new FlowLine[rayCount];
			final double dAngle = Math.PI * 2 / rayCount;
			for (int i = 0; i < rayCount; ++i) {
				final double angle = i * dAngle;
				final double dx = Math.sin(angle) * r;
				final double dy = Math.cos(angle) * r;
				final Line2D ray = new Line2D(target.getX(), target.getY(), dx, dy);
				lineArray[i] = new FlowLine(target, world.getObjectsInRange(target, (int) r), ray);
			}
			modifiableFlowGraph.put(target, lineArray);
		}
		flowGraph = Collections.unmodifiableMap(modifiableFlowGraph);
		
		outFlow = new HashMap<Building, Double>(buildings.size());
		inFlow = new HashMap<Building, Double>(buildings.size());
		inTotal = new HashMap<Building, Double>(buildings.size());
	}

	public void update(ChangeSet change) {
		inFlow.clear();
		for (Building building : world.getBurningBuildings()) {
			final double lineAffecct = calcurateLineHits(building);
			outFlow.put(building, lineAffecct);
		}
		for (Iterator<Building> it = outFlow.keySet().iterator(); it.hasNext();) {
			Building building = it.next();
			if (world.getBurningBuildings().contains(building))
				continue;
			if (!building.isOnFire()) {
				it.remove();
			}
		}
		
		affectedRanking = new ArrayList<Building>();
		for (Entry<Building, Double> entry : inFlow.entrySet()) {
			Building key = entry.getKey();
			if (key.isFierynessDefined()
					&& key.getFierynessEnum() == StandardEntityConstants.Fieryness.BURNT_OUT) {
				inTotal.remove(key);
				continue;
			}
			inTotal.put(key, getInTotal(key) + entry.getValue());
			affectedRanking.add(key);
		}
		Collections.sort(affectedRanking, new Comparator<Building>() {
			@Override
			public int compare(Building b1, Building b2) {
				double d = getInTotal(b1) - getInTotal(b2);
				if (d < 0) return -1;
				if (d > 0) return 1;
				return 0;
			}
		});
	}

	private double calcurateLineHits(Building target) {
		if (!target.isTemperatureDefined()) return 0.0;
		FlowLine[] lines = flowGraph.get(target);
		if (lines == null) return 0.0;
		
		double result = 0.0;
		for (FlowLine line : lines) {
			final Building nearestHit = line.getBuilding();
			if (nearestHit == null) continue;
			if (nearestHit.isFierynessDefined()
					&& nearestHit.getFierynessEnum() == StandardEntityConstants.Fieryness.BURNT_OUT)
				continue;
			final double dist = line.getDistance();
			final double sin = line.getSin();
			final double affectTemp = target.getTemperature();
			final double value = K * sin * affectTemp / (dist * dist);
			
			Double preAffectedValue = inFlow.get(nearestHit);
			if (preAffectedValue == null) {
				preAffectedValue = 0.0;
			}
			inFlow.put(nearestHit, preAffectedValue + value);
			
			result += value;
			if (nearestHit.isFierynessDefined()
					&& nearestHit.getFierynessEnum() == Fieryness.UNBURNT) {
				result += value;
			}
		}
		return result;
	}

	public double getOut(Building building) {
		Double result = outFlow.get(building);
		if (result == null) return 0.0;
		return result;
	}
	
	public double getOut(EntityID id) {
		return getOut((Building) world.getEntity(id));
	}
	
	public double getIn(Building building) {
		Double result = inFlow.get(building);
		if (result == null) return 0.0;
		return result;
	}
	
	public double getIn(EntityID id) {
		return getIn((Building) world.getEntity(id));
	}
	
	public double getInTotal(Building building) {
		Double result = inTotal.get(building);
		if (result == null) return 0.0;
		return result;
	}
	
	public double getInTotal(EntityID id) {
		return getInTotal((Building) world.getEntity(id));
	}
	
	public List<Building> getInTotalRanking() {
		return Collections.unmodifiableList(affectedRanking);
	}
}
