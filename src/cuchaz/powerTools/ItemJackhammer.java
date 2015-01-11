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
import net.minecraft.block.material.Material;
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

public class ItemJackhammer extends ItemOilBasedTool {
	
	// settings
	private static final int MaxUses = 400;
	private static final float DamageVsEntity = 1.0f; // should be 0-5
	private static final int Enchantability = 10; // should be 0-22
	private static final int DurabilityLostToHardBlock = 1; // should be small fraction of MaxUses
	private static final int DurabilityLostToOther = 20; //
	private static final int DurabilityLostToEntity = 5; //
	private static final int OilPowerLength = 35;
	
	private static final Material[] HardMaterials = { Material.glass, Material.ice, Material.rock };
	
	public ItemJackhammer() {
		super(OilPowerLength);
		setMaxDamage(MaxUses);
		setUnlocalizedName("cuchaz.powerTools.jackhammer");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		itemIcon = iconRegister.registerIcon("powertools:jackhammer");
	}
	
	@Override
	public boolean canHarvestBlock(Block block, ItemStack stack) {
		return isTargetBlock(block);
	}
	
	@Override
	public boolean hitEntity(ItemStack itemStack, EntityLivingBase entityTarget, EntityLivingBase entityUser) {
		
		// decrease item durability
		itemStack.damageItem(DurabilityLostToEntity, entityUser);
		
		return true;
	}
	
	@Override
	public boolean onBlockStartBreak(ItemStack itemStack, int x, int y, int z, EntityPlayer player) {
		
		// get the block
		World world = player.worldObj;
		Block block = world.getBlock(x, y, z);
		
		// if the block has hardness
		if (block.getBlockHardness(world, x, y, z) != 0.0f) {
			// decrease item durability
			itemStack.damageItem(isTargetBlock(block) ? DurabilityLostToHardBlock : DurabilityLostToOther, player);
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
		if (isTargetBlock(block)) {
			// efficiency
			// 0-12 (2,4,6,8,12 : wood,stone,iron,diamond,gold)
			
			// hardness
			// stone: 1.5
			// coal,iron,gold ore: 3.0
			// blockIron, blockEmerald: 5.0
			// obsidian: 50.0
			
			// want 1.5x -> 12
			// want 5.0x -> 40
			// want 50x -> 400
			
			// the harder the block, the better the jackhammer works
			return block.getBlockHardness(null, 0, 0, 0) * 8;
		}
		return super.func_150893_a(stack, block);
	}
	
	private boolean isTargetBlock(Block block) {
		for (Material material : HardMaterials) {
			if (block.getMaterial() == material) {
				return true;
			}
		}
		return false;
	}
}
