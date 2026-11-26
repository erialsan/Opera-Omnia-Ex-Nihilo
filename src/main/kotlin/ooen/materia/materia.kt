package ooen.materia

import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.FMLLoadCompleteEvent
import net.minecraft.item.Item
import net.minecraftforge.oredict.OreDictionary
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
    fun onLoadComplete(event: FMLLoadCompleteEvent) {
        val logger = LogManager.getLogger(MOD_ID)
        logger.info(
            "OreDictionary baseline: ingotIron entries={}",
            OreDictionary.getOres(IronForm.INGOT.oreKey, false).size,
        )
        for (form in IronForm.entries) {
            val ids =
                form.candidates().map {
                    "${Item.itemRegistry.getNameForObject(it.item)}@${it.itemDamage}"
                }
            logger.info("Materia iron form: {} candidates={} ids={}", form.oreKey, ids.size, ids)
        }
    }
}
