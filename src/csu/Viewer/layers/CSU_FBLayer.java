package csu.Viewer.layers;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.Action;
import javax.swing.AbstractAction;

import csu.Viewer.SelectedObject;
import csu.Viewer.TestFBViewer;

import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;

import rescuecore2.misc.Pair;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.config.Config;
import rescuecore2.view.Icons;
import rescuecore2.log.Logger;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.view.StandardEntityViewLayer;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.misc.AgentPath;

public class CSU_FBLayer extends StandardEntityViewLayer<Human> {

	private Set<EntityID> FBIDs;

	private Map<EntityID, Queue<Pair<Integer, Integer>>> frames;
	private boolean animationDone;

	private static final int SIZE = 10;

	private static final int HP_MAX = 10000;
	private static final int HP_INJURED = 7500;
	private static final int HP_CRITICAL = 1000;


	private static HumanSorter HUMAN_SORTER = null;

	private static final Color FIRE_BRIGADE_COLOUR = Color.RED;
	private static final Color DEAD_COLOUR = Color.BLACK;

	
	/**
	 * Construct an animated fire brigades view layer.
	 */
	public CSU_FBLayer() {
		super(Human.class);

		FBIDs = new HashSet<EntityID>();
		frames = new HashMap<EntityID, Queue<Pair<Integer, Integer>>>();
		animationDone = true;
	}
	/*--------------------------------------------animate part-----------------------------------------------*/
	/**
	 * Increase the frame number.
	 * 
	 * @return True if a new frame is actually required.
	 */
	public boolean nextFrame() {
		synchronized (this) {
			if (animationDone) {
				return false;
			}
			animationDone = true;
			for (Queue<Pair<Integer, Integer>> next : frames.values()) {
				if (next.size() > 0) {
					next.remove();
					animationDone = false;
				}
			}
			return !animationDone;
		}
	}

	protected Pair<Integer, Integer> getLocation(Human h) {
		synchronized (this) {
			Queue<Pair<Integer, Integer>> agentFrames = frames.get(h.getID());
			if (agentFrames != null && !agentFrames.isEmpty()) {
				return agentFrames.peek();
			}
		}
		return h.getLocation(world);
	}

	@Override
	protected void preView() {
		super.preView();
		FBIDs.clear();
	}

	@Override
	protected void viewObject(Object o) {
		super.viewObject(o);
		if (o instanceof Human) {
			FBIDs.add(((Human) o).getID());
		}
	}

	/**
	 * Compute the animation frames.
	 * 
	 * @param frameCount
	 *            The number of animation frames to compute.
	 */
	void computeAnimation(int frameCount) { 
		synchronized (this) {
			frames.clear();
			// Compute animation
			double step = 1.0 / (frameCount - 1.0);
			for (EntityID next : FBIDs) {
				Queue<Pair<Integer, Integer>> result = new LinkedList<Pair<Integer, Integer>>();
				Human human = (Human) world.getEntity(next);
				if (human == null) {
					continue;
				}
				AgentPath path;
				StandardEntity position = world.getEntity(human.getPosition());
				if (position instanceof AmbulanceTeam)
					// to render the civilian in ambulance team movement
					path = AgentPath.computePath((AmbulanceTeam) position,
							world);
				else
					path = AgentPath.computePath(human, world);
				if (path == null) {
					continue;
				}
				for (int i = 0; i < frameCount; ++i) {
					Pair<Integer, Integer> nextPoint = path.getPointOnPath(i
							* step);
					result.add(nextPoint);
				}
				frames.put(next, result);
			}
			animationDone = false;
		}
	}
/*---------------------------------------------------------------------------------------------------------------*/
	
	@Override
	public void initialise(Config config) {
		
		synchronized (this) {
			frames.clear();
			animationDone = true;
		}
	}

	@Override
	public String getName() {
		return "Fire Brigades(animated)";
	}

	@Override
	public Shape render(Human h, Graphics2D g, ScreenTransform t) {
		// if(h instanceof AmbulanceTeam
		// &&world.getEntity(h.getPosition())instanceof Road)
		// return null;

		Pair<Integer, Integer> location = getLocation(h);
		if (location == null) {
			return null;
		}
		int x = t.xToScreen(location.first());
		int y = t.yToScreen(location.second());
		Shape shape;
		if (h.isPositionDefined()
				&& (world.getEntity(h.getPosition()) instanceof AmbulanceTeam))
			// draw humans smaller in ambulances
			shape = new Ellipse2D.Double(x - SIZE / 3, y - SIZE / 3,
					SIZE / 3 * 2, SIZE / 3 * 2);
		else
			shape = new Ellipse2D.Double(x - SIZE / 2, y - SIZE / 2, SIZE,
					SIZE);
		Color color = getColour(h);
		EntityID selectedFB = SelectedObject.selectedAgent;
		if(h != null && h.getID().equals(selectedFB)) {
			shape = new Ellipse2D.Double(x - SIZE, y - SIZE, SIZE*2, SIZE*2);
			color = Color.ORANGE;
		}
		if(color == null)
			return shape;
		g.setColor(adjustColour(color, h.getHP()));
		g.fill(shape);
		g.setColor(getColour(h));
		g.draw(shape);
		
		return shape;
	}

	@Override
	public List<JMenuItem> getPopupMenuItems() {
		List<JMenuItem> result = new ArrayList<JMenuItem>();
	//	result.add(new JMenuItem(useIconsAction));
		return result;
	}

	@Override
	protected void postView() {
		if (world == null)
			return;
		if (HUMAN_SORTER == null)
			HUMAN_SORTER = new HumanSorter(world);
		Collections.sort(entities, HUMAN_SORTER);

	}

/*	private Map<State, Icon> generateIconMap(String type) {
		Map<State, Icon> result = new EnumMap<State, Icon>(State.class);
		for (State state : State.values()) {
			String resourceName = "rescuecore2/standard/view/" + type + "-"
					+ state.toString() + "-" + iconSize + "x" + iconSize
					+ ".png";
			URL resource = CSU_FBLayer.class.getClassLoader().getResource(
					resourceName);
			if (resource == null) {
				Logger.warn("Couldn't find resource: " + resourceName);
			} else {
				result.put(state, new ImageIcon(resource));
			}
		}
		return result;
	}
*/
	private Color getColour(Human h) {
		switch (h.getStandardURN()) {
		 case FIRE_BRIGADE:
			 	return FIRE_BRIGADE_COLOUR;
		 default:
			    return null; 
		}
	}

	private Color adjustColour(Color c, int hp) {
		if (hp == 0) {
			return DEAD_COLOUR;
		}
		if (hp < HP_CRITICAL) {
			c = c.darker();
		}
		if (hp < HP_INJURED) {
			c = c.darker();
		}
		if (hp < HP_MAX) {
			c = c.darker();
		}
		return c;
	}

/*	private Icon getIcon(Human h) {
		State state = getState(h);
		Map<State, Icon> iconMap = null;
		switch (h.getStandardURN()) {
		case CIVILIAN:
			boolean male = h.getID().getValue() % 2 == 0;
			if (male) {
				iconMap = icons.get(StandardEntityURN.CIVILIAN.toString()
						+ "-Male");
			} else {
				iconMap = icons.get(StandardEntityURN.CIVILIAN.toString()
						+ "-Female");
			}
			break;
		default:
			iconMap = icons.get(h.getStandardURN().toString());
		}
		if (iconMap == null) {
			return null;
		}
		return iconMap.get(state);
	}
*/
	private State getState(Human h) {
		int hp = h.getHP();
		if (hp <= 0) {
			return State.DEAD;
		}
		if (hp <= HP_CRITICAL) {
			return State.CRITICAL;
		}
		if (hp <= HP_INJURED) {
			return State.INJURED;
		}
		return State.HEALTHY;
	}

	private enum State {
		HEALTHY {
			@Override
			public String toString() {
				return "Healthy";
			}
		},
		INJURED {
			@Override
			public String toString() {
				return "Injured";
			}
		},
		CRITICAL {
			@Override
			public String toString() {
				return "Critical";
			}
		},
		DEAD {
			@Override
			public String toString() {
				return "Dead";
			}
		};
	}

	private static final class HumanSorter implements Comparator<Human>,
			java.io.Serializable {
		private final StandardWorldModel world;

		public HumanSorter(StandardWorldModel world) {
			this.world = world;
		}

		@Override
		public int compare(Human h1, Human h2) {
			if (world.getEntity(h1.getPosition()) instanceof AmbulanceTeam
					&& !(world.getEntity(h2.getPosition()) instanceof AmbulanceTeam))
				return 1;
			if (!(world.getEntity(h1.getPosition()) instanceof AmbulanceTeam)
					&& (world.getEntity(h2.getPosition()) instanceof AmbulanceTeam))
				return -1;
			if (h1 instanceof Civilian && !(h2 instanceof Civilian)) {
				return -1;
			}
			if (h2 instanceof Civilian && !(h1 instanceof Civilian)) {
				return 1;
			}

			// return h1.getID().getValue() - h2.getID().getValue();

			return h2.getHP() - h1.getHP();
		}
	}

/*	private final class UseIconsAction extends AbstractAction {
		public UseIconsAction() {
			super("Use icons");
			putValue(Action.SELECTED_KEY, Boolean.valueOf(useIcons));
			putValue(Action.SMALL_ICON, useIcons ? Icons.TICK : Icons.CROSS);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			useIcons = !useIcons;
			putValue(Action.SELECTED_KEY, Boolean.valueOf(useIcons));
			putValue(Action.SMALL_ICON, useIcons ? Icons.TICK : Icons.CROSS);
			component.repaint();
		}
	}*/
}
