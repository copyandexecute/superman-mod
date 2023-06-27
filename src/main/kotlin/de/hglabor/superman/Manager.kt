package de.hglabor.superman

import de.hglabor.superman.client.AbilityRenderer
import de.hglabor.superman.client.fastmode.FastModeManagerClient
import de.hglabor.superman.client.fly.FlyManagerClient
import de.hglabor.superman.client.laser.LaserManagerClient
import de.hglabor.superman.client.laser.LaserRenderer
import de.hglabor.superman.client.registry.KeyBindings
import de.hglabor.superman.client.xray.XrayManagerClient
import de.hglabor.superman.common.SupermanTransformItem
import de.hglabor.superman.common.SupermanManagerCommon
import de.hglabor.superman.common.laser.LaserManagerCommon
import de.hglabor.superman.common.network.NetworkManager
import de.hglabor.superman.common.xray.XrayManagerCommon
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.util.Rarity
import org.apache.logging.log4j.LogManager


val logger = LogManager.getLogger("superman")

object Manager : ModInitializer, DedicatedServerModInitializer, ClientModInitializer {
    val SUPERMAN_TRANSFORM_ITEM = Registry.register(
        Registries.ITEM, "superman_transform".toId(),
        SupermanTransformItem(FabricItemSettings().fireproof().rarity(Rarity.EPIC).maxCount(1))
    )
    var BRUH_SOUND = Registry.register(Registries.SOUND_EVENT, "bruh".toId(), SoundEvent.of("bruh".toId()))
    val supermanSkin = "textures/superman_skin.png".toId()

    override fun onInitialize() {
        NetworkManager.init()
        LaserManagerCommon.init()
        XrayManagerCommon.init()
        SupermanManagerCommon.init()
    }

    override fun onInitializeClient() {
        KeyBindings
        LaserRenderer
        AbilityRenderer.init()
        LaserManagerClient.init()
        XrayManagerClient.init()
        FlyManagerClient.init()
        FastModeManagerClient.init()
    }

    override fun onInitializeServer() {
    }

    fun String.toId() = Identifier("superman", this)
}
