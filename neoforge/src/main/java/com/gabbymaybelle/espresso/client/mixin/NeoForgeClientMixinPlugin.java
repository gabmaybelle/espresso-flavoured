package com.gabbymaybelle.espresso.client.mixin;

import net.neoforged.fml.loading.FMLLoader;

/** The client mixin plugin on NeoForge. */
public class NeoForgeClientMixinPlugin extends EspressoClientMixinPlugin {
	@Override
	protected boolean isLoaded(String modId) {
		return FMLLoader.getCurrent().getLoadingModList().getModFileById(modId) != null;
	}
}
