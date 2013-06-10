package cuchaz.powerTools;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemJackhammer extends ItemOilBasedTool
{
	// settings
	private static final int MaxUses = 400;
	private static final int DamageVsEntity = 1; // should be 0-5
	private static final int Enchantability = 10; // should be 0-22
	private static final int DurabilityLostToHardBlock = 1; // should be small fraction of MaxUses
	private static final int DurabilityLostToOther = 20; //
	private static final int DurabilityLostToEntity = 5; //
	private static final int OilPowerLength = 35;
	
	private static final Material[] HardMaterials =
	{
		Material.glass,
		Material.ice,
		Material.rock
	};
	
	public ItemJackhammer( int itemId )
	{
		super( itemId, OilPowerLength );
		setMaxDamage( MaxUses );
		setUnlocalizedName( "jackhammer" );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void registerIcons( IconRegister iconRegister )
	{
		itemIcon = iconRegister.registerIcon( "powerTools:jackhammer" );
	}
	
	@Override
	public boolean canHarvestBlock( Block block )
	{
		return isTargetBlock( block );
	}
	
	@Override
	public boolean hitEntity( ItemStack itemStack, EntityLiving entityTarget, EntityLiving entityUser )
	{
		// decrease item durability
		itemStack.damageItem( DurabilityLostToEntity, entityUser );
		
		return true;
	}
	
	@Override
	public boolean onBlockStartBreak( ItemStack itemStack, int x, int y, int z, EntityPlayer player )
	{
		// get the block
		World world = player.worldObj;
		Block block = Block.blocksList[world.getBlockId( x, y, z )];
		
		// if the block has hardness
		if( block.getBlockHardness( world, x, y, z ) != 0.0f )
		{
			// decrease item durability
			itemStack.damageItem( isTargetBlock( block ) ? DurabilityLostToHardBlock : DurabilityLostToOther, player );
		}
		
		return super.onBlockStartBreak( itemStack, x, y, z, player );
	}
	
	@Override
	public int getDamageVsEntity( Entity entityTarget )
	{
		return DamageVsEntity;
	}
	
	@Override
	public int getItemEnchantability( )
	{
		return Enchantability;
	}
	
	@Override
	public float getStrVsBlock( ItemStack stack, Block block, int meta )
	{
		if( isTargetBlock( block ) )
		{
			// efficiency
			// 0-12 (2,4,6,8,12 : wood,stone,iron,diamond,gold)
			
			// hardness
			// stone: 1.5
			// coal,iron,gold ore: 3.0
			// blockIron, blockEmerald: 5.0
			// obsidian: 50.0

			// want 1.5x -> 12
			// want 5.0x -> 40
			// want 50x -> 400
			
			// the harder the block, the better the jackhammer works
			return block.blockHardness * 8;
		}
		return super.getStrVsBlock( stack, block, meta );
	}
	
	private boolean isTargetBlock( Block block )
	{
		for( Material material : HardMaterials )
		{
			if( block.blockMaterial == material )
			{
				return true;
			}
		}
		return false;
	}
}
