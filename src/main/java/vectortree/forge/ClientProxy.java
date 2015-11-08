package vectortree.forge;

import vectortree.block.TileEntityVectorTree;
import vectortree.client.block.TileEntityVectorTreeRenderer;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{

    public ClientProxy()
    {
    	
    }
    
    @Override
    public void preInit(VectorTree pMod)
    {
    	super.preInit(pMod);
    }

    @Override
    public void init(VectorTree pMod)
    {
        super.init(pMod);
        
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityVectorTree.class, new TileEntityVectorTreeRenderer());
    }
    
}
