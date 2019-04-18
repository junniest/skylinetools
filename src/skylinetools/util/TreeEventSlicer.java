package skylinetools.util;

import beast.core.*;
import beast.evolution.tree.Node;
import beast.evolution.tree.*;
import beast.util.*;
import java.util.*;
import java.util.stream.*;

/**
 * TreeEventSlicer class
 *
 * @author Julija Pecerska
 *         Date: 2019/15/04
 *
 */
public class TreeEventSlicer extends TreeSlicer {

    public Input<String> breakAtInput =
            new Input<>("breakAt", "Where to break the intervals (branches/samples/branchsamples)");

    /* Break events on the tree */
    final static int BRANCHES      = 0,
                     SAMPLES       = 1,
                     BRANCHSAMPLES = 2;

    private List<Node> nodes = null;
    private int breakCriterion;

    @Override
    public void initAndValidate() {

        int dimension;
        String stopStr;
        String breakAt;

        /* Read tree */
        tree = treeInput.get();
        Anchor.update(tree);

        /* Read dimension of the slice */
        dimension = dimensionInput.get();

        /* Read to input (where to end slice) */
        if (toInput.get() != null)
            stopStr = toInput.get().toUpperCase().trim();
        else
            stopStr = "TMRCA";

        stop = null;
        for (Anchor a : Anchor.values()) {
            if (Anchor.valueOf(stopStr) == a) {
                stop = a;
            }
        }
        if (stop == null) {
            throw new IllegalArgumentException("Error in "+this.getID()+": Unknown anchor point ("+stopStr+") for to input.");
        }

        breakAt = breakAtInput.get().toLowerCase().trim();
        switch (breakAt) {
            case "branches":
            case "branch":
                breakCriterion = BRANCHES;
                break;
            case "samples":
            case "sample":
                breakCriterion = SAMPLES;
                break;
            case "branchsamples":
            case "branchsample":
                breakCriterion = BRANCHSAMPLES;
                break;
            default:
                throw new IllegalArgumentException("Unknown break criterion!");
        }

        /* Include the final anchor point as a breakpoint */
        inclusive = inclusiveInput.get();

        /* Initialise arrays */
        values = new Double[dimension];
        storedValues = new Double[dimension];

        calculateTimes(tree);

        // Initialization accounting (not really used)
        // Don't want to use super.initAndValidate() because we're doing something else with the values
        // m_fLower = Double.NEGATIVE_INFINITY;
        m_fLower = 0.0; // Height cannot be negative
        m_fUpper = Double.POSITIVE_INFINITY;
        m_bIsDirty = new boolean[dimensionInput.get()];
        minorDimension = minorDimensionInput.get();
        if (minorDimension > 0 && dimensionInput.get() % minorDimension > 0) {
            throw new IllegalArgumentException("Error in "+this.getID()+": Dimension must be divisible by stride");
        }
        this.storedValues = values.clone();

        timesKnown = false;
    }


    /**
     * Update the slice times
     *
     * Unfortunately tree.somethingIsDirty() does NOT appear to work to indicate if anchor times have changed so
     * updateAnchortimes() MUST be called.
     *
     * @param tree
     */
    @Override
    protected void calculateTimes(Tree tree) {

        /* Update newest, oldest, tmrca */
        Anchor.update(tree);
        double endTime = stop.getHeight();

        if (breakCriterion == SAMPLES) {
            // Ideally we don't need to recalculate these values, but the root time may change, so we either need to
            // update it here, which doesn't go with the flow of code, or just recalculate...
            nodes = tree.getExternalNodes();
            values = calculateTimesByNodes(endTime);
        } else
        if (breakCriterion == BRANCHES) {
            // Only add non-singletons
            nodes = tree.getInternalNodes().stream().filter(n -> n.getChildCount() > 1)
                    .collect(Collectors.<Node> toList());
            values = calculateTimesByNodes(endTime);
        } else
        if (breakCriterion == BRANCHSAMPLES) {
            nodes = tree.getInternalNodes().stream().filter(n -> n.getChildCount() > 1)
                    .collect(Collectors.<Node> toList());
            nodes.addAll(tree.getExternalNodes());
            values = calculateTimesByNodes(endTime);
        }

        timesKnown = true;
    }

    protected Double[] calculateTimesByNodes(double endtime) {
        int nodeNumber = nodes.size();
        int dimensions = getDimension();
        double [] nodeTimes = new double[nodeNumber];
        Double [] changeTimes = new Double[dimensions];

        int intervalNumber;
        if (inclusive)
            intervalNumber = dimensions - 1;
        else
            intervalNumber = dimensions;

        for (int j = 0; j < nodeNumber; j++) {
            nodeTimes[j] = this.nodes.get(j).getHeight();
        }
        HeapSort.sort(nodeTimes);

        int groupSize = (int) Math.round((double) nodeNumber / (intervalNumber - 1));
        int lastGroupSize = nodeNumber - groupSize * (intervalNumber - 2);
        int i = 0;
        changeTimes[0] = 0.0;
        for (int j = 1; j < intervalNumber; j++) {
            if (i + lastGroupSize >= nodeNumber) {
                changeTimes[j] = nodeTimes[nodeNumber - 1] + EPS;
                break;
            } else {
                i += groupSize;
                changeTimes[j] = (nodeTimes[i - 1] + nodeTimes[i]) / 2;
            }
        }

        if (inclusive) changeTimes[intervalNumber] = endtime;
        return changeTimes;
    }

    @Override
    protected boolean requiresRecalculation() {
        // Tree is a stateNode so should always use somethingIsDirty() and NOT isDirtyCalculation!
        //System.out.println("Checking recalculation "+this.ID+" "+tree.isDirtyCalculation()+" "+tree.somethingIsDirty());
        timesKnown = false;
        return true;
        //return tree.somethingIsDirty();
    }


}
