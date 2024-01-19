package dev.tehsteel.tctf

import dev.tehsteel.minigameapi.MiniGameLib
import dev.tehsteel.tctf.arena.ArenaManager
import dev.tehsteel.tctf.command.ArenaCommand
import dev.tehsteel.tctf.command.GameCommand
import dev.tehsteel.tctf.dependency.DependencyContainer
import dev.tehsteel.tctf.game.GameManager
import dev.tehsteel.tctf.game.listener.GameListener
import dev.tehsteel.tctf.game.listener.GamePlayerListener
import dev.tehsteel.tctf.player.PlayerListener
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandMap
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin


class CaptureTheFlagPlugin : JavaPlugin() {

	companion object {
		fun getInstance(): JavaPlugin = getPlugin(CaptureTheFlagPlugin::class.java)
	}


	override fun onEnable() {
		MiniGameLib.setPlugin(this)
		DependencyContainer.register(ArenaManager(this))
		DependencyContainer.register(GameManager())

		listOf(
			GameListener(),
			GamePlayerListener(),
			PlayerListener()
		).forEach { listener -> registerListener(listener) }


		listOf(ArenaCommand(), GameCommand()).forEach { command -> registerCommand(command) }
	}

	override fun onDisable() {

	}


	private fun registerListener(listener: Listener) {
		server.pluginManager.registerEvents(listener, this)
	}

	private fun registerCommand(command: Command) {
		try {
			val commandMapField = Bukkit.getServer().javaClass.getDeclaredField("commandMap")
			commandMapField.setAccessible(true)
			val commandMap = commandMapField[Bukkit.getServer()] as CommandMap
			commandMap.register(command.label, command)
		} catch (exception: NoSuchFieldException) {
			exception.printStackTrace()
		} catch (exception: IllegalArgumentException) {
			exception.printStackTrace()
		} catch (exception: IllegalAccessException) {
			exception.printStackTrace()
		}
	}


}