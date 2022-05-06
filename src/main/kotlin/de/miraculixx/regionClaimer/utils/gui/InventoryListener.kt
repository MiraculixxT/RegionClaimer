@file:Suppress("unused")

package de.miraculixx.regionClaimer.utils.gui

import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import de.miraculixx.regionClaimer.utils.*
import net.axay.kspigot.event.listen
import net.axay.kspigot.items.customModel
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.meta.SkullMeta

object InventoryListener {

    private val onClick = listen<InventoryClickEvent> {
        if (it.whoClicked !is Player) return@listen
        val player = it.whoClicked as Player
        val item = it.currentItem ?: return@listen
        if (!item.hasItemMeta()) return@listen
        if (!item.itemMeta.hasCustomModelData()) return@listen
        val title = it.view.title()
        val titles = ArrayList<Component>()
        GUITypes.values().forEach { type ->
            titles.add(type.title)
        }
        if (!titles.contains(title)) return@listen
        val loc = player.location

        it.isCancelled = true
        when (val id = item.itemMeta.customModel) {
            200 -> {
                if (title == GUITypes.REGION_MODIFY.title) return@listen
                player.click()
                GUIBuilder(GUITypes.REGION_MODIFY, player).custom().open()
            }

            1 -> if (player.ownRegion(loc, true)) {
                player.sendMessage(getMessage("command.claim.confirm"))
                player.closeInventory()
                player.error()
            } else player.noOwner()
            2 -> if (player.ownRegion(loc, true)) {
                GUIBuilder(GUITypes.REGION_FLAGS, player).scroll(0, player.location.getRegion() ?: return@listen).open()

                player.click()
            } else player.noOwner()
            3 -> {
                player.performCommand("claim info")
                player.playSound(player, Sound.BLOCK_END_PORTAL_FRAME_FILL, 1f, 1.1f)
            }
            4 -> if (player.ownRegion(loc, false)) {
                GUIBuilder(GUITypes.MODIFY_MEMBER, player).userList(loc.getRegion() ?: return@listen).open()
                player.click()
            } else player.noOwner()
            5 -> if (player.ownRegion(loc, false)) {
                GUIBuilder(GUITypes.MODIFY_OWNER, player).userList(loc.getRegion() ?: return@listen).open()
                player.click()
            } else player.noOwner()

            301 -> {
                player.closeInventory()
                player.sendMessage(
                    getMessage("event.modify.addUser")
                )
                player.playSound(player, Sound.BLOCK_END_PORTAL_FRAME_FILL, 1f, 1.1f)
            }

            10, 11 -> {
                if (!player.ownRegion(player.location, true)) {
                    player.noOwner()
                    return@listen
                }
                val meta = item.itemMeta as SkullMeta
                val skullOwner = meta.owningPlayer
                if (skullOwner == null) {
                    player.sendMessage(getMessage("event.modify.noPlayer"))
                    return@listen
                }
                val uuid = skullOwner.uniqueId
                val region = player.location.getRegion()
                val member = id == 10
                if (member) {
                    if (region?.members?.contains(uuid) == true) {
                        region.members.removePlayer(uuid)
                        GUIBuilder(GUITypes.MODIFY_MEMBER, player).userList(region).open()
                    } else {
                        player.sendMessage(getMessage("event.modify.notMember"))
                        return@listen
                    }
                } else {
                    if (region?.owners?.contains(uuid) == true) {
                        region.owners.removePlayer(uuid)
                        GUIBuilder(GUITypes.MODIFY_OWNER, player).userList(region).open()
                    } else {
                        player.sendMessage(getMessage("event.modify.notOwner", player, skullOwner.name))
                        return@listen
                    }
                }
                player.sendMessage(getMessage("event.modify.remove"))
            }

            101 -> if (player.ownRegion(loc, false)) {
                toggleFlag(player, Flags.HEALTH_REGEN, player.location.getRegion())
            } else player.noOwner()
            102 -> if (player.ownRegion(loc, false)) {
                toggleFlag(player, Flags.CHEST_ACCESS, player.location.getRegion(), true, true)
            } else player.noOwner()
            103 -> if (player.ownRegion(loc, false)) {
                toggleFlag(player, Flags.INTERACT, player.location.getRegion())
            } else player.noOwner()
            104 -> if (player.ownRegion(loc, false)) {
                val region = player.location.getRegion()
                toggleFlag(player, Flags.CREEPER_EXPLOSION, region)
                toggleFlag(player, Flags.OTHER_EXPLOSION, region, false)
                toggleFlag(player, Flags.TNT, region, false)
                toggleFlag(player, Flags.GHAST_FIREBALL, region, false)
            } else player.noOwner()
            else -> return@listen
        }
    }

    private fun Player.noOwner() {
        closeInventory()
        error()
        sendMessage(getMessage("command.claim.noOwner"))
    }

    private fun toggleFlag(player: Player, flags: StateFlag, region: ProtectedRegion?, sound: Boolean = true, remove: Boolean = false) {
        if (region == null) {
            player.error()
            return
        }
        if (region.getFlag(flags) == StateFlag.State.ALLOW) {
            //Deaktivieren
            if (remove) region.setFlag(flags, null)
            else region.setFlag(flags, StateFlag.State.DENY)
            if (sound) player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.4f)
        } else {
            //Aktivieren
            region.setFlag(flags, StateFlag.State.ALLOW)
            if (sound) player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f)
        }
        GUIBuilder(GUITypes.REGION_FLAGS, player).scroll(0, region).open()
    }

    /*
    Chest lurker
     */
    private val onChestClick = listen<PlayerInteractEvent>(priority = EventPriority.NORMAL) {
        if (it.clickedBlock?.type != Material.CHEST) return@listen
        if (it.action != Action.RIGHT_CLICK_BLOCK) return@listen
        if (it.isCancelled) return@listen
        val block = it.clickedBlock?.state as Chest
        val originalInv = block.inventory
        val inv = Bukkit.createInventory(null, originalInv.size, Component.text("Chest").color(defaultBlue))
        for ((i, itemStack) in originalInv.withIndex()) {
            if (itemStack != null)
                inv.setItem(i, itemStack)
        }
        it.player.openInventory(inv)
    }

    private val onChestItemClick = listen<InventoryClickEvent>(priority = EventPriority.HIGH) {
        if (it.view.title() == Component.text("Chest").color(defaultBlue))
            it.isCancelled = true
    }
}