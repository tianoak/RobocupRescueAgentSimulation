//package csu.agent.search;
//
//
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.TreeSet;
//
//import rescuecore2.misc.collections.LazyMap;
//import rescuecore2.standard.entities.StandardEntity;
//import rescuecore2.worldmodel.EntityID;
//import csu.agent.Agent;
//import csu.model.AdvancedWorldModel;
//import csu.model.object.CSUBuilding;
//import csu.model.object.csuZoneEntity.CsuZone;
//import csu.model.object.csuZoneEntity.CsuZones;
//
//public class ZoneBasedSearcher {
//	private AdvancedWorldModel world;
//	private StandardEntity controlledEntity;
//	private Agent<? extends StandardEntity> underlyingAgent;
//	private EntityID agentId;
//	
//	private CsuZone currentZone = null;
//	private List<CsuZone> unsearchedZone;
//	
//	public ZoneBasedSearcher(AdvancedWorldModel world) {
//		this.world = world;
//		this.controlledEntity = world.getControlledEntity();
//		this.underlyingAgent = world.getAgent();
//		this.agentId = underlyingAgent.getID();
//	}
//	
//	public void setCurrentZone(CSUBuilding targetBuilding) {
//		
//	}
//	
//	
//}
