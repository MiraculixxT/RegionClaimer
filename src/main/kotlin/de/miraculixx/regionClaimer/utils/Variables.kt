package de.miraculixx.regionClaimer.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

val defaultGrey = TextColor.color(160,160,160)
val defaultBlue = TextColor.color(36,134,196)
val prefix = Component.text("").color(defaultGrey)
    .append(Component.text("[").decoration(TextDecoration.BOLD, true))
    .append(Component.text("RG-Claimer").color(defaultBlue))
    .append(Component.text("]").color(defaultGrey).decoration(TextDecoration.BOLD, true))
    .append(Component.text(" ").decoration(TextDecoration.BOLD, false))
val mm = MiniMessage.miniMessage()
val c2s = PlainTextComponentSerializer.plainText()