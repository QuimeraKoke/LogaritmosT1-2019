package rTree.nodes;

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
