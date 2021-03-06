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

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cuchaz.modsShared.EntityUtils;
import cuchaz.modsShared.blocks.BlockSet;
import cuchaz.modsShared.blocks.BlockSide;
import cuchaz.modsShared.blocks.Coords;

public class ItemDrillShaft extends ItemDrill {
	
	// settings
	private static final int DigDepth = 2;
	
	public ItemDrillShaft() {
		setUnlocalizedName("cuchaz.powerTools.drillShaft");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		itemIcon = iconRegister.registerIcon("powertools:drillShaft");
	}
	
	@Override
	public BlockSet getOtherBlocksToDig(World world, int x, int y, int z, int side, EntityPlayer player) {
		
		// is the block closer to our feet or our eyes?
		double entityFeetPosY = player.posY - player.yOffset;
		double entityEyesPosY = EntityUtils.getPlayerEyePos(player).yCoord;
		double distToFeet = Math.abs(entityFeetPosY - y);
		double distToEyes = Math.abs(entityEyesPosY - y);
		boolean isCloserToFeet = distToFeet < distToEyes;
		int yDelta = isCloserToFeet ? 1 : -1;
		
		// make a list of blocks to dig
		BlockSet blocksToDig = new BlockSet();
		for (int i = 0; i < DigDepth; i++) {
			switch (BlockSide.getById(side)) {
				case East:
					if (i > 0) {
						blocksToDig.add(new Coords(x - i, y, z));
					}
					blocksToDig.add(new Coords(x - i, y + yDelta, z));
				break;
				
				case West:
					if (i > 0) {
						blocksToDig.add(new Coords(x + i, y, z));
					}
					blocksToDig.add(new Coords(x + i, y + yDelta, z));
				break;
				
				case North:
					if (i > 0) {
						blocksToDig.add(new Coords(x, y, z + i));
					}
					blocksToDig.add(new Coords(x, y + yDelta, z + i));
				break;
				
				case South:
					if (i > 0) {
						blocksToDig.add(new Coords(x, y, z - i));
					}
					blocksToDig.add(new Coords(x, y + yDelta, z - i));
				break;
				
				default:
					break;
			}
		}
		return blocksToDig;
	}
}
