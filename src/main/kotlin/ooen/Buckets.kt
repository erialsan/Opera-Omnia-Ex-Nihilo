package ooen

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

internal fun EntityPlayer.exchangeBucket(held: ItemStack, result: Item) {
    val output = ItemStack(result)
    if (capabilities.isCreativeMode) {
        if (inventory.hasItem(result)) return
    } else {
        held.stackSize--
        if (held.stackSize == 0) {
            inventory.setInventorySlotContents(inventory.currentItem, output)
            return
        }
    }
    if (!inventory.addItemStackToInventory(output)) dropPlayerItemWithRandomChoice(output, false)
}
