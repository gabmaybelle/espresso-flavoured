package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.Wellbeing;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A slice of cake counts as a meal for Wellbeing's hunger clock (it isn't an item, so FoodPropertiesMixin misses it). */
@Mixin(CakeBlock.class)
public abstract class CakeBlockMixin {
	@Inject(method = "eat", at = @At("RETURN"))
	private static void espresso$ateCake(LevelAccessor level, BlockPos pos, BlockState state, Player player,
			CallbackInfoReturnable<InteractionResult> cir) {
		if (player instanceof ServerPlayer serverPlayer && cir.getReturnValue().consumesAction()) {
			Wellbeing.ate(serverPlayer);
		}
	}
}
