/*******************************************************************************
 * Copyright (c) 2013 Jeff Martin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Jeff Martin - initial API and implementation
 ******************************************************************************/
package cuchaz.powerTools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import cuchaz.modsShared.BlockUtils;
import cuchaz.modsShared.BlockUtils.BlockExplorer;
import cuchaz.modsShared.BlockUtils.Neighbors;
import cuchaz.modsShared.DelayTimer;

public class TileEntityTreeHarvester extends TileEntity {
	private static final int SearchSize = 5;
	
	private DelayTimer m_delayTimer;
	private List<List<ChunkCoordinates>> m_treeBlocks;
	
	public TileEntityTreeHarvester() {
		m_delayTimer = new DelayTimer(16);
		m_treeBlocks = new ArrayList<List<ChunkCoordinates>>();
	}
	
	public static void spawn(World world, int x, int y, int z) {
		TileEntityTreeHarvester treeHarvester = new TileEntityTreeHarvester();
		world.setBlockTileEntity(x, y, z, treeHarvester);
		treeHarvester.findTree();
	}
	
	public void findTree() {
		m_treeBlocks.clear();
		
		// grab all connected wood/leaf blocks with the same meta up to a few
		// blocks away in the xz plane
		final int targetMeta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord) & 0x3;
		List<ChunkCoordinates> blocks = BlockUtils.searchForBlocks(xCoord, yCoord, zCoord, 10000, new BlockExplorer() {
			@Override
			public boolean shouldExploreBlock(ChunkCoordinates coords) {
				// is this block wood/leaves?
				int blockId = worldObj.getBlockId(coords.posX, coords.posY, coords.posZ);
				if (blockId != Block.wood.blockID && blockId != Block.leaves.blockID) {
					return false;
				}
				
				// is this block the same meta?
				int meta = worldObj.getBlockMetadata(coords.posX, coords.posY, coords.posZ) & 0x3;
				if (meta != targetMeta) {
					return false;
				}
				
				// is this block within range?
				if (coords.posY < yCoord || BlockUtils.getXZManhattanDistance(xCoord, yCoord, zCoord, coords.posX, coords.posY, coords.posZ) > SearchSize) {
					return false;
				}
				
				return true;
			}
		}, Neighbors.Faces);
		if (blocks == null) {
			return;
		}
		
		// sort the blocks into layers
		
		// get the height
		int height = 0;
		for (ChunkCoordinates block : blocks) {
			height = Math.max(height, block.posY - yCoord + 1);
		}
		
		// make the layers
		for (int i = 0; i < height; i++) {
			m_treeBlocks.add(new ArrayList<ChunkCoordinates>());
		}
		for (ChunkCoordinates block : blocks) {
			m_treeBlocks.get(block.posY - yCoord).add(block);
		}
	}
	
	@Override
	public void updateEntity() {
		if (!m_delayTimer.isDelayedUpdate()) {
			return;
		}
		
		// move all the current blocks down one level or harvest them
		Iterator<List<ChunkCoordinates>> iterBlocksThisLayer = m_treeBlocks.iterator();
		while (iterBlocksThisLayer.hasNext()) {
			List<ChunkCoordinates> blocksThisLayer = iterBlocksThisLayer.next();
			
			Iterator<ChunkCoordinates> iterBlock = blocksThisLayer.iterator();
			while (iterBlock.hasNext()) {
				ChunkCoordinates block = iterBlock.next();
				
				boolean isHarvested = moveBlockDownOrHarvest(block);
				if (isHarvested) {
					iterBlock.remove();
				}
			}
			
			if (blocksThisLayer.isEmpty()) {
				iterBlocksThisLayer.remove();
			}
		}
		
		// if we ran out of tree to move, despawn
		if (m_treeBlocks.isEmpty()) {
			despawn();
		}
	}
	
	private boolean moveBlockDownOrHarvest(ChunkCoordinates block) {
		boolean harvestBlock = false;
		
		// is this block at "the bottom" ?
		if (block.posY <= yCoord) {
			harvestBlock = true;
		}
		
		// is there something solid below this block?
		if (!worldObj.isAirBlock(block.posX, block.posY - 1, block.posZ)) {
			harvestBlock = true;
		}
		
		if (harvestBlock) {
			// harvest the block
			worldObj.destroyBlock(block.posX, block.posY, block.posZ, true);
		} else {
			// move the block down one level
			int blockId = worldObj.getBlockId(block.posX, block.posY, block.posZ);
			int meta = worldObj.getBlockMetadata(block.posX, block.posY, block.posZ);
			worldObj.setBlockToAir(block.posX, block.posY, block.posZ);
			block.posY--;
			worldObj.setBlock(block.posX, block.posY, block.posZ, blockId, meta, 3);
		}
		
		return harvestBlock;
	}
	
	private void despawn() {
		invalidate();
		worldObj.removeBlockTileEntity(xCoord, yCoord, zCoord);
	}
}
