package edu.sfsu.worldofbalance.atnsimulator;

import java.util.Arrays;

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

    public SimulationResults(SimulationParameters sp, ModelParameters mp) {
        simulationParameters = sp;
        modelParameters = mp;
        int nodeCount = mp.metabolicRate.length;
        if (sp.recordBiomass)
            biomass = new double[sp.timesteps][nodeCount];
        else
            biomass = null;
        extinctionTimesteps = new int[nodeCount];
        Arrays.fill(extinctionTimesteps, -1);
        stopEvent = SimulationEventHandler.EventType.NONE;
        timestepsSimulated = 0;
    }
}
