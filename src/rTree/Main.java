package rTree;

import rTree.nodes.AbstractNode;
import rTree.nodes.Rectangle;
import rTree.splits.LinearSplit;
import rTree.splits.QuadraticSplit;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    public final static int MAX_M = 160;
    public final static int MIN_M = (int) (MAX_M * 0.4);
    public static int DISK_ACCESSES = 0;
    
    public static final String DIR = "data" + File.separator;
	public static final File FILE = new File(DIR + "id");

    private static final int N = (int) Math.pow(2, 14);

    public static void main(String[] args) {
    	
        long startTime;
        long endTime;
        List<Rectangle> rectangles = new ArrayList<>();
        List<Rectangle> randomrectangles = new ArrayList<>();
        Random rand = new Random();
        
    	// Random rectangles (size N)
        for (int i = 0; i < N; i++) {
            rectangles.add(new Rectangle());
        }
        
        // Random selection of rectangles for search (size N/10)
        for (int i = 0; i < (int) N / 10; i++) {
        	randomrectangles.add(rectangles.get(rand.nextInt(rectangles.size())));            
        }
        
        /* 
         * Linear Split 
         * 
         */
        RTree linearRTree = new RTree(new LinearSplit());
        // Warm up with N/10
        for (int i = 0; i < (int) N / 10; i++) {
            linearRTree.insert(rectangles.get(i));            
        }
        linearRTree.clear();
        
        // Insertion
        startTime = System.currentTimeMillis();
        for (Rectangle rectangle : rectangles) {
            linearRTree.insert(rectangle);
        }
        endTime = System.currentTimeMillis();
        
        System.out.println("#### Linear Split ####");
        System.out.println("Tiempo promedio por inserción con LinearSplit: " + ((endTime - startTime) / (rectangles.size() * 1.0)) + " milisegundos");
        System.out.println("N° total de accesos a disco: " + DISK_ACCESSES);
        System.out.println("Tiempo total de construcción por inserción con LinearSplit: " + (endTime - startTime) + " milisegundos");
        System.out.println("Espacio utilizado en disco: " + linearRTree.getRoot().getDiskUsage());
        System.out.println("Porcentaje de llenado de disco:");
        System.out.println("");
        
        DISK_ACCESSES = 0;
        
        // Search
        startTime = System.currentTimeMillis();
        for (Rectangle rrectangle : randomrectangles) {
        	@SuppressWarnings("unused")
			List<Rectangle> resultrect = linearRTree.search(rrectangle);        
        }
        endTime = System.currentTimeMillis();
        System.out.println("Tiempo promedio por búsqueda con LinearSplit: " + ((endTime - startTime) / (N / 10)) + " milisegundos");
        System.out.println("N° total de accesos a disco: " + DISK_ACCESSES);
        System.out.println("Tiempo total de búsquedas con LinearSplit: " + (endTime - startTime) + " milisegundos");
        System.out.println("");
        DISK_ACCESSES = 0;
        
        /* 
         * Quadratic Split 
         * 
         */
        RTree quadraticRTree = new RTree(new QuadraticSplit());
        // Warm up with N/10
        for (int i = 0; i < (int) N / 10; i++) {
            quadraticRTree.insert(rectangles.get(i));
        }
        quadraticRTree.clear();
        
        // Insertion
        startTime = System.currentTimeMillis();
        for (Rectangle rectangle : rectangles) {
            quadraticRTree.insert(rectangle);
        }
        endTime = System.currentTimeMillis();
     
        System.out.println("#### Quadratic Split ####");
        System.out.println("Tiempo promedio por inserción con QuadraticSplit: " + ((endTime - startTime) / (rectangles.size() * 1.0)) + " milisegundos");
        System.out.println("N° total de accesos a disco: " + DISK_ACCESSES);
        System.out.println("Tiempo total de construcción por inserción con QuadraticSplit: " + (endTime - startTime) + " milisegundos");
        System.out.println("Espacio utilizado en disco: " + quadraticRTree.getRoot().getDiskUsage());
        System.out.println("Porcentaje de llenado de disco:");
        System.out.println("");
        
        DISK_ACCESSES = 0;
        
        // Search
        startTime = System.currentTimeMillis();
        for (Rectangle rrectangle : randomrectangles) {
        	@SuppressWarnings("unused")
			List<Rectangle> resultrect = quadraticRTree.search(rrectangle);        
        }
        endTime = System.currentTimeMillis();
        
        System.out.println("Tiempo promedio por búsqueda con QuadraticSplit: " + ((endTime - startTime) / (N / 10)) + " milisegundos");
        System.out.println("N° total de accesos a disco: " + DISK_ACCESSES);
        System.out.println("Tiempo total de búsquedas con QuadraticSplit: " + (endTime - startTime) + " milisegundos");

    }
    
    public static AbstractNode readFromDisk(int id) {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(DIR + "n" + id + ".node"));
            return (AbstractNode)in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
}
