package ooen.vita

import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.FMLInitializationEvent
import cpw.mods.fml.common.event.FMLLoadCompleteEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.oredict.ShapedOreRecipe
import ooen.COMMON_DEPENDENCIES
import ooen.ToolTier
import ooen.fundamentum.Fundamentum
import ooen.materia.Tags
import ooen.netheriteIngots

@Mod(
    modid = "ooen_vita",
    name = "Opera Omnia Ex Nihilo: Vita",
    version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "required-after:ooen_fundamentum;$COMMON_DEPENDENCIES",
)
object Vita {
    @JvmStatic @Mod.InstanceFactory private fun modInstance(): Vita = this

    private val wateringCans = ToolTier.entries.associateWith(::WateringCan)

    internal fun starterWateringCan(): ItemStack {
        val can = wateringCans.getValue(ToolTier.WOOD)
        return ItemStack(can).also {
            can.fill(it, FluidStack(FluidRegistry.WATER, can.getCapacity(it)), true)
        }
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        for ((tier, can) in wateringCans) {
            GameRegistry.registerItem(can, "${tier.idPrefix}_watering_can")
            if (tier.material != null) can.setCreativeTab(Fundamentum.creativeTab)
        }
    }

    @Mod.EventHandler
    fun registerRecipes(event: FMLInitializationEvent) {
        for ((tier, can) in wateringCans) {
            val material = tier.material ?: continue
            GameRegistry.addRecipe(
                ShapedOreRecipe(
                    ItemStack(can),
                    "M  ",
                    "MMS",
                    " MM",
                    'M',
                    material,
                    'S',
                    "stickWood",
                )
            )
        }
    }

    @Mod.EventHandler
    fun enableNetherite(event: FMLLoadCompleteEvent) {
        if (netheriteIngots().isNotEmpty()) {
            wateringCans.getValue(ToolTier.NETHERITE).setCreativeTab(Fundamentum.creativeTab)
        }
    }
}
