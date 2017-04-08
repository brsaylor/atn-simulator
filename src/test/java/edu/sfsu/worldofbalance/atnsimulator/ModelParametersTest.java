package edu.sfsu.worldofbalance.atnsimulator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static edu.sfsu.worldofbalance.atnsimulator.NodeAttributes.NodeType.PRODUCER;
import static edu.sfsu.worldofbalance.atnsimulator.NodeAttributes.NodeType.CONSUMER;

// ModelParameters is an essentially behavior-less data structure,
// but still needs testing, because it instantiates itself in nontrivial ways.
public class ModelParametersTest {

    @Test
    public void testNodeCountConstructor() {
        ModelParameters parameters = new ModelParameters(2);

        double v = ModelParameters.Defaults.metabolicRate;
        assertArrayEquals(new double[] {v, v}, parameters.metabolicRate, 1e-20);

        assertEquals(parameters.assimilationEfficiency.length, 2);
        v = ModelParameters.Defaults.assimilationEfficiency;
        for (double[] row : parameters.assimilationEfficiency)
            assertArrayEquals(new double[] {v, v}, row, 1e-20);
    }

    @Test
    public void testFoodWebConstructor() {
        FoodWeb web = new FoodWeb();
        web.addProducerNode(0);
        web.addConsumerNode(1);
        web.addConsumerNode(2);

        ModelParameters parameters = new ModelParameters(web);

        double v = ModelParameters.Defaults.metabolicRate;
        assertArrayEquals(new double[] {v, v, v}, parameters.metabolicRate, 1e-20);

        assertEquals(parameters.assimilationEfficiency.length, 3);
        double p = ModelParameters.Defaults.assimilationEfficiencyPlant;
        double a = ModelParameters.Defaults.assimilationEfficiencyAnimal;
        for (double[] row : parameters.assimilationEfficiency)
            assertArrayEquals(new double[] {p, a, a}, row, 1e-20);
    }

    @Test
    public void applyFoodWebDependentDefaults() {
        FoodWeb web = new FoodWeb();
        web.addProducerNode(0);
        web.addConsumerNode(1);
        ModelParameters parameters = new ModelParameters(2);

        parameters.applyFoodWebDependentDefaults(web);

        double p = ModelParameters.Defaults.assimilationEfficiencyPlant;
        double a = ModelParameters.Defaults.assimilationEfficiencyAnimal;
        for (double[] row : parameters.assimilationEfficiency)
            assertArrayEquals(new double[] {p, a}, row, 1e-20);
    }
}
