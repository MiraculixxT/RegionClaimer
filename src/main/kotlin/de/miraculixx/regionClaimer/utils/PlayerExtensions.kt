package de.miraculixx.regionClaimer.utils

import org.bukkit.Sound
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.*

fun Player.error() {
    this.playSound(this.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f ,1.1f)
}

fun Player.click() {
    this.playSound(this.location, Sound.UI_BUTTON_CLICK, 0.8f, 1f)
}

fun Long.toDate(): String {
    return try {
        val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm")
        val netDate = Date(this)
        sdf.format(netDate)
    } catch (e: Exception) {
        e.toString()
    }
}