package com.dunzo.CoffeeMachine;

import com.dunzo.CoffeeMachine.model.Beverage;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CoffeeMachine {

    private final int machineOutlets;
    private final int ingredientsContainerSize;


    //recipes for each beverage
    Map<String, Beverage> beverageRecipesMapping = new HashMap<>();

    //Marking this as final is important because we would be acquiring lock on this and if this is non-final instance can be changed
    final ConcurrentHashMap<String, Integer> availableIngredients = new ConcurrentHashMap<>();

    //Constructor to initialize machine with max outlet
    public CoffeeMachine(int machineOutlets) throws Exception {
        this.machineOutlets = machineOutlets;
        this.ingredientsContainerSize = Integer.MAX_VALUE;
        initializeCoffeeMachine();
    }

    //Constructor to initialize machine with max outlet, max Ingredients Container size
    public CoffeeMachine(int machineOutlets, int ingredientsContainerSize) throws Exception {
        this.machineOutlets = machineOutlets;
        this.ingredientsContainerSize = ingredientsContainerSize;
        initializeCoffeeMachine();
    }


    // Every time coffee Machine is started initialize it with given recipes and ingredients
    private void initializeCoffeeMachine() throws Exception {
        initializeRecipes();
        initializeIngredients();
    }

    // Initializing Recipes when Coffee Machine is started
    // Recipes fetched from json file in resources/recipes.json
    private void initializeRecipes() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Beverage> beverageRecipes = new ArrayList<>();
        try {
            beverageRecipes = objectMapper.readValue(new File("./src/main/resources/recipes.json"), new TypeReference<List<Beverage>>() {
            });
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        for (Beverage beverageRecipe : beverageRecipes) {
            if (beverageRecipesMapping.containsKey(beverageRecipe.getBeverageName())) {
                System.out.println(beverageRecipe.getBeverageName() + "already exists.\n");
            }
            beverageRecipesMapping.put(beverageRecipe.getBeverageName(), beverageRecipe);
        }
        // System.out.println(beverageRecipesMapping);
    }

    // Initializing Ingredients when Coffee Machine is started
    // Recipes fetched from json file in resources/ingredients.json
    private void initializeIngredients() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        HashMap<String, Integer> ingredients = objectMapper.readValue(new File("./src/main/resources/ingredients.json"), new TypeReference<HashMap<String, Integer>>() {
        });

        for (Map.Entry<String, Integer> ingredientsMapEntry : ingredients.entrySet()) {
            if (availableIngredients.contains(ingredientsMapEntry.getKey())) {
                System.out.println(ingredientsMapEntry.getKey() + "already exists.\n");
            }
            availableIngredients.put(ingredientsMapEntry.getKey(), Math.min(ingredientsMapEntry.getValue(), ingredientsContainerSize));
        }
        //System.out.println(availableIngredients);
    }

    // Method to prepare beverages
    public void prepareBeverage(String beverageName) {

        Beverage recipe = beverageRecipesMapping.get(beverageName);

        if (recipe == null) {
            System.out.println(beverageName + " doesn't exists in the system\n");
            return;
        }

        synchronized (availableIngredients) {
            //Iterate over all the ingredients of recipe and check if beverage can be prepared
            for (Map.Entry<String, Integer> entry : recipe.getRecipes().entrySet()) {
                String ingredientName = entry.getKey();
                int requiredQuantity = entry.getValue();

                Integer availableQuantity = availableIngredients.get(ingredientName);

                if (availableQuantity == null || availableQuantity == 0) {
                    System.out.println(beverageName + " cannot be prepared because " + ingredientName + " is not available\n");
                    return;
                } else if (availableQuantity < requiredQuantity) {
                    System.out.println(beverageName + " cannot be prepared because " + ingredientName + " is not sufficient\n");
                    return;
                }
            }

            //If code reaches this point beverage can be prepared
            for (Map.Entry<String, Integer> entry : recipe.getRecipes().entrySet()) {
                String ingredientName = entry.getKey();
                int requiredQuantity = entry.getValue();

                int availableQuantity = availableIngredients.get(ingredientName);

                availableIngredients.put(ingredientName, availableQuantity - requiredQuantity);
                checkIfIngredientRunningLowAndSignal(availableQuantity - requiredQuantity, ingredientName);
            }

            System.out.println(beverageName + " is prepared\n");

        }
    }

    //This will signal that ingredient is running low if updated quantity is less than 10% of max size
    private void checkIfIngredientRunningLowAndSignal(int updatedQuantity, String ingredientName) {
        int tenPercentOfMax = (int) (0.1 * ingredientsContainerSize);
        if (updatedQuantity <= tenPercentOfMax) {
            System.out.println(ingredientName + " is running low. Please Refill.\n");
        }
    }

    //Method to refill a particular ingredient
    public void refillIngredient(String ingredientName, int quantity) {
        synchronized (availableIngredients) {
            Integer availableQuantity = availableIngredients.get(ingredientName);
            if (availableQuantity == null) {
                System.out.println("new ingredient " + ingredientName + " added.\n");
                availableIngredients.put(ingredientName, quantity);
            } else if (ingredientsContainerSize - availableQuantity >= quantity) {
                System.out.println(ingredientName + " refilled.\n");
                availableIngredients.put(ingredientName, availableQuantity + quantity);
            } else {
                System.out.println("Refilling caused spillage as quantity added was more than container Size. " + ingredientName + " refilled.\n");
                availableIngredients.put(ingredientName, ingredientsContainerSize);
            }
        }
    }

    //Method to refill a all the ingredients
    public void refillAllIngredient(int quantity) {
        synchronized (availableIngredients) {
            for (Map.Entry<String, Integer> availableIngredients : availableIngredients.entrySet()) {
                refillIngredient(availableIngredients.getKey(), quantity);
            }
        }

    }


    //This method can be used if new Recipes are added via recipes.json file
    public void recalibrateCoffeeMachine() throws Exception {
        synchronized (beverageRecipesMapping) {
            initializeRecipes();
        }

    }

}
