package com.gabbymaybelle.espresso;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

/**
 * Home beds. A player chooses their home by crouching and using a bed with an empty hand;
 * sleeping on its own doesn't change it, so a night in a travel bed doesn't move home. Each
 * player has one home, saved with the world. Home is where rest counts: only a night within
 * {@link #HOME_RADIUS} blocks of it clears Drowsy ({@link Wellbeing}), and Cozy heals you near
 * it. (Until 0.3.0 home also kept monsters from spawning nearby; Cozy Core's Hearth Lantern does
 * that now.)
 */
public final class HomeBeds {
	/** How far from the home bed still counts as home: any bed within it is sleeping at home, and Cozy heals within it. */
	public static final int HOME_RADIUS = 32;

	private static final Platform.Data<Map<UUID, GlobalPos>> HOMES = Platform.data("home_beds",
			Codec.unboundedMap(UUIDUtil.STRING_CODEC, GlobalPos.CODEC), HashMap::new, false);

	private HomeBeds() {
	}

	static void register() {
		Platform.get().onUseBlock((player, world, hand, hit) -> {
			if (hand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown()
					|| !player.getMainHandItem().isEmpty()
					|| !(world.getBlockState(hit.getBlockPos()).getBlock() instanceof AbstractBedBlock)) {
				return InteractionResult.PASS;
			}
			if (player instanceof ServerPlayer serverPlayer && world instanceof ServerLevel level) {
				if (!canBeHome(level, hit.getBlockPos())) {
					serverPlayer.sendOverlayMessage(Component.translatable("message.espresso_flavoured.home_not_here"));
				} else if (isSomeoneElsesHome(serverPlayer, level, hit.getBlockPos())) {
					serverPlayer.sendOverlayMessage(Component.translatable("message.espresso_flavoured.home_taken"));
				} else {
					setHome(serverPlayer, level, hit.getBlockPos());
				}
			}
			// Handled on both sides, so crouch-using a bed never also tries to sleep in it (or, in
			// the Nether and the End, blows up).
			return InteractionResult.SUCCESS;
		});
	}

	/**
	 * Only a bed you could use to set your respawn point can be a home: in the Nether and the End,
	 * where beds blow up, a home could never be slept in, so Drowsy would never clear.
	 */
	private static boolean canBeHome(ServerLevel level, BlockPos bed) {
		if (!(level.getBlockState(bed).getBlock() instanceof AbstractBedBlock block)) {
			return false;
		}
		BedRule rule = block.getBedRule(level, bed);
		return !rule.destroyOnUse() && rule.canSetSpawn(level);
	}

	/**
	 * True when another player has already made this bed their home, so two players never share
	 * (or take over) one home by accident. A home is saved at whichever half was clicked, so both
	 * halves of the bed are checked.
	 */
	private static boolean isSomeoneElsesHome(ServerPlayer player, ServerLevel level, BlockPos bed) {
		BlockState state = level.getBlockState(bed);
		BlockPos otherHalf = bed.relative(state.getValue(AbstractBedBlock.PART) == BedPart.FOOT
				? state.getValue(AbstractBedBlock.FACING) : state.getValue(AbstractBedBlock.FACING).getOpposite());
		for (Map.Entry<UUID, GlobalPos> home : store(level).entrySet()) {
			if (!home.getKey().equals(player.getUUID()) && home.getValue().dimension() == level.dimension()
					&& (home.getValue().pos().equals(bed) || home.getValue().pos().equals(otherHalf))) {
				return true;
			}
		}
		return false;
	}

	private static void setHome(ServerPlayer player, ServerLevel level, BlockPos bed) {
		Map<UUID, GlobalPos> homes = new HashMap<>(store(level));
		homes.put(player.getUUID(), GlobalPos.of(level.dimension(), bed.immutable()));
		HOMES.set(level.getServer().overworld(), homes);
		player.sendOverlayMessage(Component.translatable("message.espresso_flavoured.home_set"));
		EspressoAdvancements.award(player, "guide/safe_haven", "home");
	}

	/** Homes are stored on the overworld so every dimension shares one list. */
	private static Map<UUID, GlobalPos> store(ServerLevel level) {
		return HOMES.getOrCreate(level.getServer().overworld());
	}

	/**
	 * True when a bed at {@code bed} counts as sleeping at home for this player: within
	 * {@link #HOME_RADIUS} of their home bed. Players without a home are at home in any bed.
	 */
	public static boolean isAtHome(ServerPlayer player, ServerLevel level, BlockPos bed) {
		GlobalPos home = store(level).get(player.getUUID());
		if (home == null) {
			return true;
		}
		long dx = home.pos().getX() - bed.getX(), dz = home.pos().getZ() - bed.getZ();
		return home.dimension() == level.dimension() && dx * dx + dz * dz <= (long) HOME_RADIUS * HOME_RADIUS;
	}

	/** True when the player is within {@link #HOME_RADIUS} of their own home bed (false with no home). */
	public static boolean isNearHome(ServerPlayer player) {
		if (!(player.level() instanceof ServerLevel level)) {
			return false;
		}
		GlobalPos home = store(level).get(player.getUUID());
		if (home == null || home.dimension() != level.dimension()) {
			return false;
		}
		double dx = home.pos().getX() + 0.5 - player.getX(), dz = home.pos().getZ() + 0.5 - player.getZ();
		return dx * dx + dz * dz <= (double) HOME_RADIUS * HOME_RADIUS;
	}
}
