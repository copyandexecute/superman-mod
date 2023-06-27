package de.hglabor.superman.mixins;

import de.hglabor.superman.client.xray.XrayManagerClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow private double eventDeltaWheel;

    @Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSpectator()Z"))
    private void afterMouseScrollEvent(long l, double d, double e, CallbackInfo ci) {
        XrayManagerClient.INSTANCE.afterMouseScrollEvent(l, d, e, this.eventDeltaWheel);
    }
}
