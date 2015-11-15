package vectortree.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import CoroUtil.forge.CoroAI;
import CoroUtil.util.CoroUtil;
import CoroUtil.util.ISerializableNBT;
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
public class TreeSimulation implements ISimulationTickable, ISerializableNBT {
	
	private int dimID = 0;
	private ChunkCoordinates origin = null;
	
	private int tickRateSimulation = 20;
	private int tickRateUpdateWorld = 1;
	private int blocksPerUpdateWorldTick = 20;
	
	//quick test vars, to be located into a node tree system
	private int branchLength = 0;
	private int branchLengthMax = 50;
	
	//stores data about location for all touched coords
	private HashMap<ChunkCoordinates, BlockDataEntry> lookupDataAll = new HashMap<ChunkCoordinates, BlockDataEntry>();
	//stores data about unupdated location pending changing once chunk loads
	@Deprecated
	private List<ChunkCoordinates> listPending = new ArrayList<ChunkCoordinates>();
	//private HashMap<ChunkCoordinates, BlockDataEntry> lookupDataPending = new HashMap<ChunkCoordinates, BlockDataEntry>();
	
	//for organizing pending updates based on the chunk its in
	private HashMap<ChunkCoordinates, List<ChunkCoordinates>> lookupChunkToBlockCoordsForPendingUpdate = new HashMap<ChunkCoordinates, List<ChunkCoordinates>>();
	
	//list of loaded chunks we need to tick for updates while we can, used for lookupChunkToBlockCoordsForPendingUpdate
	//this list is maintained by chunk load and unload events and if that chunk is in the lookup for pending updates...
	//TODO: make sure that if chunk is already loaded when entry is added to lookup pending, we check to see if we need to add that chunk into this list
	//private List<ChunkCoordinates> listChunksToTick = Collections.synchronizedList(new ArrayList<ChunkCoordinates>());
	//private Set<ChunkCoordinates> setChunksToTick = Collections.synchronizedSet(new HashSet<ChunkCoordinates>());
	private Set<ChunkCoordinates> setChunksToTick = new HashSet<ChunkCoordinates>();
	
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
			
			if (getWorld().getTotalWorldTime() % tickRateUpdateWorld == 0) {
				tickApplyWorldChanges();
			}
		} else {
			System.out.println("invalid! removing");
			destroy();
		}
	}

	@Override
	public void tickUpdateThreaded() {
		//for now, rate ticking is handled in the world director
		//if (getWorld().getTotalWorldTime() % tickRateSimulation == 0) {
			tickSimulate();
		//}
	}
	
	public void tickSimulate() {
		if (branchLength < branchLengthMax) {
			branchLength++;
			
			System.out.println("branchLength: " + branchLength);
			
			//push system harder for testing
			for (int xx = -10; xx <= 10; xx++) {
				for (int zz = -10; zz <= 10; zz++) {
					pushDataChange(new BlockDataEntry(new ChunkCoordinates(origin.posX + xx, origin.posY+branchLength, origin.posZ + zz), Blocks.log));
				}
			}
			
			
			if (branchLength == branchLengthMax) {
				System.out.println("tree hit max");
			}
		}
	}
	
	public void tickApplyWorldChanges() {
		//we need to know what blocks we can update... keep track of chunks loaded based on load / unload events?
		
		
		Iterator it = setChunksToTick.iterator();
		while (it.hasNext()) {
			ChunkCoordinates coords = (ChunkCoordinates) it.next();
			
			if (lookupChunkToBlockCoordsForPendingUpdate.containsKey(coords)) {
				if (lookupChunkToBlockCoordsForPendingUpdate.get(coords).size() > 0) {
					List<ChunkCoordinates> listCoords = lookupChunkToBlockCoordsForPendingUpdate.get(coords);
					Iterator itUpdates = listCoords.iterator();
					int updateCount = 0;
					while (itUpdates.hasNext() && updateCount++ < this.tickRateUpdateWorld) {
						ChunkCoordinates coordToProcess = (ChunkCoordinates) itUpdates.next();
						
						BlockDataEntry data = lookupDataAll.get(coordToProcess);
						if (data != null) {
							System.out.println("pushing live change, count for this tick: " + updateCount);
							pushLiveChange(data);
						} else {
							System.out.println("BlockDataEntry we wanted to update to world is null, design flaw?");
						}
						
						itUpdates.remove();
					}
				}
			}
		}
	}
	
	public void pushDataChange(BlockDataEntry data) {
		setData(data);
		
		//put coord of data into pending update for specific chunk coord
		List<ChunkCoordinates> listData = null;
		ChunkCoordinates chunkCoord = data.getCoordsForChunk();
		if (!lookupChunkToBlockCoordsForPendingUpdate.containsKey(chunkCoord)) {
			listData = new ArrayList<ChunkCoordinates>();
			lookupChunkToBlockCoordsForPendingUpdate.put(chunkCoord, listData);
		} else {
			listData = lookupChunkToBlockCoordsForPendingUpdate.get(chunkCoord);
		}
		
		//finally add the specific location we need to update to the list for that chunk
		listData.add(data.getCoords());
		
		if (!setChunksToTick.contains(chunkCoord)) {

			//need to know if chunk is active in a thread safe way before we can attempt to mark chunk to be ticked
			//TODO: put chunk exists requests through queue to server thread when that side is complete, for now risk a CME
			if (getWorld().blockExists(data.getCoords().posX, data.getCoords().posY, data.getCoords().posZ)) {
				System.out.println("marking chunk coord for ticking: " + chunkCoord);
				setChunksToTick.add(chunkCoord);
			}
		}
		
		/*if (canLiveUpdate()) {
			System.out.println("performing live change at " + data.getCoords());
			pushLiveChange(data);
		} else {
			System.out.println("performing pending change at " + data.getCoords());
			if (listPending.contains(data.getCoords())) {
				System.out.println("tried to add coord entry that exists already");
			} else {
				listPending.add(data.getCoords());
			}
		}*/
	}
	
	public void setData(BlockDataEntry data) {
		lookupDataAll.put(data.getCoords(), data);
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
		if (getWorld().getBlock(origin.posX, origin.posY, origin.posZ) != Blocks.log) {
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
	public void hookChunkLoad(Chunk chunk) {
		
		ChunkCoordinates chunkCoord = new ChunkCoordinates(chunk.xPosition, 0, chunk.zPosition);
		if (lookupChunkToBlockCoordsForPendingUpdate.containsKey(chunkCoord) && lookupChunkToBlockCoordsForPendingUpdate.get(chunkCoord).size() > 0) {
			if (!setChunksToTick.contains(chunkCoord)) {
				setChunksToTick.add(chunkCoord);
			}
		}
		
		/*Iterator it = listPending.iterator();
		while (it.hasNext()) {
			ChunkCoordinates coords = (ChunkCoordinates) it.next();
			//Map.Entry<ChunkCoordinates, BlockDataEntry> entry = (Entry<ChunkCoordinates, BlockDataEntry>) it.next();
			if (coords.posX / 16 == chunk.xPosition && coords.posZ / 16 == chunk.zPosition) {
				System.out.println("found a pending update for coord " + coords.toString());
				pushLiveChange(lookupDataAll.get(coords));
				
				it.remove();
				
			}
		}*/
	}
	
	public void hookChunkUnload(Chunk chunk) {
		
		ChunkCoordinates chunkCoord = new ChunkCoordinates(chunk.xPosition, 0, chunk.zPosition);
		if (setChunksToTick.contains(chunkCoord)) {
			setChunksToTick.remove(chunkCoord);
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
		
		NBTTagCompound nbtData = parData.getCompoundTag("simData");
		Iterator it = nbtData.func_150296_c().iterator();
		while (it.hasNext()) {
			String entryName = (String) it.next();
			NBTTagCompound nbtEntry = nbtData.getCompoundTag(entryName);
			
			BlockDataEntry entry = new BlockDataEntry();
			entry.readFromNBT(nbtEntry);
			
			setData(entry);
		}
		
		System.out.println("loaded tree origin as: " + origin.toString() + " with entry count: " + lookupDataAll.size());
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound parData) {
		
		parData.setString("classname", this.getClass().getCanonicalName());
		
		parData.setInteger("dimID", dimID);
		parData.setInteger("originX", origin.posX);
		parData.setInteger("originY", origin.posY);
		parData.setInteger("originZ", origin.posZ);
		

		NBTTagCompound nbtData = new NBTTagCompound();
		
		int i = 0;
		Iterator it = lookupDataAll.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<ChunkCoordinates, BlockDataEntry> entry = (Entry<ChunkCoordinates, BlockDataEntry>) it.next();
			nbtData.setTag("data_" + i++, entry.getValue().writeToNBT(new NBTTagCompound()));
		}
		
		parData.setTag("simData", nbtData);
		
		System.out.println("written out tree data");
		
		return parData;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	@Override
	public ChunkCoordinates getOrigin() {
		return origin;
	}

	@Override
	public boolean isThreaded() {
		return true;
	}

}
