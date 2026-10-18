package com.silahmod.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.silahmod.Net;
import com.silahmod.SilahMod;
import com.silahmod.item.GunItem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.KeyMapping;

public class SilahModClient implements ClientModInitializer {
	public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(SilahMod.id("silahlar"));

	public static KeyMapping reloadKey;
	public static KeyMapping menuKey;

	@Override
	public void onInitializeClient() {
		reloadKey = KeyBindingHelper.registerKeyBinding(
				new KeyMapping("key.silahmod.reload", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY));
		menuKey = KeyBindingHelper.registerKeyBinding(
				new KeyMapping("key.silahmod.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY));

		// R = doldur, B = silah menusu
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.getConnection() == null) {
				return;
			}
			ScopeGlint.tick(client);
			while (reloadKey.consumeClick()) {
				if (client.player.getMainHandItem().getItem() instanceof GunItem) {
					ClientPlayNetworking.send(new Net.ReloadPayload());
				}
			}
			while (menuKey.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(new WeaponMenuScreen());
				}
			}
		});

		// Sol tik: elinde ates edilen silah varsa vanilla saldiri yerine ates et.
		// clickCount > 0 = tusa gercekten basildi (basili tutmak surekli ates etmez).
		ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> {
			if (player.getMainHandItem().getItem() instanceof GunItem) {
				if (clickCount > 0 && !player.isSpectator()) {
					ClientPlayNetworking.send(new Net.ShootPayload());
				}
				return true; // blok kirma / vanilla saldiriyi engelle
			}
			return false;
		});

		// Scope katmani ve mermi sayaci
		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, SilahMod.id("scope"), ScopeOverlay::renderScope);
		HudElementRegistry.attachElementAfter(VanillaHudElements.INFO_BAR, SilahMod.id("ammo"), ScopeOverlay::renderAmmo);
		// Medkit bandaj sarma animasyonu (oyuncunun kendi skinindeki kol)
		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, SilahMod.id("medkit"), MedkitOverlay::render);
	}
}
