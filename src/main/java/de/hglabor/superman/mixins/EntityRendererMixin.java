package de.hglabor.superman.mixins;

import de.hglabor.superman.client.xray.XrayManagerClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {
    @Shadow
    @Final
    protected EntityRenderDispatcher dispatcher;

    @Shadow
    @Final
    private TextRenderer textRenderer;

    @Inject(method = "render", at = @At("HEAD"))
    private void renderInjection(T entity, float tickDelta, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (XrayManagerClient.INSTANCE.isXrayingAtEntity(entity) && entity instanceof LivingEntity livingEntity) {
            XrayManagerClient.INSTANCE.renderXrayInformation(livingEntity, tickDelta, g, matrixStack, vertexConsumerProvider, i, textRenderer, dispatcher);
        }
    }
}
