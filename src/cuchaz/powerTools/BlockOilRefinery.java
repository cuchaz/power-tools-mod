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
	public Icon getIcon( int par1, int par2 )
	{
		return par1 == 1 ? m_iconTop : ( par1 == 0 ? m_iconTop : ( par1 != par2 ? blockIcon : m_iconFront ) );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void registerIcons( IconRegister iconRegister )
	{
		// UNDONE: can change textures here based on state
		// UNDONE: actually make these textures
		blockIcon = iconRegister.registerIcon( "powerTools:oilRefinerySide" );
		m_iconFront = iconRegister.registerIcon( "powerTools:oilRefineryFront" );
		m_iconTop = iconRegister.registerIcon( "powerTools:oilRefineryTop" );
	}
}
