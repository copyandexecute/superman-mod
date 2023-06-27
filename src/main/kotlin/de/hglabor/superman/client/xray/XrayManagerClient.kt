package de.hglabor.superman.client.xray

import de.hglabor.superman.Manager.toId
import de.hglabor.superman.client.laser.LaserManagerClient
import de.hglabor.superman.client.registry.KeyBindings
import de.hglabor.superman.common.entity.isSuperman
import de.hglabor.superman.common.entity.isUsingLaserEyes
import de.hglabor.superman.common.entity.isXraying
import de.hglabor.superman.common.laser.LaserManagerCommon.raycastEntity
import de.hglabor.superman.common.network.xrayTogglePacket
import de.hglabor.superman.common.utils.blockPos
import de.hglabor.superman.common.xray.XrayManagerCommon
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderDispatcher
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.text.StringVisitable
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.silkmc.silk.core.annotations.ExperimentalSilkApi
import net.silkmc.silk.core.entity.directionVector
import net.silkmc.silk.core.text.literalText
import org.joml.Matrix4f
import java.text.DecimalFormat
import java.util.*
import java.util.function.Predicate
import kotlin.jvm.optionals.getOrNull


object XrayManagerClient : ClientTickEvents.EndTick, ClientTickEvents.StartTick {
    val xrayedBlockList = mutableMapOf<BlockPos, Pair<BlockState, Long>>()
    var xrayDistance = 32
    private val VIGNETTE = "textures/xray_vignette.png".toId()
    private var VIGNETTE_PROGRESS = 0.0f

    @OptIn(ExperimentalSilkApi::class)
    fun init() {
        KeyBindings.onKeyPressedOnce.listen { event ->
            val player = MinecraftClient.getInstance().player ?: return@listen
            if (KeyBindings.xrayKey.matchesKey(event.key, event.scanCode)  && player.isSuperman) {
                xrayTogglePacket.send(!player.isXraying)
            }
        }
        ClientTickEvents.END_CLIENT_TICK.register(this)
        ClientTickEvents.START_CLIENT_TICK.register(this)
    }


    override fun onStartTick(client: MinecraftClient) {
        val player = client.player ?: return
        VIGNETTE_PROGRESS = if (player.isXraying) {
            1.0f.coerceAtMost(VIGNETTE_PROGRESS + 0.1f)
        } else {
            0.0f.coerceAtLeast(VIGNETTE_PROGRESS - 0.1f)
        }
    }

    fun afterMouseScrollEvent(
        l: Double,
        d: Double,
        direction: Double,
        eventDeltaWheel: Double,
    ) {
        if (MinecraftClient.getInstance().player?.isXraying == true) {
            xrayDistance = MathHelper.clamp(xrayDistance + direction.toInt() * 5, 8, 164)
        }
    }

    fun renderXrayInformation(
        entity: LivingEntity,
        tickDelta: Float,
        unnamed: Float,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int,
        textRenderer: TextRenderer,
        dispatcher: EntityRenderDispatcher
    ) {
        val bl = !entity.isSneaky
        val f = entity.nameLabelHeight
        matrixStack.push()
        matrixStack.translate(0.0f, f, 0.0f)
        matrixStack.multiply(dispatcher.rotation)
        matrixStack.scale(-0.025f, -0.025f, 0.025f)
        val matrix4f: Matrix4f = matrixStack.peek().positionMatrix
        val g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25f)
        val k = (g * 255.0f).toInt() shl 24
        val j = 0
        val text = literalText {
            text(DecimalFormat("#.##").format(entity.health))
            text(" ❤") { color = 0xfc0303 }
        }
        val h = (-textRenderer.getWidth(text as StringVisitable?) / 2).toFloat()
        textRenderer.draw(
            text,
            h,
            j.toFloat(),
            553648127,
            false,
            matrix4f,
            vertexConsumerProvider,
            if (bl) TextRenderer.TextLayerType.SEE_THROUGH else TextRenderer.TextLayerType.NORMAL,
            k,
            i
        )
        if (bl) {
            textRenderer.draw(
                text,
                h,
                j.toFloat(),
                -1,
                false,
                matrix4f,
                vertexConsumerProvider,
                TextRenderer.TextLayerType.NORMAL,
                0,
                i
            )
        }
        matrixStack.pop()
    }

    fun isXrayingAtEntity(toRender: Entity): Boolean {
        val player = MinecraftClient.getInstance().player ?: return false
        if (player.isXraying) {
            val entity = raycastEntity(player, 64).getOrNull() ?: return false
            return entity.uuid == toRender.uuid
        }
        return false
    }

    fun onHudRender(drawContext: DrawContext, tickDelta: Float) {
        val client = MinecraftClient.getInstance()
        if (client.currentScreen == null && VIGNETTE_PROGRESS > 0.0) {
            LaserManagerClient.renderOverlay(
                client.window,
                drawContext,
                VIGNETTE,
                VIGNETTE_PROGRESS
            )
        }
    }

    override fun onEndTick(client: MinecraftClient) {
        val player = client.player ?: return
        val world = player.world
        val newXrayBlocks = mutableSetOf<BlockPos>()
        val xrayBlock = Blocks.LIME_STAINED_GLASS
        val blocksToByPass = Predicate<BlockState> {
            if (it.isAir) return@Predicate true
            if (it.isOf(Blocks.DEEPSLATE_DIAMOND_ORE)) return@Predicate true
            if (it.isOf(Blocks.BEDROCK)) return@Predicate true
            if (it.isOf(Blocks.SPAWNER)) return@Predicate true
            if (it.isOf(Blocks.END_PORTAL_FRAME)) return@Predicate true
            if (it.isOf(Blocks.CHEST)) return@Predicate true
            if (it.isOf(Blocks.TRAPPED_CHEST)) return@Predicate true
            if (it.isOf(Blocks.ENDER_CHEST)) return@Predicate true
            return@Predicate false
        }
        if (player.isXraying) {
            player.sendMessage(literalText {
                color = 0x00ff44
                text(Text.translatable("hint.superman.xray"))
                text(": ")
                text("$xrayDistance")
            },true)
            player.playSound(SoundEvents.BLOCK_BEACON_ACTIVATE, 0.18f, 3.5f)
            val directionVector = player.directionVector
            buildSet {
                repeat(xrayDistance) { iteration ->
                    addAll(
                        XrayManagerCommon.generateSphere(
                            player.eyePos.add(
                                directionVector.normalize().multiply(iteration.toDouble())
                            ).blockPos, 5
                        )
                    )
                }
            }.forEach {
                val blockState = world.getBlockState(it)
                if (!blocksToByPass.test(blockState) && blockState.isFullCube(world, it)) {
                    val element = Pair(blockState, System.currentTimeMillis())
                    if (!blockState.isOf(xrayBlock)) {
                        xrayedBlockList[it] = element
                    }
                    newXrayBlocks.add(it)
                    world.setBlockState(it, xrayBlock.defaultState)
                }
            }
        }
        val toRemove = mutableSetOf<BlockPos>()
        xrayedBlockList.forEach { (pos, pair) ->
            if (pair.second + 1000L < System.currentTimeMillis() && !newXrayBlocks.contains(pos)) {
                toRemove += pos
                world.setBlockState(pos, pair.first)
            }
        }
        toRemove.forEach(xrayedBlockList::remove)
    }
}
