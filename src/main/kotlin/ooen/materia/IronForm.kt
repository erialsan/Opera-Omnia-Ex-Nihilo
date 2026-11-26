package ooen.materia

import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary

internal enum class IronForm(val oreKey: String) {
    INGOT("ingotIron"),
    GRAVEL("ooenOreGravelIron"),
    SAND("ooenOreSandIron"),
    POWDER("ooenOrePowderIron");

    fun candidates(): List<ItemStack> =
        OreDictionary.getOres(oreKey, false).mapNotNull { stack ->
            // A wildcard matches recipe inputs but cannot identify a concrete output.
            if (stack.itemDamage == OreDictionary.WILDCARD_VALUE) null
            else stack.copy().apply { stackSize = 1 }
        }
}
