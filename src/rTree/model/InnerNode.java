package rTree.model;

import rTree.Config;
import rTree.model.geometric.Rectangle;

import java.util.List;

public class InnerNode extends AbstractNode {

    public InnerNode(List<Integer> childrenIds, List<Rectangle> rectangles) {
        super(childrenIds, rectangles);
    }

    public InnerNode() {
        super();
    }

    @Override
    public void addNode(int nodeId, Rectangle rectMBR) {
        childrenIds.add(nodeId);
        rectangles.add(rectMBR);
        mbr = mbr.minimumBoundingRectangle(rectMBR);
    }

    @Override
    public void addRectangle(Rectangle rectangle) {
        // do nothing
    }

    @Override
    public int getNElements() {
        return childrenIds.size();
    }

    @Override
    public double[] getDiskUsage() {
        double nodeP = (double) (childrenIds.size()) / Config.MAX_M;
        double totalNodes = (double) childrenIds.size();
        for (Integer id : childrenIds) {
            INode child = this.readFromDisk(id);
            double[] childrenStuff = child.getDiskUsage();
            nodeP += childrenStuff[0];
            totalNodes += childrenStuff[1];
        }
        return new double[]{nodeP, totalNodes};
    }

}
