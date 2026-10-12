package com.gabbymaybelle.espresso.content;

import com.gabbymaybelle.espresso.Espresso;
import com.gabbymaybelle.espresso.EspressoAdvancements;
import com.gabbymaybelle.espresso.compat.TrinketsCompat;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * Café charms: small things worn in Trinkets slots (necklace, belt, back, shoes, glove, pocket) that
 * each give one gentle perk. Without Trinkets installed they work from anywhere in the inventory.
 * A master barista sells a couple for Bean Coins, and they turn up now and then in café and village chests.
 */
public class CharmItem extends Item implements TooltipLines {
	/** Which Trinkets slot it goes in, for the tooltip ("necklace", "belt", ...). */
	private final String slot;

	public CharmItem(Properties properties, String slot) {
		super(properties);
		this.slot = slot;
	}

	@Override
	public void addTooltipLines(ItemStack stack, Consumer<Component> tooltip) {
		String name = stack.getItem().getDescriptionId();
		tooltip.accept(Component.translatable(name + ".desc").withStyle(ChatFormatting.GRAY));
		if (!TRINKETS) {
			tooltip.accept(Component.translatable("item.espresso_flavoured.charm.carried").withStyle(ChatFormatting.DARK_GRAY));
		} else {
			tooltip.accept(Component.translatable("item.espresso_flavoured.charm.slot", Component.translatable("item.espresso_flavoured.charm.slot." + slot))
					.withStyle(ChatFormatting.DARK_GRAY));
		}
	}

	/** Trinkets is installed: charms only work when worn in their slot. */
	private static final boolean TRINKETS = FabricLoader.getInstance().isModLoaded("trinkets");

	/** Whether this player is wearing (or, without Trinkets, carrying) a charm. */
	public static boolean isWorn(LivingEntity entity, Item charm) {
		if (!(entity instanceof Player player)) {
			return false;
		}
		return TRINKETS ? TrinketsCompat.isEquipped(player, charm) : player.getInventory().contains(new ItemStack(charm));
	}

	/** Drops the Ember Glove smelts as they fall: raw ores, sand, cobblestone and logs (data/espresso_flavoured/tags/item/ember_glove_smelts.json). */
	private static final TagKey<Item> SMELTS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "ember_glove_smelts"));
	private static final LootItemFunction SMELT = SmeltItemFunction.smelted().build();

	/** The attribute perks: Lucky Bean, Reading Socks and Café Parasol. */
	private record Perk(Item charm, Holder<Attribute> attribute, double amount) {
		Identifier id() {
			return Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "charm_" + BuiltInRegistries.ITEM.getKey(charm).getPath());
		}
	}

	private static List<Perk> perks() {
		return List.of(
				new Perk(EspressoContent.LUCKY_BEAN, Attributes.LUCK, 1),
				// Up a full block without jumping, like on a horse (players step 0.6 by default).
				new Perk(EspressoContent.READING_SOCKS, Attributes.STEP_HEIGHT, 0.5),
				new Perk(EspressoContent.CAFE_PARASOL, Attributes.SAFE_FALL_DISTANCE, 3));
	}

	static void register() {
		if (TRINKETS) {
			TrinketsCompat.equipOnUse(EspressoContent.CHARMS);
		}
		List<Perk> perks = perks();
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % 10 != 0) {
				return;
			}
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				for (Perk perk : perks) {
					AttributeInstance attribute = player.getAttribute(perk.attribute());
					if (attribute == null) {
						continue;
					}
					if (isWorn(player, perk.charm())) {
						attribute.addOrUpdateTransientModifier(new AttributeModifier(perk.id(), perk.amount(), AttributeModifier.Operation.ADD_VALUE));
					} else {
						attribute.removeModifier(perk.id());
					}
				}
				if (server.getTickCount() % 40 == 0 && EspressoContent.CHARMS.stream().allMatch(charm -> isWorn(player, charm))) {
					EspressoAdvancements.award(player, "guide/all_dressed_up", "dressed");
				}
			}
		});
		// The Ember Glove: blocks broken by hand drop smelted, unless you're crouching.
		LootTableEvents.MODIFY_DROPS.register((table, context, drops) -> {
			if (!context.hasParameter(LootContextParams.BLOCK_STATE)) {
				return;
			}
			Entity breaker = context.getOptional(LootContextParams.THIS_ENTITY);
			if (!(breaker instanceof Player player) || player.isShiftKeyDown() || !isWorn(player, EspressoContent.EMBER_GLOVE)) {
				return;
			}
			drops.replaceAll(stack -> stack.is(SMELTS) ? SMELT.apply(stack, context) : stack);
		});
	}
}
