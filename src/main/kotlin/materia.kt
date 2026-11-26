package ooen.materia

import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.FMLLoadCompleteEvent
import net.minecraftforge.oredict.OreDictionary
import org.apache.logging.log4j.LogManager

private const val MOD_ID = "ooen_materia"

@Mod(
    modid = MOD_ID,
    name = "Opera Omnia Ex Nihilo: Materia",
    version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.7.10]",
)
class Materia {
    @Mod.EventHandler
    // The event is unused; a nullable parameter avoids a generated Kotlin runtime null check.
    fun onLoadComplete(event: FMLLoadCompleteEvent?) {
        LogManager.getLogger(MOD_ID)
            .info("OreDictionary baseline: ingotIron entries={}", OreDictionary.getOres("ingotIron").size)
    }
}
