package de.miraculixx.regionClaimer

import de.miraculixx.regionClaimer.claiming.ClaimAdminCommand
import de.miraculixx.regionClaimer.claiming.ClaimCommand
import de.miraculixx.regionClaimer.utils.*
import de.miraculixx.regionClaimer.utils.gui.InventoryListener
import net.axay.kspigot.main.KSpigot
import net.milkbowl.vault.economy.Economy

class Main : KSpigot() {
    companion object {
        lateinit var plugin: KSpigot
        var economy: Economy? = null
    }

    override fun startup() {
        plugin = this
        if (!setupEconomy())
            consoleWarn(c2s.serialize(prefix) + "§cVault konnte nicht geladen werden! Alle Gebiete werden kostenlos")
        else consoleMessage(c2s.serialize(prefix) + "Vault erfolgreich geladen! Nutze vorhandenes Economy System zum claimen")

        // Global Commands
        getCommand("claim")?.setExecutor(ClaimCommand())
        getCommand("claim")?.tabCompleter = ClaimCommand()
        getCommand("adminclaim")?.setExecutor(ClaimAdminCommand())
        getCommand("adminclaim")?.tabCompleter = ClaimAdminCommand()

        // Global Listener
        InventoryListener
    }

    override fun shutdown() {

    }

    private fun setupEconomy(): Boolean {
        server.pluginManager.getPlugin("Vault") ?: return false
        val rsp = server.servicesManager.getRegistration(Economy::class.java) ?: return false
        economy = rsp.provider
        return true
    }
}
