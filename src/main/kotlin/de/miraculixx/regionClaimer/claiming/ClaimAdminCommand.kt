package de.miraculixx.regionClaimer.claiming

import de.miraculixx.regionClaimer.system.ConfigManager
import de.miraculixx.regionClaimer.system.Configs
import de.miraculixx.regionClaimer.utils.mm
import net.axay.kspigot.runnables.task
import net.kyori.adventure.title.Title
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.time.Duration

class ClaimAdminCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            return false
        }

        when (args[0].lowercase()) {
            "reset" -> Configs.values().forEach {
                ConfigManager.reset(it)
            }
            "flex" -> {
                if (sender !is Player) return false
                var msg = "<rainbow>"
                task(false, 1, 1, 100) {
                    msg += "|"
                    sender.showTitle(Title.title(mm.deserialize(msg), mm.deserialize(msg), Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO)))
                    if (it.counterUp == 100L) {
                        blink(msg, sender)
                    }
                }
            }
            "flex2" -> {
                if (sender !is Player) return false
                val msg = "\n\n\n\n\n\n\n\n\n\n<rainbow:%%>sheeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeesh"
                task(false, 1, 1, 100) {
                    sender.sendMessage(mm.deserialize(msg.replace("%%", it.counterUp.toString())))
                }
            }
        }
        return true
    }

    private fun blink(msg: String, player: Player) {
        val message = msg.replace("<rainbow>", "<rainbow:%%>")
        task(false, 1, 1, 100) {
            val m = mm.deserialize(message.replace("%%", it.counterUp.toString()))
            player.showTitle(Title.title(m, m, Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO)))
        }
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        val list = ArrayList<String>()
        if (args.size < 2) {
            list.add("reset")
        }
        return list
    }
}