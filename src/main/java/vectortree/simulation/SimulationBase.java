package vectortree.simulation;

import io.netty.util.internal.ConcurrentSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import CoroUtil.util.ISerializableNBT;
import CoroUtil.world.WorldDirector;
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
public class SimulationBase implements ISimulationTickable, ISerializableNBT {
	
	private int dimID = 0;
	private ChunkCoordinates origin = null;
	
	private int tickRateSimulation = 20;
	private int tickRateUpdateWorld = 1;
	//private int blocksPerUpdateWorldTick = 20;
	
	//stores data about location for all touched coords
	//touched by: MC, THREAD
	private ConcurrentHashMap<ChunkCoordinates, BlockDataEntry> lookupDataAll = new ConcurrentHashMap<ChunkCoordinates, BlockDataEntry>();
	//stores data about unupdated location pending changing once chunk loads
	
	//for organizing pending updates based on the chunk its in
	//touched by: MC, THREAD
	private ConcurrentHashMap<ChunkCoordinates, Set<ChunkCoordinates>> lookupChunkToBlockCoordsForPendingUpdate = new ConcurrentHashMap<ChunkCoordinates, Set<ChunkCoordinates>>();
	
	//list of loaded chunks we need to tick for updates while we can, used for lookupChunkToBlockCoordsForPendingUpdate
	//this list is maintained by chunk load and unload events and if that chunk is in the lookup for pending updates...
	//touched by: MC, THREAD?
	private ConcurrentSet<ChunkCoordinates> setChunksToTick = new ConcurrentSet<ChunkCoordinates>();
	
	//protected boolean hasInit = false;
	
	public SimulationBase() {
		//needed for generic init
	}
	
	public SimulationBase(int dimID, ChunkCoordinates origin) {
		this.dimID = dimID;
		this.origin = origin;
	}
	
	@Override
	public void tickUpdate() {
		/*if (!hasInit) {
			init();
			hasInit = true;
		}*/
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
		
	}
	
	public void tickApplyWorldChanges() {
		
		int curUpdateAmount = getWorldDirector().getSharedSimulationUpdateRateCurrent(getSharedSimulationName());
		int curUpdateLimit = getWorldDirector().getSharedSimulationUpdateRateLimit(getSharedSimulationName());
		
		if (curUpdateAmount < curUpdateLimit) {
			Set<ChunkCoordinates> listCoordsChunkToRemove = new HashSet<ChunkCoordinates>();
			
			if (setChunksToTick.size() > 0) {
				System.out.println("ticking chunks size: " + setChunksToTick.size());
			}
			
			Iterator it = setChunksToTick.iterator();
			while (it.hasNext()) {
				ChunkCoordinates coords = (ChunkCoordinates) it.next();
				
				if (lookupChunkToBlockCoordsForPendingUpdate.containsKey(coords)) {
					if (lookupChunkToBlockCoordsForPendingUpdate.get(coords).size() > 0) {
						Set<ChunkCoordinates> listCoords = lookupChunkToBlockCoordsForPendingUpdate.get(coords);
						Iterator itUpdates = listCoords.iterator();
						//int updateCount = 0;
						
						Set<ChunkCoordinates> listCoordsBlockToRemove = new HashSet<ChunkCoordinates>();
						
						while (itUpdates.hasNext() && curUpdateAmount++ < curUpdateLimit) {
							ChunkCoordinates coordToProcess = (ChunkCoordinates) itUpdates.next();
							
							BlockDataEntry data = lookupDataAll.get(coordToProcess);
							if (data != null) {
								//System.out.println("pushing live change, count for this tick: " + updateCount);
								pushLiveChange(data);
							} else {
								System.out.println("BlockDataEntry we wanted to update to world is null, design flaw?");
							}
							
							listCoordsBlockToRemove.add(coordToProcess);
							//itUpdates.remove();
						}
						
						//remove from master block coord list
						listCoords.removeAll(listCoordsBlockToRemove);
						
						//if final count is 0 for this chunk
						if (listCoords.size() == 0) {
							//remove this chunk from active tick update
							listCoordsChunkToRemove.add(coords);
						}
					}
				}
			}
		
			if (setChunksToTick.size() > 0) {
				setChunksToTick.removeAll(listCoordsChunkToRemove);
			}
		}
		
		getWorldDirector().setSharedSimulationUpdateRateCurrent(getSharedSimulationName(), curUpdateAmount);
	}
	
	public void pushDataChange(BlockDataEntry data) {
		setData(data);
		
		//put coord of data into pending update for specific chunk coord
		Set<ChunkCoordinates> listData = null;
		ChunkCoordinates chunkCoord = data.getCoordsForChunk();
		if (!lookupChunkToBlockCoordsForPendingUpdate.containsKey(chunkCoord)) {
			listData = Collections.newSetFromMap(new ConcurrentHashMap<ChunkCoordinates, Boolean>());
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
				//System.out.println("marking chunk coord for ticking: " + chunkCoord);
				setChunksToTick.add(chunkCoord);
			}
		}
	}
	
	public void setData(BlockDataEntry data) {
		lookupDataAll.put(data.getCoords(), data);
	}
	
	public void pushLiveChange(BlockDataEntry data) {
		getWorld().setBlock(data.getCoords().posX, data.getCoords().posY, data.getCoords().posZ, data.getBlock(), data.getMeta(), 3);
	}
	
	public boolean isValid() {
		
		
		return true;
	}
	
	/**
	 * For updating block data when a chunk loads up
	 * 
	 */
	public void hookChunkLoad(Chunk chunk) {
		
		//TODO: mass process a chunk if its loaded and wasnt active, client wise this would be more efficient, so all block changes happen and are sent in 1 network packet for the chunk
		//TODO: consider going more low level to apply block updates to skip some pointless overhead, maybe consider notify flag that doesnt notify neighbors, this might cause issues though
		
		ChunkCoordinates chunkCoord = new ChunkCoordinates(chunk.xPosition, 0, chunk.zPosition);
		if (lookupChunkToBlockCoordsForPendingUpdate.containsKey(chunkCoord) && lookupChunkToBlockCoordsForPendingUpdate.get(chunkCoord).size() > 0) {
			if (!setChunksToTick.contains(chunkCoord)) {
				setChunksToTick.add(chunkCoord);
			}
		}
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
	
	public WorldDirector getWorldDirector() {
		return WorldDirectorManager.instance().getCoroUtilWorldDirector(getWorld());
	}
	
	public void destroy() {
		getWorldDirector().removeTickingLocation(this);
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
		
		//read in pending updates in block coord form
		//call pushDataChange to re-categorize them to chunk->block (which calls setData again which is redundant, but no harm in that)
		NBTTagCompound nbtDataPending = parData.getCompoundTag("simDataPending");
		it = nbtDataPending.func_150296_c().iterator();
		while (it.hasNext()) {
			String entryName = (String) it.next();
			NBTTagCompound nbtEntry = nbtDataPending.getCompoundTag(entryName);
			
			ChunkCoordinates coords = new ChunkCoordinates(nbtEntry.getInteger("coordX"), nbtEntry.getInteger("coordY"), nbtEntry.getInteger("coordZ"));
			
			BlockDataEntry entry = lookupDataAll.get(coords);
			
			if (entry != null) {
				pushDataChange(entry);
			} else {
				System.out.println("BlockDataEntry null on read, design flaw");
			}
			
		}
		
		System.out.println("loaded tree origin as: " + origin.toString() + " with entry count: " + lookupDataAll.size() + " pending changes loaded chunk count: " + lookupChunkToBlockCoordsForPendingUpdate.size());
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
		
		NBTTagCompound nbtDataPending = new NBTTagCompound();
		i = 0;
		it = lookupChunkToBlockCoordsForPendingUpdate.values().iterator();
		while (it.hasNext()) {
			Iterator it2 = ((Set)it.next()).iterator();
			while (it2.hasNext()) {
				ChunkCoordinates coords = (ChunkCoordinates) it2.next();
				NBTTagCompound entry = new NBTTagCompound();
				entry.setInteger("coordX", coords.posX);
				entry.setInteger("coordY", coords.posY);
				entry.setInteger("coordZ", coords.posZ);
				nbtDataPending.setTag("data_" + i++, entry);
			}
		}
		
		parData.setTag("simDataPending", nbtDataPending);
		
		//System.out.println("written out tree data");
		
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

	@Override
	public String getSharedSimulationName() {
		return "";
	}

	@Override
	public void init() {
		
	}
	
	@Override
	public void initPost() {
		
	}

}
