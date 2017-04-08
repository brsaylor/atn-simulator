package edu.sfsu.worldofbalance.atnsimulator;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import static junit.framework.TestCase.assertTrue;

public class BatchSimulationTaskTest {

    private static FoodWeb serengeti;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUp() {
        Reader reader = new InputStreamReader(
                BatchSimulationTaskTest.class.getResourceAsStream("/foodwebs/serengeti.json"));
        serengeti = FoodWeb.createFromJson(reader);
    }

    @Test
    public void testTaskProducesOutputFile() throws IOException {
        int simulationId = 123;
        File outputDirectory = tempFolder.newFolder();
        SimulationParameters parameters = new SimulationParameters();
        parameters.stepSize = 0.1;
        parameters.stopOnSteadyState = false;
        parameters.timesteps = 100;
        String nodeConfig = "5,[3],4112.19,20.0,2,K=3134.36,R=1.0,0,[55],3975.08,0.213,1,X=0.54461,0,[71],216.842,4.99,1,X=0.233554,0,[74],1438.01,23.8,1,X=0.642048,0,[80],128.628,41.5,1,X=0.501792,0";
        int nodeConfigBiomassScale = 1000;

        BatchSimulationTask task = new BatchSimulationTask(
                serengeti,
                simulationId,
                parameters,
                nodeConfig,
                nodeConfigBiomassScale,
                outputDirectory);
        task.run();

        File expectedOutputFile = new File(outputDirectory, "ATN_123.h5");
        assertTrue(expectedOutputFile.isFile());
        assertTrue(expectedOutputFile.length() > 0);
    }
}
