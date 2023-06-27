package de.hglabor.superman.common

import de.hglabor.superman.Manager
import de.hglabor.superman.common.entity.isSuperman
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class SupermanTransformItem(settings: Settings) : Item(settings) {
    override fun use(world: World, player: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val itemStack = player.getStackInHand(hand)
        world.playSound(
            null,
            player.blockPos,
            SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED,
            SoundCategory.PLAYERS,
            1f,
            4.5f
        )
        world.playSound(
            null,
            player.blockPos,
            Manager.BRUH_SOUND,
            SoundCategory.PLAYERS,
            1f,
            1f
        )
        if (!world.isClient) {
            player.isSuperman = !player.isSuperman
        }
        return TypedActionResult.success(itemStack, world.isClient())
    }
}
