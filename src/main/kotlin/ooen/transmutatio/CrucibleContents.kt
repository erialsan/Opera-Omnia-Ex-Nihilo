package ooen.transmutatio

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.S35PacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.fluids.FluidContainerRegistry.BUCKET_VOLUME

internal const val CRUCIBLE_CAPACITY = 4 * BUCKET_VOLUME

internal class CrucibleContents : TileEntity() {
    private var pending = 0
    var fluidAmount = 0
        private set

    val total: Int
        get() = pending + fluidAmount

    fun addInput(): Boolean {
        if (total + INPUT_VOLUME > CRUCIBLE_CAPACITY) return false
        pending += INPUT_VOLUME
        changes(sync = true)
        return true
    }

    fun takeBucket(): Boolean {
        if (fluidAmount < BUCKET_VOLUME) return false
        fluidAmount -= BUCKET_VOLUME
        changes(sync = true)
        return true
    }

    override fun updateEntity() {
        if (worldObj.isRemote || pending == 0) return
        val crucible = blockType as? Crucible ?: return
        val amount = minOf(pending, crucible.rate(worldObj, xCoord, yCoord, zCoord))
        if (amount == 0) return
        val wasEmpty = fluidAmount == 0
        pending -= amount
        fluidAmount += amount
        changes(sync = wasEmpty || pending == 0 || worldObj.totalWorldTime % 5L == 0L)
    }

    private fun changes(sync: Boolean) {
        markDirty()
        if (sync) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
    }

    override fun writeToNBT(tag: NBTTagCompound) {
        super.writeToNBT(tag)
        tag.setInteger("Pending", pending)
        tag.setInteger("FluidAmount", fluidAmount)
    }

    override fun readFromNBT(tag: NBTTagCompound) {
        super.readFromNBT(tag)
        fluidAmount = tag.getInteger("FluidAmount").coerceIn(0, CRUCIBLE_CAPACITY)
        pending = tag.getInteger("Pending").coerceIn(0, CRUCIBLE_CAPACITY - fluidAmount)
    }

    override fun getDescriptionPacket(): S35PacketUpdateTileEntity {
        val tag = NBTTagCompound()
        writeToNBT(tag)
        return S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag)
    }

    override fun onDataPacket(net: NetworkManager, packet: S35PacketUpdateTileEntity) {
        readFromNBT(packet.func_148857_g())
        worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord)
    }

    private companion object {
        const val INPUT_VOLUME = BUCKET_VOLUME / 4
    }
}
