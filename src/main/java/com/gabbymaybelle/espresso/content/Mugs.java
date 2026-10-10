package com.gabbymaybelle.espresso.content;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Mugs: clay mugs hold every coffee drink, keep their dye colour through brewing, mixing and
 * drinking, and can be set down on any surface by crouching and using one on it.
 */
public final class Mugs {
	/** The colour of an undyed mug: glazed cream. */
	public static final int PLAIN = 0xFFF0E4CF;

	private Mugs() {
	}

	/** A drink in this mug: same colour, and drinking it gives this mug back. */
	public static ItemStack fill(ItemStack mug, Item drink) {
		ItemStack filled = new ItemStack(drink);
		DyedItemColor color = mug.get(DataComponents.DYED_COLOR);
		if (color != null) {
			filled.set(DataComponents.DYED_COLOR, color);
		}
		filled.set(DataComponents.USE_REMAINDER, new UseRemainder(ItemStackTemplate.fromNonEmptyStack(mug, 1)));
		return filled;
	}

	public static boolean isMug(ItemStack stack) {
		return stack.is(EspressoContent.MUG) || contentsOf(stack) != PlacedMugBlock.Contents.EMPTY;
	}

	/** What's in a mug item; EMPTY for an empty mug (or anything that isn't a mug). */
	public static PlacedMugBlock.Contents contentsOf(ItemStack stack) {
		if (stack.is(EspressoContent.ESPRESSO)) {
			return PlacedMugBlock.Contents.ESPRESSO;
		}
		if (stack.is(EspressoContent.LATTE)) {
			return PlacedMugBlock.Contents.LATTE;
		}
		if (stack.is(EspressoContent.MOCHA_LATTE)) {
			return PlacedMugBlock.Contents.MOCHA;
		}
		return PlacedMugBlock.Contents.EMPTY;
	}

	/** Placed mugs show the nearest of the 16 dye colours (0 = undyed); the exact colour is kept for when it's picked up. */
	public static int colorIndex(ItemStack stack) {
		DyedItemColor color = stack.get(DataComponents.DYED_COLOR);
		if (color == null) {
			return 0;
		}
		int rgb = color.rgb(), best = 0;
		long bestDistance = Long.MAX_VALUE;
		for (DyeColor dye : DyeColor.values()) {
			int d = dye.getTextureDiffuseColor();
			long dr = ((rgb >> 16) & 255) - ((d >> 16) & 255), dg = ((rgb >> 8) & 255) - ((d >> 8) & 255), db = (rgb & 255) - (d & 255);
			long distance = dr * dr + dg * dg + db * db;
			if (distance < bestDistance) {
				bestDistance = distance;
				best = dye.getId() + 1;
			}
		}
		return best;
	}

	/** The tint for a placed mug's colour index. */
	public static int tint(int colorIndex) {
		return colorIndex == 0 ? PLAIN : 0xFF000000 | DyeColor.byId(colorIndex - 1).getTextureDiffuseColor();
	}

	static void register() {
		// Crouch and use a mug (empty or full) on a surface to set it down there.
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			ItemStack stack = player.getItemInHand(hand);
			if (!player.isShiftKeyDown() || !isMug(stack)) {
				return InteractionResult.PASS;
			}
			BlockPos pos = hit.getBlockPos();
			if (!level.getBlockState(pos).canBeReplaced()) {
				pos = pos.relative(hit.getDirection());
			}
			BlockState state = EspressoContent.PLACED_MUG.defaultBlockState()
					.setValue(PlacedMugBlock.FACING, player.getDirection().getOpposite())
					.setValue(PlacedMugBlock.CONTENTS, contentsOf(stack))
					.setValue(PlacedMugBlock.COLOR, colorIndex(stack));
			if (!level.getBlockState(pos).canBeReplaced() || !state.canSurvive(level, pos)) {
				return InteractionResult.PASS;
			}
			if (!level.isClientSide()) {
				level.setBlock(pos, state, Block.UPDATE_ALL);
				if (level.getBlockEntity(pos) instanceof PlacedMugBlockEntity mug) {
					mug.setMug(stack.copyWithCount(1));
				}
				stack.consume(1, player);
				level.playSound(null, pos, SoundEvents.DECORATED_POT_PLACE, SoundSource.BLOCKS, 1.0F, 1.2F);
				level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, state));
			}
			return InteractionResult.SUCCESS;
		});
	}

	/** Puts a picked-up mug in the player's hand, or their inventory, or at their feet. */
	static void giveBack(Player player, InteractionHand hand, ItemStack stack) {
		if (player.getItemInHand(hand).isEmpty()) {
			player.setItemInHand(hand, stack);
		} else {
			player.getInventory().placeItemBackInInventory(stack, Prediction.SERVER_ONLY);
		}
	}
}
