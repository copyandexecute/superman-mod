package de.hglabor.superman.client

import de.hglabor.superman.client.registry.KeyBindings
import de.hglabor.superman.common.entity.isSuperman
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.silkmc.silk.core.text.literalText
import net.silkmc.silk.core.world.pos.Pos2i


object AbilityRenderer : HudRenderCallback {
    enum class Abilities(val keybinding: KeyBinding, val description: String) {
        LASER(KeyBindings.laserKey, "Lasereyes"),
        FLY(MinecraftClient.getInstance().options.jumpKey, "Fly"),
        FASTMODE(MinecraftClient.getInstance().options.sneakKey, "Fastmode"),
        XRAY(KeyBindings.xrayKey,"Xray"),
    }

    fun init() {
        HudRenderCallback.EVENT.register(this)
    }

    override fun onHudRender(drawContext: DrawContext, tickDelta: Float) {
        if (MinecraftClient.getInstance().player?.isSuperman != true) return
        val offset = 2
        Abilities.values().forEachIndexed { index, ability ->
            val text = literalText {
                text(ability.keybinding.boundKeyLocalizedText) { color = 0xfff200 }
                text(" - ") { color = 0x919191 }
                text(ability.description)
            }
            val pos = Pos2i(5, 5 + (text.height + offset * 2) * index)
            drawContext.fill(
                RenderLayer.getGuiOverlay(),
                pos.x - offset,
                pos.z - offset,
                pos.x + text.width + offset,
                pos.z + text.height + offset,
                -1873784752
            )
            drawContext.drawText(
                MinecraftClient.getInstance().textRenderer,
                text,
                pos.x,
                pos.z,
                14737632,
                true
            )
        }
    }

    val Text.width
        get() = MinecraftClient.getInstance().textRenderer.getWidth(this)

    val Text.height
        get() = MinecraftClient.getInstance().textRenderer.fontHeight
}
