package edu.sfsu.worldofbalance.atnsimulator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ben on 4/2/17.
 */
public class FoodWeb {

    private HashMap<Integer, HashSet<Integer>> links;
    private HashMap<Integer, HashSet<Integer>> reverseLinks;
    private HashMap<Integer, NodeAttributes> nodeAttributes;

    public FoodWeb() {
        links = new HashMap<>();
        reverseLinks = new HashMap<>();
        nodeAttributes = new HashMap<>();
    }

    public int nodeCount() {
        return nodeAttributes.size();
    }

    public void addNode(int nodeId) {
        if (containsNode(nodeId)) {
            throw new FoodWebDuplicateNodeException(nodeId);
        }
        links.put(nodeId, new HashSet<>());
        reverseLinks.put(nodeId, new HashSet<>());
        nodeAttributes.put(nodeId, null);
    }

    public void addLink(int preyNodeId, int predatorNodeId) {
        if (!containsNode(preyNodeId)) {
            throw new FoodWebNodeAbsentException(preyNodeId);
        }
        if (!containsNode(predatorNodeId)) {
            throw new FoodWebNodeAbsentException(predatorNodeId);
        }
        links.get(preyNodeId).add(predatorNodeId);
        reverseLinks.get(predatorNodeId).add(preyNodeId);
    }

    public boolean containsNode(int nodeId) {
        return nodeAttributes.containsKey(nodeId);
    }

    public boolean containsLink(int preyNodeId, int predatorNodeId) {
        return links.containsKey(preyNodeId) && links.get(preyNodeId).contains(predatorNodeId);
    }

    public void setNodeAttributes(int nodeId, NodeAttributes attributes) {
        if (!containsNode(nodeId)) {
            throw new FoodWebNodeAbsentException(nodeId);
        }
        nodeAttributes.put(nodeId, attributes);
    }

    public NodeAttributes getNodeAttributes(int nodeId) {
        return nodeAttributes.get(nodeId);
    }

    public Set<Integer> getPredatorsOf(int preyNodeId) {
        if (!containsNode(preyNodeId)) {
            throw new FoodWebNodeAbsentException(preyNodeId);
        }
        return links.get(preyNodeId);
    }

    public Set<Integer> getPreyOf(int predatorNodeId) {
        if (!containsNode(predatorNodeId)) {
            throw new FoodWebNodeAbsentException(predatorNodeId);
        }
        return reverseLinks.get(predatorNodeId);
    }
}
