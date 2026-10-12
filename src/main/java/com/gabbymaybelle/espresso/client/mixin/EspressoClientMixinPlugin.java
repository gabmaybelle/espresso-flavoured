package com.gabbymaybelle.espresso.client.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/**
 * Hands Mixin only the client mixins that have a target: the Iris one is left out when Iris isn't
 * installed, so the log never shows an "Error loading class" line for a class that was never meant
 * to be there.
 */
public class EspressoClientMixinPlugin implements IMixinConfigPlugin {
	@Override
	public void onLoad(String mixinPackage) {
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		// Mixins named here apply on either side, so on a dedicated server (no client classes) there are none.
		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
			return List.of();
		}
		List<String> mixins = new ArrayList<>(List.of("LocalPlayerMixin"));
		if (FabricLoader.getInstance().isModLoaded("iris")) {
			mixins.add("IrisWorldTimeMixin");
		}
		return mixins;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
}
