package cuchaz.powerTools;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockOilRefinery extends BlockContainer
{
	@SideOnly( Side.CLIENT )
	private Icon m_iconFront;
	@SideOnly( Side.CLIENT )
	private Icon[] m_iconSide;
	@SideOnly( Side.CLIENT )
	private Icon m_iconTop;
	
	protected BlockOilRefinery( int blockId )
	{
		super( blockId, Material.iron );
		
		setHardness( 5.0F );
		setResistance( 10.0F );
		setStepSound( soundMetalFootstep );
		setUnlocalizedName( "blockOilRefinery" );
		
		m_iconSide = new Icon[] { null, null };
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
		
		int wheelFrame = getMetaWheelFrame( meta );
		switch( targetSide )
		{
			case North: return m_iconFront;
			case West: return m_iconSide[wheelFrame];
			case South: return blockIcon;
			case East: return m_iconSide[wheelFrame];
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
		m_iconSide[0] = iconRegister.registerIcon( "powerTools:oilRefinerySide1" );
		m_iconSide[1] = iconRegister.registerIcon( "powerTools:oilRefinerySide2" );
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
		
		// calculate the ccw north-front rotation
		int rotation = 0;
		while( side != BlockSide.North )
		{
			side = side.getXZNextSide();
			rotation++;
		}
		
		// set the offset as the block meta
		world.setBlockMetadataWithNotify( x, y, z, computeMeta( rotation, 0 ), FlagSendChangeToClients );
    }
	
	@Override
	public boolean onBlockActivated( World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9 )
	{
		// ignore for clients (taken from BlockFurnace... but this seems backwards?)
		if( world.isRemote )
		{
			return true;
		}
		
		TileEntityOilRefinery tileEntity = (TileEntityOilRefinery)world.getBlockTileEntity( x, y, z );
		if( tileEntity != null )
		{
			player.displayGUIChest( tileEntity.getInventory() );
		}
		
		return true;
	}
	
	@Override
	public void breakBlock( World world, int x, int y, int z, int side, int meta )
	{
		Random rand = new Random();
		
		// eject the items from the refinery
		TileEntityOilRefinery tileEntity = (TileEntityOilRefinery)world.getBlockTileEntity( x, y, z );
		for( int i=0; i<tileEntity.getInventory().getSizeInventory(); i++ )
		{
			// get the item in slot i
			ItemStack itemStack = tileEntity.getInventory().getStackInSlot( i );
			if( itemStack == null )
			{
				continue;
			}
			
			// pick a random spot for the item in this block
			double dx = rand.nextDouble() * 0.8 + 0.1;
			double dy = rand.nextDouble() * 0.8 + 0.1;
			double dz = rand.nextDouble() * 0.8 + 0.1;
			
			// pick a random direction for the item
			double variance = 0.05;
			double extraUpSpeed = 0.2f;
			double vx = rand.nextGaussian() * variance;
			double vy = rand.nextGaussian() * variance + extraUpSpeed;
			double vz = rand.nextGaussian() * variance;
			
			// eject the item into the world
			EntityItem entityItem = new EntityItem(
				world,
				dx + x, dy + y, dz + z,
				new ItemStack( itemStack.itemID, itemStack.stackSize, itemStack.getItemDamage() )
			);
			entityItem.motionX = vx;
			entityItem.motionY = vy;
			entityItem.motionZ = vz;
			world.spawnEntityInWorld( entityItem );
		}
		
		super.breakBlock( world, x, y, z, side, meta );
	}
	
	public static int getMetaRotation( int meta )
	{
		return meta & 0x3;
	}
	
	public static int getMetaWheelFrame( int meta )
	{
		return ( meta & 0x4 ) >> 2;
	}
	
	public static int computeMeta( int rotation, int wheelFrame )
	{
		return ( ( wheelFrame & 0x1 ) << 2 ) | ( rotation & 0x3 );
	}
}
