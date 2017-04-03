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
}
