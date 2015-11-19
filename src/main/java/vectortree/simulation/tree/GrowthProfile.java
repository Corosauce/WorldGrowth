package vectortree.simulation.tree;

import java.util.HashMap;

import net.minecraft.block.Block;


/**
 * This can define the trunk, or branches, etc
 * 
 * what defines a node?
 * 
 * profiled and runtime values of:
 * - thickness
 * - length
 * - ideal direction
 * - 
 * 
 * just profiled:
 * - growth rate
 * 
 * random ideas:
 * - perhaps an algorithm can help define how it all functions
 * - eg: how many child branches can grow depending on what level deep we are in the tree node hierarchy
 * 
 * 
 * adjusting from old code to new:
 * - adjust branch growth patterns to allow for random variance
 * -- only do a new node if its a legit child branch
 * -- otherwise keep growing with random variance but maintaining an overall direction
 * 
 * - spreading via fruit dropping with seeds and growing new tree
 * -- simulate dont make actual items
 * 
 * ideas about profiling:
 * ======================
 * - define on a per branch level basis, since most trees dont go beyond 3-4 levels
 * 
 * branch options:
 * ---------------
 * 
 * - up/down ratio where 1 is even, 2 is up, 0 is down
 * - inherit direction from previous branch or fully use above option
 * -- amount to inherit, sliding value? 0-1?
 * - do leaf producing module
 * 
 * - 
 * 
 * leaf module options:
 * --------------------
 * 
 * - leaf block type (make some extras?, allow mod leafs assuming they will even cooperate)
 * - length/distance from origin point of module
 * - pattern:
 * -- circular flat
 * -- droop
 * -- ???
 * - maybe break down patterns to how branches can behave to provide more freedom and less templaty options
 * 
 * @author Corosus
 *
 */
public class GrowthProfile {

	private int levels;
	
	private HashMap<Integer, GrowthProfilePiece> levelProfiles = new HashMap<Integer, GrowthProfilePiece>();
	
	/**
	 * Might use this as a generic class for growing both branches with logs, or leaves with ... leaves
	 * 
	 * @author Corosus
	 *
	 */
	public static class GrowthProfilePiece {
		
		/**
		 * consider making growth direction calculations based on trunk position, not parent branch, so it spreads out in a more natural way
		 */
		
		/**
		 * For both branches and leaves maybe
		 */
		private Block blockToPlace;
		
		private float length;
		private float thickness;
		
		/**
		 * inherit vector from parent amount for initial direction
		 * ranges: 0-1
		 */
		private float inheritDirectionAmount;
		
		/**
		 * ranges: -1 to 1, -1 is down, 0 is middle, 1 is up
		 */
		private float growthDirectionVertical;
		
		/**
		 * Allow for a random variance, but still keep it within original vector direction
		 * Would prevent scenarios like branches curling over their own branch
		 * ranges: 0-1 maybe, how its used, unsure, maybe 0 = strait, 1 = allow for full 90 degree variance, but no more
		 * perhaps make -1 be a disable option
		 * 
		 */
		private float growthDirectionVarianceRandomRange;
		
		/**
		 * 
		 */
		private int childBranchesToMake;
		
		/**
		 * 
		 */
		private float growthRate;

		public GrowthProfilePiece() {
			
		}

		public float getGrowthRate() {
			return growthRate;
		}

		public void setGrowthRate(float growthRate) {
			this.growthRate = growthRate;
		}

		public Block getBlockToPlace() {
			return blockToPlace;
		}

		public void setBlockToPlace(Block blockToPlace) {
			this.blockToPlace = blockToPlace;
		}

		public float getLength() {
			return length;
		}

		public void setLength(float length) {
			this.length = length;
		}

		public float getThickness() {
			return thickness;
		}

		public void setThickness(float thickness) {
			this.thickness = thickness;
		}

		public float getInheritDirectionAmount() {
			return inheritDirectionAmount;
		}

		public void setInheritDirectionAmount(float inheritDirectionAmount) {
			this.inheritDirectionAmount = inheritDirectionAmount;
		}

		public float getGrowthDirectionVertical() {
			return growthDirectionVertical;
		}

		public void setGrowthDirectionVertical(float growthDirectionVertical) {
			this.growthDirectionVertical = growthDirectionVertical;
		}

		public float getGrowthDirectionVarianceRandomRange() {
			return growthDirectionVarianceRandomRange;
		}

		public void setGrowthDirectionVarianceRandomRange(
				float growthDirectionVarianceRandomRange) {
			this.growthDirectionVarianceRandomRange = growthDirectionVarianceRandomRange;
		}
		
		public int getChildBranchesToMake() {
			return childBranchesToMake;
		}

		public void setChildBranchesToMake(int childBranchesToMake) {
			this.childBranchesToMake = childBranchesToMake;
		}
		
		
		
	}
	
	public void setPiece(int level, GrowthProfilePiece piece) {
		levelProfiles.put(level, piece);
	}

	public int getLevels() {
		return levels;
	}

	public void setLevels(int levels) {
		this.levels = levels;
	}
	
	public GrowthProfilePiece getProfileForLevel(int level) {
		return levelProfiles.get(level);
	}
}


