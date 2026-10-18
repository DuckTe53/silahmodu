package com.silahmod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * RPG roketleri. Ozel varlik (entity) yerine sunucuda simule edilir; boylece
 * ayri bir renderer gerekmez. Giderken arkasindan ates ve duman parcacigi birakir.
 */
public final class RocketManager {
	private RocketManager() {}

	private static final class Rocket {
		ServerLevel level;
		Vec3 pos;
		Vec3 vel;
		UUID owner;
		int age;
	}

	private static final List<Rocket> ROCKETS = new ArrayList<>();

	public static void launch(ServerLevel level, ServerPlayer player) {
		Vec3 look = player.getLookAngle();
		Rocket rocket = new Rocket();
		rocket.level = level;
		rocket.pos = player.getEyePosition().add(look.scale(0.8)).add(0, -0.15, 0);
		rocket.vel = look.scale(WeaponConfig.RPG_SPEED);
		rocket.owner = player.getUUID();
		ROCKETS.add(rocket);

		Sfx.play(level, player.getX(), player.getEyeY(), player.getZ(), "entity.firework_rocket.launch", 3.0F, 0.7F);
		Sfx.play(level, player.getX(), player.getEyeY(), player.getZ(), "entity.generic.explode", 0.6F, 1.6F);
	}

	public static void tick() {
		Iterator<Rocket> it = ROCKETS.iterator();
		while (it.hasNext()) {
			if (step(it.next())) {
				it.remove();
			}
		}
	}

	public static void clear() {
		ROCKETS.clear();
	}

	/** @return roket bittiyse (patladiysa) true */
	private static boolean step(Rocket rocket) {
		ServerLevel level = rocket.level;
		Vec3 from = rocket.pos;
		Vec3 to = from.add(rocket.vel);
		rocket.age++;

		// Arkasindan ates ve duman
		level.sendParticles(ParticleTypes.FLAME, from.x, from.y, from.z, 3, 0.05, 0.05, 0.05, 0.02);
		level.sendParticles(ParticleTypes.SMOKE, from.x, from.y, from.z, 4, 0.1, 0.1, 0.1, 0.01);

		// Blok carpismasi
		BlockHitResult blockHit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
		Vec3 limit = blockHit.getType() != HitResult.Type.MISS ? blockHit.getLocation() : to;

		// Varlik carpismasi (en yakin)
		AABB box = new AABB(from, limit).inflate(0.5);
		Entity hitEntity = null;
		Vec3 hitPos = null;
		double bestDist = Double.MAX_VALUE;
		UUID owner = rocket.owner;
		boolean ownerImmune = rocket.age < 6; // ilk tickte kendi roketine carpma

		for (Entity candidate : level.getEntities((Entity) null, box,
				e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator() && !(ownerImmune && e.getUUID().equals(owner)))) {
			Optional<Vec3> hit = candidate.getBoundingBox().inflate(0.3).clip(from, limit);
			if (hit.isPresent()) {
				double dist = from.distanceToSqr(hit.get());
				if (dist < bestDist) {
					bestDist = dist;
					hitEntity = candidate;
					hitPos = hit.get();
				}
			}
		}

		if (hitEntity != null) {
			boolean head = Ballistics.isHeadHit(hitEntity, hitPos);
			explode(level, rocket.owner, hitPos, hitEntity, head);
			return true;
		}
		if (blockHit.getType() != HitResult.Type.MISS) {
			explode(level, rocket.owner, blockHit.getLocation(), null, false);
			return true;
		}
		if (rocket.age >= WeaponConfig.RPG_MAX_AGE) {
			explode(level, rocket.owner, to, null, false);
			return true;
		}

		rocket.pos = to;
		return false;
	}

	private static void explode(ServerLevel level, UUID ownerId, Vec3 pos, Entity direct, boolean headHit) {
		Entity ownerEntity = level.getEntity(ownerId);
		DamageSource source = ownerEntity instanceof Player player
				? level.damageSources().playerAttack(player)
				: level.damageSources().generic();

		AABB area = AABB.ofSize(pos, WeaponConfig.RPG_AREA, WeaponConfig.RPG_AREA, WeaponConfig.RPG_AREA);
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
			if (target.isSpectator()) {
				continue;
			}
			if (!WeaponConfig.RPG_HURTS_SHOOTER && target.getUUID().equals(ownerId)) {
				continue;
			}
			float amount = (target == direct && headHit)
					? WeaponConfig.RPG_DIRECT_HEAD * Armors.rpgHeadFactor(target)   // kask 1 kafa hasarini azaltir
					: WeaponConfig.RPG_BLAST * Armors.bodyFactor(target);           // yelek govde hasarini azaltir
			target.invulnerableTime = 0;
			target.hurtServer(level, source, amount);
		}

		if (headHit && ownerEntity instanceof ServerPlayer shooter) {
			shooter.displayClientMessage(Component.translatable("message.silahmod.headshot"), true);
		}

		level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
		level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 40, 0.8, 0.8, 0.8, 0.08);
		level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.x, pos.y, pos.z, 20, 0.8, 0.8, 0.8, 0.05);
		Sfx.play(level, pos.x, pos.y, pos.z, "entity.generic.explode", 4.0F, 0.9F);
	}
}
