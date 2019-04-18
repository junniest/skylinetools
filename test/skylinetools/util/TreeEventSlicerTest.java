package skylinetools.util;

import beast.evolution.tree.*;
import beast.util.*;
import junit.framework.*;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author Julija Pecerska
 *         Date: 2019/04/17
 */
public class TreeEventSlicerTest extends TestCase {

    @Test
    public void testSliceSamples() {

        Tree tree = new TreeParser("((((((G:1,F:2):1,E:4):1,D:6):1,C:8):1,B:10):1);", false);
        TreeEventSlicer treeSlicer = new TreeEventSlicer();
        treeSlicer.initByName("tree", tree, "to", "tmrca", "inclusive", "false",
                "dimension", "4", "breakAt", "samples");
        Double [] expected0 = {0.0, 1.5, 3.5, 5.0 + treeSlicer.EPS};
        assertArrayEquals(treeSlicer.getValues(), expected0);

        tree = new TreeParser("((((((((G:1,F:2):1,E:4):1,D:6):1,C:8):1,B:10):1,A:12):1,O:14):1);", false);
        treeSlicer = new TreeEventSlicer();
        treeSlicer.initByName("tree", tree, "to", "tmrca", "inclusive", "false",
                "dimension", "4", "breakAt", "samples");
        Double [] expected1 = {0.0, 2.5, 5.5, 7.0 + treeSlicer.EPS};
        assertArrayEquals(treeSlicer.getValues(), expected1);

        tree = new TreeParser("(((((((G:1,F:2):1,E:4):1,D:6):1,C:8):1,B:10):1,A:12):1);", false);
        treeSlicer = new TreeEventSlicer();
        treeSlicer.initByName("tree", tree, "to", "tmrca", "inclusive", "false",
                "dimension", "4", "breakAt", "samples");
        Double [] expected2 = {0.0, 1.5, 3.5, 6.0 + treeSlicer.EPS};
        assertArrayEquals(treeSlicer.getValues(), expected2);

        treeSlicer = new TreeEventSlicer();
        treeSlicer.initByName("tree", tree, "to", "tmrca", "inclusive", "true",
                               "dimension", "5", "breakAt", "samples");
        Double [] expected3 = {0.0, 1.5, 3.5, 6.0 + treeSlicer.EPS, 13.0};
        assertArrayEquals(treeSlicer.getValues(), expected3);

        treeSlicer = new TreeEventSlicer();
        treeSlicer.initByName("tree", tree, "to", "tmrca", "inclusive", "true",
                               "dimension", "4", "breakAt", "samples");
        Double [] expected4 = {0.0, 3.5, 6.0 + treeSlicer.EPS, 13.0};
        assertArrayEquals(treeSlicer.getValues(), expected4);

        treeSlicer.tree.scale(2.0);
        treeSlicer.requiresRecalculation();
        Double [] expected5 = {0.0, 3.5, 6.0 + treeSlicer.EPS, 26.0};
        assertArrayEquals(treeSlicer.getValues(), expected5);
    }

    @Test
    public void testSliceBranches() {

        Tree tree = new TreeParser("((((((G:1,F:2):1,E:4):1,D:6):1,C:8):1,B:10):1);", false);
        TreeEventSlicer treeSlicer = new TreeEventSlicer();
        treeSlicer.initByName("tree", tree, "to", "tmrca", "inclusive", "false",
                "dimension", "4", "breakAt", "branches");
        Double [] expected0 = {0.0, 7.5, 9.5, 10.0 + treeSlicer.EPS};
        assertArrayEquals(treeSlicer.getValues(), expected0);

        tree = new TreeParser("((((((((G:1,F:2):1,E:4):1,D:6):1,C:8):1,B:10):1,A:12):1,O:14):1);", false);
        treeSlicer = new TreeEventSlicer();
        treeSlicer.initByName("tree", tree, "to", "tmrca", "inclusive", "false",
                "dimension", "4", "breakAt", "branches");
        Double [] expected1 = {0.0, 9.5, 11.5, 14.0 + treeSlicer.EPS};
        assertArrayEquals(treeSlicer.getValues(), expected1);

        tree = new TreeParser("(((((((G:1,F:2):1,E:4):1,D:6):1,C:8):1,B:10):1,A:12):1);", false);
        treeSlicer = new TreeEventSlicer();
        treeSlicer.initByName("tree", tree, "to", "tmrca", "inclusive", "false",
                "dimension", "4", "breakAt", "branches");
        Double [] expected2 = {0.0, 8.5, 10.5, 12.0 + treeSlicer.EPS};
        assertArrayEquals(treeSlicer.getValues(), expected2);

        treeSlicer = new TreeEventSlicer();
        treeSlicer.initByName("tree", tree, "to", "tmrca", "inclusive", "true",
                "dimension", "5", "breakAt", "branches");
        Double [] expected3 = {0.0, 8.5, 10.5, 12.0 + treeSlicer.EPS, 13.0};
        assertArrayEquals(treeSlicer.getValues(), expected3);

        treeSlicer = new TreeEventSlicer();
        treeSlicer.initByName("tree", tree, "to", "tmrca", "inclusive", "true",
                "dimension", "4", "breakAt", "branches");
        Double [] expected4 = {0.0, 9.5, 12.0 + treeSlicer.EPS, 13.0};
        assertArrayEquals(treeSlicer.getValues(), expected4);

        treeSlicer.tree.scale(2.0);
        treeSlicer.requiresRecalculation();
        Double [] expected5 = {0.0, 19.0, 24.0 + treeSlicer.EPS, 26.0};
        assertArrayEquals(treeSlicer.getValues(), expected5);
    }

    @Test
    public void testSliceBranchesSamples() {
        Tree tree = new TreeParser("((((((G:1,F:2):1,E:4):1,D:6):1,C:8):1,B:10):1);", false);
        TreeEventSlicer treeSlicer = new TreeEventSlicer();
        treeSlicer.initByName("tree", tree, "to", "tmrca", "inclusive", "false",
                "dimension", "4", "breakAt", "branchsamples");
        Double [] expected0 = {0.0, 3.5, 7.5, 10.0 + treeSlicer.EPS};
        assertArrayEquals(treeSlicer.getValues(), expected0);

        tree = new TreeParser("((((((((G:1,F:2):1,E:4):1,D:6):1,C:8):1,B:10):1,A:12):1,O:14):1);", false);
        treeSlicer = new TreeEventSlicer();
        treeSlicer.initByName("tree", tree, "to", "tmrca", "inclusive", "false",
                "dimension", "4", "breakAt", "branchsamples");
        Double [] expected1 = {0.0, 4.5, 9.5, 14.0 + treeSlicer.EPS};
        assertArrayEquals(treeSlicer.getValues(), expected1);

        tree = new TreeParser("(((((((G:1,F:2):1,E:4):1,D:6):1,C:8):1,B:10):1,A:12):1);", false);
        treeSlicer = new TreeEventSlicer();
        treeSlicer.initByName("tree", tree, "to", "tmrca", "inclusive", "false",
                "dimension", "4", "breakAt", "branchsamples");
        Double [] expected2 = {0.0, 3.5, 7.5, 12.0 + treeSlicer.EPS};
        assertArrayEquals(treeSlicer.getValues(), expected2);

        treeSlicer = new TreeEventSlicer();
        treeSlicer.initByName("tree", tree, "to", "tmrca", "inclusive", "true",
                "dimension", "5", "breakAt", "branchsamples");
        Double [] expected3 = {0.0, 3.5, 7.5, 12.0 + treeSlicer.EPS, 13.0};
        assertArrayEquals(treeSlicer.getValues(), expected3);

        treeSlicer = new TreeEventSlicer();
        treeSlicer.initByName("tree", tree, "to", "tmrca", "inclusive", "true",
                "dimension", "4", "breakAt", "branchsamples");
        Double [] expected4 = {0.0, 6.5, 12.0 + treeSlicer.EPS, 13.0};
        assertArrayEquals(treeSlicer.getValues(), expected4);

        treeSlicer.tree.scale(2.0);
        treeSlicer.requiresRecalculation();
        Double [] expected5 = {0.0, 10.0, 24.0 + treeSlicer.EPS, 26.0};
        assertArrayEquals(treeSlicer.getValues(), expected5);
    }
}
