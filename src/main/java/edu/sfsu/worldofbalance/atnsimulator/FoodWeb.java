package edu.sfsu.worldofbalance.atnsimulator;

import com.google.gson.Gson;

import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

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

    public FoodWeb subweb(Set<Integer> nodeIds) {
        FoodWeb subweb = new FoodWeb();
        for (int nodeId : nodeIds) {
            subweb.nodeAttributes.put(nodeId, nodeAttributes.get(nodeId));
            subweb.links.put(nodeId, intersection(nodeIds, links.get(nodeId)));
            subweb.reverseLinks.put(nodeId, intersection(nodeIds, reverseLinks.get(nodeId)));
        }
        return subweb;
    }

    /**
     * Normalize node IDs to the range 0..(nodeCount-1)
     * while preserving the food web structure.
     * @return a map of the original node IDs to the normalized node IDs
     */
    public Map<Integer, Integer> normalizeNodeIds() {

        // Maps old node IDs to new, normalized node IDs
        HashMap<Integer, Integer> nodeMap = normalizedNodeIdMap();

        HashMap<Integer, NodeAttributes> normalizedNodeAttributes = new HashMap<>();
        HashMap<Integer, HashSet<Integer>> normalizedLinks = new HashMap<>();
        for (Map.Entry<Integer, Integer> e : nodeMap.entrySet()) {
            int oldNodeId = e.getKey();
            int newNodeId = e.getValue();
            normalizedNodeAttributes.put(newNodeId, nodeAttributes.get(oldNodeId));
            normalizedLinks.put(newNodeId, mapSetValues(links.get(oldNodeId), nodeMap));
        }
        nodeAttributes = normalizedNodeAttributes;
        links = normalizedLinks;
        reverseLinks.clear();
        initializeMissingLinks(reverseLinks);
        populateReverseLinks();

        return nodeMap;
    }

    public boolean nodeIdsAreNormalized() {
        return Collections.min(nodes()) == 0 && Collections.max(nodes()) == nodeCount() - 1;
    }

    public boolean equals(Object other) {
        FoodWeb otherWeb = (FoodWeb) other;
        return otherWeb.nodeAttributes.equals(this.nodeAttributes)
                && otherWeb.links.equals(this.links)
                && otherWeb.reverseLinks.equals(this.reverseLinks);
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

    /**
     * @return the intersection of the two given sets.
     */
    private static HashSet<Integer> intersection(Set<Integer> s1, HashSet<Integer> s2) {
        HashSet<Integer> intersection = new HashSet<>(s1);
        intersection.retainAll(s2);
        return intersection;
    }

    /**
     * Map the values in the given set to a set of new values,
     * using the given map of existing values to new values.
     * @param values the input set
     * @param mapping the map with existing values as keys and new values as values
     * @return the mapped set
     */
    private static HashSet<Integer> mapSetValues(Set<Integer> values, Map<Integer, Integer> mapping) {
        HashSet<Integer> mappedSet = values.stream()
                .map(oldValue -> mapping.get(oldValue))
                .collect(Collectors.toCollection(HashSet::new));
        return mappedSet;
    }

    private HashMap<Integer, Integer> normalizedNodeIdMap() {
        int[] sortedOldNodeIds = sortedNodeIds();
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int newNodeId = 0; newNodeId < nodeCount(); newNodeId++) {
            map.put(sortedOldNodeIds[newNodeId], newNodeId);
        }
        return map;
    }

    private int[] sortedNodeIds() {
        int[] sortedNodeIds = new int[nodeCount()];
        int i = 0;
        for (int nodeId : nodes()) {
            sortedNodeIds[i++] = nodeId;
        }
        Arrays.sort(sortedNodeIds);
        return sortedNodeIds;
    }
}
