package edu.sfsu.worldofbalance.atnsimulator;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
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
        web.addNode(1);
        web.addNode(2);
        web.addNode(3);
        assertEquals(3, web.nodeCount());
    }

    @Test(expected = FoodWebNodeAbsentException.class)
    public void testAddLinkEmpty() {
        web.addLink(1, 2);
    }

    @Test
    public void testAddLink() {
        web.addNode(1);
        web.addNode(2);
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
        web.addNode(1);
        web.setNodeAttributes(1, attributes);
        assertEquals(attributes.nodeType, web.getNodeAttributes(1).nodeType);
    }

    @Test(expected = FoodWebDuplicateNodeException.class)
    public void testDoubleAddNode() {
        web.addNode(1);
        web.addNode(1);
    }

    @Test
    public void testGetPreyOf() {
        web.addNode(1);
        web.addNode(2);
        web.addNode(3);
        web.addLink(1, 3);
        web.addLink(2, 3);
        Set<Integer> expectedPrey = new HashSet<>();
        expectedPrey.add(1);
        expectedPrey.add(2);
        assertEquals(expectedPrey, web.getPreyOf(3));
    }

    @Test
    public void testGetPredatorsOf() {
        web.addNode(1);
        web.addNode(2);
        web.addNode(3);
        web.addLink(1, 2);
        web.addLink(1, 3);
        Set<Integer> expectedPredators = new HashSet<>();
        expectedPredators.add(2);
        expectedPredators.add(3);
        assertEquals(expectedPredators, web.getPredatorsOf(1));
    }
}
