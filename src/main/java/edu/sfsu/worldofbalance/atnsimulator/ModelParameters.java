package edu.sfsu.worldofbalance.atnsimulator;

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
     * @param N number of nodes
     */
    public ModelParameters(int N) {
        useSystemCarryingCapacity = false;

        metabolicRate = new double[N];
        growthRate = new double[N];
        carryingCapacity = new double[N];

        maximumIngestionRate = new double[N][N];
        predatorInterference = new double[N][N];
        functionalResponseControl = new double[N][N];
        relativeHalfSaturationDensity = new double[N][N];
        halfSaturationDensity = new double[N][N];
        assimilationEfficiency = new double[N][N];
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

        // Default assimilation efficiency depends on whether prey node is a plant or animal
        public static double assimilationEfficiencyPlant = 0.5;
        public static double assimilationEfficiencyAnimal = 0.8;
    }
}
