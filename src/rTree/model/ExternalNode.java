package rTree.model;

import rTree.model.geometric.Rectangle;

public class ExternalNode extends AbstractNode {

    public ExternalNode() {
        super();
    }

    @Override
    public void addRectangle(Rectangle rectangle) {
        rectangles.add(rectangle);
        mbr = mbr.minimumBoundingRectangle(rectangle);
    }
}
