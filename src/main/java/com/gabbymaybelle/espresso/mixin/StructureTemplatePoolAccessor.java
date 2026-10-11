package com.gabbymaybelle.espresso.mixin;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * A jigsaw pool's pieces, so the café can join village houses: the weighted list it was loaded
 * with (which some mods, like Lithostitched, rebuild the pool from) and the expanded list it picks from.
 */
@Mixin(StructureTemplatePool.class)
public interface StructureTemplatePoolAccessor {
	@Accessor("templates")
	ObjectArrayList<StructurePoolElement> espresso$templates();

	@Accessor("rawTemplates")
	List<Pair<StructurePoolElement, Integer>> espresso$rawTemplates();

	@Mutable
	@Accessor("rawTemplates")
	void espresso$setRawTemplates(List<Pair<StructurePoolElement, Integer>> rawTemplates);
}
