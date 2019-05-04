package rTree.model;

import rTree.model.geometric.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import static rTree.Main.MAX_M;
import static rTree.Main.MIN_M;

public abstract class AbstractNode {
    
	public static final String DIR = "data" + File.separator;
	public static final File FILE = new File(DIR + "id");
	
	int id;
	Rectangle mbr;
    List<Integer> childrenIds;
    List<Rectangle> rectangles;
    
    AbstractNode() {
        this(new ArrayList<>(), new ArrayList<>());
    }
    
    AbstractNode(List<Integer> children, List<Rectangle> rectangles) {
    	id = newId();
        this.childrenIds = children;
        this.rectangles = rectangles;
        updateMBR();
    }

    public static int newId() {
        try {
            int result = 0;
            if (FILE.exists()) {
                FileInputStream fis = new FileInputStream(FILE);
                ObjectInputStream in = new ObjectInputStream(fis);
                result = in.read();
                in.close();
            }
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE));
            out.writeInt(result + 1);
            out.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return 0;
        }
    }
    
    public int getNElements() {
        return rectangles.size();
    }
    
    public boolean isOverflow() {
        return getNElements() > MAX_M;
    }
    
    public int getRemaining() {
        return MIN_M - getNElements();
    }
    
    public boolean isExternalNode() {
        return true;
    }
    
    public static AbstractNode readFromDisk(int id) {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(DIR + "n" + id + ".node"));
            AbstractNode noderead = (AbstractNode)in.readObject();
            in.close();
            return noderead; 
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    
    public void writeToDisk() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DIR + "n" + id + ".node"));
            out.writeObject(this);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public double[] getDiskUsage() {
        return new double[] {(double) (rectangles.size()) / MAX_M, 0};
    }

    public void addNode(int nodeId, Rectangle rectMBR) {};

    public void addRectangle(Rectangle rectangle) {};

    public void updateMBR() {
        mbr = new Rectangle(0,0,0,0);
        if (!rectangles.isEmpty()) {
            mbr = rectangles.get(0);
            if (rectangles.size() > 1) {
                for (int i = 1; i < rectangles.size(); i++) {
                    mbr = mbr.minimumBoundingRectangle(rectangles.get(i));
                }
            }
        }
    }    

}
