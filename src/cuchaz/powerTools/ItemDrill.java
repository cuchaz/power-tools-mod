package cuchaz.powerTools;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public abstract class ItemDrill extends ItemOilBasedTool
{
	// settings
	private static final int MaxUses = 400;
	private static final int DamageVsEntity = 1; // should be 0-5
	private static final int Enchantability = 10; // should be 0-22
	private static final int DurabilityLostToBlock = 1; // should be small fraction of MaxUses
	private static final int DurabilityLostToEntity = 2; //
	private static final float FillerEfficiency = 8.0f; // 0-12 (2,4,6,8,12 : wood,stone,iron,diamond,gold)
	private static final float OreEfficiency = 4.0f;
	private static final int OilPowerLength = 350;
	
	private static final int[] FillerBlocks = new int[]
  	{
  		Block.cobblestone.blockID,
  		Block.stoneDoubleSlab.blockID,
  		Block.stoneSingleSlab.blockID,
  		Block.stone.blockID,
  		Block.sandStone.blockID,
  		Block.cobblestoneMossy.blockID,
  		Block.ice.blockID,
  		Block.netherrack.blockID,
  		Block.grass.blockID,
  		Block.dirt.blockID,
  		Block.sand.blockID,
  		Block.gravel.blockID,
  		Block.snow.blockID,
  		Block.blockSnow.blockID,
  		Block.blockClay.blockID,
  		Block.tilledField.blockID,
  		Block.slowSand.blockID,
  		Block.mycelium.blockID
  	};
  	
  	private static final int[] OreBlocks = new int[]
  	{
  		Block.oreIron.blockID,
  		Block.oreCoal.blockID,
  		Block.oreGold.blockID,
  		Block.oreDiamond.blockID,
  		Block.oreLapis.blockID,
  		Block.oreRedstone.blockID,
  		Block.oreRedstoneGlowing.blockID
  	};
  	
	static
	{
		// sort the blocklists so we can use binary search
		Arrays.sort( FillerBlocks );
		Arrays.sort( OreBlocks );
	}
	
	public ItemDrill( int itemId )
	{
		super( itemId, OilPowerLength );
		
		setMaxDamage( MaxUses );
	}
	
	protected boolean isFillerBlock( Block block )
	{
		return isFillerBlock( block.blockID );
	}
	
	protected boolean isFillerBlock( int blockId )
	{
		return Arrays.binarySearch( FillerBlocks, blockId ) >= 0;
	}
	
	protected boolean isOreBlock( Block block )
	{
		return isOreBlock( block.blockID );
	}
	
	protected boolean isOreBlock( int blockId )
	{
		return Arrays.binarySearch( OreBlocks, blockId ) >= 0;
	}
	
	@Override
	public boolean canHarvestBlock( Block block )
	{
		return isFillerBlock( block ) || isOreBlock( block );
	}
	
	@Override
	public boolean hitEntity( ItemStack itemStack, EntityLiving entityTarget, EntityLiving entityUser )
	{
		// decrease item durability
		itemStack.damageItem( DurabilityLostToEntity, entityUser );
		
		return true;
	}
	
	@Override
	public boolean onBlockStartBreak( ItemStack itemStack, int x, int y, int z, EntityPlayer player )
	{
		final boolean AllowHarvest = false;
		//final boolean PreventHarvest = true;
		
		// find out which side we're hitting
		World world = player.worldObj;
		Block block = Block.blocksList[world.getBlockId( x, y, z )];
		final boolean HitLiquids = false;
		MovingObjectPosition pos = getMovingObjectPositionFromPlayer( world, player, HitLiquids );
		if( pos == null || pos.blockX != x || pos.blockY != y || pos.blockZ != z )
		{
			return AllowHarvest;
		}
		int side = pos.sideHit;
		
		// if the block has hardness
		if( block.getBlockHardness( world, x, y, z ) != 0.0f )
		{
			// decrease item durability
			itemStack.damageItem( DurabilityLostToBlock, player );
		}
		
		// dig the extra blocks
		if( isFillerBlock( block ) )
		{
			for( ChunkCoordinates coords : getOtherBlocksToDig( world, x, y, z, side, player ) )
			{
				if( isFillerBlock( world.getBlockId( coords.posX, coords.posY, coords.posZ ) ) )
				{
					world.destroyBlock( coords.posX, coords.posY, coords.posZ, true );
				}
			}
		}
		
		return AllowHarvest;
	}
	
	protected abstract List<ChunkCoordinates> getOtherBlocksToDig( World world, int x, int y, int z, int side, EntityPlayer player );
	
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
		if( isFillerBlock( block ) )
		{
			return FillerEfficiency;
		}
		else if( isOreBlock( block ) )
		{
			return OreEfficiency;
		}
		
		return super.getStrVsBlock( stack, block, meta );
	}
}
