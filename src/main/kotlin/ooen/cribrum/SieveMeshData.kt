package ooen.cribrum

import net.minecraft.block.Block
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.S35PacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

internal class SieveMeshData : TileEntity() {
    var tier: MeshTier = MeshTier.STRING
        private set

    fun install(tier: MeshTier) {
        if (this.tier == tier) return
        this.tier = tier
        markDirty()
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
    }

    override fun writeToNBT(tag: NBTTagCompound) {
        super.writeToNBT(tag)
        tag.setInteger("MeshTier", tier.ordinal)
    }

    override fun readFromNBT(tag: NBTTagCompound) {
        super.readFromNBT(tag)
        tier = MeshTier.entries.getOrNull(tag.getInteger("MeshTier")) ?: MeshTier.STRING
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

    override fun shouldRefresh(
        oldBlock: Block,
        newBlock: Block,
        oldMeta: Int,
        newMeta: Int,
        world: World,
        x: Int,
        y: Int,
        z: Int,
    ): Boolean = oldBlock != newBlock
}
