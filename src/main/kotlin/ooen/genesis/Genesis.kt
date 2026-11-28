package ooen.genesis

import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import java.util.Random
import net.minecraft.init.Blocks
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraft.world.WorldType
import net.minecraft.world.biome.BiomeGenBase
import net.minecraft.world.biome.WorldChunkManager
import net.minecraft.world.biome.WorldChunkManagerHell
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.IChunkProvider
import net.minecraft.world.gen.ChunkProviderFlat
import net.minecraft.world.gen.feature.WorldGenTrees
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.WorldEvent
import ooen.COMMON_DEPENDENCIES
import ooen.materia.Tags

private const val START_XZ = 8
private const val DIRT_Y = 64

@Mod(
    modid = "ooen_genesis",
    name = "Opera Omnia Ex Nihilo: Genesis",
    version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = COMMON_DEPENDENCIES,
)
object Genesis {
    private val skyblock = SkyblockWorldType()

    @JvmStatic @Mod.InstanceFactory private fun modInstance(): Genesis = this

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun createSpawn(event: WorldEvent.CreateSpawnPosition) {
        val world = event.world as? WorldServer ?: return
        if (world.provider.dimensionId != 0 || world.worldInfo.terrainType !== skyblock) return

        world.chunkProvider.loadChunk(START_XZ shr 4, START_XZ shr 4)
        world.setBlock(START_XZ, DIRT_Y, START_XZ, Blocks.dirt)
        check(
            WorldGenTrees(false).generate(world, Random(world.seed), START_XZ, DIRT_Y + 1, START_XZ)
        ) {
            "Could not generate the Genesis spawn tree"
        }
        val spawnY = world.getPrecipitationHeight(START_XZ, START_XZ)
        world.setSpawnLocation(START_XZ, spawnY, START_XZ)
        event.isCanceled = true
    }
}

private class SkyblockWorldType : WorldType("ooen_void") {
    override fun getChunkManager(world: World): WorldChunkManager =
        if (world.provider.dimensionId == 0) {
            WorldChunkManagerHell(BiomeGenBase.plains, BiomeGenBase.plains.floatRainfall)
        } else super.getChunkManager(world)

    override fun getChunkGenerator(world: World, generatorOptions: String): IChunkProvider =
        if (world.provider.dimensionId == 0) VoidChunkProvider(world)
        else super.getChunkGenerator(world, generatorOptions)

    // Forge 1.7.10 calls Random.nextInt(fuzz), so 1 is the zero-offset setting.
    override fun getSpawnFuzz(): Int = 1

    override fun getHorizon(world: World): Double =
        if (world.provider.dimensionId == 0) 0.0 else super.getHorizon(world)
}

private class VoidChunkProvider(world: World) :
    ChunkProviderFlat(world, world.seed, false, "2;0x0;${BiomeGenBase.plains.biomeID};") {
    override fun provideChunk(x: Int, z: Int): Chunk =
        super.provideChunk(x, z).also { chunk ->
            // Bypass Forge's population pass, including registered world generators.
            chunk.isTerrainPopulated = true
        }
}
