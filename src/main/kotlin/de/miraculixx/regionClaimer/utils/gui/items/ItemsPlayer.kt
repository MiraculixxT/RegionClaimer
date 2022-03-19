@file:Suppress("DEPRECATION")

package de.miraculixx.regionClaimer.utils.gui.items

import com.sk89q.worldguard.protection.regions.ProtectedRegion
import de.miraculixx.regionClaimer.utils.addLines
import de.miraculixx.regionClaimer.utils.toDate
import net.axay.kspigot.items.customModel
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class ItemsPlayer {

    fun getItems(int: Int, region: ProtectedRegion): List<ItemStack> {
        return when (int) {
            1 -> g1(region)
            2 -> g2(region)
            else -> listOf(ItemStack(Material.BARRIER))
        }
        /*
        1 -> Member List
        2 -> Owner List
         */
    }


    private fun g1(region: ProtectedRegion): List<ItemStack> {
        val list = ArrayList<ItemStack>()
        list.add(itemStack(Material.TURTLE_HELMET) { meta {
            customModel = 200
            name = "§9§lMitglieder Liste"
            lore = listOf("§7Greife auf alle Besitzer", "§7dieser Region zu.","§7Füge weitere hinzu oder", "§7entferne bereits vorhandene")
            addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        }})
        val dLore = listOf(" ", "§9Klicken ≫ §7Mitglied entfernen")
        region.members.uniqueIds.forEach { uuid ->
            list.add(
                itemStack(Material.PLAYER_HEAD) { meta<SkullMeta> {
                    customModel = 10
                    owningPlayer = Bukkit.getOfflinePlayer(uuid)
                    name = "§9§l${owningPlayer?.name ?: uuid}"
                    lore = ArrayList<String>().addLines("§9Zuletzt gesehen ≫ §7${owningPlayer?.lastSeen?.toDate() ?: "nie"}")
                        .addLines(dLore)
                }}
            )
        }
        return list
    }

    private fun g2(region: ProtectedRegion): List<ItemStack> {
        val list = ArrayList<ItemStack>()
        list.add(itemStack(Material.DIAMOND_HELMET) { meta {
            customModel = 200
            name = "§9§lBesitzer Liste"
            lore = listOf("§7Greife auf alle Besitzer", "§7dieser Region zu.", "§7Füge weitere hinzu oder", "§7entferne bereits vorhandene"," ", "§cACHTUNG - Alle Besitzer sind gleichgestellt")
            addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        }})
        val dLore = listOf(" ", "§9Klicken ≫ §7Besitzer entfernen")
        region.owners.uniqueIds.forEach { uuid ->
            list.add(
                itemStack(Material.PLAYER_HEAD) { meta<SkullMeta> {
                    customModel = 11
                    owningPlayer = Bukkit.getOfflinePlayer(uuid)
                    name = "§9§l${owningPlayer?.name ?: uuid}"
                    lore = ArrayList<String>().addLines("§9Zuletzt gesehen ≫ §7${owningPlayer?.lastSeen?.toDate() ?: "nie"}")
                        .addLines(dLore)
                }}
            )
        }
        return list
    }
}