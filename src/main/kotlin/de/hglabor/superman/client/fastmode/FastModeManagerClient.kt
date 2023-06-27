package de.hglabor.superman.client.fastmode

import de.hglabor.superman.client.registry.KeyBindings
import de.hglabor.superman.common.entity.isFastMode
import de.hglabor.superman.common.entity.isSuperman
import de.hglabor.superman.common.network.fastModePacket
import net.minecraft.client.MinecraftClient
import net.minecraft.sound.SoundEvents
import net.silkmc.silk.core.annotations.ExperimentalSilkApi

object FastModeManagerClient {
    @OptIn(ExperimentalSilkApi::class)
    fun init() {
        KeyBindings.onKeyPressedOnce.listen { event ->
            val player = MinecraftClient.getInstance().player ?: return@listen
            if (MinecraftClient.getInstance().options.sneakKey.matchesKey(
                    event.key,
                    event.scanCode
                ) && player.isSuperman
            ) {
                val isFastMode = !player.isFastMode
                fastModePacket.send(isFastMode)
                if (isFastMode) {
                    player.playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, 2.5f, 5f)
                    player.stepHeight = 3f
                } else {
                    player.stepHeight = 0.6f
                }
            }
        }
    }
}
