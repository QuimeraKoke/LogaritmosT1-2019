package rTree.model;

import rTree.Config;
import rTree.model.geometric.IRectangle;

public class Leaf extends AbstractNode {

    public Leaf() {
        super();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public void addNode(int nodeId, IRectangle rectangle) {
        // do nothing
    }

    @Override
    public void addRectangle(IRectangle rectangle) {
        rectangles.add(rectangle);
        mbr = mbr.minimumBoundingRectangle(rectangle);
    }

    @Override
    public int getNElements() {
        return rectangles.size();
    }

    @Override
    public double[] getDiskUsage() {
        return new double[] {(double) (rectangles.size()) / Config.MAX_M, 0};
    }
}
