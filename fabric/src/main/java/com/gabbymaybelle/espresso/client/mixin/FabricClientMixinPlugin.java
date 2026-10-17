package com.gabbymaybelle.espresso.client.mixin;

import net.fabricmc.loader.api.FabricLoader;

/** The client mixin plugin on Fabric. */
public class FabricClientMixinPlugin extends EspressoClientMixinPlugin {
	@Override
	protected boolean isLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}
}
