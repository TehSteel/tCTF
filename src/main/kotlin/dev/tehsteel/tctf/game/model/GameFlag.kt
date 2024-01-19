package dev.tehsteel.tctf.game.model

import org.bukkit.Location

data class GameFlag(
	val team: Team,
	var state: GameFlagState = GameFlagState.NONE,
	var location: Location
)


enum class GameFlagState {
	NONE,
	CAPTURED,
	BASE_CAPTURED,
}