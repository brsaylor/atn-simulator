package edu.sfsu.worldofbalance.atnsimulator;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class NodeConfigParserTest {

    @Test
    public void testParse() {
        String nodeConfig = "3,[3],33.3,0.3,2,K=333.3,R=0.3,0,[55],55.5,0.5,1,X=0.55,0,[44],44.4,0.4,1,X=0.44,0";

        NodeConfigParser.Result expected = new NodeConfigParser.Result(3);
        expected.nodeIds = new int[] {3, 55, 44};
        expected.initialBiomass = new double[] {0.0333, 0.0555, 0.0444};
        expected.parameters.carryingCapacity[0] = 0.3333;
        expected.parameters.growthRate[0] = 0.3;
        expected.parameters.metabolicRate[1] = 0.55;
        expected.parameters.metabolicRate[2] = 0.44;

        NodeConfigParser parser = new NodeConfigParser(1000);
        NodeConfigParser.Result result = parser.parse(nodeConfig);

        assertArrayEquals(expected.nodeIds, result.nodeIds);
        assertArrayEquals(expected.initialBiomass, result.initialBiomass, 1e-10);
        assertArrayEquals(expected.parameters.carryingCapacity, result.parameters.carryingCapacity, 1e-10);
        assertArrayEquals(expected.parameters.growthRate, result.parameters.growthRate, 1e-10);
        assertArrayEquals(expected.parameters.metabolicRate, result.parameters.metabolicRate, 1e-10);
    }

    @Test(expected = NodeConfigSyntaxError.class)
    public void testEmpty() {
        new NodeConfigParser().parse("");
    }

    @Test(expected = NodeConfigSyntaxError.class)
    public void testNodeCountTooHigh() {
        String nodeConfig = "4,[3],33.3,0.3,2,K=333.3,R=0.3,0,[55],55.5,0.5,1,X=0.55,0,[44],44.4,0.4,1,X=0.44,0";
        new NodeConfigParser().parse(nodeConfig);
    }

    @Test(expected = NodeConfigSyntaxError.class)
    public void testNodeCountTooLow() {
        String nodeConfig = "2,[3],33.3,0.3,2,K=333.3,R=0.3,0,[55],55.5,0.5,1,X=0.55,0,[44],44.4,0.4,1,X=0.44,0";
        new NodeConfigParser().parse(nodeConfig);
    }

    @Test(expected = NodeConfigSyntaxError.class)
    public void testBracketedIntegerExpected() {
        String nodeConfig = "3,3,33.3,0.3,2,K=333.3,R=0.3,0,[55],55.5,0.5,1,X=0.55,0,[44],44.4,0.4,1,X=0.44,0";
        new NodeConfigParser().parse(nodeConfig);
    }

    @Test(expected = NodeConfigSyntaxError.class)
    public void testInvalidNodeParameter() {
        String nodeConfig = "3,[3],33.3,0.3,2,Z=333.3,R=0.3,0,[55],55.5,0.5,1,X=0.55,0,[44],44.4,0.4,1,X=0.44,0";
        new NodeConfigParser().parse(nodeConfig);
    }

    @Test(expected = NodeConfigSyntaxError.class)
    public void testLinkParameters() {
        String nodeConfig = "3,[3],33.3,0.3,2,K=333.3,R=0.3,2,[55],55.5,0.5,1,X=0.55,0,[44],44.4,0.4,1,X=0.44,0";
        new NodeConfigParser().parse(nodeConfig);
    }
    @Test(expected = NodeConfigSyntaxError.class)
    public void testBadNumberFormat() {
        String nodeConfig = "3,[3],_3.3,0.3,2,K=333.3,R=0.3,0,[55],55.5,0.5,1,X=0.55,0,[44],44.4,0.4,1,X=0.44,0";
        new NodeConfigParser().parse(nodeConfig);
    }
}
