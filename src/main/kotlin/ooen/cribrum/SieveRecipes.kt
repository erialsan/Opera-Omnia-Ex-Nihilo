package ooen.cribrum

import java.util.Random
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import ooen.BundledJson
import ooen.materia.MetalCatalog
import ooen.materia.MetalForm

internal object SieveRecipes {
    private const val RESOURCE = "/ooen/cribrum/sieve_drops.json"

    private class Drop(val minimumTier: MeshTier, val output: ItemStack, val percent: Int)

    private lateinit var drops: Map<Block, List<Drop>>

    fun load() {
        drops =
            BundledJson.readArray(RESOURCE)
                .mapIndexedNotNull { index, element ->
                    try {
                        val row = element.asJsonObject
                        val inputId = row.get("input").asString
                        require(Block.blockRegistry.containsKey(inputId)) {
                            "Unknown input block: $inputId"
                        }
                        val input = Block.blockRegistry.getObject(inputId) as Block
                        require(Item.getItemFromBlock(input) != null) {
                            "Input block has no item: $inputId"
                        }
                        val tier = MeshTier.valueOf(row.get("minimumTier").asString)
                        val output =
                            if (row.has("metal")) {
                                val metal = row.get("metal").asString
                                val form = MetalForm.valueOf(row.get("form").asString)
                                MetalCatalog.candidates(metal, form).firstOrNull()
                                    ?: return@mapIndexedNotNull null
                            } else {
                                val outputId = row.get("output").asString
                                val item =
                                    requireNotNull(Item.itemRegistry.getObject(outputId) as? Item) {
                                        "Unknown output item: $outputId"
                                    }
                                ItemStack(item)
                            }
                        val percent = row.get("percent").asBigDecimal.intValueExact()
                        require(percent in 1..100) { "percent must be from 1 to 100" }
                        input to Drop(tier, output, percent)
                    } catch (exception: RuntimeException) {
                        throw IllegalArgumentException(
                            "$RESOURCE, entry ${index + 1}: ${exception.message}",
                            exception,
                        )
                    }
                }
                .groupBy({ it.first }, { it.second })
    }

    fun inputFor(stack: ItemStack, tier: MeshTier): Block? {
        if (stack.stackSize <= 0 || stack.itemDamage != 0) return null
        val block = Block.getBlockFromItem(stack.item)
        return block.takeIf { drops[it].orEmpty().any { drop -> tier >= drop.minimumTier } }
    }

    fun roll(input: Block, tier: MeshTier, random: Random): List<ItemStack> = buildList {
        for (drop in drops[input].orEmpty()) {
            if (
                tier >= drop.minimumTier &&
                    (drop.percent == 100 || random.nextInt(100) < drop.percent)
            ) {
                add(drop.output.copy())
            }
        }
    }
}
