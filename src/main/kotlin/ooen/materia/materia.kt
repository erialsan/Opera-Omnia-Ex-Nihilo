package ooen.materia

import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.FMLLoadCompleteEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.common.config.Configuration
import org.apache.logging.log4j.LogManager

private const val MOD_ID = "ooen_materia"

@Mod(
    modid = MOD_ID,
    name = "Opera Omnia Ex Nihilo: Materia",
    version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies =
        "required-after:forgelin@[2.4.3-2.4.10,);" +
            "required-after:falsepatternlib@[1.12.2,);required-after:gtnhlib@[0.12.10,)",
    modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter",
)
object Materia {
    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        val configuration = Configuration(event.suggestedConfigurationFile)
        MetalCatalog.configure(
            configuration.getStringList(
                "metalWhitelist",
                "materials",
                arrayOf(
                    "Iron",
                    "Gold",
                    "Copper",
                    "Tin",
                    "Aluminum",
                    "Aluminium",
                    "Lead",
                    "Nickel",
                    "Silver",
                    "Cobalt",
                    "Ardite",
                    "Osmium",
                    "Iridium",
                    "Tungsten",
                    "Boron",
                    "Uranium",
                    "Platinum",
                    "Manganese",
                    "Titanium",
                    "Mithril",
                    "Thorium",
                ),
                "JAOPCA material names allowed in Materia. Names are case-sensitive.",
            )
        )
        if (configuration.hasChanged()) configuration.save()
    }

    @Mod.EventHandler
    fun onLoadComplete(event: FMLLoadCompleteEvent) {
        // JAOPCA sorts after all mods and discovers its materials before load completion.
        val metals = MetalCatalog.names()
        LogManager.getLogger(MOD_ID).info("Materia metals: count={} names={}", metals.size, metals)
    }
}
