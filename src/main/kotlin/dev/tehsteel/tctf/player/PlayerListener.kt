package dev.tehsteel.tctf.player

import dev.tehsteel.minigameapi.api.game.player.GamePlayerQuitEvent
import dev.tehsteel.tctf.dependency.DependencyContainer
import dev.tehsteel.tctf.game.GameManager
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener : Listener {

	private val gameManager: GameManager by DependencyContainer.getInstance()

	@EventHandler
	fun onPlayerQuitEvent(event: PlayerQuitEvent) {
		val player = event.player
		val game = gameManager.findGameByPlayer(player) ?: return
		val gamePlayerQuitEvent = GamePlayerQuitEvent(game, player)

		Bukkit.getPluginManager().callEvent(gamePlayerQuitEvent)
	}
}