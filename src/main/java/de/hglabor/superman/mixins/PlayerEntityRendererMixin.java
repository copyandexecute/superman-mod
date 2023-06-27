package de.hglabor.superman.mixins;

import de.hglabor.superman.client.laser.LaserRenderer;
import de.hglabor.superman.common.entity.Superman;
import de.hglabor.superman.common.entity.SupermanKt;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.silkmc.silk.core.entity.MovementExtensionsKt;
import net.silkmc.silk.core.entity.StateExtensionsKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    public PlayerEntityRendererMixin(EntityRendererFactory.Context context, PlayerEntityModel<AbstractClientPlayerEntity> entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = {@At("RETURN")})
    private void renderLaserEyes(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        LaserRenderer.INSTANCE.renderLaserBeam(abstractClientPlayerEntity, tickDelta, matrixStack, vertexConsumerProvider, false);
    }

    @Inject(method = "setupTransforms(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;FFF)V", at = @At(value = "HEAD"), cancellable = true)
    private void setupTransformsInjection(AbstractClientPlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, float f, float g, float h, CallbackInfo ci) {
        var i = ((Superman) abstractClientPlayerEntity).getFlyingLeaningPitch(h);
        if (i > 0.0F) {
            super.setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
            var j = -90f - abstractClientPlayerEntity.getPitch();
            var k = MathHelper.lerp(i, 0.0F, j);
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(k));
            ci.cancel();
        }
    }
}
