package edu.sfsu.worldofbalance.atnsimulator;

import java.util.Arrays;

import static edu.sfsu.worldofbalance.atnsimulator.NodeAttributes.NodeType.PRODUCER;
import static edu.sfsu.worldofbalance.atnsimulator.NodeAttributes.NodeType.CONSUMER;

/**
 * Data structure holding a set of parameters to be passed to ModelEquations.
 *
 * The link-level parameters are indexed as [predatorId][preyId].
 */
public class ModelParameters {

    // System-wide parameters
    public boolean useSystemCarryingCapacity;
    public double systemCarryingCapacity;  // Ks: System-wide carrying capacity (only used if useSystemCarryingCapacity is true)

    // Node-level parameters
    public double[] metabolicRate;     // x: Mass-specific metabolic rate
    public double[] growthRate;        // r: Maximum mass-specific growth rate
    public double[] carryingCapacity;  // k: Carrying capacity

    // Link-level parameters
    public double[][] maximumIngestionRate;           // y: Maximum ingestion rate
    public double[][] predatorInterference;           // d: Predator interference
    public double[][] functionalResponseControl;      // q: Functional response control parameter
    public double[][] relativeHalfSaturationDensity;  // alpha: Relative half saturation density
    public double[][] halfSaturationDensity;          // B0: Half saturation density
    public double[][] assimilationEfficiency;         // e: Assimilation efficiency

    /**
     * Instantiate with default values, except food-web-dependent defaults
     * @param N number of nodes
     */
    public ModelParameters(int N) {
        useSystemCarryingCapacity = Defaults.useSystemCarryingCapacity;
        systemCarryingCapacity = Defaults.systemCarryingCapacity;

        metabolicRate = vector(N, Defaults.metabolicRate);
        growthRate = vector(N, Defaults.growthRate);
        carryingCapacity = vector(N, Defaults.carryingCapacity);

        maximumIngestionRate = matrix(N, Defaults.maximumIngestionRate);
        predatorInterference = matrix(N, Defaults.predatorInterference);
        functionalResponseControl = matrix(N, Defaults.functionalResponseControl);
        relativeHalfSaturationDensity = matrix(N, Defaults.relativeHalfSaturationDensity);
        halfSaturationDensity = matrix(N, Defaults.halfSaturationDensity);
        assimilationEfficiency = matrix(N, Defaults.assimilationEfficiency);
    }

    /**
     * Instantiate with default values, including food-web-dependent defaults
     */
    public ModelParameters(FoodWeb foodWeb) {
        this(foodWeb.nodeCount());
        applyFoodWebDependentDefaults(foodWeb);
    }

    /**
     * Set parameter values to defaults
     * for parameters that depend on the food web structure.
     * Currently, this only sets assimilation efficiency,
     * which depends on whether the prey node is a plant or animal.
     */
    public void applyFoodWebDependentDefaults(FoodWeb foodWeb) {
        if (!foodWeb.nodeIdsAreNormalized())
            throw new FoodWebNotNormalizedException();

        for (int nodeId : foodWeb.nodes()) {
            switch (foodWeb.getNodeAttributes(nodeId).nodeType) {
                case PRODUCER:
                    fillColumn(assimilationEfficiency, nodeId, Defaults.assimilationEfficiencyPlant);
                    break;
                case CONSUMER:
                    fillColumn(assimilationEfficiency, nodeId, Defaults.assimilationEfficiencyAnimal);
                    break;
            }
        }
    }

    public static class Defaults {
        public static boolean useSystemCarryingCapacity = false;
        public static double systemCarryingCapacity = 1;

        public static double metabolicRate = 0.5;
        public static double growthRate = 1;
        public static double carryingCapacity = 1;

        public static double maximumIngestionRate = 6;
        public static double predatorInterference = 0;
        public static double functionalResponseControl = 0.2;
        public static double relativeHalfSaturationDensity = 1;
        public static double halfSaturationDensity = 0.5;

        // Default assimilation efficiency depends on whether prey node is a plant or animal.
        public static double assimilationEfficiency = 1;
        public static double assimilationEfficiencyPlant = 0.5;
        public static double assimilationEfficiencyAnimal = 0.8;
    }

    private double[] vector(int size, double value) {
        double[] array = new double[size];
        Arrays.fill(array, value);
        return array;
    }

    private double[][] matrix(int size, double value) {
        double[][] matrix = new double[size][size];
        for (double[] row : matrix)
            Arrays.fill(row, value);
        return matrix;
    }

    private void fillColumn(double[][] matrix, int column, double value) {
        for (int row = 0; row < matrix.length; row++)
            matrix[row][column]  = value;
    }
}
