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
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cuchaz.modsShared.blocks.BlockSet;
import cuchaz.modsShared.blocks.BlockUtils;
import cuchaz.modsShared.blocks.BlockUtils.BlockExplorer;
import cuchaz.modsShared.blocks.BlockUtils.Neighbors;
import cuchaz.modsShared.blocks.Coords;
import cuchaz.modsShared.perf.DelayTimer;

public class TileEntityTreeHarvester extends TileEntity {
	private static final int SearchSize = 5;
	
	private DelayTimer m_delayTimer;
	private List<List<Coords>> m_treeBlocks;
	
	public TileEntityTreeHarvester() {
		m_delayTimer = new DelayTimer(16);
		m_treeBlocks = new ArrayList<List<Coords>>();
	}
	
	public static void spawn(World world, int x, int y, int z) {
		TileEntityTreeHarvester treeHarvester = new TileEntityTreeHarvester();
		world.setTileEntity(x, y, z, treeHarvester);
		treeHarvester.findTree();
	}
	
	public void findTree() {
		m_treeBlocks.clear();
		
		// grab all connected wood/leaf blocks with the same meta up to a few
		// blocks away in the xz plane
		final int targetMeta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord) & 0x3;
		BlockSet blocks = BlockUtils.searchForBlocks(xCoord, yCoord, zCoord, 10000, new BlockExplorer() {
			@Override
			public boolean shouldExploreBlock(Coords coords) {
				
				// is this block wood/leaves?
				Block block = worldObj.getBlock(coords.x, coords.y, coords.z);
				if (!isWoodBlock(block) && isLeavesBlock(block)) {
					return false;
				}
				
				// is this block the same meta?
				int meta = worldObj.getBlockMetadata(coords.x, coords.y, coords.z) & 0x3;
				if (meta != targetMeta) {
					return false;
				}
				
				// is this block within range?
				if (coords.y < yCoord || BlockUtils.getXZManhattanDistance(xCoord, yCoord, zCoord, coords.x, coords.y, coords.z) > SearchSize) {
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
		for (Coords block : blocks) {
			height = Math.max(height, block.y - yCoord + 1);
		}
		
		// make the layers
		for (int i = 0; i < height; i++) {
			m_treeBlocks.add(new ArrayList<Coords>());
		}
		for (Coords block : blocks) {
			m_treeBlocks.get(block.y - yCoord).add(block);
		}
	}
	
	@Override
	public void updateEntity() {
		if (!m_delayTimer.isDelayedUpdate()) {
			return;
		}
		
		// move all the current blocks down one level or harvest them
		Iterator<List<Coords>> iterBlocksThisLayer = m_treeBlocks.iterator();
		while (iterBlocksThisLayer.hasNext()) {
			List<Coords> blocksThisLayer = iterBlocksThisLayer.next();
			
			Iterator<Coords> iterBlock = blocksThisLayer.iterator();
			while (iterBlock.hasNext()) {
				Coords block = iterBlock.next();
				
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
	
	private boolean moveBlockDownOrHarvest(Coords coords) {
		boolean harvestBlock = false;
		
		// is this block at "the bottom" ?
		if (coords.y <= yCoord) {
			harvestBlock = true;
		}
		
		// is there something solid below this block?
		if (!worldObj.isAirBlock(coords.x, coords.y - 1, coords.z)) {
			harvestBlock = true;
		}
		
		if (harvestBlock) {
			// harvest the block: destroyBlock()
			worldObj.func_147480_a(coords.x, coords.y, coords.z, true);
		} else {
			// move the block down one level
			Block block = worldObj.getBlock(coords.x, coords.y, coords.z);
			int meta = worldObj.getBlockMetadata(coords.x, coords.y, coords.z);
			worldObj.setBlockToAir(coords.x, coords.y, coords.z);
			coords.y--;
			worldObj.setBlock(coords.x, coords.y, coords.z, block, meta, 3);
		}
		
		return harvestBlock;
	}
	
	private void despawn() {
		invalidate();
		worldObj.removeTileEntity(xCoord, yCoord, zCoord);
	}
	
	public static boolean isWoodBlock(Block block) {
		return block == Blocks.log || block == Blocks.log2;
	}
	
	public static boolean isLeavesBlock(Block block) {
		return block == Blocks.leaves || block == Blocks.leaves2;
	}
}
