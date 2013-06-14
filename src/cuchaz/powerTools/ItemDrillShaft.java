package cuchaz.powerTools;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cuchaz.modsShared.BlockSide;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class ItemDrillShaft extends ItemDrill
{
	// settings
	private static final int DigDepth = 2;
	
	public ItemDrillShaft( int itemId )
	{
		super( itemId );
		setUnlocalizedName( "drillShaft" );
	}
    
	@Override
	@SideOnly( Side.CLIENT )
	public void registerIcons( IconRegister iconRegister )
	{
		itemIcon = iconRegister.registerIcon( "powerTools:drillShaft" );
	}
	
	@Override
	public List<ChunkCoordinates> getOtherBlocksToDig( World world, int x, int y, int z, int side, EntityPlayer player )
	{
		// is the block closer to our feet or our eyes?
		double entityFeetPosY = player.posY - player.yOffset;
		double entityEyesPosY = entityFeetPosY + player.getEyeHeight();
		double distToFeet = Math.abs( entityFeetPosY - y );
		double distToEyes = Math.abs( entityEyesPosY - y );
		boolean isCloserToFeet = distToFeet < distToEyes;
		int yDelta = isCloserToFeet ? 1 : -1;
		
		// make a list of blocks to dig
		List<ChunkCoordinates> blocksToDig = new ArrayList<ChunkCoordinates>();
		for( int i=0; i<DigDepth; i++ )
		{
			switch( BlockSide.getById( side ) )
			{
				case East:
					if( i > 0 )
					{
						blocksToDig.add( new ChunkCoordinates( x, y, z+i ) );
					}
					blocksToDig.add( new ChunkCoordinates( x, y+yDelta, z+i ) );
				break;
				
				case West:
					if( i > 0 )
					{
						blocksToDig.add( new ChunkCoordinates( x, y, z-i ) );
					}
					blocksToDig.add( new ChunkCoordinates( x, y+yDelta, z-i ) );
				break;
				
				case North:
					if( i > 0 )
					{
						blocksToDig.add( new ChunkCoordinates( x+i, y, z ) );
					}
					blocksToDig.add( new ChunkCoordinates( x+i, y+yDelta, z ) );
				break;
				
				case South:
					if( i > 0 )
					{
						blocksToDig.add( new ChunkCoordinates( x-i, y, z ) );
					}
					blocksToDig.add( new ChunkCoordinates( x-i, y+yDelta, z ) );
				break;
			}
		}
		return blocksToDig;
	}
}
