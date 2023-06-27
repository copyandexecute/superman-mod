package de.hglabor.superman.client.fly

import de.hglabor.superman.common.entity.isSupermanFlying
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.sound.MovingSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.MathHelper

class FlyingSoundInstance(private val player: ClientPlayerEntity) :
    MovingSoundInstance(SoundEvents.ITEM_ELYTRA_FLYING, SoundCategory.PLAYERS, SoundInstance.createRandom()) {
    private var tickCount = 0

    init {
        repeat = true
        repeatDelay = 0
        volume = 0.1f
    }

    override fun tick() {
        tickCount += 10
        if (!player.isRemoved && (tickCount <= MAX_TICKS || player.isSupermanFlying)) {
            x = player.x.toFloat().toDouble()
            y = player.y.toFloat().toDouble()
            z = player.z.toFloat().toDouble()
            val f = player.velocity.lengthSquared().toFloat()
            volume = if (f.toDouble() >= 1.0E-7) {
                MathHelper.clamp(f / 4.0f, 0.0f, 1.0f)
            } else {
                0.0f
            }
            if (tickCount < MAX_TICKS) {
                volume = 0.0f
            } else if (tickCount < MAX_TICKS * 2) {
                volume *= (tickCount - MAX_TICKS) / MAX_TICKS
            }
            val g = 0.8f
            pitch = if (volume > 0.8f) {
                1.0f + (volume - 0.8f)
            } else {
                1.0f
            }
        } else {
            setDone()
        }
    }

    companion object {
        const val MAX_TICKS = 20F
    }
}
