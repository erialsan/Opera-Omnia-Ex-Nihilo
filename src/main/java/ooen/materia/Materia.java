package ooen.materia;

import net.minecraftforge.oredict.OreDictionary;

import org.apache.logging.log4j.LogManager;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;

@Mod(
    modid = Materia.MOD_ID,
    name = "Opera Omnia Ex Nihilo: Materia",
    version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.7.10]")
public class Materia {

    public static final String MOD_ID = "ooen_materia";

    @Mod.EventHandler
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        LogManager.getLogger(MOD_ID)
            .info(
                "OreDictionary baseline: ingotIron entries={}",
                OreDictionary.getOres("ingotIron")
                    .size());
    }
}
