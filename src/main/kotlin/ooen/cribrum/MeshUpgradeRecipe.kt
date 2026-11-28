package ooen.cribrum

import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.oredict.ShapedOreRecipe

internal class MeshUpgradeRecipe(output: ItemStack, material: ItemStack, previous: ItemStack) :
    ShapedOreRecipe(output, " M ", "MPM", " M ", 'M', material, 'P', previous) {
    override fun getCraftingResult(inventory: InventoryCrafting): ItemStack =
        super.getCraftingResult(inventory).also {
            it.tagCompound = inventory.getStackInSlot(4)?.tagCompound?.copy() as? NBTTagCompound
        }
}
