package ooen.probe.jaopca;

import java.util.ArrayList;
import java.util.Arrays;
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
import ooen.materia.MetalCatalog;
import ooen.materia.MetalForm;
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
    private static final MetalForm[] FORMS = {MetalForm.GRAVEL, MetalForm.SAND, MetalForm.POWDER};
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
                if (!material.getName().equals(IronProvider.METAL) || excluded(i)) excluded.add(material.getName());
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
        return i < 0 ? "ore" + IronProvider.METAL : PREFIXES[i] + IronProvider.METAL;
    }

    private static String key(int i) {
        return "ooen_probe:" + NAMES[i] + "." + IronProvider.METAL_ID;
    }

    @Override
    public void onInit(IModuleData data, FMLInitializationEvent event) {
        JAOPCAApi api = JAOPCAApi.instance();
        IMaterial metal = api.getMaterial(IronProvider.METAL);
        require(metal != null && data.getMaterials().contains(metal), "did not detect eligible " + IronProvider.METAL);
        IMaterial alias = api.getMaterial("Ooen" + IronProvider.ALIAS);
        IronProvider.LOG.info("phase=init name={} type={} ore={} earlyAliasMaterial={} sameAsTarget={} alternatives={}",
            metal.getName(), metal.getType(), api.getOredict().contains(ore(-1)), alias != null,
            alias == metal, metal.getAlternativeNames());
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
        String lateAlias = "OoenLate" + IronProvider.ALIAS;
        boolean lateOre = api.getOredict().contains("ore" + lateAlias);
        boolean lateIngot = api.getOredict().contains("ingot" + lateAlias);
        IronProvider.LOG.info("phase=loadComplete lateOreKnown={} lateIngotKnown={} lateMaterial={}",
            lateOre, lateIngot, api.getMaterial(lateAlias) != null);
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
            String expectedId = reused
                ? IronProvider.ID + ":existing_" + IronProvider.METAL_ID + "_gravel@0"
                : "jaopca:" + NAMES[i] + "." + IronProvider.METAL_ID + "@0";
            boolean keySeen = api.getRecipes().contains(key(i));
            IronProvider.LOG.info("phase=loadComplete form={} ore={} entries={} ids={} generated={} itemDuplicates={} input={} matchedRecipes={} results={} recipeDuplicates={} keySeen={}",
                NAMES[i], ore(i), ids.size(), ids, generated, Math.max(0, ids.size() - expected), ore(i - 1),
                results.size(), results, Math.max(0, results.size() - expected), keySeen);
            require(ids.size() == expected && results.size() == expected && keySeen == (expected == 1)
                && generated == (reused ? 0 : expected) && (expected == 0 || (ids.get(0).equals(expectedId)
                && results.get(0).equals(expectedId))), "form/recipe mismatch for " + NAMES[i]);
            verifyMateriaCandidates(FORMS[i], ids.toArray(new String[0]));
        }
        List<String> control = craft(new ItemStack(Items.flint));
        String controlId = id(new ItemStack(IronProvider.secondIngot));
        IronProvider.LOG.info("phase=loadComplete key={} crossPhaseMatchingRecipes={} outputs={}", RETRY,
            Collections.frequency(control, controlId), control);
        IronProvider.LOG.info("RESULT case={} material={} formAndRecipeChecks=PASS", IronProvider.CASE, IronProvider.METAL);
        List<String> metals = MetalCatalog.names();
        require(metals.containsAll(Arrays.asList("Iron", "Gold", IronProvider.METAL))
            && Collections.disjoint(metals, Arrays.asList("Diamond", "Brick", "BrickNether")),
            "unexpected Materia metal catalog: " + metals);
        require(MetalCatalog.candidates("OoenAbsent", MetalForm.INGOT).isEmpty(),
            "Materia returned an ingot for an absent metal");
        if (IronProvider.COPPER) {
            require(api.getMaterial(IronProvider.ALLOY) != null, "JAOPCA did not detect the control alloy");
            require(!metals.contains(IronProvider.ALLOY)
                && MetalCatalog.candidates(IronProvider.ALLOY, MetalForm.INGOT).isEmpty(),
                "Materia accepted an alloy outside its whitelist");
            List<ItemStack> ingots = OreDictionary.getOres("ingotCopper", false);
            require(ingots.size() == 1 && ingots.get(0).getItem() == IronProvider.secondIngot,
                "expected exactly the fixture's copper ingot");
            verifyMateriaCandidates(MetalForm.INGOT, id(new ItemStack(IronProvider.secondIngot)));
            IronProvider.LOG.info("JAOPCA_COPPER_PASS case={}", IronProvider.CASE);
        } else {
            verifyMateriaCandidates(MetalForm.INGOT, "minecraft:iron_ingot@0", IronProvider.ID + ":second_ingot@0");
            IronProvider.LOG.info("MATERIA_IRON_CANDIDATES_PASS case={}", IronProvider.CASE);
        }
    }

    private static void verifyMateriaCandidates(MetalForm form, String... expected) {
        List<ItemStack> candidates = MetalCatalog.candidates(IronProvider.METAL, form);
        List<String> remaining = new ArrayList<>(Arrays.asList(expected));
        for (ItemStack candidate : candidates) {
            require(remaining.remove(id(candidate)), "unexpected Materia candidate for " + form + ": " + id(candidate));
            require(candidate.stackSize == 1, "Materia candidate count is not one for " + form);
            candidate.stackSize = 42;
        }
        require(remaining.isEmpty(), "missing Materia candidates for " + form + ": " + remaining);
        for (ItemStack entry : OreDictionary.getOres(form.oreKey(IronProvider.METAL), false)) {
            require(entry.stackSize != 42, "Materia candidate exposed a shared stack for " + form);
        }
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
