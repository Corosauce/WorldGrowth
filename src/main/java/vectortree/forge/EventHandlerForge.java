package vectortree.forge;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.world.ChunkEvent;
import vectortree.simulation.SimulationBase;
import vectortree.simulation.tree.TreeSimulation;
import CoroUtil.world.WorldDirectorManager;
import CoroUtil.world.grid.block.BlockDataPoint;
import CoroUtil.world.location.ISimulationTickable;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EventHandlerForge {
	
	@SubscribeEvent
	public void entityTick(LivingUpdateEvent event) {
		EntityLivingBase ent = event.entityLiving;
		int walkOnRate = 5;
		
		if (!ent.worldObj.isRemote) {
			if (ent.worldObj.getTotalWorldTime() % walkOnRate == 0) {
				double speed = Math.sqrt(ent.motionX * ent.motionX + ent.motionY * ent.motionY + ent.motionZ * ent.motionZ);
				if (speed > 0.08) {
					//System.out.println(entityId + " - speed: " + speed);
					int newX = MathHelper.floor_double(ent.posX);
					int newY = MathHelper.floor_double(ent.boundingBox.minY - 1);
					int newZ = MathHelper.floor_double(ent.posZ);
					Block id = ent.worldObj.getBlock(newX, newY, newZ);
					
					//check for block that can have beaten path data
					
					if (id == Blocks.grass) {
						BlockDataPoint bdp = WorldDirectorManager.instance().getBlockDataGrid(ent.worldObj).getBlockData(newX, newY, newZ);// ServerTickHandler.wd.getBlockDataGrid(worldObj).getBlockData(newX, newY, newZ);
						
						//add depending on a weight?
						bdp.walkedOnAmount += 0.25F;
						
						//System.out.println("inc walk amount: " + bdp.walkedOnAmount);
						
						if (bdp.walkedOnAmount > 5F) {
							//System.out.println("dirt!!!");
							ent.worldObj.setBlock(newX, newY, newZ, Blocks.dirt);//BlockRegistry.dirtPath.blockID);
							//cleanup for memory
							WorldDirectorManager.instance().getBlockDataGrid(ent.worldObj).removeBlockData(newX, newY, newZ);
							//ServerTickHandler.wd.getBlockDataGrid(worldObj).removeBlockData(newX, newY, newZ);
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void chunkLoad(ChunkEvent.Load event) {
		//TODO: get list of ISimulationTickable to pass this event to more efficiently
		
		if (!event.world.isRemote) {
			for (ISimulationTickable ticker : WorldDirectorManager.instance().getCoroUtilWorldDirector(event.world).listTickingLocations) {
				if (ticker instanceof SimulationBase) {
					SimulationBase tree = (SimulationBase) ticker;
					//if (tree.getWorld().provider.dimensionId == event.world.provider.dimensionId) {
						//dont compare origin just pass the chunk thats loading, in future consider a 'chunk load listener' system for simulated trees to save having to do a full iteration
						//if (tree.getOrigin().posX / 16 == event.getChunk().xPosition && tree.getOrigin().posZ / 16 == event.getChunk().zPosition) {
							tree.hookChunkLoad(event.getChunk());
						//}
					//}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void chunkUnload(ChunkEvent.Unload event) {
		if (!event.world.isRemote) {
			for (ISimulationTickable ticker : WorldDirectorManager.instance().getCoroUtilWorldDirector(event.world).listTickingLocations) {
				if (ticker instanceof SimulationBase) {
					SimulationBase tree = (SimulationBase) ticker;
					//if (tree.getWorld().provider.dimensionId == event.world.provider.dimensionId) {
						tree.hookChunkUnload(event.getChunk());
					//}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void chunkGenPost(DecorateBiomeEvent.Post event) {
		
		if (true) return;
		
		//System.out.println("chunkgen: " + event.chunkX + " - " + event.chunkZ);
		
		World worldObj = event.world;
		int x = event.chunkX + 8;
		int z = event.chunkZ + 8;
		int y = worldObj.getTopSolidOrLiquidBlock(x, z);
		//System.out.println("material check: " + worldObj.getBlock(x, y, z).getMaterial());
		if (worldObj.getBlock(x, y, z).getMaterial() != Material.water/*== Material.grass*/) {
			worldObj.setBlock(x, y+1, z, Blocks.log);
	    	TreeSimulation sim = new TreeSimulation(worldObj.provider.dimensionId, new ChunkCoordinates(x, y+1, z));
	    	sim.init();
	    	sim.initPost();
	    	WorldDirectorManager.instance().getCoroUtilWorldDirector(worldObj).addTickingLocation(sim);
	    	//System.out.println("adding tree");
		}
	}
}
