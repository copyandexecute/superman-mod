package de.hglabor.superman.mixins;

import de.hglabor.superman.common.entity.Superman;
import de.hglabor.superman.common.entity.SupermanKt;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements Superman {
    private float lastFlyingLeaningPitch;
    private float flyingLeaningPitch;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initDataTrackerInjecetion(CallbackInfo ci) {
        this.dataTracker.startTracking(SupermanKt.getLaserEyesTracker(), false);
        this.dataTracker.startTracking(SupermanKt.getFlyTracker(), false);
        this.dataTracker.startTracking(SupermanKt.getXrayTracker(), false);
        this.dataTracker.startTracking(SupermanKt.getXrayModeTracker(), (byte) 0);
        this.dataTracker.startTracking(SupermanKt.getFastModeTracker(), false);
        this.dataTracker.startTracking(SupermanKt.getSupermanTracker(), false);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickInjection(CallbackInfo ci) {
        updateFlyingLeaningPitch();
    }

    private void updateFlyingLeaningPitch() {
        this.lastFlyingLeaningPitch = this.flyingLeaningPitch;
        if (SupermanKt.isSupermanFlying((PlayerEntity) ((Object) this))) {
            this.flyingLeaningPitch = Math.min(1.0F, this.flyingLeaningPitch + 0.09F);
        } else {
            this.flyingLeaningPitch = Math.max(0.0F, this.flyingLeaningPitch - 0.09F);
        }
    }

    @Override
    public boolean canWalkOnFluid(FluidState fluidState) {
        return SupermanKt.isFastMode((PlayerEntity) ((Object) this));
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;noClip:Z", opcode = Opcodes.PUTFIELD))
    private void noClipInjection(PlayerEntity instance, boolean value) {
        if (SupermanKt.isFastMode(instance) && SupermanKt.isSupermanFlying(instance)) {
            instance.noClip = true;
        } else {
            instance.noClip = value;
        }
    }

    @Override
    public boolean isFireImmune() {
        if (SupermanKt.isSuperman((PlayerEntity) (Object) this)) {
            return true;
        } else {
            return super.isFireImmune();
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> trackedData) {
        if (SupermanKt.getFlyTracker().equals(trackedData)) {
            this.calculateDimensions();
        }
        if (SupermanKt.getSupermanTracker().equals(trackedData)) {
            this.setInvulnerable(SupermanKt.isSuperman((PlayerEntity) (Object) this));
        }
    }

    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void getDimensionsInjection(EntityPose entityPose, CallbackInfoReturnable<EntityDimensions> cir) {
        if (SupermanKt.isSupermanFlying((PlayerEntity) ((Object) this))) {
            cir.setReturnValue(EntityDimensions.changing(0.6F, 0.6F));
        }
    }

    @Inject(method = "getActiveEyeHeight", at = @At("HEAD"), cancellable = true)
    private void getActiveEyeHeightInjection(EntityPose entityPose, EntityDimensions entityDimensions, CallbackInfoReturnable<Float> cir) {
        if (SupermanKt.isSupermanFlying((PlayerEntity) ((Object) this))) {
            cir.setReturnValue(0.2F);
        }
    }

    @Override
    public float getFlyingLeaningPitch(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastFlyingLeaningPitch, this.flyingLeaningPitch);
    }
}
