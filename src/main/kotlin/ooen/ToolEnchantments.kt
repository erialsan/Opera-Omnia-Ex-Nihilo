package ooen

import java.util.Random
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.init.Items
import net.minecraftforge.event.AnvilUpdateEvent

internal fun fortuneMultiplier(random: Random, fortune: Int): Int {
    val level = fortune.coerceIn(0, Enchantment.fortune.maxLevel)
    return if (level == 0) 1 else random.nextInt(level + 2).coerceAtLeast(1)
}

internal fun AnvilUpdateEvent.applyBookEnchantments(vararg supported: Enchantment) {
    val stack = left
    val book = right
    if (output != null || stack.stackSize != 1 || book.item != Items.enchanted_book) return

    val bookEnchantments = EnchantmentHelper.getEnchantments(book)
    val enchantments = EnchantmentHelper.getEnchantments(stack)
    var levelsCost = 0
    for (enchantment in supported) {
        val id = enchantment.effectId
        val bookLevel = bookEnchantments[id] ?: continue
        if (bookLevel <= 0) continue
        val oldLevel = enchantments[id] ?: 0
        val combined = if (bookLevel == oldLevel) oldLevel + 1 else maxOf(bookLevel, oldLevel)
        val level = combined.coerceAtMost(enchantment.maxLevel)
        if (level <= oldLevel) continue
        enchantments[id] = level
        levelsCost += 2 * level
    }
    if (levelsCost == 0) return

    val result = stack.copy()
    EnchantmentHelper.setEnchantments(enchantments, result)
    val requestedName = name
    val renamed =
        if (requestedName.isNullOrBlank()) {
            result.hasDisplayName().also { if (it) result.func_135074_t() }
        } else if (requestedName != stack.displayName) {
            result.setStackDisplayName(requestedName)
            true
        } else false
    result.repairCost = maxOf(stack.repairCost, book.repairCost) + 2

    output = result
    cost += levelsCost + if (renamed) 5 else 0
    materialCost = 1
}
