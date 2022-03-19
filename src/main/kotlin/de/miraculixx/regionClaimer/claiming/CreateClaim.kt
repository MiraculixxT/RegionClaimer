package de.miraculixx.regionClaimer.claiming

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.managers.RegionManager
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import de.miraculixx.regionClaimer.Main
import de.miraculixx.regionClaimer.system.ConfigManager
import de.miraculixx.regionClaimer.system.Configs
import de.miraculixx.regionClaimer.utils.*
import io.papermc.paper.event.player.AsyncChatEvent
import net.axay.kspigot.event.listen
import net.axay.kspigot.event.unregister
import net.axay.kspigot.runnables.sync
import net.axay.kspigot.runnables.task
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Shulker
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.scoreboard.Team
import java.time.Duration
import java.util.regex.Pattern

class CreateClaim(private val player: Player) {
    //Config Data
    private val warner = getMessage("event.create.warning", pre = false)
    private val regionManager: RegionManager?
    private val blockCost: Double
    private val economy = Main.economy

    //Runtime Variables
    private var finished = false
    private var step = CreateStep.FIRST_POSITION
    private var cooldown = false

    //Data Collector
    private var pos1: Location? = null
    private var pos2: Location? = null
    private var shulker1: Shulker? = null
    private var shulker2: Shulker? = null
    private var cost: Double? = null
    private var name: String? = null

    init {
        val container = WorldGuard.getInstance().platform.regionContainer
        regionManager = container[BukkitAdapter.adapt(player.world)]
        val c = ConfigManager.getConfig(Configs.SETTINGS)
        val regionMultiplier = c.getDouble("Region Multiplier")
        blockCost = c.getDouble("Block Cost") * regionMultiplier

        player.sendMessage(getMessage("event.create.step1"))
        run()
    }

    /*
    Public Methods - Utilitys
     */
    fun finish() {
        if (finished) return
        finished = true
        sync {
            shulker1?.remove()
            shulker2?.remove()
        }
        onClick.unregister()
        onChat.unregister()
        onSneak.unregister()
    }

    /*
    Tasks and Displays
     */
    private fun run() {
        /*
        Text System
        Countdown and Step Guide
         */
        var countDown = 120
        Component.text("test").hoverEvent(HoverEvent.showText(Component.text("ad\n")))
        task(false, 1, 20) {
            if (!player.isOnline) {
                finished
            }
            if (finished) {
                it.cancel()
                return@task
            }
            if (countDown == 0) {
                finish()
                player.sendMessage(getMessage("event.create.timedOut"))
                it.cancel()
                return@task
            }
            val upper = mm.deserialize("<gradient:#4797C9:#77A6C3:#4797C9>${step.title}")
            val lower = warner
                .append(Component.text(countDown).color(TextColor.color(198, 93, 41)).decoration(TextDecoration.BOLD, true).toBuilder())
                .append(Component.text("s").color(TextColor.color(198, 93, 41)))
            val title = Title.title(upper, lower, Title.Times.times(Duration.ZERO, Duration.ofMillis(1500), Duration.ZERO))
            player.showTitle(title)
            if (economy != null && cost != null) {
                val y1 = TextColor.color(199, 166, 33)
                val y2 = TextColor.color(231, 194, 44)
                player.sendActionBar(
                    Component.text("Cost: ").color(y1)
                        .append(Component.text(cost!!).color(y2))
                        .append(Component.text(" | ").color(defaultGrey))
                        .append(Component.text("Money: ").color(y1))
                        .append(Component.text(economy.getBalance(player)).color(y2)).toBuilder()
                )
            }
            countDown--
        }

        /*
        Highlighter - Shulker
        Flashing Shulkers that highlight the
        corner Blocks
         */
        val gold = getTeam(NamedTextColor.GOLD)
        val yellow = getTeam(NamedTextColor.YELLOW)
        val lime = getTeam(NamedTextColor.GREEN)
        val green = getTeam(NamedTextColor.DARK_GREEN)
        task(true, 10, 10, countDown * 3L) {
            if (finished) {
                gold.unregister()
                yellow.unregister()
                lime.unregister()
                green.unregister()
                it.cancel()
                return@task
            }
            if ((it.counterUp?.rem(2)) == 0L) {
                if (shulker1 != null) gold.addEntity(shulker1!!)
                if (shulker2 != null) green.addEntity(shulker2!!)
            } else {
                if (shulker1 != null) yellow.addEntity(shulker1!!)
                if (shulker2 != null) lime.addEntity(shulker2!!)
            }
        }
    }

    private fun highlight(loc: Location, first: Boolean) {
        val shulker = loc.world.spawnEntity(loc.subtract(0.0, 1.0, 0.0), EntityType.SHULKER) as Shulker
        shulker.isInvulnerable = true
        shulker.isInvisible = true
        shulker.setAI(false)
        shulker.setGravity(false)
        shulker.isGlowing = true
        if (first) {
            shulker1?.remove()
            shulker1 = shulker
        } else {
            shulker2?.remove()
            shulker2 = shulker
        }
        shulker.teleport(loc.add(0.0, 1.0, 0.0))
    }

    private fun getTeam(color: NamedTextColor): Team {
        val manager = Bukkit.getScoreboardManager()
        val scores = manager.mainScoreboard
        val name = "${color.asHexString()}_${player.uniqueId}"
        var team = scores.getTeam(name)
        team?.unregister()

        team = scores.registerNewTeam(name)
        team.color(color)

        return team
    }

    private fun cooldown() {
        cooldown = true
        task(false, 20) {
            cooldown = false
            it.cancel()
        }
    }

    private fun updateBounders() {
        val x1 = pos1?.blockX ?: return
        val x2 = pos2?.blockX ?: return
        val z1 = pos1?.blockZ ?: return
        val z2 = pos2?.blockZ ?: return
        val xSize = if (x1 < x2) x2 - x1 else x1 - x2
        val zSize = if (z1 < z2) z2 - z1 else z1 - z2
        val size = (xSize + 1L) * (zSize + 1L)
        cost = blockCost * size
    }

    private fun setLoc(loc: Location, first: Boolean): Boolean {
        regionManager ?: return false
        val vec = BlockVector3.at(loc.x, loc.y, loc.z)
        if (regionManager.getApplicableRegions(vec).size() != 0) {
            player.error()
            return false
        }

        val ph = if (first) {
            if (loc == pos1) return false
            pos1 = loc
            player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_BELL, 1f, .8f)
            "Position 1"
        } else {
            if (loc == pos2) return false
            pos2 = loc
            player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_BELL, 1f, .7f)
            "Position 2"
        }
        highlight(loc, loc == pos1)
        updateStep()
        if (pos2 != null && pos1 != null)
            updateBounders()
        player.sendMessage(getMessage("event.create.newPos", player, ph, loc.fancy()))
        return true
    }

    private fun updateStep() {
        when (step) {
            CreateStep.NAME -> return
            CreateStep.FIRST_POSITION -> {
                if (pos2 == null) {
                    step = CreateStep.SECOND_POSITION
                    player.sendMessage(getMessage("event.create.step2"))
                } else if (pos1 != null) {
                    step = CreateStep.NAME
                    player.sendMessage(getMessage("event.create.step3"))
                }
            }
            CreateStep.SECOND_POSITION -> {
                step = CreateStep.NAME
                player.sendMessage(getMessage("event.create.step3"))
            }
            CreateStep.FINISH -> {}
        }
    }

    private fun createRegion(): Boolean {
        val container = WorldGuard.getInstance().platform.regionContainer
        val regionManager = container[BukkitAdapter.adapt(player.world)]
        if (regionManager == null) {
            consoleWarn("WorldGuard Region Adapter returns nothing... Maybe corrupted World?")
            player.sendMessage(getMessage("event.create.error"))
            return false
        }

        val regionNames = HashMap<String, ProtectedRegion>()
        val regions = regionManager.regions.values
        for (region in regions) {
            regionNames[region.id] = region
        }
        if (regionNames.containsKey(name)) {
            player.sendMessage(getMessage("event.create.nameInUse", player, name))
            return false
        }


        val world = player.world
        val bv1 = BlockVector3.at(pos1!!.x, world.minHeight.toDouble(), pos1!!.z)
        val bv2 = BlockVector3.at(pos2!!.x, 256.0, pos2!!.z)
        val region = ProtectedCuboidRegion(name, bv1, bv2)
        val overlaps = region.getIntersectingRegions(regions).size
        if (overlaps != 0) {
            player.sendMessage(getMessage("event.create.overlapping", player, overlaps.toString()))
            return false
        }
        if (economy != null && cost != null) {
            val response = economy.withdrawPlayer(player, world.name, cost!!)
            if (!response.transactionSuccess()) {
                player.sendMessage(getMessage("event.create.notEnoughMoney"))
                return false
            }
            region.priority = 2
            region.owners.addPlayer(player.uniqueId)
            regionManager.addRegion(region as ProtectedRegion)

            //Finish Message + Overview#
            val newBal = response.balance
            player.clearChat()
            player.sendMessage(getMessage("event.create.success", player, name, newBal.toString()))
            player.sendMessage(
                Component.text("Name ≫ ").color(defaultBlue)
                    .append(Component.text(name!!).color(defaultGrey))
                    .append(Component.text("\nPosition 1 ≫ ").color(defaultBlue))
                    .append(Component.text(pos1!!.fancy()).color(defaultGrey))
                    .append(Component.text("\nPosition 2 ≫ ").color(defaultBlue))
                    .append(Component.text(pos2!!.fancy()).color(defaultGrey))
                    .append(Component.text("\nGeld ≫ ").color(defaultBlue))
                    .append(Component.text("${newBal + (cost ?: 0.0)} -> $newBal").color(defaultGrey))
            )
            return true
        } else {
            player.clearChat()
            player.sendMessage(getMessage("messages.create.error"))
            finish()
            return false
        }
    }


    /*
    EVENTS
    Internal Events that manage region creation.
    Only Listen to claim owner
     */
    private val onClick = listen<PlayerInteractEvent> {
        if (it.player != player) return@listen
        val block = it.clickedBlock ?: return@listen
        if (!block.type.isSolid) return@listen
        it.isCancelled = true
        if (cooldown) return@listen
        cooldown()
        val action = it.action
        val loc = block.location
        if (!setLoc(loc, action.isLeftClick)) {
            player.sendMessage(getMessage("event.create.invalidPos", player, loc.fancy()))

        }
    }

    private val onChat = listen<AsyncChatEvent> {
        if (it.player != player) return@listen
        it.isCancelled = true
        val message = mm.serialize(it.message())
        if (message.lowercase() == "stop") {
            finish()
            player.sendMessage(getMessage("event.create.cancel"))
            return@listen
        }
        if (step != CreateStep.NAME && step != CreateStep.FINISH) return@listen
        if (message.length > 16) {
            player.sendMessage(getMessage("event.create.nameTooLong", player, message))
            return@listen
        }
        if (!Pattern.matches("[a-zA-Z1-9_-]+", message)) {
            player.sendMessage(getMessage("event.create.invalidChars", player, message))
            return@listen
        }
        player.sendMessage(getMessage("event.create.newName", player, message))
        name = message
        step = CreateStep.FINISH
    }

    private val onSneak = listen<PlayerToggleSneakEvent> {
        if (it.player != player) return@listen
        if (!it.player.isSneaking) return@listen
        if (step != CreateStep.FINISH) return@listen
        if (name == null || pos1 == null || pos2 == null) {
            player.sendMessage(getMessage("event.create.error"))
            return@listen
        }
        if (economy != null) {
            val money = economy.getBalance(player)
            if ((cost ?: 0.0) > money) {
                player.sendMessage(getMessage("event.create.notEnoughMoney", player, money.toString(), cost.toString()))
                return@listen
            }
        }
        if (createRegion()) {
            finish()
            player.playSound(player.location, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1f, 1.2f)
        }
        else player.error()
    }
}