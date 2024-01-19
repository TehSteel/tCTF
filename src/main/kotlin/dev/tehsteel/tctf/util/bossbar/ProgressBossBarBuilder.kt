package dev.tehsteel.tctf.util.bossbar

import dev.tehsteel.tctf.CaptureTheFlagPlugin
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask

class ProgressBossBarBuilder private constructor(private val title: String) {
	private var color: BossBar.Color? = null
	private var overlay: BossBar.Overlay? = null
	private var flags: Collection<BossBar.Flag>? = null
	private var seconds = 10
	private var players: Collection<Player>? = null
	private var onFinishCallback: ProgressBossBarCallback? = null
	private var progressBossBarTickCallback: ProgressBossBarTickCallback? = null

	fun withColor(color: BossBar.Color?): ProgressBossBarBuilder {
		this.color = color
		return this
	}

	fun withOverlay(overlay: BossBar.Overlay?): ProgressBossBarBuilder {
		this.overlay = overlay
		return this
	}

	fun withFlags(flags: Collection<BossBar.Flag>?): ProgressBossBarBuilder {
		this.flags = flags
		return this
	}

	fun withSeconds(seconds: Int): ProgressBossBarBuilder {
		this.seconds = seconds
		return this
	}

	fun withPlayers(players: Collection<Player>?): ProgressBossBarBuilder {
		this.players = players
		return this
	}

	fun withOnFinishCallback(onFinishCallback: ProgressBossBarCallback): ProgressBossBarBuilder {
		this.onFinishCallback = onFinishCallback
		return this
	}

	fun withBossBarTickCallback(progressBossBarTickCallback: ProgressBossBarTickCallback?): ProgressBossBarBuilder {
		this.progressBossBarTickCallback = progressBossBarTickCallback
		return this
	}

	fun run() {
		val maxSeconds = seconds
		val bossBar: BossBar = BossBar.bossBar(
			MiniMessage.miniMessage().deserialize(title.replace("%seconds%", seconds.toString())),
			0F,
			color ?: BossBar.Color.RED,
			overlay ?: BossBar.Overlay.PROGRESS
		)

		flags?.takeIf { it.isNotEmpty() }?.let { bossBar.addFlags(it) }

		if (players.isNullOrEmpty()) return
		players!!.forEach { bossBar.addViewer(it as Audience) }

		color?.let { bossBar.color(it) }
		overlay?.let { bossBar.overlay(it) }
		bossBar.progress(seconds.toFloat() / maxSeconds)

		Bukkit.getScheduler().runTaskTimer(CaptureTheFlagPlugin.getInstance(), { bukkitTask: BukkitTask ->
			seconds--
			progressBossBarTickCallback?.onTick(seconds, players!!, bossBar, bukkitTask)
			bossBar.name(
				MiniMessage.miniMessage().deserialize(title.replace("%seconds%", seconds.toString()))
			)
			bossBar.progress(seconds.toFloat() / maxSeconds)
			if (seconds < 1) {
				bossBar.viewers().forEach { bossBar.removeViewer(it as Audience) }
				onFinishCallback?.onFinish()
				bukkitTask.cancel()
			}
		}, 20, 20)
	}

	companion object {
		fun of(title: String): ProgressBossBarBuilder {
			return ProgressBossBarBuilder(title)
		}
	}
}