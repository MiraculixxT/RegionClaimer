@file:Suppress("DEPRECATION")

package de.miraculixx.regionClaimer.utils.gui.items

import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import de.miraculixx.regionClaimer.utils.addLines
import net.axay.kspigot.items.customModel
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class ItemsScroll {

    fun getItems(int: Int, region: ProtectedRegion): LinkedHashMap<ItemStack, Boolean> {
        return when (int) {
            1 -> g1(region)
            else -> linkedMapOf(Pair(ItemStack(Material.BARRIER), false))
        }
    }

    private fun g1(region: ProtectedRegion): LinkedHashMap<ItemStack, Boolean> {
        val list = listOf(" ", "§9Klicken ≫ §7Toggle")
        return linkedMapOf(
            Pair(
                itemStack(Material.GOLDEN_APPLE) {
                    meta {
                        customModel = 101
                        name = "§9§lRegeneration"
                        lore = ArrayList<String>().addLines("§7Stelle ein, ob Spieler in", "§7dieser Region regenerieren können")
                            .addLines(list)
                    }
                }, region.getFlag(Flags.HEALTH_REGEN) == StateFlag.State.ALLOW
            ),
            Pair(
                itemStack(Material.CHEST) {
                    meta {
                        customModel = 102
                        name = "§9§lTruhen Zugriff"
                        lore = ArrayList<String>().addLines("§7Stelle ein, ob Spieler mit", "§7Truhen interagieren können")
                            .addLines(list)
                    }
                }, region.getFlag(Flags.CHEST_ACCESS) == StateFlag.State.ALLOW
            ),
            Pair(
                itemStack(Material.ENCHANTING_TABLE) {
                    meta {
                        customModel = 103
                        name = "§9§lInteraktion"
                        lore = ArrayList<String>().addLines("§7Stelle ein, ob Spieler mit", "§7dieser Region interagieren können", "§7(Allgemein, Truhen exkludiert)")
                            .addLines(list)
                    }
                }, region.getFlag(Flags.INTERACT) == StateFlag.State.ALLOW
            ),
            Pair(
                itemStack(Material.TNT) {
                    meta {
                        customModel = 104
                        name = "§9§lExplosionen"
                        lore = ArrayList<String>().addLines("§7Stelle ein, Explosionen in", "§7dieser Region Blockschaden machen")
                            .addLines(list)
                    }
                }, region.getFlag(Flags.CREEPER_EXPLOSION) == StateFlag.State.ALLOW
            )
        )
    }
}