package dev.tehsteel.tctf.game

import dev.tehsteel.tctf.arena.CTFArena
import dev.tehsteel.tctf.game.model.CTFGame
import org.bukkit.entity.Player
import java.util.*


class GameManager {

	private val games: MutableSet<CTFGame> = mutableSetOf()
	private val cachedGamesByPlayers: MutableMap<UUID, CTFGame?> = mutableMapOf()

	fun createGame(arena: CTFArena): CTFGame {
		val game = CTFGame(arena)
		games.add(game)
		return game
	}

	fun removeGame(game: CTFGame) {
		games.remove(game)
		cachedGamesByPlayers.values.remove(game)
	}

	fun removePlayerGameFromCache(uuid: UUID) {
		cachedGamesByPlayers.remove(uuid)
	}

	fun findFreeGame(): CTFGame? {
		return games.firstOrNull { it.isGameFree }
	}

	fun findGameByPlayer(player: Player): CTFGame? {
		return cachedGamesByPlayers[player.uniqueId] ?: run {
			val game = games.firstOrNull { it.players.contains(player) }
			cachedGamesByPlayers[player.uniqueId] = game
			game
		}
	}
}
