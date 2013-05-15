package cuchaz.powerTools;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;

@Mod( modid="cuchaz.powerTools", name="Power Tools", version="0.0.1" )
@NetworkMod( clientSideRequired=true, serverSideRequired=false )
public class PowerTools
{
	@Instance( "cuchaz.powerTools" )
	public static PowerTools m_instance;
	
	@SidedProxy( clientSide="cuchaz.powerTools.client.ClientProxy", serverSide="cuchaz.powerTools.server.ServerProxy" )
	public static BaseProxy m_proxy;
	
	@PreInit
	public void preInit( FMLPreInitializationEvent event )
	{
		// UNDONE: implement me
	}
	
	@Init
	public void load( FMLInitializationEvent event )
	{
		m_proxy.registerRenderers();
	}
	
	@PostInit
	public void postInit( FMLPostInitializationEvent event )
	{
		// UNDOME: implement me
	}
}