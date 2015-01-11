/*******************************************************************************
 * Copyright (c) 2013 Jeff Martin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Jeff Martin - initial API and implementation
 ******************************************************************************/
package cuchaz.powerTools;

import java.util.TreeMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

public class ToolStates<T extends ToolState<T>> {
	// NOTE: can't id by ItemStack instance. They change on the client sporadically
	// also, beware! Client and Server run in the same JVM in SP mode!
	
	private static class Key implements Comparable<Key> {
		private int entityId;
		private int itemId;
		private boolean isClient;
		
		public Key(EntityPlayer player, Item item) {
			entityId = player.entityId;
			itemId = item.itemID;
			isClient = player.worldObj.isRemote;
		}
		
		@Override
		public int compareTo(Key other) {
			int result = entityId - other.entityId;
			if (result != 0) {
				return result;
			}
			result = itemId - other.itemId;
			if (result != 0) {
				return result;
			}
			return Boolean.valueOf(isClient).compareTo(other.isClient);
		}
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof Key) {
				return equals((Key)other);
			}
			return false;
		}
		
		public boolean equals(Key other) {
			return entityId == other.entityId && itemId == other.itemId && isClient == other.isClient;
		}
	}
	
	private TreeMap<Key,T> m_memory;
	private Item m_tool;
	private T m_defaultState;
	
	public ToolStates(Item tool, T defaultState) {
		m_tool = tool;
		m_memory = new TreeMap<Key,T>();
		m_defaultState = defaultState;
	}
	
	public T getState(EntityPlayer player) {
		Key key = new Key(player, m_tool);
		T state = m_memory.get(key);
		if (state == null) {
			try {
				state = m_defaultState.clone();
				m_memory.put(key, state);
			} catch (CloneNotSupportedException ex) {
				// if this happens, it's a bug and a programmer needs to fix it.
				throw new Error("Tool state is not clonable!", ex);
			}
		}
		return state;
	}
}
