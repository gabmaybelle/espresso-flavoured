package com.gabbymaybelle.espresso;

import com.gabbymaybelle.espresso.content.CharmItem;
import com.gabbymaybelle.espresso.content.EspressoContent;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

/** The checks for the run with Curios installed (`-PtestCurios`): charms are worn in Curios' slots. */
final class CuriosChecks {
	private CuriosChecks() {
	}

	/** Every charm's slot, worn, found, set aside as on death, and put back. Returns what went wrong. */
	static String slots(ServerPlayer player) {
		StringBuilder out = new StringBuilder();
		ICuriosItemHandler inventory = CuriosApi.getCuriosInventoryOrNull(player);
		if (inventory == null) {
			return "\n  the player has no Curios inventory";
		}
		if (CharmItem.slots() == null) {
			return "\n  charms use Curios' slots when Curios is installed";
		}
		String[][] where = {
				{"lucky_bean", "necklace"}, {"thermos", "belt"}, {"cafe_parasol", "back"}, {"reading_socks", "feet"},
				{"ember_glove", "hands"}, {"knit_beanie", "head"}, {"pocket_hearth", "pocket"}, {"copper_tamper", "pocket"},
		};
		for (String[] entry : where) {
			Item charm = EspressoContent.CHARMS.stream().filter(c -> net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(c).getPath().equals(entry[0])).findFirst().orElseThrow();
			int index = entry[0].equals("copper_tamper") ? 1 : 0;
			boolean valid = CuriosApi.isStackValid(new SlotContext(entry[1], player, index, false, true), new ItemStack(charm));
			if (!valid) {
				out.append("\n  the ").append(entry[0]).append(" fits the ").append(entry[1]).append(" slot");
				continue;
			}
			inventory.setEquippedCurio(entry[1], index, new ItemStack(charm));
			if (!CharmItem.slots().isEquipped(player, charm)) {
				out.append("\n  the ").append(entry[0]).append(" is found once worn in the ").append(entry[1]).append(" slot");
			}
			var asUse = CuriosApi.getCurio(new ItemStack(charm));
			if (asUse.isEmpty() || !asUse.get().canEquipFromUse(new SlotContext(entry[1], player, index, false, true))) {
				out.append("\n  the ").append(entry[0]).append(" goes on with a right-click");
			}
			inventory.setEquippedCurio(entry[1], index, ItemStack.EMPTY);
		}

		// As on death: worn charms are set aside, then put back in the same slot.
		inventory.setEquippedCurio("necklace", 0, new ItemStack(EspressoContent.LUCKY_BEAN));
		Map<String, ItemStack> taken = CharmItem.slots().takeCharms(player);
		if (taken.size() != 1 || CharmItem.slots().isEquipped(player, EspressoContent.LUCKY_BEAN)) {
			out.append("\n  worn charms are set aside on death (took ").append(taken.keySet()).append(")");
		} else {
			String key = taken.keySet().iterator().next();
			if (!CharmItem.slots().putBack(player, key, taken.get(key)) || !CharmItem.slots().isEquipped(player, EspressoContent.LUCKY_BEAN)) {
				out.append("\n  the charm goes back in its slot after respawning");
			}
		}
		// Left worn for the next check, which looks at the perk a few ticks later.
		return out.toString();
	}

	/** The Lucky Bean's +1 Luck, with the bean worn in its necklace slot (left there by {@link #slots}). */
	static String luck(ServerPlayer player) {
		double luck = player.getAttributeValue(Attributes.LUCK);
		return luck == 1.0 ? "" : "\n  a Lucky Bean worn in its Curios slot gives +1 Luck (got " + luck + ")";
	}

	/** Takes the worn charms off again. */
	static void clear(ServerPlayer player) {
		CharmItem.slots().takeCharms(player);
	}

	/** The Ember Glove in its hands slot smelts a log's drop (crouching keeps the log); returns what went wrong. */
	static void wearGlove(ServerPlayer player) {
		CuriosApi.getCuriosInventoryOrNull(player).setEquippedCurio("hands", 0, new ItemStack(EspressoContent.EMBER_GLOVE));
	}
}
