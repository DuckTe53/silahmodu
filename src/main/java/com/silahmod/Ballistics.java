package com.silahmod;

import java.util.Optional;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;

/** Anlik vurus (hitscan) hesaplari ve hasar verme yardimcilari. */
public final class Ballistics {
	private Ballistics() {}

	/** Hedefin kafa bolgesi: goz hizasindan biraz asagisindan yukarisi. */
	public static boolean isHeadHit(Entity target, Vec3 hitPos) {
		return target instanceof LivingEntity && hitPos.y >= target.getEyeY() - 0.3;
	}

	public record Result(Vec3 end, Entity entity, Vec3 entityHitPos, boolean head) {}

	/** Bakilan yone isin atar; once blok, sonra en yakin varlik. */
	public static Result trace(ServerLevel level, Entity shooter, double range) {
		Vec3 eye = shooter.getEyePosition();
		Vec3 dir = shooter.getLookAngle();
		Vec3 end = eye.add(dir.scale(range));

		BlockHitResult blockHit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));
		if (blockHit.getType() != HitResult.Type.MISS) {
			end = blockHit.getLocation();
		}

		AABB searchBox = new AABB(eye, end).inflate(1.0);
		Entity best = null;
		Vec3 bestPos = null;
		double bestDist = Double.MAX_VALUE;

		for (Entity candidate : level.getEntities(shooter, searchBox, e -> e.isPickable() && !e.isSpectator())) {
			AABB box = candidate.getBoundingBox().inflate(candidate.getPickRadius());
			Optional<Vec3> hit = box.clip(eye, end);
			if (hit.isPresent()) {
				double dist = eye.distanceToSqr(hit.get());
				if (dist < bestDist) {
					bestDist = dist;
					best = candidate;
					bestPos = hit.get();
				}
			}
		}

		if (best != null) {
			return new Result(bestPos, best, bestPos, isHeadHit(best, bestPos));
		}
		return new Result(end, null, null, false);
	}

	/** Tabanca / keskin nisanci atisi. */
	public static void shoot(ServerLevel level, ServerPlayer shooter, double range, float headDamage, float bodyDamage, String shotSound, float pitch) {
		Result result = trace(level, shooter, range);

		// Mermi izi (parcacik)
		Vec3 eye = shooter.getEyePosition();
		Vec3 path = result.end().subtract(eye);
		double length = path.length();
		if (length > 0.001) {
			Vec3 step = path.scale(1.0 / length);
			int points = (int) Math.min(80, length / 1.5);
			for (int i = 2; i < points; i++) {
				Vec3 p = eye.add(step.scale(i * 1.5));
				level.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 1, 0, 0, 0, 0);
			}
		}

		Sfx.play(level, shooter.getX(), shooter.getEyeY(), shooter.getZ(), shotSound, 3.0F, pitch);

		if (result.entity() != null) {
			float damage = result.head() ? headDamage
					: bodyDamage * (result.entity() instanceof LivingEntity living ? Armors.bodyFactor(living) : 1.0F);
			damage(level, shooter, result.entity(), damage);
			if (result.head()) {
				shooter.displayClientMessage(Component.translatable("message.silahmod.headshot"), true);
			}
		}
	}

	/** Bekleme suresini (hasar sonrasi dokunulmazlik) sifirlayip hasar uygular. */
	public static void damage(ServerLevel level, ServerPlayer attacker, Entity target, float amount) {
		DamageSource source = level.damageSources().playerAttack(attacker);
		if (target instanceof LivingEntity living) {
			living.invulnerableTime = 0;
		}
		target.hurtServer(level, source, amount);
	}
}
