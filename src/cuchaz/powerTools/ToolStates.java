package cuchaz.powerTools;

import java.util.HashMap;
import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayer;

public class ToolStates<T extends ToolState<T>> implements Iterable<EntityPlayer>
{
	private HashMap<EntityPlayer,T> m_memory;
	private T m_defaultState;
	
	public ToolStates( T defaultState )
	{
		m_memory = new HashMap<EntityPlayer,T>();
		m_defaultState = defaultState;
	}
	
	public void setState( EntityPlayer player, T state )
	{
		m_memory.put( player, state );
	}
	
	public T getState( EntityPlayer player )
	{
		T state = m_memory.get( player );
		if( state == null )
		{
			try
			{
				state = m_defaultState.clone();
				m_memory.put( player, state );
			}
			catch( CloneNotSupportedException ex )
			{
				// if this happens, it's a bug and a programmer needs to fix it.
				throw new Error( "Tool state is not clonable!", ex );
			}
		}
		return state;
	}
	
	@Override
	public Iterator<EntityPlayer> iterator( )
	{
		return m_memory.keySet().iterator();
	}
}
