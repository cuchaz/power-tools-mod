package cuchaz.powerTools;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemChainsaw extends ItemOilBasedTool
{
	// settings
	private static final int MaxUses = 400;
	private static final int DamageVsEntity = 4; // should be 0-5
	private static final int Enchantability = 10; // should be 0-22
	private static final int DurabilityLostToBlockWood = 10; // should be small fraction of MaxUses
	private static final int DurabilityLostToBlockLeaves = 1; //
	private static final int DurabilityLostToBlockOther = 20;
	private static final int DurabilityLostToEntity = 5; //
	private static final float WoodEfficiency = 1.0f; // 0-12 (2,4,6,8,12 : wood,stone,iron,diamond,gold)
	private static final float LeavesEfficiency = 12.0f;
	private static final int OilPowerLength = 160;
	
  	// data members
  	private List<TileEntityTreeHarvester> m_treeHarvesters;
	
	public ItemChainsaw( int itemId )
	{
		super( itemId, OilPowerLength );
		
		setMaxDamage( MaxUses );
		
		m_treeHarvesters = new ArrayList<TileEntityTreeHarvester>();
	}
	
	@Override
	public void registerIcons( IconRegister iconRegister )
	{
		itemIcon = iconRegister.registerIcon( "powerTools:chainsaw" );
	}
	
	@Override
	public boolean canHarvestBlock( Block block )
	{
		return block == Block.wood;
	}
	
	@Override
	public boolean hitEntity( ItemStack itemStack, EntityLiving entityTarget, EntityLiving entityUser )
	{
		// decrease item durability
		itemStack.damageItem( DurabilityLostToEntity, entityUser );
		
		return true;
	}
	
	/*
	@Override
	public boolean onBlockDestroyed( ItemStack itemStack, World world, int x, int y, int z, int meta, EntityLiving entityUser )
	{
	}
	*/
	
	@Override
	public boolean onBlockStartBreak( ItemStack itemStack, int x, int y, int z, EntityPlayer player )
	{
		World world = player.worldObj;
		Block block = Block.blocksList[world.getBlockId( x, y, z )];
		
		// TEMP
		System.out.println( "Chainsaw broke block " + block.blockID + " (" + block.getUnlocalizedName() + ")  with hardness: " + block.getBlockHardness( world, x, y, z ) );
		
		// if the block has hardness
		if( block.getBlockHardness( world, x, y, z ) != 0.0f )
		{
			// decrease item durability
			int damage = 0;
			if( block.blockID == Block.wood.blockID )
			{
				// TEMP
				System.out.println( "Chainsaw broke wood!" );
				
				damage = DurabilityLostToBlockWood;
				
				// on the server, spawn a tree harvester
				if( !world.isRemote )
				{
					TileEntityTreeHarvester.spawn( world, x, y, z );
				}
			}
			else if( block.blockID == Block.leaves.blockID )
			{
				// TEMP
				System.out.println( "Chainsaw broke leaves!" );
				
				damage = DurabilityLostToBlockLeaves;
			}
			else
			{
				// TEMP
				System.out.println( "Chainsaw broke other!" );
				
				damage = DurabilityLostToBlockOther;
			}
			itemStack.damageItem( damage, player );
		}
		
		return super.onBlockStartBreak( itemStack, x, y, z, player );
	}
	
	@Override
	public int getDamageVsEntity( Entity entityTarget )
	{
		return DamageVsEntity;
	}
	
	@Override
	public int getItemEnchantability( )
	{
		return Enchantability;
	}
	
	@Override
	public float getStrVsBlock( ItemStack stack, Block block, int meta )
	{
		if( block == Block.wood )
		{
			return WoodEfficiency;
		}
		else if( block == Block.leaves )
		{
			return LeavesEfficiency;
		}
		return super.getStrVsBlock( stack, block, meta );
	}
}
