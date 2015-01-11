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

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cuchaz.modsShared.blocks.BlockSide;

public class BlockOilRefinery extends BlockContainer {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] m_iconFront;
	@SideOnly(Side.CLIENT)
	private IIcon[] m_iconSide;
	@SideOnly(Side.CLIENT)
	private IIcon m_iconTop;
	
	protected BlockOilRefinery() {
		super(Material.iron);
		
		setHardness(5.0F);
		setResistance(10.0F);
		setStepSound(Block.soundTypeMetal);
		setBlockName("blockOilRefinery");
		setCreativeTab(CreativeTabs.tabDecorations);
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityOilRefinery();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		m_iconFront = new IIcon[] { null, null, null, null };
		m_iconSide = new IIcon[] { null, null };
		
		// UNDONE: can change textures here based on state
		blockIcon = iconRegister.registerIcon("powertools:oilRefineryBack");
		m_iconFront[0] = iconRegister.registerIcon("powertools:oilRefineryFront1");
		m_iconFront[1] = iconRegister.registerIcon("powertools:oilRefineryFront2");
		m_iconFront[2] = iconRegister.registerIcon("powertools:oilRefineryFront3");
		m_iconFront[3] = iconRegister.registerIcon("powertools:oilRefineryFront4");
		m_iconSide[0] = iconRegister.registerIcon("powertools:oilRefinerySide1");
		m_iconSide[1] = iconRegister.registerIcon("powertools:oilRefinerySide2");
		m_iconTop = iconRegister.registerIcon("powertools:oilRefineryTop");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return getIcon(side, meta, 0, 0);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		TileEntityOilRefinery tileEntity = (TileEntityOilRefinery)world.getTileEntity(x, y, z);
		return getIcon(side, tileEntity.getBlockMetadata(), tileEntity.getWheelFrame(), tileEntity.getOilFrame());
	}
	
	@SideOnly(Side.CLIENT)
	private IIcon getIcon(int side, int rotation, int wheelFrame, int oilFrame) {
		
		// top and bottom are easy
		BlockSide targetSide = BlockSide.getById(side);
		switch (targetSide) {
			case Top:
				return m_iconTop;
			case Bottom:
				return blockIcon;
			default:
				break;
		}
		
		// rotate the sides
		targetSide = targetSide.rotateXZCcw(rotation);
		
		// now do the sides
		switch (targetSide) {
			case North:
				return blockIcon;
			case East:
				return m_iconSide[wheelFrame];
			case South:
				return m_iconFront[oilFrame];
			case West:
				return m_iconSide[wheelFrame];
			default:
				break;
		}
		
		// if all else fails
		return blockIcon;
	}
	
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityUser, ItemStack itemStack) {
		
		// save the block rotation to the metadata
		world.setBlockMetadataWithNotify(x, y, z, BlockSide.getByYaw(entityUser.rotationYaw).getXZOffset(), 3);
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		
		// ignore for clients
		if (world.isRemote) {
			return true;
		}
		
		TileEntityOilRefinery tileEntity = (TileEntityOilRefinery)world.getTileEntity(x, y, z);
		if (tileEntity != null) {
			player.displayGUIChest(tileEntity.getInventory());
			tileEntity.onBlockActivated();
		}
		
		return true;
	}
	
	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		Random rand = new Random();
		
		// eject the items from the refinery
		TileEntityOilRefinery tileEntity = (TileEntityOilRefinery)world.getTileEntity(x, y, z);
		for (int i = 0; i < tileEntity.getInventory().getSizeInventory(); i++) {
			// get the item in slot i
			ItemStack itemStack = tileEntity.getInventory().getStackInSlot(i);
			if (itemStack == null) {
				continue;
			}
			
			// pick a random spot for the item in this block
			double dx = rand.nextDouble() * 0.8 + 0.1;
			double dy = rand.nextDouble() * 0.8 + 0.1;
			double dz = rand.nextDouble() * 0.8 + 0.1;
			
			// pick a random direction for the item
			double variance = 0.05;
			double extraUpSpeed = 0.2f;
			double vx = rand.nextGaussian() * variance;
			double vy = rand.nextGaussian() * variance + extraUpSpeed;
			double vz = rand.nextGaussian() * variance;
			
			// eject the item into the world
			EntityItem entityItem = new EntityItem(world, dx + x, dy + y, dz + z, new ItemStack(itemStack.getItem(), itemStack.stackSize, itemStack.getItemDamage()));
			entityItem.motionX = vx;
			entityItem.motionY = vy;
			entityItem.motionZ = vz;
			world.spawnEntityInWorld(entityItem);
		}
		
		super.breakBlock(world, x, y, z, block, meta);
	}
}
