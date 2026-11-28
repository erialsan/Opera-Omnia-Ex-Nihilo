package ooen.cribrum

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
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
private const val SIEVE_RADIUS = 2

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

        if (stage >= MESH && player.isSneaking && held == null) {
            val contents = contents(stage, meshData(world, x, y, z))
            if (changeStage(FRAME)) {
                meshData(world, x, y, z)?.setMesh(MeshTier.STRING)
                for (stack in contents) {
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
                    data.setMesh(tier, held)
                    if (!player.capabilities.isCreativeMode) held.stackSize--
                }
            }
            MESH -> {
                if (held == null) return false
                val data = meshData(world, x, y, z) ?: return false
                val input = SieveRecipes.inputFor(held, data.tier) ?: return false
                if (!world.isRemote) loadArea(world, x, y, z, data, input, held, player)
            }
            else -> {
                val data = meshData(world, x, y, z) ?: return false
                if (!world.isRemote) siftArea(world, x, y, z, data, player)
            }
        }
        return true
    }

    private fun loadArea(
        world: World,
        x: Int,
        y: Int,
        z: Int,
        center: SieveMeshData,
        input: Block,
        held: ItemStack,
        player: EntityPlayer,
    ) {
        fun load(sieveX: Int, sieveZ: Int, data: SieveMeshData): Boolean {
            if (!world.setBlockMetadataWithNotify(sieveX, y, sieveZ, LOADED, 3)) return false
            data.updateInput(input)
            if (!player.capabilities.isCreativeMode) held.stackSize--
            return true
        }

        if (!load(x, z, center)) return
        forEachNeighbor(world, x, y, z) { sieveX, sieveZ, data ->
            if (
                held.stackSize > 0 &&
                    data.tier == center.tier &&
                    world.getBlockMetadata(sieveX, y, sieveZ) == MESH
            ) {
                load(sieveX, sieveZ, data)
            }
        }
    }

    private fun siftArea(
        world: World,
        x: Int,
        y: Int,
        z: Int,
        center: SieveMeshData,
        player: EntityPlayer,
    ) {
        val input = loadedInput(center)
        val progress = center.progress
        val centerProgress = sift(world, x, y, z, center, input)
        forEachNeighbor(world, x, y, z) { sieveX, sieveZ, data ->
            if (
                world.getBlockMetadata(sieveX, y, sieveZ) >= LOADED &&
                    data.tier == center.tier &&
                    data.progress == progress &&
                    loadedInput(data) == input
            ) {
                sift(world, sieveX, y, sieveZ, data, input)
            }
        }
        if (centerProgress != null && centerProgress < 100) {
            player.addChatComponentMessage(
                ChatComponentTranslation("ooen.sieve.progress", centerProgress, 100)
            )
        }
    }

    private fun sift(
        world: World,
        x: Int,
        y: Int,
        z: Int,
        data: SieveMeshData,
        input: Block,
    ): Int? {
        val installedMesh = data.meshStack(mesh)
        val efficiency =
            EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, installedMesh)
                .coerceIn(0, Enchantment.efficiency.maxLevel)
        if (!data.advance(10 + 5 * efficiency)) return null
        val progress = data.progress
        if (progress == 100 && world.setBlockMetadataWithNotify(x, y, z, MESH, 3)) {
            val fortune =
                EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, installedMesh)
            data.updateInput(null)
            for (stack in SieveRecipes.roll(input, data.tier, world.rand, fortune)) {
                dropBlockAsItem(world, x, y, z, stack)
            }
        }
        return progress
    }

    private inline fun forEachNeighbor(
        world: World,
        x: Int,
        y: Int,
        z: Int,
        visit: (Int, Int, SieveMeshData) -> Unit,
    ) {
        for (offsetX in -SIEVE_RADIUS..SIEVE_RADIUS) {
            for (offsetZ in -SIEVE_RADIUS..SIEVE_RADIUS) {
                if (offsetX == 0 && offsetZ == 0) continue
                val sieveX = x + offsetX
                val sieveZ = z + offsetZ
                if (
                    !world.blockExists(sieveX, y, sieveZ) ||
                        world.getBlock(sieveX, y, sieveZ) !is Sieve
                )
                    continue
                val data = meshData(world, sieveX, y, sieveZ) ?: continue
                visit(sieveX, sieveZ, data)
            }
        }
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
            if (metadata >= MESH) addAll(contents(metadata, meshData(world, x, y, z)))
        }

    private fun contents(stage: Int, data: SieveMeshData?): List<ItemStack> = buildList {
        add(data?.meshStack(mesh) ?: ItemStack(mesh))
        if (stage >= LOADED) add(ItemStack(loadedInput(data)))
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
        meshIcons[(meshData(world, x, y, z)?.tier ?: MeshTier.STRING).ordinal]

    @SideOnly(Side.CLIENT)
    fun loadedInputIcon(world: IBlockAccess, x: Int, y: Int, z: Int): IIcon =
        loadedInput(meshData(world, x, y, z)).getIcon(1, 0)

    // Older saves only recorded the mesh tier. Every loaded sieve then held dirt.
    private fun loadedInput(data: SieveMeshData?): Block = data?.input ?: Blocks.dirt

    private fun meshData(world: IBlockAccess, x: Int, y: Int, z: Int): SieveMeshData? =
        world.getTileEntity(x, y, z) as? SieveMeshData
}
