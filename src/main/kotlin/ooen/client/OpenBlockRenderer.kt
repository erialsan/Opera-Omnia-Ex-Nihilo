package ooen.client

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler
import cpw.mods.fml.client.registry.RenderingRegistry
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.block.Block
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.Tessellator
import net.minecraft.util.IIcon
import net.minecraft.world.IBlockAccess

@SideOnly(Side.CLIENT)
internal class OpenBlockRenderer
private constructor(
    private val id: Int,
    private val parts: List<Part>,
    private val innerMin: Double,
    private val innerMax: Double,
    private val surfaceAt: (Block, IBlockAccess, Int, Int, Int, Int) -> Surface?,
) : ISimpleBlockRenderingHandler {
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
        val renderAllFaces = renderer.renderAllFaces
        renderer.renderAllFaces = true
        try {
            for (part in parts) {
                part.applyTo(renderer)
                renderer.renderStandardBlock(block, x, y, z)
            }
            val metadata = world.getBlockMetadata(x, y, z)
            val surface = surfaceAt(block, world, x, y, z, metadata) ?: return true
            val height = surface.height
            renderer.setRenderBounds(innerMin, height, innerMin, innerMax, height, innerMax)
            val tessellator = Tessellator.instance
            tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z))
            val red = (surface.color shr 16 and 255) / 255f
            val green = (surface.color shr 8 and 255) / 255f
            val blue = (surface.color and 255) / 255f
            tessellator.setColorOpaque_F(red, green, blue)
            renderer.renderFaceYPos(block, x.toDouble(), y.toDouble(), z.toDouble(), surface.icon)
            if (surface.doubleSided) {
                tessellator.setColorOpaque_F(red * 0.6f, green * 0.6f, blue * 0.6f)
                renderer.renderFaceYNeg(
                    block,
                    x.toDouble(),
                    y.toDouble(),
                    z.toDouble(),
                    surface.icon,
                )
            }
            return true
        } finally {
            renderer.renderAllFaces = renderAllFaces
            renderer.setRenderBoundsFromBlock(block)
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
        for (part in parts) {
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
        fun register(
            parts: List<Part>,
            innerMin: Double,
            innerMax: Double,
            surfaceAt: (Block, IBlockAccess, Int, Int, Int, Int) -> Surface?,
        ): Int {
            val id = RenderingRegistry.getNextAvailableRenderId()
            RenderingRegistry.registerBlockHandler(
                OpenBlockRenderer(id, parts, innerMin, innerMax, surfaceAt)
            )
            return id
        }
    }

    class Surface(
        val icon: IIcon,
        val height: Double,
        val color: Int = 0xFFFFFF,
        val doubleSided: Boolean = false,
    )

    class Part(
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
