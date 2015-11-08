package vectortree.forge;

import net.minecraft.block.Block;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;


@Mod(modid = "VectorTree", name="Vector Tree", version="v1.0")
public class VectorTree {
	
	@Mod.Instance( value = "VectorTree" )
	public static VectorTree instance;
	public static String modID = "vectortree";
    
    /** For use in preInit ONLY */
    public Configuration config;
    
    public static Block blockBase;
    
    @SidedProxy(clientSide = "vectortree.forge.ClientProxy", serverSide = "vectortree.forge.CommonProxy")
    public static CommonProxy proxy;
    
    public static String eventChannelName = "particleman";
	public static final FMLEventChannel eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(eventChannelName);

    public VectorTree() {
    	
    }
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	
    	eventChannel.register(new EventHandlerPacket());
    	
    	config = new Configuration(event.getSuggestedConfigurationFile());

        try
        {
        	config.load();
        	//itemIDStart = config.get(Configuration.CATEGORY_BLOCK, "itemIDStart", itemIDStart).getInt(itemIDStart);
        	//hurtAnimals = config.get(Configuration.CATEGORY_GENERAL, "hurtAnimals", false).getBoolean(false);
            
        }
        catch (Exception e)
        {
            System.out.println("Hostile Worlds has a problem loading it's configuration");
        }
        finally
        {
        	config.save();
        }
        
        proxy.preInit(this);
    }
    
    @Mod.EventHandler
    public void load(FMLInitializationEvent event)
    {
    	proxy.init(this);
    	FMLCommonHandler.instance().bus().register(new EventHandlerFML());
    	
    }
    
    @Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		
	}
    
    @Mod.EventHandler
    public void serverStart(FMLServerStartedEvent event) {
    	
    }
    
    @Mod.EventHandler
    public void serverStop(FMLServerStoppedEvent event) {
    	
    }
    
	public static void dbg(Object obj) {
		if (true) System.out.println(obj);
	}
}
