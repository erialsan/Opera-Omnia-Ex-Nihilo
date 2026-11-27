package ooen.materia

import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.StatCollector
import net.minecraftforge.oredict.OreDictionary
import net.minecraftforge.oredict.ShapedOreRecipe
import ooen.BundledJson
import ooen.fundamentum.Fundamentum

internal object MateriaOreItems {
    private val forms =
        listOf(
            MetalForm.SMALL_GRAVEL to MetalForm.GRAVEL,
            MetalForm.SMALL_SAND to MetalForm.SAND,
            MetalForm.SMALL_POWDER to MetalForm.POWDER,
        )

    fun register() {
        val colors =
            BundledJson.readArray("/ooen/materia/metal_colors.json").associate { entry ->
                val row = entry.asJsonObject
                row.get("metal").asString to row.get("rgb").asString.toInt(16)
            }
        for (metal in MetalCatalog.names().distinctBy { it.lowercase() }) {
            val ingot = MetalCatalog.candidates(metal, MetalForm.INGOT).firstOrNull() ?: continue
            val color = MetalCatalog.color(metal) ?: colors[metal] ?: 0xA8A5A0
            for ((small, full) in forms) {
                val fullItem = MetalCatalog.candidates(metal, full).firstOrNull()
                val output = fullItem ?: registerVariant(metal, full, color)
                val smallItem = MetalCatalog.candidates(metal, small).firstOrNull()
                if (smallItem == null) registerVariant(metal, small, color)
                if (smallItem == null || fullItem == null) {
                    GameRegistry.addRecipe(
                        ShapedOreRecipe(output, "SS", "SS", 'S', small.oreKey(metal))
                    )
                }
                if (fullItem == null) GameRegistry.addSmelting(output, ingot.copy(), 0f)
            }
        }
    }

    private fun registerVariant(metal: String, form: MetalForm, color: Int): ItemStack {
        val name = metal.lowercase()
        val formName = form.texture
        val id =
            if (formName.startsWith("small_")) "small_${name}_${formName.removePrefix("small_")}"
            else "${name}_$formName"
        val item = OreVariant(metal, form, color).setCreativeTab(Fundamentum.creativeTab)
        GameRegistry.registerItem(item, id)
        return ItemStack(item).also { OreDictionary.registerOre(form.oreKey(metal), it) }
    }
}

private class OreVariant(
    private val metal: String,
    private val form: MetalForm,
    private val color: Int,
) : Item() {
    init {
        setUnlocalizedName("ooen.${form.texture}")
        setTextureName("ooen_materia:${form.texture}")
    }

    override fun getItemStackDisplayName(stack: ItemStack): String {
        val metalKey = "metal.ooen.${metal.lowercase()}"
        val metalName =
            if (StatCollector.canTranslate(metalKey)) StatCollector.translateToLocal(metalKey)
            else metal
        return StatCollector.translateToLocalFormatted("item.ooen.${form.texture}.name", metalName)
    }

    @SideOnly(Side.CLIENT)
    override fun getColorFromItemStack(stack: ItemStack, pass: Int): Int = color
}
