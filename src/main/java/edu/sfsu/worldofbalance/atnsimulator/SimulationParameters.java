package edu.sfsu.worldofbalance.atnsimulator;

/**
 * Parameters of a Simulation,
 * as distinct from the parameters of the model equations.
 */
public class SimulationParameters {
    public int timesteps;
    public double stepSize;
    public boolean stopOnSteadyState;
}