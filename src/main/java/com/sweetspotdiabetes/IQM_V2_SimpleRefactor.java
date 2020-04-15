package com.sweetspotdiabetes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class IQM_V2_SimpleRefactor {

    public static void main(String[] args) {
        //processFile(".\\src\\test\\resources\\data_small.txt");
        //processFile("data_25k.txt");
        processFile("data.txt");
    }

    public static double processFile(String strFile) {
        double finalMean = 0.0;
        long startTime = System.currentTimeMillis();
        Vector<Integer> sortedData = new Vector<Integer>();

        try {
            FileReader reader = new FileReader(strFile);
            BufferedReader br = new BufferedReader(reader);

            String line;
            while ((line = br.readLine()) != null) {
                Integer aVal = Integer.parseInt(line.trim());
                sortedData.add(aVal);
                Collections.sort(sortedData);

                if (sortedData.size() >= 4) {
                    // Get this increment's IQR mean.
                    double weightedMeanIQR = getMean_RefactoredCode(sortedData);
                    System.out.println(sortedData.size() + ": " + String.format("%.2f%n",weightedMeanIQR));

                    finalMean = weightedMeanIQR;
                }
            }
        } catch (Throwable t) {
            System.out.println("badness happened.");
            t.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Total milliseconds: " + (endTime - startTime));
        System.out.println("Total minutes: " + (endTime - startTime) / (double) (1000 * 60));

        return(finalMean);
    }

    public static double getMean_RefactoredCode(List<Integer> data) {
        // New code.

        // See following references:
        //      https://en.wikipedia.org/wiki/Interquartile_range
        //          - Understand this before starting rest.
        //      https://en.wikipedia.org/wiki/Interquartile_mean
        //          - Ignore the mathmatical sum-thingie (E) calculation. It's better to read the examples.
        int nTotalItems = data.size();
        // Calculate quartile size as fraction, like 4.25 items
        double dQ = nTotalItems / 4.0;
        // Truncate the fractional quartile size, and use this integer size to skip lower quartile and highest quartile
        int nQ = (int) Math.floor(dQ);
        // IQR is "Interquartile Range" - https://en.wikipedia.org/wiki/Interquartile_range
        int nFirstIndexIQR = nQ;        // Omit the first nQ items
        int nLastIndexIQR = nTotalItems - nQ - 1; // Omit the last nQ items
        List<Integer> listIQR = data.subList(nFirstIndexIQR, (nLastIndexIQR + 1));

        // Sum all items in the IQR. Note this is not yet doing any "weighting" of the two end items.
        int sumIQR = 0;
        for (Integer item : listIQR) {
            sumIQR += item;
        }

        // If quartile size (dQ) was fractional, the two end items need to be "weighting".
        // A little bit is subtracted from each, to create the weighted mean.
        double fractionToSubtract = dQ - Math.floor(dQ);        // just the fractional part of dQ
        double weightedMeanIQR;
        if (fractionToSubtract != 0) {
            double weightedSumIQR = sumIQR;
            // The two end items are fractional items so only a fractional weighted value of them is used. Subtract out for each.
            weightedSumIQR -= (fractionToSubtract * listIQR.get(0));                  // Item on front end.
            weightedSumIQR -= (fractionToSubtract * listIQR.get(listIQR.size() - 1)); // Item on back end.
            // The count of items in IQR also needed weighted for the two fractional items on the ends.
            double weightedCountIQR = listIQR.size() - (2 * fractionToSubtract);
            weightedMeanIQR = weightedSumIQR / weightedCountIQR;
        } else {
            // The two end items are whole items and not fractional items, so no weighting of them is needed.
            weightedMeanIQR = sumIQR / (double) listIQR.size();
        }

        return weightedMeanIQR;
    }

    public static double getMean_OriginalCode(List<Integer> data) {
        // Old code.

        double dQ = data.size() / 4.0;
        int i = (int) Math.ceil(dQ) - 1;
        int c = (int) Math.floor(dQ * 3) + 1;

        List<Integer> ys = data.subList(i, c);
        double factor = dQ - ((ys.size() / 2.0) - 1);
        int sum = 0;

        for (int j = 1; j < ys.size(); j++) {
            if (j == (ys.size() - 1)) {
                break;
            }

            sum += ys.get(j);
        }

        double mean = (sum + (ys.get(0) + ys.get(ys.size() - 1)) * factor) / (2 * dQ);

        return mean;
    }

}
