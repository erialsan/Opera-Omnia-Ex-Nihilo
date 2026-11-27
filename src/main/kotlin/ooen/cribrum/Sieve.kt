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

internal const val FRAME = 0
internal const val MESH = 1
private const val LOADED = 2
private const val LAST_PASS = 4

internal class Sieve(private val mesh: Item, private val wood: VanillaWood) : Block(Material.wood) {
    private lateinit var meshIcons: List<IIcon>

    init {
        setBlockName("ooen.sieve${wood.suffix}")
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
        fun changeStage(next: Int): Boolean =
            !world.isRemote && world.setBlockMetadataWithNotify(x, y, z, next, 3)

        if (stage in MESH..LAST_PASS && player.isSneaking && held == null) {
            val tier = installedTier(world, x, y, z)
            if (changeStage(FRAME)) {
                meshData(world, x, y, z)?.install(MeshTier.STRING)
                for (stack in contents(stage, tier)) {
                    if (!player.inventory.addItemStackToInventory(stack)) {
                        dropBlockAsItem(world, x, y, z, stack)
                    }
                }
            }
            return true
        }
        when (stage) {
            FRAME -> {
                if (held == null || held.item != mesh) return false
                val tier = MeshTier.entries.getOrNull(held.itemDamage) ?: return false
                if (!tier.isAvailable()) return false
                val data = meshData(world, x, y, z) ?: return false
                if (changeStage(MESH)) {
                    data.install(tier)
                    if (!player.capabilities.isCreativeMode) held.stackSize--
                }
            }
            MESH -> {
                if (
                    held == null ||
                        held.item != Item.getItemFromBlock(Blocks.dirt) ||
                        held.itemDamage != 0
                ) {
                    return false
                }
                if (changeStage(LOADED) && !player.capabilities.isCreativeMode) held.stackSize--
            }
            in LOADED until LAST_PASS -> {
                val next = stage + 1
                if (changeStage(next)) {
                    player.addChatComponentMessage(
                        ChatComponentTranslation(
                            "ooen.sieve.progress",
                            next - LOADED,
                            LAST_PASS - LOADED + 1,
                        )
                    )
                }
            }
            LAST_PASS -> {
                if (changeStage(MESH)) dropBlockAsItem(world, x, y, z, ItemStack(Items.wheat_seeds))
            }
            else -> return false
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
            if (metadata >= MESH) addAll(contents(metadata, installedTier(world, x, y, z)))
        }

    private fun contents(stage: Int, tier: MeshTier): List<ItemStack> = buildList {
        add(ItemStack(mesh, 1, tier.ordinal))
        if (stage >= LOADED) add(ItemStack(Blocks.dirt))
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
    fun installedMeshIcon(world: IBlockAccess, x: Int, y: Int, z: Int): IIcon =
        meshIcons[installedTier(world, x, y, z).ordinal]

    private fun installedTier(world: IBlockAccess, x: Int, y: Int, z: Int): MeshTier =
        meshData(world, x, y, z)?.tier ?: MeshTier.STRING

    private fun meshData(world: IBlockAccess, x: Int, y: Int, z: Int): SieveMeshData? =
        world.getTileEntity(x, y, z) as? SieveMeshData
}
