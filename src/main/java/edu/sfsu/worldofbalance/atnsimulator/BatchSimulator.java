package edu.sfsu.worldofbalance.atnsimulator;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Scanner;

public class BatchSimulator {

    private static FoodWeb serengeti;

    public static void main(String[] args) {
        CommandLineArguments arguments = new CommandLineArguments();
        JCommander jCommander = new JCommander(arguments);
        try {
            jCommander.parse(args);
        } catch (ParameterException e) {
            System.out.println(e.getMessage());
            jCommander.usage();
            return;
        }
        if (arguments.help) {
            jCommander.usage();
            return;
        }

        runBatch(arguments);
    }

    private static void runBatch(CommandLineArguments arguments) {
        Scanner input;
        try {
            input = new Scanner(arguments.nodeConfigFile);
        } catch (FileNotFoundException ex) {
            System.err.println("Input file " + arguments.nodeConfigFile + " not found");
            return;
        }

        readSerengetiFoodWeb();
        int simulationId = 0;
        while (input.hasNextLine()) {
            System.out.println("Running simulation " + simulationId);
            SimulationParameters parameters = new SimulationParameters();
            parameters.timesteps = arguments.timesteps;
            parameters.stepSize = arguments.stepSize;
            parameters.stopOnSteadyState = !arguments.noStopOnSteadyState;
            String nodeConfig = input.nextLine();

            BatchSimulationTask task = new BatchSimulationTask(
                    serengeti,
                    simulationId,
                    parameters,
                    nodeConfig,
                    arguments.nodeConfigBiomassScale,
                    arguments.outputDirectory);
            task.run();
            simulationId++;
        }
    }

    private static void readSerengetiFoodWeb() {
        Reader reader = new InputStreamReader(
                BatchSimulator.class.getResourceAsStream("/foodwebs/serengeti.json"));
        serengeti = FoodWeb.createFromJson(reader);
    }

    private static class CommandLineArguments {
        @Parameter(names = {"-h", "--help"}, help = true)
        private boolean help;

        @Parameter(names = {"-n", "--node-config-file"}, description = "Node config file", required = true)
        private File nodeConfigFile;

        @Parameter(names = {"-b", "--node-config-biomass-scale"}, description = "Node config biomass scale")
        private int nodeConfigBiomassScale = 1000;

        @Parameter(names = {"-t", "--timesteps"}, description = "Time steps to run simulations", required = true)
        private Integer timesteps;

        @Parameter(names = {"-i", "--step-interval"}, description = "Time step duration")
        private Double stepSize = 0.1;

        @Parameter(names = {"-o", "--output-dir"}, description = "Output directory", required = true)
        private File outputDirectory;

        @Parameter(names = {"-c", "--no-stop-on-steady-state"}, description = "Do not stop when a steady state is detected")
        private boolean noStopOnSteadyState = false;
    }
}

