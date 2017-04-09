package edu.sfsu.worldofbalance.atnsimulator;

/**
 * Attributes of a node in a food web, as distinct from the node-level ATN model
 * parameters.
 */
public class NodeAttributes {
    public enum NodeType {
        PRODUCER,
        CONSUMER
    }

    public NodeType nodeType;

    public NodeAttributes() {
        this.nodeType = NodeType.CONSUMER;
    }

    /**
     * Copy constructor
     */
    public NodeAttributes(NodeAttributes original) {
        this.nodeType = original.nodeType;
    }

    public NodeAttributes(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    @Override
    public boolean equals(Object other) {
        return ((NodeAttributes) other).nodeType == this.nodeType;
    }
}
