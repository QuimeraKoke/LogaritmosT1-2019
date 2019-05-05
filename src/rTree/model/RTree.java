package rTree.model;

import rTree.Config;
import rTree.model.geometric.Rectangle;
import rTree.model.splits.Split;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class RTree implements Serializable {
    private Split heuristic;

    private int rootId;
    public static final String DIR = "data" + File.separator;


    public RTree(Split heuristic) {
        INode root = new Leaf();
        this.heuristic = heuristic;
        rootId = root.getId();
        root.writeToDisk();
        Config.DISK_ACCESSES++;
    }

    public RTree(Split heuristic, INode root) {
        rootId = root.getId();
        this.heuristic = heuristic;
        root.writeToDisk();
        Config.DISK_ACCESSES++;
    }

    public AbstractNode getRoot() {
        return AbstractNode.readFromDisk(rootId);
    }

    public List<Rectangle> search(Rectangle rectangle) {
        return searchIn(rootId, rectangle);
    }

    private List<Rectangle> searchIn(int nodeId, Rectangle rectangle) {

        List<Rectangle> results = new ArrayList<>();
        INode node = AbstractNode.readFromDisk(nodeId);
        Config.DISK_ACCESSES++;

        if (node.getMBR().intersects(rectangle)) {
            if (node.isLeaf()) {
                return node.getRectangles().stream().
                        filter(rect -> rect.intersects(rectangle)).
                        collect(Collectors.toList());
            } else {
                for (Integer childId : node.getChildrenIds()) {
                    results.addAll(searchIn(childId, rectangle));
                }
            }
        }

        return results;
    }

    public void insert(Rectangle rectangle) {

        INode[] newNodes = insertIn(rootId, rectangle);
        if (newNodes[1] == null) {
            rootId = newNodes[0].getId();
        } else {
            INode newRoot = new InnerNode(new ArrayList<>(Arrays.asList(newNodes[0].getId(), newNodes[1].getId())),
                    new ArrayList<>(Arrays.asList(newNodes[0].getMBR(), newNodes[1].getMBR())));
            rootId = newRoot.getId();
            newRoot.writeToDisk();
            Config.DISK_ACCESSES++;
        }
    }

    private INode[] insertIn(int nodeId, Rectangle rectangle) {

        INode node = AbstractNode.readFromDisk(nodeId);
        Config.DISK_ACCESSES++;

        // node is a leaf, we just insert
        if (node.isLeaf()) {
            node.addRectangle(rectangle);

        } else {

            int originalIndex;
            INode[] theNewNodes;

            List<Integer> differences = new ArrayList<>();

            List<Rectangle> childMBRs = node.getRectangles();

            // get all new possible MBRs and get how much they would grow
            for (Rectangle childMBR : childMBRs) {
                Rectangle newMBR = childMBR.minimumBoundingRectangle(rectangle);
                differences.add(childMBR.differenceArea(newMBR));
            }
            // get the minimum growth
            int minGrowth = Collections.min(differences);
            //get all indexes with that minimum growth
            List<Integer> indexesOfMin = indexOfAll(minGrowth, differences);

            // if there is more than 1 possible node in which to insert
            if (indexesOfMin.size() > 1) {
                // decide by the minimum area
                List<Integer> areas = new ArrayList<>();
                for (Integer index : indexesOfMin) {
                    areas.add(childMBRs.get(index).getArea());
                }
                // get the minimum area
                int minArea = Collections.min(areas);
                //get all indexes with that minimum area
                List<Integer> indexesOfMinArea = indexOfAll(minArea, areas);

                // if there is still a draw
                if (indexesOfMinArea.size() > 1) {
                    // choose a random one
                    Random rand = new Random();
                    int randomIndex = rand.nextInt(indexesOfMinArea.size());

                    originalIndex = indexesOfMin.get(indexesOfMinArea.get(randomIndex));
                    //newNodeId = insertIn(node.getChildrenIds().get(originalIndex), rectangle);
                    theNewNodes = insertIn(node.getChildrenIds().get(originalIndex), rectangle);

                } else { //only one min area
                    // get the original index
                    originalIndex = indexesOfMin.get(indexesOfMinArea.get(0));
                    //newNodeId = insertIn(node.getChildrenIds().get(originalIndex), rectangle);
                    theNewNodes = insertIn(node.getChildrenIds().get(originalIndex), rectangle);
                }

            } else { //only one minimum
                // override the old node
                originalIndex = indexesOfMin.get(0);
                //newNodeId = insertIn(node.getChildrenIds().get(originalIndex), rectangle);
                theNewNodes = insertIn(node.getChildrenIds().get(originalIndex), rectangle);

            }

            // update the changes
            node.getChildrenIds().set(originalIndex, theNewNodes[0].getId());
            node.getRectangles().set(originalIndex, theNewNodes[0].getMBR());
            //check if there is a new node
            if (theNewNodes[1] != null) {
                node.addNode(theNewNodes[1].getId(), theNewNodes[1].getMBR());
            }

            // update the MBR
            node.updateMBR();
        }

        node.writeToDisk();
        Config.DISK_ACCESSES++;

        // TODO: FIX PLS
        INode[] newNodes = new INode[]{node, null};
        // check if overflow
        if (node.isOverflow()) {
            newNodes = heuristic.split(node);
        }

        return newNodes;
    }

    public void clear() {
        INode root = new Leaf();
        rootId = root.getId();
        root.writeToDisk();
    }

    private List<Integer> indexOfAll(int number, List<Integer> list) {
        List<Integer> indexList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++)
            if (number == list.get(i))
                indexList.add(i);
        return indexList;
    }

}
