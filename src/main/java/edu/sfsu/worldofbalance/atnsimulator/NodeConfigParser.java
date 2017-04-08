package edu.sfsu.worldofbalance.atnsimulator;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses node config strings.
 *
 * Node config strings describe the parameters for a model instance,
 * and have the following syntax (no carriage returns):
 *
 * 1. #, //#=number of nodes
 * 2. [#], //[#]=next node ID (species)
 * 3. #, //#=total biomass
 * 4. #, //#=per-unit-biomass
 * 5. #, //#=number of node parameters configured (exclude next line if = 0)
 * 6. p=#, //p=node parameter ID (K, R, X) for (carrying capacity, growth rate,
 *           met-rate)
 *  {repeat 6 based on number given in 5}
 * 7. #, //#=number of link parameters configured (exclude next two lines if = 0)
 * 8. [#], //[#]=link node ID (linked species)
 * 9. p=#, //p=link parameter ID (A, E, D, Q, Y)
 *  {repeat 8-9 based on number given in 7}
 *  {repeat 2-9 based on number given in 1}
 */
public class NodeConfigParser {

    private double biomassScale;
    private StringTokenizer tokenizer;
    private Result result;
    private Pattern bracketedIntegerPattern;
    private Pattern parameterAssignmentPattern;
    private String nodeConfig;
    private int currentNodeIndex;

    public NodeConfigParser() {
        this(1);
    }

    /**
     * Instantiate a parser with biomass scaling.
     * This treats the biomass and carrying capacity given in the node config
     * as scaled by `biomassScale` relative to the values used in the model.
     * That is, the values in result.parameters will be divided by `biomassScale`.
     */
    public NodeConfigParser(int biomassScale) {
        this.biomassScale = biomassScale;
        bracketedIntegerPattern = Pattern.compile("^\\[(\\d+)\\]$");     // e.g. [2]
        parameterAssignmentPattern = Pattern.compile("^([A-Z])=(.+$)");  // e.g. X=0.55
    }

    /**
     * Parse the given node config string.
     */
    public Result parse(String nodeConfig) {
        try {
            return parseThrowingExceptions(nodeConfig);
        } catch(NodeConfigSyntaxError e) {
            throw e;
        } catch(NumberFormatException e) {
            throw new NodeConfigSyntaxError(nodeConfig, "Bad number format");
        } catch(NoSuchElementException e) {
            throw new NodeConfigSyntaxError(nodeConfig, "Unexpected end of node config string");
        }
    }

    /**
     * Represents the results of parsing a node config string.
     */
    public static class Result {

        /**
         * The node IDs in the order specified in the node config.
         */
        public int[] nodeIds;

        /**
         * The biomass at t=0 of each node.
         * Elements correspond to `nodeIds`.
         */
        public double[] initialBiomass;

        /**
         * The parameter values specified by the node config
         * Elements in the parameter arrays correspond to `nodeIds`.
         */
        public ModelParameters parameters;

        public Result(int nodeCount) {
            nodeIds = new int[nodeCount];
            initialBiomass = new double[nodeCount];
            parameters = new ModelParameters(nodeCount);
        }
    }

    private Result parseThrowingExceptions(String nodeConfig) {
        this.nodeConfig = nodeConfig;
        tokenizer = new StringTokenizer(nodeConfig, ", ");
        int nodeCount = Integer.parseInt(tokenizer.nextToken());
        result = new Result(nodeCount);
        for (currentNodeIndex = 0; currentNodeIndex < nodeCount; currentNodeIndex++) {
            parseNodeSection();
        }
        if (tokenizer.hasMoreTokens()) {
            throw new NodeConfigSyntaxError("Expected end of string, found more tokens");
        }
        return result;
    }

    private void parseNodeSection() {
        result.nodeIds[currentNodeIndex] = parseBracketedInteger(tokenizer.nextToken());
        result.initialBiomass[currentNodeIndex] = Double.parseDouble(tokenizer.nextToken()) / biomassScale;
        tokenizer.nextToken();  // Skip per-unit biomass

        int nodeParameterCount = Integer.parseInt(tokenizer.nextToken());
        for (int p = 0; p < nodeParameterCount; p++) {
            parseNodeParameter();
        }

        int linkParameterCount = Integer.parseInt(tokenizer.nextToken());
        if (linkParameterCount != 0) {
            // TODO
            throw new NodeConfigSyntaxError(nodeConfig, "Link parameters are not yet supported");
        }
    }

    private void parseNodeParameter() {
        ParameterAssignment assignment = parseParameterAssignment(tokenizer.nextToken());
        switch (assignment.name) {
            case 'X':
                result.parameters.metabolicRate[currentNodeIndex] = assignment.value;
                break;
            case 'R':
                result.parameters.growthRate[currentNodeIndex] = assignment.value;
                break;
            case 'K':
                result.parameters.carryingCapacity[currentNodeIndex] = assignment.value / biomassScale;
                break;
            default:
                throw new NodeConfigSyntaxError(nodeConfig, "Invalid node parameter " + assignment.name);
        }
    }

    /**
     * @param token a token in the form [#], where # is an integer
     */
    private int parseBracketedInteger(String token) {
        Matcher m = bracketedIntegerPattern.matcher(token);
        if (!m.matches()) {
            throw new NodeConfigSyntaxError(nodeConfig, "Expected [#]");
        }
        return Integer.parseInt(m.group(1));
    }

    /**
     * @param token a token in the form P=#, where P is an upper-case letter and # is a decimal number
     */
    private ParameterAssignment parseParameterAssignment(String token) {
        Matcher m = parameterAssignmentPattern.matcher(token);
        if (!m.matches()) {
            throw new NodeConfigSyntaxError(nodeConfig);
        }
        return new ParameterAssignment(m.group(1).charAt(0), Double.parseDouble(m.group(2)));
    }

    private static class ParameterAssignment {
        char name;
        double value;

        public ParameterAssignment(char name, double value) {
            this.name = name;
            this.value = value;
        }
    }
}
