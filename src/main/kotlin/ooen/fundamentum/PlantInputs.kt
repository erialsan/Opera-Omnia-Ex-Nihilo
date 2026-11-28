package ooen.fundamentum

import com.gtnewhorizon.gtnhlib.util.data.ItemId
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary
import ooen.BundledJson

internal object PlantInputs {
    private const val RESOURCE = "/ooen/fundamentum/plant_inputs.json"
    private val items = mutableMapOf<Item, MutableSet<Int>>()
    private val ores = mutableSetOf<String>()

    fun load() {
        for (element in BundledJson.readArray(RESOURCE)) {
            val row = element.asJsonObject
            require(row.has("item") != row.has("ore")) { "$RESOURCE: specify one item or ore" }
            if (row.has("item")) {
                val id = row.get("item").asString
                val item = Item.itemRegistry.getObject(id) as? Item
                if (item == null && row.get("optional")?.asBoolean == true) continue
                requireNotNull(item) { "$RESOURCE: unknown item $id" }
                val metadata = row.get("metadata")?.asBigDecimal?.intValueExact()
                require(metadata == null || metadata in 0..<OreDictionary.WILDCARD_VALUE) {
                    "$RESOURCE: invalid metadata $metadata for $id"
                }
                items
                    .getOrPut(item) { mutableSetOf() }
                    .add(metadata ?: OreDictionary.WILDCARD_VALUE)
            } else {
                require(!row.has("metadata")) { "$RESOURCE: metadata is only valid with item" }
                ores.add(row.get("ore").asString)
            }
        }
    }

    fun accepts(stack: ItemStack): Boolean {
        val metadata = items[stack.item]
        return metadata?.contains(OreDictionary.WILDCARD_VALUE) == true ||
            metadata?.contains(stack.itemDamage) == true ||
            OreDictionary.getOreIDs(stack).any { OreDictionary.getOreName(it) in ores }
    }

    fun stacks(): List<ItemStack> =
        (items.asSequence().flatMap { (item, metadata) ->
                metadata.map { ItemStack(item, 1, it) }
            } + ores.asSequence().flatMap { OreDictionary.getOres(it, false) })
            .filter { it.item != null && it.stackSize > 0 }
            .distinctBy { ItemId.createNoCopy(it.item, it.itemDamage, it.tagCompound) }
            .map { it.copy().apply { stackSize = 1 } }
            .toList()
}
