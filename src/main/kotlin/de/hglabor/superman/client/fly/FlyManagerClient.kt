package de.hglabor.superman.client.fly

import de.hglabor.superman.Manager.toId
import de.hglabor.superman.client.IAnimatedPlayer
import de.hglabor.superman.client.registry.KeyBindings
import de.hglabor.superman.client.xray.XrayManagerClient
import de.hglabor.superman.common.entity.isFastMode
import de.hglabor.superman.common.entity.isSuperman
import de.hglabor.superman.common.entity.isSupermanFlying
import de.hglabor.superman.common.network.flyPacket
import de.hglabor.superman.common.utils.blockPos
import de.hglabor.superman.common.xray.XrayManagerCommon
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos
import net.silkmc.silk.core.annotations.ExperimentalSilkApi
import net.silkmc.silk.core.entity.directionVector


object FlyManagerClient : ClientTickEvents.StartTick {
    val cape = "textures/cape.png".toId()
    val noClipBlockList = mutableMapOf<BlockPos, Pair<BlockState, Long>>()
    private var abilityResyncCountdown = 0


    @OptIn(ExperimentalSilkApi::class)
    fun init() {
        ClientTickEvents.START_CLIENT_TICK.register(this)

        KeyBindings.onKeyPressedOnce.listen { event ->
            val player = MinecraftClient.getInstance().player ?: return@listen
            if (MinecraftClient.getInstance().options.jumpKey.matchesKey(
                    event.key,
                    event.scanCode
                ) && player.isSuperman
            ) {
                if (this.abilityResyncCountdown == 0) {
                    this.abilityResyncCountdown = 7
                } else {
                    val isFlying = !player.isSupermanFlying
                    flyPacket.send(isFlying)
                    val animationContainer = (player as IAnimatedPlayer).superman_getModAnimation()
                    if (isFlying) {
                        MinecraftClient.getInstance().soundManager.play(FlyingSoundInstance(player))
                        val flyAnimation = PlayerAnimationRegistry.getAnimation("flying".toId())
                            ?: error("No Fly animation ${"flying".toId()}")
                        animationContainer.animation = KeyframeAnimationPlayer(flyAnimation)
                    } else {
                        animationContainer.animation = null
                    }
                    this.abilityResyncCountdown = 0
                }
            }
        }
    }

    override fun onStartTick(client: MinecraftClient) {
        if (client.isPaused) return
        if (abilityResyncCountdown > 0) {
            abilityResyncCountdown--
        }
        val player = client.player ?: return
        val world = player.world
        val newNoClipBlocks = mutableSetOf<BlockPos>()
        val noClipBlockReplacement = Blocks.AIR
        if (player.isSupermanFlying && player.isFastMode) {
            var playSoundFlag = false
            XrayManagerCommon.generateSphere(
                player.pos.add(player.directionVector.normalize().multiply(5.0)).blockPos,
                6
            )
                .forEach {
                    val blockState = world.getBlockState(it)
                    if (!blockState.isAir) {
                        val element = Pair(blockState, System.currentTimeMillis())
                        XrayManagerClient.xrayedBlockList[it] = element
                        newNoClipBlocks.add(it)
                        world.setBlockState(it, noClipBlockReplacement.defaultState)
                        playSoundFlag = true
                    }
                }
            if (playSoundFlag) {
                player.playSound(SoundEvents.BLOCK_STONE_BREAK, 0.3f, 5f)
            }
        }
        val toRemove = mutableSetOf<BlockPos>()
        noClipBlockList.forEach { (pos, pair) ->
            if (pair.second < System.currentTimeMillis() && !newNoClipBlocks.contains(pos)) {
                toRemove += pos
                world.setBlockState(pos, pair.first)
            }
        }
        toRemove.forEach(noClipBlockList::remove)
    }
}
