package dev.tehsteel.tctf.game.listener

import dev.tehsteel.minigameapi.api.game.GameCountdownEvent
import dev.tehsteel.minigameapi.api.game.GameEndEvent
import dev.tehsteel.minigameapi.api.game.GameStartEvent
import dev.tehsteel.tctf.CaptureTheFlagPlugin
import dev.tehsteel.tctf.game.model.CTFGame
import dev.tehsteel.tctf.game.model.GameFlagState
import dev.tehsteel.tctf.game.model.GamePlayer
import dev.tehsteel.tctf.game.model.GamePlayerState
import dev.tehsteel.tctf.util.bossbar.ProgressBossBarBuilder
import dev.tehsteel.tctf.util.bossbar.ProgressBossBarCallback
import dev.tehsteel.tctf.util.bossbar.ProgressBossBarTickCallback
import net.kyori.adventure.bossbar.BossBar
import org.bukkit.*
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitTask


class GameListener : Listener {


	@EventHandler
	fun onGameCountdownEvent(event: GameCountdownEvent) {
		if (event.game !is CTFGame) return
		val game: CTFGame = event.game as CTFGame

		ProgressBossBarBuilder
			.of("Game will start in %seconds%")
			.withSeconds(5)
			.withPlayers(game.players)
			.withOverlay(BossBar.Overlay.PROGRESS)
			.withBossBarTickCallback(object : ProgressBossBarTickCallback {
				override fun onTick(seconds: Int, players: Collection<Player>, bossBar: BossBar, task: BukkitTask) {
					players.forEach { player -> player.playSound(player, Sound.BLOCK_BELL_USE, 0.3F, 0.5F) }
					game.countdown--
				}
			})
			.withOnFinishCallback(object : ProgressBossBarCallback {
				override fun onFinish() {
					game.startGame(event.isForceStart)
				}
			}).run()
	}

	@EventHandler
	fun onGameStartEvent(event: GameStartEvent) {
		if (event.game !is CTFGame) return
		val game: CTFGame = event.game as CTFGame

		setFlagLocation(game.redFlag.location, Material.RED_BANNER)
		setFlagLocation(game.blueFlag.location, Material.BLUE_BANNER)

		setTeamSpawnAndMessage(game.redTeamMap.values, game.redFlag.location)
		setTeamSpawnAndMessage(game.blueTeamMap.values, game.blueFlag.location)
	}


	@EventHandler
	fun onGameEndEvent(event: GameEndEvent) {
		if (event.game !is CTFGame) return
		val game = event.game as CTFGame

		if (event.isForceStopped) {
			game.resetGame()
			return
		}


		val winningTeamColor = if (game.redFlag.state == GameFlagState.BASE_CAPTURED) Color.BLUE else Color.RED
		val winningTeam =
			if (winningTeamColor == Color.RED) game.redTeamMap.values.map { it.player } else game.blueTeamMap.values.map { it.player }

		val winningTeamMessage = if (winningTeamColor == Color.RED) "Red team won!" else "Blue team won!"
		game.players.forEach { it.sendMessage(winningTeamMessage) }

		launchFireworks(winningTeam.toList(), winningTeamColor)

		Bukkit.getScheduler().runTaskLater(CaptureTheFlagPlugin.getInstance(), Runnable {
			game.resetGame()
		}, 10 * 20)
	}


	private fun setFlagLocation(location: Location, material: Material) {
		location.block.type = material
	}

	private fun setTeamSpawnAndMessage(players: Collection<GamePlayer>, spawnLocation: Location) {
		players.forEach { player ->
			player.giveItems()
			player.state = GamePlayerState.ALIVE
			player.player.teleport(spawnLocation)
			player.player.sendMessage("You are on team ${player.team.name}")
		}
	}


	private fun launchFireworks(players: List<Player>, color: Color) {
		var count = 0
		Bukkit.getScheduler().runTaskTimer(CaptureTheFlagPlugin.getInstance(), { task ->
			count++
			players.forEach { player ->
				spawnFirework(player.location, color)
			}
			if (count >= 5) {
				task.cancel()
			}

		}, 20, 20)
	}

	private fun spawnFirework(location: Location, color: Color) {
		val firework = location.world!!.spawnEntity(location, EntityType.FIREWORK) as Firework
		val fireworkMeta = firework.fireworkMeta

		val builder = FireworkEffect.builder()
		builder.withColor(color)
		builder.withFade(color)
		builder.with(FireworkEffect.Type.BURST)
		builder.trail(true)
		fireworkMeta.addEffect(builder.build())
		fireworkMeta.power = 1
		firework.fireworkMeta = fireworkMeta
	}


}