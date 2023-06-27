package de.hglabor.superman.common.utils

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

val Vec3d.blockPos get() = BlockPos(x.toInt(), y.toInt(), z.toInt())