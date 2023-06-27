package de.hglabor.superman.mixins;

import de.hglabor.superman.common.entity.SupermanKt;
import net.minecraft.entity.Attackable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Attackable {
    public LivingEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"))
    private void travelInjection(Vec3d vec3d, CallbackInfo ci) {
        if ((LivingEntity) (Object) this instanceof PlayerEntity player) {
            if (SupermanKt.isSupermanFlying(player)) {
                Vec3d rotationVector = this.getRotationVector().normalize().multiply(SupermanKt.isFastMode(player) ? 4.0 : 1.0);
                this.setVelocity(rotationVector.getX(), rotationVector.getY(), rotationVector.getZ());
            }
        }
    }
}
