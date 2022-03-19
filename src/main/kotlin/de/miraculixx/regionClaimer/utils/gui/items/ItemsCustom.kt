@file:Suppress("DEPRECATION")

package de.miraculixx.regionClaimer.utils.gui.items

import net.axay.kspigot.items.customModel
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class ItemsCustom {

    fun getItems(int: Int): Map<ItemStack, Int> {
        return when(int) {
            1 -> g1()
            else -> mapOf(Pair(ItemStack(Material.BARRIER), 4))
        }
    }


    private fun g1(): Map<ItemStack, Int> {
        return mapOf(
            Pair(
                itemStack(Material.STRUCTURE_VOID) { meta {
                    customModel = 1
                    name = "§9§lRegion löschen"
                    lore = listOf("§7Löscht die vollständige Region", "§7samt aller Einstellungen"," ","§cACHTUNG - Dies ist nicht umkehrbar!")
                }}, 10
            ),
            Pair(
                itemStack(Material.CRAFTING_TABLE) { meta {
                    customModel = 2
                    name = "§9§lErweiterte Einstellungen"
                    lore = listOf("§7Greife auf erweiterte Einstellungen", "§7dieser Region zu, um sie", "§7dir perfekt einzurichten")
                }}, 11
            ),
            Pair(
                itemStack(Material.SPYGLASS) { meta {
                    customModel = 3
                    name = "§9§lInformationen"
                    lore = listOf("§7Klicke hier um nützliche","§7Informationen zu erhalten")
                }}, 13
            ),
            Pair(
                itemStack(Material.TURTLE_HELMET) { meta {
                    customModel = 4
                    name = "§9§lMitglieder Liste"
                    lore = listOf("§7Greife auf alle Mitglieder", "§7dieser Region zu.","§7Füge weitere hinzu oder", "§7entferne bereits vorhandene")
                    addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                }}, 15
            ),
            Pair(
                itemStack(Material.DIAMOND_HELMET) { meta {
                    customModel = 5
                    name = "§9§lBesitzer Liste"
                    lore = listOf("§7Greife auf alle Besitzer", "§7dieser Region zu.", "§7Füge weitere hinzu oder", "§7entferne bereits vorhandene"," ", "§cACHTUNG - Alle Besitzer sind gleichgestellt")
                    addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                }}, 16
            )
        )
    }
}