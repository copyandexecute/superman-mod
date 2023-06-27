package de.hglabor.superman.client.laser

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import de.hglabor.superman.Manager.toId
import de.hglabor.superman.client.registry.KeyBindings
import de.hglabor.superman.common.entity.isSuperman
import de.hglabor.superman.common.entity.isUsingLaserEyes
import de.hglabor.superman.common.network.laserEyePacket
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.option.Perspective
import net.minecraft.client.util.Window
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.silkmc.silk.core.annotations.ExperimentalSilkApi
import net.silkmc.silk.core.text.literal

object LaserManagerClient : WorldRenderEvents.End, ClientTickEvents.StartTick {
    private val VIGNETTE = "textures/red_vignette.png".toId()
    private var VIGNETTE_PROGRESS = 0.0f

    @OptIn(ExperimentalSilkApi::class)
    fun init() {
        WorldRenderEvents.END.register(this)
        ClientTickEvents.START_CLIENT_TICK.register(this)

        KeyBindings.onKeyPressedOnce.listen { event ->
            val player = MinecraftClient.getInstance().player ?: return@listen
            if (KeyBindings.laserKey.matchesKey(event.key, event.scanCode) && player.isSuperman) {
                laserEyePacket.send(!player.isUsingLaserEyes)
            }
        }
    }

    override fun onEnd(it: WorldRenderContext) {
        val client = MinecraftClient.getInstance()
        val player = client.player ?: return
        val matrices = it.matrixStack()
        val camera = it.camera()
        val tickDelta = it.tickDelta().toDouble()
        if (client.options.perspective === Perspective.FIRST_PERSON && player.isUsingLaserEyes) {
            val vec3d = camera.pos
            val cameraX = vec3d.x
            val cameraY = vec3d.y
            val cameraZ = vec3d.z
            val lerpX = MathHelper.lerp(tickDelta, player.lastRenderX, player.x)
            val lerpY = MathHelper.lerp(tickDelta, player.lastRenderY, player.y)
            val lerpZ = MathHelper.lerp(tickDelta, player.lastRenderZ, player.z)
            val x = lerpX - cameraX
            val y = lerpY - cameraY
            val z = lerpZ - cameraZ
            val off = client.entityRenderDispatcher.getRenderer(player).getPositionOffset(player, tickDelta.toFloat())
            val offX = x + off.x
            val offY = y + off.y
            val offZ = z + off.z
            matrices.push()
            matrices.translate(offX, offY, offZ)
            matrices.translate(0.0, -0.05, 0.0)
            LaserRenderer.renderLaserBeam(
                player,
                tickDelta.toFloat(),
                matrices,
                client.bufferBuilders.effectVertexConsumers,
                true
            )
            matrices.pop()
        }
    }

    fun renderOverlay(window: Window, drawContext: DrawContext, identifier: Identifier, f: Float) {
        RenderSystem.disableDepthTest()
        RenderSystem.depthMask(false)
        drawContext.setShaderColor(1.0f, 1.0f, 1.0f, f)
        drawContext.drawTexture(
            identifier,
            0,
            0,
            -90,
            0.0f,
            0.0f,
            window.scaledWidth,
            window.scaledHeight,
            window.scaledWidth,
            window.scaledHeight
        )
        RenderSystem.depthMask(true)
        RenderSystem.enableDepthTest()
        drawContext.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
    }

    fun onHudRender(drawContext: DrawContext, tickDelta: Float) {
        val client = MinecraftClient.getInstance()
        if (client.currentScreen == null && VIGNETTE_PROGRESS > 0.0) {
            renderOverlay(client.window, drawContext, VIGNETTE, VIGNETTE_PROGRESS)
        }
    }

    override fun onStartTick(client: MinecraftClient) {
        val player = client.player ?: return
        VIGNETTE_PROGRESS = if (player.isUsingLaserEyes) {
            1.0f.coerceAtMost(VIGNETTE_PROGRESS + 0.1f)
        } else {
            0.0f.coerceAtLeast(VIGNETTE_PROGRESS - 0.1f)
        }
    }
}
