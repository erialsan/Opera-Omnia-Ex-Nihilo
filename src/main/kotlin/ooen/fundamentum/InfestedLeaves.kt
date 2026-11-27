package ooen.fundamentum

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import java.util.Random
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection

// Metadata 0 and 1 were the original starting and mature states.
private const val MATURE = 1
private const val START = 2
private const val STEPS = 10
private const val TICKS_PER_STEP = 20

internal class InfestedLeaves : Block(Material.leaves) {
    init {
        setBlockName("ooen.infested_leaves")
        setBlockTextureName("minecraft:leaves_oak")
        setCreativeTab(CreativeTabs.tabBlock)
        setHardness(0.2f)
        setStepSound(soundTypeGrass)
    }

    override fun onBlockAdded(world: World, x: Int, y: Int, z: Int) {
        if (!world.isRemote && !isMature(world.getBlockMetadata(x, y, z))) {
            world.scheduleBlockUpdate(x, y, z, this, TICKS_PER_STEP)
        }
    }

    override fun updateTick(world: World, x: Int, y: Int, z: Int, random: Random) {
        val metadata = world.getBlockMetadata(x, y, z)
        if (isMature(metadata)) return
        val next = START + stage(metadata) + 1
        if (next < START + STEPS) {
            world.setBlockMetadataWithNotify(x, y, z, next, 3)
            world.scheduleBlockUpdate(x, y, z, this, TICKS_PER_STEP)
        } else if (world.setBlockMetadataWithNotify(x, y, z, MATURE, 3)) {
            for (direction in ForgeDirection.VALID_DIRECTIONS) {
                infest(world, x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ)
            }
        }
    }

    fun canInfest(world: World, x: Int, y: Int, z: Int): Boolean {
        if (!world.blockExists(x, y, z)) return false
        val block = world.getBlock(x, y, z)
        return block != this && block.isLeaves(world, x, y, z)
    }

    fun infest(world: World, x: Int, y: Int, z: Int): Boolean =
        canInfest(world, x, y, z) && world.setBlock(x, y, z, this, START, 3)

    fun isMature(metadata: Int): Boolean = metadata == MATURE

    private fun stage(metadata: Int): Int =
        if (isMature(metadata)) STEPS else (metadata - START).coerceIn(0, STEPS - 1)

    fun progressPercent(metadata: Int): Int = stage(metadata) * 100 / STEPS

    override fun canSilkHarvest(): Boolean = false

    override fun getDrops(
        world: World,
        x: Int,
        y: Int,
        z: Int,
        metadata: Int,
        fortune: Int,
    ): ArrayList<ItemStack> = arrayListOf()

    @SideOnly(Side.CLIENT)
    override fun colorMultiplier(world: IBlockAccess, x: Int, y: Int, z: Int): Int {
        val progress = stage(world.getBlockMetadata(x, y, z))
        val red = 108 + (255 - 108) * progress / STEPS
        val green = 155 + (255 - 155) * progress / STEPS
        val blue = 76 + (255 - 76) * progress / STEPS
        return (red shl 16) or (green shl 8) or blue
    }
}
