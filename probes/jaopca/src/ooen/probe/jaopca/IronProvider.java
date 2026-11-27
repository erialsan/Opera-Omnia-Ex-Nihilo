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
@Mod(modid = IronProvider.ID, name = "OOEN JAOPCA metal trial", version = "1", dependencies = "required-before:jaopca")
public class IronProvider {
    static final String ID = "ooen_jaopca_probe";
    static final String CASE = System.getProperty("ooen.jaopca.case", "reuse");
    static final boolean COPPER = "copper".equals(System.getProperty("ooen.probe.metal"));
    static final String METAL = COPPER ? "Copper" : "Iron";
    static final String METAL_ID = COPPER ? "copper" : "iron";
    static final String ALIAS = COPPER ? "Cuprum" : "Ferrum";
    static final String ALLOY = "OoenBronze";
    static final Logger LOG = LogManager.getLogger("OOEN-JAOPCA");
    static Item secondIngot;
    static Item gravel;
    private ItemStack ore;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (COPPER) {
            int ingots = OreDictionary.getOres("ingotCopper", false).size();
            int ores = OreDictionary.getOres("oreCopper", false).size();
            if (GameRegistry.findItem("minecraft", "copper_ingot") != null
                || GameRegistry.findBlock("minecraft", "copper_ore") != null || ingots != 0 || ores != 0) {
                throw new IllegalStateException("OOEN-JAOPCA: copper already exists before fixture registration");
            }
            LOG.info("phase=preInit material=Copper beforeProvider ingotCopper={} oreCopper={}", ingots, ores);
            Item copperOre = new Item().setUnlocalizedName("ooen.probe.copper_ore").setTextureName("minecraft:flint");
            GameRegistry.registerItem(copperOre, "copper_ore");
            ore = new ItemStack(copperOre);
            OreDictionary.registerOre("oreCopper", ore);
            Item alloy = new Item().setUnlocalizedName("ooen.probe.bronze").setTextureName("minecraft:gold_ingot");
            GameRegistry.registerItem(alloy, "bronze_ingot");
            OreDictionary.registerOre("ingot" + ALLOY, new ItemStack(alloy));
            OreDictionary.registerOre("ore" + ALLOY, ore);
        } else {
            ore = new ItemStack(Blocks.iron_ore);
        }
        secondIngot = new Item().setUnlocalizedName("ooen.probe.ingot")
            .setTextureName(COPPER ? "minecraft:brick" : "minecraft:iron_ingot");
        GameRegistry.registerItem(secondIngot, COPPER ? "copper_ingot" : "second_ingot");
        OreDictionary.registerOre("ingot" + METAL, new ItemStack(secondIngot));
        registerAlias("Ooen" + ALIAS);
        if (!CASE.equals("generate")) {
            gravel = new Item().setUnlocalizedName("ooen.probe.gravel").setTextureName("minecraft:flint");
            GameRegistry.registerItem(gravel, "existing_" + METAL_ID + "_gravel");
            OreDictionary.registerOre("ooenOreGravel" + METAL, new ItemStack(gravel));
        }
        LOG.info("phase=preInit case={} material={} ingots={} earlyGravel={}", CASE, METAL,
            OreDictionary.getOres("ingot" + METAL, false).size(), OreDictionary.getOres("ooenOreGravel" + METAL, false).size());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // Same input stacks as the early alias; only registration time and suffix differ.
        registerAlias("OoenLate" + ALIAS);
    }

    private void registerAlias(String name) {
        OreDictionary.registerOre("ingot" + name, new ItemStack(secondIngot));
        OreDictionary.registerOre("ore" + name, ore);
    }
}
