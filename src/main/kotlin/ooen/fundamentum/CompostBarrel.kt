package ooen.fundamentum

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import java.util.Random
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.ChatComponentTranslation
import net.minecraft.util.IIcon
import net.minecraft.world.World

private const val CAPACITY = 8
private const val READY = CAPACITY + 1

internal class CompostBarrel : Block(Material.wood) {
    init {
        setBlockName("ooen.barrel")
        setBlockTextureName("minecraft:log_oak")
        setCreativeTab(CreativeTabs.tabBlock)
        setHardness(1.5f)
        setStepSound(soundTypeWood)
    }

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
            if (!world.isRemote) {
                when (fill) {
                    READY ->
                        if (world.setBlockMetadataWithNotify(x, y, z, 0, 3)) {
                            dropBlockAsItem(world, x, y, z, ItemStack(Blocks.dirt))
                        }
                    CAPACITY ->
                        player.addChatComponentMessage(
                            ChatComponentTranslation("ooen.barrel.working")
                        )
                    else ->
                        player.addChatComponentMessage(
                            ChatComponentTranslation("ooen.barrel.fill", fill, CAPACITY)
                        )
                }
            }
            return true
        }
        if (fill >= CAPACITY || !matchesOre(held, "treeLeaves")) return false
        if (!world.isRemote && world.setBlockMetadataWithNotify(x, y, z, fill + 1, 3)) {
            if (!player.capabilities.isCreativeMode) held.stackSize--
            if (fill + 1 == CAPACITY) world.scheduleBlockUpdate(x, y, z, this, 200)
        }
        return true
    }

    override fun updateTick(world: World, x: Int, y: Int, z: Int, random: Random) {
        if (world.getBlockMetadata(x, y, z) == CAPACITY) {
            world.setBlockMetadataWithNotify(x, y, z, READY, 3)
        }
    }

    @SideOnly(Side.CLIENT)
    override fun getIcon(side: Int, metadata: Int): IIcon =
        when {
            side != 1 -> super.getIcon(side, metadata)
            metadata == READY -> Blocks.dirt.getIcon(side, 0)
            metadata > 0 -> Blocks.leaves.getIcon(side, 0)
            else -> Blocks.planks.getIcon(side, 0)
        }
}
