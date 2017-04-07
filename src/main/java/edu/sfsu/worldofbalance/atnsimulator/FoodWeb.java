package edu.sfsu.worldofbalance.atnsimulator;

import com.google.gson.Gson;

import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

public class FoodWeb {

    private HashMap<Integer, HashSet<Integer>> links;
    private transient HashMap<Integer, HashSet<Integer>> reverseLinks;
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

    public String toJson() {
        return (new Gson()).toJson(this);
    }

    public int nodeCount() {
        return nodeAttributes.size();
    }

    public int linkCount() {
        int count = 0;
        for (Set<Integer> nodeIds : links.values())
            count += nodeIds.size();
        return count;
    }

    public Set<Integer> nodes() {
        return nodeAttributes.keySet();
    }

    public void addNode(int nodeId, NodeAttributes attributes) {
        if (containsNode(nodeId)) {
            throw new FoodWebDuplicateNodeException(nodeId);
        }
        links.put(nodeId, new HashSet<>());
        reverseLinks.put(nodeId, new HashSet<>());
        nodeAttributes.put(nodeId, attributes);
    }

    public void addProducerNode(int nodeId) {
        addNode(nodeId, new NodeAttributes(NodeAttributes.NodeType.PRODUCER));
    }

    public void addConsumerNode(int nodeId) {
        addNode(nodeId, new NodeAttributes(NodeAttributes.NodeType.CONSUMER));
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
     * Return a subgraph of this food web ("subweb")
     * which includes all of the given nodes and the links between them.
     * @param nodeIds the node IDs to include in the subweb
     * @return the generated subweb
     */
    public FoodWeb subweb(Set<Integer> nodeIds) {
        FoodWeb subweb = new FoodWeb();
        for (int nodeId : nodeIds) {
            subweb.nodeAttributes.put(nodeId, nodeAttributes.get(nodeId));
            subweb.links.put(nodeId, intersection(nodeIds, links.get(nodeId)));
            subweb.reverseLinks.put(nodeId, intersection(nodeIds, reverseLinks.get(nodeId)));
        }
        return subweb;
    }

    public FoodWeb subweb(int[] nodeIds) {
        Set<Integer> nodeIdSet = new HashSet<>();
        for (int nodeId : nodeIds)
            nodeIdSet.add(nodeId);
        return subweb(nodeIdSet);
    }

    /**
     * Return a new FoodWeb with the same structure and attributes as this one,
     * but with node IDs normalized to the range 0..(nodeCount-1).
     * The `nodeIds` argument defines the mapping between existing node IDs and new node IDs;
     * i.e., the values are the existing node IDs, and the indices are the corresponding new node IDs.
     *
     * @param nodeIds an array of all existing node IDs in this food web
     * @return a normalized copy of this food web
     */
    public FoodWeb normalizedCopy(int[] nodeIds) {
        if (nodeIds.length != nodeCount())
            throw new IllegalArgumentException("Wrong number of node IDs");

        // Maps old node IDs to new, normalized node IDs
        HashMap<Integer, Integer> nodeMap = normalizedNodeIdMap(nodeIds);

        FoodWeb newWeb = new FoodWeb();
        for (Map.Entry<Integer, Integer> e : nodeMap.entrySet()) {
            int oldNodeId = e.getKey();
            int newNodeId = e.getValue();
            if (!containsNode(oldNodeId))
                throw new FoodWebNodeAbsentException(oldNodeId);
            newWeb.addNode(newNodeId, new NodeAttributes(nodeAttributes.get(oldNodeId)));
            newWeb.links.put(newNodeId, mapSetValues(links.get(oldNodeId), nodeMap));
        }
        newWeb.initializeMissingLinks(newWeb.reverseLinks);
        newWeb.populateReverseLinks();

        return newWeb;
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

    private HashMap<Integer, Integer> normalizedNodeIdMap(int[] nodeIds) {
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int newNodeId = 0; newNodeId < nodeIds.length; newNodeId++) {
            int oldNodeId = nodeIds[newNodeId];
            map.put(oldNodeId, newNodeId);
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
