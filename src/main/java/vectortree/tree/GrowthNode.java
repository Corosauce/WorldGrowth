package vectortree.tree;

import java.util.ArrayList;
import java.util.Random;

import CoroUtil.util.CoroUtilBlock;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class GrowthNode {

	public GrowthManager growMan;
	
	//Counters
	public int type = 0;
	public int childIteration = 0;
	public int age = 0;
	public int stageCur = 0;
	
	//Internally used
	public Vec3 curPos;
	public Vec3 curDir;
	public ArrayList<Vec3> growthPoints; //a sort of growth waypoint, for making rigid branches
	public double growthSpeed = 0.1F;
	
	//User configurables
	public double spreadRange = ConfigTrees.branchInitialSpreadRangeXZMax;
	public double distBetweenPoints = ConfigTrees.branchDistBetweenSubPieces;
	public double growthRate = ConfigTrees.growSpeed;
	public int stageMax = 5;//ConfigTrees.branchSubPieces;
	public int childIterationMax = 1;//ConfigTrees.branchGenerationMax;
	
	public Block idBranch = Blocks.log;//ConfigTrees.blockIDBranch;
	public Block idLeaf = Blocks.leaves;
	
	//Keep like this or make own class, how would leafs slowly grow?
	//public ArrayList<Vec3> leafPos;
	//public ArrayList<Vec3> leafDir;
	
	public GrowthNode parentNode;
	public ArrayList<GrowthNode> childNodes;
	
	public GrowthNode(GrowthManager parGrowMan, GrowthNode parParent) {
		growMan = parGrowMan;
		childNodes = new ArrayList<GrowthNode>();
		parentNode = parParent;
		
		growthPoints = new ArrayList<Vec3>();
		
		if (parentNode != null) {
			childIteration = parentNode.childIteration + 1;
			
			System.out.println("new node with childIteration: " + childIteration);
			
			curPos = parentNode.curPos;
			//curDir = parentNode.curDir; //needed?
			Random rand = new Random();
			//curDir = Vec3.createVectorHelper(rand.nextDouble() - rand.nextDouble(), rand.nextDouble()/* - rand.nextDouble()*/, rand.nextDouble() - rand.nextDouble()).normalize();
			curDir = parentNode.curDir;
			//curDir.yCoord = 0.3F;
			double range = spreadRange;
			/*curDir.rotateAroundX((rand.nextFloat() * range) - (rand.nextFloat() * range));
			curDir.rotateAroundY((rand.nextFloat() * range) - (rand.nextFloat() * range));
			curDir.rotateAroundZ((rand.nextFloat() * range) - (rand.nextFloat() * range));*/
			curDir.xCoord += (rand.nextFloat() * range) - (rand.nextFloat() * range);
			curDir.zCoord += (rand.nextFloat() * range) - (rand.nextFloat() * range);
			
			curDir = Vec3.createVectorHelper((rand.nextDouble() * range) - (rand.nextDouble() * range), ConfigTrees.branchInitialSpreadRangeY + rand.nextDouble() * ConfigTrees.branchInitialSpreadRangeY/* - rand.nextDouble()*/, (rand.nextDouble() * range) - (rand.nextDouble() * range)).normalize();
			
			growthPoints.add(curPos);
		} else {
			curDir = Vec3.createVectorHelper(0, 1, 0);
		}
	}
	
	public void tickGrowth(boolean recursive) {
		
		age++; //??
		
		double rate = growthRate;
		
		if (rate < 1D) {
			if (age % (int)(1 / (rate + 0.0001D)) == 0) {
				grow();
			}
		} else {
			for (int i = 0; i < rate; i++) {
				grow();
			}
		}
		
		/*if (age % 1 / rate == 0) {
			grow();
		}*/
		
		if (recursive) {
			for (int i = 0; i < childNodes.size(); i++) {
				GrowthNode gn = childNodes.get(i);
				//if (gn.isActive()) {
					gn.tickGrowth(recursive);
				//}
			}
		}
	}
	
	public void grow() {
		Random rand = new Random();
		//move in a direction
		Vec3 origPos = curPos;
		//boolean keepTry = true;
		int tryCount = 0;
		curPos = curPos.addVector(curDir.xCoord * growthSpeed, (curDir.yCoord * growthSpeed), curDir.zCoord * growthSpeed);
		
		int bestLightVal = 0;
		Vec3 bestLightPos = null;
		Vec3 bestLightRealPos = null;
		Vec3 bestLightDir = null;
		Vec3 peekPos = curPos;
		
		int maxTries = 8;
		
		while (/*keepTry == true && */tryCount++ < maxTries) {
			
			Block id = growMan.worldObj.getBlock((int)peekPos.xCoord, (int)peekPos.yCoord, (int)peekPos.zCoord);
			
			if (CoroUtilBlock.isAir(id)) {
				int lightVal = growMan.worldObj.getBlockLightValue((int)peekPos.xCoord, (int)peekPos.yCoord, (int)peekPos.zCoord);
				//System.out.println("lightVal: " + lightVal);
				if (lightVal > 10) {
					break;
				} else {
					if (lightVal > 4 && lightVal > bestLightVal) {
						bestLightVal = lightVal;
						bestLightPos = peekPos;
						bestLightRealPos = curPos;
						bestLightDir = curDir;
					}
				}
			} else {
				if (id.getMaterial() == Material.wood || id.getMaterial() == Material.leaves || id == idBranch || id == idLeaf) {
					//it is in itself, let it grow
					//keepTry = false;
					break;
				} else {
					//hit something else, revert 
					
				}
			}
			
			double range = spreadRange;
			
			//grow up, else, super random
			if (bestLightVal > 2) {
				//curDir = Vec3.createVectorHelper(rand.nextDouble() - rand.nextDouble(), 0.3 + rand.nextDouble() * 0.3/* - rand.nextDouble()*/, rand.nextDouble() - rand.nextDouble()).normalize();
				curDir = Vec3.createVectorHelper((rand.nextDouble() * range) - (rand.nextDouble() * range), ConfigTrees.branchInitialSpreadRangeY + rand.nextDouble() * ConfigTrees.branchInitialSpreadRangeY/* - rand.nextDouble()*/, (rand.nextDouble() * range) - (rand.nextDouble() * range)).normalize();
			} else {
				curDir = Vec3.createVectorHelper(rand.nextDouble() - rand.nextDouble(), rand.nextDouble() - rand.nextDouble()/* - rand.nextDouble()*/, rand.nextDouble() - rand.nextDouble()).normalize();
			}
			curPos = curPos.addVector(curDir.xCoord * growthSpeed, curDir.yCoord * growthSpeed, curDir.zCoord * growthSpeed);
			double peekDist = 1.5D;
			peekPos = curPos.addVector(curDir.xCoord * peekDist, curDir.yCoord * peekDist, curDir.zCoord * peekDist);
		}
		
		if (tryCount >= maxTries) {
			//fail, go in best light direction and hope
			if (bestLightRealPos != null) {
				curPos = bestLightRealPos;
				curDir = bestLightDir;
			} else {
				//absolute fail, return for now, hope for light later?, needs more throttle
				curPos = origPos;
				return;
			}
			//stageCur = stageMax;
			//return;
		} else {
			
		}
		
		boolean lastTickForStage = curPos.distanceTo(growthPoints.get(growthPoints.size()-1)) > distBetweenPoints - (childIteration * ConfigTrees.branchGenerationShortenRate);
		
		if (CoroUtilBlock.isAir(growMan.worldObj.getBlock(MathHelper.floor_double(curPos.xCoord), MathHelper.floor_double(curPos.yCoord), MathHelper.floor_double(curPos.zCoord)))) {
			//tickLeafPlace(lastTickForStage);
			tickBranchPlace();
		}
		
		if (lastTickForStage) {
		//if (age > stageCur * stageTicksBetween) {
			growthPoints.add(curPos);
			
			int chance = 1+(childIteration);
			
			if (childIteration % ConfigTrees.branchNewBranchRate == 0 && /*stageCur > 0 && stageCur < stageMax - 1 && */childIteration < childIterationMax) {
				//System.out.println(this + " - new node");
				newNode();
			}
			//change direction a little bit
			float range = 0.9F;
			//curDir.xCoord += (rand.nextFloat() * range) - (rand.nextFloat() * range);
			//curDir.zCoord += (rand.nextFloat() * range) - (rand.nextFloat() * range);
			nextStage();
		}

		//if (age > stageCur * stageTicksBetween) nextStage();
	}
	
	public void tickBranchPlace() {
		//extra loop code here to make a bigger trunk
		if (childIteration == 0) {
			for (double i = 0; i < 360; i+=45) {
				double rad = Math.PI/180F;
				double dist = 1.0D;
				double droopStart = 999;
				//for (double dist = 1D; dist < maxDist; dist++) {
					double aimX = curPos.xCoord + (Math.sin(i * rad) * dist);
					double aimZ = curPos.zCoord + (Math.cos(i * rad) * dist);
					double aimY = curPos.yCoord - (dist > droopStart ? ((dist - droopStart) / 3) : 0);
					
					if (CoroUtilBlock.isAir(growMan.worldObj.getBlock((int)aimX, (int)aimY, (int)aimZ)) && !CoroUtilBlock.isAir(growMan.worldObj.getBlock((int)aimX, (int)aimY-1, (int)aimZ))) {
						growMan.worldObj.setBlock((int)aimX, (int)aimY, (int)aimZ, idBranch);
					}
				//}
			}
		}
		growMan.worldObj.setBlock((int)curPos.xCoord, (int)curPos.yCoord, (int)curPos.zCoord, idBranch);
	}
	
	public void tickLeafPlace(Boolean force) {
		if (!force && age % ConfigTrees.leafPlaceRate != 0) return;
		Random rand = new Random();
		if (childIteration >= ConfigTrees.leafMinBranchGenerationForLeafs) {
			for (int i = 0; i < 0/*Math.min(1 + childIteration, 6)*/; i++) {
				int xx = rand.nextInt(2) - rand.nextInt(2);
				int yy = rand.nextInt(2) - rand.nextInt(2);
				int zz = rand.nextInt(2) - rand.nextInt(2);
				
				if (CoroUtilBlock.isAir(growMan.worldObj.getBlock((int)curPos.xCoord+xx, (int)curPos.yCoord+yy, (int)curPos.zCoord+zz))) {
					Block placeID = idLeaf;
					//if (childIteration < 1) placeID = idBranch;
					growMan.worldObj.setBlock((int)curPos.xCoord+xx, (int)curPos.yCoord+yy, (int)curPos.zCoord+zz, placeID);
				}
			}
			
			Block placeID = idLeaf;
			
			for (double i = 0; i < 360; i+=15) {
				double rad = Math.PI/180F;
				double maxDist = ConfigTrees.leafCreationRadius;
				double droopStart = 4;
				for (double dist = 1D; dist < maxDist; dist++) {
					double aimX = curPos.xCoord + (Math.sin(i * rad) * dist);
					double aimZ = curPos.zCoord + (Math.cos(i * rad) * dist);
					double aimY = curPos.yCoord - (dist > droopStart ? ((dist - droopStart) / 3) : 0);
					
					if (CoroUtilBlock.isAir(growMan.worldObj.getBlock((int)aimX, (int)aimY, (int)aimZ))) {
						growMan.worldObj.setBlock((int)aimX, (int)aimY, (int)aimZ, placeID);
					}
				}
			}
		}
	}
	
	public void nextStage() {
		stageCur++;
		System.out.println("growth next stage - " + stageCur + " - " + (int)curPos.xCoord + " - " + (int)curPos.yCoord + " - " + (int)curPos.zCoord);
		
		
	}
	
	public void newNode() {
		GrowthNode gn = new GrowthNode(growMan, this);
		childNodes.add(gn);
		//TODO: find out why i was redesigning this 
		//i think this class was being phased out actually, profiled version is replacement and used instead of this class
		//growMan.activeGrowths.add(gn);
	}
	
	public boolean isActive() {
		return stageCur < stageMax;
	}
	
}
