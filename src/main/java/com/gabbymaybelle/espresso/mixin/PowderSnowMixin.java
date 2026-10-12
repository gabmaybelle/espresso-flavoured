package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.content.CharmItem;
import com.gabbymaybelle.espresso.content.EspressoContent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.PowderSnowBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A Pocket Hearth lets you walk on powder snow, like leather boots. */
@Mixin(PowderSnowBlock.class)
public abstract class PowderSnowMixin {
	@Inject(method = "canEntityWalkOnPowderSnow", at = @At("HEAD"), cancellable = true)
	private static void espresso$hearth(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if (entity instanceof LivingEntity living && CharmItem.isWorn(living, EspressoContent.POCKET_HEARTH)) {
			cir.setReturnValue(true);
		}
	}
}
