package dev.tehsteel.tctf.util.bossbar

import net.kyori.adventure.bossbar.BossBar
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask


interface ProgressBossBarTickCallback {
	fun onTick(seconds: Int, players: Collection<Player>, bossBar: BossBar, task: BukkitTask)
}