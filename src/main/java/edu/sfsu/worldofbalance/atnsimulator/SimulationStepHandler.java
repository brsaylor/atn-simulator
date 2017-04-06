package edu.sfsu.worldofbalance.atnsimulator;

import org.apache.commons.math3.ode.sampling.FixedStepHandler;

/**
 * Fixed step handler for integrating ModelEquations.
 * Copies the integrator output into the biomass output array at a fixed time step.
 */
public class SimulationStepHandler implements FixedStepHandler {
    private int timestep;            // Current time step
    private double[][] outputArray;  // Biomass output array
    private double stepSize;        // Step size

    /**
     * Constructor.
     * @param outputArray biomass array with dimensions #timesteps x #nodes
     * @param stepSize step size
     */
    public SimulationStepHandler(double[][] outputArray, double stepSize) {
        timestep = -1;
        this.outputArray = outputArray;
        this.stepSize = stepSize;
    }

    @Override
    public void init(double t0, double[] y0, double t) {
    }

    @Override
    public void handleStep (double t, double[] y, double[] yDot, boolean isLast) {
        timestep = (int) Math.round(t / stepSize);
        // Ensure we don't go past the last time step due to rounding error
        if (timestep < outputArray.length) {
            System.arraycopy(y, 0, outputArray[timestep], 0, outputArray[timestep].length);
        }
    }

    /**
     * Get the last time step for which handleStep() was called.
     * @return the last handled time step
     */
    public int getLastHandledTimestep() {
        return timestep;
    }
}