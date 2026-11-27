package ooen.materia

import com.gtnewhorizon.gtnhlib.config.ConfigurationManager
import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.FMLLoadCompleteEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
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
        ConfigurationManager.registerConfig(MateriaConfig::class.java)
        MetalCatalog.configure(MateriaConfig.metalWhitelist)
    }

    @Mod.EventHandler
    fun onLoadComplete(event: FMLLoadCompleteEvent) {
        // JAOPCA sorts after all mods and discovers its materials before load completion.
        val metals = MetalCatalog.names()
        LogManager.getLogger(MOD_ID).info("Materia metals: count={} names={}", metals.size, metals)
    }
}
