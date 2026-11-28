package ooen.malleus

import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.FMLLoadCompleteEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent
import net.minecraftforge.oredict.ShapedOreRecipe
import ooen.GTNH_LIB_DEPENDENCY
import ooen.ToolTier
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
    private val dust =
        (object : Block(Material.sand) {})
            .setBlockName("ooen.dust")
            .setBlockTextureName("ooen_malleus:dust")
            .setHardness(0.5f)
            .setStepSound(Block.soundTypeSand)

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        GameRegistry.registerBlock(dust.setCreativeTab(Fundamentum.creativeTab), "dust")
        MinecraftForge.EVENT_BUS.register(this)
        for (tier in ToolTier.entries) {
            val material = tier.material ?: continue
            val id = "${tier.idPrefix}_hammer"
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

    @SubscribeEvent
    fun crushHarvest(event: HarvestDropsEvent) {
        val player = event.harvester ?: return
        if (event.world.isRemote || player.capabilities.isCreativeMode || event.isSilkTouching)
            return
        if (player.currentEquippedItem?.item !is Hammer) return
        val output = crushedBlock(event.block) ?: return
        val original = Item.getItemFromBlock(event.block)
        event.drops.removeAll { stack ->
            stack.item == original || (event.block == Blocks.gravel && stack.item == Items.flint)
        }
        event.drops.add(ItemStack(output))
    }

    internal fun crushedBlock(block: Block): Block? =
        when (block) {
            Blocks.cobblestone -> Blocks.gravel
            Blocks.gravel -> Blocks.sand
            Blocks.sand -> dust
            else -> null
        }
}

private class Hammer(id: String) : Item() {
    init {
        setUnlocalizedName("ooen.$id")
        setTextureName("ooen_malleus:$id")
        setMaxStackSize(1)
        setFull3D()
    }

    override fun canHarvestBlock(block: Block, stack: ItemStack): Boolean =
        Malleus.crushedBlock(block) != null

    override fun getDigSpeed(stack: ItemStack, block: Block, metadata: Int): Float =
        if (Malleus.crushedBlock(block) != null) 4f else super.getDigSpeed(stack, block, metadata)

    @SideOnly(Side.CLIENT)
    override fun getSubItems(item: Item, tab: CreativeTabs?, items: MutableList<ItemStack>) {
        if (creativeTab != null) super.getSubItems(item, tab, items)
    }
}
