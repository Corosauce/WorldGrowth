package vectortree.simulation.tree;

import vectortree.simulation.TreeSimulation;

/**
 * 
 * @author Corosus
 *
 */
public class GrowthNodeNew {

	private TreeSimulation tree;
	private GrowthNodeNew parent;
	private int level;
	
	public GrowthNodeNew(TreeSimulation tree, GrowthNodeNew parent, int level) {
		this.tree = tree;
		this.parent = parent;
		this.level = level;
	}
	
}
