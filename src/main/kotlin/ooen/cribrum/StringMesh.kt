package ooen.cribrum

import com.gtnewhorizon.gtnhlib.client.renderer.cel.api.util.ColorARGB
import com.gtnewhorizon.gtnhlib.util.ResourceUtil
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import java.awt.image.BufferedImage
import java.io.IOException
import javax.imageio.ImageIO
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.resources.IResourceManager
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.IIcon
import net.minecraft.util.ResourceLocation

private const val SIZE = 16

internal class StringMesh : Item() {
    private lateinit var icons: List<IIcon>

    init {
        setUnlocalizedName("ooen.string_mesh")
        setTextureName("ooen_cribrum:string_mesh")
        setHasSubtypes(true)
    }

    override fun getUnlocalizedName(stack: ItemStack): String {
        val tier = MeshTier.entries.getOrNull(stack.itemDamage) ?: MeshTier.STRING
        return "item.ooen.${tier.key}"
    }

    @SideOnly(Side.CLIENT)
    override fun getSubItems(item: Item, tab: CreativeTabs?, items: MutableList<ItemStack>) {
        for (tier in MeshTier.entries) {
            if (tier.isAvailable()) items.add(ItemStack(item, 1, tier.ordinal))
        }
    }

    @SideOnly(Side.CLIENT)
    override fun registerIcons(register: IIconRegister) {
        icons = MeshTier.entries.map { registerMeshIcon(register, "minecraft:string", it) }
        itemIcon = icons.first()
    }

    @SideOnly(Side.CLIENT)
    override fun getIconFromDamage(metadata: Int): IIcon = icons.getOrNull(metadata) ?: itemIcon
}

@SideOnly(Side.CLIENT)
internal fun registerMeshIcon(register: IIconRegister, fallback: String, tier: MeshTier): IIcon {
    if (register !is TextureMap) return register.registerIcon(fallback)
    val name = "ooen_cribrum:${tier.key}"
    return register.getTextureExtry(name)
        ?: MeshSprite(name, tier.color).also { register.setTextureEntry(name, it) }
}

@SideOnly(Side.CLIENT)
private class MeshSprite(name: String, private val tint: Int) : TextureAtlasSprite(name) {
    override fun hasCustomLoader(manager: IResourceManager, location: ResourceLocation): Boolean =
        true

    override fun load(manager: IResourceManager, location: ResourceLocation): Boolean {
        val wool =
            try {
                manager
                    .getResource(
                        ResourceUtil.getCompleteBlockTextureResourceLocation(
                            "minecraft:wool_colored_white"
                        )
                    )
                    .inputStream
                    .use { ImageIO.read(it) }
            } catch (exception: IOException) {
                null
            }
        val icon = BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB)
        val tintRed = ColorARGB.unpackRed(tint)
        val tintGreen = ColorARGB.unpackGreen(tint)
        val tintBlue = ColorARGB.unpackBlue(tint)
        for (y in 2..13) {
            for (x in 2..13) {
                if (x != 13 && y != 13 && (x - 2) % 3 != 0 && (y - 2) % 3 != 0) continue
                val color =
                    wool?.getRGB(x * wool.width / SIZE, y * wool.height / SIZE)
                        ?: 0xFFE5E0D5.toInt()
                val red = ColorARGB.unpackRed(color) * tintRed / 255
                val green = ColorARGB.unpackGreen(color) * tintGreen / 255
                val blue = ColorARGB.unpackBlue(color) * tintBlue / 255
                icon.setRGB(x, y, ColorARGB.pack(red, green, blue, ColorARGB.unpackAlpha(color)))
            }
        }
        loadSprite(arrayOf(icon), null, false)
        // Forge adds custom sprites to the atlas when the loader returns false.
        return false
    }
}
