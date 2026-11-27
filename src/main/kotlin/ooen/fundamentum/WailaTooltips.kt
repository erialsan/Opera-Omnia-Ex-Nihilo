package ooen.fundamentum

import mcp.mobius.waila.api.IWailaConfigHandler
import mcp.mobius.waila.api.IWailaDataAccessor
import mcp.mobius.waila.api.IWailaDataProvider
import mcp.mobius.waila.api.IWailaRegistrar
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.StatCollector
import net.minecraft.world.World

// Waila loads this callback through IMC when it is installed.
internal object WailaTooltips : IWailaDataProvider {
    @JvmStatic
    fun register(registrar: IWailaRegistrar) {
        registrar.registerBodyProvider(this, InfestedLeaves::class.java)
        registrar.registerBodyProvider(this, CompostBarrel::class.java)
    }

    override fun getWailaBody(
        stack: ItemStack,
        tooltip: MutableList<String>,
        accessor: IWailaDataAccessor,
        config: IWailaConfigHandler,
    ): MutableList<String> {
        val metadata = accessor.metadata
        when (val block = accessor.block) {
            is InfestedLeaves ->
                tooltip.add(
                    StatCollector.translateToLocalFormatted(
                        "ooen.infested_leaves.progress",
                        block.progressPercent(metadata),
                    )
                )
            is CompostBarrel -> tooltip.add(block.compostStatus(metadata))
        }
        return tooltip
    }

    override fun getWailaStack(
        accessor: IWailaDataAccessor,
        config: IWailaConfigHandler,
    ): ItemStack? = null

    override fun getWailaHead(
        stack: ItemStack,
        tooltip: MutableList<String>,
        accessor: IWailaDataAccessor,
        config: IWailaConfigHandler,
    ): MutableList<String> = tooltip

    override fun getWailaTail(
        stack: ItemStack,
        tooltip: MutableList<String>,
        accessor: IWailaDataAccessor,
        config: IWailaConfigHandler,
    ): MutableList<String> = tooltip

    override fun getNBTData(
        player: EntityPlayerMP,
        tile: TileEntity?,
        tag: NBTTagCompound,
        world: World,
        x: Int,
        y: Int,
        z: Int,
    ): NBTTagCompound = tag
}
