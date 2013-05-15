package cuchaz.powerTools;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemOil extends Item
{
	public ItemOil( int itemId )
	{
		super( itemId );
		
		maxStackSize = 64;
		setCreativeTab( CreativeTabs.tabMaterials );
		setUnlocalizedName( "oil" );
	}
	
	@Override
	public void registerIcons( IconRegister iconRegister )
	{
		itemIcon = iconRegister.registerIcon( "powerTools:oil" );
	}
}
