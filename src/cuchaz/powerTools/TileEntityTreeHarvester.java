package cuchaz.powerTools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import cuchaz.modsShared.DelayTimer;

public class TileEntityTreeHarvester extends TileEntity
{
	private static final int SearchSize = 5;
	
	private DelayTimer m_delayTimer;
	private List<List<ChunkCoordinates>> m_treeBlocks;
	
	public TileEntityTreeHarvester( )
	{
		m_delayTimer = new DelayTimer( 16 );
		m_treeBlocks = new ArrayList<List<ChunkCoordinates>>();
	}
	
	public static void spawn( World world, int x, int y, int z )
	{
		TileEntityTreeHarvester treeHarvester = new TileEntityTreeHarvester();
        world.setBlockTileEntity( x, y, z, treeHarvester );
        treeHarvester.findTree();
	}
	
	public void findTree( )
	{
		ChunkCoordinates sourceBlock = new ChunkCoordinates( xCoord, yCoord, zCoord );
		int targetMeta = worldObj.getBlockMetadata( xCoord, yCoord, zCoord ) & 0x3;
		
		// grab all connected wood/leaf blocks with the same meta up to a few blocks away in the xz plane
		// using BFS
		ChunkCoordinates origin = new ChunkCoordinates( xCoord, yCoord, zCoord );
		LinkedHashSet<ChunkCoordinates> queue = new LinkedHashSet<ChunkCoordinates>();
		HashSet<ChunkCoordinates> visitedBlocks = new HashSet<ChunkCoordinates>();
		queue.add( origin );
		
		while( !queue.isEmpty() )
		{
			// get the block and visit it
			ChunkCoordinates block = queue.iterator().next();
			queue.remove( block );
			visitedBlocks.add( block );
			
			// is this block wood/leaves?
			int blockId = worldObj.getBlockId( block.posX, block.posY, block.posZ );
			if( blockId != Block.wood.blockID && blockId != Block.leaves.blockID )
			{
				continue;
			}
			
			// is this block the same meta?
			int meta = worldObj.getBlockMetadata( block.posX, block.posY, block.posZ ) & 0x3;
			if( meta != targetMeta )
			{
				continue;
			}
			
			// is this block within range?
			if( block.posY < yCoord || getXZManhattanDistance( block ) > SearchSize )
			{
				continue;
			}
			
			// this is a target block! Add it to the list
			
			// make space for this layer if needed
			int height = block.posY - yCoord;
			assert( height <= m_treeBlocks.size() );
			if( height == m_treeBlocks.size() )
			{
				m_treeBlocks.add( new ArrayList<ChunkCoordinates>() );
			}
			
			// finally, add the block (as long as it's not the source block. That one will disappear soon!)
			if( !block.equals( sourceBlock ) )
			{
				m_treeBlocks.get( height ).add( block );
			}
			
			// queue up the block's neighbors
			List<ChunkCoordinates> neighbors = new ArrayList<ChunkCoordinates>();
			for( int dx : new int[] { -1, 1 } )
			{
				neighbors.add( new ChunkCoordinates( block.posX + dx, block.posY, block.posZ ) );
			}
			for( int dy : new int[] { -1, 1 } )
			{
				neighbors.add( new ChunkCoordinates( block.posX, block.posY + dy, block.posZ ) );
			}
			for( int dz : new int[] { -1, 1 } )
			{
				neighbors.add( new ChunkCoordinates( block.posX, block.posY, block.posZ + dz ) );
			}
			
			for( ChunkCoordinates neighbor : neighbors )
			{
				if( !visitedBlocks.contains( neighbor ) && !queue.contains( neighbor ) )
				{
					queue.add( neighbor );
				}
			}
		}
	}
	
	@Override
	public void updateEntity( )
	{
		if( !m_delayTimer.isDelayedUpdate() )
		{
			return;
		}
		
		// move all the current blocks down one level or harvest them
		Iterator<List<ChunkCoordinates>> iterBlocksThisLayer = m_treeBlocks.iterator();
		while( iterBlocksThisLayer.hasNext() )
		{
			List<ChunkCoordinates> blocksThisLayer = iterBlocksThisLayer.next();
			
			Iterator<ChunkCoordinates> iterBlock =  blocksThisLayer.iterator();
			while( iterBlock.hasNext() )
			{
				ChunkCoordinates block = iterBlock.next();
				
				boolean isHarvested = moveBlockDownOrHarvest( block );
				if( isHarvested )
				{
					iterBlock.remove();
				}
			}
			
			if( blocksThisLayer.isEmpty() )
			{
				iterBlocksThisLayer.remove();
			}
		}
		
		// if we ran out of tree to move, despawn
		if( m_treeBlocks.isEmpty() )
		{
			despawn();
		}
	}
	
	private int getXZManhattanDistance( ChunkCoordinates block )
	{
		return Math.abs( block.posX - xCoord ) + Math.abs( block.posZ - zCoord );
	}
	
	private boolean moveBlockDownOrHarvest( ChunkCoordinates block )
	{
		boolean harvestBlock = false;
		
		// is this block at "the bottom" ?
		if( block.posY <= yCoord )
		{
			harvestBlock = true;
		}
		
		// is there something solid below this block?
		if( !worldObj.isAirBlock( block.posX, block.posY - 1, block.posZ ) )
		{
			harvestBlock = true;
		}
		
		if( harvestBlock )
		{
			// harvest the block
			worldObj.destroyBlock( block.posX, block.posY, block.posZ, true );
		}
		else
		{
			// move the block down one level
			int blockId = worldObj.getBlockId( block.posX, block.posY, block.posZ );
			int meta = worldObj.getBlockMetadata( block.posX, block.posY, block.posZ );
			worldObj.setBlockToAir( block.posX, block.posY, block.posZ );
			block.posY--;
			worldObj.setBlock( block.posX, block.posY, block.posZ, blockId, meta, 3 );
		}
		
		return harvestBlock;
	}
	
	private void despawn( )
	{
		invalidate();
		worldObj.removeBlockTileEntity( xCoord, yCoord, zCoord );
	}
}