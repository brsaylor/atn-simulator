package edu.sfsu.worldofbalance.atnsimulator;

import org.junit.Test;

import javax.jws.WebParam;

import static org.junit.Assert.*;

// TODO: More comprehensive tests
// - most testing done by comparing simulation output with implementation in wob-server
public class SimulationTest {

    @Test(expected = IncorrectParameterDimensionsException.class)
    public void testIncorrectDimensions() {
        FoodWeb web = new FoodWeb();
        web.addProducerNode(0);
        ModelParameters parameters = new ModelParameters(1);
        ModelEquations equations = new ModelEquations(web, parameters);
        new Simulation(new SimulationParameters(), equations, new double[2]);
    }

    @Test
    public void testSingleProducer() {
        FoodWeb web = new FoodWeb();
        web.addProducerNode(0);

        ModelParameters parameters = new ModelParameters(web);
        ModelEquations equations = new ModelEquations(web, parameters);

        SimulationParameters simParams = new SimulationParameters();
        simParams.stepSize = 0.1;
        simParams.timesteps = 100;
        simParams.stopOnSteadyState = false;

        double[] initialBiomass = new double[] {0.1};
        Simulation simulation = new Simulation(simParams, equations, initialBiomass);
        simulation.run();
        SimulationResults results = simulation.getResults();

        assertEquals(simParams.timesteps, results.biomass.length);
        assertTrue(biomassIsIncreasing(results.biomass, 0));
    }

    @Test
    public void testSingleConsumer() {
        FoodWeb web = new FoodWeb();
        web.addConsumerNode(0);

        ModelParameters parameters = new ModelParameters(web);
        ModelEquations equations = new ModelEquations(web, parameters);

        SimulationParameters simParams = new SimulationParameters();
        simParams.stepSize = 0.1;
        simParams.timesteps = 100;
        simParams.stopOnSteadyState = false;

        double[] initialBiomass = {1};
        Simulation simulation = new Simulation(simParams, equations, initialBiomass);
        simulation.run();
        SimulationResults results = simulation.getResults();

        assertTrue(biomassIsDecreasing(results.biomass, 0));
    }

    @Test
    public void testWellFedConsumer() {
        FoodWeb web = new FoodWeb();
        web.addProducerNode(0);
        web.addConsumerNode(1);
        web.addLink(0, 1);

        ModelParameters parameters = new ModelParameters(web);
        parameters.carryingCapacity[0] = 1e10;
        ModelEquations equations = new ModelEquations(web, parameters);

        SimulationParameters simParams = new SimulationParameters();
        simParams.stepSize = 0.1;
        simParams.timesteps = 10;
        simParams.stopOnSteadyState = false;

        double[] initialBiomass = {1, 0.1};
        Simulation simulation = new Simulation(simParams, equations, initialBiomass);
        simulation.run();
        SimulationResults results = simulation.getResults();

        assertTrue(biomassIsIncreasing(results.biomass, 1));
    }

    @Test
    public void testZeroBiomass() {
        FoodWeb web = new FoodWeb();
        web.addProducerNode(0);
        web.addConsumerNode(1);
        web.addLink(0, 1);

        ModelParameters parameters = new ModelParameters(web);

        ModelEquations equations = new ModelEquations(web, parameters);

        SimulationParameters simParams = new SimulationParameters();
        simParams.stepSize = 0.1;
        simParams.timesteps = 10;
        simParams.stopOnSteadyState = false;

        Simulation simulation = new Simulation(simParams, equations, new double[web.nodeCount()]);
        simulation.run();
        SimulationResults results = simulation.getResults();

        assertEquals(simParams.timesteps, results.biomass.length);

        double[] expectedBiomassRow = new double[web.nodeCount()];
        for (double[] biomassRow : results.biomass) {
            assertArrayEquals(expectedBiomassRow, biomassRow, 1e-20);
        }
    }

    @Test
    public void testExtinctionTimesteps() {
        FoodWeb web = new FoodWeb();
        web.addProducerNode(0);
        web.addConsumerNode(1);
        web.addConsumerNode(2);

        ModelParameters parameters = new ModelParameters(web);
        parameters.metabolicRate[1] = 200;  // Make producer 1 go extinct first
        parameters.metabolicRate[2] = 100;  // Make producer 2 go extinct second

        // Absence of link and high metabolic rate will make the consumer go extinct quickly

        ModelEquations equations = new ModelEquations(web, parameters);

        SimulationParameters simParams = new SimulationParameters();
        simParams.stepSize = 0.1;
        simParams.timesteps = 10;
        simParams.stopOnSteadyState = false;

        double[] initialBiomass = {1, 1, 1};
        Simulation simulation = new Simulation(simParams, equations, initialBiomass);
        simulation.run();
        SimulationResults results = simulation.getResults();

        assertEquals(-1, results.extinctionTimesteps[0]);

        // Consumer shouldn't go extinct; leave at -1.
        // Producer 1 should go extinct, then producer 2 should go extinct.
        int[] expectedExtinctionTimesteps = new int[] {-1, -1, -1};
        for (int consumer = 1; consumer < 3; consumer++) {
            for (int t = 0; t < simParams.timesteps; t++) {
                if (results.biomass[t][consumer] < ModelEquations.EXTINCT) {
                    expectedExtinctionTimesteps[consumer] = t;
                    break;
                }
            }
        }

        assertTrue(expectedExtinctionTimesteps[1] != -1 && expectedExtinctionTimesteps[2] != -1);
        assertTrue(expectedExtinctionTimesteps[1] < expectedExtinctionTimesteps[2]);
        assertArrayEquals(expectedExtinctionTimesteps, results.extinctionTimesteps);
    }

    @Test
    public void testNoRecordBiomass() {
        FoodWeb web = new FoodWeb();
        web.addProducerNode(0);
        ModelParameters mp = new ModelParameters(web);
        ModelEquations equations = new ModelEquations(web, mp);
        SimulationParameters sp = new SimulationParameters();
        sp.timesteps = 10;
        sp.stepSize = 0.1;
        sp.stopOnSteadyState = false;
        sp.recordBiomass = false;
        Simulation sim = new Simulation(sp, equations, new double[] {1});
        sim.run();
        SimulationResults results = sim.getResults();
        assertNull(results.biomass);
    }

    @Test
    public void testFinalBiomass() {
        FoodWeb web = new FoodWeb();
        web.addProducerNode(0);
        ModelParameters mp = new ModelParameters(web);
        ModelEquations equations = new ModelEquations(web, mp);
        SimulationParameters sp = new SimulationParameters();
        sp.timesteps = 10;
        sp.stepSize = 0.1;
        sp.stopOnSteadyState = false;
        sp.recordBiomass = false;
        double[] initialBiomass = new double[] {0.5};
        Simulation sim = new Simulation(sp, equations, initialBiomass);
        sim.run();
        SimulationResults results = sim.getResults();
        assertTrue(results.finalBiomass[0] > initialBiomass[0]);
    }

    private boolean biomassIsIncreasing(double[][] biomass, int nodeId) {
        for (int t = 1; t < biomass.length; t++)
            if (biomass[t-1][nodeId] >= biomass[t][nodeId])
                return false;
        return true;
    }

    private boolean biomassIsDecreasing(double[][] biomass, int nodeId) {
        for (int t = 1; t < biomass.length; t++)
            if (biomass[t-1][nodeId] <= biomass[t][nodeId])
                return false;
        return true;
    }
}
