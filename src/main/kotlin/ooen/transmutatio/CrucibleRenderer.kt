package ooen.transmutatio

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.init.Blocks
import net.minecraft.world.ColorizerFoliage
import ooen.client.OpenBlockRenderer
import ooen.client.OpenBlockRenderer.Part
import ooen.client.OpenBlockRenderer.Surface

@SideOnly(Side.CLIENT)
internal object CrucibleRenderer {
    fun register(): Int =
        OpenBlockRenderer.register(parts, INNER_MIN, INNER_MAX) { block, world, x, y, z, _ ->
            val contents = world.getTileEntity(x, y, z) as? CrucibleContents
            if (block !is Crucible || contents == null || contents.total == 0) null
            else {
                val height =
                    (BASE + (TOP - BASE) * contents.total / CRUCIBLE_CAPACITY).coerceAtMost(
                        TOP - 1.0 / 64
                    )
                val surface =
                    if (contents.fluidAmount > 0) {
                        if (block.isWooden) Blocks.water else Blocks.lava
                    } else if (block.isWooden) Blocks.leaves else Blocks.cobblestone
                val color =
                    if (surface == Blocks.leaves) ColorizerFoliage.getFoliageColorBasic()
                    else 0xFFFFFF
                Surface(surface.getIcon(1, 0), height, color)
            }
        }

    private const val OUTER_MIN = 2.0 / 16
    private const val OUTER_MAX = 14.0 / 16
    private const val INNER_MIN = 4.0 / 16
    private const val INNER_MAX = 12.0 / 16
    private const val FOOT = 2.0 / 16
    private const val BASE = 4.0 / 16
    private const val TOP = 13.0 / 16

    private val parts =
        listOf(
            Part(INNER_MIN, 0.0, INNER_MIN, INNER_MAX, FOOT, INNER_MAX),
            Part(OUTER_MIN, FOOT, OUTER_MIN, OUTER_MAX, BASE, OUTER_MAX),
            Part(OUTER_MIN, BASE, OUTER_MIN, OUTER_MAX, TOP, INNER_MIN),
            Part(OUTER_MIN, BASE, INNER_MAX, OUTER_MAX, TOP, OUTER_MAX),
            Part(OUTER_MIN, BASE, INNER_MIN, INNER_MIN, TOP, INNER_MAX),
            Part(INNER_MAX, BASE, INNER_MIN, OUTER_MAX, TOP, INNER_MAX),
        )
}
