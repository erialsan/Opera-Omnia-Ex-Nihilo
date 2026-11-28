package ooen

import net.minecraft.item.ItemStack

internal fun MutableList<ItemStack>.addStackCopies(stack: ItemStack, quantity: Int) {
    val limit = stack.maxStackSize.coerceAtLeast(1)
    var remaining = quantity
    while (remaining > 0) {
        val count = minOf(remaining, limit)
        add(stack.copy().apply { stackSize = count })
        remaining -= count
    }
}
