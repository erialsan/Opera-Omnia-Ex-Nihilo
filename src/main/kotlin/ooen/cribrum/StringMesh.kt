package ooen.cribrum

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import java.awt.image.BufferedImage
import java.io.IOException
import javax.imageio.ImageIO
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.resources.IResourceManager
import net.minecraft.item.Item
import net.minecraft.util.IIcon
import net.minecraft.util.ResourceLocation

private const val TEXTURE_NAME = "ooen_cribrum:string_mesh"
private const val SIZE = 16

internal class StringMesh : Item() {
    init {
        setTextureName(TEXTURE_NAME)
    }

    @SideOnly(Side.CLIENT)
    override fun registerIcons(register: IIconRegister) {
        itemIcon = registerMeshIcon(register, "minecraft:string")
    }
}

@SideOnly(Side.CLIENT)
internal fun registerMeshIcon(register: IIconRegister, fallback: String): IIcon {
    if (register !is TextureMap) return register.registerIcon(fallback)
    return register.getTextureExtry(TEXTURE_NAME)
        ?: MeshSprite().also { register.setTextureEntry(TEXTURE_NAME, it) }
}

@SideOnly(Side.CLIENT)
private class MeshSprite : TextureAtlasSprite(TEXTURE_NAME) {
    override fun hasCustomLoader(manager: IResourceManager, location: ResourceLocation): Boolean =
        true

    override fun load(manager: IResourceManager, location: ResourceLocation): Boolean {
        val wool =
            try {
                manager
                    .getResource(
                        ResourceLocation("minecraft", "textures/blocks/wool_colored_white.png")
                    )
                    .inputStream
                    .use { ImageIO.read(it) }
            } catch (exception: IOException) {
                null
            }
        val icon = BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB)
        for (y in 2..13) {
            for (x in 2..13) {
                if (x != 13 && y != 13 && (x - 2) % 3 != 0 && (y - 2) % 3 != 0) continue
                val color =
                    wool?.getRGB(x * wool.width / SIZE, y * wool.height / SIZE)
                        ?: 0xFFE5E0D5.toInt()
                icon.setRGB(x, y, color)
            }
        }
        loadSprite(arrayOf(icon), null, false)
        // Forge adds custom sprites to the atlas when the loader returns false.
        return false
    }
}
