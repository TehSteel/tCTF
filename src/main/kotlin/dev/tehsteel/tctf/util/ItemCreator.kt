package dev.tehsteel.tctf.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.LeatherArmorMeta
import java.util.function.Consumer


class ItemCreator private constructor(material: Material) {
	private val item: ItemStack
	private val meta: ItemMeta?

	init {
		item = ItemStack(material)
		meta = item.itemMeta
	}

	fun setDisplayName(text: String): ItemCreator {
		meta!!.displayName(MiniMessage.miniMessage().deserialize(text))
		return this
	}

	fun setDisplayName(text: String, italic: Boolean): ItemCreator {
		meta!!.displayName(MiniMessage.miniMessage().deserialize(text).decoration(TextDecoration.ITALIC, italic))
		return this
	}

	fun setDyedColor(color: Color): ItemCreator {
		meta as LeatherArmorMeta
		meta.setColor(color)
		return this
	}

	fun setLore(lore: List<String>): ItemCreator {
		val components: MutableList<Component> = ArrayList()
		lore.forEach(Consumer { text: String ->
			components.add(
				MiniMessage.miniMessage().deserialize(text)
			)
		})
		meta!!.lore(components)
		return this
	}

	fun build(): ItemStack {
		item.setItemMeta(meta)
		return item
	}

	companion object {
		fun of(material: Material): ItemCreator {
			return ItemCreator(material)
		}
	}
}