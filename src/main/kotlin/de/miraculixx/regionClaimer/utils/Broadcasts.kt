package de.miraculixx.regionClaimer.utils

import org.bukkit.Bukkit

fun consoleMessage(vararg string: String) {
    string.forEach {
        Bukkit.getConsoleSender().sendMessage(it)
    }
}

fun consoleWarn(vararg string: String) {
    val log = Bukkit.getLogger()
    string.forEach {
        log.warning(it)
    }
}
