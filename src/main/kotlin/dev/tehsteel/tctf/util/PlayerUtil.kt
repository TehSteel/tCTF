package dev.tehsteel.tctf.util

import com.google.common.io.ByteStreams
import dev.tehsteel.tctf.CaptureTheFlagPlugin
import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.*
import java.util.function.Consumer


object PlayerUtil {
	fun isPlayerInArea(location: Location, areaCenter: Location, radius: Double): Boolean {
		return location.distanceSquared(areaCenter) <= radius * radius
	}

	fun clear(player: Player) {
		player.inventory.clear()
		player.inventory.armorContents = null
		player.activePotionEffects.forEach(Consumer { potionEffect: PotionEffect ->
			player.removePotionEffect(
				potionEffect.type
			)
		})
		player.gameMode = GameMode.SURVIVAL
		player.foodLevel = 20
		player.health = 20.0
	}
}