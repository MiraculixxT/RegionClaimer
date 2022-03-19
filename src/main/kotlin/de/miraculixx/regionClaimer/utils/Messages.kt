package de.miraculixx.regionClaimer.utils

import de.miraculixx.regionClaimer.system.ConfigManager
import de.miraculixx.regionClaimer.system.Configs
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.ChatColor
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

fun getMessageList(key: String, inline: String? = null): List<String> {
    val config = ConfigManager.getConfig(Configs.LANGUAGE)
    val final = if (inline == null) {
        val l = ArrayList<String>()
        config.getStringList(key).forEach {
            l.add(it.replaceColor())
        }
        l
    } else {
        val l = ArrayList<String>()
        config.getStringList(key).forEach {
            l.add(inline + it.replaceColor())
        }
        l
    }

    if (final.isEmpty()) final.add("§c$key")
    return final
}

fun String.cropColor(): String {
    return ChatColor.stripColor(this)
}

fun String.replaceColor(): String {
    return ChatColor.translateAlternateColorCodes('&', this)
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