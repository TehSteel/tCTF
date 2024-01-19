package dev.tehsteel.tctf.game.model

import dev.tehsteel.minigameapi.arena.ArenaState
import dev.tehsteel.minigameapi.game.model.Game
import dev.tehsteel.tctf.arena.CTFArena
import dev.tehsteel.tctf.dependency.DependencyContainer
import dev.tehsteel.tctf.game.GameManager
import dev.tehsteel.tctf.util.PlayerUtil
import org.bukkit.entity.Player
import java.util.*


class CTFGame(arena: CTFArena) : Game(arena) {
	private val gameManager: GameManager by DependencyContainer.getInstance()

	val redTeamMap: MutableMap<UUID, GamePlayer> = mutableMapOf()
	val blueTeamMap: MutableMap<UUID, GamePlayer> = mutableMapOf()
	lateinit var redFlag: GameFlag
	lateinit var blueFlag: GameFlag

	init {
		countdown = 5
	}

	override fun startGame(forceStart: Boolean) {
		redFlag = GameFlag(Team.RED, GameFlagState.NONE, arena.redTeamSpawn!!)
		blueFlag = GameFlag(Team.BLUE, GameFlagState.NONE, arena.blueTeamSpawn!!)

		super.startGame(forceStart)
	}

	override fun addPlayer(player: Player) {
		val teams = Team.entries.toTypedArray()
		val randomTeam = teams[Random().nextInt(teams.size)]
		val gamePlayer = GamePlayer(player.uniqueId, player, randomTeam)
		val targetMap = if (randomTeam == Team.RED) redTeamMap else blueTeamMap
		targetMap[gamePlayer.uuid] = gamePlayer

		super.addPlayer(player)
	}

	override fun removePlayer(player: Player) {
		redTeamMap.remove(player.uniqueId)
		blueTeamMap.remove(player.uniqueId)
		gameManager.removePlayerGameFromCache(player.uniqueId)
		super.removePlayer(player)
	}

	override fun resetGame() {
		players.forEach { player ->
			run {
				PlayerUtil.clear(player)
				gameManager.removePlayerGameFromCache(player.uniqueId)
				player.teleport(player.world.spawnLocation)
			}
		}
		redTeamMap.clear()
		blueTeamMap.clear()
		players.clear()
		arena.state = ArenaState.READY

		gameManager.removeGame(this)
	}

	override fun shouldGameEnd(): Boolean {
		return redTeamMap.values.none { gamePlayer -> gamePlayer.state == GamePlayerState.SPECTATOR }
				|| blueTeamMap.values.none { gamePlayer -> gamePlayer.state == GamePlayerState.SPECTATOR }
				|| players.size == 0
				|| blueFlag.state == GameFlagState.BASE_CAPTURED
				|| redFlag.state == GameFlagState.BASE_CAPTURED
	}


	fun getGamePlayer(uuid: UUID): GamePlayer? {
		return redTeamMap.getOrDefault(uuid, blueTeamMap[uuid])
	}


	override fun getArena(): CTFArena {
		return super.getArena() as CTFArena
	}
}