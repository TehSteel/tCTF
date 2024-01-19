package dev.tehsteel.tctf.command

import dev.tehsteel.minigameapi.game.GameState
import dev.tehsteel.tctf.arena.ArenaManager
import dev.tehsteel.tctf.dependency.DependencyContainer
import dev.tehsteel.tctf.game.GameManager
import dev.tehsteel.tctf.game.model.CTFGame
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class GameCommand : Command("game") {
	private val gameManager: GameManager by DependencyContainer.getInstance()

	private val arenaManager: ArenaManager by DependencyContainer.getInstance()
	override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
		val player: Player = sender as Player





		when (args[0]) {
			"join" -> {
				var freeGame = gameManager.findFreeGame()

				if (freeGame == null) {
					val arena = arenaManager.getFreeArena()

					if (arena == null) {
						player.sendMessage("lol")
						return false
					}

					freeGame = gameManager.createGame(arena)
					freeGame.addPlayer(player)
				} else {
					freeGame.addPlayer(player)
				}
			}

			"leave" -> {
				val game: CTFGame = gameManager.findGameByPlayer(player) ?: return false
				player.sendMessage("remove")
				game.removePlayer(player)
			}

			"forcestart" -> {
				val game: CTFGame = gameManager.findGameByPlayer(player) ?: return false
				if (game.state !== GameState.WAITING) return false
				game.startCountdown(true)
			}

			"forceend" -> {
				val game: CTFGame = gameManager.findGameByPlayer(player) ?: return false
				game.endGame(true)
			}
		}
		return true
	}
}