package com.silahmod;

import java.util.function.Function;

import com.silahmod.item.KnifeItem;
import com.silahmod.item.GrenadeItem;
import com.silahmod.item.MedkitItem;
import com.silahmod.item.RevolverItem;
import com.silahmod.item.RpgItem;
import com.silahmod.item.SniperItem;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

public final class ModItems {
	private ModItems() {}

	// Menu ve creative sekmesindeki sira: sniper, rpg, revolver, bicak
	public static final SniperItem SNIPER = register("sniper", SniperItem::new,
			new Item.Properties().stacksTo(1)
					.component(ModComponents.AMMO, new AmmoData(WeaponConfig.SNIPER_MAG, WeaponConfig.SNIPER_RESERVE)));

	public static final RpgItem RPG = register("rpg", RpgItem::new,
			new Item.Properties().stacksTo(1)
					.component(ModComponents.AMMO, new AmmoData(WeaponConfig.RPG_MAG, WeaponConfig.RPG_RESERVE)));

	public static final RevolverItem REVOLVER = register("revolver", RevolverItem::new,
			new Item.Properties().stacksTo(1)
					.component(ModComponents.AMMO, new AmmoData(WeaponConfig.REVOLVER_MAG, WeaponConfig.REVOLVER_RESERVE)));

	public static final KnifeItem KNIFE = register("knife", KnifeItem::new,
			new Item.Properties().stacksTo(1));

	// ---------------- ZIRHLAR (klasik envanterdeki zirh yuvalarindan giyilir) ----------------
	/** Kask 1: demir kask gibi 2 zirh. RPG kafa hasari azaltmasi Armors.java'da. */
	public static final Item HELMET_1 = register("helmet_1", Item::new,
			armorProperties(EquipmentSlot.HEAD, EquipmentSlotGroup.HEAD, "helmet_1", SoundEvents.ARMOR_EQUIP_IRON,
					WeaponConfig.HELMET_1_ARMOR, WeaponConfig.HELMET_1_TOUGHNESS, 0.0));

	/** Yelek 1: govde hasarini %15 azaltir (Armors.java). */
	public static final Item VEST_1 = register("vest_1", Item::new,
			armorProperties(EquipmentSlot.CHEST, EquipmentSlotGroup.CHEST, "vest_1", SoundEvents.ARMOR_EQUIP_LEATHER,
					0.0, 0.0, 0.0));

	/** Kask 2: elmas pantolon gibi 6 zirh + 2 sertlik (+ Koruma II buyusu stack uzerinde) + altin kalp. */
	public static final Item HELMET_2 = register("helmet_2", Item::new,
			armorProperties(EquipmentSlot.HEAD, EquipmentSlotGroup.HEAD, "helmet_2", SoundEvents.ARMOR_EQUIP_DIAMOND,
					WeaponConfig.HELMET_2_ARMOR, WeaponConfig.HELMET_2_TOUGHNESS,
					WeaponConfig.hearts(WeaponConfig.HELMET_2_GOLD_HEARTS)));

	/** Yelek 2: govde hasarini %30 azaltir (Armors.java). */
	public static final Item VEST_2 = register("vest_2", Item::new,
			armorProperties(EquipmentSlot.CHEST, EquipmentSlotGroup.CHEST, "vest_2", SoundEvents.ARMOR_EQUIP_LEATHER,
					0.0, 0.0, 0.0));

	/** El bombasi: sag tik ile atilir, carptigi yerde TNT gucunde patlar. */
	public static final Item GRENADE = register("grenade", GrenadeItem::new, new Item.Properties().stacksTo(16));

	/** Medkit: sag tik basili tut, bandaj sarilir; biter bitmez can dolar ve gecici sari kalpler gelir. */
	public static final Item MEDKIT = register("medkit", MedkitItem::new, new Item.Properties().stacksTo(4));

	public static final ResourceKey<CreativeModeTab> TAB_KEY =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, SilahMod.id("silahlar"));

	private static <T extends Item> T register(String name, Function<Item.Properties, T> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, SilahMod.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
	}

	/**
	 * Zirh esyasi ozellikleri: giyilebilir (equippable), zirh / sertlik / ek can (altin kalp) ozellikleri.
	 * Dayaniklilik yok (silahlar gibi sinirsiz). Giyilince gorunen doku: assets/silahmod/equipment/<ad>.json
	 */
	private static Item.Properties armorProperties(EquipmentSlot slot, EquipmentSlotGroup group, String name,
			Holder<SoundEvent> equipSound, double armor, double toughness, double maxAbsorption) {
		ItemAttributeModifiers.Builder attrs = ItemAttributeModifiers.builder();
		if (armor > 0.0) {
			attrs.add(Attributes.ARMOR, new AttributeModifier(SilahMod.id("armor." + name), armor,
					AttributeModifier.Operation.ADD_VALUE), group);
		}
		if (toughness > 0.0) {
			attrs.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(SilahMod.id("toughness." + name), toughness,
					AttributeModifier.Operation.ADD_VALUE), group);
		}
		if (maxAbsorption > 0.0) {
			attrs.add(Attributes.MAX_ABSORPTION, new AttributeModifier(SilahMod.id("absorption." + name), maxAbsorption,
					AttributeModifier.Operation.ADD_VALUE), group);
		}
		return new Item.Properties().stacksTo(1)
				.attributes(attrs.build())
				.component(DataComponents.EQUIPPABLE, Equippable.builder(slot)
						.setEquipSound(equipSound)
						.setAsset(ResourceKey.create(EquipmentAssets.ROOT_ID, SilahMod.id(name)))
						.build());
	}

	/** Kask 2'nin Koruma II buyulu hali (buyu kayit sistemi gerektirdigi icin dunya acikken olusturulur). */
	public static ItemStack createHelmet2(HolderLookup.Provider registries) {
		ItemStack stack = new ItemStack(HELMET_2);
		stack.enchant(registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PROTECTION),
				WeaponConfig.HELMET_2_PROTECTION_LEVEL);
		return stack;
	}

	/** Toplam menu sirasi: 0-3 silahlar, 4-7 zirhlar. */
	public static final int COUNT = 10;

	/** 0 sniper, 1 rpg, 2 revolver, 3 bicak, 4 kask 1, 5 yelek 1, 6 kask 2, 7 yelek 2, 8 el bombasi, 9 medkit */
	public static Item byIndex(int index) {
		return switch (index) {
			case 0 -> SNIPER;
			case 1 -> RPG;
			case 2 -> REVOLVER;
			case 3 -> KNIFE;
			case 4 -> HELMET_1;
			case 5 -> VEST_1;
			case 6 -> HELMET_2;
			case 7 -> VEST_2;
			case 8 -> GRENADE;
			case 9 -> MEDKIT;
			default -> null;
		};
	}

	public static void init() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY, FabricItemGroup.builder()
				.title(Component.translatable("itemGroup.silahmod.silahlar"))
				.icon(() -> new ItemStack(SNIPER))
				.displayItems((context, entries) -> {
					// Creative'den alinan silahlar dolu mermiyle gelir
					entries.accept(SNIPER.createFull());
					entries.accept(RPG.createFull());
					entries.accept(REVOLVER.createFull());
					entries.accept(KNIFE);
					entries.accept(new ItemStack(GRENADE, WeaponConfig.GRENADE_MENU_GIVE));
					entries.accept(new ItemStack(MEDKIT, WeaponConfig.MEDKIT_MENU_GIVE));
					entries.accept(HELMET_1);
					entries.accept(VEST_1);
					entries.accept(createHelmet2(context.holders()));
					entries.accept(VEST_2);
				})
				.build());
	}
}
