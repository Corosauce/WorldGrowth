package vectortree.simulation.tree;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

/**
 * For growing fruits or veggies off of other nodes that are branches / leaves
 * 
 * Todo:
 * - should it just be restricted to 1 block pos? for now yes
 * 
 * @author Corosus
 *
 */
public class FoodNode extends BaseNode {

	private GrowthNodeNew parent;
	
	/**
	 * The exact point on the parent this food node is growing off of, typically going to be 1 block pos above position of this node
	 */
	private ChunkCoordinates posParent;
	
	private ChunkCoordinates pos;
	
	public FoodNode(GrowthNodeNew parent, ChunkCoordinates pos, ChunkCoordinates posParent) {
		this.parent = parent;
		this.pos = pos;
		this.posParent = posParent;
	}
	
	@Override
	public void tick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		// TODO Auto-generated method stub
		return nbt;
	}
	
	/**
	 * Basically for checking if the thing its growing off of is still there
	 * 
	 * @return
	 */
	public boolean isValid() {
		
		return true;
	}
	
}
