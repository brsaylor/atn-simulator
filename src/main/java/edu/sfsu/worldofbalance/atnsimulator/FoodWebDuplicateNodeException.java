package edu.sfsu.worldofbalance.atnsimulator;

public class FoodWebDuplicateNodeException extends RuntimeException {
    public FoodWebDuplicateNodeException(int nodeId) {
        super("Food web already contains node " + nodeId);
    }
}
