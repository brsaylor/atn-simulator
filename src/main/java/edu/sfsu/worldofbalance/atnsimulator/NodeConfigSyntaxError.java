package edu.sfsu.worldofbalance.atnsimulator;

public class NodeConfigSyntaxError extends RuntimeException {
    public NodeConfigSyntaxError(String nodeConfig) {
        super("Syntax error in node config: " + nodeConfig);
    }

    public NodeConfigSyntaxError(String nodeConfig, String message) {
        super("Syntax error in node config: " + message + ": " + nodeConfig);
    }
}
