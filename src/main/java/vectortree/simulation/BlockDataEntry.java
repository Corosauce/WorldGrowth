package vectortree.simulation;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
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

	private ChunkCoordinates coords;
	private Block block;
	private int meta;
	
	public BlockDataEntry() {
		
	}
	
	public BlockDataEntry(ChunkCoordinates coords, Block block) {
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
		nbt.setInteger("blockID", Block.getIdFromBlock(block));
		nbt.setInteger("meta", meta);
		
    	nbt.setInteger("xCoord", coords.posX);
    	nbt.setInteger("yCoord", coords.posY);
    	nbt.setInteger("zCoord", coords.posZ);
    	
    	return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		
		block = Block.getBlockById(nbt.getInteger("blockID"));
    	meta = nbt.getInteger("meta");
		
		coords = new ChunkCoordinates(nbt.getInteger("xCoord"), nbt.getInteger("yCoord"), nbt.getInteger("zCoord"));
	}
}
