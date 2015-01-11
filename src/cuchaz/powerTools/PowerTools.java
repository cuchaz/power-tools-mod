/*******************************************************************************
 * Copyright (c) 2013 Jeff Martin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Jeff Martin - initial API and implementation
 ******************************************************************************/
package cuchaz.powerTools;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class PowerTools {
	@Instance("cuchaz.powerTools")
	public static PowerTools instance;
	
	// dictionary names
	public static String Oil = "oil";
	
	// item registration: use ids [7308,7319]
	public static final ItemOil ItemOil = new ItemOil(7308);
	public static final ItemDrillShaft ItemDrillShaft = new ItemDrillShaft(7309);
	public static final ItemDrillWideBore ItemDrillWideBore = new ItemDrillWideBore(7310);
	public static final ItemChainsaw ItemChainsaw = new ItemChainsaw(7311);
	public static final ItemJackhammer ItemJackhammer = new ItemJackhammer(7312);
	
	// block registration: use ids [1150,1154]
	public static final BlockOilRefinery BlockOilRefinery = new BlockOilRefinery(1150);
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		// nothing to do
	}
	
	@EventHandler
	public void load(FMLInitializationEvent event) {
		// register stuff
		GameRegistry.registerItem(ItemOil, Oil);
		GameRegistry.registerItem(ItemDrillShaft, "drillShaft");
		GameRegistry.registerItem(ItemDrillWideBore, "drillWideBore");
		GameRegistry.registerItem(ItemChainsaw, "chainsaw");
		GameRegistry.registerItem(ItemJackhammer, "jackhammer");
		
		GameRegistry.registerBlock(BlockOilRefinery, "oilRefinery");
		
		GameRegistry.registerTileEntity(TileEntityOilRefinery.class, "oilRefinery");
		
		// name stuff
		LanguageRegistry.addName(ItemOil, "Oil");
		LanguageRegistry.addName(ItemDrillShaft, "Shaft Drill");
		LanguageRegistry.addName(ItemDrillWideBore, "Wide-Bore Drill");
		LanguageRegistry.addName(ItemChainsaw, "Chainsaw");
		LanguageRegistry.addName(ItemJackhammer, "Jackhammer");
		
		LanguageRegistry.addName(BlockOilRefinery, "Oil Refinery");
		
		// register for events
		MinecraftForge.EVENT_BUS.register(ItemDrillShaft);
		MinecraftForge.EVENT_BUS.register(ItemDrillWideBore);
		MinecraftForge.EVENT_BUS.register(ItemChainsaw);
		MinecraftForge.EVENT_BUS.register(ItemJackhammer);
		
		// register in dictionaries
		OreDictionary.registerOre(Oil, new ItemStack(ItemOil));
		
		ItemStack coalStack = new ItemStack(Item.coal);
		ItemStack waterStack = new ItemStack(Item.bucketWater);
		ItemStack ironStack = new ItemStack(Item.ingotIron);
		ItemStack stickStack = new ItemStack(Item.stick);
		ItemStack redstoneStack = new ItemStack(Item.redstone);
		
		// crafting recipes
		
		// oil
		GameRegistry.addShapelessRecipe(new ItemStack(ItemOil, 4), waterStack, coalStack, coalStack, coalStack);
		GameRegistry.addRecipe(new ItemStack(ItemOil, 16), "xxx", "xyx", "xxx", 'x', coalStack, 'y', waterStack);
		
		// tools
		GameRegistry.addRecipe(new ItemStack(ItemDrillShaft, 1), " xx", "yyy", " zz", 'x', redstoneStack, 'y', ironStack, 'z', stickStack);
		GameRegistry.addRecipe(new ItemStack(ItemDrillWideBore, 1), "yxx", "yyy", "yzz", 'x', redstoneStack, 'y', ironStack, 'z', stickStack);
		GameRegistry.addRecipe(new ItemStack(ItemChainsaw, 1), "x y", "xy ", "zz ", 'x', redstoneStack, 'y', ironStack, 'z', stickStack);
		GameRegistry.addRecipe(new ItemStack(ItemJackhammer, 1), "zzz", "xyx", " y ", 'x', redstoneStack, 'y', ironStack, 'z', stickStack);
		
		// oil refinery
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BlockOilRefinery), "xxx", "xyx", "xxx", 'x', ironStack, 'y', Oil));
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		// nothing to do
	}
}
