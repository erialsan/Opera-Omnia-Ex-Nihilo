package ooen.cribrum

import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.ShapedOreRecipe
import ooen.fundamentum.VanillaWood
import ooen.materia.Tags

@Mod(
    modid = "ooen_cribrum",
    name = "Opera Omnia Ex Nihilo: Cribrum",
    version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "required-after:ooen_fundamentum",
    modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter",
)
object Cribrum {
    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        val mesh =
            StringMesh()
                .setUnlocalizedName("ooen.string_mesh")
                .setCreativeTab(CreativeTabs.tabMaterials)
        GameRegistry.registerItem(mesh, "string_mesh")
        GameRegistry.addRecipe(
            ShapedOreRecipe(
                ItemStack(mesh),
                " S ",
                "SWS",
                " S ",
                'S',
                Items.string,
                'W',
                Items.stick,
            )
        )
        for (wood in VanillaWood.entries) {
            val sieve = Sieve(mesh, wood)
            GameRegistry.registerBlock(sieve, "sieve" + wood.suffix)
            GameRegistry.addRecipe(
                ShapedOreRecipe(
                    ItemStack(sieve),
                    "P P",
                    "PPP",
                    "S S",
                    'P',
                    ItemStack(Blocks.planks, 1, wood.metadata),
                    'S',
                    Items.stick,
                )
            )
        }
    }
}
