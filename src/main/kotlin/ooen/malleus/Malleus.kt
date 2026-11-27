package ooen.malleus

import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.FMLLoadCompleteEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.ShapedOreRecipe
import ooen.GTNH_LIB_DEPENDENCY
import ooen.fundamentum.Fundamentum
import ooen.materia.Tags
import ooen.netheriteIngots

@Mod(
    modid = "ooen_malleus",
    name = "Opera Omnia Ex Nihilo: Malleus",
    version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "required-after:ooen_fundamentum;$GTNH_LIB_DEPENDENCY",
    modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter",
)
object Malleus {
    private const val NETHERITE_ID = "netherite_hammer"
    private val netheriteHammer = Hammer(NETHERITE_ID)

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        val materials =
            mapOf(
                "wooden_hammer" to "plankWood",
                "stone_hammer" to "cobblestone",
                "iron_hammer" to "ingotIron",
                "diamond_hammer" to "gemDiamond",
                "emerald_hammer" to "gemEmerald",
            )
        for ((id, material) in materials) {
            val hammer = Hammer(id).setCreativeTab(Fundamentum.creativeTab)
            GameRegistry.registerItem(hammer, id)
            GameRegistry.addRecipe(
                ShapedOreRecipe(
                        ItemStack(hammer),
                        " M ",
                        " SM",
                        "S  ",
                        'M',
                        material,
                        'S',
                        "stickWood",
                    )
                    .setMirrored(false)
            )
        }
        // Keep the registry ID stable across worlds with different optional mods.
        GameRegistry.registerItem(netheriteHammer, NETHERITE_ID)
    }

    @Mod.EventHandler
    fun enableNetherite(event: FMLLoadCompleteEvent) {
        if (netheriteIngots().isNotEmpty()) netheriteHammer.setCreativeTab(Fundamentum.creativeTab)
    }
}

private class Hammer(id: String) : Item() {
    init {
        setUnlocalizedName("ooen.$id")
        setTextureName("ooen_malleus:$id")
        setMaxStackSize(1)
        setFull3D()
    }

    @SideOnly(Side.CLIENT)
    override fun getSubItems(item: Item, tab: CreativeTabs?, items: MutableList<ItemStack>) {
        if (creativeTab != null) super.getSubItems(item, tab, items)
    }
}
