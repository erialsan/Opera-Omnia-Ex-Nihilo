package ooen.transmutatio

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.IIcon
import net.minecraft.world.World
import ooen.exchangeBucket
import ooen.fundamentum.PlantInputs
import ooen.fundamentum.VanillaWood

internal class Crucible(private val wood: VanillaWood?) :
    Block(if (wood == null) Material.rock else Material.wood) {
    val isWooden = wood != null

    init {
        if (wood == null) {
            setBlockName("ooen.fired_crucible")
            setHardness(2f)
            setStepSound(soundTypeStone)
            setHarvestLevel("pickaxe", 0)
        } else {
            setBlockName("ooen.wooden_crucible${wood.suffix}")
            setHardness(1.5f)
            setStepSound(soundTypeWood)
        }
        setBlockBounds(2f / 16, 0f, 2f / 16, 14f / 16, 13f / 16, 14f / 16)
    }

    override fun isOpaqueCube(): Boolean = false

    override fun renderAsNormalBlock(): Boolean = false

    override fun getRenderType(): Int = Transmutatio.crucibleRenderType

    override fun hasTileEntity(metadata: Int): Boolean = true

    override fun createTileEntity(world: World, metadata: Int): TileEntity = CrucibleContents()

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
        val held = player.currentEquippedItem ?: return false
        if (held.stackSize <= 0) return false
        val contents = world.getTileEntity(x, y, z) as? CrucibleContents ?: return false
        if (held.item == Items.bucket) {
            if (!world.isRemote && contents.takeBucket()) {
                player.exchangeBucket(held, if (isWooden) Items.water_bucket else Items.lava_bucket)
            }
            return true
        }
        if (!accepts(held)) return false
        if (!world.isRemote && contents.addInput() && !player.capabilities.isCreativeMode)
            held.stackSize--
        return true
    }

    private fun accepts(stack: ItemStack): Boolean =
        if (isWooden) PlantInputs.accepts(stack)
        else Block.getBlockFromItem(stack.item) == Blocks.cobblestone && stack.itemDamage == 0

    fun rate(world: World, x: Int, y: Int, z: Int): Int =
        if (isWooden) 5
        else CrucibleHeat.rate(world.getBlock(x, y - 1, z), world.getBlockMetadata(x, y - 1, z))

    @SideOnly(Side.CLIENT) override fun registerBlockIcons(register: IIconRegister) {}

    @SideOnly(Side.CLIENT)
    override fun getIcon(side: Int, metadata: Int): IIcon {
        val woodMeta = wood?.metadata ?: return Blocks.hardened_clay.getIcon(side, 0)
        return if (woodMeta < 4) Blocks.log.getIcon(2, woodMeta)
        else Blocks.log2.getIcon(2, woodMeta - 4)
    }
}
