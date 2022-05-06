package de.miraculixx.regionClaimer.utils.gui.items

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import de.miraculixx.regionClaimer.utils.consoleMessage
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.lang.reflect.Field
import java.util.*

fun skullTexture(meta: SkullMeta, base64: String): SkullMeta {
    val profile = GameProfile(UUID.randomUUID(), "")
    profile.properties.put("textures", Property("textures", base64))
    val profileField: Field?
    try {
        profileField = meta.javaClass.getDeclaredField("profile")
        profileField.isAccessible = true
        profileField[meta] = profile
    } catch (e: Exception) {
        e.printStackTrace()
        consoleMessage("§cHead Builder failed to apply Base64 Code to Skull!")
        consoleMessage("§cCode: §7$base64")
    }
    return meta
}

class ItemLib {

    fun getPlayers(int: Int, region: ProtectedRegion): List<ItemStack> {
        return ItemsPlayer().getItems(int, region)
    }

    fun getCustom(int: Int): Map<ItemStack, Int> {
        return ItemsCustom().getItems(int)
    }

    fun getScroll(int: Int, set: ProtectedRegion): LinkedHashMap<ItemStack, Boolean> {
        return ItemsScroll().getItems(int, set)
    }
}