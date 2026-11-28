package ooen.cribrum

import com.google.gson.JsonObject
import com.gtnewhorizon.gtnhlib.util.ItemUtil
import java.util.Random
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary
import ooen.BundledJson
import ooen.addStackCopies
import ooen.concreteOreStacks
import ooen.fortuneMultiplier
import ooen.materia.MetalCatalog
import ooen.materia.MetalForm

internal object SieveRecipes {
    private const val RESOURCE = "/ooen/cribrum/sieve_drops.json"
    private const val METAL_TIERS = "/ooen/cribrum/metal_tiers.json"
    private const val DEFAULT_METAL_TIER = 2

    private class Drop(val minimumTier: Int, val outputs: List<ItemStack>, val percent: Int)

    private lateinit var drops: Map<Block, List<Drop>>

    fun load() {
        val metalTiers = buildMap {
            for (element in BundledJson.readArray(METAL_TIERS)) {
                val row = element.asJsonObject
                val tier = row.get("tier").asBigDecimal.intValueExact()
                require(tier in 1..4) { "$METAL_TIERS: tier must be from 1 to 4" }
                for (metal in row.getAsJsonArray("metals")) {
                    require(put(metal.asString, tier) == null) {
                        "$METAL_TIERS: duplicate metal ${metal.asString}"
                    }
                }
            }
        }
        val metalsByTier = MetalCatalog.names().groupBy { metalTiers[it] ?: DEFAULT_METAL_TIER }
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
                        val tier = row.get("minimumTier").asBigDecimal.intValueExact()
                        require(tier in 0..4) { "minimumTier must be from 0 to 4" }
                        val percent = row.get("percent").asBigDecimal.intValueExact()
                        require(percent in 1..100) { "percent must be from 1 to 100" }
                        val outputs = resolveOutputs(row, metalsByTier[tier].orEmpty())
                        if (outputs.isEmpty()) return@mapIndexedNotNull null
                        input to Drop(tier, outputs, percent)
                    } catch (exception: RuntimeException) {
                        throw IllegalArgumentException(
                            "$RESOURCE, entry ${index + 1}: ${exception.message}",
                            exception,
                        )
                    }
                }
                .groupBy({ it.first }, { it.second })
    }

    private fun resolveOutputs(row: JsonObject, metals: List<String>): List<ItemStack> =
        when {
            row.has("form") -> {
                val form = MetalForm.valueOf(row.get("form").asString)
                metals.mapNotNull { MetalCatalog.candidates(it, form).firstOrNull() }
            }
            row.has("ore") -> {
                val stack = concreteOreStacks(row.get("ore").asString).firstOrNull()
                listOfNotNull(ItemUtil.copyAmount(1, stack))
            }
            else -> {
                val outputId = row.get("output").asString
                val item =
                    requireNotNull(Item.itemRegistry.getObject(outputId) as? Item) {
                        "Unknown output item: $outputId"
                    }
                val metadata = row.get("metadata")?.asBigDecimal?.intValueExact() ?: 0
                require(metadata in 0..<OreDictionary.WILDCARD_VALUE) {
                    "Invalid metadata: $metadata"
                }
                listOf(ItemStack(item, 1, metadata))
            }
        }

    fun inputFor(stack: ItemStack, tier: MeshTier): Block? {
        if (stack.stackSize <= 0 || stack.itemDamage != 0) return null
        val block = Block.getBlockFromItem(stack.item)
        return block.takeIf { drops[it].orEmpty().any { drop -> tier.oreTier >= drop.minimumTier } }
    }

    fun roll(input: Block, tier: MeshTier, random: Random, fortune: Int = 0): List<ItemStack> =
        buildList {
            for (drop in drops[input].orEmpty()) {
                if (
                    tier.oreTier >= drop.minimumTier &&
                        (drop.percent == 100 || random.nextInt(100) < drop.percent)
                ) {
                    val outputs = drop.outputs
                    val output =
                        if (outputs.size == 1) outputs.first()
                        else outputs[random.nextInt(outputs.size)]
                    addStackCopies(output, output.stackSize * fortuneMultiplier(random, fortune))
                }
            }
        }
}
