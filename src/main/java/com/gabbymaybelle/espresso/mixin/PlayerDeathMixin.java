package com.gabbymaybelle.espresso.mixin;

import com.gabbymaybelle.espresso.KeepOnDeath;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keep the hotbar, armour and off-hand before the rest of the inventory drops. */
@Mixin(Player.class)
public abstract class PlayerDeathMixin {
	@Inject(method = "dropEquipment", at = @At("HEAD"))
	private void espresso$keepHotbarAndArmour(ServerLevel level, CallbackInfo ci) {
		KeepOnDeath.stashKeptItems((Player) (Object) this);
	}
}
