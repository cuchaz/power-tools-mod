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
import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cuchaz.modsShared.BlockSide;

public class ItemDrillWideBore extends ItemDrill
{
	public ItemDrillWideBore( int itemId )
	{
		super( itemId );
		setUnlocalizedName( "drillWideBore" );
	}
    
	@Override
	@SideOnly( Side.CLIENT )
	public void registerIcons( IconRegister iconRegister )
	{
		itemIcon = iconRegister.registerIcon( "powertools:drillWideBore" );
	}
	
	@Override
	public List<ChunkCoordinates> getOtherBlocksToDig( World world, int x, int y, int z, int side, EntityPlayer player )
	{
		// make the 8-block pattern (like the 9-block pattern, but skip 0,0)
		int[] dim1 = { -1, -1, -1, 0, /*0,*/ 0, 1, 1, 1 };
		int[] dim2 = { -1, 0, 1, -1, /*0,*/ 1, -1, 0, 1};
		
		// make a list of blocks to dig
		List<ChunkCoordinates> blocksToDig = new ArrayList<ChunkCoordinates>();
		for( int i=0; i<dim1.length; i++ )
		{
			switch( BlockSide.getById( side ) )
			{
				case North:
				case South:
					blocksToDig.add( new ChunkCoordinates( x + dim1[i], y + dim2[i], z ) );
				break;
				
				case Top:
				case Bottom:
					blocksToDig.add( new ChunkCoordinates( x + dim1[i], y, z + dim2[i] ) );
				break;
				
				case East:
				case West:
					blocksToDig.add( new ChunkCoordinates( x, y + dim1[i], z + dim2[i] ) );
				break;
			}
		}
		return blocksToDig;
	}
}
