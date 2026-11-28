package ooen.transmutatio

import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.FMLLoadCompleteEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import ooen.GTNH_LIB_DEPENDENCY
import ooen.fundamentum.CompostBarrel
import ooen.fundamentum.Fundamentum
import ooen.fundamentum.VanillaWood
import ooen.materia.Tags

@Mod(
    modid = "ooen_transmutatio",
    name = "Opera Omnia Ex Nihilo: Transmutatio",
    version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies =
        "required-after:ooen_fundamentum;required-after:ooen_malleus;$GTNH_LIB_DEPENDENCY",
    modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter",
)
object Transmutatio {
    internal var crucibleRenderType = 0
        private set

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        if (event.side.isClient) crucibleRenderType = CrucibleRenderer.register()
        GameRegistry.registerTileEntity(CrucibleContents::class.java, "ooen_transmutatio:crucible")

        val barrel = CompostBarrel(null).setCreativeTab(Fundamentum.creativeTab)
        GameRegistry.registerBlock(barrel, "stone_barrel")
        GameRegistry.addShapedRecipe(
            ItemStack(barrel),
            "S S",
            "S S",
            "SLS",
            'S',
            Blocks.stone,
            'L',
            ItemStack(Blocks.stone_slab),
        )

        for (wood in VanillaWood.entries) {
            val crucible = Crucible(wood).setCreativeTab(Fundamentum.creativeTab)
            GameRegistry.registerBlock(crucible, "wooden_crucible${wood.suffix}")
            val log =
                if (wood.metadata < 4) ItemStack(Blocks.log, 1, wood.metadata)
                else ItemStack(Blocks.log2, 1, wood.metadata - 4)
            GameRegistry.addShapedRecipe(ItemStack(crucible), "L L", "L L", "LLL", 'L', log)
        }

        val porcelain = ingredient("porcelain_clay")
        val unfired = ingredient("unfired_crucible")
        val fired = Crucible(null).setCreativeTab(Fundamentum.creativeTab)
        GameRegistry.registerBlock(fired, "fired_crucible")
        GameRegistry.addShapelessRecipe(
            ItemStack(porcelain),
            Items.clay_ball,
            ItemStack(Items.dye, 1, 15),
        )
        GameRegistry.addShapedRecipe(ItemStack(unfired), "P P", "P P", "PPP", 'P', porcelain)
        GameRegistry.addSmelting(unfired, ItemStack(fired), 0.1f)
    }

    @Mod.EventHandler fun loadHeatSources(event: FMLLoadCompleteEvent) = CrucibleHeat.load()

    private fun ingredient(id: String): Item =
        Item()
            .setUnlocalizedName("ooen.$id")
            .setTextureName("ooen_transmutatio:$id")
            .setCreativeTab(Fundamentum.creativeTab)
            .also { GameRegistry.registerItem(it, id) }
}
