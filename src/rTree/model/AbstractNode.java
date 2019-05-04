package rTree.model;

import rTree.model.geometric.Rectangle;
import rTree.model.geometric.Utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import static rTree.Config.MAX_M;
import static rTree.Config.MIN_M;


public abstract class AbstractNode implements INode {

    List<Integer> childrenIds;
    List<Rectangle> rectangles;
    Rectangle mbr;
    private int id;


    AbstractNode(List<Integer> children, List<Rectangle> rectangles) {
        this.childrenIds = children;
        id = IdGenerator.nextId();
        this.rectangles = rectangles;
        updateMBR();
    }

    public static INode readFromDisk(int id) {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(RTree.DIR + "n" + id + ".node"));
            return (INode) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    AbstractNode() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public Rectangle getMBR() {
        return mbr;
    }

    @Override
    public List<Integer> getChildrenIds() {
        return childrenIds;
    }

    @Override
    public List<Rectangle> getRectangles() {
        return rectangles;
    }

    @Override
    public boolean isOverflow() {
        return getNElements() > MAX_M;
    }

    @Override
    public abstract void addNode(int nodeId, Rectangle rectMBR);

    @Override
    public abstract void addRectangle(Rectangle rectangle);

    @Override
    public void updateMBR() {
        mbr = Utils.minBoundingRectangle(rectangles);
    }

    @Override
    public abstract int getNElements();

    @Override
    public int getRemaining() {
        return MIN_M - getNElements();
    }

    @Override
    public void writeToDisk() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(RTree.DIR + "n" + id + ".node"));
            out.writeObject(this);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
