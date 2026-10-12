package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.Espresso;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Rest spots only on dry land. They sit on the world surface, which counts water, so in a lake
 * the bench and lamp post ended up standing in the water. Where any part of the spot (it can turn
 * any way about its start, so 8 blocks each side) has water on top, it isn't placed.
 */
@Mixin(JigsawStructure.class)
public abstract class RestSpotMixin {
	private static final Identifier REST_SPOT = Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "rest_spot");

	@Shadow @Final private Holder<StructureTemplatePool> startPool;

	@Inject(method = "findGenerationPoint", at = @At("HEAD"), cancellable = true)
	private void espresso$dryLandOnly(Structure.GenerationContext context, CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir) {
		if (!startPool.is(REST_SPOT)) {
			return;
		}
		ChunkPos chunk = context.chunkPos();
		for (int dx = -8; dx <= 8; dx += 4) {
			for (int dz = -8; dz <= 8; dz += 4) {
				int x = chunk.getMinBlockX() + dx, z = chunk.getMinBlockZ() + dz;
				int surface = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
				int floor = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
				if (surface != floor) {
					cir.setReturnValue(Optional.empty());
					return;
				}
			}
		}
	}
}
