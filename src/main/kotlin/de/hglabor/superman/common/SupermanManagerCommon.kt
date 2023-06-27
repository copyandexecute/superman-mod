package de.hglabor.superman.common

import de.hglabor.superman.common.entity.isSuperman
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.world.ServerWorld

object SupermanManagerCommon : ServerTickEvents.EndWorldTick {
    fun init() {
        ServerTickEvents.END_WORLD_TICK.register(this)
    }

    override fun onEndTick(world: ServerWorld) {
        world.players.filter { it.isSuperman }.forEach {
            it.hungerManager.foodLevel = 40
            it.hungerManager.saturationLevel = 40f
        }
    }
}
