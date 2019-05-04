package rTree;


import rTree.model.RTree;
import rTree.model.geometric.Rectangle;
import rTree.model.splits.QuadraticSplit;
import rTree.model.splits.LinearSplit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.FileWriter;
import java.io.File;
import java.text.DecimalFormat;


public class MainTest {

    private static final int RANGE_ORIGIN = 500000;
    private static final int RANGE_LENGTH = 100;

    private static final String TEST_DIR = "tests" + File.separator;

    private static final DecimalFormat f = new DecimalFormat("###.###");

    //Delimiter used in CSV file
    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";
    //CSV file header
    private static final String INSERT_FILE_HEADER = "n,accesses,usage,time";
    private static final String SEARCH_FILE_HEADER = "n,accesses,time,lookups";

    private static Rectangle randomRectangle() {
        Random rand = new Random();
        return new Rectangle(rand.nextInt(RANGE_ORIGIN), rand.nextInt(RANGE_ORIGIN),
                rand.nextInt(RANGE_LENGTH) + 1, rand.nextInt(RANGE_LENGTH) + 1);
    }

    public static void main(String[] args) {

        int insertionDiskCounterLinear = 0;
        int lookUpDiskCounterLinear;

        int insertionDiskCounterQuadratic = 0;
        int lookUpDiskCounterQuadratic;

        int warmupLimit = (int) Math.pow(2, 12);

        long startTimeLinear;
        long startTimeQuadratic;
        long endTimeLinear;
        long endTimeQuadratic;

        long totalInsertionTimeLinear = 0;
        long totalInsertionTimeQuadratic = 0;

        long totalLookUpTimeLinear;
        long totalLookUpTimeQuadratic;
        double[] diskUsage;


        List<String> linearInsertionTestText = new ArrayList<>();
        List<String> linearLookUpTestText = new ArrayList<>();
        List<String> quadraticInsertionTestText = new ArrayList<>();
        List<String> quadraticLookUpTestText = new ArrayList<>();

        // list of n's to test
        List<Integer> nList = new ArrayList<>();

        RTree linearRTree = new RTree(new LinearSplit());
        RTree quadraticRTree = new RTree(new QuadraticSplit());


        Rectangle rectangle;

        nList.add(0);
        for(int n = 9; n <= 25; n++) {
            nList.add((int) Math.pow(2, n));
        }

        // insert warm up

        System.out.println("Starting insertion warm up");

        for (int i = 0; i < warmupLimit; i++) {
            rectangle = randomRectangle();
            linearRTree.insert(rectangle);
            quadraticRTree.insert(rectangle);
        }
        /*
        System.out.println("Starting search warm up");

        for (int i = 0; i < (warmupLimit / 10); i++) {
            rectangle = randomRectangle();
            linearRTree.search(rectangle);
            quadraticRTree.search(rectangle);
        }
           */
        System.out.println("End of warm up");

        linearRTree.clear();
        quadraticRTree.clear();


        FileWriter linear_insert_fw = null;
        FileWriter linear_lookup_fw = null;
        FileWriter quadratic_insert_fw = null;
        FileWriter quadratic_lookup_fw = null;

        String linear_file_line;
        String quadratic_file_line;

        try {

            // Change nList.size() if you prefer less
            // k < 10 -> 2^17
            for(int k = 1; k < 10; k++) {

                int currentRange = nList.get(k) - nList.get(k - 1);
                for (int i = 0; i < currentRange; i++) {
                    Config.DISK_ACCESSES = 0;

                    rectangle = randomRectangle();

                    startTimeLinear = System.nanoTime();
                    linearRTree.insert(rectangle);
                    endTimeLinear = System.nanoTime();

                    insertionDiskCounterLinear += Config.DISK_ACCESSES;

                    totalInsertionTimeLinear += (endTimeLinear - startTimeLinear);

                    Config.DISK_ACCESSES = 0;

                    startTimeQuadratic = System.nanoTime();
                    quadraticRTree.insert(rectangle);
                    endTimeQuadratic = System.nanoTime();

                    totalInsertionTimeQuadratic += (endTimeQuadratic - startTimeQuadratic);

                    insertionDiskCounterQuadratic += Config.DISK_ACCESSES;
                }

                diskUsage = linearRTree.getRoot().getDiskUsage();
                // Save results in list to write them later

                linear_file_line = "" + nList.get(k)
                        + COMMA_DELIMITER + insertionDiskCounterLinear
                        + COMMA_DELIMITER + f.format((diskUsage[0] / (diskUsage[1] + 1)))
                        + COMMA_DELIMITER + f.format((totalInsertionTimeLinear / 1000000.0))
                        + NEW_LINE_SEPARATOR;


                diskUsage = quadraticRTree.getRoot().getDiskUsage();

                quadratic_file_line = "" + nList.get(k)
                        + COMMA_DELIMITER + insertionDiskCounterQuadratic
                        + COMMA_DELIMITER + f.format((diskUsage[0] / (diskUsage[1] + 1)))
                        + COMMA_DELIMITER +f.format ((totalInsertionTimeQuadratic / 1000000.0))
                        + NEW_LINE_SEPARATOR;


                linearInsertionTestText.add(linear_file_line);
                quadraticInsertionTestText.add(quadratic_file_line);

                System.out.println("Insertions done :" + nList.get(k));


                // Search test

                totalLookUpTimeLinear = 0;
                lookUpDiskCounterLinear = 0;
                totalLookUpTimeQuadratic = 0;
                lookUpDiskCounterQuadratic = 0;

                for (int j = 0; j < (nList.get(k) / 10); j++) {
                    rectangle = randomRectangle();

                    Config.DISK_ACCESSES = 0;

                    startTimeLinear = System.nanoTime();
                    linearRTree.search(rectangle);
                    endTimeLinear = System.nanoTime();

                    totalLookUpTimeLinear += (endTimeLinear - startTimeLinear);
                    lookUpDiskCounterLinear += Config.DISK_ACCESSES;

                    Config.DISK_ACCESSES = 0;

                    startTimeQuadratic = System.nanoTime();
                    quadraticRTree.search(rectangle);
                    endTimeQuadratic = System.nanoTime();

                    totalLookUpTimeQuadratic += (endTimeQuadratic - startTimeQuadratic);
                    lookUpDiskCounterQuadratic += Config.DISK_ACCESSES;
                }

                // Save results in list to write them later

                linear_file_line = "" + nList.get(k)
                        + COMMA_DELIMITER + lookUpDiskCounterLinear
                        + COMMA_DELIMITER + f.format ((totalLookUpTimeLinear / 1000000.0))
                        + COMMA_DELIMITER + (nList.get(k) / 10)
                        + NEW_LINE_SEPARATOR;

                quadratic_file_line = "" + nList.get(k)
                        + COMMA_DELIMITER + lookUpDiskCounterQuadratic
                        + COMMA_DELIMITER + f.format((totalLookUpTimeQuadratic / 1000000.0))
                        + COMMA_DELIMITER + (nList.get(k) / 10)
                        + NEW_LINE_SEPARATOR;


                linearLookUpTestText.add(linear_file_line);
                quadraticLookUpTestText.add(quadratic_file_line);

                System.out.println("Lookups done :" + (nList.get(k) / 10));

            }

            // Write to csv

            linear_insert_fw = new FileWriter(TEST_DIR + "linear_insert.csv");
            quadratic_insert_fw = new FileWriter(TEST_DIR + "quadratic_insert.csv");

            linear_lookup_fw = new FileWriter(TEST_DIR + "linear_lookup.csv");
            quadratic_lookup_fw = new FileWriter(TEST_DIR + "quadratic_lookup.csv");

            linear_insert_fw.append(INSERT_FILE_HEADER);
            linear_insert_fw.append(NEW_LINE_SEPARATOR);

            linear_lookup_fw.append(SEARCH_FILE_HEADER);
            linear_lookup_fw.append(NEW_LINE_SEPARATOR);

            quadratic_insert_fw.append(INSERT_FILE_HEADER);
            quadratic_insert_fw.append(NEW_LINE_SEPARATOR);

            quadratic_lookup_fw.append(SEARCH_FILE_HEADER);
            quadratic_lookup_fw.append(NEW_LINE_SEPARATOR);

            for(int i = 0; i < linearInsertionTestText.size(); i++) {
                linear_insert_fw.append(linearInsertionTestText.get(i));
                linear_lookup_fw.append(linearLookUpTestText.get(i));
                quadratic_insert_fw.append(quadraticInsertionTestText.get(i));
                quadratic_lookup_fw.append(quadraticLookUpTestText.get(i));
            }

        } catch (Exception e) {
            System.out.println("Error writing to files");
            e.printStackTrace();
        } finally {
            try {
                linear_insert_fw.flush();
                linear_insert_fw.close();
                linear_lookup_fw.flush();
                linear_lookup_fw.close();
                quadratic_insert_fw.flush();
                quadratic_insert_fw.close();
                quadratic_lookup_fw.flush();
                quadratic_lookup_fw.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriters");
                e.printStackTrace();
            }
        }

    }
}
