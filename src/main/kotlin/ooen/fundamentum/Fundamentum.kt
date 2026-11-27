package ooen.fundamentum

import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.FMLInterModComms
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent
import net.minecraftforge.oredict.OreDictionary
import net.minecraftforge.oredict.ShapedOreRecipe
import ooen.materia.Tags

@Mod(
    modid = "ooen_fundamentum",
    name = "Opera Omnia Ex Nihilo: Fundamentum",
    version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "required-after:forgelin@[2.4.3-2.4.10,)",
    modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter",
)
object Fundamentum {
    internal var barrelRenderType: Int = 0
        private set

    internal val creativeTab: CreativeTabs =
        object : CreativeTabs("ooen") {
            @SideOnly(Side.CLIENT) override fun getTabIconItem(): Item = crook
        }

    private val infestedLeaves = InfestedLeaves()
    private val crook =
        Crook()
            .setUnlocalizedName("ooen.crook")
            .setTextureName("ooen_fundamentum:crook_wood")
            .setCreativeTab(creativeTab)
    private val larva =
        Silkworm(infestedLeaves)
            .setUnlocalizedName("ooen.silkworm")
            .setTextureName("ooen_fundamentum:silkworm")
            .setCreativeTab(creativeTab)

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        if (event.side.isClient) barrelRenderType = BarrelRenderer.register()
        GameRegistry.registerItem(crook, "crook")
        GameRegistry.registerItem(larva, "silkworm")
        GameRegistry.registerBlock(infestedLeaves.setCreativeTab(creativeTab), "infested_leaves")
        GameRegistry.addRecipe(
            ShapedOreRecipe(ItemStack(crook), "SS", " S", " S", 'S', Items.stick)
        )
        for (wood in VanillaWood.entries) {
            val barrel = CompostBarrel(wood).setCreativeTab(creativeTab)
            GameRegistry.registerBlock(barrel, "barrel" + wood.suffix)
            GameRegistry.addRecipe(
                ShapedOreRecipe(
                    ItemStack(barrel),
                    "P P",
                    "P P",
                    "PSP",
                    'P',
                    ItemStack(Blocks.planks, 1, wood.metadata),
                    'S',
                    ItemStack(Blocks.wooden_slab, 1, wood.metadata),
                )
            )
        }
        FMLInterModComms.sendMessage("Waila", "register", "ooen.fundamentum.WailaTooltips.register")
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun harvestLeaves(event: HarvestDropsEvent) {
        val player = event.harvester ?: return
        val tool = player.currentEquippedItem ?: return
        if (event.world.isRemote || player.capabilities.isCreativeMode || tool.item != crook) return
        if (event.isSilkTouching) return

        val drop =
            if (event.block == infestedLeaves) {
                if (infestedLeaves.isMature(event.blockMetadata)) ItemStack(Items.string, 2)
                else null
            } else {
                val item = Item.getItemFromBlock(event.block) ?: return
                val leaf = ItemStack(item, 1, event.block.damageDropped(event.blockMetadata))
                if (!matchesOre(leaf, "treeLeaves")) return
                leaf
            }
        if (drop != null) {
            event.drops.add(drop)
            // Give a new crook a reliable first larva, then one every four harvests.
            if (tool.itemDamage % 4 == 0) event.drops.add(ItemStack(larva))
        }
        tool.damageItem(1, player)
        if (tool.stackSize == 0) player.destroyCurrentEquippedItem()
    }
}

private class Silkworm(private val leaves: InfestedLeaves) : Item() {
    override fun onItemUse(
        stack: ItemStack,
        player: EntityPlayer,
        world: World,
        x: Int,
        y: Int,
        z: Int,
        side: Int,
        hitX: Float,
        hitY: Float,
        hitZ: Float,
    ): Boolean {
        if (!player.canPlayerEdit(x, y, z, side, stack) || !leaves.canInfest(world, x, y, z)) {
            return false
        }
        if (world.isRemote) return true
        if (!leaves.infest(world, x, y, z)) return false
        if (!player.capabilities.isCreativeMode) stack.stackSize--
        return true
    }
}

private class Crook : Item() {
    init {
        maxStackSize = 1
        setMaxDamage(64)
    }

    override fun getDigSpeed(stack: ItemStack, block: Block, meta: Int): Float =
        if (block.material == Material.leaves) 4.0f else 1.0f
}

internal fun matchesOre(stack: ItemStack, oreName: String): Boolean =
    OreDictionary.getOres(oreName, false).any { OreDictionary.itemMatches(it, stack, false) }
