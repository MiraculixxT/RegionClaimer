package de.miraculixx.regionClaimer.system

import de.miraculixx.regionClaimer.Main
import de.miraculixx.regionClaimer.utils.consoleMessage
import de.miraculixx.regionClaimer.utils.prefix
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File


class Config(private val name: String) {

    private lateinit var configFile: File
    private lateinit var config: FileConfiguration
    private val plugin = Main.plugin

    init {
        load(false)
    }

    private fun load(reset: Boolean) {
        configFile = File(plugin.dataFolder, "$name.yml")
        if (!configFile.exists() || reset) {
            configFile.parentFile.mkdirs()
            plugin.saveResource("$name.yml", true)
            configFile.createNewFile()
        }
        config = YamlConfiguration()
        try {
            config.load(configFile)
        } catch (e: Exception) {
            e.printStackTrace()
            consoleMessage("$prefix §c$name.yml Config failed to load! ^^ Reason above ^^")
            consoleMessage("$prefix §cCopy and Save your §nlatest.log§c to get Support!")
        }
    }

    fun getConfig(): FileConfiguration {
        return config
    }

    fun save() {
        config.save(configFile)
    }

    fun reset() {
        configFile.delete()
        configFile.deleteOnExit()
        load(true)
    }

    fun reload(): FileConfiguration {
        load(false)
        return config
    }
}