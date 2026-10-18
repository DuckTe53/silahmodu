package com.silahmod.client;

import com.silahmod.AmmoData;
import com.silahmod.item.GunItem;
import com.silahmod.item.SniperItem;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/** Scope katmani (kod ile cizilir, ekstra doku gerekmez) ve mermi sayaci. */
public final class ScopeOverlay {
	private ScopeOverlay() {}

	private static final int BLACK = 0xFF000000;
	private static final int RED = 0xFFFF2020;
	private static final int WHITE = 0xFFFFFFFF;

	public static boolean isScoped() {
		Minecraft mc = Minecraft.getInstance();
		return mc.player != null
				&& mc.options.getCameraType().isFirstPerson()
				&& mc.player.isUsingItem()
				&& mc.player.getUseItem().getItem() instanceof SniperItem;
	}

	public static void renderScope(GuiGraphics graphics, DeltaTracker tickCounter) {
		if (!isScoped()) {
			return;
		}
		int w = graphics.guiWidth();
		int h = graphics.guiHeight();
		int cx = w / 2;
		int cy = h / 2;
		int r = (int) (h * 0.46F);

		// Dairenin disi siyah
		graphics.fill(0, 0, w, Math.max(0, cy - r), BLACK);
		graphics.fill(0, Math.min(h, cy + r), w, h, BLACK);
		for (int y = Math.max(0, cy - r); y < Math.min(h, cy + r); y++) {
			int dy = y - cy;
			int half = (int) Math.sqrt((double) r * r - (double) dy * dy);
			graphics.fill(0, y, cx - half, y + 1, BLACK);
			graphics.fill(cx + half, y, w, y + 1, BLACK);
		}

		// Artı sembolu (ortada bosluk birakir)
		int gap = 6;
		graphics.fill(cx - r, cy, cx - gap, cy + 1, BLACK);
		graphics.fill(cx + gap, cy, cx + r, cy + 1, BLACK);
		graphics.fill(cx, cy - r, cx + 1, cy - gap, BLACK);
		graphics.fill(cx, cy + gap, cx + 1, cy + r, BLACK);
		graphics.fill(cx - 1, cy - 1, cx + 1, cy + 1, RED);
	}

	public static void renderAmmo(GuiGraphics graphics, DeltaTracker tickCounter) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui) {
			return;
		}
		ItemStack stack = mc.player.getMainHandItem();
		if (!(stack.getItem() instanceof GunItem gun)) {
			return;
		}
		AmmoData ammo = gun.ammo(stack);
		String text = ammo.mag() + " / " + ammo.reserve();
		int x = graphics.guiWidth() - mc.font.width(text) - 10;
		int y = graphics.guiHeight() - 24;
		graphics.drawString(mc.font, text, x, y, ammo.mag() == 0 ? RED : WHITE);
	}
}
