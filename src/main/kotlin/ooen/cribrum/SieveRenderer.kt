package ooen.cribrum

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.init.Blocks
import ooen.client.OpenBlockRenderer
import ooen.client.OpenBlockRenderer.Part
import ooen.client.OpenBlockRenderer.Surface

@SideOnly(Side.CLIENT)
internal object SieveRenderer {
    fun register(): Int =
        OpenBlockRenderer.register(frame, INNER_MIN, INNER_MAX) { block, world, x, y, z, metadata ->
            when (metadata) {
                FRAME -> null
                MESH ->
                    Surface(
                        (block as Sieve).installedMeshIcon(world, x, y, z),
                        MESH_HEIGHT,
                        doubleSided = true,
                    )
                else -> Surface(Blocks.dirt.getIcon(1, 0), DIRT_HEIGHT, doubleSided = true)
            }
        }

    private const val OUTER_MIN = 1.0 / 16
    private const val OUTER_MAX = 15.0 / 16
    private const val INNER_MIN = 3.0 / 16
    private const val INNER_MAX = 13.0 / 16
    private const val RIM_BASE = 9.0 / 16
    private const val RIM_TOP = 12.0 / 16
    private const val MESH_HEIGHT = 10.0 / 16
    private const val DIRT_HEIGHT = 11.0 / 16

    private val frame =
        listOf(
            Part(OUTER_MIN, 0.0, OUTER_MIN, INNER_MIN, RIM_BASE, INNER_MIN),
            Part(INNER_MAX, 0.0, OUTER_MIN, OUTER_MAX, RIM_BASE, INNER_MIN),
            Part(OUTER_MIN, 0.0, INNER_MAX, INNER_MIN, RIM_BASE, OUTER_MAX),
            Part(INNER_MAX, 0.0, INNER_MAX, OUTER_MAX, RIM_BASE, OUTER_MAX),
            Part(OUTER_MIN, RIM_BASE, OUTER_MIN, OUTER_MAX, RIM_TOP, INNER_MIN),
            Part(OUTER_MIN, RIM_BASE, INNER_MAX, OUTER_MAX, RIM_TOP, OUTER_MAX),
            Part(OUTER_MIN, RIM_BASE, INNER_MIN, INNER_MIN, RIM_TOP, INNER_MAX),
            Part(INNER_MAX, RIM_BASE, INNER_MIN, OUTER_MAX, RIM_TOP, INNER_MAX),
        )
}
