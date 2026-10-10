package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.Config;
import net.minecraft.world.level.block.SugarCaneBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Sugar cane grows {@link Config#sugarCaneHeight} blocks tall (vanilla: 3), so a few plants keep you in sugar. */
@Mixin(SugarCaneBlock.class)
public abstract class SugarCaneBlockMixin {
	@ModifyConstant(method = "randomTick", constant = @Constant(intValue = 3))
	private int espresso$caneHeight(int vanilla) {
		return Config.sugarCaneHeight;
	}
}
