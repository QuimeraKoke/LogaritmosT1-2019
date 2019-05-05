package rTree.nodes;

import java.util.List;

import rTree.Main;
import rTree.splits.Split;

import static rTree.Main.MAX_M;

public class InternalNode extends AbstractNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
            AbstractNode child = Main.readFromDisk(id);
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
    
    @Override
    public AbstractNode[] split(Split split) {
        return split.splitInternalNode(this);
    }

}
