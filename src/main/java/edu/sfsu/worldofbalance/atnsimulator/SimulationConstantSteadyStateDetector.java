package edu.sfsu.worldofbalance.atnsimulator;

import org.apache.commons.math3.ode.events.EventHandler;

/**
 * Handler for discrete events encountered during integration of ModelEquations.
 * It stops the integration when the system reaches a steady state, such as:
 * - All nodes are extinct
 * - Consumers are extinct and producers have reached carrying capacity
 * - All derivatives are effectively 0
 */
public class SimulationConstantSteadyStateDetector extends SimulationEventHandler {

    public final double ABS_RELATIVE_DERIVATIVE_THRESHOLD = 1e-10;

    private int[] producers;  // i/j indices of producers in parameter arrays
    private int[] consumers;  // i/j indices of consumers in parameter arrays

    private double[] BDot;                 // Derivative of biomass of each node at time t
    private double maxBiomass;             // Maximum biomass of a node at time t
    private double maxAbsRelDerivative;    // Maximum absolute value of a derivative relative to biomass at time t

    public SimulationConstantSteadyStateDetector(ModelEquations equations) {
        super(equations);
        producers = equations.getProducers();
        consumers = equations.getConsumers();
        BDot = new double[equations.getDimension()];
    }

    @Override
    public void init(double t0, double[] y0, double t) {
        timeStopped = -1;
    }

    /**
     * Compute the switching function.
     * When the sign of this continuous function changes,
     * an event is triggered and handled by eventOccurred().
     *
     * @param t Time
     * @param Bt Biomass of each node at time t
     * @return the value of the switching function
     */
    @Override
    public double g(double t, double[] Bt) {

        // When maxBiomass goes below extinction threshold, all nodes are extinct
        // When the derivatives are effectively 0, the system is in a steady state
        maxBiomass = 0;
        maxAbsRelDerivative = 0;
        equations.computeDerivatives(t, Bt, BDot);  // FIXME: This must be a recomputation - how to avoid?
        for (int i = 0; i < Bt.length; i++) {
            maxBiomass = Math.max(maxBiomass, Bt[i]);
            double absRelDerivative = Bt[i] == 0 ? 0 : Math.abs(BDot[i] / Bt[i]);
            maxAbsRelDerivative = Math.max(maxAbsRelDerivative, Math.abs(absRelDerivative));
        }

        return Math.min(maxBiomass - ModelEquations.EXTINCT, maxAbsRelDerivative - ABS_RELATIVE_DERIVATIVE_THRESHOLD);
    }

    /**
     * Handle an event and stop the integration.
     * @param t Time
     * @param Bt Biomass of each node at time t
     * @param increasing Must be set to false using an EventFilter
     * @return
     */
    @Override
    public EventHandler.Action eventOccurred(double t, double[] Bt, boolean increasing) {
        timeStopped = t;
        if (maxBiomass <= ModelEquations.EXTINCT) {
            stopEvent = EventType.TOTAL_EXTINCTION;
        } else if (maxAbsRelDerivative <= ABS_RELATIVE_DERIVATIVE_THRESHOLD) {
            // Check if any consumers are still alive
            boolean consumerAlive = false;
            for (int i : consumers) {
                if (Bt[i] > ModelEquations.EXTINCT) {
                    consumerAlive = true;
                    break;
                }
            }
            if (consumerAlive) {
                stopEvent = EventType.CONSTANT_BIOMASS_WITH_CONSUMERS;
            } else {
                stopEvent = EventType.CONSTANT_BIOMASS_PRODUCERS_ONLY;
            }
        } else {
            // FIXME: This happens very occasionally, but probably shouldn't
            stopEvent = EventType.UNKNOWN_EVENT;
        }
        return Action.STOP;
    }
}
