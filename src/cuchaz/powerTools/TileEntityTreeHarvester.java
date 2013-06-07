package cuchaz.powerTools;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileEntityTreeHarvester extends TileEntity
{
	private int m_lifespan;
	
	public TileEntityTreeHarvester( )
	{
		// nothing to do
		
		// TEMP
		m_lifespan = 16;
	}
	
	public static void spawn( World world, int x, int y, int z )
	{
		TileEntityTreeHarvester treeHarvester = new TileEntityTreeHarvester();
        world.setBlockTileEntity( x, y, z, treeHarvester );
        treeHarvester.findTree();
	}
	
	public void findTree( )
	{
		// UNDONE: decide what blocks are part of the tree
		
		// TEMP
		System.out.println( ( worldObj.isRemote ? "CLIENT" : "SERVER" ) + " findTree() at " + xCoord + "," + yCoord + "," + zCoord );
	}
	
	@Override
	public void updateEntity( )
	{
		// TEMP: just do a few ticks and then disappear
		m_lifespan--;
		System.out.println( ( worldObj.isRemote ? "CLIENT" : "SERVER" ) + " tick: " + m_lifespan + " " + ( this.isInvalid() ? "INVALID" : "valid" ) );
		if( m_lifespan <= 0 )
		{
			// remove self
			worldObj.removeBlockTileEntity( xCoord, yCoord, zCoord );
			// UNDONE: this doesn't stop updates for some reason...
			// possibly because of
			boolean hasTileEntity = Block.blocksList[worldObj.getBlockId( xCoord, yCoord, zCoord )].hasTileEntity( worldObj.getBlockMetadata( xCoord, yCoord, zCoord ) );
			// try getting the chunk directly and removing the tile entity the hard way
		}
	}
}
