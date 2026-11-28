package ooen.fundamentum

import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import java.util.Random
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.util.IIcon
import net.minecraft.util.StatCollector
import net.minecraft.world.World
import ooen.exchangeBucket

internal const val COMPOST_CAPACITY = 8
internal const val COMPOST_READY = COMPOST_CAPACITY + 1
internal const val BARREL_WATER = 10
internal const val BARREL_LAVA = 11
private const val BARREL_CLAY = 12
private const val BARREL_QUARTZ = 13
private const val BARREL_END_STONE = 14

internal class CompostBarrel(private val wood: VanillaWood?) :
    Block(if (wood == null) Material.rock else Material.wood) {
    init {
        setBlockName("ooen.barrel${wood?.suffix ?: "_stone"}")
        if (wood == null) {
            setHardness(2f)
            setStepSound(soundTypeStone)
            setHarvestLevel("pickaxe", 0)
        } else {
            setHardness(1.5f)
            setStepSound(soundTypeWood)
        }
    }

    override fun isOpaqueCube(): Boolean = false

    override fun renderAsNormalBlock(): Boolean = false

    override fun getRenderType(): Int = Fundamentum.barrelRenderType

    override fun onBlockActivated(
        world: World,
        x: Int,
        y: Int,
        z: Int,
        player: EntityPlayer,
        side: Int,
        hitX: Float,
        hitY: Float,
        hitZ: Float,
    ): Boolean {
        val fill = world.getBlockMetadata(x, y, z)
        val held = player.currentEquippedItem
        if (held == null) {
            val product = product(fill) ?: return false
            if (!world.isRemote && world.setBlockMetadataWithNotify(x, y, z, 0, 3)) {
                dropBlockAsItem(world, x, y, z, product)
            }
            return true
        }
        if (held.stackSize <= 0) return false
        if (held.item == Items.water_bucket || held.item == Items.lava_bucket) {
            val liquid = if (held.item == Items.water_bucket) BARREL_WATER else BARREL_LAVA
            if (
                fill == 0 &&
                    (liquid == BARREL_WATER || wood == null) &&
                    !world.isRemote &&
                    world.setBlockMetadataWithNotify(x, y, z, liquid, 3) &&
                    !player.capabilities.isCreativeMode
            ) {
                player.exchangeBucket(held, Items.bucket)
            }
            return true
        }
        if (held.item == Items.bucket && (fill == BARREL_WATER || fill == BARREL_LAVA)) {
            if (!world.isRemote && world.setBlockMetadataWithNotify(x, y, z, 0, 3)) {
                val result = if (fill == BARREL_WATER) Items.water_bucket else Items.lava_bucket
                player.exchangeBucket(held, result)
            }
            return true
        }
        val mixed =
            when {
                fill == BARREL_WATER &&
                    held.item == GameRegistry.findItem("ooen_malleus", "dust") -> BARREL_CLAY
                fill == BARREL_LAVA && held.item == Items.redstone -> BARREL_QUARTZ
                fill == BARREL_LAVA && held.item == Items.glowstone_dust -> BARREL_END_STONE
                else -> null
            }
        if (mixed != null) {
            if (
                !world.isRemote &&
                    world.setBlockMetadataWithNotify(x, y, z, mixed, 3) &&
                    !player.capabilities.isCreativeMode
            ) {
                held.stackSize--
            }
            return true
        }
        if (fill >= COMPOST_CAPACITY || !PlantInputs.accepts(held)) return false
        if (!world.isRemote && world.setBlockMetadataWithNotify(x, y, z, fill + 1, 3)) {
            if (!player.capabilities.isCreativeMode) held.stackSize--
            if (fill + 1 == COMPOST_CAPACITY) world.scheduleBlockUpdate(x, y, z, this, 200)
        }
        return true
    }

    override fun updateTick(world: World, x: Int, y: Int, z: Int, random: Random) {
        if (world.getBlockMetadata(x, y, z) == COMPOST_CAPACITY) {
            world.setBlockMetadataWithNotify(x, y, z, COMPOST_READY, 3)
        }
    }

    @SideOnly(Side.CLIENT) override fun registerBlockIcons(register: IIconRegister) {}

    @SideOnly(Side.CLIENT)
    override fun getIcon(side: Int, metadata: Int): IIcon =
        wood?.let { Blocks.planks.getIcon(side, it.metadata) } ?: Blocks.stone.getIcon(side, 0)

    override fun getDrops(
        world: World,
        x: Int,
        y: Int,
        z: Int,
        metadata: Int,
        fortune: Int,
    ): ArrayList<ItemStack> =
        super.getDrops(world, x, y, z, metadata, fortune).apply {
            if (metadata > COMPOST_READY) product(metadata)?.let(::add)
        }

    fun compostStatus(metadata: Int): String =
        when (metadata) {
            COMPOST_READY -> StatCollector.translateToLocal("ooen.barrel.ready")
            COMPOST_CAPACITY -> StatCollector.translateToLocal("ooen.barrel.working")
            BARREL_WATER -> liquidStatus(Blocks.water)
            BARREL_LAVA -> liquidStatus(Blocks.lava)
            else ->
                product(metadata)?.displayName
                    ?: StatCollector.translateToLocalFormatted(
                        "ooen.barrel.fill",
                        metadata,
                        COMPOST_CAPACITY,
                    )
        }

    private fun liquidStatus(block: Block): String =
        StatCollector.translateToLocalFormatted("ooen.barrel.liquid", block.localizedName, 1000)

    private fun product(fill: Int): ItemStack? =
        when (fill) {
            COMPOST_READY -> ItemStack(Blocks.dirt)
            BARREL_CLAY -> ItemStack(Blocks.clay)
            BARREL_QUARTZ -> ItemStack(Items.quartz)
            BARREL_END_STONE -> ItemStack(Blocks.end_stone)
            else -> null
        }
}

internal fun barrelSurfaceBlock(fill: Int): Block? =
    when (fill) {
        COMPOST_READY -> Blocks.dirt
        BARREL_WATER -> Blocks.water
        BARREL_LAVA -> Blocks.lava
        BARREL_CLAY -> Blocks.clay
        BARREL_QUARTZ -> Blocks.quartz_block
        BARREL_END_STONE -> Blocks.end_stone
        else -> null
    }
