package com.sweetspotdiabetes;

import com.sweetspotdiabetes.IQM_V2_SimpleRefactor;
import com.sweetspotdiabetes.IQM_V4_RunningSum;
import org.testng.Assert;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collections;
import java.util.Vector;

/**
 *  Created by Pete on 6/17/2016.
 */
public class IncrementalIQM_CompareOldNew_Test {


    public static void main(String[] args) throws Exception {
        //testThisFile(".\\src\\test\\resources\\data_small.txt", 303.75);
        testThisFile(".\\data_10k.txt", -1);
    }


    // The following data sets are from Wikipedia article:
    //      https://en.wikipedia.org/wiki/Interquartile_mean#Examples
    @org.testng.annotations.Test
    public void data_test_01() throws Exception {
        testThisFile(".\\src\\test\\resources\\data_test_01.txt", 6.5);
    }
    @org.testng.annotations.Test
    public void data_test_02() throws Exception {
        testThisFile(".\\src\\test\\resources\\data_test_02.txt", 3.0);
    }
    @org.testng.annotations.Test
    public void data_test_03() throws Exception {
        testThisFile(".\\src\\test\\resources\\data_test_03.txt", 9.0);
    }
    @org.testng.annotations.Test
    public void data_test_03a() throws Exception {
        testThisFile(".\\src\\test\\resources\\data_test_03a.txt", 9.0);
    }
    @org.testng.annotations.Test
    public void data_test_03b() throws Exception {
        testThisFile(".\\src\\test\\resources\\data_test_03b.txt", 9.0);
    }
    @org.testng.annotations.Test
    public void data_test_03c() throws Exception {
        testThisFile(".\\src\\test\\resources\\data_test_03c.txt", 9.0);
    }

    // The following data sets are from:
    //      Samples provided by Justin, and subsets of them.
    @org.testng.annotations.Test
    public void data_10k() throws Exception {
        testThisFile(".\\data_10k.txt", 357.28735632183907000000000000000000);
    }
    @org.testng.annotations.Test
    public void data_25k() throws Exception {
        testThisFile(".\\data_25k.txt", 438.87336000000000000000000000000000);
    }


    
    private static void testThisFile(String strFile, double expectedMean) {
        double finalMean = processFile_CompareAllMethods(strFile);

        System.out.println("finalMean = " + String.format("%.32f",finalMean) + "    expectedMean = " + String.format("%.32f",expectedMean) + "\n");
        if (expectedMean != -1) {
            Assert.assertEquals(finalMean, expectedMean);
        }
    }

    private static double processFile_CompareAllMethods(String strFile) {
        double finalMean = 0.0;
        long startTime = System.currentTimeMillis();
        Vector<Integer> sortedData = new Vector<Integer>();
        IQM_V4_RunningSum.IQR_Info savedIQRInfo = new IQM_V4_RunningSum.IQR_Info();

        try {
            FileReader reader = new FileReader(strFile);
            BufferedReader br = new BufferedReader(reader);

            String line;
            while ((line = br.readLine()) != null) {
                Integer newVal = Integer.parseInt(line.trim());
                sortedData.add(newVal);
                Collections.sort(sortedData);

                // Every row in data file gets passed to all 3 methods, so we verify same result for every single
                // row in the data file, not just the final value.
                if (sortedData.size() >= 4) {
                    System.out.println("data.size() = " + sortedData.size());

                    // Original code from Justin, before any refactoring
                    double weightedMeanIQR_OriginalCode;
                    weightedMeanIQR_OriginalCode = IQM_V2_SimpleRefactor.getMean_OriginalCode(sortedData);
                    System.out.println(sortedData.size() + ": getMean_OriginalCode() = " + String.format("%.32f", weightedMeanIQR_OriginalCode));

                    // V2: Refactored code
                    double weightedMeanIQR_V2_RefactoredCode;
                    weightedMeanIQR_V2_RefactoredCode = IQM_V2_SimpleRefactor.getMean_RefactoredCode(sortedData);
                    System.out.println(sortedData.size() + ": getMean_RefactoredCode() = " + String.format("%.32f", weightedMeanIQR_V2_RefactoredCode));

                    // V4: Using "running sum" method
                    double weightedMeanIQR_V4;
                    if (sortedData.size() > 4) {
                        weightedMeanIQR_V4 = IQM_V4_RunningSum.getMean_V4(newVal, sortedData, savedIQRInfo);
                    } else {
                        // Special case to seed savedIQRInfo.
                        weightedMeanIQR_V4 = IQM_V4_RunningSum.handleSpecialCaseOfOnly4Items(sortedData, savedIQRInfo);
                    }
                    System.out.println(sortedData.size() + ": getMean_V4() = " + String.format("%.32f", weightedMeanIQR_V4));

                    // Verify all 3 methods got same result on this row.
                    Assert.assertEquals(weightedMeanIQR_OriginalCode, weightedMeanIQR_V2_RefactoredCode, 0);
                    Assert.assertEquals(weightedMeanIQR_OriginalCode, weightedMeanIQR_V4, 0);

                    finalMean = weightedMeanIQR_V2_RefactoredCode;
                } else if (sortedData.size() == 4) {
                }
            }
        } catch (Throwable t) {
            System.out.println("badness happened.");
            t.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("com.sweetspotdiabetes.IncrementalIQM_CompareOldNew_Test: Total milliseconds: " + (endTime - startTime));
        System.out.println("Total minutes: " + (endTime - startTime) / (double) (1000 * 60));

        return(finalMean);
    }

}
