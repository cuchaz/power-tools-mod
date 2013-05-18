package cuchaz.powerTools;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
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
		final int Bottom = 0;
		final int Top = 1;
		final int East = 2;
		final int West = 3;
		final int North = 4;
		final int South = 5;
		
		// UNDONE: use metadata to store the "front" side
		// NOTE: for now, front is always north
		switch( side )
		{
			case Top: return m_iconTop;
			
			case North: return m_iconFront;
			
			case East:
			case West: return m_iconSide;
			
			case South:
			case Bottom:
			default: return blockIcon;
		}
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void registerIcons( IconRegister iconRegister )
	{
		// UNDONE: can change textures here based on state
		// UNDONE: actually make these textures
		blockIcon = iconRegister.registerIcon( "powerTools:oilRefineryBack" );
		m_iconFront = iconRegister.registerIcon( "powerTools:oilRefineryFront" );
		m_iconSide = iconRegister.registerIcon( "powerTools:oilRefinerySide" );
		m_iconTop = iconRegister.registerIcon( "powerTools:oilRefineryTop" );
	}
}
