package rTree.nodes;

import rTree.splits.Split;

public class ExternalNode extends AbstractNode {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExternalNode() {
        super();
    }

    @Override
    public void addRectangle(Rectangle rectangle) {
        rectangles.add(rectangle);
        mbr = mbr.minimumBoundingRectangle(rectangle);
    }
    
    @Override
    public AbstractNode[] split(Split split) {
        return split.splitExternalNode(this);
    }
}
