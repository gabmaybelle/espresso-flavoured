package com.gabbymaybelle.espresso.content;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Sitting down: use a bench or chair (any upright stairs) with an empty hand to sit on it, facing
 * away from its back. You sit on an invisible seat that goes away when you stand up (crouch) or
 * the stairs are broken.
 */
public class Seat extends Entity {
	public Seat(EntityType<? extends Seat> type, Level level) {
		super(type, level);
		noPhysics = true;
	}

	static void register() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (hand != InteractionHand.MAIN_HAND || player.isShiftKeyDown() || player.isSpectator() || player.isPassenger()
					|| !player.getMainHandItem().isEmpty() || hit.getDirection() == Direction.DOWN) {
				return InteractionResult.PASS;
			}
			BlockPos pos = hit.getBlockPos();
			BlockState state = level.getBlockState(pos);
			if (!(state.getBlock() instanceof StairBlock) || state.getValue(StairBlock.HALF) != Half.BOTTOM
					|| !level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
					|| player.distanceToSqr(Vec3.atCenterOf(pos)) > 9.0) {
				return InteractionResult.PASS;
			}
			if (!level.getEntitiesOfClass(Seat.class, new AABB(pos)).isEmpty()) {
				return InteractionResult.PASS; // someone's sitting there
			}
			if (!level.isClientSide()) {
				Seat seat = new Seat(EspressoContent.SEAT, level);
				seat.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5); // on the stair's seat
				seat.setYRot(state.getValue(StairBlock.FACING).getOpposite().toYRot());
				level.addFreshEntity(seat);
				player.startRiding(seat);
			}
			return InteractionResult.SUCCESS;
		});
	}

	@Override
	public void tick() {
		super.tick();
		if (level() instanceof ServerLevel && (getPassengers().isEmpty() || !(level().getBlockState(blockPosition()).getBlock() instanceof StairBlock))) {
			ejectPassengers();
			discard();
		}
	}

	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
		return Vec3.ZERO;
	}

	/** Standing up puts you on top of the bench. */
	@Override
	public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
		return Vec3.atBottomCenterOf(blockPosition().above());
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return false;
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
	}
}
