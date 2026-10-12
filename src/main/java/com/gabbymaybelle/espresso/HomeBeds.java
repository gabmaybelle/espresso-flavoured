package com.gabbymaybelle.espresso;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.level.block.AbstractBedBlock;

/**
 * The safe home radius. A player chooses their home by crouching and using a bed with an
 * empty hand; sleeping on its own doesn't change it, so a night in a travel bed leaves
 * the base protected. Each player has one home, saved with the world. Hostile mobs don't
 * spawn naturally within {@link Config#safeHomeRadius} blocks (measured across, at any
 * height) of any player's home.
 */
public final class HomeBeds {
	private static final AttachmentType<Map<UUID, GlobalPos>> HOMES = AttachmentRegistry.create(
			Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "home_beds"),
			builder -> builder
					.persistent(Codec.unboundedMap(UUIDUtil.STRING_CODEC, GlobalPos.CODEC))
					.initializer(HashMap::new));

	private HomeBeds() {
	}

	static void register() {
		UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
			if (Config.safeHomeRadius <= 0 || hand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown()
					|| !player.getMainHandItem().isEmpty()
					|| !(world.getBlockState(hit.getBlockPos()).getBlock() instanceof AbstractBedBlock)) {
				return InteractionResult.PASS;
			}
			if (player instanceof ServerPlayer serverPlayer && world instanceof ServerLevel level) {
				if (canBeHome(level, hit.getBlockPos())) {
					setHome(serverPlayer, level, hit.getBlockPos());
				} else {
					serverPlayer.sendOverlayMessage(Component.translatable("message.espresso_flavoured.home_not_here"));
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

	private static void setHome(ServerPlayer player, ServerLevel level, BlockPos bed) {
		Map<UUID, GlobalPos> homes = new HashMap<>(store(level));
		homes.put(player.getUUID(), GlobalPos.of(level.dimension(), bed.immutable()));
		level.getServer().overworld().setAttached(HOMES, homes);
		player.sendOverlayMessage(Component.translatable("message.espresso_flavoured.home_set", Config.safeHomeRadius));
		EspressoAdvancements.award(player, "guide/safe_haven", "home");
	}

	/** Homes are stored on the overworld so every dimension shares one list. */
	private static Map<UUID, GlobalPos> store(ServerLevel level) {
		return level.getServer().overworld().getAttachedOrCreate(HOMES);
	}

	/**
	 * True when a bed at {@code bed} counts as sleeping at home for this player: inside the
	 * safe home radius of their home bed. Players without a home (or with the radius off)
	 * are at home in any bed.
	 */
	public static boolean isAtHome(ServerPlayer player, ServerLevel level, BlockPos bed) {
		GlobalPos home = store(level).get(player.getUUID());
		int radius = Config.safeHomeRadius;
		if (home == null || radius <= 0) {
			return true;
		}
		long dx = home.pos().getX() - bed.getX(), dz = home.pos().getZ() - bed.getZ();
		return home.dimension() == level.dimension() && dx * dx + dz * dz <= (long) radius * radius;
	}

	/** True when the player is within the safe home radius of their own home bed (false with no home). */
	public static boolean isNearHome(ServerPlayer player) {
		if (!(player.level() instanceof ServerLevel level)) {
			return false;
		}
		GlobalPos home = store(level).get(player.getUUID());
		int radius = Math.max(Config.safeHomeRadius, 8);
		if (home == null || home.dimension() != level.dimension()) {
			return false;
		}
		double dx = home.pos().getX() + 0.5 - player.getX(), dz = home.pos().getZ() + 0.5 - player.getZ();
		return dx * dx + dz * dz <= (double) radius * radius;
	}

	/** True when a hostile spawn at {@code pos} would be inside someone's safe home radius. */
	public static boolean isProtected(ServerLevel level, BlockPos pos) {
		int radius = Config.safeHomeRadius;
		if (radius <= 0) {
			return false;
		}
		long limit = (long) radius * radius;
		for (GlobalPos home : store(level).values()) {
			if (home.dimension() != level.dimension()) {
				continue;
			}
			BlockPos bed = home.pos();
			long dx = bed.getX() - pos.getX(), dz = bed.getZ() - pos.getZ();
			if (dx * dx + dz * dz > limit) {
				continue;
			}
			// A bed that has been broken no longer protects anything.
			if (level.isLoaded(bed) && !(level.getBlockState(bed).getBlock() instanceof AbstractBedBlock)) {
				continue;
			}
			return true;
		}
		return false;
	}
}
