package cuchaz.powerTools;

import java.util.Arrays;
import java.util.Comparator;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemDrillShaft extends Item
{
	// settings
	private static final int MaxStackSize = 1;
	private static final int MaxUses = 400;
	private static final int DamageVsEntity = 1; // should be 0-5
	private static final int Enchantability = 10; // should be 0-22
	private static final int DurabilityLostToBlock = 1; // should be small fraction of MaxUses
	private static final int DurabilityLostToEntity = 2; //
	private static final float FillerEfficiency = 8.0f; // 0-12 (2,4,6,8,12 : wood,stone,iron,diamond,gold)
	private static final float OreEfficiency = 0.0f;
	
	private static final int[] FillerBlocks = new int[] {
		Block.cobblestone.blockID,
		Block.stoneDoubleSlab.blockID,
		Block.stoneSingleSlab.blockID,
		Block.stone.blockID,
		Block.sandStone.blockID,
		Block.cobblestoneMossy.blockID,
		Block.ice.blockID,
		Block.netherrack.blockID,
		Block.rail.blockID,
		Block.railDetector.blockID,
		Block.railPowered.blockID,
		Block.railActivator.blockID,
		Block.grass.blockID,
		Block.dirt.blockID,
		Block.sand.blockID,
		Block.gravel.blockID,
		Block.snow.blockID,
		Block.blockSnow.blockID,
		Block.blockClay.blockID,
		Block.tilledField.blockID,
		Block.slowSand.blockID,
		Block.mycelium.blockID
	};
	
	private static final int[] OreBlocks = new int[] {
		Block.oreIron.blockID,
		Block.blockIron.blockID,
		Block.oreCoal.blockID,
		Block.blockGold.blockID,
		Block.oreGold.blockID,
		Block.oreDiamond.blockID,
		Block.blockDiamond.blockID,
		Block.oreLapis.blockID,
		Block.blockLapis.blockID,
		Block.oreRedstone.blockID,
		Block.oreRedstoneGlowing.blockID
	};
	
	static
	{
		// sort the blocklists so we can use binary search
		Arrays.sort( FillerBlocks );
		Arrays.sort( OreBlocks );
	}
	
    public ItemDrillShaft( int itemId )
    {
    	super( itemId );
    	
        maxStackSize = MaxStackSize;
        setUnlocalizedName( "drillShaft" );
        setMaxDamage( MaxUses );
        setCreativeTab( CreativeTabs.tabTools );
    }
    
    @Override
	public void registerIcons( IconRegister iconRegister )
	{
		itemIcon = iconRegister.registerIcon( "powerTools:drillShaft" );
	}
    
    private boolean isFillerBlock( Block block )
    {
    	return isFillerBlock( block.blockID );
    }
    
    private boolean isFillerBlock( int blockId )
    {
    	return Arrays.binarySearch( FillerBlocks, blockId ) >= 0;
    }
    
    private boolean isOreBlock( Block block )
    {
    	return isOreBlock( block.blockID );
    }
    
    private boolean isOreBlock( int blockId )
    {
    	return Arrays.binarySearch( OreBlocks, blockId ) >= 0;
    }
    
    @Override
    public boolean canHarvestBlock( Block block )
    {
    	return isFillerBlock( block ) || isOreBlock( block );
    }
    
    @Override
    public boolean hitEntity( ItemStack itemStack, EntityLiving entityTarget, EntityLiving entityUser )
    {
    	// decrease item durability
        itemStack.damageItem( DurabilityLostToEntity, entityUser );
        
        return true;
    }
    
    @Override
    public boolean onBlockDestroyed( ItemStack itemStack, World world, int blockId, int x, int y, int z, EntityLiving entityUser )
    {
    	// if the block has hardness
    	if( (double)Block.blocksList[blockId].getBlockHardness( world, x, y, z ) != 0.0 )
        {
    		// decrease item durability
    		itemStack.damageItem( DurabilityLostToBlock, entityUser );
        }
    	
    	// UNDONE: destroy nearby blocks
    	// mine this block too
    	int nextX = x+1;
    	int nextY = y;
    	int nextZ = z;
    	if( isFillerBlock( world.getBlockId( nextX, nextY, nextZ ) ) )
    	{
    		world.destroyBlock( nextX, nextY, nextZ, true );
    	}
    	
        return true;
    }
    
    public int getDamageVsEntity( Entity entityTarget )
    {
    	return DamageVsEntity;
    }
    
	public int getItemEnchantability()
	{
    	return Enchantability;
	}
	
    @Override
    public float getStrVsBlock( ItemStack stack, Block block, int meta )
    {
    	// should be 1-4
    	if( isFillerBlock( block ) )
    	{
    		return FillerEfficiency;
    	}
    	else if( isOreBlock( block ) )
    	{
    		return OreEfficiency;
    	}
    	return 0.0f;
    }
}
