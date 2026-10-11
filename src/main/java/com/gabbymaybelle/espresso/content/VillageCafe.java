package com.gabbymaybelle.espresso.content;

import com.gabbymaybelle.espresso.Espresso;
import com.gabbymaybelle.espresso.mixin.StructureTemplatePoolAccessor;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

/**
 * A little cottage café that can turn up in villages, with an espresso machine on the counter for a
 * barista to claim. When the server starts, the café (see tools/generate-cafe.js) joins the
 * houses pool of each vanilla village style, and of the Towns and Towers villages it suits, in
 * the closest of its five looks.
 */
public final class VillageCafe {
	/** Houses pools, and the café style that suits them. Pools that aren't there (no Towns and Towers) are skipped. */
	private static final Map<String, String> POOLS = new LinkedHashMap<>();

	static {
		for (String style : List.of("plains", "desert", "savanna", "taiga", "snowy")) {
			POOLS.put("minecraft:village/" + style + "/houses", style);
		}
		// Towns and Towers: oak-and-spruce villages get the plains café, and so on. Styles a cosy
		// wooden café would look out of place in (tribal, tipi, fantasy...) are left out.
		for (String village : List.of("exclusives/classic", "exclusives/tudor", "exclusives/rustic", "meadow_swiss",
				"birch_forest_romanian", "swamp_boat")) {
			POOLS.put("kaisyn:village/" + village + "/houses", "plains");
		}
		// Japanese villages get a teahouse of their own.
		POOLS.put("kaisyn:village/flower_forest_japanese/houses", "japanese");
		POOLS.put("kaisyn:village/exclusives/swedish/houses", "taiga");
		POOLS.put("kaisyn:village/old_growth_taiga_polish/houses", "taiga");
		POOLS.put("kaisyn:village/snowy_taiga_viking/houses", "snowy");
		POOLS.put("kaisyn:village/exclusives/mediterranean/houses/regular", "desert");
		POOLS.put("kaisyn:village/exclusives/iberian/houses", "desert");
		POOLS.put("kaisyn:village/exclusives/nilotic/houses", "desert");
		POOLS.put("kaisyn:village/badlands_pueblo/houses", "desert");
	}

	/**
	 * The café's share of each houses pool: a fifth of the houses already there. The cottage only fits
	 * on some plots, so this works out at about one village in two. -Despresso.cafeWeight=300 makes them
	 * turn up everywhere, for testing.
	 */
	private static final Integer TEST_WEIGHT = Integer.getInteger("espresso.cafeWeight");

	private VillageCafe() {
	}

	static void register() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			Registry<StructureTemplatePool> pools = server.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
			Registry<StructureProcessorList> processors = server.registryAccess().lookupOrThrow(Registries.PROCESSOR_LIST);
			int added = 0;
			for (Map.Entry<String, String> entry : POOLS.entrySet()) {
				StructureTemplatePool pool = pools.getOptional(Identifier.parse(entry.getKey())).orElse(null);
				if (pool == null) {
					continue;
				}
				String style = entry.getValue();
				String location = Espresso.MOD_ID + ":village/" + style + "/cafe";
				StructureTemplatePoolAccessor access = (StructureTemplatePoolAccessor) pool;
				ObjectArrayList<StructurePoolElement> templates = access.espresso$templates();
				if (templates.stream().anyMatch(element -> element.toString().contains(location))) {
					continue;
				}
				int total = access.espresso$rawTemplates().stream().mapToInt(Pair::getSecond).sum();
				int weight = TEST_WEIGHT != null ? TEST_WEIGHT : Math.max(1, Math.round(total / 5.0F));
				// Plains and taiga houses get a little moss, like their neighbours.
				Holder<StructureProcessorList> weathering = style.equals("plains") || style.equals("taiga")
						? processors.getOrThrow(ProcessorLists.MOSSIFY_10_PERCENT) : processors.getOrThrow(ProcessorLists.EMPTY);
				StructurePoolElement cafe = StructurePoolElement.single(location, weathering).apply(StructureTemplatePool.Projection.RIGID);
				for (int i = 0; i < weight; i++) {
					templates.add(cafe);
				}
				// Also in the weighted list the pool was loaded with, which mods like Lithostitched rebuild it from.
				List<Pair<StructurePoolElement, Integer>> raw = new ArrayList<>(access.espresso$rawTemplates());
				raw.add(Pair.of(cafe, weight));
				access.espresso$setRawTemplates(raw);
				added++;
			}
			Espresso.LOGGER.info("Village cafés added to {} village houses pools{}", added, TEST_WEIGHT != null ? " (test weight " + TEST_WEIGHT + ")" : "");
		});
	}
}
