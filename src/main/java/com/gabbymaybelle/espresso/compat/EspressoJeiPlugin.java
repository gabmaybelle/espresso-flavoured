package com.gabbymaybelle.espresso.compat;

import com.gabbymaybelle.espresso.Espresso;
import com.gabbymaybelle.espresso.content.EspressoContent;
import com.gabbymaybelle.espresso.content.Mugs;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * JEI (only loaded when JEI is installed): a page for what the espresso machine brews, since its
 * drinks aren't crafting recipes, and a note on where water lilies come from.
 */
public class EspressoJeiPlugin implements IModPlugin {
	/** One brew: what goes in the top slot, what each mug becomes, and whether it leaves grounds. */
	public record Brew(ItemStack beans, ItemStack drink, boolean grounds) {
	}

	static final IRecipeType<Brew> BREWING = IRecipeType.create(Espresso.MOD_ID, "espresso_machine", Brew.class);

	@Override
	public Identifier getPluginUid() {
		return Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "jei");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(new BrewingCategory(registration.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		ItemStack mug = new ItemStack(EspressoContent.MUG);
		registration.addRecipes(BREWING, List.of(
				new Brew(new ItemStack(EspressoContent.ROASTED_COFFEE_BEANS), Mugs.fill(mug, EspressoContent.ESPRESSO), true),
				new Brew(new ItemStack(EspressoContent.DECAF_BEANS), Mugs.fill(mug, EspressoContent.ESPRESSO, true), true),
				new Brew(new ItemStack(EspressoContent.BARISTAS_RESERVE), Mugs.fill(mug, EspressoContent.RISTRETTO), true),
				new Brew(new ItemStack(EspressoContent.COCOA_MIX), Mugs.fill(mug, EspressoContent.HOT_CHOCOLATE), false)));
		registration.addIngredientInfo(EspressoContent.WATER_LILY_ITEM, Component.translatable("jei.espresso_flavoured.water_lily.info"));
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addCraftingStation(BREWING, EspressoContent.ESPRESSO_MACHINE_ITEM);
	}

	/** Beans on top and a mug below, an arrow, then the drink (and the used grounds). */
	static class BrewingCategory implements IRecipeCategory<Brew> {
		private final IDrawable icon;

		BrewingCategory(IGuiHelper gui) {
			icon = gui.createDrawableItemLike(EspressoContent.ESPRESSO_MACHINE_ITEM);
		}

		@Override
		public IRecipeType<Brew> getRecipeType() {
			return BREWING;
		}

		@Override
		public Component getTitle() {
			return Component.translatable("block.espresso_flavoured.espresso_machine");
		}

		@Override
		public int getWidth() {
			return 116;
		}

		@Override
		public int getHeight() {
			return 50;
		}

		@Override
		public IDrawable getIcon() {
			return icon;
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, Brew brew, IFocusGroup focuses) {
			builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).setStandardSlotBackground().add(brew.beans());
			builder.addSlot(RecipeIngredientRole.INPUT, 1, 21).setStandardSlotBackground()
					.addItemStacks(List.of(new ItemStack(EspressoContent.MUG), new ItemStack(EspressoContent.GOLDEN_MUG)));
			builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 11).setStandardSlotBackground().add(brew.drink());
			if (brew.grounds()) {
				builder.addSlot(RecipeIngredientRole.OUTPUT, 91, 11).setStandardSlotBackground().add(new ItemStack(EspressoContent.COFFEE_GROUNDS));
			}
		}

		@Override
		public void createRecipeExtras(IRecipeExtrasBuilder builder, Brew brew, IFocusGroup focuses) {
			builder.addAnimatedRecipeArrowWidget(200).setPosition(27, 11);
			builder.addText(Component.translatable("jei.espresso_flavoured.espresso_machine.needs_bar").withStyle(ChatFormatting.GRAY), 116, 10)
					.setPosition(0, 41);
		}
	}
}
