package ooen.cribrum

import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.FMLLoadCompleteEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.AnvilUpdateEvent
import net.minecraftforge.oredict.RecipeSorter
import net.minecraftforge.oredict.ShapedOreRecipe
import ooen.GTNH_LIB_DEPENDENCY
import ooen.fundamentum.Fundamentum
import ooen.fundamentum.VanillaWood
import ooen.materia.Tags
import ooen.netheriteIngots

@Mod(
    modid = "ooen_cribrum",
    name = "Opera Omnia Ex Nihilo: Cribrum",
    version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies =
        "required-after:ooen_fundamentum;required-after:ooen_malleus;required-after:ooen_materia;$GTNH_LIB_DEPENDENCY",
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
        val pebble =
            Item()
                .setUnlocalizedName("ooen.stone_pebble")
                .setTextureName("ooen_cribrum:stone_pebble")
                .setCreativeTab(Fundamentum.creativeTab)
        GameRegistry.registerItem(pebble, "stone_pebble")
        GameRegistry.addShapedRecipe(ItemStack(Blocks.cobblestone), "PP", "PP", 'P', pebble)
        MinecraftForge.EVENT_BUS.register(this)
        RecipeSorter.register(
            "ooen_cribrum:mesh_upgrade",
            MeshUpgradeRecipe::class.java,
            RecipeSorter.Category.SHAPED,
            "after:forge:shapedore before:minecraft:shapeless",
        )
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
            GameRegistry.registerBlock(sieve, "sieve${wood.suffix}")
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

    @SubscribeEvent
    fun enchantMesh(event: AnvilUpdateEvent) {
        val stack = event.left
        val book = event.right
        if (event.output != null || stack.item != mesh || stack.stackSize != 1) return
        if (book.item != Items.enchanted_book) return

        val bookEnchantments = EnchantmentHelper.getEnchantments(book)
        val enchantments = EnchantmentHelper.getEnchantments(stack)
        var cost = 0
        for (enchantment in listOf(Enchantment.fortune, Enchantment.efficiency)) {
            val id = enchantment.effectId
            val bookLevel = bookEnchantments[id] ?: continue
            if (bookLevel <= 0) continue
            val oldLevel = enchantments[id] ?: 0
            val combined = if (bookLevel == oldLevel) oldLevel + 1 else maxOf(bookLevel, oldLevel)
            val level = combined.coerceAtMost(enchantment.maxLevel)
            if (level <= oldLevel) continue
            enchantments[id] = level
            cost += 2 * level
        }
        if (cost == 0) return

        val output = stack.copy()
        EnchantmentHelper.setEnchantments(enchantments, output)
        val name = event.name
        val rename =
            if (name.isNullOrBlank()) {
                output.hasDisplayName().also { if (it) output.func_135074_t() }
            } else if (name != stack.displayName) {
                output.setStackDisplayName(name)
                true
            } else false
        output.repairCost = maxOf(stack.repairCost, book.repairCost) + 2

        event.output = output
        event.cost += cost + if (rename) 5 else 0
        event.materialCost = 1
    }

    @Mod.EventHandler
    fun onLoadComplete(event: FMLLoadCompleteEvent) {
        SieveRecipes.load()
        val ingots = netheriteIngots()
        netheriteAvailable = ingots.isNotEmpty()
        for (ingot in ingots) registerUpgrade(MeshTier.NETHERITE, ingot)
    }

    private fun registerUpgrade(tier: MeshTier, material: ItemStack) {
        GameRegistry.addRecipe(
            MeshUpgradeRecipe(
                ItemStack(mesh, 1, tier.ordinal),
                material,
                ItemStack(mesh, 1, tier.ordinal - 1),
            )
        )
    }
}
