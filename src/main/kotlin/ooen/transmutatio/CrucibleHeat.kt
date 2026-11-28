package ooen.transmutatio

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary
import ooen.BundledJson

internal object CrucibleHeat {
    private const val RESOURCE = "/ooen/transmutatio/crucible_heat_sources.json"
    private val blocks = mutableMapOf<Block, Int>()
    private val ores = mutableMapOf<String, Int>()

    fun load() {
        for (element in BundledJson.readArray(RESOURCE)) {
            val row = element.asJsonObject
            val rate = row.get("mbPerTick").asBigDecimal.intValueExact()
            require(rate > 0) { "$RESOURCE: mbPerTick must be positive" }
            require(row.has("block") != row.has("ore")) { "$RESOURCE: specify one block or ore" }
            if (row.has("block")) {
                val id = row.get("block").asString
                require(Block.blockRegistry.containsKey(id)) { "$RESOURCE: unknown block $id" }
                val block = Block.blockRegistry.getObject(id) as Block
                require(blocks.put(block, rate) == null) { "$RESOURCE: duplicate block $id" }
            } else {
                val ore = row.get("ore").asString
                require(ores.put(ore, rate) == null) { "$RESOURCE: duplicate ore $ore" }
            }
        }
    }

    fun rate(block: Block, metadata: Int): Int {
        blocks[block]?.let {
            return it
        }
        val item = Item.getItemFromBlock(block) ?: return 0
        return OreDictionary.getOreIDs(ItemStack(item, 1, metadata)).maxOfOrNull { id ->
            ores[OreDictionary.getOreName(id)] ?: 0
        } ?: 0
    }
}
