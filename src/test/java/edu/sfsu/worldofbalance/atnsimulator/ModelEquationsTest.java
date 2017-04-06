package edu.sfsu.worldofbalance.atnsimulator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

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
        new ModelEquations(web, parameters);
    }

    @Test(expected = IncorrectParameterDimensionsException.class)
    public void testIncorrectParameterDimensions() {
        web.addProducerNode(0);
        ModelParameters parameters = new ModelParameters(2);
        ModelEquations equations = new ModelEquations(web, parameters);
    }

    @Test(expected = EmptyFoodWebException.class)
    public void testEmpty() {
        ModelParameters parameters = new ModelParameters(0);
        new ModelEquations(web, parameters);
    }

    @Test
    public void testComputeDerivativesZero() {
        web.addProducerNode(0);
        web.addConsumerNode(1);
        web.addLink(0, 1);
        ModelParameters parameters = new ModelParameters(web.nodeCount());
        setDenominatorParametersToOne(parameters);
        ModelEquations equations = new ModelEquations(web, parameters);
        double[] BDot = new double[web.nodeCount()];
        equations.computeDerivatives(0, new double[web.nodeCount()], BDot);
        double[] expectedBDot = new double[web.nodeCount()];
        assertArrayEquals(expectedBDot, BDot, 1e-20);
    }

    @Test
    public void testSingleProducer() {
        web.addProducerNode(0);
        ModelParameters parameters = new ModelParameters(web);
        ModelEquations equations = new ModelEquations(web, parameters);
        double[] Bt = new double[] {0.5};
        double[] BDot = new double[1];
        equations.computeDerivatives(0, Bt, BDot);
        assertTrue(BDot[0] > 0);
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
