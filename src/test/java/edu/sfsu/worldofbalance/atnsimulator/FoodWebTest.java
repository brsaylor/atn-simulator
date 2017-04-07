package edu.sfsu.worldofbalance.atnsimulator;

import org.junit.Before;
import org.junit.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;

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

    @Test
    public void testLinkCount() {
        web.addProducerNode(1);
        web.addConsumerNode(2);
        web.addConsumerNode(3);
        web.addLink(1, 2);
        web.addLink(1, 3);
        web.addLink(2, 3);
        web.addLink(3, 3);
        assertEquals(4, web.linkCount());
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
    public void testToJson() {
        initializeSmallFoodWeb(web);
        String json = web.toJson();
        FoodWeb webFromJson = FoodWeb.createFromJson(new StringReader(json));
        assertEquals(web, webFromJson);
    }

    @Test
    public void testSubwebSet() {
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
    public void testSubwebArray() {
        initializeSmallFoodWeb(web);

        FoodWeb expectedSubWeb = new FoodWeb();
        expectedSubWeb.addProducerNode(1);
        expectedSubWeb.addConsumerNode(2);
        expectedSubWeb.addLink(1, 2);

        int[] subwebNodes = new int[] {1, 2};
        FoodWeb actualSubweb = web.subweb(subwebNodes);

        assertEquals(expectedSubWeb, actualSubweb);
    }

    @Test
    public void testNormalizedCopy() {
        web.addProducerNode(100);
        web.addConsumerNode(101);
        web.addConsumerNode(102);
        web.addLink(100, 101);
        web.addLink(100, 102);
        web.addLink(101, 102);

        FoodWeb expectedWeb = new FoodWeb();
        expectedWeb.addProducerNode(0);
        expectedWeb.addConsumerNode(1);
        expectedWeb.addConsumerNode(2);
        expectedWeb.addLink(0, 1);
        expectedWeb.addLink(0, 2);
        expectedWeb.addLink(1, 2);

        int[] nodeIds = new int[] {100, 101, 102};
        FoodWeb actualWeb = web.normalizedCopy(nodeIds);

        assertEquals(expectedWeb, actualWeb);
    }

    @Test(expected = FoodWebNodeAbsentException.class)
    public void testNormalizedCopyGivenNonExistentNodes() {
        web.addProducerNode(1);
        web.addProducerNode(2);
        web.normalizedCopy(new int[] {1, 3});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNormalizedCopyGivenWrongNumberOfNodes() {
        web.addProducerNode(1);
        web.addProducerNode(2);
        web.normalizedCopy(new int[] {1, 2, 3});
    }

    @Test
    public void testNodeIdsAreNormalized() {
        web.addProducerNode(0);
        web.addConsumerNode(1);
        assertTrue(web.nodeIdsAreNormalized());
    }

    @Test
    public void testNodeIdsAreNotNormalized() {
        web.addProducerNode(100);
        web.addConsumerNode(200);
        assertFalse(web.nodeIdsAreNormalized());
    }

    @Test
    public void testSerengeti() {
        Reader reader = new InputStreamReader(getClass().getResourceAsStream("/foodwebs/serengeti.json"));
        FoodWeb serengeti = FoodWeb.createFromJson(reader);
        assertEquals(87, serengeti.nodeCount());
        assertEquals(538, serengeti.linkCount());

        // Spot check links and node types
        assertTrue(serengeti.containsLink(59, 49));
        assertEquals(NodeAttributes.NodeType.PRODUCER, serengeti.getNodeAttributes(2).nodeType);
        assertEquals(NodeAttributes.NodeType.CONSUMER, serengeti.getNodeAttributes(95).nodeType);
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
