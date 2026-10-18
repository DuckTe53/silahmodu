package com.silahmod.item;

import java.util.List;

import com.silahmod.AmmoData;
import com.silahmod.ModComponents;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;

/** Mermisi olan silahlarin ortak sinifi. Sinirsiz dayaniklilik: esyada hasar/dayaniklilik yok. */
public abstract class GunItem extends Item {
	public final int magSize;
	public final int reserveMax;
	public final int fireDelay;
	public final int reloadTicks;

	protected GunItem(Properties properties, int magSize, int reserveMax, int fireDelay, int reloadTicks) {
		super(properties);
		this.magSize = magSize;
		this.reserveMax = reserveMax;
		this.fireDelay = fireDelay;
		this.reloadTicks = reloadTicks;
	}

	/** Mermisi bitmis halin ozel modeli (roketsiz RPG gibi) varsa bu kare numarasi gosterilir. */
	public static final int EMPTY_FRAME = 99;

	/** Reload animasyonunun kare sayisi. tools/gen_models.py'deki kare sayisiyla ayni olmali. */
	public abstract int reloadFrames();

	/** Kare numarasina (1'den baslar, dizi 0'dan) denk gelen ses; null = ses yok. */
	public String[] reloadSounds() {
		return new String[0];
	}

	/** Mermi bitince farkli (bos) model gosterilecekse true. */
	protected boolean hasEmptyModel() {
		return false;
	}

	/**
	 * Animasyon karesi, esyanin custom_model_data bileseninde tasinir (items/*.json bunu okur).
	 * 0 = bilesen yok = normal model.
	 */
	public int frame(ItemStack stack) {
		CustomModelData data = stack.get(DataComponents.CUSTOM_MODEL_DATA);
		if (data == null || data.floats().isEmpty()) {
			return 0;
		}
		return Math.round(data.floats().get(0));
	}

	public void setFrame(ItemStack stack, int frame) {
		if (frame(stack) == frame) {
			return;
		}
		if (frame <= 0) {
			stack.remove(DataComponents.CUSTOM_MODEL_DATA);
		} else {
			stack.set(DataComponents.CUSTOM_MODEL_DATA,
					new CustomModelData(List.of((float) frame), List.of(), List.of(), List.of()));
		}
	}

	/** Animasyon yokken gosterilmesi gereken kareyi ayarla (normal model ya da bos model). */
	public void refreshIdleFrame(ItemStack stack) {
		setFrame(stack, hasEmptyModel() && ammo(stack).mag() <= 0 ? EMPTY_FRAME : 0);
	}

	/** Sunucuda atis: mermi dusme ve bekleme suresi Combat tarafindan halledilir. */
	public abstract void fire(ServerPlayer player, ServerLevel level);

	public AmmoData ammo(ItemStack stack) {
		return stack.getOrDefault(ModComponents.AMMO, new AmmoData(this.magSize, this.reserveMax));
	}

	public void setAmmo(ItemStack stack, AmmoData data) {
		stack.set(ModComponents.AMMO, data);
	}

	public ItemStack createFull() {
		ItemStack stack = new ItemStack(this);
		stack.set(ModComponents.AMMO, new AmmoData(this.magSize, this.reserveMax));
		return stack;
	}
}
