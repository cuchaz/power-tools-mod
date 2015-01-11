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
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import cuchaz.modsShared.blocks.BlockSet;
import cuchaz.modsShared.blocks.Coords;

public abstract class ItemDrill extends ItemOilBasedTool {
	
	// settings
	private static final int MaxUses = 400;
	private static final float DamageVsEntity = 1.0f; // should be 0-5
	private static final int Enchantability = 10; // should be 0-22
	private static final int DurabilityLostToBlock = 1; // should be small fraction of MaxUses
	private static final int DurabilityLostToEntity = 2; //
	private static final float FillerEfficiency = 8.0f; // 0-12 (2,4,6,8,12 : wood,stone,iron,diamond,gold)
	private static final float OreEfficiency = 1.0f;
	private static final int OilPowerLength = 35;
	
	private static final Set<Block> FillerBlocks = Sets.newHashSet(Arrays.asList(
		Blocks.cobblestone,
		Blocks.stone_slab,
		Blocks.stone,
		Blocks.sandstone,
		Blocks.mossy_cobblestone,
		Blocks.ice,
		Blocks.netherrack,
		Blocks.grass,
		Blocks.dirt,
		Blocks.sand,
		Blocks.gravel,
		Blocks.snow,
		Blocks.snow_layer,
		Blocks.clay,
		Blocks.farmland,
		Blocks.mycelium
	));
	
	private static final Set<Block> OreBlocks = Sets.newHashSet(Arrays.asList(
		Blocks.iron_ore,
		Blocks.iron_block,
		Blocks.coal_ore,
		Blocks.coal_block,
		Blocks.gold_ore,
		Blocks.gold_block,
		Blocks.diamond_ore,
		Blocks.diamond_block,
		Blocks.lapis_ore,
		Blocks.lapis_block,
		Blocks.redstone_ore,
		Blocks.redstone_block
	));
	
	public ItemDrill() {
		super(OilPowerLength);
		
		setMaxDamage(MaxUses);
	}
	
	@Override
	public boolean canHarvestBlock(Block block, ItemStack stack) {
		return FillerBlocks.contains(block) || OreBlocks.contains(block);
	}
	
	@Override
	public boolean hitEntity(ItemStack itemStack, EntityLivingBase entityTarget, EntityLivingBase entityUser) {
		
		// decrease item durability
		itemStack.damageItem(DurabilityLostToEntity, entityUser);
		
		return true;
	}
	
	@Override
	public boolean onBlockStartBreak(ItemStack itemStack, int x, int y, int z, EntityPlayer player) {
		final boolean AllowHarvest = false;
		// final boolean PreventHarvest = true;
		
		// find out which side we're hitting
		World world = player.worldObj;
		Block block = world.getBlock(x, y, z);
		final boolean HitLiquids = false;
		MovingObjectPosition pos = getMovingObjectPositionFromPlayer(world, player, HitLiquids);
		if (pos == null || pos.blockX != x || pos.blockY != y || pos.blockZ != z) {
			return AllowHarvest;
		}
		int side = pos.sideHit;
		
		if (!player.capabilities.isCreativeMode) {
			// if the block has hardness
			if (block.getBlockHardness(world, x, y, z) != 0.0f) {
				// decrease item durability
				itemStack.damageItem(DurabilityLostToBlock, player);
			}
		}
		
		if (!world.isRemote) {
			// dig the extra blocks
			if (FillerBlocks.contains(block)) {
				for (Coords coords : getOtherBlocksToDig(world, x, y, z, side, player)) {
					if (FillerBlocks.contains(world.getBlock(coords.x, coords.y, coords.z))) {
						digBlock(player, coords);
					}
				}
			}
		}
		
		return AllowHarvest;
	}
	
	private void digBlock(EntityPlayer player, Coords coords) {
		
		World world = player.worldObj;
		Block block = world.getBlock(coords.x, coords.y, coords.z);
		int meta = world.getBlockMetadata(coords.x, coords.y, coords.z);
		
		// set the block to air and drop the item
		block.harvestBlock(world, player, coords.x, coords.y, coords.z, meta);
		world.setBlock(coords.x, coords.y, coords.z, Blocks.air, 0, 3);
	}

	protected abstract BlockSet getOtherBlocksToDig(World world, int x, int y, int z, int side, EntityPlayer player);
	
	@SuppressWarnings({ "rawtypes", "deprecation", "unchecked" })
	public Multimap getItemAttributeModifiers() {
        Multimap multimap = super.getItemAttributeModifiers();
        multimap.put(
        	SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(),
        	new AttributeModifier(field_111210_e, "Tool modifier", (double)DamageVsEntity, 0)
        );
        return multimap;
    }
	
	@Override
	public int getItemEnchantability() {
		return Enchantability;
	}
	
	@Override // getStrVsBlock
	public float func_150893_a(ItemStack stack, Block block) {
		if (FillerBlocks.contains(block)) {
			return FillerEfficiency;
		} else if (OreBlocks.contains(block)) {
			return OreEfficiency;
		}
		return super.func_150893_a(stack, block);
	}
}
