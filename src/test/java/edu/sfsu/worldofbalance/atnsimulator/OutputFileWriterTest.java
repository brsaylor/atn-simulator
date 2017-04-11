package edu.sfsu.worldofbalance.atnsimulator;

import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5SimpleReader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;

public class OutputFileWriterTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testWrite() throws IOException {
        File outputDirectory = tempFolder.newFolder("output");

        int timesteps = 10;
        int nodeCount = 2;

        FoodWeb web = new FoodWeb();
        web.addProducerNode(0);
        web.addConsumerNode(1);
        web.addLink(0, 1);

        SimulationParameters simulationParameters = new SimulationParameters();
        simulationParameters.timesteps = timesteps;
        simulationParameters.stepSize = 0.1;
        simulationParameters.stopOnSteadyState = true;
        ModelParameters modelParameters = new ModelParameters(web);

        SimulationResults results = new SimulationResults(timesteps, nodeCount);
        results.simulationParameters = simulationParameters;
        results.modelParameters = modelParameters;
        for (int t = 0; t < timesteps; t++) {
            for (int i = 0; i < nodeCount; i++) {
                results.biomass[t][i]  = t * 100 + i;
            }
        }
        results.extinctionTimesteps = new int[] {-1, 1};
        results.stopEvent = SimulationEventHandler.EventType.OSCILLATING_STEADY_STATE;
        results.timestepsSimulated = timesteps;

        OutputFileWriter writer = new OutputFileWriter(outputDirectory);
        int simulationId = 1;
        int[] nodeIds = new int[] {0, 1};

        OutputFileData data = new OutputFileData();
        data.simulationId = simulationId;
        data.simulationResults = results;
        data.nodeConfig = "placeholder";
        data.nodeConfigBiomassScale = 1000;
        data.originalNodeIds = nodeIds;
        data.originalSubweb = web;
        writer.write(data);

        File outputFile = new File(outputDirectory, "ATN_1.h5");
        assertTrue(outputFile.exists());

        IHDF5SimpleReader reader = HDF5Factory.openForReading(outputFile);

        assertEquals(web.toJson(), reader.readString("/food_web_json"));
        assertMatrixEquals(results.biomass, reader.readFloatMatrix("/biomass"));
        assertArrayEquals(results.extinctionTimesteps, reader.readIntArray("/extinction_timesteps"));
        assertArrayEquals(nodeIds, reader.readIntArray("/node_ids"));
        assertEquals(data.nodeConfig, reader.readString("/node_config"));
        assertEquals(data.nodeConfigBiomassScale, reader.readDouble("/node_config_biomass_scale"));
        assertEquals(results.stopEvent.toString(), reader.readString("/stop_event"));

        // Simulation parameters
        assertEquals(data.simulationResults.simulationParameters.stepSize,
                reader.readDouble("/parameters/simulation/step_size"));
        assertEquals(data.simulationResults.simulationParameters.stopOnSteadyState,
                reader.readBoolean("/parameters/simulation/stop_on_steady_state"));

        // System parameters
        assertEquals(modelParameters.useSystemCarryingCapacity,
                reader.readBoolean("/parameters/system/use_system_carrying_capacity"));
        assertEquals(modelParameters.systemCarryingCapacity,
                reader.readDouble("/parameters/system/system_carrying_capacity"));

        // Node parameters
        assertArrayEquals(modelParameters.metabolicRate,
                reader.readDoubleArray("/parameters/node/metabolic_rate"), 1e-20);
        assertArrayEquals(modelParameters.growthRate,
                reader.readDoubleArray("/parameters/node/growth_rate"), 1e-20);
        assertArrayEquals(modelParameters.carryingCapacity,
                reader.readDoubleArray("/parameters/node/carrying_capacity"), 1e-20);

        // Link parameters
        assertMatrixEquals(modelParameters.maximumIngestionRate,
                reader.readDoubleMatrix("/parameters/link/maximum_ingestion_rate"));
        assertMatrixEquals(modelParameters.predatorInterference,
                reader.readDoubleMatrix("/parameters/link/predator_interference"));
        assertMatrixEquals(modelParameters.functionalResponseControl,
                reader.readDoubleMatrix("/parameters/link/functional_response_control"));
        assertMatrixEquals(modelParameters.relativeHalfSaturationDensity,
                reader.readDoubleMatrix("/parameters/link/relative_half_saturation_density"));
        assertMatrixEquals(modelParameters.halfSaturationDensity,
                reader.readDoubleMatrix("/parameters/link/half_saturation_density"));
        assertMatrixEquals(modelParameters.assimilationEfficiency,
                reader.readDoubleMatrix("/parameters/link/assimilation_efficiency"));

        reader.close();
    }

    private void assertMatrixEquals(double[][] expected, float[][] actual) {
        assertEquals(expected.length, actual.length);
        float[] expectedRow = new float[expected[0].length];
        for (int i = 0; i < expected.length; i++) {
            for (int j = 0; j < expectedRow.length; j++)
                expectedRow[j] = (float) expected[i][j];
            assertArrayEquals(expectedRow, actual[i], 1e-20f);
        }
    }

    private void assertMatrixEquals(double[][] expected, double[][] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++)
            assertArrayEquals(expected[i], actual[i], 1e-20);
    }
}
