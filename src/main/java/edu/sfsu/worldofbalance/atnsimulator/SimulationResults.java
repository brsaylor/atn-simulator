package edu.sfsu.worldofbalance.atnsimulator;

/**
 * SimulationResults contains
 * everything we want to know about a completed simulation
 * that the Simulation object can provide.
 */
public class SimulationResults {
    public SimulationParameters simulationParameters;
    public ModelParameters modelParameters;
    public double[][] biomass;
    public SimulationEventHandler.EventType stopEvent;
    public int timestepsSimulated;

    public SimulationResults(int timesteps, int nodeCount) {
        biomass = new double[timesteps][nodeCount];
    }
}
