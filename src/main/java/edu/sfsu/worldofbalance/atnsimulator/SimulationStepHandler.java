package edu.sfsu.worldofbalance.atnsimulator;

import org.apache.commons.math3.ode.sampling.FixedStepHandler;

import java.util.Arrays;

/**
 * Fixed step handler for integrating ModelEquations.
 * Copies the integrator output into the biomass output array at a fixed time step.
 */
public class SimulationStepHandler implements FixedStepHandler {
    private int timestep;            // Current time step
    private double[][] outputArray;  // Biomass output array
    private double stepSize;        // Step size
    private int[] extinctionTimesteps;

    /**
     * Constructor.
     * @param outputArray biomass array with dimensions #timesteps x #nodes
     * @param stepSize step size
     */
    public SimulationStepHandler(double[][] outputArray, double stepSize) {
        timestep = -1;
        this.outputArray = outputArray;
        this.stepSize = stepSize;
        this.extinctionTimesteps = new int[outputArray[0].length];
        Arrays.fill(extinctionTimesteps, -1);
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

        // Record any extinctions that occurred this timestep
        for (int i = 0; i < y.length; i++)
            if (extinctionTimesteps[i] == -1 && y[i] < ModelEquations.EXTINCT)
                extinctionTimesteps[i] = timestep;
    }

    /**
     * @return the timesteps at which each node went extinct (-1 for no extinction)
     */
    public int[] getExtinctionTimesteps() {
        return extinctionTimesteps;
    }

    /**
     * @return the last time step for which handleStep() was called.
     */
    public int getLastHandledTimestep() {
        return timestep;
    }
}