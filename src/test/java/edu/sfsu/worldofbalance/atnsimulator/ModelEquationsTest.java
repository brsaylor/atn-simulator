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
        web.addNode(1);
        ModelParameters parameters = new ModelParameters(1);
        new ModelEquations(web, parameters);
    }

    @Test(expected = IncorrectParameterDimensionsException.class)
    public void testIncorrectParameterDimensions() {
        web.addNode(0);
        ModelParameters parameters = new ModelParameters(2);
        new ModelEquations(web, parameters);
    }

    @Test(expected = EmptyFoodWebException.class)
    public void testEmpty() {
        ModelParameters parameters = new ModelParameters(0);
        new ModelEquations(web, parameters);
    }

    @Test
    public void testComputeDerivativesZero() {
        web.addNode(0);
        web.addNode(1);
        web.setNodeAttributes(0, new NodeAttributes(PRODUCER));
        web.setNodeAttributes(1, new NodeAttributes(CONSUMER));
        web.addLink(0, 1);
        ModelParameters parameters = new ModelParameters(web.nodeCount());
        setDenominatorParametersToOne(parameters);
        ModelEquations equations = new ModelEquations(web, parameters);
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
