package edu.sfsu.worldofbalance.atnsimulator;

import java.io.File;

/**
 * Runs a single simulation from a node config string
 * and saves the results to a file.
 */
public class BatchSimulationTask implements Runnable {

    private FoodWeb fullFoodWeb;
    private int simulationId;
    private SimulationParameters simulationParameters;
    private String nodeConfig;
    private int nodeConfigBiomassScale;
    private File outputDirectory;

    public BatchSimulationTask(
            FoodWeb fullFoodWeb,
            int simulationId,
            SimulationParameters simulationParameters,
            String nodeConfig,
            int nodeConfigBiomassScale,
            File outputDirectory) {
        this.fullFoodWeb = fullFoodWeb;
        this.simulationId = simulationId;
        this.simulationParameters = simulationParameters;
        this.nodeConfig = nodeConfig;
        this.nodeConfigBiomassScale = nodeConfigBiomassScale;
        this.outputDirectory = outputDirectory;
    }

    @Override
    public void run() {
        NodeConfigParser parser = new NodeConfigParser(nodeConfigBiomassScale);
        NodeConfigParser.Result parseResult = parser.parse(nodeConfig);

        FoodWeb subweb = fullFoodWeb.subweb(parseResult.nodeIds);
        FoodWeb normalizedSubweb = subweb.normalizedCopy(parseResult.nodeIds);
        parseResult.parameters.applyFoodWebDependentDefaults(normalizedSubweb);

        ModelEquations equations = new ModelEquations(normalizedSubweb, parseResult.parameters);
        Simulation simulation = new Simulation(simulationParameters, equations, parseResult.initialBiomass);

        simulation.run();

        OutputFileData data = new OutputFileData();
        data.simulationId = simulationId;
        data.simulationResults = simulation.getResults();
        data.nodeConfig = nodeConfig;
        data.nodeConfigBiomassScale = nodeConfigBiomassScale;
        data.originalNodeIds = parseResult.nodeIds;
        data.originalSubweb = subweb;
        OutputFileWriter writer = new OutputFileWriter(outputDirectory);
        writer.write(data);
    }
}
