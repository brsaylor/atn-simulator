package edu.sfsu.worldofbalance.atnsimulator;

/**
 * Everything about a completed simulation to be written to an output file
 */
public class OutputFileData {
    int simulationId;
    SimulationResults simulationResults;
    String nodeConfig;
    double nodeConfigBiomassScale;
    int[] originalNodeIds;
    FoodWeb originalSubweb;
}
