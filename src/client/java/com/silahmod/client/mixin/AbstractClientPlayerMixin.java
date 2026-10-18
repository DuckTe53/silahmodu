package com.silahmod.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.silahmod.WeaponConfig;
import com.silahmod.client.ScopeOverlay;

import net.minecraft.client.player.AbstractClientPlayer;

/**
 * Sniper scope acikken gorus alanini daraltir (yakinlastirma).
 * Vanilla durbun (spyglass) ayni yontemle zoom yapar.
 * require = 0: hedef metot bulunamazsa oyun cokmez, sadece zoom calismaz.
 */
@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
	@Inject(method = "getFieldOfViewModifier", at = @At("HEAD"), cancellable = true, require = 0)
	private void silahmod$sniperZoom(CallbackInfoReturnable<Float> cir) {
		if (ScopeOverlay.isScoped()) {
			cir.setReturnValue(1.0F / WeaponConfig.SNIPER_ZOOM);
		}
	}
}
