package com.silahmod;

/**
 * Tum silah ayarlari burada. Sayilari degistirip tekrar derlemen yeterli.
 *
 * Hasar birimi: HP. 1 kalp = 2 HP (5 kalp = 10 HP).
 * Sure birimi: tick. 20 tick = 1 saniye.
 */
public final class WeaponConfig {
	private WeaponConfig() {}

	public static float hearts(float hearts) {
		return hearts * 2.0F;
	}

	// ---------------- KESKIN NISANCI ----------------
	public static final int SNIPER_MAG = 3;            // sarjor
	public static final int SNIPER_RESERVE = 12;       // yedek mermi
	public static final float SNIPER_HEAD = 200.0F;    // kafa
	public static final float SNIPER_BODY = hearts(5); // govde = 10 HP
	public static final float SNIPER_ZOOM = 2.5F;      // scope yakinlastirma (2.5x)
	public static final double SNIPER_RANGE = 300.0;
	public static final int SNIPER_FIRE_DELAY = 30;    // atislar arasi (1.5 sn) - benim varsayimim
	public static final int SNIPER_RELOAD = 70;        // doldurma suresi - benim varsayimim
	public static final int SNIPER_RELOAD_FRAMES = 10; // reload animasyon kare sayisi (tools/gen_models.py ile ayni olmali)
	public static final boolean SNIPER_GLINT = true;   // durbun acan oyuncunun durbununde, BASKALARININ gordugu isik
	public static final int SNIPER_GLINT_INTERVAL = 2; // isik kac tickte bir parlar (kucuk = daha sik)
	public static final int SNIPER_GLINT_COLOR = 0xFFF2B0; // isik rengi (RGB). Kirmizi istersen 0xFF2020
	public static final float SNIPER_GLINT_SIZE = 1.3F;    // isik buyuklugu

	// ---------------- RPG ----------------
	public static final int RPG_MAG = 3;
	public static final int RPG_RESERVE = 18;
	public static final float RPG_BLAST = hearts(5);       // alan hasari = 10 HP
	public static final float RPG_DIRECT_HEAD = hearts(7.5F); // tam kafa = 15 HP
	public static final double RPG_AREA = 5.0;             // 5x5x5 blokluk alan
	public static final boolean RPG_HURTS_SHOOTER = true;  // false yaparsan kendine zarar vermez
	public static final double RPG_SPEED = 1.6;            // blok/tick
	public static final int RPG_MAX_AGE = 120;             // 6 sn sonra kendiliginden patlar
	public static final int RPG_FIRE_DELAY = 40;
	public static final int RPG_RELOAD = 80;
	public static final int RPG_RELOAD_FRAMES = 10;

	// ---------------- REVOLVER ----------------
	public static final int REVOLVER_MAG = 8;
	public static final int REVOLVER_RESERVE = 32;
	public static final float REVOLVER_HEAD = 200.0F;
	public static final float REVOLVER_BODY = hearts(4);   // 8 HP
	public static final double REVOLVER_RANGE = 120.0;
	public static final int REVOLVER_FIRE_DELAY = 8;
	public static final int REVOLVER_RELOAD = 50;
	public static final int REVOLVER_RELOAD_FRAMES = 9;

	// ---------------- ZIRHLAR ----------------
	// Kask 1 (yesil): demir kask gibi (2 zirh) + RPG'nin tam kafa isabetini azaltir. Azaltma orani benim varsayimim.
	public static final double HELMET_1_ARMOR = 2.0;
	public static final double HELMET_1_TOUGHNESS = 0.0;
	public static final float HELMET_1_RPG_HEAD_REDUCTION = 0.25F;   // RPG tam kafa hasari %25 azalir

	// Yelek 1 (gri kamuflaj): tum silahlarin govde hasarini azaltir
	public static final float VEST_1_BODY_REDUCTION = 0.15F;         // %15

	// Kask 2 (koyu, siperlikli): Koruma II buyulu elmas pantolon gibi (6 zirh, 2 sertlik, Koruma II) + altin kalp
	public static final double HELMET_2_ARMOR = 6.0;
	public static final double HELMET_2_TOUGHNESS = 2.0;
	public static final int HELMET_2_PROTECTION_LEVEL = 2;           // Koruma buyusu seviyesi
	public static final int HELMET_2_GOLD_HEARTS = 2;                // giyince verilen altin kalp

	// Yelek 2 (kahverengi): tum silahlarin govde hasarini azaltir
	public static final float VEST_2_BODY_REDUCTION = 0.30F;         // %30

	// ---------------- MEDKIT ----------------
	public static final int MEDKIT_USE_TICKS = 80;          // bandaj sarma suresi (20 tick = 1 sn) -> 4 sn
	public static final int MEDKIT_ABSORB_HEARTS = 10;      // gecici sari kalp (10 kalp = 20 can)
	public static final int MEDKIT_ABSORB_TICKS = 1200;     // sari kalplerin suresi -> 60 sn
	public static final int MEDKIT_MENU_GIVE = 2;           // menuden alinca kac adet verilir

	// ---------------- EL BOMBASI ----------------
	// Patlama TNT ile birebir ayni (guc 4.0, blok kirar, ayni hasar); sadece TNT gorunmez, bomba modeli gorunur.
	public static final float GRENADE_POWER = 4.0F;         // TNT = 4.0
	public static final float GRENADE_SPEED = 1.2F;         // atis hizi
	public static final int GRENADE_COOLDOWN_TICKS = 20;    // iki atis arasi bekleme
	public static final int GRENADE_MAX_AGE_TICKS = 100;    // bir sey'e carpmazsa en gec bu surede patlar (5 sn)
	public static final int GRENADE_MENU_GIVE = 4;          // menuden alinca kac adet verilir

	// ---------------- BICAK ----------------
	public static final float KNIFE_BACKSTAB = 200.0F;     // arkadan
	public static final float KNIFE_FRONT = hearts(3);     // onden govde = 6 HP
	public static final float KNIFE_HEAD = hearts(6);      // onden kafa = 12 HP
	public static final int KNIFE_COOLDOWN = 10;
	/** Hedefin arkasindan sayilma acisi: 0.5 = yaklasik 60 derece koni. */
	public static final double KNIFE_BACK_DOT = 0.5;
}
