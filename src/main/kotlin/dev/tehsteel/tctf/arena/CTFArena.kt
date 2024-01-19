package dev.tehsteel.tctf.arena

import dev.tehsteel.minigameapi.MiniGameLib
import dev.tehsteel.minigameapi.arena.ArenaException
import dev.tehsteel.minigameapi.arena.model.Arena
import dev.tehsteel.minigameapi.util.CustomLocation
import org.bukkit.Location
import org.bukkit.configuration.file.FileConfiguration


class CTFArena(arena: String?) : Arena(arena) {

	var redTeamSpawn: Location? = null
	var blueTeamSpawn: Location? = null
	override fun serialize() {
		val gson = MiniGameLib.getGson()
		config["ArenaData.name"] = name

		if (waitingLocation != null) {
			config["ArenaData.waitingLocation"] = gson.toJson(
				CustomLocation.fromBukkitLocation(waitingLocation),
				CustomLocation::class.java
			)
		}

		config["ArenaData.maxPlayers"] = maxPlayers
		config["ArenaData.minPlayers"] = minPlayers

		if (redTeamSpawn != null) {
			config["ArenaData.redTeamSpawn"] = gson.toJson(
				CustomLocation.fromBukkitLocation(redTeamSpawn),
				CustomLocation::class.java
			)
		}


		if (blueTeamSpawn != null) {
			config["ArenaData.blueTeamSpawn"] = gson.toJson(
				CustomLocation.fromBukkitLocation(blueTeamSpawn),
				CustomLocation::class.java
			)
		}

		saveConfig()
	}


	companion object {
		/**
		 * Deserializes an Arena from a configuration file.
		 *
		 * @param config The FileConfiguration object containing the serialized Arena data.
		 * @return The deserialized Arena.
		 */
		fun deserialize(config: FileConfiguration?): CTFArena {
			val gson = MiniGameLib.getGson()
			if (config == null) {
				throw ArenaException("The config hasn't been initialized yet.")
			}
			if (config.getString("ArenaData.name") == null || config.getString("ArenaData.name")!!.isEmpty()) {
				throw ArenaException("The arena data named does not exist.")
			}
			val arena = CTFArena(config.getString("ArenaData.name"))

			if (config.getString("ArenaData.waitingLocation") != null) arena.waitingLocation = gson.fromJson(
				config.getString("ArenaData.waitingLocation"),
				CustomLocation::class.java
			).toBukkitLocation()

			arena.maxPlayers = config.getInt("ArenaData.maxPlayers")
			arena.minPlayers = config.getInt("ArenaData.minPlayers")

			arena.redTeamSpawn = gson.fromJson(
				config.getString("ArenaData.redTeamSpawn"),
				CustomLocation::class.java
			).toBukkitLocation()


			arena.blueTeamSpawn = gson.fromJson(
				config.getString("ArenaData.blueTeamSpawn"),
				CustomLocation::class.java
			).toBukkitLocation()

			return arena
		}
	}


}