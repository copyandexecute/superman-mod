package de.hglabor.superman.mixins;

import com.mojang.authlib.GameProfile;
import de.hglabor.superman.Manager;
import de.hglabor.superman.client.IAnimatedPlayer;
import de.hglabor.superman.client.fly.FlyManagerClient;
import de.hglabor.superman.common.entity.SupermanKt;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin implements IAnimatedPlayer {

    //Unique annotation will rename private methods/fields if needed to avoid collisions.
    @Unique
    private final ModifierLayer<IAnimation> modAnimationContainer = new ModifierLayer<>();

    /**
     * Add the animation registration to the end of the constructor
     * Or you can use {@link dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess#REGISTER_ANIMATION_EVENT} event for this
     */
    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void init(ClientWorld world, GameProfile profile, CallbackInfo ci) {
        //Mixin does not know (yet) that this will be merged with AbstractClientPlayerEntity
        PlayerAnimationAccess.getPlayerAnimLayer((AbstractClientPlayerEntity) (Object) this).addAnimLayer(1000, modAnimationContainer); //Register the layer with a priority
    }

    @Inject(method = "getCapeTexture", at = @At("HEAD"), cancellable = true)
    private void getCapeTextureInjection(CallbackInfoReturnable<Identifier> cir) {
        if (SupermanKt.isSuperman((PlayerEntity) ((Object) this))) {
            cir.setReturnValue(FlyManagerClient.INSTANCE.getCape());
        }
    }

    @Inject(method = "getSkinTexture", at = @At("HEAD"), cancellable = true)
    private void getSkinTextureInjection(CallbackInfoReturnable<Identifier> cir) {
        if (SupermanKt.isSuperman((PlayerEntity) ((Object) this))) {
            cir.setReturnValue(Manager.INSTANCE.getSupermanSkin());
        }
    }

    /**
     * Override the interface function, so we can use it in the future
     */
    @Override
    public @NotNull ModifierLayer<IAnimation> superman_getModAnimation() {
        return modAnimationContainer;
    }
}
