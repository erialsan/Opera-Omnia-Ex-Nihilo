package ooen.cribrum

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.S35PacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

internal class SieveMeshData : TileEntity() {
    var tier: MeshTier = MeshTier.STRING
        private set

    private var meshTag: NBTTagCompound? = null

    var input: Block? = null
        private set

    var progress = 0
        private set

    private var nextSiftTick = 0L

    fun setMesh(tier: MeshTier, stack: ItemStack? = null) {
        this.tier = tier
        meshTag = stack?.tagCompound?.copy() as? NBTTagCompound
        updateInput(null)
    }

    fun meshStack(item: Item): ItemStack =
        ItemStack(item, 1, tier.ordinal).also {
            it.tagCompound = meshTag?.copy() as? NBTTagCompound
        }

    fun updateInput(input: Block?) {
        this.input = input
        progress = 0
        markDirty()
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
    }

    fun advance(amount: Int): Boolean {
        val tick = worldObj.totalWorldTime
        if (tick < nextSiftTick) return false
        nextSiftTick = tick + 4
        progress = (progress + amount).coerceAtMost(100)
        markDirty()
        return true
    }

    override fun writeToNBT(tag: NBTTagCompound) {
        super.writeToNBT(tag)
        tag.setInteger("MeshTier", tier.ordinal)
        val storedMeshTag = meshTag
        if (storedMeshTag == null) tag.removeTag("MeshTag")
        else tag.setTag("MeshTag", storedMeshTag.copy())
        tag.setString(
            "SieveInput",
            input?.let { Block.blockRegistry.getNameForObject(it) }.orEmpty(),
        )
        tag.setInteger("SieveProgress", progress)
    }

    override fun readFromNBT(tag: NBTTagCompound) {
        super.readFromNBT(tag)
        tier = MeshTier.entries.getOrNull(tag.getInteger("MeshTier")) ?: MeshTier.STRING
        meshTag =
            if (tag.hasKey("MeshTag", 10)) tag.getCompoundTag("MeshTag").copy() as? NBTTagCompound
            else null
        val inputId = tag.getString("SieveInput")
        input =
            if (Block.blockRegistry.containsKey(inputId))
                Block.blockRegistry.getObject(inputId) as? Block
            else null
        progress = tag.getInteger("SieveProgress").coerceIn(0, 99)
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
