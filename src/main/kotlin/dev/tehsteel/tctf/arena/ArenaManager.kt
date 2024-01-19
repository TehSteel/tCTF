package dev.tehsteel.tctf.arena

import dev.tehsteel.minigameapi.arena.model.Arena
import dev.tehsteel.tctf.CaptureTheFlagPlugin
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*


class ArenaManager(private val plugin: CaptureTheFlagPlugin) {

	val arenas: MutableMap<String, CTFArena> = mutableMapOf()

	init {
		loadArenas()
	}

	fun createArena(name: String): CTFArena {
		val arena = CTFArena(name)
		arenas[name] = arena
		return arena
	}


	fun deleteArena(arena: Arena) {
		arena.deleteArenaConfig()
	}

	fun deleteArena(name: String) {
		findArenaByName(name)?.let { deleteArena(it) }
	}

	fun findArenaByName(name: String): CTFArena? {
		return arenas[name]
	}


	fun getFreeArena(): CTFArena? {
		return arenas
			.values
			.stream()
			.filter { obj: CTFArena -> obj.isArenaReady }
			.findFirst().orElse(null)
	}

	private fun loadArenas() {
		if (!File("${plugin.dataFolder}/arenas").exists()) return
		Arrays.stream(File("${plugin.dataFolder}/arenas").listFiles()).forEach { file ->
			val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
			if (config.getString("ArenaData.name") != null) {
				val arena: CTFArena = CTFArena.deserialize(config)
				arenas[arena.name] = arena
			}
		}
	}
}