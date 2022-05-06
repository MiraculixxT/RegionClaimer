package de.miraculixx.regionClaimer.utils

import de.miraculixx.regionClaimer.system.ConfigManager
import de.miraculixx.regionClaimer.system.Configs
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Player

fun getMessage(key: String, player: Player? = null, input: String? = null, input2: String? = null, pre: Boolean = true): Component {
    val config = ConfigManager.getConfig(Configs.LANGUAGE)
    val msg = config.getString(key)
    var final = msg ?: key
    val component = if (pre) prefix else Component.text("")

    if (player != null) final = final.replace("%PLAYER%", player.displayName)
    if (input != null) final = final.replace("%INPUT%", input)
    if (input2 != null) final = final.replace("%INPUT-2%", input2)

    return component.append(mm.deserialize(final))
}

fun MutableList<String>.addLines(vararg lines: String): MutableList<String> {
    lines.forEach { this.add(it) }
    return this
}

fun MutableList<String>.addLines(vararg lines: List<String>): MutableList<String> {
    lines.forEach { list ->
        list.forEach {
            this.add(it)
        }
    }
    return this
}

fun Location.fancy(): String {
    return "${this.blockX} ${this.blockY} ${this.blockZ}"
}

fun Player.clearChat() {
    val message = StringBuilder("\n ")
    repeat(30) {
        message.append("\n ")
    }
    player?.sendMessage(Component.text(message.toString()))
}