package com.silahmod.client;

import com.silahmod.ModItems;
import com.silahmod.Net;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * B tusuyla acilan menu. Sol sutun silahlar, sag sutun zirhlar.
 * Bir esyaya tiklayinca sunucu onu envantere verir.
 *
 * NOT: Arka plani (renderBackground) elle CAGIRMA. Screen.render() zaten cizer; ikinci kez cizmek
 * Minecraft 1.21.6+ surumlerinde "Can only blur once per frame" hatasiyla oyunu coker.
 */
public class WeaponMenuScreen extends Screen {
	/** Sira ModItems.byIndex ile ayni: 0 sniper, 1 rpg, 2 revolver, 3 bicak, 4-7 zirhlar, 8 el bombasi, 9 medkit. */
	private static final String[] NAME_KEYS = {
			"item.silahmod.sniper", "item.silahmod.rpg", "item.silahmod.revolver", "item.silahmod.knife",
			"item.silahmod.helmet_1", "item.silahmod.vest_1", "item.silahmod.helmet_2", "item.silahmod.vest_2",
			"item.silahmod.grenade", "item.silahmod.medkit"
	};
	/** Menu yerlesimi: her sutun icin esya indeksleri. Sol = Silahlar, sag = Zirhlar. */
	private static final int[][] COLUMNS = {
			{ 0, 1, 2, 3, 8 },
			{ 4, 5, 6, 7, 9 }
	};
	private static final int ROWS = 5;           // her sutunda 5 esya
	private static final int BUTTON_W = 130;
	private static final int BUTTON_H = 22;
	private static final int GAP = 5;
	private static final int ICON_SPACE = 22;    // butonun solundaki ikon alani
	private static final int COLUMN_GAP = 14;

	public WeaponMenuScreen() {
		super(Component.translatable("screen.silahmod.menu"));
	}

	private int top() {
		int total = ROWS * (BUTTON_H + GAP) + BUTTON_H + 10;
		return this.height / 2 - total / 2 + 12;
	}

	/** Sutunun (0 = silahlar, 1 = zirhlar) butonlarinin sol x konumu. */
	private int columnX(int column) {
		int blockW = 2 * (ICON_SPACE + BUTTON_W) + COLUMN_GAP;
		int left = this.width / 2 - blockW / 2;
		return left + column * (ICON_SPACE + BUTTON_W + COLUMN_GAP) + ICON_SPACE;
	}

	@Override
	protected void init() {
		int y = top();
		for (int column = 0; column < COLUMNS.length; column++) {
			for (int row = 0; row < COLUMNS[column].length; row++) {
				final int index = COLUMNS[column][row];
				this.addRenderableWidget(Button.builder(Component.translatable(NAME_KEYS[index]),
								button -> ClientPlayNetworking.send(new Net.GiveWeaponPayload(index)))
						.pos(columnX(column), y + row * (BUTTON_H + GAP))
						.size(BUTTON_W, BUTTON_H)
						.build());
			}
		}
		int closeW = 130;
		this.addRenderableWidget(Button.builder(Component.translatable("gui.silahmod.close"), button -> this.onClose())
				.pos(this.width / 2 - closeW / 2, y + ROWS * (BUTTON_H + GAP) + 6)
				.size(closeW, BUTTON_H)
				.build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		// Arka plan + butonlar (super.render arka plani kendisi cizer)
		super.render(graphics, mouseX, mouseY, delta);

		graphics.drawCenteredString(this.font, this.title, this.width / 2, top() - 22, 0xFFFFFFFF);
		graphics.drawCenteredString(this.font, Component.translatable("gui.silahmod.weapons"),
				columnX(0) + BUTTON_W / 2, top() - 10, 0xFFAAAAAA);
		graphics.drawCenteredString(this.font, Component.translatable("gui.silahmod.armors"),
				columnX(1) + BUTTON_W / 2, top() - 10, 0xFFAAAAAA);

		// Her butonun soluna esya ikonu
		int y = top();
		for (int column = 0; column < COLUMNS.length; column++) {
			for (int row = 0; row < COLUMNS[column].length; row++) {
				Item item = ModItems.byIndex(COLUMNS[column][row]);
				if (item == null) {
					continue;
				}
				graphics.renderItem(new ItemStack(item), columnX(column) - 19, y + row * (BUTTON_H + GAP) + 3);
			}
		}
	}
}
