package ooen.cribrum

import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.FMLLoadCompleteEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary
import net.minecraftforge.oredict.ShapedOreRecipe
import ooen.fundamentum.Fundamentum
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
    private val mesh = StringMesh()
    internal var netheriteAvailable = false
        private set

    internal var sieveRenderType: Int = 0
        private set

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        if (event.side.isClient) sieveRenderType = SieveRenderer.register()
        mesh.setCreativeTab(Fundamentum.creativeTab)
        GameRegistry.registerItem(mesh, "string_mesh")
        GameRegistry.registerTileEntity(SieveMeshData::class.java, "ooen_cribrum:sieve_mesh")
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
        registerUpgrade(MeshTier.FLINT, ItemStack(Items.flint))
        registerUpgrade(MeshTier.IRON, ItemStack(Items.iron_ingot))
        registerUpgrade(MeshTier.GOLD, ItemStack(Items.gold_ingot))
        registerUpgrade(MeshTier.DIAMOND, ItemStack(Items.diamond))
        registerUpgrade(MeshTier.EMERALD, ItemStack(Items.emerald))
        for (wood in VanillaWood.entries) {
            val sieve = Sieve(mesh, wood).setCreativeTab(Fundamentum.creativeTab)
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

    @Mod.EventHandler
    fun registerNetherite(event: FMLLoadCompleteEvent) {
        val ingots =
            listOf("ingotNetherite", "NetheriteIngot")
                .flatMap { OreDictionary.getOres(it, false) }
                .filter { it.item != null && it.itemDamage != OreDictionary.WILDCARD_VALUE }
                .distinctBy { it.item to it.itemDamage }
        netheriteAvailable = ingots.isNotEmpty()
        for (ingot in ingots) registerUpgrade(MeshTier.NETHERITE, ingot)
    }

    private fun registerUpgrade(tier: MeshTier, material: ItemStack) {
        GameRegistry.addRecipe(
            ShapedOreRecipe(
                ItemStack(mesh, 1, tier.ordinal),
                " M ",
                "MPM",
                " M ",
                'M',
                material,
                'P',
                ItemStack(mesh, 1, tier.ordinal - 1),
            )
        )
    }
}
