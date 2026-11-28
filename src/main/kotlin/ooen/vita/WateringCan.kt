package ooen.vita

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import java.util.WeakHashMap
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK
import net.minecraft.util.StatCollector
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.common.IPlantable
import net.minecraftforge.fluids.FluidContainerRegistry.BUCKET_VOLUME
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.ItemFluidContainer
import ooen.ToolTier

internal class WateringCan(tier: ToolTier) :
    ItemFluidContainer(
        0,
        if (tier == ToolTier.NETHERITE) 64 * BUCKET_VOLUME else BUCKET_VOLUME shl tier.ordinal,
    ) {

    private val radius = tier.ordinal

    init {
        val id = "${tier.idPrefix}_watering_can"
        setUnlocalizedName("ooen.$id")
        setTextureName("ooen_vita:$id")
        setMaxStackSize(1)
    }

    override fun fill(container: ItemStack, resource: FluidStack?, doFill: Boolean): Int =
        if (resource != null && resource.fluid == FluidRegistry.WATER && resource.amount > 0) {
            super.fill(container, resource, doFill)
        } else 0

    override fun onItemRightClick(stack: ItemStack, world: World, player: EntityPlayer): ItemStack {
        if (world !is WorldServer) return stack
        val hit = getMovingObjectPositionFromPlayer(world, player, true) ?: return stack
        if (hit.typeOfHit != BLOCK) return stack
        val x = hit.blockX
        val y = hit.blockY
        val z = hit.blockZ
        val block = world.getBlock(x, y, z)
        if (
            (block == Blocks.water || block == Blocks.flowing_water) &&
                world.getBlockMetadata(x, y, z) == 0
        ) {
            val water = FluidStack(FluidRegistry.WATER, BUCKET_VOLUME)
            if (
                canWater(world, player, stack, x, y, z) &&
                    fill(stack, water, false) > 0 &&
                    (player.capabilities.isCreativeMode || world.setBlockToAir(x, y, z))
            ) {
                fill(stack, water, true)
            }
            return stack
        }

        val lastTick = lastWateringTick[player]
        val tick = player.ticksExisted
        if (waterAmount(stack) == 0 || (lastTick != null && tick - lastTick < WATERING_INTERVAL)) {
            return stack
        }
        val plantY = if (block == Blocks.farmland) y + 1 else y
        var watered = false
        for (targetX in x - radius..x + radius) {
            for (targetZ in z - radius..z + radius) {
                if (waterColumn(world, player, stack, targetX, plantY, targetZ)) watered = true
            }
        }
        if (watered) {
            lastWateringTick[player] = tick
            if (!player.capabilities.isCreativeMode) drain(stack, WATER_PER_USE, true)
            world.func_147487_a("splash", x + 0.5, y + 1.0, z + 0.5, 5, 0.3, 0.2, 0.3, 0.0)
        }
        return stack
    }

    private fun waterColumn(
        world: World,
        player: EntityPlayer,
        stack: ItemStack,
        x: Int,
        y: Int,
        z: Int,
    ): Boolean {
        if (!world.blockExists(x, y, z)) return false

        val soilY = y - 1
        val farmland =
            world.getBlock(x, soilY, z) == Blocks.farmland &&
                canWater(world, player, stack, x, soilY, z)
        if (farmland && world.getBlockMetadata(x, soilY, z) < 7) {
            world.setBlockMetadataWithNotify(x, soilY, z, 7, 2)
        }

        val plant = world.getBlock(x, y, z)
        if (!plant.tickRandomly || (plant !is IPlantable && plant != Blocks.cocoa)) return farmland
        var growthY = y
        if (plant == Blocks.reeds || plant == Blocks.cactus) {
            while (world.getBlock(x, growthY + 1, z) == plant) growthY++
        }
        if (!canWater(world, player, stack, x, growthY, z)) return farmland
        plant.updateTick(world, x, growthY, z, world.rand)
        return true
    }

    private fun canWater(
        world: World,
        player: EntityPlayer,
        stack: ItemStack,
        x: Int,
        y: Int,
        z: Int,
    ): Boolean = world.canMineBlock(player, x, y, z) && player.canPlayerEdit(x, y, z, 1, stack)

    private fun waterAmount(stack: ItemStack): Int = getFluid(stack)?.amount ?: 0

    override fun showDurabilityBar(stack: ItemStack): Boolean = true

    override fun getDurabilityForDisplay(stack: ItemStack): Double =
        1.0 - waterAmount(stack).toDouble() / capacity

    @SideOnly(Side.CLIENT)
    override fun addInformation(
        stack: ItemStack,
        player: EntityPlayer,
        tooltip: MutableList<String>,
        advanced: Boolean,
    ) {
        val width = radius * 2 + 1
        tooltip.add(
            EnumChatFormatting.GRAY.toString() +
                StatCollector.translateToLocalFormatted(
                    "ooen.watering_can.water",
                    waterAmount(stack),
                    capacity,
                )
        )
        tooltip.add(
            EnumChatFormatting.GRAY.toString() +
                StatCollector.translateToLocalFormatted("ooen.watering_can.area", width, width)
        )
    }

    @SideOnly(Side.CLIENT)
    override fun getSubItems(item: Item, tab: CreativeTabs?, items: MutableList<ItemStack>) {
        if (creativeTab != null) super.getSubItems(item, tab, items)
    }

    private companion object {
        const val WATERING_INTERVAL = 4
        const val WATER_PER_USE = 1
        val lastWateringTick = WeakHashMap<EntityPlayer, Int>()
    }
}
