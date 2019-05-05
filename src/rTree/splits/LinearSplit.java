package rTree.splits;

import rTree.Config;
import rTree.nodes.AbstractNode;
import rTree.nodes.ExternalNode;
import rTree.nodes.InternalNode;
import rTree.nodes.Rectangle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class LinearSplit implements Split {

    @Override
    public AbstractNode[] split(AbstractNode node) {
        if (node.isExternalNode()) {
            return splitLeaf((ExternalNode) node);
        }
        return splitNode((InternalNode) node);
    }

    // TODO: REFACTOR
    // TODO: REUSE THE INPUT NODE, CLEAR METHOD IN NODE
    @Override
    public AbstractNode[] splitLeaf(ExternalNode leaf) {
        // for each dimension (2) we maximize the separation between rectangles
        List<Rectangle> rectangles = leaf.rectangles;
        // width
        Rectangle maxLowerSide1 = getRectangleWithMaxLowerSide(rectangles, 1);
        Rectangle minUpperSide1 = getRectangleWithMinUpperSide(rectangles, 1);
        double normalizedSeparation1 = normalizeSeparation(rectangles, maxLowerSide1, minUpperSide1, 1);

        // height
        Rectangle maxLowerSide2 = getRectangleWithMaxLowerSide(rectangles, 2);
        Rectangle minUpperSide2 = getRectangleWithMinUpperSide(rectangles, 2);
        double normalizedSeparation2 = normalizeSeparation(rectangles, maxLowerSide2, minUpperSide2, 2);

        AbstractNode leaf1 = new ExternalNode();
        AbstractNode leaf2 = new ExternalNode();

        if (normalizedSeparation1 > normalizedSeparation2) {
            leaf1.addRectangle(maxLowerSide1);
            rectangles.remove(maxLowerSide1);
            leaf2.addRectangle(minUpperSide1);
            rectangles.remove(minUpperSide1);
        } else {
            leaf1.addRectangle(maxLowerSide2);
            rectangles.remove(maxLowerSide2);
            leaf2.addRectangle(minUpperSide2);
            rectangles.remove(minUpperSide2);
        }

        Random rand = new Random();
        while (!rectangles.isEmpty()) {
            Rectangle newRectangle = rectangles.get(rand.nextInt(rectangles.size()));

            int diffExternalNode1 = leaf1.mbr.minimumBoundingRectangle(newRectangle).differenceArea(leaf1.mbr);
            int diffExternalNode2 = leaf2.mbr.minimumBoundingRectangle(newRectangle).differenceArea(leaf2.mbr);

            if (diffExternalNode1 < diffExternalNode2) {
                leaf1.addRectangle(newRectangle);
            } else if (diffExternalNode1 > diffExternalNode2) {
                leaf2.addRectangle(newRectangle);
            } else { //same differenceArea
                //decide with min area
                if (leaf1.mbr.getArea() < leaf2.mbr.getArea())) {
                    leaf1.addRectangle(newRectangle);
                } else if (leaf1.mbr.getArea() > leaf2.mbr.getArea()) {
                    leaf2.addRectangle(newRectangle);
                } else { //still a draw, decide by number of MBRs
                    if (leaf1.rectangles.size() < leaf2.rectangles.size()) {
                        leaf1.addRectangle(newRectangle);
                    } else if (leaf1.rectangles.size() > leaf2.rectangles.size()) {
                        leaf2.addRectangle(newRectangle);
                    } else { // still draw, choose leaf1 just because
                        leaf1.addRectangle(newRectangle);
                    }
                }
            }

            rectangles.remove(newRectangle);

            // the other leaf have more than MIN_M rectangles already
            if (leaf1.getRemaining() == rectangles.size()) {
                for (Rectangle rectangle : rectangles) {
                    leaf1.addRectangle(rectangle);
                }
                rectangles.clear();

            } else if (leaf2.getRemaining() == rectangles.size()) {
                for (Rectangle rectangle : rectangles) {
                    leaf2.addRectangle(rectangle);
                }
                rectangles.clear();
            }
        }

        // optimized
        leaf1.writeToDisk();
        Config.DISK_ACCESSES++;
        leaf2.writeToDisk();
        Config.DISK_ACCESSES++;

        return new AbstractNode[]{leaf1, leaf2};
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
    public AbstractNode[] splitNode(InternalNode node) {
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
        Config.DISK_ACCESSES++;
        node2.writeToDisk();
        Config.DISK_ACCESSES++;

        return new AbstractNode[]{node1, node2};
    }
}
