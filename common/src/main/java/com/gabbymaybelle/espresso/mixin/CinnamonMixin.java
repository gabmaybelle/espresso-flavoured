package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.Espresso;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cinnamon: stripping a log in the {@code espresso_flavoured:cinnamon_bark} block tag (jungle and
 * acacia) sometimes drops a piece of bark, rolled from the {@code espresso_flavoured:strip/cinnamon}
 * loot table. This watches the data-driven transformer that axes (and hoes and shovels) use, so
 * vanilla's own transformer data is left alone and other mods' woods keep stripping as they should.
 */
@Mixin(BlockTransformer.class)
public abstract class CinnamonMixin {
	@Unique
	private static final TagKey<Block> ESPRESSO$CINNAMON_BARK = TagKey.create(Registries.BLOCK,
			Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "cinnamon_bark"));
	@Unique
	private static final ResourceKey<LootTable> ESPRESSO$CINNAMON = ResourceKey.create(Registries.LOOT_TABLE,
			Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "strip/cinnamon"));
	/** The block that was there before this transform, per thread (the server and client each call this). */
	@Unique
	private static final ThreadLocal<BlockState> ESPRESSO$BEFORE = new ThreadLocal<>();

	@Inject(method = "transformBlock", at = @At("HEAD"))
	private void espresso$rememberBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
		ESPRESSO$BEFORE.set(context.getLevel().getBlockState(context.getClickedPos()));
	}

	@Inject(method = "transformBlock", at = @At("RETURN"))
	private void espresso$dropCinnamon(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
		BlockState before = ESPRESSO$BEFORE.get();
		ESPRESSO$BEFORE.remove();
		if (before == null || !cir.getReturnValue().consumesAction() || !(context.getLevel() instanceof ServerLevel level)
				|| !before.is(ESPRESSO$CINNAMON_BARK)) {
			return;
		}
		BlockPos pos = context.getClickedPos();
		if (level.getBlockState(pos) == before) {
			return; // nothing changed (the shield won, say)
		}
		Block.dropFromBlockInteractLootTable(level, ESPRESSO$CINNAMON, pos, before, level.getBlockEntity(pos), context.getItemInHand(),
				context.getPlayer(), (world, stack) -> Block.popResourceFromFace(world, pos, context.getClickedFace(), stack));
	}
}
