package com.sweetspotdiabetes;

/**
 *  Created by Pete on 7/11/2016.
 */
public class Tools {

    public static void main(String[] args) {
        //calculateAandB(args);
        calculateAandBForMany(args);
    }

    public static void calculateAandB(String[] args) {
        System.out.println("args[0] = " + args[0]);
        int nTotalItems = new Integer(args[0]);
        calculateAandB(nTotalItems);
    }

    public static void calculateAandBForMany(String[] args) {
        int nHowMany = new Integer(args[0]);
        System.out.println("nHowMany = " + nHowMany);
        for (int nTotalItems = 4; nTotalItems <= nHowMany; nTotalItems++) {
            calculateAandB(nTotalItems);
        }
    }

    public static void calculateAandB(int nTotalItems) {
        double dQ = nTotalItems / 4.0;
        // Truncate the fractional quartile size, and use this integer size to skip lower quartile and highest quartile
        int nQ = (int) Math.floor(dQ);
        // The IQR is represented by indexes to: A) first item of IQR, and B) last item of IQR
        int idxA = nQ;                     // Omit the first nQ items
        int idxB = nTotalItems - nQ - 1;   // Omit the last nQ items

        System.out.println("n=" + nTotalItems + "   A,B = " + idxA + "," + idxB);
    }

}
