package rTree.splits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import rTree.Main;
import rTree.nodes.AbstractNode;
import rTree.nodes.ExternalNode;
import rTree.nodes.InternalNode;
import rTree.nodes.Rectangle;

public class LinearSplit implements Split {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public AbstractNode[] split(AbstractNode node) {
        if (node.isExternalNode()) {
            return splitExternalNode((ExternalNode) node);
        }
        return splitInternalNode((InternalNode) node);
    }

    // TODO: REFACTOR
    // TODO: REUSE THE INPUT NODE, CLEAR METHOD IN NODE
    @Override
    public AbstractNode[] splitExternalNode(ExternalNode ExternalNode) {
        // for each dimension (2) we maximize the separation between rectangles
        List<Rectangle> rectangles = ExternalNode.rectangles;
        // width
        Rectangle maxLowerSide1 = getRectangleWithMaxLowerSide(rectangles, 1);
        Rectangle minUpperSide1 = getRectangleWithMinUpperSide(rectangles, 1);
        double normalizedSeparation1 = normalizeSeparation(rectangles, maxLowerSide1, minUpperSide1, 1);

        // height
        Rectangle maxLowerSide2 = getRectangleWithMaxLowerSide(rectangles, 2);
        Rectangle minUpperSide2 = getRectangleWithMinUpperSide(rectangles, 2);
        double normalizedSeparation2 = normalizeSeparation(rectangles, maxLowerSide2, minUpperSide2, 2);

        AbstractNode ExternalNode1 = new ExternalNode();
        AbstractNode ExternalNode2 = new ExternalNode();

        if (normalizedSeparation1 > normalizedSeparation2) {
            ExternalNode1.addRectangle(maxLowerSide1);
            rectangles.remove(maxLowerSide1);
            ExternalNode2.addRectangle(minUpperSide1);
            rectangles.remove(minUpperSide1);
        } else {
            ExternalNode1.addRectangle(maxLowerSide2);
            rectangles.remove(maxLowerSide2);
            ExternalNode2.addRectangle(minUpperSide2);
            rectangles.remove(minUpperSide2);
        }

        Random rand = new Random();
        while (!rectangles.isEmpty()) {
            Rectangle newRectangle = rectangles.get(rand.nextInt(rectangles.size()));

            int diffExternalNode1 = ExternalNode1.mbr.minimumBoundingRectangle(newRectangle).differenceArea(ExternalNode1.mbr);
            int diffExternalNode2 = ExternalNode2.mbr.minimumBoundingRectangle(newRectangle).differenceArea(ExternalNode2.mbr);

            if (diffExternalNode1 < diffExternalNode2) {
                ExternalNode1.addRectangle(newRectangle);
            } else if (diffExternalNode1 > diffExternalNode2) {
                ExternalNode2.addRectangle(newRectangle);
            } else { //same differenceArea
                //decide with min area
                if (ExternalNode1.mbr.getArea() < ExternalNode2.mbr.getArea()) {
                    ExternalNode1.addRectangle(newRectangle);
                } else if (ExternalNode1.mbr.getArea() > ExternalNode2.mbr.getArea()) {
                    ExternalNode2.addRectangle(newRectangle);
                } else { //still a draw, decide by number of MBRs
                    if (ExternalNode1.rectangles.size() < ExternalNode2.rectangles.size()) {
                        ExternalNode1.addRectangle(newRectangle);
                    } else if (ExternalNode1.rectangles.size() > ExternalNode2.rectangles.size()) {
                        ExternalNode2.addRectangle(newRectangle);
                    } else { // still draw, choose ExternalNode1 just because
                        ExternalNode1.addRectangle(newRectangle);
                    }
                }
            }

            rectangles.remove(newRectangle);

            // the other ExternalNode have more than MIN_M rectangles already
            if (ExternalNode1.getRemaining() == rectangles.size()) {
                for (Rectangle rectangle : rectangles) {
                    ExternalNode1.addRectangle(rectangle);
                }
                rectangles.clear();

            } else if (ExternalNode2.getRemaining() == rectangles.size()) {
                for (Rectangle rectangle : rectangles) {
                    ExternalNode2.addRectangle(rectangle);
                }
                rectangles.clear();
            }
        }

        // optimized
        ExternalNode1.writeToDisk();
        Main.DISK_ACCESSES++;
        ExternalNode2.writeToDisk();
        Main.DISK_ACCESSES++;

        return new AbstractNode[]{ExternalNode1, ExternalNode2};
    }

    private Rectangle getRectangleWithMaxLowerSide(List<Rectangle> rectangles, int dimension) {

        List<Integer> maxLowerList = new ArrayList<>();
        for (Rectangle rectangle : rectangles) {
            maxLowerList.add(rectangle.getDimension(dimension)[0]);
        }
        int maxLowerIndex = maxLowerList.indexOf(Collections.max(maxLowerList));
        return rectangles.get(maxLowerIndex);
    }

    private Rectangle getRectangleWithMinUpperSide(List<Rectangle> rectangles, int dimension) {
        List<Integer> minUpperList = new ArrayList<>();
        for (Rectangle rectangle : rectangles) {
            minUpperList.add(rectangle.getDimension(dimension)[1]);
        }
        int minUpperIndex = minUpperList.indexOf(Collections.min(minUpperList));
        return rectangles.get(minUpperIndex);
    }

    private double normalizeSeparation(List<Rectangle> rectangles, Rectangle maxLowerSide,
                                       Rectangle minUpperSide, int dimension) {
        List<Integer> maxUpperList = new ArrayList<>();
        List<Integer> minLowerList = new ArrayList<>();
        for (Rectangle rectangle : rectangles) {
            minLowerList.add(rectangle.getDimension(dimension)[0]);
            maxUpperList.add(rectangle.getDimension(dimension)[1]);
        }

        int minLower = Collections.min(minLowerList);
        int maxUpper = Collections.max(maxUpperList);
        return (double) Math.abs(maxLowerSide.getDimension(dimension)[0] - minUpperSide.getDimension(dimension)[1]) /
                Math.abs(maxUpper - minLower);
    }

    @Override
    public AbstractNode[] splitInternalNode(InternalNode node) {
        List<Integer> childrenIds = node.childrenIds;
        // for each dimension (2) we maximize the separation between rectangles
        List<Rectangle> rectangles = node.rectangles;
        // width
        Rectangle maxLowerSide1 = getRectangleWithMaxLowerSide(rectangles, 1);
        Rectangle minUpperSide1 = getRectangleWithMinUpperSide(rectangles, 1);
        double normalizedSeparation1 = normalizeSeparation(rectangles, maxLowerSide1, minUpperSide1, 1);

        // height
        Rectangle maxLowerSide2 = getRectangleWithMaxLowerSide(rectangles, 2);
        Rectangle minUpperSide2 = getRectangleWithMinUpperSide(rectangles, 2);
        double normalizedSeparation2 = normalizeSeparation(rectangles, maxLowerSide2, minUpperSide2, 2);

        AbstractNode node1 = new InternalNode();
        AbstractNode node2 = new InternalNode();

        if (normalizedSeparation1 > normalizedSeparation2) {

            node1.addNode(childrenIds.get(rectangles.indexOf(maxLowerSide1)), maxLowerSide1);
            childrenIds.remove(childrenIds.get(rectangles.indexOf(maxLowerSide1)));
            rectangles.remove(maxLowerSide1);

            node2.addNode(childrenIds.get(rectangles.indexOf(minUpperSide1)), minUpperSide1);
            childrenIds.remove(childrenIds.get(rectangles.indexOf(minUpperSide1)));
            rectangles.remove(minUpperSide1);

        } else {
            node1.addNode(childrenIds.get(rectangles.indexOf(maxLowerSide2)), maxLowerSide2);
            childrenIds.remove(childrenIds.get(rectangles.indexOf(maxLowerSide2)));
            rectangles.remove(maxLowerSide2);

            node2.addNode(childrenIds.get(rectangles.indexOf(minUpperSide2)), minUpperSide2);
            childrenIds.remove(childrenIds.get(rectangles.indexOf(minUpperSide2)));
            rectangles.remove(minUpperSide2);
        }

        Random rand = new Random();
        while (!childrenIds.isEmpty()) {
            Rectangle newRectangle = rectangles.get(rand.nextInt(rectangles.size()));

            int diffNode1 = node1.mbr.minimumBoundingRectangle(newRectangle).differenceArea(node1.mbr);
            int diffNode2 = node2.mbr.minimumBoundingRectangle(newRectangle).differenceArea(node2.mbr);

            if (diffNode1 < diffNode2) {
                node1.addNode(childrenIds.get(rectangles.indexOf(newRectangle)), newRectangle);
            } else if (diffNode1 > diffNode2) {
                node2.addNode(childrenIds.get(rectangles.indexOf(newRectangle)), newRectangle);
            } else { //same differenceArea
                //decide with min area
                if (node1.mbr.getArea() < node2.mbr.getArea()) {
                    node1.addNode(childrenIds.get(rectangles.indexOf(newRectangle)), newRectangle);
                } else if (node1.mbr.getArea() > node2.mbr.getArea()) {
                    node2.addNode(childrenIds.get(rectangles.indexOf(newRectangle)), newRectangle);
                } else { //still a draw, decide by number of MBRs
                    if (node1.childrenIds.size() < node2.childrenIds.size()) {
                        node1.addNode(childrenIds.get(rectangles.indexOf(newRectangle)), newRectangle);
                    } else if (node1.childrenIds.size() > node2.childrenIds.size()) {
                        node2.addNode(childrenIds.get(rectangles.indexOf(newRectangle)), newRectangle);
                    } else { // still draw, choose node1 just because
                        node1.addNode(childrenIds.get(rectangles.indexOf(newRectangle)), newRectangle);
                    }
                }
            }
            childrenIds.remove(rectangles.indexOf(newRectangle));
            rectangles.remove(newRectangle);

            // the other node have more than MIN_M rectangles already
            if (node1.getRemaining() == childrenIds.size()) {
                for (Rectangle rectangle : rectangles) {
                    node1.addNode(childrenIds.get(rectangles.indexOf(rectangle)), rectangle);
                }

            } else if (node2.getRemaining() == childrenIds.size()) {
                for (Rectangle rectangle : rectangles) {
                    node2.addNode(childrenIds.get(rectangles.indexOf(rectangle)), rectangle);
                }
            }
        }
        childrenIds.clear();
        rectangles.clear();

        node1.writeToDisk();
        Main.DISK_ACCESSES++;
        node2.writeToDisk();
        Main.DISK_ACCESSES++;

        return new AbstractNode[]{node1, node2};
    }
}
