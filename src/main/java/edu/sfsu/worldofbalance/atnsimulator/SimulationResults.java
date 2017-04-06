package edu.sfsu.worldofbalance.atnsimulator;

public class SimulationResults {
    public double[][] biomass;
    public SimulationEventHandler.EventType stopEvent;
    public int timestepsSimulated;

    public SimulationResults(int timesteps, int nodeCount) {
        biomass = new double[timesteps][nodeCount];
    }
}
