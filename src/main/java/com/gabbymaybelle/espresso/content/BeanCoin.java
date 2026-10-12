package com.gabbymaybelle.espresso.content;

import com.gabbymaybelle.espresso.Espresso;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Bean Coins: every Espresso advancement pays one, once per player, straight into the inventory.
 * A master barista takes them for the Golden Mug (8) and a couple of treats. Every second, any
 * Espresso advancement a player has finished but not yet been paid for pays out.
 */
public class BeanCoin extends Item implements TooltipLines {
	/** Advancements a player has already been paid a coin for. */
	private static final AttachmentType<List<String>> PAID = AttachmentRegistry.create(
			Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "bean_coins_paid"),
			builder -> builder.persistent(Codec.STRING.listOf()).copyOnDeath());
	private static final String GUIDE = "guide/";
	private static final String ROOT = "guide/root";

	public BeanCoin(Properties properties) {
		super(properties);
	}

	@Override
	public void addTooltipLines(ItemStack stack, Consumer<Component> tooltip) {
		tooltip.accept(Component.translatable("item.espresso_flavoured.bean_coin.desc").withStyle(ChatFormatting.GRAY));
	}

	static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % 20 == 0) {
				for (ServerPlayer player : server.getPlayerList().getPlayers()) {
					payCoins(server, player);
				}
			}
		});
	}

	private static void payCoins(MinecraftServer server, ServerPlayer player) {
		List<String> paid = player.getAttachedOrElse(PAID, List.of());
		List<String> nowPaid = null;
		for (AdvancementHolder advancement : server.getAdvancements().getAllAdvancements()) {
			Identifier id = advancement.id();
			if (!id.getNamespace().equals(Espresso.MOD_ID) || !id.getPath().startsWith(GUIDE) || id.getPath().equals(ROOT)
					|| paid.contains(id.toString()) || !player.getAdvancements().getOrStartProgress(advancement).isDone()) {
				continue;
			}
			if (nowPaid == null) {
				nowPaid = new ArrayList<>(paid);
			}
			nowPaid.add(id.toString());
		}
		if (nowPaid == null) {
			return;
		}
		int coins = nowPaid.size() - paid.size();
		player.setAttached(PAID, List.copyOf(nowPaid));
		player.getInventory().placeItemBackInInventory(new ItemStack(EspressoContent.BEAN_COIN, coins), Prediction.SERVER_ONLY);
		player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.4F);
	}
}
