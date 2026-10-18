package com.silahmod.entity;

import com.silahmod.WeaponConfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Atilan el bombasi. Teknik olarak kartopu (Snowball) varliginin alt sinifi: boylece sunucuda ozel varlik kaydi
 * gerekmez, istemci onu zaten esyasinin modeliyle cizer (yani havada bomba modeli gorunur, TNT degil).
 * Carptigi anda TNT ile ayni patlamayi yapar (guc 4.0, ExplosionInteraction.TNT).
 */
public class GrenadeProjectile extends Snowball {
	public GrenadeProjectile(Level level, LivingEntity owner, ItemStack stack) {
		super(level, owner, stack);
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.level().isClientSide() && !this.isRemoved() && this.tickCount >= WeaponConfig.GRENADE_MAX_AGE_TICKS) {
			detonate(this.position());
		}
	}

	/** Kartopunun "kar patlamasi" davranisi yerine patla (super.onHit cagrilmaz). */
	@Override
	protected void onHit(HitResult result) {
		if (!this.level().isClientSide() && !this.isRemoved()) {
			detonate(result.getLocation());
		}
	}

	private void detonate(Vec3 pos) {
		if (this.level() instanceof ServerLevel server) {
			this.discard();
			// PrimedTnt ile ayni cagri: kaynak varlik + guc + TNT etkilesimi (blok kirma kurallari TNT gibi)
			server.explode(this, pos.x, pos.y, pos.z, WeaponConfig.GRENADE_POWER, Level.ExplosionInteraction.TNT);
		}
	}
}
