package edu.sfsu.worldofbalance.atnsimulator;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;

import java.util.LinkedList;
import java.util.List;

/**
 * Implements the ATN model differential equations in a form usable by the Apache Commons Math integrators.
 * The equations come from the Dynamics.bioenergetic.ModelDerivative class in the Network3D web services codebase, with
 * the following differences:
 * 1. e[j][i] is used in place of e[i][j] for consistency with the literature
 * 2. the - x[i] * B[i] is excluded from the producer equation (Network3D automatically sets x[i] for producers to 0)
 * 3. A system-wide carrying capacity can optionally be used. In this case, Ks = sum(k[i]) and
 *    G[i] = 1 - ((sum for j in producers of (c[i][j] * B[j])) / Ks)
 *    (Boit et al. 2012)
 *    The competition coefficient c[i][j] is currently fixed at 1.
 *
 * @author Ben Saylor
 * @see <a href="https://commons.apache.org/proper/commons-math/userguide/ode.html">The Apache Commons Math ode package documentation</a>
 */
public class ModelEquations implements FirstOrderDifferentialEquations {

    public static final double EXTINCT = 1.0e-15;  // Extinction threshold

    private FoodWeb foodWeb;
    private ModelParameters p;
    private int nodeCount;
    private int[] producers;                // node IDs of producers
    private int[] consumers;                // node IDs of consumers
    private double[] biomass;               // Current biomass of each node
    private int[][] predatorsOf;            // predatorsOf[i] contains the node IDs of the predators of node i
    private int[][] preyOf;                 // preyOf[i] contains the node IDs of the prey of node i
    private double[] growthFunction;        // Computed values of growth function (G in the literature)
    private double[][] functionalResponse;  // Computed values of functional response (F in the literature)

    private double[] currentDerivatives;    // Most recently computed derivatives for use by event handlers

    public ModelEquations(FoodWeb foodWeb, ModelParameters parameters) {
        nodeCount = foodWeb.nodeCount();

        if (nodeCount == 0)
            throw new EmptyFoodWebException();
        if (!foodWeb.nodeIdsAreNormalized())
            throw new FoodWebNotNormalizedException();

        setParameters(parameters);

        this.foodWeb = foodWeb;
        producers = getNodeIdsOfType(NodeAttributes.NodeType.PRODUCER);
        consumers = getNodeIdsOfType(NodeAttributes.NodeType.CONSUMER);
        biomass = new double[nodeCount];

        predatorsOf = new int[nodeCount][];
        preyOf = new int[nodeCount][];
        for (int i = 0; i < nodeCount; i++) {
            predatorsOf[i] = foodWeb.getPredatorsOf(i).stream().mapToInt(j -> j).toArray();
            preyOf[i] = foodWeb.getPreyOf(i).stream().mapToInt(j -> j).toArray();
        }

        growthFunction = new double[nodeCount];
        functionalResponse = new double[nodeCount][nodeCount];
    }

    public void setParameters(ModelParameters parameters) {
        if (!parametersHaveCorrectDimensions(parameters))
            throw new IncorrectParameterDimensionsException();
        this.p = parameters;
    }

    public ModelParameters getParameters() {
        return p;
    }

    @Override
    public int getDimension() {
        return nodeCount;
    }

    /**
     * Compute the derivatives of biomass of each node.
     *
     * @param t Time
     * @param Bt Biomass of each node at time t
     * @param BDot Output: derivative of biomass of each node at time t
     */
    @Override
    public void computeDerivatives(double t, double[] Bt, double[] BDot) {
        // Copy Bt to biomass, setting biomass below extinction threshold to 0
        // (Copying because API doesn't specify whether state vector Bt can be modified)
        for (int i = 0; i < nodeCount; i++) {
            biomass[i] = Bt[i] < EXTINCT ? 0.0 : Bt[i];
        }

        computeFunctionalResponse();
        computeGrowthFunction();
        computeProducerDerivatives(BDot);
        computeConsumerDerivatives(BDot);

        // Save derivatives for use by event handlers
        this.currentDerivatives = BDot;
    }

    public double[] getCurrentDerivatives() {
        return currentDerivatives;
    }

    public int[] getProducers() {
        return producers;
    }

    public int[] getConsumers() {
        return consumers;
    }

    private void computeFunctionalResponse() {
        for (int i : consumers) {
            for (int j : preyOf[i]) {
                double numerator = Math.pow(biomass[j], 1 + p.functionalResponseControl[i][j]);
                double denominator = Math.pow(p.halfSaturationDensity[i][j], 1 + p.functionalResponseControl[i][j]);
                for (int m : preyOf[i]) {
                    denominator += p.relativeHalfSaturationDensity[i][m] *
                            Math.pow(biomass[m], 1 + p.functionalResponseControl[i][m]);
                }
                functionalResponse[i][j] = numerator / denominator;
            }
        }
    }

    private void computeGrowthFunction() {
        if (p.useSystemCarryingCapacity) {
            // Use system-wide carrying capacity
            for (int i : producers) {
                double numerator = 0;
                for (int j : producers) {
                    numerator += biomass[j];  // Assumes producer competition coefficient c_ij is 1
                }
                growthFunction[i] = 1 - numerator / p.systemCarryingCapacity;
            }
        } else {
            // Use node-level carrying capacity
            for (int i : producers) {
                growthFunction[i] = 1 - biomass[i] / p.carryingCapacity[i];
            }
        }
    }

    private void computeProducerDerivatives(double[] BDot) {
        for (int i : producers) {
            BDot[i] = p.growthRate[i] * biomass[i] * growthFunction[i];
            for (int j : predatorsOf[i]) {
                BDot[i] -= p.metabolicRate[j]
                        * p.maximumIngestionRate[j][i]
                        * p.relativeHalfSaturationDensity[j][i]
                        * functionalResponse[j][i]
                        * biomass[j]
                        / p.assimilationEfficiency[j][i];
            }
        }
    }

    private void computeConsumerDerivatives(double[] BDot) {
        for (int i : consumers) {
            BDot[i] = -p.metabolicRate[i] * biomass[i];
            for (int j : preyOf[i]) {
                BDot[i] += p.metabolicRate[i]
                        * p.maximumIngestionRate[i][j]
                        * p.relativeHalfSaturationDensity[i][j]
                        * functionalResponse[i][j]
                        * biomass[i];
            }
            for (int j : predatorsOf[i]) {
                BDot[i] -= p.metabolicRate[j]
                        * p.maximumIngestionRate[j][i]
                        * p.relativeHalfSaturationDensity[j][i]
                        * functionalResponse[j][i]
                        * biomass[j]
                        / p.assimilationEfficiency[j][i];
            }
        }
    }

    private int[] getNodeIdsOfType(NodeAttributes.NodeType nodeType) {
        List<Integer> nodeIds = new LinkedList<>();
        for (int i = 0; i < nodeCount; i++)
            if (foodWeb.getNodeAttributes(i).nodeType == nodeType)
                nodeIds.add(i);
        return nodeIds.stream().mapToInt(i -> i).toArray();
    }

    private boolean parametersHaveCorrectDimensions(ModelParameters parameters) {
        return parameters.metabolicRate.length == nodeCount;
    }
}
