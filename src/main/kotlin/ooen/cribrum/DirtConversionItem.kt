package ooen.cribrum

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import ooen.fundamentum.Fundamentum

internal class DirtConversionItem(
    private val result: Block,
    id: String,
    texture: String,
    private val color: Int,
) : Item() {
    init {
        setUnlocalizedName("ooen.$id")
        setTextureName(texture)
        setCreativeTab(Fundamentum.creativeTab)
    }

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
        if (stack.stackSize <= 0 || world.getBlock(x, y, z) != Blocks.dirt) return false
        if (world.getBlockMetadata(x, y, z) != 0) return false
        if (!world.canMineBlock(player, x, y, z) || !player.canPlayerEdit(x, y, z, side, stack)) {
            return false
        }
        if (world.isRemote) return true
        if (!world.setBlock(x, y, z, result)) return false
        if (!player.capabilities.isCreativeMode) stack.stackSize--
        return true
    }

    @SideOnly(Side.CLIENT)
    override fun getColorFromItemStack(stack: ItemStack, pass: Int): Int = color
}
