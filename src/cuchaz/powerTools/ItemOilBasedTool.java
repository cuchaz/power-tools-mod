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

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cuchaz.modsShared.perf.DelayTimer;

public class ItemOilBasedTool extends Item {
	
	// settings
	private static final int MaxStackSize = 1;
	private static final int MaxItemUseDuration = 72000;
	
	private static class State {
		public int powerCountdown;
		
		public State() {
			powerCountdown = 0;
		}
	}
	
	// data members
	private int m_oilPowerLength;
	private ToolStates<State> m_states;
	private DelayTimer m_delayTimer;
	
	public ItemOilBasedTool(int oilPowerLength) {
		
		maxStackSize = MaxStackSize;
		setCreativeTab(CreativeTabs.tabTools);
		
		m_oilPowerLength = oilPowerLength;
		m_states = new ToolStates<State>(this);
		m_delayTimer = new DelayTimer(10);
	}
	
	@Override
	public void onUpdate(ItemStack itemStack, World world, Entity entity, int itemInventoryId, boolean isCurrentItem) {
		
		// only update every so often
		if (!m_delayTimer.isDelayedUpdate()) {
			return;
		}
		
		// get the player if possible
		if (!(entity instanceof EntityPlayer)) {
			return;
		}
		EntityPlayer player = (EntityPlayer)entity;
		
		// only update oil consumption
		updateOilConsumption(player);
	}
	
	@Override
	public boolean onEntitySwing(EntityLivingBase entity, ItemStack itemStack) {
		
		// NOTE: this function seems to be purely cosmetic
		final boolean AbortSwing = true;
		final boolean AllowSwing = false;
		
		// get the player if possible
		if (!(entity instanceof EntityPlayer)) {
			return AbortSwing;
		}
		EntityPlayer player = (EntityPlayer)entity;
		
		if (isPowered(player)) {
			return AllowSwing;
		} else {
			return AbortSwing;
		}
	}
	
	@SubscribeEvent
	public void onBlockHardnessEvent(PlayerEvent.BreakSpeed event) {
		
		// get the player
		EntityPlayer player = event.entityPlayer;
		if (player == null) {
			return;
		}
		
		// is the player even wielding this tool?
		ItemStack itemStack = player.getHeldItem();
		if (itemStack == null || itemStack.getItem() != this) {
			return;
		}
		
		// if we're not powered, blocks are infinitely hard
		if (!powerUp(player)) {
			event.newSpeed = 0.0f;
		}
	}
	
	@Override
	public int getMaxItemUseDuration(ItemStack itemStack) {
		return MaxItemUseDuration;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isFull3D() {
		return true;
	}
	
	private void updateOilConsumption(EntityPlayer player) {
		
		if (player.capabilities.isCreativeMode) {
			// don't consume power in creative mode
			return;
		} else {
			// consume power in other modes
			State state = getState(player);
			state.powerCountdown = Math.max(state.powerCountdown - 1, 0);
		}
	}
	
	private State getState(EntityPlayer player) {
		State state = m_states.get(player);
		if (state == null) {
			state = new State();
			m_states.set(player, state);
		}
		return state;
	}
	
	private boolean isPowered(EntityPlayer player) {
		return player.capabilities.isCreativeMode || getState(player).powerCountdown > 0;
	}
	
	private boolean powerUp(EntityPlayer player) {
		
		if (player.capabilities.isCreativeMode) {
			// don't need oil in creative mode
			return true;
		}
		
		State state = getState(player);
		
		// if we're already powered, then we're already powered
		if (state.powerCountdown > 0) {
			return true;
		}
		
		// if we have some oil, then we can power up
		if (consumeOil(player)) {
			state.powerCountdown = m_oilPowerLength;
			return true;
		}
		
		return false;
	}
	
	private boolean consumeOil(EntityPlayer player) {
		List<ItemStack> oilItemStacks = OreDictionary.getOres(PowerTools.Oil);
		
		// do we have an oil stack?
		int oilStackIndex = -1;
		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack itemStack = player.inventory.getStackInSlot(i);
			if (itemStack != null && itemStackInList(oilItemStacks, itemStack)) {
				oilStackIndex = i;
			}
		}
		if (oilStackIndex < 0) {
			return false;
		}
		
		// use 1 oil
		ItemStack oilStack = player.inventory.getStackInSlot(oilStackIndex);
		assert (oilStack.stackSize > 0);
		oilStack.stackSize--;
		
		// remove empty stacks
		if (oilStack.stackSize <= 0) {
			player.inventory.setInventorySlotContents(oilStackIndex, null);
		}
		
		return true;
	}
	
	private boolean itemStackInList(List<ItemStack> stacks, ItemStack stack) {
		for (ItemStack s : stacks) {
			if (s.getItem() == stack.getItem()) {
				return true;
			}
		}
		return false;
	}
}
