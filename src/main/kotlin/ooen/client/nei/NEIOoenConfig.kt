package ooen.client.nei

import codechicken.nei.PositionedStack
import codechicken.nei.api.API
import codechicken.nei.api.IConfigureNEI
import codechicken.nei.recipe.IUsageHandler
import codechicken.nei.recipe.TemplateRecipeHandler
import com.gtnewhorizon.gtnhlib.util.data.ItemId
import cpw.mods.fml.common.registry.GameRegistry
import java.text.NumberFormat
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.StatCollector
import net.minecraftforge.oredict.OreDictionary
import ooen.cribrum.MeshTier
import ooen.cribrum.SieveRecipes
import ooen.fundamentum.BARREL_LAVA
import ooen.fundamentum.BarrelReaction
import ooen.fundamentum.COMPOST_CAPACITY
import ooen.fundamentum.COMPOST_READY
import ooen.fundamentum.COMPOST_TICKS
import ooen.fundamentum.PlantInputs
import ooen.fundamentum.VanillaWood
import ooen.fundamentum.barrelBucket
import ooen.fundamentum.barrelProduct
import ooen.materia.Tags

class NEIOoenConfig : IConfigureNEI {
    override fun getName(): String = "Opera Omnia Ex Nihilo"

    override fun getVersion(): String = Tags.VERSION

    override fun loadConfig() {
        for (category in Category.entries) {
            val handler = ProcessHandler(category)
            API.registerRecipeHandler(handler)
            API.registerUsageHandler(handler)
            for (machine in category.machines) API.addRecipeCatalyst(ItemStack(machine), handler)
        }
    }
}

private enum class Category(val id: String, val modId: String) {
    BARREL("barrel", "ooen_fundamentum"),
    SIEVE("sieve", "ooen_cribrum");

    val machines: List<Item> by lazy {
        val wooden = VanillaWood.entries.mapNotNull { GameRegistry.findItem(modId, id + it.suffix) }
        if (this == BARREL) wooden + listOfNotNull(stoneBarrel) else wooden
    }
}

private val stoneBarrel: Item?
    get() = GameRegistry.findItem("ooen_transmutatio", "stone_barrel")

private class Row(
    val inputs: List<List<ItemStack>>,
    val output: ItemStack,
    val details: String,
    val stoneOnly: Boolean = false,
)

private class ProcessHandler(private val category: Category) : TemplateRecipeHandler() {
    override fun getRecipeName(): String = StatCollector.translateToLocal("ooen.nei.${category.id}")

    override fun getOverlayIdentifier(): String = "ooen.${category.id}"

    override fun getHandlerId(): String = overlayIdentifier

    override fun getGuiTexture(): String = "minecraft:textures/gui/container/furnace.png"

    override fun newInstance(): TemplateRecipeHandler = ProcessHandler(category)

    override fun loadCraftingRecipes(outputId: String, vararg results: Any) {
        if (outputId == overlayIdentifier || outputId == "all") {
            for (row in rows()) arecipes.add(Display(row))
        } else {
            super.loadCraftingRecipes(outputId, *results)
        }
    }

    override fun loadCraftingRecipes(result: ItemStack) {
        for (row in rows()) {
            if (OreDictionary.itemMatches(row.output, result, false)) arecipes.add(Display(row))
        }
    }

    override fun loadUsageRecipes(ingredient: ItemStack) {
        val machine = ingredient.item in category.machines
        for (row in rows()) {
            if (machine && (!row.stoneOnly || ingredient.item == stoneBarrel)) {
                arecipes.add(Display(row))
            } else if (
                row.inputs.any { slot ->
                    slot.any { OreDictionary.itemMatches(it, ingredient, false) }
                }
            ) {
                arecipes.add(Display(row, ingredient))
            }
        }
    }

    override fun getUsageAndCatalystHandler(
        inputId: String,
        vararg ingredients: Any,
    ): IUsageHandler = getUsageHandler(inputId, *ingredients)

    override fun drawBackground(recipe: Int) {
        val display = arecipes[recipe] as Display
        for (slot in display.ingredients + display.result) {
            Gui.drawRect(
                slot.relx - 1,
                slot.rely - 1,
                slot.relx + 17,
                slot.rely + 17,
                0xff555555.toInt(),
            )
            Gui.drawRect(slot.relx, slot.rely, slot.relx + 17, slot.rely + 17, 0xff8b8b8b.toInt())
        }
    }

    override fun drawExtras(recipe: Int) {
        val font = Minecraft.getMinecraft().fontRenderer
        val details = (arecipes[recipe] as Display).details
        font.drawString("→", 91, 16, 0x404040)
        for ((line, text) in font.listFormattedStringToWidth(details, 144).withIndex()) {
            font.drawString(
                text,
                (166 - font.getStringWidth(text)) / 2,
                32 + line * font.FONT_HEIGHT,
                0x404040,
            )
        }
    }

    private fun rows(): List<Row> =
        when (category) {
            Category.BARREL -> barrelRows()
            Category.SIEVE -> sieveRows()
        }

    private inner class Display(row: Row, selected: ItemStack? = null) : CachedRecipe() {
        val details = row.details
        private val inputSlots =
            row.inputs.mapIndexed { index, stacks ->
                val matching = selected?.let { stack ->
                    stacks
                        .firstOrNull { OreDictionary.itemMatches(it, stack, false) }
                        ?.let { stack.copy().apply { stackSize = it.stackSize } }
                }
                PositionedStack(matching ?: stacks.map(ItemStack::copy), 30 + index * 29, 12)
            }

        override fun getIngredients(): List<PositionedStack> = inputSlots

        override fun getResult(): PositionedStack = output

        private val output = PositionedStack(row.output.copy(), 117, 12)
    }
}

private fun barrelRows(): List<Row> = buildList {
    val compost = PlantInputs.stacks().map { it.apply { stackSize = COMPOST_CAPACITY } }
    if (compost.isNotEmpty()) {
        add(
            Row(
                listOf(compost),
                requireNotNull(barrelProduct(COMPOST_READY)),
                StatCollector.translateToLocalFormatted(
                    "ooen.nei.barrel.compost",
                    COMPOST_CAPACITY,
                    COMPOST_TICKS / 20,
                ),
            )
        )
    }
    for (reaction in BarrelReaction.entries) {
        val ingredient = reaction.ingredient() ?: continue
        val stoneOnly = reaction.liquid == BARREL_LAVA
        if (stoneOnly && stoneBarrel == null) continue
        val bucket = requireNotNull(barrelBucket(reaction.liquid))
        add(
            Row(
                listOf(listOf(ItemStack(bucket)), listOf(ItemStack(ingredient))),
                reaction.output(),
                if (stoneOnly) StatCollector.translateToLocal("ooen.nei.barrel.stone") else "",
                stoneOnly,
            )
        )
    }
}

private class SieveOutput(val tier: Int, val stack: ItemStack, val chance: Double)

private fun sieveRows(): List<Row> = buildList {
    val mesh = requireNotNull(GameRegistry.findItem("ooen_cribrum", "string_mesh"))
    val tiers = MeshTier.entries.filter(MeshTier::isAvailable)
    val percent = NumberFormat.getPercentInstance().apply { maximumFractionDigits = 2 }
    for ((block, drops) in SieveRecipes.entries()) {
        val outputs = drops.flatMap { drop ->
            // One randomly chosen output per successful roll.
            drop.outputs.groupBy(::stackKey).values.map {
                SieveOutput(
                    drop.minimumTier,
                    it.first(),
                    drop.percent / 100.0 * it.size / drop.outputs.size,
                )
            }
        }
        for (rolls in outputs.groupBy { it.tier to stackKey(it.stack) }.values) {
            val first = rolls.first()
            val meshes =
                tiers.filter { it.oreTier >= first.tier }.map { ItemStack(mesh, 1, it.ordinal) }
            val chances =
                rolls
                    .groupingBy { it.chance }
                    .eachCount()
                    .entries
                    .joinToString(" + ") { (chance, count) ->
                        val formatted = percent.format(chance)
                        if (count == 1) formatted else "$formatted × $count"
                    }
            add(
                Row(
                    listOf(listOf(ItemStack(block)), meshes),
                    first.stack,
                    StatCollector.translateToLocalFormatted("ooen.nei.sieve.chance", chances),
                )
            )
        }
    }
}

private fun stackKey(stack: ItemStack) =
    ItemId.createNoCopy(stack.item, stack.itemDamage, stack.tagCompound) to stack.stackSize
