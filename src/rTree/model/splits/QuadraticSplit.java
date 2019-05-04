package rTree.model.splits;

import rTree.Config;
import rTree.model.INode;
import rTree.model.InnerNode;
import rTree.model.Leaf;
import rTree.model.geometric.IRectangle;

import java.util.ArrayList;
import java.util.List;

import static rTree.Config.MAX_M;
import static rTree.Config.MIN_M;

@SuppressWarnings("Duplicates")
public class QuadraticSplit implements Split {

    @Override
    public INode[] split(INode node) {
        if (node.isLeaf()) {
            return splitLeaf((Leaf) node);
        }
        return splitNode((InnerNode) node);
    }

    @Override
    public INode[] splitLeaf(Leaf leaf) {
        List<IRectangle> rectangles = leaf.getRectangles();

        IRectangle[] maxFreeAreaRectanglePair = getMaxFreeArea(rectangles);

        INode leaf1 = new Leaf();
        INode leaf2 = new Leaf();

        leaf1.addRectangle(maxFreeAreaRectanglePair[0]);
        leaf2.addRectangle(maxFreeAreaRectanglePair[1]);
        rectangles.remove(maxFreeAreaRectanglePair[0]);
        rectangles.remove(maxFreeAreaRectanglePair[1]);

        int threshold = MAX_M - MIN_M + 1;

        while ((leaf1.getNElements() != threshold) && (leaf2.getNElements() != threshold) && !rectangles.isEmpty()) {

            List<Integer> d1 = new ArrayList<>();
            for (IRectangle rectangle : rectangles) {
                d1.add(leaf1.getMBR().differenceArea(leaf1.getMBR().minimumBoundingRectangle(rectangle)));
            }

            List<Integer> d2 = new ArrayList<>();
            for (IRectangle rectangle : rectangles) {
                d2.add(leaf2.getMBR().differenceArea(leaf2.getMBR().minimumBoundingRectangle(rectangle)));
            }

            IRectangle newRectangle = rectangles.get(getMaxDifferenceRectangleIndex(d1, d2));

            int diffLeaf1 = leaf1.getMBR().differenceArea(leaf1.getMBR().minimumBoundingRectangle(newRectangle));
            int diffLeaf2 = leaf2.getMBR().differenceArea(leaf2.getMBR().minimumBoundingRectangle(newRectangle));

            if (diffLeaf1 < diffLeaf2) {
                leaf1.addRectangle(newRectangle);
            } else if (diffLeaf1 > diffLeaf2) {
                leaf2.addRectangle(newRectangle);
            } else { //draw so check by min area
                if (leaf1.getMBR().area() < leaf2.getMBR().area()) {
                    leaf1.addRectangle(newRectangle);
                } else if (leaf1.getMBR().area() > leaf2.getMBR().area()) {
                    leaf2.addRectangle(newRectangle);
                } else { //draw so check by min number of MBRs
                    if (leaf1.getRectangles().size() < leaf2.getRectangles().size()) {
                        leaf1.addRectangle(newRectangle);
                    } else if (leaf1.getRectangles().size() > leaf2.getRectangles().size()) {
                        leaf2.addRectangle(newRectangle);
                    } else { //draw so choose leaf1 just because
                        leaf1.addRectangle(newRectangle);
                    }
                }
            }

            rectangles.remove(newRectangle);

        }
        // one node has M-m+1
        if (leaf1.getRectangles().size() == threshold) {
            for (IRectangle rectangle : rectangles) {
                leaf2.addRectangle(rectangle);
            }
        } else {
            for (IRectangle rectangle : rectangles) {
                leaf1.addRectangle(rectangle);
            }
        }

        leaf1.writeToDisk();
        Config.DISK_ACCESSES++;
        leaf2.writeToDisk();
        Config.DISK_ACCESSES++;

        return new INode[]{leaf1, leaf2};
    }

    private int getMaxDifferenceRectangleIndex(List<Integer> areaGrowth1, List<Integer> areaGrowth2) {
        int result = 0;
        int maxDiff = 0;
        for (int i = 0; i < areaGrowth1.size(); i++) {
            int possMaxDiff = Math.abs(areaGrowth1.get(i) - areaGrowth2.get(i));
            if (maxDiff < possMaxDiff) {
                maxDiff = possMaxDiff;
                result = i;
            }
        }
        return result;
    }

    private IRectangle[] getMaxFreeArea(List<IRectangle> rectangles) {
        int maxFreeArea = 0;
        int node1Index = 0;
        int node2Index = 0;

        for (int i = 0; i < rectangles.size(); i++) {
            int rect2Index = 0;
            int freeArea = 0;
            for (int j = i + 1; j < rectangles.size(); j++) {
                IRectangle candidateMBR = rectangles.get(i).minimumBoundingRectangle(rectangles.get(j));
                int candidateFreeArea = candidateMBR.area() - rectangles.get(i).area() - rectangles.get(j).area() +
                        rectangles.get(i).getIntersectionRect(rectangles.get(j)).area();
                if (freeArea < candidateFreeArea) {
                    freeArea = candidateFreeArea;
                    rect2Index = j;
                }
            }
            if (maxFreeArea < freeArea) {
                maxFreeArea = freeArea;
                node1Index = i;
                node2Index = rect2Index;
            }
        }

        return new IRectangle[]{rectangles.get(node1Index), rectangles.get(node2Index)};

    }

    @Override
    public INode[] splitNode(InnerNode node) {
        List<IRectangle> rectangles = node.getRectangles();
        List<Integer> childrenIds = node.getChildrenIds();

        IRectangle[] maxFreeAreaRectanglePair = getMaxFreeArea(rectangles);

        INode node1 = new InnerNode();
        INode node2 = new InnerNode();

        node1.addNode(childrenIds.get(rectangles.indexOf(maxFreeAreaRectanglePair[0])), maxFreeAreaRectanglePair[0]);
        node2.addNode(childrenIds.get(rectangles.indexOf(maxFreeAreaRectanglePair[1])), maxFreeAreaRectanglePair[1]);

        int threshold = MAX_M - MIN_M + 1;

        while ((node1.getNElements() != threshold) && (node2.getNElements() != threshold) && !rectangles.isEmpty()) {

            List<Integer> d1 = new ArrayList<>();
            for (IRectangle rectangle : rectangles) {
                d1.add(node1.getMBR().differenceArea(node1.getMBR().minimumBoundingRectangle(rectangle)));
            }

            List<Integer> d2 = new ArrayList<>();
            for (IRectangle rectangle : rectangles) {
                d2.add(node2.getMBR().differenceArea(node2.getMBR().minimumBoundingRectangle(rectangle)));
            }

            IRectangle newRectangle = rectangles.get(getMaxDifferenceRectangleIndex(d1, d2));

            int diffLeaf1 = node1.getMBR().differenceArea(node1.getMBR().minimumBoundingRectangle(newRectangle));
            int diffLeaf2 = node2.getMBR().differenceArea(node2.getMBR().minimumBoundingRectangle(newRectangle));
            int index = rectangles.indexOf(newRectangle);

            if (diffLeaf1 < diffLeaf2) {
                node1.addNode(childrenIds.get(index), newRectangle);
            } else if (diffLeaf1 > diffLeaf2) {
                node2.addNode(childrenIds.get(index), newRectangle);
            } else { //draw so check by min area
                if (node1.getMBR().area() < node2.getMBR().area()) {
                    node1.addNode(childrenIds.get(index), newRectangle);
                } else if (node1.getMBR().area() > node2.getMBR().area()) {
                    node2.addNode(childrenIds.get(index), newRectangle);
                } else { //draw so check by min number of MBRs
                    if (node1.getChildrenIds().size() < node2.getChildrenIds().size()) {
                        node1.addNode(childrenIds.get(index), newRectangle);
                    } else if (node1.getChildrenIds().size() > node2.getChildrenIds().size()) {
                        node2.addNode(childrenIds.get(index), newRectangle);
                    } else { //draw so choose leaf1 just because
                        node1.addNode(childrenIds.get(index), newRectangle);
                    }
                }
            }
            childrenIds.remove(index);
            rectangles.remove(newRectangle);

        }
        // one node has M-m+1
        if (node1.getChildrenIds().size() == threshold) {
            for (IRectangle rectangle : rectangles) {
                node2.addNode(childrenIds.get(rectangles.indexOf(rectangle)), rectangle);
            }
        } else {
            for (IRectangle rectangle : rectangles) {
                node1.addNode(childrenIds.get(rectangles.indexOf(rectangle)), rectangle);
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
