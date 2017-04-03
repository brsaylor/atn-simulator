package edu.sfsu.worldofbalance.atnsimulator;

public class FoodWebNotNormalizedException extends RuntimeException {
    public FoodWebNotNormalizedException() {
        super("A food web with normalized node IDs is required.");
    }
}
