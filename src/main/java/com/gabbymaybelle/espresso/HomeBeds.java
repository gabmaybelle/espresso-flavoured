package com.gabbymaybelle.espresso;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
	private static final Identifier SAFE_HAVEN = Identifier.fromNamespaceAndPath(Espresso.MOD_ID, "guide/safe_haven");

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
				setHome(serverPlayer, level, hit.getBlockPos());
			}
			// Handled on both sides, so crouch-using a bed never also tries to sleep in it.
			return InteractionResult.SUCCESS;
		});
	}

	private static void setHome(ServerPlayer player, ServerLevel level, BlockPos bed) {
		Map<UUID, GlobalPos> homes = new HashMap<>(store(level));
		homes.put(player.getUUID(), GlobalPos.of(level.dimension(), bed.immutable()));
		level.getServer().overworld().setAttached(HOMES, homes);
		player.sendOverlayMessage(Component.literal("This bed is now your home. Monsters won't spawn within "
				+ Config.safeHomeRadius + " blocks of it."));
		AdvancementHolder advancement = level.getServer().getAdvancements().get(SAFE_HAVEN);
		if (advancement != null) {
			player.getAdvancements().award(advancement, "home");
		}
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
