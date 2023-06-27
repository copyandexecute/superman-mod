package de.hglabor.superman.mixins;

import de.hglabor.superman.client.laser.LaserManagerClient;
import de.hglabor.superman.client.xray.XrayManagerClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;getArmorStack(I)Lnet/minecraft/item/ItemStack;"))
    private void onRender(DrawContext drawContext, float delta, CallbackInfo ci) {
        LaserManagerClient.INSTANCE.onHudRender(drawContext, delta);
        XrayManagerClient.INSTANCE.onHudRender(drawContext, delta);
    }
}
