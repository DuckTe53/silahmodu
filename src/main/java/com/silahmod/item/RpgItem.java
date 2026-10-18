package com.silahmod.item;

import com.silahmod.RocketManager;
import com.silahmod.WeaponConfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class RpgItem extends GunItem {
	public RpgItem(Properties properties) {
		super(properties, WeaponConfig.RPG_MAG, WeaponConfig.RPG_RESERVE,
				WeaponConfig.RPG_FIRE_DELAY, WeaponConfig.RPG_RELOAD);
	}

	@Override
	public int reloadFrames() {
		return WeaponConfig.RPG_RELOAD_FRAMES;
	}

	@Override
	public String[] reloadSounds() {
		return new String[] { "item.armor.equip_iron", "block.piston.contract", null, "block.dispenser.dispense", null, "item.crossbow.loading_start", null, "block.piston.extend", "item.crossbow.loading_middle", "item.crossbow.loading_end" };
	}

	@Override
	protected boolean hasEmptyModel() {
		return true;
	}

	@Override
	public void fire(ServerPlayer player, ServerLevel level) {
		RocketManager.launch(level, player);
	}
}
