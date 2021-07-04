package com.dunzo.CoffeeMachine;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CoffeeMachineMain {

    public static void main(String[] args) {
        List<String> beveragesToPrepare = new ArrayList<>(Arrays.asList("hot_tea", "hot_coffee", "black_tea", "green_tea", "elaichi_tea"));
        CoffeeMachine coffeeMachine = null;

        //not enough ingredients for each beverage
        try {
            System.out.println("========Test Case 1 Started=========");

            try {
                coffeeMachine = new CoffeeMachine(4);
            } catch (Exception e) {
                e.printStackTrace();
            }

            runTestForGivenOutletsAndBeverages(4, beveragesToPrepare, coffeeMachine);
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("========Test Case 1 End=========");

        //enough ingredients for each beverage except green_tea (no ingredients available)
        System.out.println("========Test Case 2 Started=========");
        try {
            coffeeMachine = new CoffeeMachine(4);
            coffeeMachine.refillAllIngredient(1000);

            runTestForGivenOutletsAndBeverages(4, beveragesToPrepare, coffeeMachine);
        } catch (Exception e) {
            e.printStackTrace();
        }


        System.out.println("========Test Case 2 End=========");

        //enough ingredients for each beverage with green_mixture added (ingredients for green_tea)
        System.out.println("========Test Case 3 Started=========");
        try {
            coffeeMachine = new CoffeeMachine(4);
            coffeeMachine.refillAllIngredient(1000);

            //added green mixture
            coffeeMachine.refillIngredient("green_mixture", 500);
            runTestForGivenOutletsAndBeverages(4, beveragesToPrepare, coffeeMachine);
        } catch (Exception e) {
            e.printStackTrace();
        }


        System.out.println("========Test Case 3 End=========");

        // more outlets than beverages with enough ingredients for each beverage
        System.out.println("========Test Case 4 Started=========");

        try {
            coffeeMachine = new CoffeeMachine(5);
            coffeeMachine.refillAllIngredient(1000);
            coffeeMachine.refillIngredient("green_mixture", 1000);
            runTestForGivenOutletsAndBeverages(4, beveragesToPrepare, coffeeMachine);
        } catch (Exception e) {
            e.printStackTrace();
        }


        System.out.println("========Test Case 4 End=========");
    }

    private static void runTestForGivenOutletsAndBeverages(int n, List<String> beveragesToPrepare, CoffeeMachine coffeeMachine) throws Exception {

        //Making executor service equal to outlets, so n beverages can be processed in parallel
        ExecutorService executorService = Executors.newFixedThreadPool(n);

        for (int i = 0; i < beveragesToPrepare.size(); i++) {
            MachineThread machineThread = new MachineThread(beveragesToPrepare.get(i), coffeeMachine);
            executorService.execute(machineThread);
        }

        executorService.shutdown();
        while (!executorService.isTerminated()) {
        }
    }
}


