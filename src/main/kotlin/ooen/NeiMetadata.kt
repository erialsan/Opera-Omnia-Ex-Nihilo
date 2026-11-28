package ooen

import cpw.mods.fml.common.event.FMLInterModComms
import net.minecraft.nbt.NBTTagCompound

internal fun registerNeiMetadata(modId: String, machine: String) {
    FMLInterModComms.sendMessage(
        "NotEnoughItems",
        "registerHandlerInfo",
        NBTTagCompound().apply {
            setString("handler", "ooen.$machine")
            setString("modId", modId)
            setString("modName", "Opera Omnia Ex Nihilo")
            setString("itemName", "$modId:$machine")
            setInteger("handlerHeight", 54)
            setBoolean("multipleWidgetsAllowed", true)
            setBoolean("showOverlayButton", false)
        },
    )
}
