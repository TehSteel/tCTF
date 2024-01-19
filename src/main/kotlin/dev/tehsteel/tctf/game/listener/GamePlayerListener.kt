package dev.tehsteel.tctf.game.listener

import dev.tehsteel.minigameapi.api.game.player.GamePlayerJoinEvent
import dev.tehsteel.minigameapi.api.game.player.GamePlayerQuitEvent
import dev.tehsteel.minigameapi.game.GameState
import dev.tehsteel.tctf.CaptureTheFlagPlugin
import dev.tehsteel.tctf.dependency.DependencyContainer
import dev.tehsteel.tctf.game.GameManager
import dev.tehsteel.tctf.game.model.CTFGame
import dev.tehsteel.tctf.game.model.GameFlagState
import dev.tehsteel.tctf.game.model.GamePlayerState
import dev.tehsteel.tctf.game.model.Team
import dev.tehsteel.tctf.util.PlayerUtil
import dev.tehsteel.tctf.util.bossbar.ProgressBossBarBuilder
import dev.tehsteel.tctf.util.bossbar.ProgressBossBarCallback
import dev.tehsteel.tctf.util.bossbar.ProgressBossBarTickCallback
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask
import kotlin.math.cos
import kotlin.math.sin


class GamePlayerListener : Listener {
	private val gameManager: GameManager by DependencyContainer.getInstance()

	@EventHandler
	fun onGamePlayerJoinEvent(event: GamePlayerJoinEvent) {
		if (event.game !is CTFGame) return
		val game: CTFGame = event.game as CTFGame
		val player = event.player ?: return
		val gamePlayer = game.getGamePlayer(player.uniqueId) ?: return

		PlayerUtil.clear(player)
		gamePlayer.state = GamePlayerState.INLOBBY
		player.teleport(game.arena.waitingLocation)
	}

	@EventHandler
	fun onGamePlayerQuitEvent(event: GamePlayerQuitEvent) {
		if (event.game !is CTFGame) return
		val game: CTFGame = event.game as CTFGame
		val player = event.player ?: return
		PlayerUtil.clear(player)
		player.teleport(player.world.spawnLocation)


		if (game.shouldGameEnd()) {
			game.endGame(true)
		}
	}

	@EventHandler
	fun onPlayerInteract(event: PlayerInteractEvent) {
		if (event.hand != EquipmentSlot.HAND) return

		val player = event.player
		val game = gameManager.findGameByPlayer(player) ?: return
		val gamePlayer = game.getGamePlayer(player.uniqueId) ?: return
		val block = event.clickedBlock ?: return

		val isRedBanner = block.type == Material.RED_BANNER
		val isBlueBanner = block.type == Material.BLUE_BANNER

		if (!isRedBanner && !isBlueBanner) return

		val oppositeTeamFlagLocation =
			if (gamePlayer.team == Team.RED) game.blueFlag.location else game.redFlag.location

		val oppositeTeamFlag = if (gamePlayer.team == Team.RED) game.blueFlag else game.redFlag


		if (block.type.name.contains(gamePlayer.team.name)) {
			val flag = gamePlayer.getFlag()
			if (flag?.state != GameFlagState.CAPTURED) {
				event.isCancelled = true
				return
			}
			flag.state = GameFlagState.BASE_CAPTURED
			game.endGame(false)
			return
		}


		ProgressBossBarBuilder
			.of("Stay in the circle for %seconds%")
			.withPlayers(listOf(player))
			.withOverlay(BossBar.Overlay.PROGRESS)
			.withBossBarTickCallback(object : ProgressBossBarTickCallback {
				val radius = 3.0
				var angle = 0.0
				val yOffset = 1.0
				override fun onTick(seconds: Int, players: Collection<Player>, bossBar: BossBar, task: BukkitTask) {
					if (PlayerUtil.isPlayerInArea(player.location, oppositeTeamFlagLocation, 5.0)) {
						player.playSound(player, Sound.ITEM_BOTTLE_FILL, 1F, 1F)


						val centerX: Double = player.location.x
						val centerY: Double = player.location.y
						val centerZ: Double = player.location.z


						var i = 0
						while (i < 360) {
							val x = centerX + radius * cos(Math.toRadians(angle + i))
							val z: Double = centerZ + radius * sin(Math.toRadians(angle + i))
							val particleLocation = Location(player.location.world, x, centerY + yOffset, z)
							player.location.world?.spawnParticle(Particle.FLAME, particleLocation, 1)
							i += 10
						}


						angle += 10.0
					} else {
						bossBar.viewers().forEach { bossBar.removeViewer(it as Audience) }
						player.sendMessage("You left the area!")
						task.cancel()
					}
				}
			})
			.withOnFinishCallback(object : ProgressBossBarCallback {
				override fun onFinish() {
					val bannerItem = ItemStack(block.type)
					val bannerMeta = bannerItem.itemMeta
					bannerMeta?.displayName(Component.text("${block.type.name} Team Flag"))
					bannerItem.itemMeta = bannerMeta

					oppositeTeamFlag.state = GameFlagState.CAPTURED
					gamePlayer.setFlag(oppositeTeamFlag)

					player.inventory.helmet = bannerItem
					block.type = Material.AIR
					player.sendMessage("You captured the flag!")
					player.playSound(player.location, Sound.ITEM_HONEY_BOTTLE_DRINK, 1F, 1F)
				}
			}).run()
	}

	@EventHandler
	fun onPlayerDeathEvent(event: PlayerDeathEvent) {
		val deadPlayer = event.entity
		val game = gameManager.findGameByPlayer(deadPlayer) ?: return
		val deadGamePlayer = game.getGamePlayer(deadPlayer.uniqueId) ?: return

		deadGamePlayer.state = GamePlayerState.DEAD

		deadPlayer.gameMode = GameMode.SPECTATOR

		deadPlayer.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 5 * 20, 255, true, false))
		deadPlayer.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 255, true, false))


		Bukkit.getScheduler().runTaskLater(CaptureTheFlagPlugin.getInstance(), Runnable {
			deadPlayer.spigot().respawn()
			if (deadGamePlayer.team == Team.RED) {
				deadPlayer.teleport(game.redFlag.location)
			} else {
				deadPlayer.teleport(game.blueFlag.location)
			}
		}, 7)

		Bukkit.getScheduler().runTaskLater(CaptureTheFlagPlugin.getInstance(), Runnable {
			run {
				deadPlayer.gameMode = GameMode.SURVIVAL
				deadGamePlayer.state = GamePlayerState.ALIVE
				if (deadGamePlayer.team == Team.RED) {
					deadPlayer.teleport(game.redFlag.location)
				} else {
					deadPlayer.teleport(game.blueFlag.location)
				}
			}
		}, 20 * 5)
	}

	@EventHandler
	fun onEntityDamageByEntityEvent(event: EntityDamageByEntityEvent) {
		if (event.damager !is Player) return
		val damagerPlayer = event.damager as Player
		if (event.entity !is Player) return
		val damagedPlayer = event.entity as Player
		val game = gameManager.findGameByPlayer(damagerPlayer) ?: return

		if (game.state != GameState.INGAME) {
			event.isCancelled = true
			return
		}

		val damagerGamePlayer = game.getGamePlayer(damagerPlayer.uniqueId) ?: return
		val damagedGamePlayer = game.getGamePlayer(damagedPlayer.uniqueId) ?: return

		if (damagerGamePlayer.team == damagedGamePlayer.team) {
			event.isCancelled = true
			return
		}
	}


	@EventHandler
	fun onPlayerInventoryClick(event: InventoryClickEvent) {
		if (event.whoClicked.gameMode == GameMode.CREATIVE) return
		event.isCancelled = true
	}

	@EventHandler
	fun onBlockBreakEvent(event: BlockBreakEvent) {
		if (event.player.gameMode == GameMode.CREATIVE) return
		event.isCancelled = true
	}

	@EventHandler
	fun onBlockPlaceEvent(event: BlockPlaceEvent) {
		if (event.player.gameMode == GameMode.CREATIVE) return
		event.isCancelled = true
	}
}

