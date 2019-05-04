package rTree;

import rTree.model.RTree;
import rTree.model.geometric.Rectangle;
import rTree.model.splits.QuadraticSplit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    public final static int MAX_M = 160;
    public final static int MIN_M = (int) (MAX_M * 0.4);
    public static int DISK_ACCESSES = 0;

    private static final int RANGE_ORIGIN = 500000;
    private static final int RANGE_LENGTH = 100;

    public static Rectangle randomRectangle() {
        Random rand = new Random();
        return new Rectangle(rand.nextInt(RANGE_ORIGIN), rand.nextInt(RANGE_ORIGIN),
                rand.nextInt(RANGE_LENGTH) + 1, rand.nextInt(RANGE_LENGTH) + 1);
    }

    public static void main(String[] args) {
        /*
        Experiments here
         */
        int benchLimit = (int) Math.pow(2, 25);

        List<Rectangle> rectangles = new ArrayList<>();
        for (int i = 0; i < benchLimit; i++) {
            rectangles.add(randomRectangle());
        }
        System.out.println(rectangles.size());

        RTree linearRTree = new RTree(new QuadraticSplit());
        // warm up
        for (int i = 0; i < (int) (benchLimit * 0.1); i++) {
            linearRTree.insert(rectangles.get(i));
        }
        linearRTree.clear();

        long startTime = System.currentTimeMillis();
        for (Rectangle rectangle : rectangles) {
            linearRTree.insert(rectangle);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Total time: " + (endTime - startTime) + "ms");
        System.out.println("Average insertion time: " + ((double) (endTime - startTime) / rectangles.size()) + "ms");

        Random rand = new Random();
        List<Rectangle> found = linearRTree.search(rectangles.get(rand.nextInt(rectangles.size())));
        System.out.println(found);

        System.out.println("Acceso promedio a disco: " + ((double) (DISK_ACCESSES) / benchLimit));
        System.out.println("Acceso promedio a disco: " + linearRTree.getRoot().getDiskUsage());

        //RTree quadraticRTree = new RTree(new QuadraticSplit());

    }
}
