package de.hglabor.superman.client

import dev.kosmx.playerAnim.api.layered.IAnimation
import dev.kosmx.playerAnim.api.layered.ModifierLayer

interface IAnimatedPlayer {
    fun superman_getModAnimation(): ModifierLayer<IAnimation>
}
