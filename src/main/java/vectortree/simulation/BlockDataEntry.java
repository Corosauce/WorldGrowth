package vectortree.simulation;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import CoroUtil.util.ISerializableNBT;

/**
 * Used to store data about a particular coordinate
 * 
 * We could have maybe used BlockDataPoint from grid system, but would need a more elegant system to implement multiple types first, so lets use a custom class (for now?)
 * 
 * @author Corosus
 *
 */
public class BlockDataEntry implements ISerializableNBT {

	private int dimID;
	private ChunkCoordinates coords;
	private Block block;
	private int meta;
	
	/**
	 * Mostly temporary, to be used until we have data query system in place
	 * We shouldnt use this since we'd want more intelligent adaptation to something in the way of growing against solid areas
	 */
	private boolean checkForAirWaterBeforePlace = true;
	
	public BlockDataEntry() {
		
	}
	
	public BlockDataEntry(int dimID, ChunkCoordinates coords, Block block) {
		this.coords = coords;
		this.block = block;
	}
	
	public ChunkCoordinates getCoords() {
		return coords;
	}
	
	public ChunkCoordinates getCoordsForChunk() {
		return new ChunkCoordinates(coords.posX / 16, 0, coords.posZ / 16);
	}

	public void setCoords(ChunkCoordinates coords) {
		this.coords = coords;
	}

	public Block getBlock() {
		return block;
	}

	public void setBlock(Block block) {
		this.block = block;
	}

	public int getMeta() {
		return meta;
	}

	public void setMeta(int meta) {
		this.meta = meta;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("dimID", dimID);
		nbt.setInteger("blockID", Block.getIdFromBlock(block));
		nbt.setInteger("meta", meta);
		
    	nbt.setInteger("xCoord", coords.posX);
    	nbt.setInteger("yCoord", coords.posY);
    	nbt.setInteger("zCoord", coords.posZ);
    	
    	return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		
		dimID = nbt.getInteger("dimID");
		block = Block.getBlockById(nbt.getInteger("blockID"));
    	meta = nbt.getInteger("meta");
		
		coords = new ChunkCoordinates(nbt.getInteger("xCoord"), nbt.getInteger("yCoord"), nbt.getInteger("zCoord"));
	}
	
	public boolean performOperation() {
		World world = DimensionManager.getWorld(dimID);
		
		if (world != null) {
			if (!checkForAirWaterBeforePlace) {
				world.setBlock(getCoords().posX, getCoords().posY, getCoords().posZ, getBlock(), getMeta(), 3);
			} else {
				Block block = world.getBlock(getCoords().posX, getCoords().posY, getCoords().posZ);
				if (block == Blocks.air || block.getMaterial() == Material.water) {
					world.setBlock(getCoords().posX, getCoords().posY, getCoords().posZ, getBlock(), getMeta(), 3);
				}
			}
		} else {
			System.out.println("warning! couldnt get world for dimID: " + dimID);
		}
		
		return true;
	}
}
