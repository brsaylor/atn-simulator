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

    /** Timestep at which each node went extinct (-1 for nodes that did not go extinct) */
    public int[] extinctionTimesteps;

    public SimulationEventHandler.EventType stopEvent;
    public int timestepsSimulated;

    public SimulationResults(int timesteps, int nodeCount) {
        biomass = new double[timesteps][nodeCount];
        extinctionTimesteps = new int[nodeCount];
    }
}
