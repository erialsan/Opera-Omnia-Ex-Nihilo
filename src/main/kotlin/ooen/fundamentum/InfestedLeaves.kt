package ooen.fundamentum

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import java.util.Random
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.util.IIcon
import net.minecraft.world.World

internal class InfestedLeaves : Block(Material.leaves) {
    init {
        setBlockName("ooen.infested_leaves")
        setBlockTextureName("minecraft:leaves_oak")
        setCreativeTab(CreativeTabs.tabBlock)
        setHardness(0.2f)
        setStepSound(soundTypeGrass)
    }

    override fun onBlockAdded(world: World, x: Int, y: Int, z: Int) {
        if (!world.isRemote) world.scheduleBlockUpdate(x, y, z, this, 100)
    }

    override fun updateTick(world: World, x: Int, y: Int, z: Int, random: Random) {
        if (world.getBlockMetadata(x, y, z) == 0) world.setBlockMetadataWithNotify(x, y, z, 1, 3)
    }

    override fun getDrops(
        world: World,
        x: Int,
        y: Int,
        z: Int,
        metadata: Int,
        fortune: Int,
    ): ArrayList<ItemStack> =
        if (metadata == 0) arrayListOf(ItemStack(this)) else arrayListOf(ItemStack(Items.string, 2))

    @SideOnly(Side.CLIENT)
    override fun getIcon(side: Int, metadata: Int): IIcon =
        Blocks.leaves.getIcon(side, if (metadata == 0) 0 else 2)
}
