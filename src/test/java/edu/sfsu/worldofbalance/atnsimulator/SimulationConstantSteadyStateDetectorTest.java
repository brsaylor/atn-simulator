package edu.sfsu.worldofbalance.atnsimulator;

import org.apache.commons.math3.ode.events.EventHandler;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SimulationConstantSteadyStateDetectorTest {
    private final double SURVIVING = 1;
    private final double ALMOST_EXTINCT = ModelEquations.EXTINCT * 1.1;
    private final double EXTINCT = ModelEquations.EXTINCT * 0.9;
    private final double t1 = 0.9;
    private final double t2 = 1.1;

    private ModelEquations equations;
    private SimulationConstantSteadyStateDetector detector;

    // Note: switching function g() modifies the state of the detector
    // to enable the eventOccurred() method to determine what type of event occurred

    @Test
    public void testSwitchingFunctionTotalExtinction() {
        makeSingleConsumerModel();
        assertTrue(detector.g(t1, new double[] {ALMOST_EXTINCT}) > 0);
        assertTrue(detector.g(t2, new double[] {EXTINCT}) < 0);
    }

    @Test
    public void testEventOccurredTotalExtinction() {
        makeSingleConsumerModel();
        double[] biomass = new double[] {EXTINCT};
        detector.g(t1, biomass);
        assertEquals(EventHandler.Action.STOP, detector.eventOccurred(t1, biomass, false));
        assertEquals(SimulationEventHandler.EventType.TOTAL_EXTINCTION, detector.getStopEvent());
        assertEquals(t1, detector.getTimeStopped(), 1e-20);
    }

    @Test
    public void testSwitchingFunctionLoneSaturatedProducer() {
        makeProducerConsumerModel();
        assertTrue(detector.g(t1, new double[] {nearCarryingCapacity(0), EXTINCT}) > 0);
        assertTrue(detector.g(t2, new double[] {carryingCapacity(0), EXTINCT}) < 0);
    }

    @Test
    public void testEventOccurredLoneSaturatedProducer() {
        makeProducerConsumerModel();
        double[] biomass = new double[] {carryingCapacity(0), EXTINCT};
        detector.g(t1, biomass);
        assertEquals(EventHandler.Action.STOP, detector.eventOccurred(t1, biomass, false));
        assertEquals(SimulationEventHandler.EventType.CONSTANT_BIOMASS_PRODUCERS_ONLY, detector.getStopEvent());
        assertEquals(t1, detector.getTimeStopped(), 1e-20);
    }

    @Test
    public void testSwitchingFunctionConstantBiomassWithConsumers() {
        makeProducerConsumerModel();
        holdConsumerBiomassConstant(1);
        assertTrue(detector.g(t1, new double[] {nearCarryingCapacity(0), SURVIVING}) > 0);
        assertTrue(detector.g(t2, new double[] {carryingCapacity(0), SURVIVING}) < 0);
    }

    @Test
    public void testEventOccurredConstantBiomassWithConsumers() {
        makeProducerConsumerModel();
        holdConsumerBiomassConstant(1);
        double[] biomass = new double[] {carryingCapacity(0), SURVIVING};
        detector.g(t1, biomass);
        assertEquals(EventHandler.Action.STOP, detector.eventOccurred(t1, biomass, false));
        assertEquals(SimulationEventHandler.EventType.CONSTANT_BIOMASS_WITH_CONSUMERS, detector.getStopEvent());
        assertEquals(t1, detector.getTimeStopped(), 1e-20);
    }

    private void makeSingleConsumerModel() {
        makeSingleNodeModel(NodeAttributes.NodeType.CONSUMER);
    }

    private void makeSingleProducerModel() {
        makeSingleNodeModel(NodeAttributes.NodeType.PRODUCER);
    }

    private void makeSingleNodeModel(NodeAttributes.NodeType nodeType) {
        FoodWeb web = new FoodWeb();
        web.addNode(0, new NodeAttributes(nodeType));
        ModelParameters parameters = new ModelParameters(web);
        equations = new ModelEquations(web, parameters);
        detector = new SimulationConstantSteadyStateDetector(equations);
    }

    private void makeProducerConsumerModel() {
        FoodWeb web = new FoodWeb();
        web.addProducerNode(0);
        web.addConsumerNode(1);
        web.addLink(0, 1);
        ModelParameters parameters = new ModelParameters(web);
        equations = new ModelEquations(web, parameters);
        detector = new SimulationConstantSteadyStateDetector(equations);
    }

    private double nearCarryingCapacity(int nodeId) {
        return equations.getParameters().carryingCapacity[nodeId] * 0.9;
    }

    private double carryingCapacity(int nodeId) {
        return equations.getParameters().carryingCapacity[nodeId];
    }

    private void holdConsumerBiomassConstant(int nodeId) {
        equations.getParameters().metabolicRate[nodeId] = 0;
    }
}
