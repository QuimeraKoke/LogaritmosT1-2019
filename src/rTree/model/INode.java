package rTree.model;

import rTree.model.geometric.Rectangle;

import java.io.Serializable;
import java.util.List;

public interface INode extends Serializable {

    boolean isLeaf();

    Rectangle getMBR();

    //List<INode> getChildrenIds();
    List<Integer> getChildrenIds();

    List<Rectangle> getRectangles();

    boolean isOverflow();

    //void addNode(INode node);
    void addNode(int nodeId, Rectangle rectangle);

    void addRectangle(Rectangle rectangle);

    void updateMBR();

    int getRemaining();

    int getId();

    int getNElements();

    void writeToDisk();

    double[] getDiskUsage();
}
