package dev.tehsteel.tctf.command

import dev.tehsteel.tctf.arena.ArenaManager
import dev.tehsteel.tctf.dependency.DependencyContainer
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ArenaCommand : Command("arena") {
	private val arenaManager: ArenaManager by DependencyContainer.getInstance()
	override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
		val player: Player = sender as Player

		when (args[0]) {
			"create" -> {
				val arena = arenaManager.createArena(args[1])
				arena.serialize()
			}

			"setwaitinglocation" -> {
				val arena = arenaManager.findArenaByName(args[1]) ?: return false
				arena.waitingLocation = player.location
				arena.minPlayers = 2
				arena.maxPlayers = 50
				arena.serialize()
			}


			"setredteam" -> {
				val arena = arenaManager.findArenaByName(args[1]) ?: return false
				arena.redTeamSpawn = player.location
				arena.serialize()
			}

			"setblueteam" -> {
				val arena = arenaManager.findArenaByName(args[1]) ?: return false
				arena.blueTeamSpawn = player.location
				arena.serialize()
			}

			"save" -> {
				val arena = arenaManager.findArenaByName(args[1]) ?: return false
				arena.serialize()
			}
		}

		return true
	}
}