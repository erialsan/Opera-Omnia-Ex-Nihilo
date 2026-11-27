package ooen.materia

import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.FMLLoadCompleteEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.common.config.Configuration
import ooen.BundledJson
import ooen.GTNH_LIB_DEPENDENCY
import org.apache.logging.log4j.LogManager

private const val MOD_ID = "ooen_materia"

@Mod(
    modid = MOD_ID,
    name = "Opera Omnia Ex Nihilo: Materia",
    version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies =
        "required-after:forgelin@[2.4.3-2.4.10,);" +
            "required-after:falsepatternlib@[1.12.2,);$GTNH_LIB_DEPENDENCY",
    modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter",
)
object Materia {
    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        val defaults =
            BundledJson.readArray("/ooen/materia/metal_whitelist.json")
                .map { it.asString }
                .toTypedArray()
        val config = Configuration(event.suggestedConfigurationFile)
        MetalCatalog.configure(
            config.getStringList(
                "metalWhitelist",
                "materials",
                defaults,
                "JAOPCA material names allowed in Materia. Names are case-sensitive.",
            )
        )
        if (config.hasChanged()) config.save()
    }

    @Mod.EventHandler
    fun onLoadComplete(event: FMLLoadCompleteEvent) {
        // JAOPCA sorts after all mods and discovers its materials before load completion.
        val metals = MetalCatalog.names()
        LogManager.getLogger(MOD_ID).info("Materia metals: count={} names={}", metals.size, metals)
    }
}
