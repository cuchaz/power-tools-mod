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

import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

import com.google.common.collect.Maps;

import cuchaz.modsShared.math.HashCalculator;

public class ToolStates<T> {
	
	private static class StateKey {
		private int entityId;
		private int itemId;
		private boolean isClient;
		
		public StateKey(EntityPlayer player, Item item) {
			entityId = player.getEntityId();
			itemId = Item.getIdFromItem(item);
			isClient = player.worldObj.isRemote;
		}
		
		@Override
		public int hashCode() {
			return HashCalculator.hashIds(entityId, itemId, isClient ? 1 : 0);
		}
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof StateKey) {
				return equals((StateKey)other);
			}
			return false;
		}
		
		public boolean equals(StateKey other) {
			return entityId == other.entityId && itemId == other.itemId && isClient == other.isClient;
		}
	}
	
	private Map<StateKey,T> m_states;
	private Item m_tool;
	
	public ToolStates(Item tool) {
		m_tool = tool;
		m_states = Maps.newHashMap();
	}
	
	public T get(EntityPlayer player) {
		return m_states.get(new StateKey(player, m_tool));
	}
	
	public void set(EntityPlayer player, T state) {
		m_states.put(new StateKey(player, m_tool), state);
	}
}
