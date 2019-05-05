package rTree.splits;

import rTree.Config;
import rTree.model.INode;
import rTree.model.InnerNode;
import rTree.model.Leaf;
import rTree.model.geometric.IRectangle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@SuppressWarnings("Duplicates")
public class LinearSplit implements Split {

    @Override
    public INode[] split(INode node) {
        if (node.isLeaf()) {
            return splitLeaf((Leaf) node);
        }
        return splitNode((InnerNode) node);
    }

    // TODO: REFACTOR
    // TODO: REUSE THE INPUT NODE, CLEAR METHOD IN NODE
    @Override
    public INode[] splitLeaf(Leaf leaf) {
        // for each dimension (2) we maximize the separation between rectangles
        List<IRectangle> rectangles = leaf.getRectangles();
        // width
        IRectangle maxLowerSide1 = getRectangleWithMaxLowerSide(rectangles, 1);
        IRectangle minUpperSide1 = getRectangleWithMinUpperSide(rectangles, 1);
        double normalizedSeparation1 = normalizeSeparation(rectangles, maxLowerSide1, minUpperSide1, 1);

        // height
        IRectangle maxLowerSide2 = getRectangleWithMaxLowerSide(rectangles, 2);
        IRectangle minUpperSide2 = getRectangleWithMinUpperSide(rectangles, 2);
        double normalizedSeparation2 = normalizeSeparation(rectangles, maxLowerSide2, minUpperSide2, 2);

        INode leaf1 = new Leaf();
        INode leaf2 = new Leaf();

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
            IRectangle newRectangle = rectangles.get(rand.nextInt(rectangles.size()));

            int diffLeaf1 = leaf1.getMBR().minimumBoundingRectangle(newRectangle).differenceArea(leaf1.getMBR());
            int diffLeaf2 = leaf2.getMBR().minimumBoundingRectangle(newRectangle).differenceArea(leaf2.getMBR());

            if (diffLeaf1 < diffLeaf2) {
                leaf1.addRectangle(newRectangle);
            } else if (diffLeaf1 > diffLeaf2) {
                leaf2.addRectangle(newRectangle);
            } else { //same differenceArea
                //decide with min area
                if (leaf1.getMBR().area() < leaf2.getMBR().area()) {
                    leaf1.addRectangle(newRectangle);
                } else if (leaf1.getMBR().area() > leaf2.getMBR().area()) {
                    leaf2.addRectangle(newRectangle);
                } else { //still a draw, decide by number of MBRs
                    if (leaf1.getRectangles().size() < leaf2.getRectangles().size()) {
                        leaf1.addRectangle(newRectangle);
                    } else if (leaf1.getRectangles().size() > leaf2.getRectangles().size()) {
                        leaf2.addRectangle(newRectangle);
                    } else { // still draw, choose leaf1 just because
                        leaf1.addRectangle(newRectangle);
                    }
                }
            }

            rectangles.remove(newRectangle);

            // the other leaf have more than MIN_M rectangles already
            if (leaf1.getRemaining() == rectangles.size()) {
                for (IRectangle rectangle : rectangles) {
                    leaf1.addRectangle(rectangle);
                }
                rectangles.clear();

            } else if (leaf2.getRemaining() == rectangles.size()) {
                for (IRectangle rectangle : rectangles) {
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

        return new INode[]{leaf1, leaf2};
    }

    private IRectangle getRectangleWithMaxLowerSide(List<IRectangle> rectangles, int dimension) {

        List<Integer> maxLowerList = new ArrayList<>();
        for (IRectangle rectangle : rectangles) {
            maxLowerList.add(rectangle.getDimension(dimension)[0]);
        }
        int maxLowerIndex = maxLowerList.indexOf(Collections.max(maxLowerList));
        return rectangles.get(maxLowerIndex);
    }

    private IRectangle getRectangleWithMinUpperSide(List<IRectangle> rectangles, int dimension) {
        List<Integer> minUpperList = new ArrayList<>();
        for (IRectangle rectangle : rectangles) {
            minUpperList.add(rectangle.getDimension(dimension)[1]);
        }
        int minUpperIndex = minUpperList.indexOf(Collections.min(minUpperList));
        return rectangles.get(minUpperIndex);
    }

    private double normalizeSeparation(List<IRectangle> rectangles, IRectangle maxLowerSide,
                                       IRectangle minUpperSide, int dimension) {
        List<Integer> maxUpperList = new ArrayList<>();
        List<Integer> minLowerList = new ArrayList<>();
        for (IRectangle rectangle : rectangles) {
            minLowerList.add(rectangle.getDimension(dimension)[0]);
            maxUpperList.add(rectangle.getDimension(dimension)[1]);
        }

        int minLower = Collections.min(minLowerList);
        int maxUpper = Collections.max(maxUpperList);
        return (double) Math.abs(maxLowerSide.getDimension(dimension)[0] - minUpperSide.getDimension(dimension)[1]) /
                Math.abs(maxUpper - minLower);
    }

    @Override
    public INode[] splitNode(InnerNode node) {
        List<Integer> childrenIds = node.getChildrenIds();
        // for each dimension (2) we maximize the separation between rectangles
        List<IRectangle> rectangles = node.getRectangles();
        // width
        IRectangle maxLowerSide1 = getRectangleWithMaxLowerSide(rectangles, 1);
        IRectangle minUpperSide1 = getRectangleWithMinUpperSide(rectangles, 1);
        double normalizedSeparation1 = normalizeSeparation(rectangles, maxLowerSide1, minUpperSide1, 1);

        // height
        IRectangle maxLowerSide2 = getRectangleWithMaxLowerSide(rectangles, 2);
        IRectangle minUpperSide2 = getRectangleWithMinUpperSide(rectangles, 2);
        double normalizedSeparation2 = normalizeSeparation(rectangles, maxLowerSide2, minUpperSide2, 2);

        INode node1 = new InnerNode();
        INode node2 = new InnerNode();

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
            IRectangle newRectangle = rectangles.get(rand.nextInt(rectangles.size()));

            int diffNode1 = node1.getMBR().minimumBoundingRectangle(newRectangle).differenceArea(node1.getMBR());
            int diffNode2 = node2.getMBR().minimumBoundingRectangle(newRectangle).differenceArea(node2.getMBR());

            if (diffNode1 < diffNode2) {
                node1.addNode(childrenIds.get(rectangles.indexOf(newRectangle)), newRectangle);
            } else if (diffNode1 > diffNode2) {
                node2.addNode(childrenIds.get(rectangles.indexOf(newRectangle)), newRectangle);
            } else { //same differenceArea
                //decide with min area
                if (node1.getMBR().area() < node2.getMBR().area()) {
                    node1.addNode(childrenIds.get(rectangles.indexOf(newRectangle)), newRectangle);
                } else if (node1.getMBR().area() > node2.getMBR().area()) {
                    node2.addNode(childrenIds.get(rectangles.indexOf(newRectangle)), newRectangle);
                } else { //still a draw, decide by number of MBRs
                    if (node1.getChildrenIds().size() < node2.getChildrenIds().size()) {
                        node1.addNode(childrenIds.get(rectangles.indexOf(newRectangle)), newRectangle);
                    } else if (node1.getChildrenIds().size() > node2.getChildrenIds().size()) {
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
                for (IRectangle rectangle : rectangles) {
                    node1.addNode(childrenIds.get(rectangles.indexOf(rectangle)), rectangle);
                }

            } else if (node2.getRemaining() == childrenIds.size()) {
                for (IRectangle rectangle : rectangles) {
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

        return new INode[]{node1, node2};
    }
}
