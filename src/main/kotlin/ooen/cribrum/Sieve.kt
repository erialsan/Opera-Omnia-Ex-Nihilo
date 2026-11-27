package ooen.cribrum

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
import net.minecraft.util.ChatComponentTranslation
import net.minecraft.util.IIcon
import net.minecraft.world.World

private const val FRAME = 0
private const val MESH = 1
private const val LOADED = 2
private const val LAST_PASS = 4

internal class Sieve(private val mesh: Item) : Block(Material.wood) {
    init {
        setBlockName("ooen.sieve")
        setBlockTextureName("minecraft:planks_oak")
        setCreativeTab(CreativeTabs.tabBlock)
        setHardness(1.5f)
        setStepSound(soundTypeWood)
        setBlockBounds(0f, 0f, 0f, 1f, 0.75f, 1f)
    }

    override fun isOpaqueCube(): Boolean = false

    override fun canSilkHarvest(): Boolean = false

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
        val stage = world.getBlockMetadata(x, y, z)
        val held = player.currentEquippedItem
        val next =
            when (stage) {
                FRAME -> if (held?.item == mesh) MESH else return false
                MESH ->
                    if (held?.item == Item.getItemFromBlock(Blocks.dirt) && held.itemDamage == 0) {
                        LOADED
                    } else return false
                in LOADED until LAST_PASS -> stage + 1
                LAST_PASS -> MESH
                else -> return false
            }
        if (!world.isRemote && world.setBlockMetadataWithNotify(x, y, z, next, 3)) {
            when (stage) {
                FRAME,
                MESH -> if (!player.capabilities.isCreativeMode) held!!.stackSize--
                LAST_PASS -> dropBlockAsItem(world, x, y, z, ItemStack(Items.wheat_seeds))
                else ->
                    player.addChatComponentMessage(
                        ChatComponentTranslation(
                            "ooen.sieve.progress",
                            next - LOADED,
                            LAST_PASS - LOADED + 1,
                        )
                    )
            }
        }
        return true
    }

    override fun getDrops(
        world: World,
        x: Int,
        y: Int,
        z: Int,
        metadata: Int,
        fortune: Int,
    ): ArrayList<ItemStack> =
        arrayListOf(ItemStack(this)).apply {
            if (metadata >= MESH) add(ItemStack(mesh))
            if (metadata >= LOADED) add(ItemStack(Blocks.dirt))
        }

    @SideOnly(Side.CLIENT)
    override fun getIcon(side: Int, metadata: Int): IIcon =
        when {
            side != 1 || metadata == FRAME -> super.getIcon(side, metadata)
            metadata == MESH -> Blocks.wool.getIcon(side, 0)
            else -> Blocks.dirt.getIcon(side, 0)
        }
}
