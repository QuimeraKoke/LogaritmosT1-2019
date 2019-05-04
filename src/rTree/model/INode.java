package rTree.model;

import rTree.model.geometric.IRectangle;

import java.io.Serializable;
import java.util.List;

public interface INode extends Serializable {

    boolean isLeaf();

    IRectangle getMBR();

    //List<INode> getChildrenIds();
    List<Integer> getChildrenIds();

    List<IRectangle> getRectangles();

    boolean isOverflow();

    //void addNode(INode node);
    void addNode(int nodeId, IRectangle rectangle);

    void addRectangle(IRectangle rectangle);

    void updateMBR();

    int getRemaining();

    int getId();

    int getNElements();

    void writeToDisk();

    double[] getDiskUsage();
}
