package vectortree.simulation.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import vectortree.simulation.BlockDataEntry;
import vectortree.simulation.TreeSimulation;
import vectortree.simulation.tree.GrowthProfile.GrowthProfilePiece;
import CoroUtil.util.ISerializableNBT;
import CoroUtil.util.Vector3f;

/**
 * TODO: consider ways to define when along this branch we make the child branches, eg:
 * - evenly during the growth
 * - all at the end (doing this for now)
 * 
 * @author Corosus
 *
 */
public class GrowthNodeNew implements ISerializableNBT  {

	private TreeSimulation tree;
	private GrowthNodeNew parent;
	private int level;
	
	//runtime values 
	//private Vector3f growthDirection = new Vector3f();
	//private Vector3f growthDirectionInitial = new Vector3f();
	private int growthDirection;

	private int growthDirectionInitial;
	
	/**
	 * Not accurate, use getLength to determine real length
	 */
	private float growthLength;
	
	private List<GrowthNodeNew> listChildNodes = new ArrayList<GrowthNodeNew>();
	
	private ChunkCoordinates startCoord = null;
	private ChunkCoordinates cachedCoord = null;

	/**
	 * Use this or something better to monitor if parts should be actively ticked or not?
	 */
	private boolean isActive = true;
	
	public GrowthNodeNew(TreeSimulation tree, GrowthNodeNew parent, int level) {
		this.tree = tree;
		this.parent = parent;
		this.level = level;
	}
	
	public void initFromParent() {
		startCoord = new ChunkCoordinates(parent.getGrowthPosition());
		/*GrowthProfilePiece profile = getProfilePieceForLevel();
		
		//TODO: maths for inherit direction adjustment, maybe relocate this code to the location that initializes this class object
		//growthDirectionInitial = new Vector3f(parent.getGrowthDirection());
		float randAngle = (new Random()).nextFloat() * (float)Math.PI * 2F;
		float x = (float) Math.sin(randAngle);
		//float y = parent.getGrowthDirection().y;
		float y = profile.getGrowthDirectionVertical();
		float z = (float) Math.cos(randAngle);
		growthDirectionInitial = new Vector3f(x, y, z);
		growthDirection = new Vector3f(growthDirectionInitial);*/
		
		initGeneric();
	}
	
	public void initFromSimulation() {
		startCoord = tree.getOrigin();
		
		initGeneric();
	}
	
	public void initGeneric() {

		GrowthProfilePiece profile = getProfilePieceForLevel();
		
		Random rand = tree.rand;//new Random();
		
		if (profile.isInheritDirectionFromParent() && parent != null) {
			//switch to angles for directional stuff, use random with that, no having to fix values afterwards
			growthDirectionInitial = parent.growthDirection;
			if (profile.getInitialDirectionVariance() > 0) {
				growthDirectionInitial += rand.nextInt(profile.getInitialDirectionVariance()) - rand.nextInt(profile.getInitialDirectionVariance());
			}
		} else {
			growthDirectionInitial = rand.nextInt(360);
		}
		
		//float x = (rand.nextFloat() - rand.nextFloat()) * Math.min(profile.getGrowthDirectionVarianceRandomRate(), profile.getGrowthDirectionVarianceRandomRange());
		//float z = (rand.nextFloat() - rand.nextFloat()) * Math.min(profile.getGrowthDirectionVarianceRandomRate(), profile.getGrowthDirectionVarianceRandomRange());
		
		
		
		//growthDirectionInitial = new Vector3f(x, profile.getGrowthDirectionVertical(), z);
		growthDirection = growthDirectionInitial;
	}
	
	public void tick() {
		if (isActive()) {
			GrowthProfilePiece profile = getProfilePieceForLevel();
			
			//TODO: recalculate how isActive plays into length, we need to get the real length now since this one is scaled down due to horiz and vert scaling
			growthLength += profile.getGrowthRate();
			
			Random rand = new Random();
			
			//update random direction change and clamp within allowed variance of initial direction
			if (profile.getGrowthDirectionVarianceRandomRate() > 0) {
				growthDirection += rand.nextInt(profile.getGrowthDirectionVarianceRandomRate()) - rand.nextInt(profile.getGrowthDirectionVarianceRandomRate());
				if (growthDirection > growthDirectionInitial + profile.getGrowthDirectionVarianceRandomRange()) {
					growthDirection = growthDirectionInitial + profile.getGrowthDirectionVarianceRandomRange();
				} else if (growthDirection < growthDirectionInitial - profile.getGrowthDirectionVarianceRandomRange()) {
					growthDirection = growthDirectionInitial - profile.getGrowthDirectionVarianceRandomRange();
				}
			}
			
			//System.out.println("growing to length: " + growthLength);
			
			ChunkCoordinates curCoord = getGrowthPosition();
			
			if (cachedCoord != null) {
				if (!cachedCoord.equals(curCoord)) {
					//detected at new block pos for main position, do some block updates
					//System.out.println("new coord to place!");
					tree.pushDataChange(new BlockDataEntry(curCoord, profile.getBlockToPlace()));
					cachedCoord = curCoord;
				} else {
					
				}
			} else {
				cachedCoord = curCoord;
				tree.pushDataChange(new BlockDataEntry(curCoord, profile.getBlockToPlace()));
			}
			
			
			if (getLength() >= profile.getLength()) {
				//System.out.println("reached end of growth");
				//growthLength = profile.getLength();
				endOfGrowth();
			}
		}
	}
	
	public double getLength() {
		double x1 = this.startCoord.posX + 0.5D;
		double y1 = this.startCoord.posY + 0.5D;
		double z1 = this.startCoord.posZ + 0.5D;
		
		ChunkCoordinates coords = this.getGrowthPosition();
		
		double x2 = coords.posX + 0.5D;
		double y2 = coords.posY + 0.5D;
		double z2 = coords.posZ + 0.5D;
		
		double x3 = x1 - x2;
		double y3 = y1 - y2;
		double z3 = z1 - z2;
		
		return Math.sqrt(x3 * x3 + y3 * y3 + z3 * z3);
	}
	
	public void endOfGrowth() {
		//TODO: child branch creation
		
		GrowthProfilePiece profile = getProfilePieceForLevel();
		
		for (int i = 0; i < profile.getChildBranchesToMake(); i++) {
			//System.out.println("new node!");
			newChildNode(true);
		}
		
		setActive(false);
	}
	
	public GrowthNodeNew newChildNode(boolean isActive) {
		GrowthNodeNew newNode = new GrowthNodeNew(tree, this, level+1);
		newNode.initFromParent();
		listChildNodes.add(newNode);
		if (isActive) tree.addTickingGrowth(newNode);
		return newNode;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		
		this.level = nbt.getInteger("level");
		this.startCoord = new ChunkCoordinates(nbt.getInteger("startCoordX"), nbt.getInteger("startCoordY"), nbt.getInteger("startCoordZ"));
		this.isActive = nbt.getBoolean("isActive");
		
		this.growthLength = nbt.getFloat("growthLength");
		
		this.growthDirection = nbt.getInteger("growthDir");
		this.growthDirectionInitial = nbt.getInteger("growthDirInit");//, nbt.getFloat("growthDirYInit"), nbt.getFloat("growthDirZInit"));
		
		//System.out.println("loaded in branch of level: " + level);
		
		NBTTagCompound nodes = nbt.getCompoundTag("nodes");
		Iterator it = nodes.func_150296_c().iterator();
		
		while (it.hasNext()) {
			String keyName = (String)it.next();
			NBTTagCompound nbtNode = nodes.getCompoundTag(keyName);
			
			GrowthNodeNew node = newChildNode(nbtNode.getBoolean("isActive"));
			node.readFromNBT(nbtNode);
		}
		
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		
		nbt.setInteger("level", level);
		nbt.setInteger("startCoordX", this.startCoord.posX);
		nbt.setInteger("startCoordY", this.startCoord.posY);
		nbt.setInteger("startCoordZ", this.startCoord.posZ);
		
		nbt.setBoolean("isActive", isActive);
		
		nbt.setFloat("growthLength", growthLength);
		
		nbt.setInteger("growthDir", growthDirection);
		//nbt.setFloat("growthDirY", growthDirection.y);
		//nbt.setFloat("growthDirZ", growthDirection.z);
		
		nbt.setInteger("growthDirInit", growthDirectionInitial);
		//nbt.setFloat("growthDirYInit", growthDirectionInitial.y);
		//nbt.setFloat("growthDirZInit", growthDirectionInitial.z);
		
		NBTTagCompound nbtNodes = new NBTTagCompound();
		
		int nodeIndex = 0;
		for (GrowthNodeNew node : listChildNodes) {
			NBTTagCompound nbtNode = node.writeToNBT(new NBTTagCompound());
			
			nbtNodes.setTag("node_" + nodeIndex++, nbtNode);
		}
		
		nbt.setTag("nodes", nbtNodes);
		
		return nbt;
	}
	
	public GrowthProfilePiece getProfilePieceForLevel() {
		return tree.getProfile().getProfileForLevel(level);
	}
	
	public ChunkCoordinates getGrowthPosition() {
		
		GrowthProfilePiece profile = getProfilePieceForLevel();
		
		//TODO: verify accurate use of sin/cos for x/z, if it even matters
		double x = (Math.sin(Math.toRadians(growthDirection)) * growthLength * profile.getGrowthRateScaleHorizontal());
		double z = (Math.cos(Math.toRadians(growthDirection)) * growthLength * profile.getGrowthRateScaleHorizontal());
		double y = (growthLength * profile.getGrowthDirectionVertical()); 
		return new ChunkCoordinates(MathHelper.floor_double(startCoord.posX + x), 
				MathHelper.floor_double(startCoord.posY + y), 
				MathHelper.floor_double(startCoord.posZ + z));
	}

	public float getGrowthLength() {
		return growthLength;
	}

	public void setGrowthLength(float growthLength) {
		this.growthLength = growthLength;
	}

	public ChunkCoordinates getStartCoord() {
		return startCoord;
	}

	public void setStartCoord(ChunkCoordinates startCoord) {
		this.startCoord = startCoord;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	public int getGrowthDirection() {
		return growthDirection;
	}

	public void setGrowthDirection(int growthDirection) {
		this.growthDirection = growthDirection;
	}

	public int getGrowthDirectionInitial() {
		return growthDirectionInitial;
	}

	public void setGrowthDirectionInitial(int growthDirectionInitial) {
		this.growthDirectionInitial = growthDirectionInitial;
	}
	
}
