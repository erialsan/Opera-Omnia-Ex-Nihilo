package ooen.materia

import com.gtnewhorizon.gtnhlib.util.ItemUtil
import com.gtnewhorizon.gtnhlib.util.data.ItemId
import cpw.mods.fml.common.Loader
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import ooen.concreteOreStacks
import ooen.netheriteIngots
import thelm.jaopca.api.JAOPCAApi
import thelm.jaopca.api.materials.IMaterial

internal enum class MetalForm(private val orePrefix: String, val texture: String) {
    INGOT("ingot", "ingot"),
    GRAVEL("ooenOreGravel", "ore_gravel"),
    SAND("ooenOreSand", "ore_sand"),
    POWDER("ooenOrePowder", "ore_dust"),
    SMALL_GRAVEL("ooenSmallOreGravel", "small_ore_gravel"),
    SMALL_SAND("ooenSmallOreSand", "small_ore_sand"),
    SMALL_POWDER("ooenSmallOrePowder", "small_ore_dust");

    fun oreKey(metal: String): String = "$orePrefix$metal"
}

internal object MetalCatalog {
    private var allowedMetals: Set<String> = emptySet()

    fun configure(whitelist: Array<String>) {
        allowedMetals = whitelist.mapTo(mutableSetOf()) { it.trim() }
    }

    @JvmStatic
    fun names(): List<String> {
        if (!Loader.isModLoaded("jaopca")) return allowedMetals.filter { ingots(it).any() }.sorted()
        val names = JaopcaMetals.names(allowedMetals)
        if ("Netherite" !in allowedMetals || netheriteIngots().isEmpty()) return names
        return (names + "Netherite").distinct().sorted()
    }

    @JvmStatic
    fun candidates(metal: String, form: MetalForm): List<ItemStack> {
        if (Loader.isModLoaded("jaopca")) {
            val candidates = JaopcaMetals.candidates(metal, form, allowedMetals)
            if (candidates.isNotEmpty() || metal != "Netherite") return candidates
        }
        if (metal !in allowedMetals) return emptyList()
        val ingots = ingots(metal)
        if (form == MetalForm.INGOT) return ingots.copies()
        return if (ingots.any()) stacks(sequenceOf(metal), form).copies() else emptyList()
    }

    fun color(metal: String): Int? =
        if (Loader.isModLoaded("jaopca")) JaopcaMetals.color(metal) else null

    private fun ingots(metal: String): Sequence<ItemStack> =
        if (metal == "Netherite") netheriteIngots().asSequence()
        else stacks(sequenceOf(metal), MetalForm.INGOT)
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
        return material.stacks(form).copies()
    }

    fun color(metal: String): Int? =
        if (JAOPCAApi.initialized()) JAOPCAApi.instance().getMaterial(metal)?.color else null

    private fun IMaterial.isAllowedMetal(allowedMetals: Set<String>): Boolean =
        name in allowedMetals && type.isIngot && stacks(MetalForm.INGOT).any()

    private fun IMaterial.stacks(form: MetalForm): Sequence<ItemStack> =
        stacks(sequenceOf(name) + alternativeNames.asSequence(), form)
}

private fun stacks(metals: Sequence<String>, form: MetalForm): Sequence<ItemStack> =
    metals.flatMap { metal ->
        concreteOreStacks(form.oreKey(metal))
            // Forge registers both vanilla brick items under ingot keys.
            .filter {
                form != MetalForm.INGOT || (it.item != Items.brick && it.item != Items.netherbrick)
            }
    }

private fun Sequence<ItemStack>.copies(): List<ItemStack> =
    distinctBy { ItemId.createNoCopy(it.item, it.itemDamage, it.tagCompound) }
        .mapNotNull { ItemUtil.copyAmount(1, it) }
        .toList()
