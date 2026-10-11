package com.gabbymaybelle.espresso.content;

import com.gabbymaybelle.espresso.Espresso;
import com.gabbymaybelle.espresso.mixin.StructureTemplatePoolAccessor;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
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
 * A little café that can turn up in villages of every style, with an espresso machine on the
 * counter for a barista to claim. Each village's houses pool gets the café (see
 * tools/generate-cafe.js for the buildings) when the server starts.
 */
public final class VillageCafe {
	private static final List<String> STYLES = List.of("plains", "desert", "savanna", "taiga", "snowy");
	/**
	 * Out of roughly 70-90 for the houses already in each pool: about one village in two gets a café.
	 * -Despresso.cafeWeight=200 makes them turn up everywhere, for testing.
	 */
	private static final int WEIGHT = Integer.getInteger("espresso.cafeWeight", 4);

	private VillageCafe() {
	}

	static void register() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			Registry<StructureTemplatePool> pools = server.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
			Registry<StructureProcessorList> processors = server.registryAccess().lookupOrThrow(Registries.PROCESSOR_LIST);
			int added = 0;
			for (String style : STYLES) {
				StructureTemplatePool pool = pools.getOptional(Identifier.withDefaultNamespace("village/" + style + "/houses")).orElse(null);
				if (pool == null) {
					continue;
				}
				String location = Espresso.MOD_ID + ":village/" + style + "/cafe";
				ObjectArrayList<StructurePoolElement> templates = ((StructureTemplatePoolAccessor) pool).espresso$templates();
				if (templates.stream().anyMatch(element -> element.toString().contains(location))) {
					continue;
				}
				// Plains and taiga houses get a little moss, like their neighbours.
				Holder<StructureProcessorList> weathering = style.equals("plains") || style.equals("taiga")
						? processors.getOrThrow(ProcessorLists.MOSSIFY_10_PERCENT) : processors.getOrThrow(ProcessorLists.EMPTY);
				StructurePoolElement cafe = StructurePoolElement.single(location, weathering).apply(StructureTemplatePool.Projection.RIGID);
				for (int i = 0; i < WEIGHT; i++) {
					templates.add(cafe);
				}
				added++;
			}
			Espresso.LOGGER.info("Village cafés added to {} village styles (weight {})", added, WEIGHT);
		});
	}
}
