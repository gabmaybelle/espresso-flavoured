package com.gabbymaybelle.espresso.compat;

import com.gabbymaybelle.espresso.Espresso;
import com.gabbymaybelle.espresso.content.CoffeeBushBlock;
import com.gabbymaybelle.espresso.content.EspressoMachineBlock;
import com.gabbymaybelle.espresso.content.EspressoMachineBlockEntity;
import com.gabbymaybelle.espresso.content.Mugs;
import com.gabbymaybelle.espresso.content.PlacedMugBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.JadeIds;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

/**
 * Jade tooltips (only loaded when Jade is installed): what's in a placed mug, how far along a
 * coffee bush is, and how the espresso machine's brew is going.
 */
@WailaPlugin
public class EspressoJadePlugin implements IWailaPlugin {
	private static final Identifier BUSH = Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "coffee_bush");
	private static final Identifier MUG = Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "placed_mug");
	private static final Identifier MACHINE = Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "espresso_machine");

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(MachineData.INSTANCE, EspressoMachineBlockEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerBlockComponent(BushTooltip.INSTANCE, CoffeeBushBlock.class);
		registration.registerBlockComponent(MachineTooltip.INSTANCE, EspressoMachineBlock.class);
		registration.registerBlockComponent(MugTooltip.INSTANCE, PlacedMugBlock.class);
	}

	/** "Seedling", "Young", "Grown", "Turning" or "Ripe: ready to pick". */
	enum BushTooltip implements IBlockComponentProvider {
		INSTANCE;

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			int age = accessor.getBlockState().getValue(CoffeeBushBlock.AGE);
			Component stage = Component.translatable("jade.espresso_flavoured.coffee_bush." + age);
			tooltip.add(age == CoffeeBushBlock.MAX_AGE ? stage.copy().withStyle(ChatFormatting.GREEN) : stage);
		}

		@Override
		public Identifier getUid() {
			return BUSH;
		}
	}

	/** A placed mug is named after its drink ("Honey Latte") rather than just "Mug". */
	enum MugTooltip implements IBlockComponentProvider {
		INSTANCE;

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			PlacedMugBlock.Contents contents = accessor.getBlockState().getValue(PlacedMugBlock.CONTENTS);
			if (contents != PlacedMugBlock.Contents.EMPTY) {
				tooltip.replace(JadeIds.CORE_OBJECT_NAME, IThemeHelper.get().title(Mugs.drinkFor(contents).getHoverName()));
			}
		}

		@Override
		public Identifier getUid() {
			return MUG;
		}
	}

	/** Brewing (with how far along), waiting for beans or mugs, or not on a coffee bar. */
	enum MachineTooltip implements IBlockComponentProvider {
		INSTANCE;

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			CompoundTag data = accessor.getServerData();
			if (!data.getBooleanOr("OnCoffeeBar", true)) {
				tooltip.add(Component.translatable("jade.espresso_flavoured.espresso_machine.no_bar").withStyle(ChatFormatting.RED));
				return;
			}
			int brewTime = data.getIntOr("BrewTime", 0);
			if (brewTime > 0) {
				int percent = (EspressoMachineBlockEntity.BREW_TIME - brewTime) * 100 / EspressoMachineBlockEntity.BREW_TIME;
				tooltip.add(Component.translatable("jade.espresso_flavoured.espresso_machine.brewing", percent));
			} else {
				tooltip.add(Component.translatable("jade.espresso_flavoured.espresso_machine.idle").withStyle(ChatFormatting.GRAY));
			}
		}

		@Override
		public Identifier getUid() {
			return MACHINE;
		}
	}

	enum MachineData implements IServerDataProvider<BlockAccessor> {
		INSTANCE;

		@Override
		public void appendServerData(CompoundTag data, BlockAccessor accessor) {
			if (accessor.getBlockEntity() instanceof EspressoMachineBlockEntity machine) {
				data.putInt("BrewTime", machine.brewTime());
				data.putBoolean("OnCoffeeBar", machine.onCoffeeBar());
			}
		}

		@Override
		public Identifier getUid() {
			return MACHINE;
		}
	}
}
