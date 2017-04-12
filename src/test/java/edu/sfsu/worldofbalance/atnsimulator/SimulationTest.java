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

        ModelParameters parameters = new ModelParameters(web);
        parameters.metabolicRate[1] = 100;

        // Absence of link and high metabolic rate will make the consumer go extinct quickly

        ModelEquations equations = new ModelEquations(web, parameters);

        SimulationParameters simParams = new SimulationParameters();
        simParams.stepSize = 0.1;
        simParams.timesteps = 10;
        simParams.stopOnSteadyState = false;

        double[] initialBiomass = {1, 1};
        Simulation simulation = new Simulation(simParams, equations, initialBiomass);
        simulation.run();
        SimulationResults results = simulation.getResults();

        // Producer doesn't go extinct
        assertEquals(-1, results.extinctionTimesteps[0]);

        int consumerExtinctionTimestep = -1;
        for (int t = 0; t < simParams.timesteps; t++) {
            if (results.biomass[t][1] < ModelEquations.EXTINCT) {
                consumerExtinctionTimestep = t;
                break;
            }
        }
        assertTrue(consumerExtinctionTimestep > -1);

        // Consumer goes extinct
        assertEquals(consumerExtinctionTimestep, results.extinctionTimesteps[1]);
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
