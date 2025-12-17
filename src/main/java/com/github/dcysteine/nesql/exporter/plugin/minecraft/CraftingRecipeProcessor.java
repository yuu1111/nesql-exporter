package com.github.dcysteine.nesql.exporter.plugin.minecraft;

import WayofTime.alchemicalWizardry.api.items.ShapedBloodOrbRecipe;
import WayofTime.alchemicalWizardry.api.items.ShapelessBloodOrbRecipe;
import appeng.api.recipes.IIngredient;
import appeng.recipes.game.ShapedRecipe;
import appeng.recipes.game.ShapelessRecipe;
import bartworks.API.recipe.BWNBTDependantCraftingRecipe;
import codechicken.nei.NEIServerUtils;
import com.github.dcysteine.nesql.exporter.main.Log;
import com.github.dcysteine.nesql.exporter.plugin.PluginExporter;
import com.github.dcysteine.nesql.exporter.plugin.PluginHelper;
import com.github.dcysteine.nesql.exporter.plugin.base.factory.RecipeBuilder;
import com.github.dcysteine.nesql.sql.base.recipe.RecipeType;
import logisticspipes.recipes.ShapelessResetRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CraftingRecipeProcessor extends PluginHelper {
    private final RecipeType shapedCrafting;
    private final RecipeType shapelessCrafting;

    public CraftingRecipeProcessor(
            PluginExporter exporter, MinecraftRecipeTypeHandler recipeTypeHandler) {
        super(exporter);
        this.shapedCrafting =
                recipeTypeHandler.getRecipeType(
                        MinecraftRecipeTypeHandler.MinecraftRecipeType.SHAPED_CRAFTING);
        this.shapelessCrafting =
                recipeTypeHandler.getRecipeType(
                        MinecraftRecipeTypeHandler.MinecraftRecipeType.SHAPELESS_CRAFTING);
    }

    public void process() {
        @SuppressWarnings("unchecked")
        List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
        int total = recipes.size();
        logger.info("Processing {} crafting recipes...", total);

        int count = 0;
        for (IRecipe recipe : recipes) {
            count++;

            if (recipe.getRecipeOutput() == null) {
                Log.warn(logger, "Skipping crafting recipe with null output: {}", recipe);
                continue;
            }

            if (recipe instanceof ShapedRecipes) {
                processShapedRecipe((ShapedRecipes) recipe);
            } else if (recipe instanceof ShapedRecipe) {
                // AE2 ShapedRecipe
                processAe2ShapedRecipe((ShapedRecipe) recipe);
            } else if (recipe instanceof ShapedOreRecipe) {
                processShapedOreRecipe((ShapedOreRecipe) recipe);
            } else if (recipe instanceof ShapelessRecipes) {
                processShapelessRecipe((ShapelessRecipes) recipe);
            } else if (recipe instanceof ShapelessRecipe) {
                // AE2 ShapelessRecipe
                processAe2ShapelessRecipe((ShapelessRecipe) recipe);
            } else if (recipe instanceof ShapelessOreRecipe) {
                processShapelessOreRecipe((ShapelessOreRecipe) recipe);
            } else if (recipe instanceof BWNBTDependantCraftingRecipe) {
                // BartWorks NBT-dependent recipe (Circuit Imprints, etc.)
                processBartWorksNbtRecipe((BWNBTDependantCraftingRecipe) recipe);
            } else if (recipe instanceof ShapedBloodOrbRecipe) {
                // Blood Magic shaped recipe
                processBloodMagicShapedRecipe((ShapedBloodOrbRecipe) recipe);
            } else if (recipe instanceof ShapelessBloodOrbRecipe) {
                // Blood Magic shapeless recipe
                processBloodMagicShapelessRecipe((ShapelessBloodOrbRecipe) recipe);
            } else if (recipe instanceof ShapelessResetRecipe) {
                // Logistics Pipes reset recipe
                processLogisticsPipesResetRecipe((ShapelessResetRecipe) recipe);
            } else if (isShulkerNbtRecipe(recipe)) {
                // GTNH Et Futurum shulker dyeing recipe - skip (2300+ variants of dye recipes)
                // These are just shulker box color variants, not very useful to export
            } else {
                Log.warn(logger, "Unhandled crafting recipe: {}", recipe);
            }

            if (Log.intermittentLog(count)) {
                logger.info("Processed crafting recipe {} of {}", count, total);
                logger.info(
                        "Most recent recipe: {}", recipe.getRecipeOutput().getDisplayName());
            }
        }

        exporterState.flushEntityManager();
        logger.info("Finished processing crafting recipes!");
    }

    private void processShapedRecipe(ShapedRecipes recipe) {
        RecipeBuilder builder = new RecipeBuilder(exporter, shapedCrafting);
        for (Object itemInput : recipe.recipeItems) {
            if (itemInput == null) {
                builder.skipItemInput();
                continue;
            }

            handleItemInput(builder, itemInput);
        }
        builder.addItemOutput(recipe.getRecipeOutput()).build();
    }

    private void processShapedOreRecipe(ShapedOreRecipe recipe) {
        RecipeBuilder builder = new RecipeBuilder(exporter, shapedCrafting);
        for (Object itemInput : recipe.getInput()) {
            if (itemInput == null) {
                builder.skipItemInput();
                continue;
            } else if (itemInput instanceof List && ((List<?>) itemInput).isEmpty()) {
                Log.warn(logger, "Shaped ore crafting recipe with empty list ingredient: {}", recipe);
                builder.skipItemInput();
                continue;
            }

            handleItemInput(builder, itemInput);
        }
        builder.addItemOutput(recipe.getRecipeOutput()).build();
    }

    private void processShapelessRecipe(ShapelessRecipes recipe) {
        // Apparently this actually happens? At least, according to a comment in NEI source.
        if (recipe.recipeItems == null) {
            Log.warn(logger, "Crafting recipe with null inputs: {}", recipe);
            return;
        }

        RecipeBuilder builder = new RecipeBuilder(exporter, shapelessCrafting);
        for (Object itemInput : recipe.recipeItems) {
            handleItemInput(builder, itemInput);
        }
        builder.addItemOutput(recipe.getRecipeOutput()).build();
    }

    private void processShapelessOreRecipe(ShapelessOreRecipe recipe) {
        RecipeBuilder builder = new RecipeBuilder(exporter, shapelessCrafting);
        for (Object itemInput : recipe.getInput()) {
            if (itemInput instanceof List && ((List<?>) itemInput).isEmpty()) {
                Log.warn(logger, "Shapeless ore crafting recipe with empty list ingredient: {}", recipe);
                builder.skipItemInput();
                continue;
            }

            handleItemInput(builder, itemInput);
        }
        builder.addItemOutput(recipe.getRecipeOutput()).build();
    }

    private void processAe2ShapedRecipe(ShapedRecipe recipe) {
        RecipeBuilder builder = new RecipeBuilder(exporter, shapedCrafting);
        for (Object itemInput : recipe.getInput()) {
            if (itemInput == null) {
                builder.skipItemInput();
                continue;
            }
            // AE2 uses IIngredient objects, handled by handleItemInput
            handleItemInput(builder, itemInput);
        }
        builder.addItemOutput(recipe.getRecipeOutput()).build();
    }

    private void processAe2ShapelessRecipe(ShapelessRecipe recipe) {
        RecipeBuilder builder = new RecipeBuilder(exporter, shapelessCrafting);
        for (Object itemInput : recipe.getInput()) {
            if (itemInput == null) {
                builder.skipItemInput();
                continue;
            }
            // AE2 uses IIngredient objects, handled by handleItemInput
            handleItemInput(builder, itemInput);
        }
        builder.addItemOutput(recipe.getRecipeOutput()).build();
    }

    @SuppressWarnings("unchecked")
    private void processBartWorksNbtRecipe(BWNBTDependantCraftingRecipe recipe) {
        // BWNBTDependantCraftingRecipe implements IRecipe directly (not extending ShapedOreRecipe)
        // We need to use reflection to access the internal fields: shape and charToStackMap
        try {
            Field shapeField = BWNBTDependantCraftingRecipe.class.getDeclaredField("shape");
            Field charToStackMapField = BWNBTDependantCraftingRecipe.class.getDeclaredField("charToStackMap");
            shapeField.setAccessible(true);
            charToStackMapField.setAccessible(true);

            String[] shape = (String[]) shapeField.get(recipe);
            Map<Character, ItemStack> charToStackMap =
                    (Map<Character, ItemStack>) charToStackMapField.get(recipe);

            RecipeBuilder builder = new RecipeBuilder(exporter, shapedCrafting);

            // Process each row of the 3x3 crafting grid
            for (String row : shape) {
                for (char c : row.toCharArray()) {
                    if (c == ' ') {
                        builder.skipItemInput();
                    } else {
                        ItemStack itemStack = charToStackMap.get(c);
                        if (itemStack == null) {
                            builder.skipItemInput();
                        } else {
                            handleItemInput(builder, itemStack);
                        }
                    }
                }
            }

            builder.addItemOutput(recipe.getRecipeOutput()).build();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.warn(logger, "Failed to process BartWorks NBT recipe via reflection: {}", recipe, e);
        }
    }

    private void handleItemInput(RecipeBuilder builder, Object itemInput) {
        // レシピメタデータ (Integer/Character) はスキップ
        if (itemInput instanceof Integer || itemInput instanceof Character) {
            return;
        }

        // AE2 IIngredient を処理
        if (itemInput instanceof IIngredient ingredient) {
            if (ingredient.isAir()) {
                builder.skipItemInput();
                return;
            }
            try {
                ItemStack[] stacks = ingredient.getItemStackSet();
                if (stacks != null && stacks.length > 0) {
                    builder.addItemGroupInput(stacks);
                } else {
                    builder.skipItemInput();
                }
            } catch (Exception e) {
                Log.warn(logger, "Failed to get ItemStack from AE2 IIngredient: {}", itemInput);
                builder.skipItemInput();
            }
            return;
        }

        ItemStack[] itemStacks = NEIServerUtils.extractRecipeItems(itemInput);
        if (itemStacks == null || itemStacks.length == 0) {
            builder.skipItemInput();
            return;
        }

        // For some reason, a bunch of crafting recipes have stack size > 1, even though crafting
        // recipes only ever consume one item from each slot. This is probably a bug in the recipes.
        // We'll fix this by manually setting stack sizes to 1.
        ItemStack[] fixedItemStacks = new ItemStack[itemStacks.length];
        boolean foundBadStackSize = false;
        for (int i = 0; i < itemStacks.length; i++) {
            ItemStack itemStack = itemStacks[i];

            if (itemStack.stackSize != 1) {
                foundBadStackSize = true;
                fixedItemStacks[i] = itemStack.copy();
                fixedItemStacks[i].stackSize = 1;
            } else {
                fixedItemStacks[i] = itemStack;
            }
        }

        if (foundBadStackSize) {
            Log.warn(logger, "Crafting recipe with bad stack size: {}", Arrays.toString(itemStacks));
        }

        builder.addItemGroupInput(fixedItemStacks);
    }

    private void processBloodMagicShapedRecipe(ShapedBloodOrbRecipe recipe) {
        RecipeBuilder builder = new RecipeBuilder(exporter, shapedCrafting);
        for (Object itemInput : recipe.getInput()) {
            if (itemInput == null) {
                builder.skipItemInput();
                continue;
            } else if (itemInput instanceof List && ((List<?>) itemInput).isEmpty()) {
                builder.skipItemInput();
                continue;
            } else if (itemInput instanceof Integer || itemInput instanceof Character) {
                // レシピメタデータ (shapedレシピの幅/高さ) をスキップ
                continue;
            }

            handleItemInput(builder, itemInput);
        }
        builder.addItemOutput(recipe.getRecipeOutput()).build();
    }

    @SuppressWarnings("unchecked")
    private void processBloodMagicShapelessRecipe(ShapelessBloodOrbRecipe recipe) {
        RecipeBuilder builder = new RecipeBuilder(exporter, shapelessCrafting);
        ArrayList<Object> inputs = recipe.getInput();
        for (Object itemInput : inputs) {
            if (itemInput instanceof List && ((List<?>) itemInput).isEmpty()) {
                builder.skipItemInput();
                continue;
            }

            handleItemInput(builder, itemInput);
        }
        builder.addItemOutput(recipe.getRecipeOutput()).build();
    }

    private void processLogisticsPipesResetRecipe(ShapelessResetRecipe recipe) {
        // ShapelessResetRecipe is a simple recipe that takes one item and returns
        // the same item with reset NBT data. We use reflection to get the item/meta.
        try {
            Field itemField = ShapelessResetRecipe.class.getDeclaredField("item");
            Field metaField = ShapelessResetRecipe.class.getDeclaredField("meta");
            itemField.setAccessible(true);
            metaField.setAccessible(true);

            Item item = (Item) itemField.get(recipe);
            int meta = metaField.getInt(recipe);

            ItemStack inputStack = new ItemStack(item, 1, meta);
            RecipeBuilder builder = new RecipeBuilder(exporter, shapelessCrafting);
            handleItemInput(builder, inputStack);
            builder.addItemOutput(recipe.getRecipeOutput()).build();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.warn(logger, "Failed to process Logistics Pipes reset recipe via reflection: {}", recipe, e);
        }
    }

    /**
     * Checks if the recipe is a GTNH ShulkerNBTRecipe (from Et Futurum Requiem scripts).
     * We check by class name since NewHorizonsCoreMod is not a compile-time dependency.
     */
    private boolean isShulkerNbtRecipe(IRecipe recipe) {
        return recipe.getClass().getName().contains("ShulkerNBTRecipe");
    }
}
