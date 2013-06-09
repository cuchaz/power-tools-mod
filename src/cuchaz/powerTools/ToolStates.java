package cuchaz.powerTools;

import java.util.HashMap;

import net.minecraft.item.ItemStack;

public class ToolStates<T extends ToolState<T>>
{
	private HashMap<ItemStack,T> m_memory;
	private T m_defaultState;
	
	public ToolStates( T defaultState )
	{
		m_memory = new HashMap<ItemStack,T>();
		m_defaultState = defaultState;
	}
	
	public T getState( ItemStack itemStack )
	{
		T state = m_memory.get( itemStack );
		if( state == null )
		{
			try
			{
				state = m_defaultState.clone();
				m_memory.put( itemStack, state );
			}
			catch( CloneNotSupportedException ex )
			{
				// if this happens, it's a bug and a programmer needs to fix it.
				throw new Error( "Tool state is not clonable!", ex );
			}
		}
		return state;
	}
}
