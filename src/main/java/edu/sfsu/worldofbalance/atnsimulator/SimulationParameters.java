package edu.sfsu.worldofbalance.atnsimulator;

/**
 * Parameters of a Simulation,
 * as distinct from the parameters of the model equations.
 */
public class SimulationParameters {
    public int timesteps = 100;                // Number of timesteps to simulate
    public double stepSize = 0.1;              // Time increment per timestep
    public boolean stopOnSteadyState = false;  // Stop simulation when a steady state is detected
    public boolean recordBiomass = true;       // Include biomass in SimulationResults
}