package com.gmail.arhamjsiddiqui.runebot.entity

import com.gmail.arhamjsiddiqui.runebot.data.YAMLParse
import com.gmail.arhamjsiddiqui.runebot.randomItem
import com.mikebull94.rsapi.RuneScapeAPI
import java.awt.Color
import java.util.*
import java.util.concurrent.ThreadLocalRandom

/**
 * Represents items that can be acquired.
 *
 * @author Arham 4
 */
data class Item(val id: Int, var count: Int = 1, val rarity: Rarity = Rarity.COMMON) {
    val definition by lazy { GRAND_EXCHANGE_API.itemPriceInformation(id).get().item }
    val name by lazy { definition.name }
    val imageLink = "https://www.runelocus.com/items/img/$id.png"

    override fun equals(other: Any?): Boolean {
        if (other is Item) return other.id == id
        return false
    }

    override fun hashCode(): Int {
        return id
    }
}

enum class Rarity(val color: Color) {
    COMMON(Color.YELLOW), UNCOMMON(Color.ORANGE), RARE(Color.RED)
}

data class ItemsDto(val commonItems: Array<Int>, val uncommonItems: Array<Int>, val rareItems: Array<Int>, private val statRequirements: Map<String, Array<Any>>) {
    /**
     * Need to find a better way than to do Array<Any> @ statRequirements
     */

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemsDto

        if (!Arrays.equals(commonItems, other.commonItems)) return false
        if (!Arrays.equals(uncommonItems, other.uncommonItems)) return false
        if (!Arrays.equals(rareItems, other.rareItems)) return false
        if (statRequirements != other.statRequirements) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(commonItems)
        result = 31 * result + Arrays.hashCode(uncommonItems)
        result = 31 * result + Arrays.hashCode(rareItems)
        result = 31 * result + statRequirements.hashCode()
        return result
    }

    fun getRequirementsForItem(name: String): List<Array<Any>> {
        return statRequirements.filter { entry -> name.toLowerCase().startsWith(entry.key.toLowerCase()) }.map { it -> it.value }
    }
}

object ItemFunctions {
    fun generateRandomItem(): Item {
        val randomNumber = ThreadLocalRandom.current().nextInt(1, 101)
        return when (randomNumber) {
            in 1..50 -> Item(items.commonItems.randomItem(), rarity = Rarity.COMMON)
            in 51..80 -> Item(items.uncommonItems.randomItem(), rarity = Rarity.UNCOMMON)
            else -> Item(items.rareItems.randomItem(), rarity = Rarity.RARE)
        }
    }

    fun saveItems(player: Player) {
        Player.sql { dsl, table ->
            dsl.update(table).set(table.ITEM_IDS, player.items.map { it.id }.toTypedArray())
                    .set(table.ITEM_COUNTS, player.items.map{ it.count }.toTypedArray())
                    .where(table.DISCORD_ID.eq(player.asDiscordUser.id))
                    .execute()
        }
    }
}

val items: ItemsDto = YAMLParse.parseDto("data/items_data.yaml", ItemsDto::class)
val GRAND_EXCHANGE_API = RuneScapeAPI.createHttp().grandExchange()