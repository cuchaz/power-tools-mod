package cuchaz.powerTools;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
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
	
	public boolean isUseableByPlayer( EntityPlayer player )
	{
		if( worldObj.getBlockTileEntity( xCoord, yCoord, zCoord ) != this )
		{
			return false;
		}

		return player.getDistanceSq( xCoord + 0.5, yCoord + 0.5, zCoord + 0.5 ) <= 64;
	}
	
	// UNDONE: implement save state stuff
	
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
		
		// TEMP
		System.out.println( "Update: " + ( isPowered() ? "Powered!" : "Not powered... =(" ) );
		
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
			// TEMP
			System.out.println( "Setting wheel frame to: " + newWheelFrame + " (" + BlockOilRefinery.computeMeta( BlockOilRefinery.getMetaRotation( meta ), newWheelFrame ) + ")" );
			
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
		int targetId = Block.waterStill.blockID;
		for( ChunkCoordinates coords : blocks )
		{
			if( worldObj.getBlockId( coords.posX, coords.posY, coords.posZ ) != targetId )
			{
				continue;
			}
			
			// is the block flowing? (meta 1-8 indicates flowing water)
			if( worldObj.getBlockMetadata( coords.posX, coords.posY, coords.posZ ) > 0 )
			{
				return true;
			}
		}
		
		return false;
	}
}
