package com.silahmod.item;

import com.silahmod.Ballistics;
import com.silahmod.WeaponConfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class RevolverItem extends GunItem {
	public RevolverItem(Properties properties) {
		super(properties, WeaponConfig.REVOLVER_MAG, WeaponConfig.REVOLVER_RESERVE,
				WeaponConfig.REVOLVER_FIRE_DELAY, WeaponConfig.REVOLVER_RELOAD);
	}

	@Override
	public int reloadFrames() {
		return WeaponConfig.REVOLVER_RELOAD_FRAMES;
	}

	@Override
	public String[] reloadSounds() {
		return new String[] { "item.crossbow.loading_start", "block.iron_trapdoor.open", null, "block.piston.extend", null, "item.armor.equip_iron", "block.lever.click", "block.iron_trapdoor.close", "item.crossbow.loading_end" };
	}

	@Override
	public void fire(ServerPlayer player, ServerLevel level) {
		Ballistics.shoot(level, player, WeaponConfig.REVOLVER_RANGE,
				WeaponConfig.REVOLVER_HEAD, WeaponConfig.REVOLVER_BODY,
				"entity.firework_rocket.blast", 1.3F);
	}
}
