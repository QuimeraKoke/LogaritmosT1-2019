package rTree.model;

import rTree.model.geometric.Rectangle;
import java.util.List;

public class InternalNode extends AbstractNode {

	public InternalNode() {
        super();
    }
	
    public InternalNode(List<Integer> childrenIds, List<Rectangle> rectangles) {
        super(childrenIds, rectangles);
    }
    
    @Override
    public int getNElements() {
        return childrenIds.size();
    }
    
    @Override
    public boolean isExternalNode() {
        return false;
    }
    
    @Override
    public double[] getDiskUsage() {
        double nodeP = (double) (childrenIds.size()) / MAX_M;
        double totalNodes = (double) childrenIds.size();
        for (Integer id : childrenIds) {
            AbstractNode child = readFromDisk(id);
            double[] childrenStuff = child.getDiskUsage();
            nodeP += childrenStuff[0];
            totalNodes += childrenStuff[1];
        }
        return new double[]{nodeP, totalNodes};
    }
    
    @Override
    public void addNode(int nodeId, Rectangle rectMBR) {
        childrenIds.add(nodeId);
        rectangles.add(rectMBR);
        mbr = mbr.minimumBoundingRectangle(rectMBR);
    }

}
