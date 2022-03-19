package de.miraculixx.regionClaimer.utils.gui

import de.miraculixx.regionClaimer.utils.mm
import net.kyori.adventure.text.Component

enum class GUITypes(t: Component, i: Int) {
    MODIFY_MEMBER(mm.deserialize("<color:#45b9bf>Region • Members</color>"), 6),
    MODIFY_OWNER(mm.deserialize("<color:#45b9bf>Region • Owner</color>"), 6),

    REGION_FLAGS(mm.deserialize("<color:#45b9bf>Region • Owner</color>"), 4),
    REGION_MODIFY(mm.deserialize("<color:#45b9bf>Region Einstellungen</color>"), 3);

    val title = t
    val size = i
}