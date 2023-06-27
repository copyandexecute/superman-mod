package de.hglabor.superman.common.entity

import net.minecraft.entity.Entity
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandler
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.player.PlayerEntity

interface Superman {
    fun getFlyingLeaningPitch(tickDelta: Float): Float
}

val laserEyesTracker: TrackedData<Boolean> =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
val flyTracker: TrackedData<Boolean> =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
val xrayTracker: TrackedData<Boolean> =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
val xrayModeTracker: TrackedData<Byte> =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BYTE)
val fastModeTracker: TrackedData<Boolean> =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
val supermanTracker: TrackedData<Boolean> =
    DataTracker.registerData(PlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)

var PlayerEntity.isUsingLaserEyes: Boolean
    get() = this.dataTracker.get(laserEyesTracker) && isSuperman
    set(value) = this.dataTracker.set(laserEyesTracker, value)

var PlayerEntity.isSupermanFlying: Boolean
    get() = this.dataTracker.get(flyTracker) && isSuperman
    set(value) = this.dataTracker.set(flyTracker, value)

var PlayerEntity.isFastMode: Boolean
    get() = this.dataTracker.get(fastModeTracker) && isSuperman
    set(value) = this.dataTracker.set(fastModeTracker, value)

var PlayerEntity.isXraying: Boolean
    get() = this.dataTracker.get(xrayTracker) && isSuperman
    set(value) = this.dataTracker.set(xrayTracker, value)

var PlayerEntity.isSuperman: Boolean
    get() = this.dataTracker.get(supermanTracker)
    set(value) = this.dataTracker.set(supermanTracker, value)

var PlayerEntity.xrayMode: Byte
    get() = this.dataTracker.get(xrayModeTracker)
    set(value) = this.dataTracker.set(xrayModeTracker, value)

