package com.dunzo.CoffeeMachine.model;

import lombok.Data;

import java.util.HashMap;

@Data
public class Beverage {

    //name of the beverage,unique for our case
    String beverageName;

    //Map to maintain Recipes of beverage
    HashMap<String, Integer> recipes;
}
