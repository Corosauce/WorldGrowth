package vectortree.simulation;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import CoroUtil.world.WorldDirectorManager;
import CoroUtil.world.location.ISimulationTickable;

/**
 * Simulation of tree growth separated from chunk so it can happen while the chunk is unloaded
 * 
 * if chunk loaded: live update data or at a rate
 * if chunk unloaded: just update the sim data
 * when chunk loads: update chunk with new sim data
 * 
 * potential problem: knowing if we can grow in a direction without having the chunk loaded
 * potential solution: cache chunk data within reasonable ranges on ChunkUnload event
 * 
 * @author Corosus
 *
 */
public class TreeSimulation implements ISimulationTickable {
	
	private int dimID = 0;
	private ChunkCoordinates origin = null;
	
	private int tickRate = 20;
	
	//quick test vars, to be located into a node tree system
	private int branchLength = 1;
	private int branchLengthMax = 10;
	
	public TreeSimulation(int dimID, ChunkCoordinates origin) {
		this.dimID = dimID;
		this.origin = origin;
	}
	
	@Override
	public void tickUpdate() {
		if (isValid()) {
			if (getWorld().getTotalWorldTime() % tickRate == 0) {
				tickUpdateAct();
			}
		} else {
			System.out.println("invalid! removing");
			destroy();
		}
	}
	
	public void tickUpdateAct() {
		if (branchLength < branchLengthMax) {
			branchLength++;
			
			System.out.println("branchLength: " + branchLength);
			
			if (canLiveUpdate()) {
				getWorld().setBlock(origin.posX, origin.posY+branchLength, origin.posZ, Blocks.log2);
			}
			
			if (branchLength == branchLengthMax) {
				System.out.println("tree hit max");
			}
		}
	}
	
	public boolean isActive() {
		return branchLength < branchLengthMax;
	}
	
	public boolean isValid() {
		if (getWorld().getBlock(origin.posX, origin.posY, origin.posZ) != Blocks.log2) {
			return false;
		}
		return true;
	}
	
	/**
	 * Only factors in origin position, node pieces will likely be spread beyond origin chunk
	 * 
	 * @return
	 */
	public boolean canLiveUpdate() {
		if (getWorld().blockExists(origin.posX, origin.posY, origin.posZ)) {
			return true;
		}
		return false;
	}
	
	/**
	 * For updating block data when a chunk loads up
	 * 
	 */
	public void syncChunkFromData() {
		
	}
	
	public World getWorld() {
		return DimensionManager.getWorld(dimID);
	}
	
	public void destroy() {
		WorldDirectorManager.instance().getCoroUtilWorldDirector(getWorld()).removeTickingLocation(this);
	}

	@Override
	public void readFromNBT(NBTTagCompound parData) {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeToNBT(NBTTagCompound parData) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	@Override
	public ChunkCoordinates getOrigin() {
		return origin;
	}

}
