package com.sweetspotdiabetes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class IncrementalIQM_ORIG {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        Vector<Integer> data = new Vector<Integer>();

        try {
            //FileReader reader = new FileReader("data_25k.txt");
            FileReader reader = new FileReader("data.txt");
            BufferedReader br = new BufferedReader(reader);

            String line;
            while ((line = br.readLine()) != null) {
                Integer aVal = Integer.parseInt(line.trim());
                data.add(aVal);
                Collections.sort(data);

                if (data.size() >= 4) {
                    double q = data.size() / 4.0;
                    int i = (int)Math.ceil(q) - 1;
                    int c = (int) Math.floor(q * 3) + 1;

                    List<Integer> ys = data.subList(i, c);
                    double factor = q - ((ys.size() / 2.0) - 1);
                    int sum = 0;

                    for (int j = 1; j < ys.size(); j++) {
                        if (j == (ys.size() - 1)) {
                            break;
                        }

                        sum += ys.get(j);
                    }

                    double mean = (sum + (ys.get(0) + ys.get(ys.size() - 1)) * factor) / ( 2 * q);
                    System.out.println(data.size() + ": " + String.format("%.2f%n",mean));
                }
            }
        } catch (Throwable t) {
            System.out.println("badness happened.");
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Total milliseconds: " + (endTime - startTime));
        System.out.println("Total minutes: " + (endTime - startTime) / (double) (1000 * 60));
    }
}
