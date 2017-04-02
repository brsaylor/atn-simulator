package edu.sfsu.worldofbalance.atnsimulator;

public class FoodWebNodeAbsentException extends RuntimeException {
    public FoodWebNodeAbsentException(int nodeId) {
        super("Food web does not contain node " + nodeId);
    }
}
