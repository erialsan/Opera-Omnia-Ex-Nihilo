package ooen.fundamentum

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.init.Blocks
import net.minecraft.world.ColorizerFoliage
import ooen.client.OpenBlockRenderer
import ooen.client.OpenBlockRenderer.Part
import ooen.client.OpenBlockRenderer.Surface

@SideOnly(Side.CLIENT)
internal object BarrelRenderer {
    fun register(): Int =
        OpenBlockRenderer.register(parts, INNER_MIN, INNER_MAX) { _, _, _, _, _, fill ->
            val contents = barrelSurfaceBlock(fill)
            val height =
                FLOOR_TOP +
                    (FILL_TOP - FLOOR_TOP) * fill.coerceAtMost(COMPOST_CAPACITY) / COMPOST_CAPACITY
            when {
                fill == 0 -> null
                contents != null -> Surface(contents.getIcon(1, 0), height)
                else ->
                    Surface(
                        Blocks.leaves.getIcon(1, 0),
                        height,
                        ColorizerFoliage.getFoliageColorBasic(),
                    )
            }
        }

    private const val OUTER_MIN = 2.0 / 16
    private const val OUTER_MAX = 14.0 / 16
    private const val INNER_MIN = 3.0 / 16
    private const val INNER_MAX = 13.0 / 16
    private const val FLOOR_TOP = 2.0 / 16
    private const val FILL_TOP = 15.0 / 16

    private val parts =
        listOf(
            Part(OUTER_MIN, 0.0, OUTER_MIN, OUTER_MAX, FLOOR_TOP, OUTER_MAX),
            Part(OUTER_MIN, FLOOR_TOP, OUTER_MIN, OUTER_MAX, 1.0, INNER_MIN),
            Part(OUTER_MIN, FLOOR_TOP, INNER_MAX, OUTER_MAX, 1.0, OUTER_MAX),
            Part(OUTER_MIN, FLOOR_TOP, INNER_MIN, INNER_MIN, 1.0, INNER_MAX),
            Part(INNER_MAX, FLOOR_TOP, INNER_MIN, OUTER_MAX, 1.0, INNER_MAX),
        )
}
