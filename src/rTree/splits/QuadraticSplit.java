package rTree.splits;

import rTree.nodes.AbstractNode;
import rTree.nodes.InternalNode;
import rTree.nodes.ExternalNode;
import rTree.nodes.Rectangle;

import java.util.ArrayList;
import java.util.List;

import rTree.Main;

public class QuadraticSplit implements Split {

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

    @Override
    public AbstractNode[] splitExternalNode(ExternalNode ExternalNode) {
        List<Rectangle> rectangles = ExternalNode.rectangles;

        Rectangle[] maxFreeAreaRectanglePair = getMaxFreeArea(rectangles);

        AbstractNode ExternalNode1 = new ExternalNode();
        AbstractNode ExternalNode2 = new ExternalNode();

        ExternalNode1.addRectangle(maxFreeAreaRectanglePair[0]);
        ExternalNode2.addRectangle(maxFreeAreaRectanglePair[1]);
        rectangles.remove(maxFreeAreaRectanglePair[0]);
        rectangles.remove(maxFreeAreaRectanglePair[1]);

        int threshold = Main.MAX_M - Main.MIN_M + 1;

        while ((ExternalNode1.getNElements() != threshold) && (ExternalNode2.getNElements() != threshold) && !rectangles.isEmpty()) {

            List<Integer> d1 = new ArrayList<>();
            for (Rectangle rectangle : rectangles) {
                d1.add(ExternalNode1.mbr.differenceArea(ExternalNode1.mbr.minimumBoundingRectangle(rectangle)));
            }

            List<Integer> d2 = new ArrayList<>();
            for (Rectangle rectangle : rectangles) {
                d2.add(ExternalNode2.mbr.differenceArea(ExternalNode2.mbr.minimumBoundingRectangle(rectangle)));
            }

            Rectangle newRectangle = rectangles.get(getMaxDifferenceRectangleIndex(d1, d2));

            int diffExternalNode1 = ExternalNode1.mbr.differenceArea(ExternalNode1.mbr.minimumBoundingRectangle(newRectangle));
            int diffExternalNode2 = ExternalNode2.mbr.differenceArea(ExternalNode2.mbr.minimumBoundingRectangle(newRectangle));

            if (diffExternalNode1 < diffExternalNode2) {
                ExternalNode1.addRectangle(newRectangle);
            } else if (diffExternalNode1 > diffExternalNode2) {
                ExternalNode2.addRectangle(newRectangle);
            } else { //draw so check by min area
                if (ExternalNode1.mbr.getArea() < ExternalNode2.mbr.getArea()) {
                    ExternalNode1.addRectangle(newRectangle);
                } else if (ExternalNode1.mbr.getArea() > ExternalNode2.mbr.getArea()) {
                    ExternalNode2.addRectangle(newRectangle);
                } else { //draw so check by min number of MBRs
                    if (ExternalNode1.rectangles.size() < ExternalNode2.rectangles.size()) {
                        ExternalNode1.addRectangle(newRectangle);
                    } else if (ExternalNode1.rectangles.size() > ExternalNode2.rectangles.size()) {
                        ExternalNode2.addRectangle(newRectangle);
                    } else { //draw so choose ExternalNode1 just because
                        ExternalNode1.addRectangle(newRectangle);
                    }
                }
            }

            rectangles.remove(newRectangle);

        }
        // one node has M-m+1
        if (ExternalNode1.rectangles.size() == threshold) {
            for (Rectangle rectangle : rectangles) {
                ExternalNode2.addRectangle(rectangle);
            }
        } else {
            for (Rectangle rectangle : rectangles) {
                ExternalNode1.addRectangle(rectangle);
            }
        }

        ExternalNode1.writeToDisk();
        Main.DISK_ACCESSES++;
        ExternalNode2.writeToDisk();
        Main.DISK_ACCESSES++;

        return new AbstractNode[]{ExternalNode1, ExternalNode2};
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

    private Rectangle[] getMaxFreeArea(List<Rectangle> rectangles) {
        int maxFreeArea = 0;
        int node1Index = 0;
        int node2Index = 0;

        for (int i = 0; i < rectangles.size(); i++) {
            int rect2Index = 0;
            int freeArea = 0;
            for (int j = i + 1; j < rectangles.size(); j++) {
                Rectangle candidateMBR = rectangles.get(i).minimumBoundingRectangle(rectangles.get(j));
                int candidateFreeArea = candidateMBR.getArea() - rectangles.get(i).getArea() - rectangles.get(j).getArea() +
                        rectangles.get(i).getIntersectionRect(rectangles.get(j)).getArea();
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

        return new Rectangle[]{rectangles.get(node1Index), rectangles.get(node2Index)};

    }

    @Override
    public AbstractNode[] splitInternalNode(InternalNode node) {
        List<Rectangle> rectangles = node.rectangles;
        List<Integer> childrenIds = node.childrenIds;

        Rectangle[] maxFreeAreaRectanglePair = getMaxFreeArea(rectangles);

        AbstractNode node1 = new InternalNode();
        AbstractNode node2 = new InternalNode();

        node1.addNode(childrenIds.get(rectangles.indexOf(maxFreeAreaRectanglePair[0])), maxFreeAreaRectanglePair[0]);
        node2.addNode(childrenIds.get(rectangles.indexOf(maxFreeAreaRectanglePair[1])), maxFreeAreaRectanglePair[1]);

        int threshold = Main.MAX_M - Main.MIN_M + 1;

        while ((node1.getNElements() != threshold) && (node2.getNElements() != threshold) && !rectangles.isEmpty()) {

            List<Integer> d1 = new ArrayList<>();
            for (Rectangle rectangle : rectangles) {
                d1.add(node1.mbr.differenceArea(node1.mbr.minimumBoundingRectangle(rectangle)));
            }

            List<Integer> d2 = new ArrayList<>();
            for (Rectangle rectangle : rectangles) {
                d2.add(node2.mbr.differenceArea(node2.mbr.minimumBoundingRectangle(rectangle)));
            }

            Rectangle newRectangle = rectangles.get(getMaxDifferenceRectangleIndex(d1, d2));

            int diffExternalNode1 = node1.mbr.differenceArea(node1.mbr.minimumBoundingRectangle(newRectangle));
            int diffExternalNode2 = node2.mbr.differenceArea(node2.mbr.minimumBoundingRectangle(newRectangle));
            int index = rectangles.indexOf(newRectangle);

            if (diffExternalNode1 < diffExternalNode2) {
                node1.addNode(childrenIds.get(index), newRectangle);
            } else if (diffExternalNode1 > diffExternalNode2) {
                node2.addNode(childrenIds.get(index), newRectangle);
            } else { //draw so check by min area
                if (node1.mbr.getArea() < node2.mbr.getArea()) {
                    node1.addNode(childrenIds.get(index), newRectangle);
                } else if (node1.mbr.getArea() > node2.mbr.getArea()) {
                    node2.addNode(childrenIds.get(index), newRectangle);
                } else { //draw so check by min number of MBRs
                    if (node1.childrenIds.size() < node2.childrenIds.size()) {
                        node1.addNode(childrenIds.get(index), newRectangle);
                    } else if (node1.childrenIds.size() > node2.childrenIds.size()) {
                        node2.addNode(childrenIds.get(index), newRectangle);
                    } else { //draw so choose ExternalNode1 just because
                        node1.addNode(childrenIds.get(index), newRectangle);
                    }
                }
            }
            childrenIds.remove(index);
            rectangles.remove(newRectangle);

        }
        // one node has M-m+1
        if (node1.childrenIds.size() == threshold) {
            for (Rectangle rectangle : rectangles) {
                node2.addNode(childrenIds.get(rectangles.indexOf(rectangle)), rectangle);
            }
        } else {
            for (Rectangle rectangle : rectangles) {
                node1.addNode(childrenIds.get(rectangles.indexOf(rectangle)), rectangle);
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
