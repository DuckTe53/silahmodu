package com.silahmod.client;

import com.silahmod.WeaponConfig;
import com.silahmod.item.SniperItem;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Sniper'da durbun acan oyuncunun durbunu yanip soner (lens parlamasi).
 * Sadece BASKA oyuncular gorur; durbun acan kisi kendi isigini gormez.
 *
 * Ag paketi gerekmez: "esya kullaniliyor" durumu zaten tum istemcilere senkronize edilir
 * (vanilla durbunde de baskalari kolunu kaldirdigini boyle gorur).
 */
public final class ScopeGlint {
	private ScopeGlint() {}

	private static final DustParticleOptions GLINT =
			new DustParticleOptions(WeaponConfig.SNIPER_GLINT_COLOR, WeaponConfig.SNIPER_GLINT_SIZE);

	public static void tick(Minecraft client) {
		if (!WeaponConfig.SNIPER_GLINT || client.level == null || client.player == null) {
			return;
		}
		if (client.level.getGameTime() % Math.max(1, WeaponConfig.SNIPER_GLINT_INTERVAL) != 0) {
			return;
		}
		for (Player other : client.level.players()) {
			if (other == client.player || other.isSpectator() || !other.isAlive()) {
				continue; // kendi isigini gorme
			}
			if (!other.isUsingItem() || !(other.getUseItem().getItem() instanceof SniperItem)) {
				continue;
			}
			Vec3 look = other.getLookAngle();
			Vec3 right = look.cross(new Vec3(0.0, 1.0, 0.0));
			right = right.lengthSqr() < 1.0E-4 ? Vec3.ZERO : right.normalize();
			// Silahin ustundeki durbun: gozun biraz onunde, sagda ve asagida
			Vec3 pos = other.getEyePosition().add(look.scale(0.9)).add(right.scale(0.25)).add(0.0, -0.3, 0.0);
			// "Always visible": uzaktan da gorunsun (vanilla parcaciklar ~32 blokta kaybolur)
			client.level.addAlwaysVisibleParticle(GLINT, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
		}
	}
}
