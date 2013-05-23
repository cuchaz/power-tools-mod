package cuchaz.powerTools;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;

public class TileEntityOilRefinery extends TileEntity
{
	private static final String InventoryName = "Oil Refinery";
	private static final int InventorySize = 9; // needs to be a multiple of 9 or the GUI won't work
	private static final int ProcessingTime = 32;
	private static final int OilPerCoal = 2;
	
	private InventoryBasic m_inventory;
	private int m_delayCounter;
	private int m_processingTimer;
	private int m_wheelFrame;
	private int m_oilFrame;
	
	public TileEntityOilRefinery( )
	{
		m_inventory = new InventoryBasic( InventoryName, false, InventorySize );
		m_delayCounter = 0;
		m_processingTimer = 0;
		m_wheelFrame = 0;
		m_oilFrame = 0;
	}
	
	public IInventory getInventory( )
	{
		return m_inventory;
	}
	
	public int getWheelFrame( )
	{
		return m_wheelFrame;
	}
	
	public int getOilFrame( )
	{
		return m_oilFrame;
	}
	
	@Override
	public void readFromNBT( NBTTagCompound nbt )
	{
		super.readFromNBT( nbt );
		
		// load the items
		NBTTagList tagList = nbt.getTagList( "items" );
		for( int i=0; i<tagList.tagCount(); i++ )
		{
			NBTTagCompound itemNbt = (NBTTagCompound)tagList.tagAt( i );
			byte slot = itemNbt.getByte( "slot" );
			if( slot >= 0 && slot < m_inventory.getSizeInventory() )
			{
				m_inventory.setInventorySlotContents( slot, ItemStack.loadItemStackFromNBT( itemNbt ) );
			}
		}
	}
	
	@Override
	public void writeToNBT( NBTTagCompound nbt )
	{
		super.writeToNBT( nbt );
		
		// save the items
		NBTTagList tagList = new NBTTagList();
		for( int i=0; i<m_inventory.getSizeInventory(); i++ )
		{
			// get the item stack
			ItemStack itemStack = m_inventory.getStackInSlot( i );
			if( itemStack == null )
			{
				continue;
			}
			
			// write it
			NBTTagCompound itemNbt = new NBTTagCompound();
			itemNbt.setByte( "slot", (byte)i );
			itemStack.writeToNBT( itemNbt );
			tagList.appendTag( itemNbt );
		}
        nbt.setTag( "items", tagList );
	}
	
	private boolean isDelayedUpdate( )
	{
		boolean isDelayedUpdate = m_delayCounter == 0;
		m_delayCounter = ( m_delayCounter + 1 ) % 6;
		return isDelayedUpdate;
	}
	
	@Override
	public void updateEntity( )
	{
		// the client never has any inventory!!
		// we have to do all the processing on the server
		if( worldObj.isRemote )
		{
			return;
		}
		
		if( isDelayedUpdate() )
		{
			boolean isPowered = isPowered();
			updateWheels( isPowered );
			updateCoalProcessing( isPowered );
			
			// UNDONE: tell the client the state changed!
			// maybe we need to send a packet?
		}
	}
	
	private void updateWheels( boolean isPowered )
	{
		if( isPowered )
		{
			// spin the wheels
			m_wheelFrame = m_wheelFrame == 0 ? 1 : 0;
		}
	}
	
	private void updateCoalProcessing( boolean isPowered )
	{
		if( isPowered && hasCoal() )
		{
			// calculate the oil frame
			if( m_processingTimer <= 0 )
			{
				m_oilFrame = 0;
			}
			else if( m_processingTimer < ProcessingTime/3 )
			{
				m_oilFrame = 1;
			}
			else if( m_processingTimer < ProcessingTime*2/3 )
			{
				m_oilFrame = 2;
			}
			else
			{
				m_oilFrame = 3;
			}
			
			// did we just finish a processing?
			if( m_processingTimer == ProcessingTime )
			{
				// convert coal into oil
				boolean atLeastOneOilAdded = incrementOil( OilPerCoal );
				if( atLeastOneOilAdded )
				{
					decrementCoal();
				}
				
				m_processingTimer = 0;
			}
			else
			{
				// progress the timer
				m_processingTimer++;
			}
		}
		else
		{
			// reset the timer
			m_processingTimer = 0;
		}
	}
	
	private boolean isPowered( )
	{
		// along which axis are the sides?
		boolean sidesAreNorthSouth = worldObj.getBlockMetadata( xCoord, yCoord, zCoord ) % 2 == 0;
		
		// get a list of the blocks on the sides
		List<ChunkCoordinates> blocks = new ArrayList<ChunkCoordinates>();
		if( sidesAreNorthSouth )
		{
			blocks.add( new ChunkCoordinates( xCoord, yCoord, zCoord + 1 ) );
			blocks.add( new ChunkCoordinates( xCoord, yCoord, zCoord - 1 ) );
		}
		else
		{
			blocks.add( new ChunkCoordinates( xCoord + 1, yCoord, zCoord ) );
			blocks.add( new ChunkCoordinates( xCoord - 1, yCoord, zCoord ) );
		}
		
		// does either side have a flowing water block?
		for( ChunkCoordinates coords : blocks )
		{
			int blockId = worldObj.getBlockId( coords.posX, coords.posY, coords.posZ );
			
			if( blockId == Block.waterStill.blockID )
			{
				// is the block flowing? (meta 1-8 indicates flowing water)
				if( worldObj.getBlockMetadata( coords.posX, coords.posY, coords.posZ ) > 0 )
				{
					return true;
				}
			}
			else if( blockId == Block.waterMoving.blockID )
			{
				// moving water is really falling water, or water that is changing depths
				return true;
			}
		}
		return false;
	}
	
	private boolean hasCoal( )
	{
		return getAnyCoalStackIndex() >= 0;
	}
	
	private void decrementCoal( )
	{
		// find a coal stack
		int stackIndex = getAnyCoalStackIndex();
		if( stackIndex < 0 )
		{
			return;
		}
		
		// get the stack
		ItemStack itemStack = m_inventory.getStackInSlot( stackIndex );
		assert( itemStack.stackSize > 0 );
		
		// decrement the stack
		itemStack.stackSize--;
		
		// remove the empty stack if needed
		if( itemStack.stackSize <= 0 )
		{
			m_inventory.setInventorySlotContents( stackIndex, null );
		}
	}
	
	private int getAnyCoalStackIndex( )
	{
		for( int i=0; i<m_inventory.getSizeInventory(); i++ )
		{
			ItemStack itemStack = m_inventory.getStackInSlot( i );
			if( itemStack != null && itemStack.itemID == Item.coal.itemID )
			{
				return i;
			}
		}
		return -1;
	}
	
	private boolean incrementOil( int quantity )
	{
		boolean someAdded = false;
		for( int i=0; i<quantity; i++ )
		{
			someAdded = incrementOil() || someAdded;
		}
		return someAdded;
	}
	
	private boolean incrementOil( )
	{
		// find an unfull stack to increment
		int targetIndex = getAnyUnfullOilStackIndex();
		if( targetIndex >= 0 )
		{
			m_inventory.getStackInSlot( targetIndex ).stackSize++;
			return true;
		}
		
		// make a new stack in an empty slot
		targetIndex = getAnyEmptyStackIndex();
		if( targetIndex >= 0 )
		{
			m_inventory.setInventorySlotContents( targetIndex, new ItemStack( PowerTools.ItemOil, 1 ) );
			return true;
		}
		
		// nowhere to put new oil
		return false;
	}
	
	private int getAnyUnfullOilStackIndex( )
	{
		for( int i=0; i<m_inventory.getSizeInventory(); i++ )
		{
			ItemStack itemStack = m_inventory.getStackInSlot( i );
			if( itemStack != null && itemStack.itemID == PowerTools.ItemOil.itemID )
			{
				if( itemStack.stackSize < PowerTools.ItemOil.getItemStackLimit() )
				{
					return i;
				}
			}
		}
		return -1;
	}
	
	private int getAnyEmptyStackIndex( )
	{
		for( int i=0; i<m_inventory.getSizeInventory(); i++ )
		{
			if( m_inventory.getStackInSlot( i ) == null )
			{
				return i;
			}
		}
		return -1;
	}
}
