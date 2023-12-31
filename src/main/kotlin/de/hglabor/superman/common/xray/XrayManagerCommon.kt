package de.hglabor.superman.common.xray

import net.minecraft.util.math.BlockPos

object XrayManagerCommon {
    fun init() {
    }

    fun generateSphere(centerBlock: BlockPos, radius: Int, hollow: Boolean = false): List<BlockPos> {
        val circleBlocks: MutableList<BlockPos> = ArrayList()
        val bx: Int = centerBlock.x
        val by: Int = centerBlock.y
        val bz: Int = centerBlock.z
        for (x in bx - radius..bx + radius) {
            for (y in by - radius..by + radius) {
                for (z in bz - radius..bz + radius) {
                    val distance = ((bx - x) * (bx - x) + (bz - z) * (bz - z) + (by - y) * (by - y)).toDouble()
                    if (distance < radius * radius && !(hollow && distance < (radius - 1) * (radius - 1))) {
                        circleBlocks.add(BlockPos(x, y, z))
                    }
                }
            }
        }
        return circleBlocks
    }
}
