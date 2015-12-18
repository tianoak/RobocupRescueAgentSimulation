package csu.model.object;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;

import csu.model.AdvancedWorldModel;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.worldmodel.EntityID;

public class CSUBlockade {
	
	private Polygon polygon;
	
	private Blockade underlyingBlockade;
	
	private EntityID blockadeId;
	
//	private AdvancedWorldModel world;
	
	private List<Pair<Integer, Integer>> vertexes = new ArrayList<>();
	
	public CSUBlockade(EntityID blockadeId, AdvancedWorldModel world) {
//		this.world = world;
		this.underlyingBlockade = world.getEntity(blockadeId, Blockade.class);
		this.blockadeId = blockadeId;
		
		this.polygon = createPolygon(underlyingBlockade.getApexes());
	}
	
	/**
	 * Only for test
	 */
	public CSUBlockade(EntityID id, int[] apexes, int x, int y) {
		this.underlyingBlockade = new Blockade(blockadeId);
		
		this.underlyingBlockade.setX(x);
		this.underlyingBlockade.setY(y);
		this.underlyingBlockade.setApexes(apexes);
		
		this.blockadeId = id;
		this.polygon = createPolygon(apexes);
	}
	
	private Polygon createPolygon(int[] apexes) {
		int vertexCount = apexes.length / 2;
		int[] xCoordinates = new int[vertexCount];
		int[] yCOordinates = new int[vertexCount];
		
		for (int i = 0; i < vertexCount; i++) {
			xCoordinates[i] = apexes[2 * i];
			yCOordinates[i] = apexes[2 * i + 1];
			
			vertexes.add(new Pair<Integer, Integer>(apexes[2 * i], apexes[2 * i + 1]));
		}
		
		return new Polygon(xCoordinates, yCOordinates, vertexCount);
	}
	
	public Polygon getPolygon() {
		return this.polygon;
	}
	
	public Blockade getSelfBlockade() {
		return this.underlyingBlockade;
	}
	
	public EntityID getBlockadeId() {
		return this.blockadeId;
	}
	
	public List<Pair<Integer, Integer>> getVertexesList() {
		return this.vertexes;
	}
}
