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
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;

public class TileEntityOilRefinery extends TileEntity
{
	private static final String InventoryName = "Oil Refinery";
	private static final int InventorySize = 9; // needs to be a multiple of 9 or the GUI won't work
	private static final int ProcessingTime = 32;
	private static final int OilPerCoal = 2;
	
	private InventoryBasic m_inventory;
	private DelayTimer m_delayTimer;
	private int m_processingTimer;
	private int m_wheelFrame;
	private int m_oilFrame;
	
	public TileEntityOilRefinery( )
	{
		m_inventory = new InventoryBasic( InventoryName, false, InventorySize );
		m_delayTimer = new DelayTimer( 6 );
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
		
		// load the processing state
		m_processingTimer = nbt.getByte( "processingTimer" );
		m_wheelFrame = nbt.getByte( "wheelFrame" );
		m_oilFrame = nbt.getByte( "oilFrame" );
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
        
        // save the processing state
        nbt.setByte( "processingTimer", (byte)( m_processingTimer & 0xff ) );
        nbt.setByte( "wheelFrame", (byte)( m_wheelFrame & 0xff ) );
        nbt.setByte( "oilFrame", (byte)( m_oilFrame & 0xff ) );
	}
	
	@Override
	public Packet getDescriptionPacket( )
	{
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT( nbt );
		return new Packet132TileEntityData( xCoord, yCoord, zCoord, 0, nbt );
	}
	
	@Override
	public void onDataPacket( INetworkManager net, Packet132TileEntityData packet )
	{
		readFromNBT( packet.customParam1 );
		
		// re-render if we're on the client
		if( worldObj.isRemote )
		{
			worldObj.markBlockForRenderUpdate( xCoord, yCoord, zCoord );
		}
	}
	
	public void onBlockActivated( )
	{
		// NOTE: this is only called on the server
		assert( !worldObj.isRemote );
		
		// if we're done processing, reset the oil frame
		if( m_processingTimer == 0 )
		{
			m_oilFrame = 0;
			
			// update the client
			worldObj.markBlockForUpdate( xCoord, yCoord, zCoord );
		}
	}
	
	@Override
	public void updateEntity( )
	{
		if( m_delayTimer.isDelayedUpdate() )
		{
			boolean isPowered = isPowered();
			boolean wheelsUpdated = updateWheels( isPowered );
			boolean oilUpdated = updateCoalProcessing( isPowered );
			
			// on the client...
			if( worldObj.isRemote )
			{
				if( wheelsUpdated )
				{
					// re-render the block
					worldObj.markBlockForRenderUpdate( xCoord, yCoord, zCoord );
				}
			}
			// on the server...
			else
			{
				if( oilUpdated )
				{
					// update the client
					worldObj.markBlockForUpdate( xCoord, yCoord, zCoord );
				}
			}
		}
	}
	
	private boolean updateWheels( boolean isPowered )
	{
		if( isPowered )
		{
			// spin the wheels
			m_wheelFrame = m_wheelFrame == 0 ? 1 : 0;
			
			return true;
		}
		
		return false;
	}
	
	private boolean updateCoalProcessing( boolean isPowered )
	{
		int newOilFrame = m_oilFrame;
		
		// the client never has any inventory!!
		// we have to do all the processing on the server
		if( worldObj.isRemote )
		{
			return false;
		}
		
		if( isPowered && hasCoal() )
		{
			// calculate the oil frame
			if( m_processingTimer <= ProcessingTime*1/6 )
			{
				newOilFrame = 0;
			}
			else if( m_processingTimer <= ProcessingTime*3/6 )
			{
				newOilFrame = 1;
			}
			else if( m_processingTimer <= ProcessingTime*5/6 )
			{
				newOilFrame = 2;
			}
			else
			{
				newOilFrame = 3;
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
		
		// did we update the animation state?
		if( newOilFrame != m_oilFrame )
		{
			m_oilFrame = newOilFrame;
			return true;
		}
		return false;
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
