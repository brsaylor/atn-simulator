package edu.sfsu.worldofbalance.atnsimulator;

public class EmptyFoodWebException extends RuntimeException {
    public EmptyFoodWebException() {
        super("Food web is empty.");
    }
}
