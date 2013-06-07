package cuchaz.powerTools;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.PlayerEvent;
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
		
		updateOilConsumption( player );
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
		
		if( isPowered( player ) )
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
		if( !isPowered( player ) )
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
	
	protected void updateOilConsumption( EntityPlayer player )
	{
		State state = m_states.getState( player );
		
		// consume power
		state.powerCountdown = Math.max( state.powerCountdown - 1, 0 );
	}
	
	protected boolean isPowered( EntityPlayer player )
	{
		State state = m_states.getState( player );
		
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
	
	protected EntityPlayer getPlayerFromEntity( Entity entity )
	{
		if( entity instanceof EntityPlayer )
		{
			return (EntityPlayer)entity;
		}
		return null;
	}
}
