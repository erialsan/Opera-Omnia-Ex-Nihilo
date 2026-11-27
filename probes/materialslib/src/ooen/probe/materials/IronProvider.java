package ooen.probe.materials;

import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;

import com.gtnewhorizons.materialslib.api.event.MaterialEvent;
import com.gtnewhorizons.materialslib.api.event.MaterialRegistryEvent;
import com.gtnewhorizons.materialslib.api.material.Material;
import com.gtnewhorizons.materialslib.api.registry.IMaterialRegistry;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = IronProvider.ID, name = "OOEN Probe Iron Provider", version = "1", dependencies = "required-after:materialslib")
public final class IronProvider {
    static final String ID = "ooen_iron_provider";
    static final boolean COPPER = "copper".equals(System.getProperty("ooen.probe.metal"));
    static final String MATERIAL_NAME = COPPER ? "copper" : "iron";
    static final int MATERIAL_ID = COPPER ? 29 : 26;
    private IMaterialRegistry registry;

    @Mod.EventHandler
    public void construct(FMLConstructionEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void createRegistry(MaterialRegistryEvent event) {
        registry = event.getManager().createRegistry(ID);
        LogManager.getLogger(ID).info("MaterialRegistryEvent created registry={}", registry.getModId());
    }

    @SubscribeEvent
    public void registerMaterial(MaterialEvent event) {
        Material material = Material.builder(ID, MATERIAL_NAME, MATERIAL_ID).build();
        if (registry.getMaterial(MATERIAL_ID) != material) {
            throw new IllegalStateException("Material was not registered in provider registry: " + MATERIAL_NAME);
        }
        LogManager.getLogger(ID).info("MaterialEvent registered {}:{} id={}", material.getModId(), material.getName(), material.getId());
    }
}
