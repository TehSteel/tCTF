package dev.tehsteel.tctf.game.model

import dev.tehsteel.tctf.util.ItemCreator
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*


data class GamePlayer(
	val uuid: UUID,
	var player: Player,
	val team: Team
) {
	var state: GamePlayerState = GamePlayerState.INLOBBY
	private var flag: GameFlag? = null

	fun giveItems() {
		val sword = ItemCreator.of(Material.WOODEN_SWORD).build()

		val chestplate = ItemCreator.of(Material.LEATHER_CHESTPLATE).setDyedColor(team.color).build()
		val leggings = ItemCreator.of(Material.LEATHER_LEGGINGS).setDyedColor(team.color).build()
		val boots = ItemCreator.of(Material.LEATHER_BOOTS).setDyedColor(team.color).build()

		player.inventory.addItem(sword)
		player.inventory.armorContents = arrayOf(boots, leggings, chestplate, null)
	}

	fun setFlag(flag: GameFlag?) {
		this.flag = flag
		if (flag != null) {
			flag.state = GameFlagState.CAPTURED
		}
	}

	fun getFlag(): GameFlag? {
		return this.flag
	}
}


enum class Team(val color: Color) {
	RED(Color.RED),
	BLUE(Color.BLUE)

}

enum class GamePlayerState {
	ALIVE,
	DEAD,
	INLOBBY,
	SPECTATOR
}