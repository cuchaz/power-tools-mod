package cuchaz.powerTools;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemOilBasedTool extends Item
{
	// settings
	private static final int MaxStackSize = 1;
	private static final int MaxItemUseDuration = 72000;
	
	private static class State implements ToolState<State>
	{
		public int powerCountdown;
		
		public State( )
		{
			powerCountdown = 0;
		}
		
		public State clone( )
		throws CloneNotSupportedException
		{
			return (State)super.clone();
		}
	}
	
	// data members
	private int m_oilPowerLength;
	private ToolStates<State> m_states;
	
	public ItemOilBasedTool( int itemId, int oilPowerLength )
	{
		super( itemId );
		
		maxStackSize = MaxStackSize;
		setCreativeTab( CreativeTabs.tabTools );
		
		m_oilPowerLength = oilPowerLength;
		m_states = new ToolStates<State>( new State() );
	}
	
	@Override
	public void onUpdate( ItemStack itemStack, World world, Entity entityUser, int itemInventoryId, boolean isCurrentItem )
	{
		// get the player if possible
		EntityPlayer player = getPlayerFromEntity( entityUser );
		if( player == null )
		{
			return;
		}
		
		updateOilConsumption( player, itemStack );
	}
	
	@Override
	public boolean onEntitySwing( EntityLiving entityLiving, ItemStack itemStack )
	{
		final boolean AbortSwing = true;
		final boolean AllowSwing = false;
		
		// get the player if possible
		EntityPlayer player = getPlayerFromEntity( entityLiving );
		if( player == null )
		{
			return AllowSwing;
		}
		
		if( isPowered( player, itemStack ) )
		{
			return AllowSwing;
		}
		else
		{
			return AbortSwing;
		}
	}
	
	@ForgeSubscribe
	public void onBlockHardnessEvent( PlayerEvent.BreakSpeed event )
	{
		// get the player if possible
		EntityPlayer player = event.entityPlayer;
		if( player == null )
		{
			return;
		}
		
		// is the player even wielding this tool?
		if( player.getHeldItem() != null && player.getHeldItem().getItem().itemID != itemID )
		{
			return;
		}
		
		// if we're not powered, blocks are infinitely hard
		if( !isPowered( player, player.getHeldItem() ) )
		{
			event.newSpeed = 0.0f;
		}
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
	
	protected void updateOilConsumption( EntityPlayer player, ItemStack itemStack )
	{
		State state = m_states.getState( itemStack );
		
		// consume power
		state.powerCountdown = Math.max( state.powerCountdown - 1, 0 );
		
		// UNDONE: need to fix client/server sync on item consumption
		// maybe only consume items on the server, but keep power timer on client?
		
		// TEMP
		if( state.powerCountdown > 0 )
		{
			System.out.println( ( player.worldObj.isRemote ? "CLIENT" : "SERVER" ) + ": " + getClass().getSimpleName() + ": oil power: " + state.powerCountdown );
		}
	}
	
	protected boolean isPowered( EntityPlayer player, ItemStack itemStack )
	{
		State state = m_states.getState( itemStack );
		
		// if we're already powered, then we're already powered
		if( state.powerCountdown > 0 )
		{
			return true;
		}
		
		// if we have some oil, then we're powered
		if( consumeOil( player ) )
		{
			state.powerCountdown = m_oilPowerLength;
			return true;
		}
		
		return false;
	}
	
	private boolean consumeOil( EntityPlayer player )
	{
		List<ItemStack> oilItemStacks = OreDictionary.getOres( PowerTools.Oil );
		
		// do we have an oil stack?
		int oilStackIndex = -1;
		for( int i=0; i<player.inventory.getSizeInventory(); i++ )
		{
			ItemStack itemStack = player.inventory.getStackInSlot( i );
			if( itemStack != null && itemStackInList( oilItemStacks, itemStack ) )
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
		
		// TEMP
		System.out.println( ( player.worldObj.isRemote ? "CLIENT" : "SERVER" ) + ": " + getClass().getSimpleName() + ": consumed 1 oil! " + oilStack.stackSize + " oil remaining." );
		
		// remove empty stacks
		if( oilStack.stackSize <= 0 )
		{
			player.inventory.setInventorySlotContents( oilStackIndex, null );
		}
		
		return true;
	}
	
	private boolean itemStackInList( List<ItemStack> stacks, ItemStack stack )
	{
		for( ItemStack s : stacks )
		{
			if( s.getItem().itemID == stack.getItem().itemID )
			{
				return true;
			}
		}
		return false;
	}

	protected EntityPlayer getPlayerFromEntity( Entity entity )
	{
		if( entity instanceof EntityPlayer )
		{
			return (EntityPlayer)entity;
		}
		return null;
	}
}
