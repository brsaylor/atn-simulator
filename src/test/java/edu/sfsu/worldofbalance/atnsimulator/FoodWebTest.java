package edu.sfsu.worldofbalance.atnsimulator;

import org.junit.Before;
import org.junit.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class FoodWebTest {

    private FoodWeb web;

    @Before
    public void setUp() {
        web = new FoodWeb();
    }

    @Test
    public void testSizeEmpty() {
        assertEquals(0, web.nodeCount());
    }

    @Test
    public void testNodeCount() {
        web.addProducerNode(1);
        web.addConsumerNode(2);
        web.addConsumerNode(3);
        assertEquals(3, web.nodeCount());
    }

    @Test(expected = FoodWebNodeAbsentException.class)
    public void testAddLinkEmpty() {
        web.addLink(1, 2);
    }

    @Test
    public void testAddLink() {
        web.addProducerNode(1);
        web.addConsumerNode(2);
        web.addLink(1, 2);
        assertEquals(2, web.nodeCount());
        assertTrue(web.containsNode(1));
        assertTrue(web.containsNode(2));
        assertTrue(web.containsLink(1, 2));
    }

    @Test
    public void testNodeAttributes() {
        NodeAttributes attributes = new NodeAttributes();
        attributes.nodeType = NodeAttributes.NodeType.PRODUCER;
        web.addNode(1, new NodeAttributes(NodeAttributes.NodeType.CONSUMER));
        web.setNodeAttributes(1, attributes);
        assertEquals(attributes.nodeType, web.getNodeAttributes(1).nodeType);
    }

    @Test(expected = FoodWebDuplicateNodeException.class)
    public void testDoubleAddNode() {
        web.addProducerNode(1);
        web.addProducerNode(1);
    }

    @Test
    public void testGetPreyOf() {
        web.addProducerNode(1);
        web.addConsumerNode(2);
        web.addConsumerNode(3);
        web.addLink(1, 3);
        web.addLink(2, 3);
        Set<Integer> expectedPrey = new HashSet<>();
        expectedPrey.add(1);
        expectedPrey.add(2);
        assertEquals(expectedPrey, web.getPreyOf(3));
    }

    @Test
    public void testGetPredatorsOf() {
        web.addProducerNode(1);
        web.addConsumerNode(2);
        web.addConsumerNode(3);
        web.addLink(1, 2);
        web.addLink(1, 3);
        Set<Integer> expectedPredators = new HashSet<>();
        expectedPredators.add(2);
        expectedPredators.add(3);
        assertEquals(expectedPredators, web.getPredatorsOf(1));
    }

    @Test
    public void testNodes() {
        web.addProducerNode(1);
        web.addConsumerNode(2);
        web.addConsumerNode(3);
        Set<Integer> expectedNodes = new HashSet<>();
        expectedNodes.add(1);
        expectedNodes.add(2);
        expectedNodes.add(3);
        assertEquals(expectedNodes, web.nodes());
    }

    @Test
    public void testNodeAttributesEquals() {
        assertEquals(
                new NodeAttributes(NodeAttributes.NodeType.PRODUCER),
                new NodeAttributes(NodeAttributes.NodeType.PRODUCER));
        assertNotEquals(
                new NodeAttributes(NodeAttributes.NodeType.PRODUCER),
                new NodeAttributes(NodeAttributes.NodeType.CONSUMER));
    }

    @Test
    public void testEquals() {
        initializeSmallFoodWeb(web);
        FoodWeb otherWeb = new FoodWeb();
        initializeSmallFoodWeb(otherWeb);
        assertEquals(web, otherWeb);
    }

    @Test
    public void testNotEqualsBecauseDifferentNodes() {
        initializeSmallFoodWeb(web);
        FoodWeb otherWeb = new FoodWeb();
        initializeSmallFoodWeb(otherWeb);
        otherWeb.addConsumerNode(4);
        assertNotEquals(web, otherWeb);
    }

    @Test
    public void testNotEqualsBecauseDifferentLinks() {
        initializeSmallFoodWeb(web);
        FoodWeb otherWeb = new FoodWeb();
        initializeSmallFoodWeb(otherWeb);
        otherWeb.addLink(2, 1);
        assertNotEquals(web, otherWeb);
    }

    @Test
    public void testNotEqualsBecauseDifferentAttributes() {
        initializeSmallFoodWeb(web);
        FoodWeb otherWeb = new FoodWeb();
        initializeSmallFoodWeb(otherWeb);
        otherWeb.setNodeAttributes(1, new NodeAttributes(NodeAttributes.NodeType.CONSUMER));
        assertNotEquals(web, otherWeb);
    }

    @Test
    public void testCreateFromJson() {
        initializeSmallFoodWeb(web);
        Reader reader = new InputStreamReader(web.getClass().getResourceAsStream("/small-food-web.json"));
        FoodWeb jsonWeb = FoodWeb.createFromJson(reader);
        assertEquals(web, jsonWeb);
    }

    @Test
    public void testSubweb() {
        initializeSmallFoodWeb(web);

        FoodWeb expectedSubWeb = new FoodWeb();
        expectedSubWeb.addProducerNode(1);
        expectedSubWeb.addConsumerNode(2);
        expectedSubWeb.addLink(1, 2);

        Set<Integer> subwebNodes = new HashSet<>();
        subwebNodes.add(1);
        subwebNodes.add(2);
        FoodWeb actualSubweb = web.subweb(subwebNodes);

        assertEquals(expectedSubWeb, actualSubweb);
    }

    @Test
    public void testNormalizeNodeIds() {
        web.addProducerNode(100);
        web.addConsumerNode(101);
        web.addConsumerNode(102);
        web.addLink(100, 101);
        web.addLink(100, 102);
        web.addLink(101, 102);

        Map<Integer, Integer> nodeMap = web.normalizeNodeIds();

        Map<Integer, Integer> expectedNodeMap = new HashMap<>();
        expectedNodeMap.put(100, 0);
        expectedNodeMap.put(101, 1);
        expectedNodeMap.put(102, 2);

        FoodWeb expectedWeb = new FoodWeb();
        expectedWeb.addProducerNode(0);
        expectedWeb.addConsumerNode(1);
        expectedWeb.addConsumerNode(2);
        expectedWeb.addLink(0, 1);
        expectedWeb.addLink(0, 2);
        expectedWeb.addLink(1, 2);

        assertEquals(expectedNodeMap, nodeMap);
        assertEquals(expectedWeb, web);
    }

    @Test
    public void testNodeIdsAreNormalized() {
        web.addProducerNode(100);
        web.addConsumerNode(200);
        web.normalizeNodeIds();
        assertTrue(web.nodeIdsAreNormalized());
    }

    @Test
    public void testNodeIdsAreNotNormalized() {
        web.addProducerNode(100);
        web.addConsumerNode(200);
        assertFalse(web.nodeIdsAreNormalized());
    }

    // Corresponds to small-food-web.json test file
    private void initializeSmallFoodWeb(FoodWeb web) {
        web.addProducerNode(1);
        web.addConsumerNode(2);
        web.addConsumerNode(3);
        web.addLink(1, 2);
        web.addLink(1, 3);
        web.addLink(2, 3);
    }
}
