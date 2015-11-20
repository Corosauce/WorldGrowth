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
	private Vector3f growthDirection = new Vector3f();
	private Vector3f growthDirectionInitial = new Vector3f();
	
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
		GrowthProfilePiece profile = getProfilePieceForLevel();
		
		//TODO: maths for inherit direction adjustment, maybe relocate this code to the location that initializes this class object
		//growthDirectionInitial = new Vector3f(parent.getGrowthDirection());
		float randAngle = (new Random()).nextFloat() * (float)Math.PI * 2F;
		float x = (float) Math.sin(randAngle);
		//float y = parent.getGrowthDirection().y;
		float y = profile.getGrowthDirectionVertical();
		float z = (float) Math.cos(randAngle);
		growthDirectionInitial = new Vector3f(x, y, z);
		growthDirection = new Vector3f(growthDirectionInitial);
	}
	
	public void initFromSimulation() {
		startCoord = tree.getOrigin();
		GrowthProfilePiece profile = getProfilePieceForLevel();
		
		growthDirectionInitial = new Vector3f(0, profile.getGrowthDirectionVertical(), 0);
		growthDirection = new Vector3f(growthDirectionInitial);
	}
	
	public void tick() {
		if (isActive()) {
			GrowthProfilePiece profile = getProfilePieceForLevel();
			growthLength += profile.getGrowthRate();
			
			System.out.println("growing to length: " + growthLength);
			
			ChunkCoordinates curCoord = getGrowthPosition();
			
			if (cachedCoord != null) {
				if (!cachedCoord.equals(curCoord)) {
					//detected at new block pos for main position, do some block updates
					System.out.println("new coord to place!");
					tree.pushDataChange(new BlockDataEntry(curCoord, profile.getBlockToPlace()));
					cachedCoord = curCoord;
				} else {
					
				}
			} else {
				cachedCoord = curCoord;
				tree.pushDataChange(new BlockDataEntry(curCoord, profile.getBlockToPlace()));
			}
			
			
			if (growthLength >= profile.getLength()) {
				System.out.println("reached end of growth");
				growthLength = profile.getLength();
				endOfGrowth();
			}
		}
	}
	
	public void endOfGrowth() {
		//TODO: child branch creation
		
		GrowthProfilePiece profile = getProfilePieceForLevel();
		
		for (int i = 0; i < profile.getChildBranchesToMake(); i++) {
			System.out.println("new node!");
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
		this.growthDirection = new Vector3f(nbt.getFloat("growthDirX"), nbt.getFloat("growthDirY"), nbt.getFloat("growthDirZ"));
		
		this.growthDirectionInitial = new Vector3f(nbt.getFloat("growthDirXInit"), nbt.getFloat("growthDirYInit"), nbt.getFloat("growthDirZInit"));
		
		Iterator it = nbt.getCompoundTag("nodes").func_150296_c().iterator();
		
		while (it.hasNext()) {
			String keyName = (String)it.next();
			NBTTagCompound nbtNode = nbt.getCompoundTag(keyName);
			
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
		
		nbt.setFloat("growthDirX", growthDirection.x);
		nbt.setFloat("growthDirY", growthDirection.y);
		nbt.setFloat("growthDirZ", growthDirection.z);
		
		nbt.setFloat("growthDirXInit", growthDirectionInitial.x);
		nbt.setFloat("growthDirYInit", growthDirectionInitial.y);
		nbt.setFloat("growthDirZInit", growthDirectionInitial.z);
		
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
		return new ChunkCoordinates(MathHelper.floor_float(startCoord.posX + growthDirection.x * growthLength), 
				MathHelper.floor_float(startCoord.posY + growthDirection.y * growthLength), 
				MathHelper.floor_float(startCoord.posZ + growthDirection.z * growthLength));
	}
	
	public Vector3f getGrowthDirection() {
		return growthDirection;
	}

	public void setGrowthDirection(Vector3f growthDirection) {
		this.growthDirection = growthDirection;
	}

	public Vector3f getGrowthDirectionInitial() {
		return growthDirectionInitial;
	}

	public void setGrowthDirectionInitial(Vector3f growthDirectionInitial) {
		this.growthDirectionInitial = growthDirectionInitial;
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
	
}
