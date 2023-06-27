package de.hglabor.superman.common.laser

import de.hglabor.superman.common.entity.isUsingLaserEyes
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.block.Blocks
import net.minecraft.block.TntBlock
import net.minecraft.entity.Entity
import net.minecraft.entity.mob.CreeperEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.silkmc.silk.core.text.literal
import java.util.*
import java.util.function.Predicate

object LaserManagerCommon : ServerTickEvents.EndWorldTick {
    fun init() {
        ServerTickEvents.END_WORLD_TICK.register(this)
    }

    fun raycastEntity(entity: Entity, i: Int): Optional<Entity> {
        return run {
            val vec3d = entity.eyePos
            val vec3d2 = entity.getRotationVec(1.0f).multiply(i.toDouble())
            val vec3d3 = vec3d.add(vec3d2)
            val box = entity.boundingBox.stretch(vec3d2).expand(1.0)
            val j = i * i
            val predicate =
                Predicate { entityx: Entity -> !entityx.isSpectator && entityx.canHit() }
            val entityHitResult = ProjectileUtil.raycast(entity, vec3d, vec3d3, box, predicate, j.toDouble())
            if (entityHitResult == null) {
                Optional.empty()
            } else {
                if (vec3d.squaredDistanceTo(entityHitResult.pos) > j.toDouble()) Optional.empty() else Optional.of(
                    entityHitResult.entity
                )
            }
        }
    }

    private fun handleLaserBreakEffects(world: ServerWorld, pos: Vec3d, target: Entity?) {
        if (target != null) {
            world.playSound(
                null,
                pos.x,
                pos.y,
                pos.z,
                SoundEvents.ENTITY_GENERIC_EXPLODE,
                SoundCategory.PLAYERS,
                0.25f,
                2.5f
            )
        }
        world.spawnParticles(
            ParticleTypes.SMOKE,
            pos.x,
            pos.y + ((target?.height ?: 2.0f) / 2.0),
            pos.z,
            if (target != null) 100 else 20,
            0.0,
            0.0,
            0.0,
            0.05
        )
    }

    override fun onEndTick(world: ServerWorld) {
        world.getPlayers { it.isUsingLaserEyes }.forEach {
            val blockRaycast = it.raycast(200.0, 1f, false)
            val entityRaycast = raycastEntity(it, 200)
            world.playSound(
                null, it.x, it.eyeY, it.z, SoundEvents.ENTITY_BEE_POLLINATE, SoundCategory.PLAYERS, 1.0f, 0.5f
            )
            world.playSound(
                null, it.x, it.eyeY, it.z, SoundEvents.ENTITY_BEE_POLLINATE, SoundCategory.PLAYERS, 0.5f, 2.5f
            )
            if (entityRaycast.isPresent) {
                val target = entityRaycast.get()
                target.damage(it.damageSources.onFire(), 8.0f)
                target.setOnFireFor(20)
                handleLaserBreakEffects(world, target.pos, target)
                if (target is CreeperEntity) {
                    target.fuseSpeed = 120000
                }
            } else if (blockRaycast is BlockHitResult) {
                val blockState = world.getBlockState(blockRaycast.blockPos)
                if (!blockState.isAir) {
                    if (blockState.isOf(Blocks.TNT)) {
                        TntBlock.primeTnt(world, blockRaycast.blockPos)
                        world.setBlockState(blockRaycast.blockPos, Blocks.AIR.defaultState, 3)
                        handleLaserBreakEffects(world, blockRaycast.blockPos.toCenterPos(), null)
                    } else if (blockState.isOf(Blocks.FIRE)) {
                        val block = blockRaycast.blockPos.offset(blockRaycast.side.opposite)
                        world.breakBlock(block, true)
                    } else {
                        world.breakBlock(blockRaycast.blockPos, true)
                        world.setBlockState(blockRaycast.blockPos, Blocks.FIRE.defaultState)
                        handleLaserBreakEffects(world, blockRaycast.blockPos.toCenterPos(), null)
                    }
                }
            }
        }
    }

    val BlockPos.vec3d get() = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())
}
