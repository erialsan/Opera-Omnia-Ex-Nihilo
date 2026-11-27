package ooen.cribrum

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler
import cpw.mods.fml.client.registry.RenderingRegistry
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.block.Block
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.Tessellator
import net.minecraft.world.IBlockAccess

@SideOnly(Side.CLIENT)
internal class SieveRenderer(private val id: Int) : ISimpleBlockRenderingHandler {
    override fun getRenderId(): Int = id

    override fun shouldRender3DInInventory(modelId: Int): Boolean = true

    override fun renderWorldBlock(
        world: IBlockAccess,
        x: Int,
        y: Int,
        z: Int,
        block: Block,
        modelId: Int,
        renderer: RenderBlocks,
    ): Boolean {
        val sieve = block as Sieve
        val renderAllFaces = renderer.renderAllFaces
        renderer.renderAllFaces = true
        try {
            for (part in frame) {
                part.applyTo(renderer)
                renderer.renderStandardBlock(sieve, x, y, z)
            }
            val metadata = world.getBlockMetadata(x, y, z)
            val surface = sieve.surfaceIcon(metadata) ?: return true
            val height = if (metadata == MESH) MESH_HEIGHT else DIRT_HEIGHT
            renderer.setRenderBounds(INNER_MIN, height, INNER_MIN, INNER_MAX, height, INNER_MAX)
            val tessellator = Tessellator.instance
            tessellator.setBrightness(sieve.getMixedBrightnessForBlock(world, x, y, z))
            tessellator.setColorOpaque_F(1f, 1f, 1f)
            renderer.renderFaceYPos(sieve, x.toDouble(), y.toDouble(), z.toDouble(), surface)
            tessellator.setColorOpaque_F(0.6f, 0.6f, 0.6f)
            renderer.renderFaceYNeg(sieve, x.toDouble(), y.toDouble(), z.toDouble(), surface)
            return true
        } finally {
            renderer.renderAllFaces = renderAllFaces
            renderer.setRenderBoundsFromBlock(sieve)
        }
    }

    override fun renderInventoryBlock(
        block: Block,
        metadata: Int,
        modelId: Int,
        renderer: RenderBlocks,
    ) {
        val wood = block.getIcon(0, metadata)
        val tessellator = Tessellator.instance
        val origin = -0.5
        tessellator.startDrawingQuads()
        for (part in frame) {
            part.applyTo(renderer)
            tessellator.setNormal(0f, -1f, 0f)
            renderer.renderFaceYNeg(block, origin, origin, origin, wood)
            tessellator.setNormal(0f, 1f, 0f)
            renderer.renderFaceYPos(block, origin, origin, origin, wood)
            tessellator.setNormal(0f, 0f, -1f)
            renderer.renderFaceZNeg(block, origin, origin, origin, wood)
            tessellator.setNormal(0f, 0f, 1f)
            renderer.renderFaceZPos(block, origin, origin, origin, wood)
            tessellator.setNormal(-1f, 0f, 0f)
            renderer.renderFaceXNeg(block, origin, origin, origin, wood)
            tessellator.setNormal(1f, 0f, 0f)
            renderer.renderFaceXPos(block, origin, origin, origin, wood)
        }
        tessellator.draw()
        renderer.setRenderBoundsFromBlock(block)
    }

    companion object {
        fun register(): Int {
            val id = RenderingRegistry.getNextAvailableRenderId()
            RenderingRegistry.registerBlockHandler(SieveRenderer(id))
            return id
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

    private class Part(
        private val minX: Double,
        private val minY: Double,
        private val minZ: Double,
        private val maxX: Double,
        private val maxY: Double,
        private val maxZ: Double,
    ) {
        fun applyTo(renderer: RenderBlocks) {
            renderer.setRenderBounds(minX, minY, minZ, maxX, maxY, maxZ)
        }
    }
}
