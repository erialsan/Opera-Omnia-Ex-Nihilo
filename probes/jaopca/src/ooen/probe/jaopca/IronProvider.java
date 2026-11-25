package ooen.probe.jaopca;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

/** A second provider, independent of the JAOPCA module which consumes its OreDictionary entries. */
@Mod(modid = IronProvider.ID, name = "OOEN JAOPCA iron trial", version = "1", dependencies = "required-before:jaopca")
public class IronProvider {
    static final String ID = "ooen_jaopca_probe";
    static final String CASE = System.getProperty("ooen.jaopca.case", "reuse");
    static final Logger LOG = LogManager.getLogger("OOEN-JAOPCA");
    static Item secondIngot;
    static Item gravel;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        secondIngot = new Item().setUnlocalizedName("ooen.probe.ingot").setTextureName("minecraft:iron_ingot");
        GameRegistry.registerItem(secondIngot, "second_ingot");
        OreDictionary.registerOre("ingotIron", new ItemStack(secondIngot));
        OreDictionary.registerOre("ingotOoenFerrum", new ItemStack(secondIngot));
        OreDictionary.registerOre("oreOoenFerrum", Blocks.iron_ore);
        if (!CASE.equals("generate")) {
            gravel = new Item().setUnlocalizedName("ooen.probe.gravel").setTextureName("minecraft:flint");
            GameRegistry.registerItem(gravel, "existing_iron_gravel");
            OreDictionary.registerOre("ooenOreGravelIron", new ItemStack(gravel));
        }
        LOG.info("phase=preInit case={} ingotIron={} earlyGravel={}", CASE,
            OreDictionary.getOres("ingotIron", false).size(), OreDictionary.getOres("ooenOreGravelIron", false).size());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // Same input stacks as the early alias; only registration time and suffix differ.
        OreDictionary.registerOre("ingotOoenLateFerrum", new ItemStack(secondIngot));
        OreDictionary.registerOre("oreOoenLateFerrum", Blocks.iron_ore);
    }
}
