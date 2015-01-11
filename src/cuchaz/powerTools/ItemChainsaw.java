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

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.google.common.collect.Multimap;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemChainsaw extends ItemOilBasedTool {
	
	// settings
	private static final int MaxUses = 400;
	private static final float DamageVsEntity = 4.0f; // should be 0-5
	private static final int Enchantability = 10; // should be 0-22
	private static final int DurabilityLostToBlockWood = 8; // should be small fraction of MaxUses
	private static final int DurabilityLostToBlockLeaves = 1; //
	private static final int DurabilityLostToBlockOther = 20; //
	private static final int DurabilityLostToEntity = 5; //
	private static final float WoodEfficiency = 1.0f; // 0-12 (2,4,6,8,12 : wood,stone,iron,diamond,gold)
	private static final float LeavesEfficiency = 12.0f;
	private static final int OilPowerLength = 16;
	
	public ItemChainsaw() {
		super(OilPowerLength);
		setMaxDamage(MaxUses);
		setUnlocalizedName("cuchaz.powerTools.chainsaw");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		itemIcon = iconRegister.registerIcon("powertools:chainsaw");
	}
	
	@Override
	public boolean canHarvestBlock(Block block, ItemStack stack) {
		return TileEntityTreeHarvester.isWoodBlock(block);
	}
	
	@Override
	public boolean hitEntity(ItemStack itemStack, EntityLivingBase entityTarget, EntityLivingBase entityUser) {
		
		// decrease item durability
		itemStack.damageItem(DurabilityLostToEntity, entityUser);
		
		return true;
	}
	
	@Override
	public boolean onBlockStartBreak(ItemStack itemStack, int x, int y, int z, EntityPlayer player) {
		World world = player.worldObj;
		Block block = world.getBlock(x, y, z);
		
		// if the block has hardness
		if (block.getBlockHardness(world, x, y, z) != 0.0f) {
			
			// decrease item durability
			int damage = 0;
			if (TileEntityTreeHarvester.isWoodBlock(block)) {
				damage = DurabilityLostToBlockWood;
				
				// on the server, spawn a tree harvester
				if (!world.isRemote) {
					TileEntityTreeHarvester.spawn(world, x, y, z);
				}
			} else if (TileEntityTreeHarvester.isLeavesBlock(block)) {
				damage = DurabilityLostToBlockLeaves;
			} else {
				damage = DurabilityLostToBlockOther;
			}
			itemStack.damageItem(damage, player);
		}
		
		return super.onBlockStartBreak(itemStack, x, y, z, player);
	}
	
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
		if (TileEntityTreeHarvester.isWoodBlock(block)) {
			return WoodEfficiency;
		} else if (TileEntityTreeHarvester.isLeavesBlock(block)) {
			return LeavesEfficiency;
		}
		return super.func_150893_a(stack, block);
	}
}
