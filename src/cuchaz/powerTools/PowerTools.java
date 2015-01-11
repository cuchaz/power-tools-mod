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

import java.util.Arrays;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(
	modid = "cuchaz.powerTools",
	name = "Power Tools",
	version = "1.7.10-1.3",
	dependencies = "required-after:Forge@[10.13.2.1230,)",
	acceptedMinecraftVersions = "[1.7.10,)"
)
public class PowerTools {
	
	@Mod.Instance("cuchaz.powerTools")
	public static PowerTools instance;
	
	// dictionary names
	public static String Oil = "oil";
	
	// items
	public static final ItemOil ItemOil = new ItemOil();
	public static final ItemDrillShaft ItemDrillShaft = new ItemDrillShaft();
	public static final ItemDrillWideBore ItemDrillWideBore = new ItemDrillWideBore();
	public static final ItemChainsaw ItemChainsaw = new ItemChainsaw();
	public static final ItemJackhammer ItemJackhammer = new ItemJackhammer();
	
	// blocks
	public static final BlockOilRefinery BlockOilRefinery = new BlockOilRefinery();
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		
		// set the metadata
		ModMetadata meta = event.getModMetadata();
		meta.autogenerated = false;
		meta.description = "Save time with better tools!";
		meta.credits = "Created by Cuchaz";
		meta.url = "http://www.cuchazinteractive.com/power-tools/";
		meta.updateUrl = meta.url;
		meta.authorList = Arrays.asList("Cuchaz");
	}
	
	@Mod.EventHandler
	public void load(FMLInitializationEvent event) {
		
		// register stuff
		GameRegistry.registerItem(ItemOil, Oil);
		GameRegistry.registerItem(ItemDrillShaft, "drillShaft");
		GameRegistry.registerItem(ItemDrillWideBore, "drillWideBore");
		GameRegistry.registerItem(ItemChainsaw, "chainsaw");
		GameRegistry.registerItem(ItemJackhammer, "jackhammer");
		
		GameRegistry.registerBlock(BlockOilRefinery, "oilRefinery");
		
		GameRegistry.registerTileEntity(TileEntityOilRefinery.class, "oilRefinery");
		
		// register for events
		MinecraftForge.EVENT_BUS.register(ItemDrillShaft);
		MinecraftForge.EVENT_BUS.register(ItemDrillWideBore);
		MinecraftForge.EVENT_BUS.register(ItemChainsaw);
		MinecraftForge.EVENT_BUS.register(ItemJackhammer);
		
		// register in dictionaries
		OreDictionary.registerOre(Oil, new ItemStack(ItemOil));
		
		ItemStack coalStack = new ItemStack(Items.coal);
		ItemStack waterStack = new ItemStack(Items.water_bucket);
		ItemStack ironStack = new ItemStack(Items.iron_ingot);
		ItemStack stickStack = new ItemStack(Items.stick);
		ItemStack redstoneStack = new ItemStack(Items.redstone);
		
		// crafting recipes
		
		// oil
		GameRegistry.addShapelessRecipe(
			new ItemStack(ItemOil, 4),
			waterStack, coalStack, coalStack, coalStack
		);
		GameRegistry.addRecipe(
			new ItemStack(ItemOil, 16),
			"xxx",
			"xyx",
			"xxx",
			'x', coalStack,
			'y', waterStack
		);
		
		// tools
		GameRegistry.addRecipe(
			new ItemStack(ItemDrillShaft, 1),
			" xx",
			"yyy",
			" zz",
			'x', redstoneStack,
			'y', ironStack,
			'z', stickStack
		);
		
		GameRegistry.addRecipe(
			new ItemStack(ItemDrillWideBore, 1),
			"yxx",
			"yyy",
			"yzz",
			'x', redstoneStack,
			'y', ironStack,
			'z', stickStack
		);
		
		GameRegistry.addRecipe(
			new ItemStack(ItemChainsaw, 1),
			"x y",
			"xy ",
			"zz ",
			'x', redstoneStack,
			'y', ironStack,
			'z', stickStack
		);
		
		GameRegistry.addRecipe(
			new ItemStack(ItemJackhammer, 1),
			"zzz",
			"xyx",
			" y ",
			'x', redstoneStack,
			'y', ironStack,
			'z', stickStack
		);
		
		// oil refinery
		GameRegistry.addRecipe(new ShapedOreRecipe(
			new ItemStack(BlockOilRefinery),
			"xxx",
			"xyx",
			"xxx",
			'x', ironStack,
			'y', Oil
		));
	}
}
