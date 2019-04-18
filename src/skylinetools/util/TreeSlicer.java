package skylinetools.util;

import beast.core.*;
import beast.core.parameter.*;
import beast.evolution.tree.*;
import java.util.*;


/**
 * Base TreeSlicer class
 *
 * Equidistant slices between present and an anchor point on the tree.
 *
 * Input dates always have dimension one less than the treeslicer itself (unless the last date is equal to the most recent tip).
 * (Since the last value in the slice has to be 0 (i.e. the most recent tip in the tree).
 *
 *
 * @author Louis du Plessis
 *         Date: 2018/02/12
 *
 */
public class TreeSlicer extends RealParameter {
    static final double EPS = 1e-7;

    public Input<Tree> treeInput =
            new Input<>("tree", "Tree over which to calculate the slice", Input.Validate.REQUIRED);

    public Input<String> toInput =
            new Input<>("to", "Anchor point to stop the slicing intervals (tmrca/oldestsample)", "tmrca");

    public Input<Boolean> inclusiveInput =
            new Input<>("inclusive", "Include the final anchor point (to criterion) in the vector",true);



    /* Anchor times on the tree */
    protected enum Anchor {

        PRESENT,       // The present, or the time of the most recent sample, should be at height 0.0
        OLDESTSAMPLE,  // The height of the oldest sample in the tree, 0 <= OLDESTSAMPLE <= TMRCA
        TMRCA;         // The height of the tree (tMRCA)


        private double height, date;

        public double getHeight() { return this.height; }

        public double getDate() { return this.date; }


        /**
         * Update anchor times in the tree
         *
         * O(n) for n nodes
         *
         * Use getNodesAsArray() instead of getExternalNodes() because it only passes a pointer whereas getExterNodes()
         * requires constructing an ArrayList (so it is O(n) plus a lot of extra memory operations).
         *
         * Unfortunately tree.somethingIsDirty() does NOT appear to work to indicate if anchor times would have changed or
         * not, so this MUST be recalculated every time the times are updated.
         *
         * @param tree
         */
        public static void update(Tree tree) {

            double height;

            TMRCA.height = tree.getRoot().getHeight();
            TMRCA.date   = tree.getRoot().getDate();

            // This next part should only be necessary when tipdates are sampled (How can this be checked?)
            // May be optimised by skipping the first n-1 nodes, but this way is very secure, though slightly slower
            OLDESTSAMPLE.height = 0;
            PRESENT.height = TMRCA.height;
            for (Node N : tree.getNodesAsArray()) {
                if (N.isLeaf()) {
                    height = N.getHeight();

                    if (height > OLDESTSAMPLE.height) {
                        // Need to adjust so BDSKY likelihood nonzero
                        OLDESTSAMPLE.height = height + EPS;
                        OLDESTSAMPLE.date   = N.getDate();
                    }

                    if (height  < PRESENT.height) {
                        PRESENT.height  = height;
                        PRESENT.date    = N.getDate();
                    }
                }
            }

        }

    }
    /* End Anchor times */


    protected Tree tree;
    protected Anchor stop;
    protected boolean inclusive;
    protected boolean timesKnown;


    // Override input rule of RealParameter (base class)
    public TreeSlicer() {
        valuesInput.setRule(Input.Validate.OPTIONAL);
    }

    @Override
    public void initAndValidate() {

        int dimension;
        String stopStr;

        /* Read tree */
        tree      = treeInput.get();
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


        /* Include the final anchor point as a breakpoint */
        inclusive = inclusiveInput.get();


        /* Initialise arrays */
        values = new Double[dimension];
        storedValues = new Double[dimension];
        calculateTimes(tree);
        // System.out.println(this.ID+"\t"+this.getDimension());


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



    /* Methods should only be called after anchor times have been updated */

    protected double dateToHeight(double date) {
        Anchor.update(tree);
        return (Anchor.PRESENT.getDate() - date);
    }

    protected double heightToDate(double height) {
        Anchor.update(tree);
        return (Anchor.PRESENT.getDate() - height);
    }


   /**
     * Update the slice times
     *
     * Unfortunately tree.somethingIsDirty() does NOT appear to work to indicate if anchor times have changed so
     * updateAnchortimes() MUST be called.
     *
     * @param tree
     */
    protected void calculateTimes(Tree tree) {

        double endTime, stepSize;

        /* Update anchor times */
        Anchor.update(tree);
        //for (Anchor a : Anchor.values()) {
        //    System.out.println(a.toString()+"\t"+a.getHeight()+"\t"+a.getDate());
        //}

        endTime = stop.getHeight();
        if (inclusive)
            stepSize = endTime / (getDimension() - 1);
        else
            stepSize = endTime / (getDimension());


        for (int i = 0; i < getDimension(); i++) {
            values[i] = i * stepSize;
        }

        timesKnown = true;
    }


    @Override
    protected boolean requiresRecalculation() {
        // Tree is a stateNode so should always use somethingIsDirty() and NOT isDirtyCalculation!
        //System.out.println("Checking recalculation "+this.ID+" "+tree.isDirtyCalculation()+" "+tree.somethingIsDirty());
        timesKnown = false;
        return true;
        //return tree.somethingIsDirty();
    }


    /* Override methods to make sure times get recalculated whenever times are accessed */

    @Override
    public Double getValue() {
        if (!timesKnown) {
            calculateTimes(tree);
        }
        return values[0];
    }

    @Override
    public Double getValue(final int index) {
        if (!timesKnown) {
            calculateTimes(tree);
        }
        return values[index];
    }

    @Override
    public double getArrayValue() {
        if (!timesKnown) {
            calculateTimes(tree);
        }
        return values[0];
    }

    @Override
    public double getArrayValue(final int index) {
        if (!timesKnown) {
            calculateTimes(tree);
        }
        return values[index];
    }

    @Override
    public Double [] getValues() {
        if (!timesKnown) {
            calculateTimes(tree);
        }
        return Arrays.copyOf(values, values.length);
    }

}
