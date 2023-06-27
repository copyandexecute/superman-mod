package de.hglabor.superman.client.laser

import com.mojang.blaze3d.systems.RenderSystem
import de.hglabor.superman.Manager.toId
import de.hglabor.superman.common.entity.isUsingLaserEyes
import de.hglabor.superman.common.utils.RaycastUtils
import net.minecraft.client.render.*
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d
import org.joml.Matrix3f
import org.joml.Matrix4f
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sin

/**
 * Credits to [Source](https://github.com/vini2003/Maven)
 */
object LaserRenderer {
    private val LASER_BEAM_TEXTURE = "textures/laser_eyes.png".toId()

    private val LASER_LAYER = Util.memoize { identifier: Identifier ->
        val multiPhaseParameters = MultiPhaseParameters.builder()
            .program(RenderPhase.POSITION_COLOR_TEXTURE_LIGHTMAP_PROGRAM)
            .texture(RenderPhase.Texture(identifier, false, false))
            .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
            .writeMaskState(RenderPhase.COLOR_MASK)
            .cull(RenderPhase.DISABLE_CULLING)
            .build(false)
        RenderLayer.of(
            "laserlayer_${identifier.toUnderscoreSeparatedString()}",
            VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS,
            256,
            false,
            false,
            multiPhaseParameters
        )
    }

    private fun lerpPosition(entity: Entity, yOffset: Double, delta: Float): Vec3d {
        return Vec3d(entity.x, entity.y + yOffset, entity.z)
    }

    fun renderLaserBeam(
        player: PlayerEntity,
        delta: Float,
        matrices: MatrixStack,
        consumerProvider: VertexConsumerProvider,
        firstPerson: Boolean
    ) {
        if (!player.isUsingLaserEyes) return
        val layer = LASER_LAYER.apply(LASER_BEAM_TEXTURE)
        val raycast = player.raycast(256.0, 0.0f, true)
        val targetPos = raycast.pos
        val progress = 0.9f + sin((player.age.toFloat() / 5.0f).toDouble()).toFloat() / 10.0f
        val eyeHeight = player.getEyeHeight(player.pose)
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapProgram)
        matrices.push()
        matrices.translate(0.0, eyeHeight.toDouble(), 0.0)
        val lerpPlayerPos = lerpPosition(player, eyeHeight.toDouble(), delta)
        var lerpPos = targetPos.subtract(lerpPlayerPos)
        var distance: Double = lerpPos.length()
        val found = RaycastUtils.raycastEntity(player, delta, 64.0f)
        if (found != null) {
            distance = found.pos.subtract(lerpPlayerPos).length()
        }
        lerpPos = lerpPos.normalize()
        val acosLerpY = acos(lerpPos.y).toFloat()
        val atan2LerpXZ = atan2(lerpPos.z, lerpPos.x).toFloat() //TODO notice
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((1.5707964f - atan2LerpXZ) * 57.295776f))
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(acosLerpY * 57.295776f))
        if (!firstPerson) {
            if (player.pitch <= 0.0f) {
                matrices.translate(0.0, 0.0, (player.pitch / 450.0f).toDouble())
            } else {
                matrices.translate(0.0, 0.0, (-player.pitch / 450.0f).toDouble())
            }
        }
        val colorProgress = progress * progress
        val r = 64 + (colorProgress * 191.0f).toInt()
        val g = 32 + (colorProgress * 191.0f).toInt()
        val b = 128 - (colorProgress * 64.0f).toInt()
        val consumer = consumerProvider.getBuffer(layer)
        val peek = matrices.peek()
        val positionMatrix = peek.positionMatrix
        val normalMatrix = peek.normalMatrix
        val end = distance.toFloat()
        val scale = (1.0 + sin((player.age.toFloat() / 32.0f).toDouble()) / 10.0).toFloat()
        matrices.scale(scale, 1.0f, scale)
        matrices.translate(0.07500000298023224, 0.0, -0.02500000037252903)
        renderQuad(consumer, positionMatrix, normalMatrix, 0.0f, 0.0f, 0.0f, 0.11f, end, 0.0f, r, g, b)
        renderQuad(consumer, positionMatrix, normalMatrix, 0.0f, 0.0f, 0.08f, 0.11f, end, 0.0f, r, g, b)
        renderQuad(consumer, positionMatrix, normalMatrix, 0.0f, 0.0f, 0.0f, 0.0f, end, 0.08f, r, g, b)
        renderQuad(consumer, positionMatrix, normalMatrix, 0.11f, 0.0f, 0.0f, 0.0f, end, 0.08f, r, g, b)
        matrices.translate(-0.25, -0.0, 0.0)
        renderQuad(consumer, positionMatrix, normalMatrix, 0.0f, 0.0f, 0.0f, 0.11f, end, 0.0f, r, g, b)
        renderQuad(consumer, positionMatrix, normalMatrix, 0.0f, 0.0f, 0.08f, 0.11f, end, 0.0f, r, g, b)
        renderQuad(consumer, positionMatrix, normalMatrix, 0.0f, 0.0f, 0.0f, 0.0f, end, 0.08f, r, g, b)
        renderQuad(consumer, positionMatrix, normalMatrix, 0.11f, 0.0f, 0.0f, 0.0f, end, 0.08f, r, g, b)
        matrices.pop()
        return
    }

    private fun renderQuad(
        consumer: VertexConsumer,
        positionMatrix: Matrix4f,
        normalMatrix: Matrix3f,
        startX: Float,
        startY: Float,
        startZ: Float,
        width: Float,
        height: Float,
        depth: Float,
        r: Int,
        g: Int,
        b: Int
    ) {
        vertex(consumer, positionMatrix, normalMatrix, startX, startY, startZ, r, g, b)
        vertex(consumer, positionMatrix, normalMatrix, startX, startY + height, startZ, r, g, b)
        vertex(consumer, positionMatrix, normalMatrix, startX + width, startY + height, startZ + depth, r, g, b)
        vertex(consumer, positionMatrix, normalMatrix, startX + width, startY, startZ + depth, r, g, b)
    }

    private fun vertex(
        consumer: VertexConsumer,
        positionMatrix: Matrix4f,
        normalMatrix: Matrix3f,
        x: Float,
        y: Float,
        z: Float,
        r: Int,
        g: Int,
        b: Int
    ) {
        consumer.vertex(positionMatrix, x, y, z)
            .color(r, g, b, 150)
            .texture(1.0f, 1.0f)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(0x00f000f0)
            .normal(normalMatrix, 0.0f, 1.0f, 0.0f)
            .next()
    }
}
