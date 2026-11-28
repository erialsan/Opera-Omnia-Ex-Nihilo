package ooen.materia

import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.util.StatCollector
import net.minecraft.world.IBlockAccess
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
            val name = metal.lowercase()
            for ((small, full) in forms) {
                val block = OreBlock(metal, full, color)
                GameRegistry.registerBlock(
                    block,
                    MateriaOreBlockItem::class.java,
                    "${name}_${full.texture}",
                )
                val output = ItemStack(block)
                OreDictionary.registerOre(full.oreKey(metal), output)

                val fragments = MetalCatalog.candidates(metal, small)
                if (fragments.isEmpty()) {
                    val item =
                        OreFragment(metal, small, color).setCreativeTab(Fundamentum.creativeTab)
                    val fragmentId = "small_${name}_${small.texture.removePrefix("small_")}"
                    GameRegistry.registerItem(item, fragmentId)
                    OreDictionary.registerOre(small.oreKey(metal), ItemStack(item))
                } else if (OreDictionary.getOres(small.oreKey(metal), false).isEmpty()) {
                    // A fragment found through a JAOPCA alias must also match the canonical recipe
                    // key.
                    for (fragment in fragments) OreDictionary.registerOre(
                        small.oreKey(metal),
                        fragment,
                    )
                }
                GameRegistry.addRecipe(
                    ShapedOreRecipe(output, "SS", "SS", 'S', small.oreKey(metal))
                )
                GameRegistry.addSmelting(output, ingot.copy(), 0f)
            }
        }
    }
}

private class OreFragment(
    private val metal: String,
    form: MetalForm,
    private val color: Int,
) : Item() {
    init {
        setUnlocalizedName("ooen.${form.texture}")
        setTextureName("ooen_materia:${form.texture}")
    }

    override fun getItemStackDisplayName(stack: ItemStack): String = oreName(unlocalizedName, metal)

    @SideOnly(Side.CLIENT)
    override fun getColorFromItemStack(stack: ItemStack, pass: Int): Int = color
}

internal class OreBlock(val metal: String, val form: MetalForm, private val color: Int) :
    Block(Material.sand) {
    init {
        setBlockName("ooen.${form.texture}")
        setBlockTextureName(
            when (form) {
                MetalForm.GRAVEL -> "minecraft:gravel"
                MetalForm.SAND -> "minecraft:sand"
                MetalForm.POWDER -> "ooen_malleus:dust"
                else -> error("No block texture for $form")
            }
        )
        val gravel = form == MetalForm.GRAVEL
        setHardness(if (gravel) 0.6f else 0.5f)
        setStepSound(if (gravel) soundTypeGravel else soundTypeSand)
        setHarvestLevel("shovel", 0)
        setCreativeTab(Fundamentum.creativeTab)
    }

    override fun getLocalizedName(): String = oreName(unlocalizedName, metal)

    @SideOnly(Side.CLIENT) override fun getRenderColor(metadata: Int): Int = color

    @SideOnly(Side.CLIENT)
    override fun colorMultiplier(world: IBlockAccess, x: Int, y: Int, z: Int): Int = color
}

internal class MateriaOreBlockItem(private val block: Block) : ItemBlock(block) {
    override fun getItemStackDisplayName(stack: ItemStack): String = block.localizedName
}

private fun oreName(unlocalizedName: String, metal: String): String {
    val metalKey = "metal.ooen.${metal.lowercase()}"
    val metalName =
        if (StatCollector.canTranslate(metalKey)) StatCollector.translateToLocal(metalKey)
        else metal
    return StatCollector.translateToLocalFormatted("$unlocalizedName.name", metalName)
}
