package com.silahmod.item;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.silahmod.WeaponConfig;
import com.silahmod.entity.GrenadeProjectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** El bombasi: sag tikla firlatilir, bir seye carpinca TNT gucunde patlar (Creative'de harcanmaz). */
public class GrenadeItem extends Item {
	/** Oyuncu basina bir sonraki atis zamani (oyun tick'i). */
	private static final Map<UUID, Long> NEXT_THROW = new HashMap<>();

	public GrenadeItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level instanceof ServerLevel server) {
			long now = server.getGameTime();
			Long next = NEXT_THROW.get(player.getUUID());
			if (next != null && now < next) {
				return InteractionResult.FAIL;
			}
			NEXT_THROW.put(player.getUUID(), now + WeaponConfig.GRENADE_COOLDOWN_TICKS);

			GrenadeProjectile grenade = new GrenadeProjectile(server, player, stack.copyWithCount(1));
			grenade.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, WeaponConfig.GRENADE_SPEED, 1.0F);
			server.addFreshEntity(grenade);
			server.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW,
					SoundSource.PLAYERS, 0.6F, 0.4F / (server.getRandom().nextFloat() * 0.4F + 0.8F));
			stack.consume(1, player);
		}
		return InteractionResult.SUCCESS;
	}
}
