package de.hglabor.superman.client.registry

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.silkmc.silk.core.annotations.ExperimentalSilkApi
import net.silkmc.silk.core.event.Event
import org.lwjgl.glfw.GLFW

object KeyBindings {
    val laserKey = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.superman.laser", // The translation key of the keybinding's name
            InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
            GLFW.GLFW_KEY_R, // The keycode of the key
            "category.superman.abilities" // The translation key of the keybinding's category.
        )
    )
    val xrayKey = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.superman.xray", // The translation key of the keybinding's name
            InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
            GLFW.GLFW_KEY_X, // The keycode of the key
            "category.superman.abilities" // The translation key of the keybinding's category.
        )
    )

    open class KeyEvent(val key: Int, val scanCode: Int, val client: MinecraftClient)

    @OptIn(ExperimentalSilkApi::class)
    val onKeyPressedOnce = Event.onlySyncImmutable<KeyEvent>()
}
