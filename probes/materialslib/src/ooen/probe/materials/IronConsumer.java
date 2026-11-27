package ooen.probe.materials;

import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;

import com.gtnewhorizons.materialslib.api.MaterialsAPI;
import com.gtnewhorizons.materialslib.api.event.MaterialRegistryEvent;
import com.gtnewhorizons.materialslib.api.material.Material;
import com.gtnewhorizons.materialslib.api.registry.IMaterialRegistry;
import com.gtnewhorizons.materialslib.api.registry.IMaterialRegistryManager;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = IronConsumer.ID, name = "OOEN Probe Iron Consumer", version = "1", dependencies = "required-after:ooen_iron_provider")
public final class IronConsumer {
    static final String ID = "ooen_iron_consumer";
    private IMaterialRegistry secondRegistry;

    @Mod.EventHandler
    public void construct(FMLConstructionEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void namespaceControl(MaterialRegistryEvent event) {
        secondRegistry = event.getManager().createRegistry(ID);
    }

    @Mod.EventHandler
    public void verify(FMLLoadCompleteEvent event) {
        IMaterialRegistryManager manager = MaterialsAPI.materialManager;
        IMaterialRegistry owner = manager.getRegistry(IronProvider.ID);
        String name = IronProvider.MATERIAL_NAME;
        int id = IronProvider.MATERIAL_ID;
        Material material = owner.getMaterial(id);
        if (manager.getPhase() != IMaterialRegistryManager.Phase.FROZEN
            || !IronProvider.ID.equals(owner.getModId()) || material == null || material != owner.getMaterial(name)
            || !IronProvider.ID.equals(material.getModId()) || !name.equals(material.getName()) || material.getId() != id) {
            throw new IllegalStateException("Consumer failed to look up " + IronProvider.ID + ":" + name + " (" + id + ") in FROZEN registry");
        }
        String marker = IronProvider.COPPER ? "MATERIALSLIB_COPPER_PASS" : "MATERIALSLIB_IRON_PASS";
        LogManager.getLogger(ID).info("{} registry={} name={} id={} phase={}", marker, owner.getModId(), name, id, manager.getPhase());
        IMaterialRegistry bySecondName = manager.getRegistry(ID);
        LogManager.getLogger(ID).info("MATERIALSLIB_NAMESPACE_CONTROL isolated={} requested={} returned={}", bySecondName == secondRegistry, ID, bySecondName.getModId());
    }
}
