package vectortree.simulation;

import CoroUtil.world.WorldDirectorManager;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

public class TreeSimulation extends SimulationBase {


	
	//quick test vars, to be located into a node tree system
	private int branchLength = 0;
	private int branchLengthMax = 50;
	
	public TreeSimulation() {
		
	}
	
	public TreeSimulation(int dimID, ChunkCoordinates origin) {
		super(dimID, origin);
	}

	@Override
	public void init() {
		super.init();
		
		getWorldDirector().setSharedSimulationUpdateRateLimit(getSharedSimulationName(), 16);		
	}
	
	@Override
	public void cleanup() {
		super.cleanup();
	}
	
	@Override
	public void tickSimulate() {
		super.tickSimulate();
		
		if (isActive()) {
			branchLength++;
			
			System.out.println("branchLength: " + branchLength);
			
			//push system harder for testing
			for (int xx = -10; xx <= 10; xx++) {
				for (int zz = -10; zz <= 10; zz++) {
					pushDataChange(new BlockDataEntry(new ChunkCoordinates(getOrigin().posX + xx, getOrigin().posY+branchLength, getOrigin().posZ + zz), Blocks.log));
				}
			}
			
			
			if (branchLength == branchLengthMax) {
				System.out.println("tree hit max");
			}
		}
	}
	
	public boolean isActive() {
		return branchLength < branchLengthMax;
	}
	
	@Override
	public boolean isValid() {
		if (super.isValid()) {
			//TODO: consider rewriting this validation to use cached data only
			if (!getWorld().blockExists(getOrigin().posX, getOrigin().posY, getOrigin().posZ)) return true;
			if (getWorld().getBlock(getOrigin().posX, getOrigin().posY, getOrigin().posZ) != Blocks.log) {
				return false;
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound parData) {

		branchLength = parData.getInteger("branchLength");
		
		super.readFromNBT(parData);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound parData) {
		
		parData.setInteger("branchLength", branchLength);
		
		return super.writeToNBT(parData);
	}
	
	@Override
	public String getSharedSimulationName() {
		return "vectortree";
	}
	
}
