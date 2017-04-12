package edu.sfsu.worldofbalance.atnsimulator;

import org.apache.commons.math3.ode.sampling.FixedStepHandler;

import java.util.Arrays;

/**
 * Fixed step handler for integrating ModelEquations.
 * Copies the integrator output into the biomass output array (if not null) at a fixed time step.
 * Records timesteps of extinction events.
 */
public class SimulationStepHandler implements FixedStepHandler {
    private int timestep;        // Current time step
    private double[][] biomass;  // Biomass output array
    private double stepSize;     // Step size
    private int[] extinctionTimesteps;
    private boolean recordBiomass;

    /**
     * @param nodeCount number of nodes
*      @param biomass biomass output array (null if biomass series should not be stored)
     * @param stepSize interval between timesteps
     */
    public SimulationStepHandler(int nodeCount, double[][] biomass, double stepSize) {
        timestep = -1;
        this.biomass = biomass;
        this.stepSize = stepSize;
        this.extinctionTimesteps = new int[nodeCount];
        Arrays.fill(extinctionTimesteps, -1);
        this.recordBiomass = biomass != null;
    }

    @Override
    public void init(double t0, double[] y0, double t) {
    }

    @Override
    public void handleStep (double t, double[] y, double[] yDot, boolean isLast) {
        timestep = (int) Math.round(t / stepSize);
        // Ensure we don't go past the last time step due to rounding error
        if (recordBiomass && timestep < biomass.length) {
            System.arraycopy(y, 0, biomass[timestep], 0, biomass[timestep].length);
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