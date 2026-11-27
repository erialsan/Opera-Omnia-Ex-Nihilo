package ooen.cribrum

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ChatComponentTranslation
import net.minecraft.util.IIcon
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import ooen.fundamentum.VanillaWood

private const val FRAME = 0
internal const val MESH = 1
private const val LOADED = 2
private const val LAST_PASS = 4

internal class Sieve(private val mesh: Item, private val wood: VanillaWood) : Block(Material.wood) {
    private lateinit var meshIcons: List<IIcon>

    init {
        setBlockName("ooen.sieve" + wood.suffix)
        setHardness(1.5f)
        setStepSound(soundTypeWood)
        setBlockBounds(0f, 0f, 0f, 1f, 0.75f, 1f)
    }

    override fun isOpaqueCube(): Boolean = false

    override fun renderAsNormalBlock(): Boolean = false

    override fun getRenderType(): Int = Cribrum.sieveRenderType

    override fun canSilkHarvest(): Boolean = false

    override fun hasTileEntity(metadata: Int): Boolean = true

    override fun createTileEntity(world: World, metadata: Int): SieveMeshData = SieveMeshData()

    override fun removedByPlayer(
        world: World,
        player: EntityPlayer,
        x: Int,
        y: Int,
        z: Int,
        willHarvest: Boolean,
    ): Boolean = willHarvest || super.removedByPlayer(world, player, x, y, z, false)

    override fun harvestBlock(
        world: World,
        player: EntityPlayer,
        x: Int,
        y: Int,
        z: Int,
        metadata: Int,
    ) {
        // Keep the tile available until Forge has collected the installed mesh.
        super.harvestBlock(world, player, x, y, z, metadata)
        world.setBlockToAir(x, y, z)
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
        val stage = world.getBlockMetadata(x, y, z)
        val held = player.currentEquippedItem
        if (stage in MESH..LAST_PASS && player.isSneaking && held == null) {
            val tier = installedTier(world, x, y, z)
            if (!world.isRemote && world.setBlockMetadataWithNotify(x, y, z, FRAME, 3)) {
                val recovered = ItemStack(mesh, 1, tier.ordinal)
                (world.getTileEntity(x, y, z) as? SieveMeshData)?.install(MeshTier.STRING)
                if (!player.inventory.addItemStackToInventory(recovered)) {
                    dropBlockAsItem(world, x, y, z, recovered)
                }
                if (stage >= LOADED) {
                    val dirt = ItemStack(Blocks.dirt)
                    if (!player.inventory.addItemStackToInventory(dirt)) {
                        dropBlockAsItem(world, x, y, z, dirt)
                    }
                }
            }
            return true
        }
        val inserted = if (held?.item == mesh) MeshTier.entries.getOrNull(held.itemDamage) else null
        val meshData = if (stage == FRAME) world.getTileEntity(x, y, z) as? SieveMeshData else null
        val next =
            when (stage) {
                FRAME ->
                    if (inserted?.isAvailable() == true && meshData != null) MESH else return false
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
                FRAME -> {
                    meshData!!.install(inserted!!)
                    if (!player.capabilities.isCreativeMode) held!!.stackSize--
                }
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
            if (metadata >= MESH) add(ItemStack(mesh, 1, installedTier(world, x, y, z).ordinal))
            if (metadata >= LOADED) add(ItemStack(Blocks.dirt))
        }

    @SideOnly(Side.CLIENT)
    override fun registerBlockIcons(register: IIconRegister) {
        meshIcons =
            MeshTier.entries.map { registerMeshIcon(register, "minecraft:wool_colored_white", it) }
    }

    @SideOnly(Side.CLIENT)
    override fun getIcon(side: Int, metadata: Int): IIcon =
        Blocks.planks.getIcon(side, wood.metadata)

    @SideOnly(Side.CLIENT)
    fun surfaceIcon(world: IBlockAccess, x: Int, y: Int, z: Int, metadata: Int): IIcon? =
        when {
            metadata == FRAME -> null
            metadata == MESH -> meshIcons[installedTier(world, x, y, z).ordinal]
            else -> Blocks.dirt.getIcon(1, 0)
        }

    private fun installedTier(world: IBlockAccess, x: Int, y: Int, z: Int): MeshTier =
        (world.getTileEntity(x, y, z) as? SieveMeshData)?.tier ?: MeshTier.STRING
}
