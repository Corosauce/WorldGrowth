package vectortree.simulation;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import vectortree.simulation.tree.GrowthNodeNew;
import vectortree.simulation.tree.GrowthProfile;
import vectortree.simulation.tree.GrowthProfile.GrowthProfilePiece;

public class TreeSimulation extends SimulationBase {
	
	//TODO: init / nbt load order is a mess, clean it up

	private GrowthNodeNew baseNode;
	private GrowthProfile profile;
	
	private List<GrowthNodeNew> listTickingGrowths =  new ArrayList<GrowthNodeNew>();

	public TreeSimulation() {
		
	}
	
	public TreeSimulation(int dimID, ChunkCoordinates origin) {
		super(dimID, origin);
	}

	@Override
	public void init() {
		super.init();
		
		getWorldDirector().setSharedSimulationUpdateRateLimit(getSharedSimulationName(), 16);
		
		//TODO: replace with json
		initTestProfile();
		
		if (baseNode == null) {
			baseNode = new GrowthNodeNew(this, null, 0);
			baseNode.initFromSimulation();
			addTickingGrowth(baseNode);
		}
	}
	
	/**
	 * 
	 * All this will be definable in json files in future
	 * 
	 */
	public void initTestProfile() {
		profile = new GrowthProfile();
		profile.setLevels(3);
		
		GrowthProfilePiece piece = new GrowthProfilePiece();
		piece.setBlockToPlace(Blocks.log);
		piece.setThickness(3);
		piece.setLength(5);
		piece.setInheritDirectionAmount(0);
		piece.setGrowthDirectionVertical(1);
		piece.setChildBranchesToMake(5);
		piece.setGrowthRate(0.9F);
		profile.setPiece(0, piece);
		
		piece = new GrowthProfilePiece();
		piece.setBlockToPlace(Blocks.log);
		piece.setThickness(1);
		piece.setLength(8);
		piece.setInheritDirectionAmount(0);
		piece.setGrowthDirectionVertical(0);
		piece.setChildBranchesToMake(2);
		piece.setGrowthRate(0.3F);
		profile.setPiece(1, piece);
		
		piece = new GrowthProfilePiece();
		piece.setBlockToPlace(Blocks.log);
		piece.setThickness(1);
		piece.setLength(5);
		piece.setInheritDirectionAmount(0);
		piece.setGrowthDirectionVertical(-0.5F);
		piece.setChildBranchesToMake(0);
		piece.setGrowthRate(0.3F);
		profile.setPiece(2, piece);
	}
	
	@Override
	public void cleanup() {
		super.cleanup();
	}
	
	@Override
	public void tickSimulate() {
		
		//if (!hasInit) return;
		
		super.tickSimulate();
		
		if (isActive()) {
			//int size = listTickingGrowths.size();
			for (int i = 0; i < listTickingGrowths.size(); i++) {
				GrowthNodeNew node = listTickingGrowths.get(i);
				if (node.isActive()) {
					node.tick();
				} else {
					listTickingGrowths.remove(i--);
				}
			}
			/*for (GrowthNodeNew node : listTickingGrowths) {
				node.tick();
			}*/
			/*branchLength++;
			
			System.out.println("branchLength: " + branchLength);
			
			//push system harder for testing
			for (int xx = -10; xx <= 10; xx++) {
				for (int zz = -10; zz <= 10; zz++) {
					pushDataChange(new BlockDataEntry(new ChunkCoordinates(getOrigin().posX + xx, getOrigin().posY+branchLength, getOrigin().posZ + zz), Blocks.log));
				}
			}
			
			
			if (branchLength == branchLengthMax) {
				System.out.println("tree hit max");
			}*/
		}
	}
	
	public boolean isActive() {
		return true;//branchLength < branchLengthMax;
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
		
		if (baseNode == null) {
			baseNode = new GrowthNodeNew(this, null, 0);
			baseNode.initFromSimulation();
			addTickingGrowth(baseNode);
		}
		
		baseNode.readFromNBT(parData.getCompoundTag("baseNode"));
		
		super.readFromNBT(parData);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound parData) {
		
		parData.setTag("baseNode", baseNode.writeToNBT(new NBTTagCompound()));
		
		return super.writeToNBT(parData);
	}
	
	@Override
	public String getSharedSimulationName() {
		return "vectortree";
	}
	
	public GrowthProfile getProfile() {
		return profile;
	}

	public void setProfile(GrowthProfile profile) {
		this.profile = profile;
	}
	
	public GrowthNodeNew getBaseNode() {
		return baseNode;
	}

	public void setBaseNode(GrowthNodeNew baseNode) {
		this.baseNode = baseNode;
	}
	
	public void addTickingGrowth(GrowthNodeNew node) {
		listTickingGrowths.add(node);
	}
	
}
