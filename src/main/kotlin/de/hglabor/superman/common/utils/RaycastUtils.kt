package de.hglabor.superman.common.utils

import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import java.util.function.Predicate

/**
 * Credits to [Source](https://github.com/vini2003/Maven)
 */
object RaycastUtils {
    fun raycastEntity(source: PlayerEntity, delta: Float, maxDist: Float): Entity? {
        val pos = source.getCameraPosVec(delta)
        val rot = source.getCameraPosVec(1.0f)
        val end = pos.add(rot.x * maxDist.toDouble(), rot.y * maxDist.toDouble(), rot.z * maxDist.toDouble())
        val sourceBox = source.boundingBox.stretch(rot.multiply(maxDist.toDouble())).expand(2.5, 2.5, 2.5)
        val entityHitResult = raycastEntity(
            source,
            pos,
            end,
            sourceBox,
            { entity: Entity -> !entity.isSpectator && entity.canHit() },
            (maxDist * maxDist).toDouble()
        )
        return entityHitResult?.entity
    }

    fun raycastEntity(
        source: PlayerEntity,
        pos: Vec3d,
        end: Vec3d?,
        box: Box?,
        predicate: Predicate<Entity>?,
        sqrMaxDist: Double
    ): EntityHitResult? {
        val world = source.world
        var distToGo = sqrMaxDist
        var target: Entity? = null
        var targetPos: Vec3d? = null
        for (entity in world.getOtherEntities(source, box, predicate)) {
            val entityBox = entity.boundingBox.expand(entity.targetingMargin.toDouble())
            val optPos = entityBox.raycast(pos, end)
            if (entityBox.contains(pos)) {
                if (distToGo >= 0.0) {
                    target = entity
                    targetPos = optPos.orElse(pos)
                    distToGo = 0.0
                }
            } else if (optPos.isPresent) {
                val entityPos = optPos.get()
                val sqrDist = pos.squaredDistanceTo(entityPos)
                if (sqrDist < distToGo || distToGo == 0.0) {
                    if (entity.rootVehicle === source.rootVehicle) {
                        if (distToGo == 0.0) {
                            target = entity
                            targetPos = entityPos
                        }
                    } else {
                        target = entity
                        targetPos = entityPos
                        distToGo = sqrDist
                    }
                }
            }
        }
        return target?.let { EntityHitResult(it, targetPos) }
    }
}
