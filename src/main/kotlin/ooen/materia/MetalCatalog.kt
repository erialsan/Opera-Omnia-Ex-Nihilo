package ooen.materia

import com.gtnewhorizon.gtnhlib.util.ItemUtil
import com.gtnewhorizon.gtnhlib.util.data.ItemId
import cpw.mods.fml.common.Loader
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary
import thelm.jaopca.api.JAOPCAApi
import thelm.jaopca.api.materials.IMaterial

internal enum class MetalForm(private val orePrefix: String) {
    INGOT("ingot"),
    GRAVEL("ooenOreGravel"),
    SAND("ooenOreSand"),
    POWDER("ooenOrePowder");

    fun oreKey(metal: String): String = "$orePrefix$metal"
}

internal object MetalCatalog {
    private var allowedMetals: Set<String> = emptySet()

    fun configure(whitelist: Array<String>) {
        allowedMetals = whitelist.mapTo(mutableSetOf()) { it.trim() }
    }

    @JvmStatic
    fun names(): List<String> =
        if (Loader.isModLoaded("jaopca")) JaopcaMetals.names(allowedMetals) else emptyList()

    @JvmStatic
    fun candidates(metal: String, form: MetalForm): List<ItemStack> =
        if (Loader.isModLoaded("jaopca")) {
            JaopcaMetals.candidates(metal, form, allowedMetals)
        } else emptyList()
}

// Keep references to the optional API out of MetalCatalog's class.
private object JaopcaMetals {
    fun names(allowedMetals: Set<String>): List<String> {
        if (!JAOPCAApi.initialized()) return emptyList()
        return JAOPCAApi.instance()
            .materials
            .filter { it.isAllowedMetal(allowedMetals) }
            .map { it.name }
            .sorted()
    }

    fun candidates(metal: String, form: MetalForm, allowedMetals: Set<String>): List<ItemStack> {
        if (!JAOPCAApi.initialized()) return emptyList()
        val material = JAOPCAApi.instance().getMaterial(metal) ?: return emptyList()
        if (!material.isAllowedMetal(allowedMetals)) return emptyList()
        return material
            .stacks(form)
            .distinctBy { ItemId.createNoCopy(it.item, it.itemDamage, it.tagCompound) }
            .mapNotNull { ItemUtil.copyAmount(1, it) }
            .toList()
    }

    private fun IMaterial.isAllowedMetal(allowedMetals: Set<String>): Boolean =
        name in allowedMetals && type.isIngot && stacks(MetalForm.INGOT).any()

    private fun IMaterial.stacks(form: MetalForm): Sequence<ItemStack> =
        (sequenceOf(name) + alternativeNames.asSequence()).flatMap { metal ->
            OreDictionary.getOres(form.oreKey(metal), false)
                .asSequence()
                .filterNot(ItemUtil::isStackInvalid)
                // Wildcards match inputs but do not identify concrete output stacks.
                .filter { it.itemDamage != OreDictionary.WILDCARD_VALUE }
                // Forge registers both vanilla brick items under ingot keys.
                .filter {
                    form != MetalForm.INGOT ||
                        (it.item != Items.brick && it.item != Items.netherbrick)
                }
        }
}
