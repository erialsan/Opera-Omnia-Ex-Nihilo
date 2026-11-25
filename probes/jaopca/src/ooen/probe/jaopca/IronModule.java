package ooen.probe.jaopca;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import thelm.jaopca.api.JAOPCAApi;
import thelm.jaopca.api.forms.IForm;
import thelm.jaopca.api.forms.IFormRequest;
import thelm.jaopca.api.materials.IMaterial;
import thelm.jaopca.api.materials.MaterialType;
import thelm.jaopca.api.modules.IModule;
import thelm.jaopca.api.modules.IModuleData;
import thelm.jaopca.api.modules.JAOPCAModule;

@JAOPCAModule
public class IronModule implements IModule {
    private static final String[] NAMES = {"ooen_gravel", "ooen_sand", "ooen_powder"};
    private static final String[] PREFIXES = {"ooenOreGravel", "ooenOreSand", "ooenOrePowder"};
    private static final String RETRY = "ooen_probe:phase_retry";

    @Override
    public String getName() {
        return "ooen_iron_trial";
    }

    @Override
    public Set<MaterialType> getMaterialTypes() {
        return Collections.singleton(MaterialType.INGOT);
    }

    @Override
    public List<IFormRequest> getFormRequests() {
        JAOPCAApi api = JAOPCAApi.instance();
        List<IFormRequest> requests = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            List<String> excluded = new ArrayList<>();
            for (IMaterial material : api.getMaterials()) {
                if (!material.getName().equals("Iron") || excluded(i)) excluded.add(material.getName());
            }
            IForm form = api.newForm(this, NAMES[i], api.itemFormType()).setSecondaryName(PREFIXES[i])
                .setMaterialTypes(MaterialType.INGOT).setDefaultMaterialBlacklist(excluded);
            // Independent requests: an existing gravel stack must not force the group to regenerate gravel.
            requests.add(form.toRequest());
        }
        return requests;
    }

    private static boolean excluded(int i) {
        return i == 2 && IronProvider.CASE.equals("exclude");
    }

    private static String ore(int i) {
        return i < 0 ? "oreIron" : PREFIXES[i] + "Iron";
    }

    private static String key(int i) {
        return "ooen_probe:" + NAMES[i] + ".iron";
    }

    @Override
    public void onInit(IModuleData data, FMLInitializationEvent event) {
        JAOPCAApi api = JAOPCAApi.instance();
        IMaterial iron = api.getMaterial("Iron");
        require(iron != null && data.getMaterials().contains(iron), "did not detect eligible Iron");
        IronProvider.LOG.info("phase=init name={} type={} ore={} earlyAliasMaterial={} sameAsIron={} alternatives={}",
            iron.getName(), iron.getType(), api.getOredict().contains("oreIron"), api.getMaterial("OoenFerrum") != null,
            api.getMaterial("OoenFerrum") == iron, iron.getAlternativeNames());
        for (int i = 0; i < NAMES.length; i++) {
            if (excluded(i)) continue;
            // Strings resolve even when JAOPCA skipped generating a form because another provider owns it.
            boolean first = api.registerShapelessRecipe(key(i), ore(i), 1, ore(i - 1), Items.paper);
            boolean duplicate = api.registerShapelessRecipe(key(i), ore(i), 1, ore(i - 1), Items.paper);
            IronProvider.LOG.info("phase=init key={} firstAccepted={} samePhaseRetryAccepted={}", key(i), first, duplicate);
            require(first && !duplicate, "unexpected queue result for " + key(i));
        }
        require(registerPhaseControl(), "first control recipe was rejected");
    }

    private static boolean registerPhaseControl() {
        return JAOPCAApi.instance().registerShapelessRecipe(RETRY, IronProvider.secondIngot, 1, Items.flint, Items.paper);
    }

    @Override
    public void onPostInit(IModuleData data, FMLPostInitializationEvent event) {
        IronProvider.LOG.info("phase=postInit key={} crossPhaseRetryAccepted={}", RETRY, registerPhaseControl());
    }

    @Override
    public void onLoadComplete(IModuleData data, FMLLoadCompleteEvent event) {
        JAOPCAApi api = JAOPCAApi.instance();
        boolean lateOre = api.getOredict().contains("oreOoenLateFerrum");
        boolean lateIngot = api.getOredict().contains("ingotOoenLateFerrum");
        IronProvider.LOG.info("phase=loadComplete lateOreKnown={} lateIngotKnown={} lateMaterial={}",
            lateOre, lateIngot, api.getMaterial("OoenLateFerrum") != null);
        require(lateOre && lateIngot, "late registrations did not reach JAOPCA's OreDictionary listener");
        for (int i = 0; i < NAMES.length; i++) {
            boolean reused = i == 0 && IronProvider.gravel != null;
            int expected = excluded(i) ? 0 : 1;
            List<ItemStack> stacks = OreDictionary.getOres(ore(i), false);
            List<String> ids = new ArrayList<>();
            for (ItemStack stack : stacks) ids.add(id(stack));
            List<ItemStack> inputs = OreDictionary.getOres(ore(i - 1), false);
            require(!inputs.isEmpty(), "missing recipe input " + ore(i - 1));
            List<String> results = craft(inputs.get(0));
            int generated = api.getForm(NAMES[i]).getMaterials().size();
            String expectedId = (reused ? IronProvider.ID + ":existing_iron_gravel" : "jaopca:" + NAMES[i] + ".iron") + "@0";
            boolean keySeen = api.getRecipes().contains(key(i));
            IronProvider.LOG.info("phase=loadComplete form={} ore={} entries={} ids={} generated={} itemDuplicates={} input={} matchedRecipes={} results={} recipeDuplicates={} keySeen={}",
                NAMES[i], ore(i), ids.size(), ids, generated, Math.max(0, ids.size() - expected), ore(i - 1),
                results.size(), results, Math.max(0, results.size() - expected), keySeen);
            require(ids.size() == expected && results.size() == expected && keySeen == (expected == 1)
                && generated == (reused ? 0 : expected) && (expected == 0 || (ids.get(0).equals(expectedId)
                && results.get(0).equals(expectedId))), "form/recipe mismatch for " + NAMES[i]);
        }
        List<String> control = craft(new ItemStack(Items.flint));
        String controlId = id(new ItemStack(IronProvider.secondIngot));
        IronProvider.LOG.info("phase=loadComplete key={} crossPhaseMatchingRecipes={} outputs={}", RETRY,
            Collections.frequency(control, controlId), control);
        IronProvider.LOG.info("RESULT case={} ironFormAndRecipeChecks=PASS", IronProvider.CASE);
    }

    // Count actual Forge recipes by running their matcher and taking the result, not counting JAOPCA queue keys.
    private static List<String> craft(ItemStack input) {
        InventoryCrafting grid = new InventoryCrafting(new Container() {
            @Override public boolean canInteractWith(EntityPlayer player) { return false; }
        }, 2, 2);
        grid.setInventorySlotContents(0, input.copy());
        grid.setInventorySlotContents(1, new ItemStack(Items.paper));
        List<String> outputs = new ArrayList<>();
        for (Object object : CraftingManager.getInstance().getRecipeList()) {
            if (object.getClass() != ShapelessOreRecipe.class) continue;
            ShapelessOreRecipe recipe = (ShapelessOreRecipe) object;
            if (recipe.matches(grid, null)) outputs.add(id(recipe.getCraftingResult(grid)));
        }
        return outputs;
    }

    private static String id(ItemStack stack) {
        return Item.itemRegistry.getNameForObject(stack.getItem()) + "@" + stack.getItemDamage();
    }

    private static void require(boolean ok, String message) {
        if (!ok) throw new IllegalStateException("OOEN-JAOPCA: " + message);
    }
}
