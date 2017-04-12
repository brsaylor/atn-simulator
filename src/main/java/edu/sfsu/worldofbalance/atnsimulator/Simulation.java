package edu.sfsu.worldofbalance.atnsimulator;

import org.apache.commons.math3.analysis.solvers.BisectionSolver;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.events.EventFilter;
import org.apache.commons.math3.ode.events.FilterType;
import org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerIntegrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepNormalizer;
import org.apache.commons.math3.ode.sampling.StepNormalizerBounds;
import org.apache.commons.math3.ode.sampling.StepNormalizerMode;

import java.util.Arrays;

/**
 * Runs a simulation using a given model. Basic usage consists of
 * calling run() followed by getResults().
 */
public class Simulation implements Runnable {

    private int nodeCount;
    private SimulationParameters simulationParameters;
    private int timesteps;
    private double stepSize;
    private boolean stopOnSteadyState;
    private ModelEquations equations;
    private double[] initialBiomass;
    private FirstOrderIntegrator integrator;
    private SimulationStepHandler stepHandler;
    private SimulationConstantSteadyStateDetector constantDetector;
    private SimulationOscillatingSteadyStateDetector oscillationDetector;
    private SimulationResults results;

    /**
     * @param simulationParameters the parameters for the simulation
     * @param equations the parameterized ATN model equations to simulate
     * @param initialBiomass the biomass at timestep 0
     */
    public Simulation(SimulationParameters simulationParameters, ModelEquations equations, double[] initialBiomass) {
        if (equations.getDimension() != initialBiomass.length)
            throw new IncorrectParameterDimensionsException();
        this.nodeCount = initialBiomass.length;
        this.simulationParameters = simulationParameters;
        this.timesteps = simulationParameters.timesteps;
        this.stepSize = simulationParameters.stepSize;
        this.stopOnSteadyState = simulationParameters.stopOnSteadyState;
        this.equations = equations;
        this.initialBiomass = initialBiomass;
    }

    /**
     * Run the simulation and store the results.
     */
    public void run() {
        results = new SimulationResults(simulationParameters, equations.getParameters());
        initializeIntegrator();
        if (stopOnSteadyState) {
            constantDetector = new SimulationConstantSteadyStateDetector(equations);
            oscillationDetector = new SimulationOscillatingSteadyStateDetector(equations);
            addConstantSteadyStateDetector();
            // Oscillating steady state detector will be added after the simulation runs a while
        }
        doIntegration();
    }

    /**
     * Retrieve the results of this simulation after run() has completed.
     * @return the results of the simulation
     */
    public SimulationResults getResults() {
        return results;
    }

    private void initializeIntegrator() {
        integrator = new GraggBulirschStoerIntegrator(
                1.0e-8,    // minimal step
                100.0,     // maximal step
                equations.EXTINCT,  // allowed absolute error
                1.0e-10);  // allowed relative error

        // Set up the StepHandler, which is triggered at each time step by the integrator,
        // and copies the current biomass of each species into calcBiomass[timestep].
        // See the "Continuous Output" section of https://commons.apache.org/proper/commons-math/userguide/ode.html
        stepHandler = new SimulationStepHandler(results.biomass, stepSize);
        StepHandler stepNormalizer = new StepNormalizer(
                stepSize, stepHandler,
                StepNormalizerMode.MULTIPLES,  // step at multiples of stepSize
                StepNormalizerBounds.FIRST);   // ensure the first time step is handled
        integrator.addStepHandler(stepNormalizer);
    }

    private void addConstantSteadyStateDetector() {
        // TODO: Choose best parameter values
        integrator.addEventHandler(new EventFilter(constantDetector, FilterType.TRIGGER_ONLY_DECREASING_EVENTS),
                1,  // maximal time interval between switching function checks (this interval prevents missing sign changes in case the integration steps becomes very large)
                0.0001,  // convergence threshold in the event time search
                1000,  // upper limit of the iteration count in the event time search
                new BisectionSolver()
        );
    }

    private void addOscillatingSteadyStateDetector() {
        integrator.addEventHandler(oscillationDetector, stepSize, 0.0001, 1000, new BisectionSolver());
    }

    private void doIntegration() {

        // Initialize biomass data for timestep 0
        if (simulationParameters.recordBiomass)
            System.arraycopy(initialBiomass, 0, results.biomass[0], 0, initialBiomass.length);
        double[] currentBiomass = Arrays.copyOf(initialBiomass, initialBiomass.length);

        // Run the integrator to compute the biomass time series.
        // The integration is run in chunks to facilitate the use of the oscillation detection event handler.
        // Because the period of an oscillating state could be of any length,
        // we double the chunk length each time.
        int prevStartTimestep = -1;
        for (int i = 0, startTimestep = 0, endTimestep = Math.min(1000, timesteps);

             startTimestep < timesteps && startTimestep > prevStartTimestep;

             prevStartTimestep = startTimestep,
                     startTimestep = stepHandler.getLastHandledTimestep(),
                     endTimestep = Math.min(timesteps, endTimestep * 2),
                     i++) {

            // Only start checking for oscillations starting with the second integration
            if (stopOnSteadyState && i == 1) {
                addOscillatingSteadyStateDetector();
            }

            try {
                integrator.integrate(equations,
                        startTimestep * stepSize,
                        currentBiomass,
                        endTimestep * stepSize,
                        currentBiomass);
            } catch (NoBracketingException e) {
                System.err.println();
                System.err.println(e);
                System.err.println("\n*** NoBracketingException caught; removing event handlers\n");
                integrator.clearEventHandlers();
            }

            if (stopOnSteadyState
                    && (constantDetector.integrationWasStopped() || oscillationDetector.integrationWasStopped())) {
                break;
            }
        }

        if (stopOnSteadyState && constantDetector.integrationWasStopped()) {
            results.timestepsSimulated = (int) (constantDetector.getTimeStopped() / stepSize);
            results.stopEvent = constantDetector.getStopEvent();
        } else if (stopOnSteadyState && oscillationDetector != null && oscillationDetector.integrationWasStopped()) {
            results.timestepsSimulated = (int) (oscillationDetector.getTimeStopped() / stepSize);
            results.stopEvent = oscillationDetector.getStopEvent();
        } else {
            results.timestepsSimulated = timesteps;
            results.stopEvent = SimulationEventHandler.EventType.NONE;
        }
        if (simulationParameters.recordBiomass)
            results.timestepsSimulated = Math.min(results.timestepsSimulated, results.biomass.length);
        results.extinctionTimesteps = stepHandler.getExtinctionTimesteps();
        System.arraycopy(currentBiomass, 0, results.finalBiomass, 0, nodeCount);
    }
}