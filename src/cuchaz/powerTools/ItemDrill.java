package cuchaz.powerTools;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
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

public abstract class ItemDrill extends Item
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
	private static final int OilPowerLength = 128;
	
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
	private int m_powerCountdown;
	private EntityPlayer m_lastPlayerToHold;
  	
	static
	{
		// sort the blocklists so we can use binary search
		Arrays.sort( FillerBlocks );
		Arrays.sort( OreBlocks );
	}
	
	public ItemDrill( int itemId )
	{
		super( itemId );
		
		maxStackSize = MaxStackSize;
		setMaxDamage( MaxUses );
		setCreativeTab( CreativeTabs.tabTools );
		
		m_updateDelayTimer = 0;
		resetDiggingState();
		m_powerCountdown = 0;
		m_lastPlayerToHold = null;
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
	public boolean onEntitySwing( EntityLiving entityLiving, ItemStack itemStack )
	{
		//final boolean AbortSwing = true;
		final boolean AllowSwing = false;
		
		return AllowSwing;
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
		for( ChunkCoordinates coords : getOtherBlocksToDig( world, x, y, z, side, player ) )
		{
			if( isFillerBlock( world.getBlockId( coords.posX, coords.posY, coords.posZ ) ) )
			{
				world.destroyBlock( coords.posX, coords.posY, coords.posZ, true );
			}
		}
		
		return false;
	}
	
	protected abstract List<ChunkCoordinates> getOtherBlocksToDig( World world, int x, int y, int z, int side, EntityPlayer player );
	
	@Override
	public ItemStack onItemRightClick( ItemStack itemStack, World world, EntityPlayer entityUser )
	{
		// start use mode
		entityUser.setItemInUse( itemStack, getMaxItemUseDuration( itemStack ) );
        
		return itemStack;
	}
	
	@Override
	public void onPlayerStoppedUsing( ItemStack itemStack, World world, EntityPlayer entityUser, int itemInUseCount )
	{
		resetDiggingState();
	}
	
	private void resetDiggingState( )
	{
		m_isDiggingBlock = false;
		m_diggingBlockX = 0;
		m_diggingBlockY = 0;
		m_diggingBlockZ = 0;
		m_blockDamage = 0.0f;
	}
	
	private boolean isDelayedUpdate( )
	{
		boolean isDelayed = m_updateDelayTimer == 0;
		m_updateDelayTimer = ( m_updateDelayTimer + 1 ) % 2;
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
		m_lastPlayerToHold = player;
		
		// do delayed updates
		if( isDelayedUpdate() )
		{
			if( isCurrentItem && player != null && player.isUsingItem() )
			{
				updateDigging( itemStack, world, player );
			}
			
			updateOilConsumption( player );
		}
		
		// do tick updates
		if( player != null )
		{
			// only on the client...
			if( world.isRemote )
			{
				updateBlockDamage( itemStack, world, player );
			}
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
			resetDiggingState();
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
			
			// if the block has hardness
			if( block.getBlockHardness( world, x, y, z ) != 0.0f )
			{
				// decrease durability
				itemStack.damageItem( DurabilityLostToBlock, player );
			}
		}
	}
	
	private void updateOilConsumption( EntityPlayer player )
	{
		// consume power
		m_powerCountdown = Math.max( m_powerCountdown - 1, 0 );
	}
	
	@SideOnly( Side.CLIENT )
	private void updateBlockDamage( ItemStack itemStack, World world, EntityPlayer player )
	{
		// only update when we're actually digging
		if( m_isDiggingBlock )
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
		if( isPowered( m_lastPlayerToHold ) )
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
		}
		return 0.0f;
	}
	
	private boolean isPowered( EntityPlayer player )
	{
		if( player == null )
		{
			return false;
		}
		
		// if we're already powered, then we're already powered
		if( m_powerCountdown > 0 )
		{
			return true;
		}
		
		// if we have some oil, then we're powered
		if( consumeOil( player ) )
		{
			m_powerCountdown = OilPowerLength;
			return true;
		}
		
		return false;
	}
	
	private boolean consumeOil( EntityPlayer player )
	{
		// do we have an oil stack?
		int oilStackIndex = -1;
		for( int i=0; i<player.inventory.getSizeInventory(); i++ )
		{
			ItemStack itemStack = player.inventory.getStackInSlot( i );
			if( itemStack != null && itemStack.itemID == PowerTools.ItemOil.itemID )
			{
				oilStackIndex = i;
			}
		}
		if( oilStackIndex < 0 )
		{
			return false;
		}
		
		// use 1 oil
		ItemStack oilStack = player.inventory.getStackInSlot( oilStackIndex );
		assert( oilStack.stackSize > 0 );
		oilStack.stackSize--;
		
		// remove empty stacks
		if( oilStack.stackSize <= 0 )
		{
			player.inventory.setInventorySlotContents( oilStackIndex, null );
		}
		
		return true;
	}
}
