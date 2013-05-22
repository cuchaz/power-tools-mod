package cuchaz.powerTools;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;

public class TileEntityOilRefinery extends TileEntity
{
	private static final String InventoryName = "Oil Refinery";
	private static final int InventorySize = 9; // needs to be a multiple of 9 or the GUI won't work
	
	private InventoryBasic m_inventory;
	private int m_delayCounter;
	
	public TileEntityOilRefinery( )
	{
		m_inventory = new InventoryBasic( InventoryName, false, InventorySize );
		m_delayCounter = 0;
	}
	
	public IInventory getInventory( )
	{
		return m_inventory;
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
		
		// UNDONE: load other state
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
        
        // UNDONE: write other state
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
		if( !isDelayedUpdate() )
		{
			return;
		}
		
		// UNDONE: handle coal processing
		
		// get the wheel frame
		int meta = worldObj.getBlockMetadata( xCoord, yCoord, zCoord );
		int oldWheelFrame = BlockOilRefinery.getMetaWheelFrame( meta );
		
		// update the wheel frame if needed
		int newWheelFrame = 0;
		if( isPowered() )
		{
			// spin the wheels
			newWheelFrame = oldWheelFrame == 0 ? 1 : 0;
		}
		
		// update the metadata if needed
		if( newWheelFrame != oldWheelFrame )
		{
			final int FlagSendChangeToClients = 2;
			worldObj.setBlockMetadataWithNotify(
				xCoord, yCoord, zCoord,
				BlockOilRefinery.computeMeta( BlockOilRefinery.getMetaRotation( meta ), newWheelFrame ),
				FlagSendChangeToClients
			);
		}
	}
	
	public boolean isPowered( )
	{
		// along which axis are the sides?
		boolean sidesAreNorthSouth = BlockOilRefinery.getMetaRotation( worldObj.getBlockMetadata( xCoord, yCoord, zCoord ) ) % 2 == 0;
		
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
}
