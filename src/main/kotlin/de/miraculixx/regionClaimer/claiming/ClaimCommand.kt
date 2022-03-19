package de.miraculixx.regionClaimer.claiming

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import de.miraculixx.regionClaimer.system.ConfigManager
import de.miraculixx.regionClaimer.system.Configs
import de.miraculixx.regionClaimer.utils.*
import de.miraculixx.regionClaimer.utils.gui.GUIBuilder
import de.miraculixx.regionClaimer.utils.gui.GUITypes
import net.axay.kspigot.extensions.onlinePlayers
import net.axay.kspigot.runnables.taskRunLater
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.entity.Shulker
import java.util.*

class ClaimCommand : CommandExecutor, TabCompleter {

    private val claims = HashMap<UUID, CreateClaim>()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("$prefix Only Players can perform this command!")
            return false
        }
        if (args.isEmpty()) {
            sender.sendMessage(getMessage("command.claim.help"))
            return false
        }

        when (args[0].lowercase()) {
            "info" -> {
                val container = WorldGuard.getInstance().platform.regionContainer
                val regionManager = container[BukkitAdapter.adapt(sender.world)]
                val loc = sender.location
                val regions = regionManager?.getApplicableRegions(BlockVector3.at(loc.x, loc.y, loc.z))?.regions ?: return false
                val lPlayer = WorldGuardPlugin.inst().wrapPlayer(sender)
                if (regions.isEmpty()) {
                    sender.sendMessage(getMessage("command.claim.noRegion"))
                    return false
                }
                regions.forEach { rg ->
                    if (rg.isOwner(lPlayer) || rg.isMember(lPlayer) || sender.hasPermission("rgclaim.bypass.info")) {
                        val message = Component.text("\n").toBuilder()
                        message.append(mm.deserialize("<color:#3b702a><st>                   </st>[ <color:#87ff60>Region</color> ]<st>                   </st></color>"))
                        message.append(mm.deserialize("\n<color:#509739>Region Name</color> <color:#adadad>≫</color> <color:#86BF73><hover:show_text:'<color:#D14545><b>Modify Region</b></color><newline><color:#BB6262>Der Regionsname kann nicht<newline>verändert werden</color>'>${rg.id}</hover></color>\n"))
                        val pos1 = rg.maximumPoint
                        val pos2 = rg.minimumPoint
                        val world = sender.world
                        highlight(pos1, world)
                        highlight(pos2, world)
                        message.append(mm.deserialize("<color:#509739>Position <color:#adadad>≫</color> <color:#86BF73><hover:show_text:'<#D14545><b>Modify Region</b><newline><#BB6262>Die Regionsposition kann nicht<newline>verändert werden'>${pos1.blockX} ${pos1.blockZ} <-> ${pos2.blockX} ${pos2.blockZ}</hover></color>\n"))
                        message.append(mm.deserialize("<color:#509739>Besitzer</color> <color:#adadad>≫</color> <color:#86BF73>"))
                        rg.owners.playerDomain.uniqueIds.forEach { uuid ->
                            val target = Bukkit.getOfflinePlayer(uuid)
                            message.append(mm.deserialize("<color:#86BF73><hover:show_text:'<color:#509739><b>Modify Region</b></color><newline><color:#86bf73>Klicke hier um die Besitzer<newline>zu modifizieren</color>'><click:run_command:'/claim modify'>${target.name} </click></hover></color>"))
                        }
                        message.append(mm.deserialize("\n<color:#509739>Mitglieder</color> <color:#adadad>≫</color> <color:#86BF73>"))
                        val members = rg.members.playerDomain.uniqueIds
                        if (members.isEmpty()) {
                            message.append(mm.deserialize("<hover:show_text:'<color:#509739><b>Modify Region</b></color><newline><color:#86bf73>Klicke hier um die Mitglieder<newline>zu modifizieren</color>'><click:run_command:'/claim modify'><i>Keine</i></click></hover>"))
                        } else {
                            members.forEach { uuid ->
                                val target = Bukkit.getOfflinePlayer(uuid)
                                message.append(mm.deserialize("<color:#86BF73><hover:show_text:'<color:#509739><b>Modify Region</b></color><newline><color:#86bf73>Klicke hier um die Mitglieder<newline>zu modifizieren</color>'><click:run_command:'/claim modify'>${target.name} </click></hover></color>"))
                            }
                        }
                        sender.sendMessage(message.asComponent())
                    } else {
                        sender.sendMessage(getMessage("command.claim.noOwner"))
                        return false
                    }
                }

            }
            "new" -> {
                val container = WorldGuard.getInstance().platform.regionContainer
                val regionManager = container[BukkitAdapter.adapt(sender.world)]
                val count = regionManager?.getRegionCountOfPlayer(WorldGuardPlugin.inst().wrapPlayer(sender)) ?: 0
                val c = ConfigManager.getConfig(Configs.SETTINGS)
                if (count >= c.getInt("Max Region Amount"))
                    sender.sendMessage(getMessage("command.claim.toManyRegions"))
                else CreateClaim(sender)
            }
            "modify" -> {
                val container = WorldGuard.getInstance().platform.regionContainer
                val regionManager = container[BukkitAdapter.adapt(sender.world)]
                val lPlayer = WorldGuardPlugin.inst().wrapPlayer(sender)
                val loc = lPlayer.location
                val regions = regionManager?.getApplicableRegions(BlockVector3.at(loc.x, loc.y, loc.z))?.regions ?: return false
                if (regions.isEmpty()) {
                    sender.sendMessage(getMessage("command.claim.noRegion"))
                    return false
                }
                regions.forEach { rg ->
                    if (rg.isOwner(lPlayer) || sender.hasPermission("rgclaim.bypass.modify")) {
                        GUIBuilder(GUITypes.REGION_MODIFY, sender).custom().open()
                        sender.playSound(sender, Sound.BLOCK_ENDER_CHEST_OPEN, 1f, 1f)
                    } else {
                        sender.sendMessage(getMessage("command.claim.noOwner"))
                        return false
                    }
                }
            }
            "cancel" -> {
                stopCurrent(sender)
            }
            "retry" -> {
                stopCurrent(sender)
                sender.sendMessage("\n \n \n \n \n ")
                CreateClaim(sender)
            }
            "adduser" -> {
                if (sender.ownRegion(sender.location, true)) {
                    if (args.size < 3) {
                        sender.sendMessage(getMessage("event.modify.addUser"))
                        sender.error()
                        return false
                    }
                    // Deprecated usage of getPlayer for user-friendly access
                    val target = Bukkit.getOfflinePlayer(args[1])
                    val region = sender.location.getRegion()
                    if (region == null || !target.hasPlayedBefore()) {
                        sender.sendMessage(getMessage("event.modify.addError"))
                        sender.error()
                        return false
                    }
                    when (args[2].lowercase()) {
                        "owner" -> {
                            if (region.owners.contains(target.uniqueId)) {
                                sender.sendMessage(getMessage("event.modify.alreadyAdded", sender, target.name, "Besitzer"))
                                sender.error()
                                return false
                            }
                            if (region.members.contains(target.uniqueId)) region.members.removePlayer(target.uniqueId)
                            region.owners.addPlayer(target.uniqueId)
                        }
                        "member" -> {
                            if (region.members.contains(target.uniqueId) || region.owners.contains(target.uniqueId)) {
                                sender.sendMessage(getMessage("event.modify.alreadyAdded", sender, target.name, "Mitglied"))
                                sender.error()
                                return false
                            }
                            region.members.addPlayer(target.uniqueId)
                        }
                        else -> {
                            sender.sendMessage(getMessage("event.modify.addUser"))
                            sender.error()
                            return false
                        }
                    }
                    sender.sendMessage(getMessage("event.modify.addSuccess", sender, target.name, args[2]))
                } else sender.sendMessage(getMessage("command.claim.noOwner"))
            }
            "ireallywanttodeletethisregion" -> {
                if (sender.ownRegion(sender.location, true)) {
                    val container = WorldGuard.getInstance().platform.regionContainer
                    val regionManager = container[BukkitAdapter.adapt(sender.world)]
                    regionManager?.removeRegion(sender.location.getRegion()?.id)
                    sender.sendMessage(getMessage("command.claim.delete"))
                    sender.playSound(sender, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1.2f)
                } else sender.sendMessage(getMessage("command.claim.noOwner"))
            }
            else -> sender.sendMessage(getMessage("command.claim.help"))
        }
        return true
    }

    private fun stopCurrent(player: Player) {
        val id = player.uniqueId
        if (!claims.contains(id)) {
            player.sendMessage(getMessage("command.claim.noCurrentTry"))
            return
        }
        val obj = claims[id]
        obj?.finish()
    }

    private fun highlight(bv3: BlockVector3, world: World) {
        val loc = world.getHighestBlockAt(bv3.x, bv3.z).location.clone().subtract(0.0,1.0,0.0)
        val shulker = world.spawn(loc, Shulker::class.java)
        shulker.isGlowing = true
        shulker.setAI(false)
        shulker.isInvisible = true
        shulker.isInvulnerable = true
        shulker.teleport(loc.add(0.0,1.0,0.0))
        taskRunLater(30) {
            shulker.remove()
        }
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        // claim addPlayer <Player> owner/member
        return if (args.size < 2)
            mutableListOf("new", "modify", "info")
        else if (args[0].lowercase() == "adduser") {
            when (args.size) {
                2 -> {
                    val list = ArrayList<String>()
                    onlinePlayers.forEach { pl ->
                        list.add(pl.name)
                    }
                    list
                }
                3 -> mutableListOf("member", "owner")
                else -> mutableListOf()
            }
        } else mutableListOf()
    }
}