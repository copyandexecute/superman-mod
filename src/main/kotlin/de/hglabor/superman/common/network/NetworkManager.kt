package de.hglabor.superman.common.network

import de.hglabor.superman.Manager.toId
import de.hglabor.superman.common.entity.isFastMode
import de.hglabor.superman.common.entity.isSupermanFlying
import de.hglabor.superman.common.entity.isUsingLaserEyes
import de.hglabor.superman.common.entity.isXraying
import kotlinx.serialization.ExperimentalSerializationApi
import net.minecraft.entity.attribute.EntityAttributes
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.network.packet.c2sPacket

@OptIn(ExperimentalSerializationApi::class)
val laserEyePacket = c2sPacket<Boolean>("lasereye".toId())

@OptIn(ExperimentalSerializationApi::class)
val xrayTogglePacket = c2sPacket<Boolean>("xray_toggle".toId())

@OptIn(ExperimentalSerializationApi::class)
val flyPacket = c2sPacket<Boolean>("fly".toId())

@OptIn(ExperimentalSerializationApi::class)
val fastModePacket = c2sPacket<Boolean>("fastmode".toId())

object NetworkManager {
    fun init() {
        laserEyePacket.receiveOnServer { packet, context -> context.player.isUsingLaserEyes = packet }
        xrayTogglePacket.receiveOnServer { packet, context -> context.player.isXraying = packet }
        flyPacket.receiveOnServer { packet, context -> context.player.isSupermanFlying = packet }
        fastModePacket.receiveOnServer { packet, context ->
            context.player.isFastMode = packet
            if (context.player.isFastMode) {
                context.player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)?.baseValue = 1.0
                context.player.stepHeight = 3.0f
            } else {
                context.player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)?.baseValue = 0.10000000149011612
                context.player.stepHeight = 1.0f
            }
        }
    }
}
