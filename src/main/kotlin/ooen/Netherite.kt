package ooen

import com.gtnewhorizon.gtnhlib.util.data.ItemId
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary

internal fun netheriteIngots(): List<ItemStack> =
    listOf("ingotNetherite", "NetheriteIngot")
        .flatMap { OreDictionary.getOres(it, false) }
        .filter { it.item != null && it.itemDamage != OreDictionary.WILDCARD_VALUE }
        .distinctBy { ItemId.createNoCopy(it.item, it.itemDamage, null) }
