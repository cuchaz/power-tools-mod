package cuchaz.powerTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemDrillShaft extends Item
{
	// settings
	private static final int MaxStackSize = 1;
	private static final int MaxUses = 400;
	private static final int DamageVsEntity = 1; // should be 0-5
	private static final int Enchantability = 10; // should be 0-22
	private static final int DurabilityLostToBlock = 1; // should be small fraction of MaxUses
	private static final int DurabilityLostToEntity = 2; //
	private static final float FillerEfficiency = 8.0f; // 0-12 (2,4,6,8,12 : wood,stone,iron,diamond,gold)
	private static final float OreEfficiency = 8.0f;
	private static final int MaxItemUseDuration = 72000;
	private static final int DigDepth = 2;
	
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
		Block.blockIron.blockID,
		Block.oreCoal.blockID,
		Block.blockGold.blockID,
		Block.oreGold.blockID,
		Block.oreDiamond.blockID,
		Block.blockDiamond.blockID,
		Block.oreLapis.blockID,
		Block.blockLapis.blockID,
		Block.oreRedstone.blockID,
		Block.oreRedstoneGlowing.blockID
	};
	
	// data members
	private int m_updateDelayTimer;
	private float m_blockDamage;
	private boolean m_isDiggingBlock;
	private int m_diggingBlockX;
	private int m_diggingBlockY;
	private int m_diggingBlockZ;
	
	static
	{
		// sort the blocklists so we can use binary search
		Arrays.sort( FillerBlocks );
		Arrays.sort( OreBlocks );
	}
	
	public ItemDrillShaft( int itemId )
	{
		super( itemId );
		
		maxStackSize = MaxStackSize;
		setUnlocalizedName( "drillShaft" );
		setMaxDamage( MaxUses );
		setCreativeTab( CreativeTabs.tabTools );
		
		m_updateDelayTimer = 0;
		resetDiggingState();
	}
    
	@Override
	public void registerIcons( IconRegister iconRegister )
	{
		itemIcon = iconRegister.registerIcon( "powerTools:drillShaft" );
	}
	
	private boolean isFillerBlock( Block block )
	{
		return isFillerBlock( block.blockID );
	}
	
	private boolean isFillerBlock( int blockId )
	{
		return Arrays.binarySearch( FillerBlocks, blockId ) >= 0;
	}
	
	private boolean isOreBlock( Block block )
	{
		return isOreBlock( block.blockID );
	}
	
	private boolean isOreBlock( int blockId )
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
	public boolean onBlockDestroyed( ItemStack itemStack, World world, int blockId, int x, int y, int z, EntityLiving entityUser )
	{
		// get the player
		EntityPlayer player;
		if( entityUser instanceof EntityPlayer )
		{
			player = (EntityPlayer)entityUser;
		}
		else
		{
			return false;
		}
		
		// find out where we're aiming
		Block block = Block.blocksList[world.getBlockId( x, y, z )];
		final boolean HitLiquids = false;
		MovingObjectPosition pos = getMovingObjectPositionFromPlayer( world, player, HitLiquids );
		if( pos.blockX != x || pos.blockY != y || pos.blockZ != z )
		{
			return false;
		}
		int side = pos.sideHit;
		
		// if the block has hardness
		if( block.getBlockHardness( world, x, y, z ) != 0.0f )
		{
			// decrease item durability
			itemStack.damageItem( DurabilityLostToBlock, entityUser );
		}
		
		final int ZPos = 2; // east
		final int ZNeg = 3; // west
		final int XPos = 4; // north
		final int XNeg = 5; // south
		
		// is the block closer to our feet or our eyes?
		double entityFeetPosY = entityUser.getPosition( 1.0f ).yCoord;
		double entityEyesPosY = entityFeetPosY + entityUser.getEyeHeight();
		double distToFeet = Math.abs( entityFeetPosY - y );
		double distToEyes = Math.abs( entityEyesPosY - y );
		boolean isCloserToFeet = distToFeet < distToEyes;
		int yDelta = isCloserToFeet ? 1 : -1;
		
		// make a list of blocks to dig
		List<ChunkCoordinates> blocksToCheck = new ArrayList<ChunkCoordinates>();
		for( int i=0; i<DigDepth; i++ )
		{
			switch( side )
			{
				case ZPos:
					if( i > 0 )
					{
						blocksToCheck.add( new ChunkCoordinates( x, y, z+i ) );
					}
					blocksToCheck.add( new ChunkCoordinates( x, y+yDelta, z+i ) );
				break;
				
				case ZNeg:
					if( i > 0 )
					{
						blocksToCheck.add( new ChunkCoordinates( x, y, z-i ) );
					}
					blocksToCheck.add( new ChunkCoordinates( x, y+yDelta, z-i ) );
				break;
				
				case XPos:
					if( i > 0 )
					{
						blocksToCheck.add( new ChunkCoordinates( x+i, y, z ) );
					}
					blocksToCheck.add( new ChunkCoordinates( x+i, y+yDelta, z ) );
				break;
				
				case XNeg:
					if( i > 0 )
					{
						blocksToCheck.add( new ChunkCoordinates( x-i, y, z ) );
					}
					blocksToCheck.add( new ChunkCoordinates( x-i, y+yDelta, z ) );
				break;
			}
		}
		
		// dig the extra blocks
		for( ChunkCoordinates coords : blocksToCheck )
		{
			if( isFillerBlock( world.getBlockId( coords.posX, coords.posY, coords.posZ ) ) )
			{
				world.destroyBlock( coords.posX, coords.posY, coords.posZ, true );
			}
		}
		
		return false;
	}
	
	@Override
	public ItemStack onItemRightClick( ItemStack itemStack, World world, EntityPlayer entityUser )
	{
		// start use mode
		entityUser.setItemInUse( itemStack, getMaxItemUseDuration( itemStack ) );
        
		return itemStack;
	}
	
	private void resetDiggingState( )
	{
		m_isDiggingBlock = false;
		m_diggingBlockX = 0;
		m_diggingBlockY = 0;
		m_diggingBlockZ = 0;
		m_blockDamage = 0.0f;
	}
	
	@Override
	public void onPlayerStoppedUsing( ItemStack itemStack, World world, EntityPlayer entityUser, int itemInUseCount )
	{
		resetDiggingState();
	}
	
	private boolean isDelayedUpdate( )
	{
		boolean isDelayed = m_updateDelayTimer == 0;
		
		// update the delay timer
		m_updateDelayTimer = ( m_updateDelayTimer + 1 ) % 4;
		
		return isDelayed;
	}
	
	@Override
	public void onUpdate( ItemStack itemStack, World world, Entity entityUser, int itemInventoryId, boolean isCurrentItem )
	{
		// get the player if possible
		EntityPlayer player = null;
		if( entityUser instanceof EntityPlayer )
		{
			player = (EntityPlayer)entityUser;
		}
		
		// do delayed updates
		if( isDelayedUpdate() )
		{
			if( isCurrentItem && player != null && player.isUsingItem() )
			{
				updateDigging( itemStack, world, player );
			}
			
			// UNDONE: handle oil consumption here
		}
		
		// do tick updates
		if( player != null )
		{
			updateBlockDamage( itemStack, world, player );
		}
	}
	
	private void updateDigging( ItemStack itemStack, World world, EntityPlayer player )
	{
		// find out what we're aiming at
		final boolean HitLiquids = false;
		MovingObjectPosition pos = getMovingObjectPositionFromPlayer( world, player, HitLiquids );
		if( pos == null || pos.typeOfHit != EnumMovingObjectType.TILE )
		{
			resetDiggingState();
			return;
		}
		int x = pos.blockX;
		int y = pos.blockY;
		int z = pos.blockZ;
		Block block = Block.blocksList[world.getBlockId( x, y, z )];
		
		// is this a new block?
		if( !m_isDiggingBlock || m_diggingBlockX != x || m_diggingBlockY != y || m_diggingBlockZ != z )
		{
			m_isDiggingBlock = true;
			m_diggingBlockX = x;
			m_diggingBlockY = y;
			m_diggingBlockZ = z;
			m_blockDamage = 0.0f;
		}
		
		// can we dig this block?
		if( !isFillerBlock( block ) && !isOreBlock( block ) )
		{
			return;
		}
		
		// is this the first time we've dug this block?
		if( m_blockDamage == 0.0f )
		{
			// tell the block it's being dug
			block.onBlockClicked( world, x, y, z, player );
		}
		
		// compute block damage
		float damageDelta = block.getPlayerRelativeBlockHardness( player, world, x, y, z );
		m_blockDamage += damageDelta;
		
		// apply block damage
		if( m_blockDamage >= 1.0f )
		{
			world.destroyBlock( x, y, z, true );
			
			resetDiggingState();
			
			// if the block has hardness
			if( block.getBlockHardness( world, x, y, z ) != 0.0f )
			{
				// decrease durability
				itemStack.damageItem( DurabilityLostToBlock, player );
			}
		}
	}
	
	private void updateBlockDamage( ItemStack itemStack, World world, EntityPlayer player )
	{
		// only update when we're actually digging on the client
		if( m_isDiggingBlock && world.isRemote )
		{
			int damageState = (int)( m_blockDamage*9.0f );
			world.destroyBlockInWorldPartially( player.entityId, m_diggingBlockX, m_diggingBlockY, m_diggingBlockZ, damageState );
		}
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
	public int getMaxItemUseDuration( ItemStack itemStack )
	{
		return MaxItemUseDuration;
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean isFull3D( )
    {
        return true;
    }
	
	@Override
	public float getStrVsBlock( ItemStack stack, Block block, int meta )
	{
		// should be 1-4
		if( isFillerBlock( block ) )
		{
			return FillerEfficiency;
		}
		else if( isOreBlock( block ) && m_isDiggingBlock )
		{
			return OreEfficiency;
		}
		return 0.0f;
	}
}
