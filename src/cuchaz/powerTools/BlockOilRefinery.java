package cuchaz.powerTools;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockOilRefinery extends BlockContainer
{
	// based on BlockFurnace
	
	@SideOnly( Side.CLIENT )
	private Icon m_iconFront;
	@SideOnly( Side.CLIENT )
	private Icon m_iconSide;
	@SideOnly( Side.CLIENT )
	private Icon m_iconTop;
	
	protected BlockOilRefinery( int blockId )
	{
		super( blockId, Material.iron );
	}
	
	@Override
	public TileEntity createNewTileEntity( World world )
	{
		return new TileEntityOilRefinery();
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public Icon getIcon( int side, int meta )
	{
		// top and bottom are easy
		BlockSide targetSide = BlockSide.getById( side );
		switch( targetSide )
		{
			case Top: return m_iconTop;
			case Bottom: return blockIcon;
		}
		
		// rotate the side using the offset
		int offset = meta;
		for( int i=0; i<offset; i++ )
		{
			targetSide = targetSide.getXZNextSide();
		}
		
		switch( targetSide )
		{
			case North: return m_iconFront;
			case West: return m_iconSide;
			case South: return blockIcon;
			case East: return m_iconSide;
		}
		
		// if all else fails
		return blockIcon;
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void registerIcons( IconRegister iconRegister )
	{
		// UNDONE: can change textures here based on state
		blockIcon = iconRegister.registerIcon( "powerTools:oilRefineryBack" );
		m_iconFront = iconRegister.registerIcon( "powerTools:oilRefineryFront" );
		m_iconSide = iconRegister.registerIcon( "powerTools:oilRefinerySide" );
		m_iconTop = iconRegister.registerIcon( "powerTools:oilRefineryTop" );
	}
	
	public void onBlockPlacedBy( World world, int x, int y, int z, EntityLiving entityUser, ItemStack itemStack )
    {
		//final int FlagCauseBlockUpdate = 1;
		final int FlagSendChangeToClients = 2;
		//final int FlagPreventReRender = 3;
		
		// find the side facing the entity
		int quadrant = MathHelper.floor_double((double)( entityUser.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		BlockSide side = null;
		switch( quadrant )
		{
			case 0:
				side = BlockSide.East;
			break;
			case 1:
				side = BlockSide.South;
			break;
			case 2:
				side = BlockSide.West;
			break;
			case 3:
				side = BlockSide.North;
			break;
		}
		
		// calculate the ccw north-front offset
		int offset = 0;
		while( side != BlockSide.North )
		{
			side = side.getXZNextSide();
			offset++;
		}
		
		// set the offset as the block meta
		world.setBlockMetadataWithNotify( x, y, z, offset, FlagSendChangeToClients );
    }
}
