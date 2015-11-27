package vectortree.simulation.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import CoroUtil.util.BlockCoord;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import vectortree.simulation.SimulationBase;

public class TreeSimulation extends SimulationBase {
	

	private GrowthNodeNew baseNode;
	private GrowthProfile profile;
	
	private List<BaseNode> listTickingGrowths =  new ArrayList<BaseNode>();
	
	public Random rand = new Random();

	public TreeSimulation() {
		
	}
	
	public TreeSimulation(int dimID, BlockCoord origin) {
		super(dimID, origin);
	}

	@Override
	public void init() {
		super.init();
		
		getWorldDirector().setSharedSimulationUpdateRateLimit(getSharedSimulationName(), 16);
		
		//TODO: replace with json
		initTestProfile();
		
		baseNode = new GrowthNodeNew(this, null, 0);
	}
	
	@Override
	public void initPost() {
		super.initPost();
		
		baseNode.initFromSimulation();
		addTickingGrowth(baseNode);
	}
	
	/**
	 * 
	 * All this will be definable in json files in future
	 * 
	 */
	public void initTestProfile() {
		profile = new GrowthProfile();
		profile.setLevels(3);
		
		
		
		GrowthProfile.GrowthProfilePiece piece = new GrowthProfile.GrowthProfilePiece();
		piece.setBlockToPlace(Blocks.log);
		piece.setThickness(3);
		piece.setLength(5);
		piece.setInheritDirectionFromParent(false);
		piece.setGrowthDirectionVertical(1);
		
		
		piece.setGrowthDirectionVarianceRandomRange(360);
		piece.setGrowthDirectionVarianceRandomRate(90);
		piece.setGrowthRateScaleHorizontal(0.3F);
		piece.setGrowthRate(1F);
		
		piece.setChildBranchesToMake(5);
		profile.setPiece(0, piece);
		
		
		
		piece = new GrowthProfile.GrowthProfilePiece();
		piece.setBlockToPlace(Blocks.log2);
		piece.setThickness(1);
		piece.setLength(10);
		piece.setInheritDirectionFromParent(true);
		
		piece.setInitialDirectionVariance(360);
		piece.setGrowthDirectionVarianceRandomRange(0);
		piece.setGrowthDirectionVarianceRandomRate(0);
		piece.setGrowthDirectionVertical(0.3F);
		piece.setGrowthRateScaleHorizontal(0.5F);
		piece.setGrowthRate(1F);
		
		piece.setChildBranchesToMake(2);
		profile.setPiece(1, piece);
		
		
		
		piece = new GrowthProfile.GrowthProfilePiece();
		piece.setBlockToPlace(Blocks.planks);
		piece.setThickness(1);
		piece.setLength(15);
		piece.setInheritDirectionFromParent(true);
		
		piece.setInitialDirectionVariance(360);
		piece.setGrowthDirectionVarianceRandomRange(0);
		piece.setGrowthDirectionVarianceRandomRate(0);
		piece.setGrowthDirectionVertical(0.7F);
		piece.setGrowthRateScaleHorizontal(1);
		piece.setGrowthRate(10F);
		
		piece.setChildBranchesToMake(0);
		profile.setPiece(2, piece);
	}
	
	@Override
	public void cleanup() {
		super.cleanup();
	}
	
	@Override
	public void tickSimulate() {
		
		super.tickSimulate();
		
		if (isActive()) {
			//int size = listTickingGrowths.size();
			for (int i = 0; i < listTickingGrowths.size(); i++) {
				BaseNode node = listTickingGrowths.get(i);
				if (node.isActive()) {
					node.tick();
				} else {
					listTickingGrowths.remove(i--);
				}
			}
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
		
		/*if (baseNode == null) {
			baseNode = new GrowthNodeNew(this, null, 0);
			baseNode.initFromSimulation();
			addTickingGrowth(baseNode);
		}*/
		
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
