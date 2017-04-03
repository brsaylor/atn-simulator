package edu.sfsu.worldofbalance.atnsimulator;

public class IncorrectParameterDimensionsException extends RuntimeException {
    public IncorrectParameterDimensionsException() {
        super("The given parameters are for the wrong number of nodes.");
    }
}
