package vectortree.simulation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
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
	private int branchLength = 0;
	private int branchLengthMax = 50;
	
	//stores data about location for all touched coords
	private HashMap<ChunkCoordinates, BlockDataEntry> lookupDataAll = new HashMap<ChunkCoordinates, BlockDataEntry>();
	//stores data about unupdated location pending changing once chunk loads
	private HashMap<ChunkCoordinates, BlockDataEntry> lookupDataPending = new HashMap<ChunkCoordinates, BlockDataEntry>();
	
	public TreeSimulation() {
		//needed for generic init
	}
	
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
			
			pushDataChange(new BlockDataEntry(new ChunkCoordinates(origin.posX, origin.posY+branchLength, origin.posZ), Blocks.log2));
			/*if (canLiveUpdate()) {
				getWorld().setBlock(origin.posX, origin.posY+branchLength, origin.posZ, Blocks.log2);
			}*/
			
			if (branchLength == branchLengthMax) {
				System.out.println("tree hit max");
			}
		}
	}
	
	public void pushDataChange(BlockDataEntry data) {
		lookupDataAll.put(data.getCoords(), data);
		if (canLiveUpdate()) {
			System.out.println("performing live change at " + data.getCoords());
			pushLiveChange(data);
		} else {
			System.out.println("performing pending change at " + data.getCoords());
			lookupDataPending.put(data.getCoords(), data);
		}
	}
	
	public void pushLiveChange(BlockDataEntry data) {
		getWorld().setBlock(data.getCoords().posX, data.getCoords().posY, data.getCoords().posZ, data.getBlock(), data.getMeta(), 3);
	}
	
	public boolean isActive() {
		return branchLength < branchLengthMax;
	}
	
	public boolean isValid() {
		//TODO: consider rewriting this validation to use cached data only
		if (!getWorld().blockExists(origin.posX, origin.posY, origin.posZ)) return true;
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
	public void syncChunkFromData(Chunk chunk) {
		//System.out.println("received chunk load event for chunk: " + chunk.xPosition + " - " + chunk.zPosition);
		
		/*if (chunk.xPosition > 600) {
			System.out.println("WAT");
		}*/
		
		Iterator it = lookupDataPending.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<ChunkCoordinates, BlockDataEntry> entry = (Entry<ChunkCoordinates, BlockDataEntry>) it.next();
			if (entry.getKey().posX / 16 == chunk.xPosition && entry.getKey().posZ / 16 == chunk.zPosition) {
				System.out.println("found a pending update for coord " + entry.getKey().toString());
				pushLiveChange(entry.getValue());

				//TODO: REMOVE ENTRY!!! confirm if this is best practice
				//lookupDataPending.remove(entry.getKey());
				it.remove();
				
			}
		}
	}
	
	public World getWorld() {
		return DimensionManager.getWorld(dimID);
	}
	
	public void destroy() {
		WorldDirectorManager.instance().getCoroUtilWorldDirector(getWorld()).removeTickingLocation(this);
	}

	@Override
	public void readFromNBT(NBTTagCompound parData) {
		dimID = parData.getInteger("dimID");
		origin = new ChunkCoordinates(parData.getInteger("originX"), parData.getInteger("originY"), parData.getInteger("originZ"));
		System.out.println("loaded tree origin as: " + origin.toString());
	}

	@Override
	public void writeToNBT(NBTTagCompound parData) {
		
		parData.setString("classname", this.getClass().getCanonicalName());
		
		parData.setInteger("dimID", dimID);
		parData.setInteger("originX", origin.posX);
		parData.setInteger("originY", origin.posY);
		parData.setInteger("originZ", origin.posZ);
		System.out.println("written out tree data");
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
