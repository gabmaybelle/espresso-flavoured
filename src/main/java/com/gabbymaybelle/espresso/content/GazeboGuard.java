package com.gabbymaybelle.espresso.content;

import com.gabbymaybelle.espresso.Config;
import com.gabbymaybelle.espresso.Espresso;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Keeps the spawn gazebo standing: inside its box, players outside creative can't break or place
 * blocks or reshape them with tools, and explosions, fire, pistons and buckets leave it alone (see
 * the mixins that call {@link #guards}). The chest, the benches and the flower pots still work, and
 * the two decorated pots can still be broken for what's inside them. {@link Config#protectGazebo}
 * switches it off for anyone who wants to remodel.
 */
public final class GazeboGuard {
	/** The gazebo's box in the overworld, saved when it's built. */
	private static final AttachmentType<BoundingBox> BOX = AttachmentRegistry.create(
			Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "spawn_gazebo_box"),
			builder -> builder.persistent(BoundingBox.CODEC));
	/** How far below the ground the box reaches, so nobody digs the gazebo out from underneath. */
	static final int DEPTH = 3;
	/** How far above the ground the box reaches: the roof, with a little to spare. */
	static final int HEIGHT = 10;

	/** The running server's gazebo, or null when there's none. */
	private static volatile BoundingBox box;

	private GazeboGuard() {
	}

	static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> box = load(server.overworld()));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> box = null);
		PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) ->
				state.getBlock() instanceof DecoratedPotBlock || !protects(level, player, pos));
		// Tools that turn one block into another: stripping, paths, farmland.
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			ItemStack stack = player.getItemInHand(hand);
			boolean reshapes = stack.is(ItemTags.AXES) || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES);
			return reshapes && protects(level, player, hit.getBlockPos()) ? InteractionResult.FAIL : InteractionResult.PASS;
		});
	}

	/** Remembers where the gazebo was just built. */
	static void guard(ServerLevel level, BoundingBox built) {
		level.setAttached(BOX, built);
		box = built;
	}

	/** True when {@code pos} is part of the gazebo and it's being protected. Server side only. */
	public static boolean guards(Level level, BlockPos pos) {
		BoundingBox current = box;
		return current != null && Config.protectGazebo && !level.isClientSide()
				&& level.dimension() == Level.OVERWORLD && current.isInside(pos);
	}

	/** {@link #guards}, unless it's a creative player doing it (so the gazebo can still be edited). */
	public static boolean protects(Level level, Entity entity, BlockPos pos) {
		return !(entity instanceof Player player && player.isCreative()) && guards(level, pos);
	}

	/**
	 * The saved box. Worlds whose gazebo was built before the box was saved get one if the spore
	 * blossom still hangs four blocks above the spawn point, where every version of the gazebo put it.
	 */
	private static BoundingBox load(ServerLevel level) {
		BoundingBox saved = level.getAttached(BOX);
		if (saved != null) {
			return saved;
		}
		BlockPos spawn = level.getRespawnData().pos();
		if (!level.getBlockState(spawn.above(4)).is(Blocks.SPORE_BLOSSOM)) {
			return null;
		}
		// Older gazebos stood a block lower; take in both.
		int ground = spawn.getY() - 3;
		BoundingBox found = new BoundingBox(spawn.getX() - SpawnGazebo.HALF, ground - 1 - DEPTH, spawn.getZ() - SpawnGazebo.HALF,
				spawn.getX() + SpawnGazebo.HALF, ground + HEIGHT, spawn.getZ() + SpawnGazebo.HALF);
		level.setAttached(BOX, found);
		Espresso.LOGGER.info("Found the spawn gazebo at {}; it's protected now", spawn);
		return found;
	}
}
