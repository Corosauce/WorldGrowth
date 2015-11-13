package vectortree.simulation;

import net.minecraft.block.Block;
import net.minecraft.util.ChunkCoordinates;

/**
 * Used to store data about a particular coordinate
 * 
 * We could have maybe used BlockDataPoint from grid system, but would need a more elegant system to implement multiple types first, so lets use a custom class (for now?)
 * 
 * @author Corosus
 *
 */
public class BlockDataEntry {

	private ChunkCoordinates coords;
	private Block block;
	private int meta;
	
	public ChunkCoordinates getCoords() {
		return coords;
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

	public BlockDataEntry(ChunkCoordinates coords, Block block) {
		this.coords = coords;
		this.block = block;
	}
}
