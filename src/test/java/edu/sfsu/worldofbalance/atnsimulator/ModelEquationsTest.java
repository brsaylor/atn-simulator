package edu.sfsu.worldofbalance.atnsimulator;

import org.junit.Before;
import org.junit.Test;

import static edu.sfsu.worldofbalance.atnsimulator.NodeAttributes.NodeType.CONSUMER;
import static edu.sfsu.worldofbalance.atnsimulator.NodeAttributes.NodeType.PRODUCER;
import static org.junit.Assert.assertArrayEquals;

// TODO: More comprehensive tests
// - most testing done by comparing simulation output with implementation in wob-server
public class ModelEquationsTest {

    private FoodWeb web;

    @Before
    public void setUp() {
        web = new FoodWeb();
    }

    @Test(expected = FoodWebNotNormalizedException.class)
    public void testFoodWebNotNormalized() {
        web.addProducerNode(1);
        ModelParameters parameters = new ModelParameters(1);
        new ModelEquations(web);
    }

    @Test(expected = IncorrectParameterDimensionsException.class)
    public void testIncorrectParameterDimensions() {
        web.addProducerNode(0);
        ModelParameters parameters = new ModelParameters(2);
        ModelEquations equations = new ModelEquations(web);
        equations.setParameters(parameters);
    }

    @Test(expected = EmptyFoodWebException.class)
    public void testEmpty() {
        ModelParameters parameters = new ModelParameters(0);
        new ModelEquations(web);
    }

    @Test
    public void testComputeDerivativesZero() {
        web.addProducerNode(0);
        web.addConsumerNode(1);
        web.addLink(0, 1);
        ModelParameters parameters = new ModelParameters(web.nodeCount());
        setDenominatorParametersToOne(parameters);
        ModelEquations equations = new ModelEquations(web);
        equations.setParameters(parameters);
        double[] BDot = new double[web.nodeCount()];
        equations.computeDerivatives(0, new double[web.nodeCount()], BDot);
        double[] expectedBDot = new double[web.nodeCount()];
        assertArrayEquals(expectedBDot, BDot, 1e-20);
    }

    private void setDenominatorParametersToOne(ModelParameters parameters) {
        int nodeCount = parameters.metabolicRate.length;
        for (int i = 0; i < nodeCount; i++) {
            parameters.carryingCapacity[i] = 1;
            for (int j = 0; j < nodeCount; j++) {
                parameters.assimilationEfficiency[i][j] = 1;
                parameters.halfSaturationDensity[i][j] = 1;
            }
        }
    }
}
