package com.gabbymaybelle.espresso.content;

import com.gabbymaybelle.espresso.Espresso;
import com.gabbymaybelle.espresso.Platform;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
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
	private static final Platform.Data<List<String>> PAID = Platform.data("bean_coins_paid", Codec.STRING.listOf(), List::of, true);
	private static final String GUIDE = "guide/";
	private static final String ROOT = "guide/root";

	public BeanCoin(Properties properties) {
		super(properties);
	}

	@Override
	public void addTooltipLines(ItemStack stack, Consumer<Component> tooltip) {
		tooltip.accept(Component.translatable("item.espresso_flavoured.bean_coin.desc").withStyle(ChatFormatting.GRAY));
	}

	/** The advancements that pay a coin, picked out once per server start or data reload rather than every second. */
	private static List<AdvancementHolder> paying = List.of();

	static void register() {
		Platform.Hooks hooks = Platform.get();
		hooks.onServerStarted(server -> paying = payingAdvancements(server));
		hooks.onDataReload(server -> paying = payingAdvancements(server));
		hooks.onServerStopped(server -> paying = List.of());
		hooks.onServerTick(server -> {
			if (server.getTickCount() % 20 == 0 && !paying.isEmpty()) {
				for (ServerPlayer player : server.getPlayerList().getPlayers()) {
					payCoins(player);
				}
			}
		});
	}

	/** Every Espresso guide/* advancement except the root, which only opens the tab. */
	private static List<AdvancementHolder> payingAdvancements(MinecraftServer server) {
		List<AdvancementHolder> out = new ArrayList<>();
		for (AdvancementHolder advancement : server.getAdvancements().getAllAdvancements()) {
			Identifier id = advancement.id();
			if (id.getNamespace().equals(Espresso.MOD_ID) && id.getPath().startsWith(GUIDE) && !id.getPath().equals(ROOT)) {
				out.add(advancement);
			}
		}
		return List.copyOf(out);
	}

	private static void payCoins(ServerPlayer player) {
		List<String> paid = PAID.getOrElse(player, List.of());
		Set<String> paidSet = new HashSet<>(paid);
		List<String> nowPaid = null;
		for (AdvancementHolder advancement : paying) {
			Identifier id = advancement.id();
			if (paidSet.contains(id.toString()) || !player.getAdvancements().getOrStartProgress(advancement).isDone()) {
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
		PAID.set(player, List.copyOf(nowPaid));
		player.getInventory().placeItemBackInInventory(new ItemStack(EspressoContent.BEAN_COIN, coins), Prediction.SERVER_ONLY);
		player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.4F);
	}
}
