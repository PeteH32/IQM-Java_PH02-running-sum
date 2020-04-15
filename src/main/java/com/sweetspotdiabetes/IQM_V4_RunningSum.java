package com.sweetspotdiabetes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class IQM_V4_RunningSum {

    public static void main(String[] args) {
        //processFile(".\\src\\test\\resources\\data_small.txt");
        //processFile("data_25k.txt");
        processFile("data.txt");
    }
    
    public static double processFile(String strFile) {
        long startTime = System.currentTimeMillis();
        Vector<Integer> sortedData = new Vector<Integer>();
        IQR_Info savedIQRInfo = new IQR_Info();

        try {
            FileReader reader = new FileReader(strFile);
            BufferedReader br = new BufferedReader(reader);

            String line;
            while ((line = br.readLine()) != null) {
                Integer newVal = Integer.parseInt(line.trim());
                sortedData.add(newVal);
                Collections.sort(sortedData);

                if (sortedData.size() > 4) {
                    // Get this increment's IQR mean.
                    getMean_V4(newVal, sortedData, savedIQRInfo);
                } else if (sortedData.size() == 4) {
                    handleSpecialCaseOfOnly4Items(sortedData, savedIQRInfo);
                }
                System.out.println(sortedData.size() + ": " + String.format("%.2f%n", savedIQRInfo.weightedMeanForIQR));
            }
        } catch (Throwable t) {
            System.out.println("badness happened.");
            t.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Total milliseconds: " + (endTime - startTime));
        System.out.println("Total minutes: " + (endTime - startTime) / (double) (1000 * 60));

        return(savedIQRInfo.weightedMeanForIQR);
    }
    
    public static double handleSpecialCaseOfOnly4Items(List<Integer> sortedData, IQR_Info savedIQRInfo) {
        // Only 4 items, so trivial to calculate IQR.
        savedIQRInfo.wholeSumIQR = (sortedData.get(1) + sortedData.get(2));
        savedIQRInfo.weightedMeanForIQR = savedIQRInfo.wholeSumIQR / (double) 2;

        // Seed info for savedIQRInfo (since this would be first time calculating anything).
        savedIQRInfo.idxA = 1;
        savedIQRInfo.valA = sortedData.get(savedIQRInfo.idxA);
        savedIQRInfo.valPreceedingA = sortedData.get(savedIQRInfo.idxA - 1);    // save for when we do next IQR
        savedIQRInfo.idxB = 2;
        savedIQRInfo.valB = sortedData.get(savedIQRInfo.idxB);
        savedIQRInfo.valPreceedingB = sortedData.get(savedIQRInfo.idxB - 1);    // save for when we do next IQR
        
        return savedIQRInfo.weightedMeanForIQR;
    }

    public static class IQR_Info {
        // The IQR is represented by indexes to: 
        //      A: first item of the IQR
        //      B: last item of the IQR
        // Visual representation:
        //                 |-------- IQR -------|
        //     |-- Q1 --|  |-- Q2 --|  |-- Q3 --|  |-- Q4 --|
        //                 ^                    ^
        //                 A                    B
        public int idxA;  // index to first item of the IQR
        public int valA;  // value before any weighting applied
        public int valPreceedingA;      // also need this to determine which Q newVal got inserted into
        public int idxB;  // index to last item of the IQR
        public int valB;  // value before any weighting applied
        public int valPreceedingB;      // also need this to determine which Q newVal got inserted into

        public long wholeSumIQR = 0;    // sum before any weighting applied

        public double weightedMeanForIQR = 0.0;

        public static void copyValues(IQR_Info from, IQR_Info to) {
            to.idxA = from.idxA;
            to.valA = from.valA;
            to.valPreceedingA = from.valPreceedingA;
            to.idxB = from.idxB;
            to.valB = from.valB;
            to.valPreceedingB = from.valPreceedingB;
            to.wholeSumIQR = from.wholeSumIQR;
            to.weightedMeanForIQR = from.weightedMeanForIQR;
        }
    }
    
    public static double getMean_V4(Integer newVal, List<Integer> sortedData, IQR_Info iqr1){
        // See following references:
        //      https://en.wikipedia.org/wiki/Interquartile_range
        //          - Understand this before starting rest.
        //      https://en.wikipedia.org/wiki/Interquartile_mean
        //          - Ignore the mathmatical sum-thingie (E) calculation. It's better to read the examples.

        // NEW IQR ENDPOINTS (A & B)
        // Determine new IQR after newVal was added. IQR is represented by points "A" and "B".
        IQR_Info iqr2 = new IQR_Info();
        int nTotalItems = sortedData.size();
        // Calculate quartile size as fraction, like 4.25 items
        double dQ = nTotalItems / 4.0;
        // Truncate the fractional quartile size, and use this integer size to skip lower quartile (Q1) and highest quartile (Q4).
        int nQ = (int) Math.floor(dQ);
        // The IQR is represented by indexes to two key items: 
        //      "A" first item of IQR
        //      "B" last item of IQR
        // Visual representation:
        //                 |-------- IQR -------|
        //     |-- Q1 --|  |-- Q2 --|  |-- Q3 --|  |-- Q4 --|
        //                 ^                    ^
        //                 A                    B
        iqr2.idxA = nQ;                                 // Omit the first nQ items
        iqr2.valA = sortedData.get(iqr2.idxA);                  // save for when we do next IQR
        iqr2.valPreceedingA = sortedData.get(iqr2.idxA - 1);    // save for when we do next IQR
        iqr2.idxB = nTotalItems - nQ - 1;               // Omit the last nQ items
        iqr2.valB = sortedData.get(iqr2.idxB);                  // save for when we do next IQR
        iqr2.valPreceedingB = sortedData.get(iqr2.idxB - 1);    // save for when we do next IQR
        // Get sublist view representing the IQR (note this does not copy items, it is merely a view into original list).
        List<Integer> listIQR = sortedData.subList(iqr2.idxA, (iqr2.idxB + 1));

        // NON-WEIGHTED SUM
        // Non-weighted sum (wholeSumIQR) of IQR is before applying "weighting" of the two end items.
        iqr2.wholeSumIQR = determineNewWholeSumIQR(newVal, iqr1, iqr2);
        
        // WEIGHTED SUM & WEIGHTED MEAN
        // If quartile size (dQ) was fractional, the two end items need to be "weighted" a little bit 
        // is subtracted from each end item, to create the weighted mean.
        double fractionToSubtract = dQ - Math.floor(dQ);        // just the fractional part of dQ
        double newWeightedMeanIQR;
        if (fractionToSubtract != 0) {
            double weightedSumIQR = iqr2.wholeSumIQR;
            // Subtract some out for each end item. The two end items are fractional items so only a fractional weighted value of them is used.
            weightedSumIQR -= (fractionToSubtract * listIQR.get(0));                  // Item on front end.
            weightedSumIQR -= (fractionToSubtract * listIQR.get(listIQR.size() - 1)); // Item on back end.
            // The count of items in IQR also needs weighted for the two fractional items on the ends.
            double weightedCountIQR = listIQR.size() - (2 * fractionToSubtract);
            newWeightedMeanIQR = weightedSumIQR / weightedCountIQR;
        } else {
            // The two end items are whole items and not fractional items, so no weighting of them is needed.
            newWeightedMeanIQR = iqr2.wholeSumIQR / (double) listIQR.size();
        }
        iqr2.weightedMeanForIQR = newWeightedMeanIQR;

        // Save iqr2's info for next iteration, where it will be iqr1.
        IQR_Info.copyValues(iqr2, iqr1);   // iqr1 is what the caller is using to save info between iterations
        
        return iqr2.weightedMeanForIQR;
    }
    
    public enum Quarter {Q1, IQR, Q4, INVALID};     // "IQR" is Q2+Q3, since we don't really care which one it is

    static private long determineNewWholeSumIQR(int newVal, IQR_Info iqr1, IQR_Info iqr2) {
        // Calculate new whole sum of the IQR, before any weighting is applied. Instead of re-summing every item in IQR, 
        // we will extrapolate from iqr1.wholeSumIQR to get new iqr2.wholeSumIQR (avoids an inner loop, to improve performance). 

        // Visual representation:
        //                 |-------- IQR -------|
        //     |-- Q1 --|  |-- Q2 --|  |-- Q3 --|  |-- Q4 --|
        //                 ^                    ^
        //                 A                    B
        // iqr1 = BEFORE adding newVal
        // iqr2 = AFTER adding newVal
        //
        // Only 3 things can change “wholeSumIQR”:
        //    (1) “newVal” could have been inserted into the IQR
        //    (2) “A” could move up one (higher) 
        //    (3) “B” could move up one (higher)

        // First determine if A or B shifted.
        boolean bAShifted;
        if (iqr1.idxA == iqr2.idxA) {
            bAShifted = false;
        } else if (iqr1.idxA + 1 == iqr2.idxA) {
            bAShifted = true;
        } else {
            throw new RuntimeException("A moved in mathmetically impossible way.");
        }

        boolean bBShifted;
        if (iqr1.idxB == iqr2.idxB) {
            bBShifted = false;
        } else if (iqr1.idxB + 1 == iqr2.idxB) {
            bBShifted = true;
        } else {
            throw new RuntimeException("B moved in mathmetically impossible way.");
        }
        
        if (bAShifted && bBShifted) {
            throw new RuntimeException("Both A and B moved, which is mathmetically impossible.");
        }
        if (!bAShifted && !bBShifted) {
            throw new RuntimeException("Neither A nor B moved, which is mathmetically impossible.");
        }

        // Next determine which quarter the newVal was inserted into.
        Quarter newValInsertedInto = Quarter.INVALID;
        if (    (newVal < iqr1.valA) ||
                (newVal == iqr1.valPreceedingA) ||
                ((newVal == iqr1.valA) && (bAShifted)) ) {
            newValInsertedInto = Quarter.Q1;
        } else if (     ((newVal > iqr1.valA) && (newVal < iqr1.valB)) ||
                        ((newVal == iqr1.valA) && (newVal != iqr1.valPreceedingA) && (!bAShifted)) ) {
            newValInsertedInto = Quarter.IQR;
        } else if (newVal == iqr1.valB) {           // based also on the other conditions checked before this
            newValInsertedInto = Quarter.IQR;
        } else if (newVal > iqr1.valB) {            // redundant check at this point, but doing it to be explicit
            newValInsertedInto = Quarter.Q4;
        }

        if (newValInsertedInto == Quarter.INVALID) {
            throw new RuntimeException("Could not determine which quarter newVal was inserted into.");
        }

        // Now adjust wholeSumIQR based on the only 3 things that could have changed wholeSumIQR (see comment above).
        // Start with same value, then make needed adjustments
        iqr2.wholeSumIQR = iqr1.wholeSumIQR;
        // Does newVal need added to IQR?
        if (newValInsertedInto == Quarter.IQR) {
            // Need to add newVal to the whole sum 
            iqr2.wholeSumIQR += newVal;
        }
        // Did A or B move?
        if (bAShifted && !bBShifted) {
            // Check if old A is no longer in the IQR, since A shifted up
            if (newValInsertedInto != Quarter.Q1){
                iqr2.wholeSumIQR -= iqr1.valA;
            }
            // Check if old B is no longer in the IQR, since B did not shift up
            if ((newValInsertedInto == Quarter.Q1) || (newValInsertedInto == Quarter.IQR)) {
                iqr2.wholeSumIQR -= iqr1.valB;
            }
        } else if (!bAShifted && bBShifted) {
            // Check if new A was not in the IQR before, but is now since A did not shift up
            if (newValInsertedInto == Quarter.Q1){
                iqr2.wholeSumIQR += iqr2.valA;
            }
            // Check if new B was not in the IQR before, but is now since B shifted up
            if (newValInsertedInto == Quarter.Q4){
                iqr2.wholeSumIQR += iqr2.valB;
            }
        } else {
            throw new RuntimeException("A and B moved in mathmetically impossible way.");
        }
        
        return iqr2.wholeSumIQR;
    }
    
}
