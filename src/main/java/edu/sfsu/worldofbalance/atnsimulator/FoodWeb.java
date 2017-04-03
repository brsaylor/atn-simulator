package edu.sfsu.worldofbalance.atnsimulator;

import com.google.gson.Gson;

import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class FoodWeb {

    private HashMap<Integer, HashSet<Integer>> links;
    private HashMap<Integer, HashSet<Integer>> reverseLinks;
    private HashMap<Integer, NodeAttributes> nodeAttributes;

    public FoodWeb() {
        links = new HashMap<>();
        reverseLinks = new HashMap<>();
        nodeAttributes = new HashMap<>();
    }

    public static FoodWeb createFromJson(Reader reader) {
        Gson gson = new Gson();
        FoodWeb web = gson.fromJson(reader, FoodWeb.class);
        web.initializeMissingLinks(web.links);
        web.initializeMissingLinks(web.reverseLinks);
        web.populateReverseLinks();
        return web;
    }

    public int nodeCount() {
        return nodeAttributes.size();
    }

    public Set<Integer> nodes() {
        return nodeAttributes.keySet();
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

    /**
     * Update `linkSet` (either `links` or `reverseLinks`) so that it contains all the same keys (nodeIds)
     * as `nodeAttributes`.
     */
    private void initializeMissingLinks(HashMap<Integer, HashSet<Integer>> linkSet) {
        for (int nodeId : nodeAttributes.keySet()) {
            if (!linkSet.containsKey(nodeId)) {
                linkSet.put(nodeId, new HashSet<>());
            }
        }
    }

    /**
     * Populate `reverseLinks` based on `links`.
     */
    private void populateReverseLinks() {
        for (int preyNodeId : links.keySet()) {
            for (int predatorNodeId : links.get(preyNodeId)) {
                reverseLinks.get(predatorNodeId).add(preyNodeId);
            }
        }
    }
}
