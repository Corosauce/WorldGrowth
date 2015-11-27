package vectortree.simulation.tree;

import net.minecraft.nbt.NBTTagCompound;
import vectortree.simulation.INodeTickable;
import CoroUtil.util.ISerializableNBT;

public class BaseNode implements ISerializableNBT, INodeTickable {

	@Override
	public void tick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		// TODO Auto-generated method stub
		return nbt;
	}

}
