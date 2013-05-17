package cuchaz.powerTools;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod( modid="cuchaz.powerTools", name="Power Tools", version="0.0.1" )
@NetworkMod( clientSideRequired=true, serverSideRequired=true )
public class PowerTools
{
	@Instance( "cuchaz.powerTools" )
	public static PowerTools m_instance;
	
	// item registration: use ids [7308,7319]
	public static final ItemOil ItemOil = new ItemOil( 7308 );
	public static final ItemDrillShaft ItemDrillShaft = new ItemDrillShaft( 7309 );
	public static final ItemDrillWideBore ItemDrillWideBore = new ItemDrillWideBore( 7310 );
	
	// block registration: use ids [1150,1154]
	public static final BlockOilRefinery BlockOilRefinery = new BlockOilRefinery( 1150 );
	
	@PreInit
	public void preInit( FMLPreInitializationEvent event )
	{
		// nothing to do
	}
	
	@Init
	public void load( FMLInitializationEvent event )
	{
		// register stuff
		GameRegistry.registerItem( ItemOil, "oil" );
		GameRegistry.registerItem( ItemDrillShaft, "drillShaft" );
		GameRegistry.registerItem( ItemDrillWideBore, "drillWideBore" );
		
		GameRegistry.registerBlock( BlockOilRefinery, "oilRefinery" );
		
		GameRegistry.registerTileEntity( TileEntityOilRefinery.class, "oilRefinery" );
		
		// name stuff
		LanguageRegistry.addName( ItemOil, "Oil" );
		LanguageRegistry.addName( ItemDrillShaft, "Shaft Drill" );
		LanguageRegistry.addName( ItemDrillWideBore, "Wide-Bore Drill" );
		
		LanguageRegistry.addName( BlockOilRefinery, "Oil Refinery" );
		
		ItemStack coalStack = new ItemStack( Item.coal );
		ItemStack waterStack = new ItemStack( Item.bucketWater );
		ItemStack ironStack = new ItemStack( Item.ingotIron );
		ItemStack stickStack = new ItemStack( Item.stick );
		ItemStack redstoneStack = new ItemStack( Item.redstone );
		ItemStack oilStack = new ItemStack( ItemOil );
		
		// crafting recipes
		
		// oil
		GameRegistry.addShapelessRecipe(
			new ItemStack( ItemOil, 4 ),
			waterStack, coalStack, coalStack, coalStack
		);
		GameRegistry.addRecipe(
			new ItemStack( ItemOil, 16 ),
			"xxx", "xyx", "xxx",
			'x', coalStack,
			'y', waterStack
		);
		
		// drills
		GameRegistry.addRecipe(
			new ItemStack( ItemDrillShaft, 1 ),
			" xx", "yyy", " zz",
			'x', redstoneStack,
			'y', ironStack,
			'z', stickStack
		);
		GameRegistry.addRecipe(
			new ItemStack( ItemDrillWideBore, 1 ),
			"yxx", "yyy", "yzz",
			'x', redstoneStack,
			'y', ironStack,
			'z', stickStack
		);
		
		// oil refinery
		GameRegistry.addRecipe(
			new ItemStack( BlockOilRefinery ),
			"xxx", "xyx", "xxx",
			'x', ironStack,
			'y', oilStack
		);
	}
	
	@PostInit
	public void postInit( FMLPostInitializationEvent event )
	{
		// nothing to do
	}
}