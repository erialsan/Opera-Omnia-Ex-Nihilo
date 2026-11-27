package ooen

import com.gtnewhorizon.gtnhlib.util.ItemUtil
import com.gtnewhorizon.gtnhlib.util.data.ItemId
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary

internal fun concreteOreStacks(key: String): Sequence<ItemStack> =
    OreDictionary.getOres(key, false).asSequence().filterNot(ItemUtil::isStackInvalid).filter {
        it.itemDamage != OreDictionary.WILDCARD_VALUE
    }

internal fun netheriteIngots(): List<ItemStack> =
    sequenceOf("ingotNetherite", "NetheriteIngot")
        .flatMap(::concreteOreStacks)
        .distinctBy { ItemId.createNoCopy(it.item, it.itemDamage, null) }
        .toList()
