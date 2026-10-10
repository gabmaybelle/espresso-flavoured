package com.gabbymaybelle.espresso.content;

import com.gabbymaybelle.espresso.Espresso;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * The café loyalty card. It arrives with the Barista advancement and collects a stamp for
 * each coffee advancement; a full card can be swapped with the wandering trader for the
 * Golden Mug. Stamps are a bitmask in {@link EspressoContent#STAMPS}, kept in step with the
 * holder's advancements every couple of seconds.
 */
public class LoyaltyCard extends Item {
	/** The stamps, in order: the advancement each one stands for. */
	public static final List<String> STAMPS = List.of("barista", "first_sip", "mug_shot", "fair_trade", "house_special", "sweet_dreams");
	public static final int FULL = (1 << STAMPS.size()) - 1;
	/** Set once a player has had their card, so it's never handed out twice. */
	private static final AttachmentType<Boolean> HAD_CARD = AttachmentRegistry.create(
			Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "had_loyalty_card"), builder -> builder.persistent(Codec.BOOL).copyOnDeath());

	public LoyaltyCard(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		int stamps = stack.getOrDefault(EspressoContent.STAMPS, 0);
		for (int i = 0; i < STAMPS.size(); i++) {
			boolean stamped = (stamps & (1 << i)) != 0;
			tooltip.accept(Component.literal(stamped ? "☕ " : "○ ")
					.append(Component.translatable("advancements.espresso.guide." + STAMPS.get(i) + ".title"))
					.withStyle(stamped ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY));
		}
		if (stamps == FULL) {
			tooltip.accept(Component.translatable("item.espresso.loyalty_card.full").withStyle(ChatFormatting.YELLOW));
		}
	}

	static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % 40 == 0) {
				for (ServerPlayer player : server.getPlayerList().getPlayers()) {
					updateCards(server, player);
				}
			}
		});
	}

	private static void updateCards(MinecraftServer server, ServerPlayer player) {
		int stamps = 0;
		for (int i = 0; i < STAMPS.size(); i++) {
			AdvancementHolder advancement = server.getAdvancements().get(Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "guide/" + STAMPS.get(i)));
			if (advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone()) {
				stamps |= 1 << i;
			}
		}
		Inventory inventory = player.getInventory();
		if ((stamps & 1) != 0 && !player.getAttachedOrElse(HAD_CARD, false)) {
			// Earned Barista before cards existed (or just now: the advancement gives one).
			if (!inventory.contains(stack -> stack.is(EspressoContent.LOYALTY_CARD))) {
				inventory.placeItemBackInInventory(new ItemStack(EspressoContent.LOYALTY_CARD), Prediction.SERVER_ONLY);
			}
			player.setAttached(HAD_CARD, true);
		}
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.is(EspressoContent.LOYALTY_CARD) && stack.getOrDefault(EspressoContent.STAMPS, 0) != stamps) {
				stack.set(EspressoContent.STAMPS, stamps);
				// The card's picture shows how many stamps it has.
				stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of((float) Integer.bitCount(stamps)), List.of(), List.of(), List.of()));
			}
		}
	}
}
