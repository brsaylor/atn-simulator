package edu.sfsu.worldofbalance.atnsimulator;

/**
 * Parameters of a Simulation,
 * as distinct from the parameters of the model equations.
 */
public class SimulationParameters {
    public int timesteps;              // Number of timesteps to simulate
    public double stepSize;            // Time increment per timestep
    public boolean stopOnSteadyState;  // Stop simulation when a steady state is detected
}