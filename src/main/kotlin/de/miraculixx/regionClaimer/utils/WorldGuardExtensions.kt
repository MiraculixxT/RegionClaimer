package de.miraculixx.regionClaimer.utils

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import org.bukkit.Location
import org.bukkit.entity.Player

fun Player.ownRegion(location: Location, owner: Boolean): Boolean {
    if (hasPermission("rgclaim.bypass.owner")) return true

    val container = WorldGuard.getInstance().platform.regionContainer
    val regionManager = container[BukkitAdapter.adapt(location.world)]
    val lPlayer = WorldGuardPlugin.inst().wrapPlayer(player)
    val regions = regionManager?.getApplicableRegions(BlockVector3.at(location.x, location.y, location.z))?.regions ?: return false
    if (regions.isEmpty()) return false
    regions.forEach {
        return if (owner) it.isOwner(lPlayer) else it.isMember(lPlayer)
    }
    return true
}

fun Location.getRegion(): ProtectedRegion? {
    val container = WorldGuard.getInstance().platform.regionContainer
    val regionManager = container[BukkitAdapter.adapt(world)]
    val regions = regionManager?.getApplicableRegions(BlockVector3.at(x,y,z))?.regions
    return if (regions?.isEmpty() == true) null
    else regions?.first()
}