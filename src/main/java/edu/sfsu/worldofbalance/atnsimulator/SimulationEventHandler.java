package edu.sfsu.worldofbalance.atnsimulator;

import org.apache.commons.math3.ode.events.EventHandler;

public abstract class SimulationEventHandler implements EventHandler {
    public enum EventType {
        NONE,
        UNKNOWN_EVENT,
        TOTAL_EXTINCTION,
        CONSTANT_BIOMASS_PRODUCERS_ONLY,
        CONSTANT_BIOMASS_WITH_CONSUMERS,
        OSCILLATING_STEADY_STATE
    }

    protected ModelEquations equations;
    protected EventType stopEvent = EventType.NONE;
    protected double timeStopped = -1;  // Time at which the integration was stopped

    public SimulationEventHandler(ModelEquations equations) {
        this.equations = equations;
    }

    /**
     * @return the time at which the integration was stopped due to detection of a steady state
     */
    public double getTimeStopped() {
        return timeStopped;
    }

    /**
     * @return true if the integration was stopped
     */
    public boolean integrationWasStopped() {
        return timeStopped != -1;
    }

    public EventType getStopEvent() {
        return stopEvent;
    }

    @Override
    public void resetState(double t, double[] y) { }
}
